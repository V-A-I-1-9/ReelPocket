package com.junkfood.seal.ui.page.settings.general

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.HistoryToggleOff
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PlaylistAddCheck
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.junkfood.seal.App
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.booleanState
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.PreferenceInfo
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.ui.component.PreferenceSubtitle
import com.junkfood.seal.ui.component.PreferenceSwitch
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.DOWNLOAD_ARCHIVE
import com.junkfood.seal.util.NOTIFICATION
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.PLAYLIST
import com.junkfood.seal.util.PRIVATE_MODE
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.ToastUtil
import com.junkfood.seal.util.UpdateUtil
import com.junkfood.seal.util.YT_DLP_VERSION
import com.yausername.youtubedl_android.YoutubeDL
import com.junkfood.seal.ui.page.download.NotificationPermissionDialog
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import kotlinx.coroutines.launch


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GeneralDownloadPreferences(onNavigateBack: () -> Unit, navigateToTemplate: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showYtdlpDialog by remember { mutableStateOf(false) }
    var isUpdating by remember { mutableStateOf(false) }

    var downloadPlaylist by remember { mutableStateOf(PLAYLIST.getBoolean()) }
    var downloadNotification by remember { mutableStateOf(NOTIFICATION.getBoolean()) }
    var isPrivateModeEnabled by remember { mutableStateOf(PRIVATE_MODE.getBoolean()) }

    var isNotificationPermissionGranted by remember {
        mutableStateOf(NotificationUtil.areNotificationsEnabled())
    }

    val notificationPermission =
        if (Build.VERSION.SDK_INT >= 33)
            rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS) { status ->
                if (!status) ToastUtil.makeToast(context.getString(R.string.permission_denied))
                else isNotificationPermissionGranted = true
            }
        else null

    var showNotificationDialog by remember { mutableStateOf(false) }

    val storagePermission =
        rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE)

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(),
            canScroll = { true },
        )
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = stringResource(id = R.string.general_settings)) },
                navigationIcon = { BackButton { onNavigateBack() } },
                scrollBehavior = scrollBehavior,
            )
        },
        content = {
            val isCustomCommandEnabled by remember { mutableStateOf(CUSTOM_COMMAND.getBoolean()) }
            LazyColumn(modifier = Modifier, contentPadding = it) {
                if (isCustomCommandEnabled)
                    item {
                        PreferenceInfo(
                            text = stringResource(id = R.string.custom_command_enabled_hint)
                        )
                    }
                item {
                    var ytdlpVersion by remember {
                        mutableStateOf(
                            YoutubeDL.getInstance().version(context.applicationContext)
                                ?: context.getString(R.string.ytdlp_update)
                        )
                    }
                    PreferenceItem(
                        title = stringResource(id = R.string.ytdlp_update_action),
                        description = ytdlpVersion,
                        leadingIcon = {
                            if (isUpdating) UpdateProgressIndicator()
                            else {
                                Icon(
                                    imageVector = Icons.Outlined.Update,
                                    contentDescription = null,
                                    modifier =
                                        Modifier
                                            .padding(start = 8.dp, end = 16.dp)
                                            .size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        onClick = {
                            scope.launch {
                                runCatching {
                                    isUpdating = true
                                    UpdateUtil.updateYtDlp()
                                    ytdlpVersion = YT_DLP_VERSION.getString()
                                }
                                    .onFailure { th ->
                                        th.printStackTrace()
                                        ToastUtil.makeToastSuspend(
                                            App.context.getString(R.string.yt_dlp_update_fail)
                                        )
                                    }
                                    .onSuccess {
                                        ToastUtil.makeToastSuspend(
                                            context.getString(R.string.yt_dlp_up_to_date) +
                                                    " (${YT_DLP_VERSION.getString()})"
                                        )
                                    }
                                isUpdating = false
                            }
                        },
                        onClickLabel = stringResource(id = R.string.update),
                        trailingIcon = {
                            IconButton(onClick = { showYtdlpDialog = true }) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = stringResource(id = R.string.open_settings),
                                )
                            }
                        },
                    )
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.download_notification),
                        description =
                            stringResource(
                                id =
                                    if (isNotificationPermissionGranted)
                                        R.string.download_notification_desc
                                    else R.string.permission_denied
                            ),
                        icon =
                            if (!isNotificationPermissionGranted) Icons.Outlined.NotificationsOff
                            else if (!downloadNotification) Icons.Outlined.Notifications
                            else Icons.Outlined.NotificationsActive,
                        isChecked = downloadNotification && isNotificationPermissionGranted,
                        onClick = {
                            if (notificationPermission?.status is PermissionStatus.Denied) {
                                showNotificationDialog = true
                            } else if (isNotificationPermissionGranted) {
                                if (downloadNotification) NotificationUtil.cancelAllNotifications()
                                downloadNotification = !downloadNotification
                                PreferenceUtil.updateValue(NOTIFICATION, downloadNotification)
                            }
                        },
                    )
                }

                /*
                // REMOVED: Configure before download - Not functional after UI simplification
                item {
                    var configureBeforeDownload by CONFIGURE.booleanState
                    PreferenceSwitch(
                        title = stringResource(id = R.string.settings_before_download),
                        description = stringResource(id = R.string.settings_before_download_desc),
                        icon =
                        if (configureBeforeDownload) Icons.Outlined.DoneAll
                        else Icons.Outlined.RemoveDone,
                        isChecked = configureBeforeDownload,
                        onClick = {
                            configureBeforeDownload = !configureBeforeDownload
                            PreferenceUtil.updateValue(CONFIGURE, configureBeforeDownload)
                        },
                    )
                }
                */

                /*
                // REMOVED: Save thumbnail - Non-essential feature
                item {
                    var thumbnailSwitch by remember { mutableStateOf(THUMBNAIL.getBoolean()) }
                    PreferenceSwitch(
                        title = stringResource(id = R.string.create_thumbnail),
                        description = stringResource(id = R.string.create_thumbnail_summary),
                        enabled = !isCustomCommandEnabled,
                        icon = Icons.Outlined.Image,
                        isChecked = thumbnailSwitch,
                        onClick = {
                            thumbnailSwitch = !thumbnailSwitch
                            PreferenceUtil.updateValue(THUMBNAIL, thumbnailSwitch)
                        },
                    )
                }
                */

                /*
                // REMOVED: Detailed output - Debug feature
                item {
                    var displayErrorReport by DEBUG.booleanState
                    PreferenceSwitch(
                        title = stringResource(R.string.print_details),
                        description = stringResource(R.string.print_details_desc),
                        icon =
                        if (displayErrorReport) Icons.Outlined.Print
                        else Icons.Outlined.PrintDisabled,
                        enabled = !isCustomCommandEnabled,
                        onClick = {
                            displayErrorReport = !displayErrorReport
                            PreferenceUtil.updateValue(DEBUG, displayErrorReport)
                        },
                        isChecked = displayErrorReport,
                    )
                }
                */


                item { PreferenceSubtitle(text = stringResource(id = R.string.privacy)) }

                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.private_mode),
                        description = stringResource(R.string.private_mode_desc),
                        icon =
                            if (isPrivateModeEnabled) Icons.Outlined.HistoryToggleOff
                            else Icons.Outlined.History,
                        isChecked = isPrivateModeEnabled,
                        enabled = !isCustomCommandEnabled,
                        onClick = {
                            isPrivateModeEnabled = !isPrivateModeEnabled
                            PreferenceUtil.updateValue(PRIVATE_MODE, isPrivateModeEnabled)
                        },
                    )
                }

                /*
                // REMOVED: Disable preview - Non-essential feature
                item {
                    var isPreviewDisabled by remember { mutableStateOf(DISABLE_PREVIEW.getBoolean()) }
                    PreferenceSwitch(
                        title = stringResource(R.string.disable_preview),
                        description = stringResource(R.string.disable_preview_desc),
                        icon =
                        if (isPreviewDisabled) Icons.Outlined.VisibilityOff
                        else Icons.Outlined.Visibility,
                        isChecked = isPreviewDisabled,
                        enabled = !isCustomCommandEnabled,
                        onClick = {
                            isPreviewDisabled = !isPreviewDisabled
                            PreferenceUtil.updateValue(DISABLE_PREVIEW, isPreviewDisabled)
                        },
                    )
                }
                */

                item { PreferenceSubtitle(text = stringResource(R.string.advanced_settings)) }
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.download_playlist),
                        onClick = {
                            downloadPlaylist = !downloadPlaylist
                            PreferenceUtil.updateValue(PLAYLIST, downloadPlaylist)
                        },
                        icon = Icons.Outlined.PlaylistAddCheck,
                        enabled = !isCustomCommandEnabled,
                        description = stringResource(R.string.download_playlist_desc),
                        isChecked = downloadPlaylist,
                    )
                }

                /*
                // REMOVED: Download archive - Advanced feature
                item {
                    var useDownloadArchive by DOWNLOAD_ARCHIVE.booleanState
                    var showClearArchiveDialog by remember { mutableStateOf(false) }
                    var archiveFileContent by remember { mutableStateOf("") }
                    PreferenceSwitchWithDivider(
                        title = stringResource(id = R.string.download_archive),
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                archiveFileContent = context.getArchiveFile().readText()
                                withContext(Dispatchers.Main) { showClearArchiveDialog = true }
                            }
                        },
                        icon = Icons.Outlined.Archive,
                        description = stringResource(R.string.download_archive_desc),
                        isChecked = useDownloadArchive,
                        onChecked = {
                            useDownloadArchive = !useDownloadArchive
                            DOWNLOAD_ARCHIVE.updateBoolean(useDownloadArchive)
                        },
                        enabled = isPermissionGranted,
                    )
                }
                */

                /*
                // REMOVED: SponsorBlock - Advanced feature
                item {
                    var showSponsorBlockDialog by remember { mutableStateOf(false) }
                    var isSponsorBlockEnabled by remember { mutableStateOf(SPONSORBLOCK.getBoolean()) }
                    PreferenceSwitchWithDivider(
                        title = stringResource(R.string.sponsorblock),
                        description = stringResource(R.string.sponsorblock_desc),
                        icon = Icons.Outlined.MoneyOff,
                        enabled = !isCustomCommandEnabled,
                        isChecked = isSponsorBlockEnabled,
                        onChecked = {
                            isSponsorBlockEnabled = !isSponsorBlockEnabled
                            PreferenceUtil.updateValue(SPONSORBLOCK, isSponsorBlockEnabled)
                        },
                        onClick = { showSponsorBlockDialog = true },
                    )
                }
                */

            }
        },
    )

    if (showYtdlpDialog) {
        YtdlpUpdateChannelDialog(onDismissRequest = { showYtdlpDialog = false })
    }

    if (showNotificationDialog) {
        NotificationPermissionDialog(
            onDismissRequest = { showNotificationDialog = false },
            onPermissionGranted = {
                notificationPermission?.launchPermissionRequest()
                NOTIFICATION.updateBoolean(true)
                downloadNotification = true
                showNotificationDialog = false
            },
        )
    }
}

@Composable
private fun UpdateProgressIndicator() {
    CircularProgressIndicator(
        modifier = Modifier
            .padding(start = 8.dp, end = 16.dp)
            .size(24.dp)
            .padding(2.dp)
    )
}