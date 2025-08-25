@file:OptIn(ExperimentalPermissionsApi::class)

package com.junkfood.seal.ui.page.settings.directory

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SdCardAlert
import androidx.compose.material.icons.outlined.FolderDelete
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.junkfood.seal.App
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.PreferenceInfo
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.ui.component.PreferenceSubtitle
import com.junkfood.seal.ui.component.PreferencesHintCard
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.FileUtil.getConfigDirectory
import com.junkfood.seal.util.FileUtil.getExternalTempDir
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun String.isValidDirectory(): Boolean {
    val publicDownloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
    val publicDocumentDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).path
    return isEmpty() || contains(publicDownloadsDirectory) || contains(publicDocumentDirectory)
}

enum class Directory {
    AUDIO,
    VIDEO,
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DownloadDirectoryPreferences(onNavigateBack: () -> Unit) {

    val scope = rememberCoroutineScope()
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(),
            canScroll = { true },
        )
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var videoDirectoryText by remember { mutableStateOf(App.videoDownloadDir) }
    var audioDirectoryText by remember { mutableStateOf(App.audioDownloadDir) }

    var showClearTempDialog by remember { mutableStateOf(false) }
    var editingDirectory by remember { mutableStateOf(Directory.VIDEO) }

    val storagePermission =
        rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val showDirectoryAlert =
        Build.VERSION.SDK_INT >= 30 &&
                !Environment.isExternalStorageManager() &&
                (!audioDirectoryText.isValidDirectory() ||
                        !videoDirectoryText.isValidDirectory())

    val launcher =
        rememberLauncherForActivityResult(
            object : ActivityResultContracts.OpenDocumentTree() {
                override fun createIntent(context: Context, input: Uri?): Intent {
                    return (super.createIntent(context, input)).apply {
                        flags =
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    }
                }
            }
        ) {
            it?.let { uri ->
                App.updateDownloadDir(uri, editingDirectory)
                val path = FileUtil.getRealPath(uri)
                when (editingDirectory) {
                    Directory.AUDIO -> audioDirectoryText = path
                    Directory.VIDEO -> videoDirectoryText = path
                }
            }
        }

    fun openDirectoryChooser(directory: Directory = Directory.VIDEO) {
        editingDirectory = directory
        if (Build.VERSION.SDK_INT > 29 || storagePermission.status == PermissionStatus.Granted)
            launcher.launch(null)
        else storagePermission.launchPermissionRequest()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = {
            SnackbarHost(modifier = Modifier.systemBarsPadding(), hostState = snackbarHostState)
        },
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        modifier = Modifier,
                        text = stringResource(id = R.string.download_directory),
                    )
                },
                navigationIcon = { BackButton { onNavigateBack() } },
                scrollBehavior = scrollBehavior,
            )
        },
    ) {
        LazyColumn(modifier = Modifier, contentPadding = it) {
            if (CUSTOM_COMMAND.getBoolean())
                item {
                    PreferenceInfo(text = stringResource(id = R.string.custom_command_enabled_hint))
                }

            if (showDirectoryAlert)
                item {
                    PreferencesHintCard(
                        title = stringResource(R.string.permission_issue),
                        description = stringResource(R.string.permission_issue_desc),
                        icon = Icons.Filled.SdCardAlert,
                    ) {
                        if (
                            Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()
                        ) {
                            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                data = Uri.parse("package:" + context.packageName)
                                if (resolveActivity(context.packageManager) != null)
                                    context.startActivity(this)
                            }
                        }
                    }
                }
            item { PreferenceSubtitle(text = stringResource(R.string.general_settings)) }
            item {
                PreferenceItem(
                    title = stringResource(id = R.string.video_directory),
                    description = videoDirectoryText,
                    icon = Icons.Outlined.VideoLibrary,
                ) {
                    openDirectoryChooser(directory = Directory.VIDEO)
                }
            }
            item {
                PreferenceItem(
                    title = stringResource(id = R.string.audio_directory),
                    description = audioDirectoryText,
                    icon = Icons.Outlined.LibraryMusic,
                ) {
                    openDirectoryChooser(directory = Directory.AUDIO)
                }
            }
            /*
            // REMOVED: Custom command directory
            item {
                PreferenceItem(
                    title = stringResource(id = R.string.custom_command_directory),
                    description =
                    customCommandDirectory.ifEmpty {
                        stringResource(id = R.string.set_directory_desc)
                    },
                    icon = Icons.Outlined.Folder,
                ) {
                    showCustomCommandDirectoryDialog = true
                }
            }
            */
            /*
            // REMOVED: SD card folder
            item {
                PreferenceSwitchWithDivider(
                    title = stringResource(id = R.string.sdcard_directory),
                    description =
                    sdcardUri.ifEmpty { stringResource(id = R.string.set_directory_desc) },
                    isChecked = sdcardDownload,
                    enabled = !isCustomCommandEnabled,
                    isSwitchEnabled = !isCustomCommandEnabled,
                    onChecked = {
                        if (sdcardUri.isNotEmpty()) {
                            sdcardDownload = !sdcardDownload
                            PreferenceUtil.updateValue(SDCARD_DOWNLOAD, sdcardDownload)
                        } else {
                            openDirectoryChooser(Directory.SDCARD)
                        }
                    },
                    icon = Icons.Outlined.SdCard,
                    onClick = { openDirectoryChooser(Directory.SDCARD) },
                )
            }
            */
            /*
            // REMOVED: Save to subdirectory
            item {
                PreferenceItem(
                    title = stringResource(id = R.string.subdirectory),
                    description = stringResource(id = R.string.subdirectory_desc),
                    icon = Icons.Outlined.SnippetFolder,
                    enabled = !isCustomCommandEnabled && !sdcardDownload,
                ) {
                    showSubdirectoryDialog = true
                }
            }
            */
            /*
            // REMOVED: Private directory
            item { PreferenceSubtitle(text = stringResource(R.string.privacy)) }
            item {
                PreferenceSwitch(
                    title = stringResource(id = R.string.private_directory),
                    description = stringResource(R.string.private_directory_desc),
                    icon = Icons.Outlined.TabUnselected,
                    enabled = !showDirectoryAlert && !sdcardDownload && !isCustomCommandEnabled,
                    isChecked = isPrivateDirectoryEnabled,
                    onClick = {
                        isPrivateDirectoryEnabled = !isPrivateDirectoryEnabled
                        PreferenceUtil.updateValue(PRIVATE_DIRECTORY, isPrivateDirectoryEnabled)
                    },
                )
            }
            */
            item { PreferenceSubtitle(text = stringResource(R.string.advanced_settings)) }
            /*
            // REMOVED: Output template
            item {
                PreferenceItem(
                    title = stringResource(R.string.output_template),
                    description = stringResource(id = R.string.output_template_desc),
                    icon = Icons.Outlined.FolderSpecial,
                    enabled = !isCustomCommandEnabled && !sdcardDownload,
                    onClick = { showOutputTemplateDialog = true },
                )
            }
            */
            /*
            // REMOVED: Restrict filenames
            item {
                var restrictFilenames by RESTRICT_FILENAMES.booleanState
                PreferenceSwitch(
                    title = stringResource(id = R.string.restrict_filenames),
                    icon = Icons.Outlined.Spellcheck,
                    description = stringResource(id = R.string.restrict_filenames_desc),
                    isChecked = restrictFilenames,
                ) {
                    restrictFilenames = !restrictFilenames
                    RESTRICT_FILENAMES.updateBoolean(restrictFilenames)
                }
            }
            */
            item {
                PreferenceItem(
                    title = stringResource(R.string.clear_temp_files),
                    description = stringResource(R.string.clear_temp_files_desc),
                    icon = Icons.Outlined.FolderDelete,
                    onClick = { showClearTempDialog = true },
                )
            }
        }
    }

    if (showClearTempDialog) {
        AlertDialog(
            onDismissRequest = { showClearTempDialog = false },
            icon = { Icon(Icons.Outlined.FolderDelete, null) },
            title = { Text(stringResource(id = R.string.clear_temp_files)) },
            dismissButton = { DismissButton { showClearTempDialog = false } },
            text = {
                Text(
                    stringResource(
                        R.string.clear_temp_files_info,
                        getExternalTempDir().absolutePath,
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            confirmButton = {
                ConfirmButton {
                    showClearTempDialog = false
                    scope.launch(Dispatchers.IO) {
                        FileUtil.clearTempFiles(context.getConfigDirectory())
                        val count =
                            FileUtil.run {
                                clearTempFiles(getExternalTempDir()) +
                                        clearTempFiles(context.getSdcardTempDir(null)) +
                                        clearTempFiles(context.getInternalTempDir())
                            }

                        withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.clear_temp_files_count).format(count)
                            )
                        }
                    }
                }
            },
        )
    }
}