package com.junkfood.seal.ui.page.downloadv2.configure

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.junkfood.seal.App
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.HapticFeedback.longPressHapticFeedback
import com.junkfood.seal.ui.common.motion.materialSharedAxisX
import com.junkfood.seal.ui.component.OutlinedButtonWithIcon
import com.junkfood.seal.ui.component.SealModalBottomSheet
import com.junkfood.seal.ui.component.VideoFilterChip
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.Action
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.SheetState
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.SheetState.*
import com.junkfood.seal.util.*
import com.junkfood.seal.util.PreferenceUtil.updateBoolean

data class Config(
    val savedLinks: Set<String> = PreferenceUtil.getSavedLinks(),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadDialog(
    modifier: Modifier = Modifier,
    sheetState: androidx.compose.material3.SheetState,
    preferences: DownloadUtil.DownloadPreferences,
    onPreferencesUpdate: (DownloadUtil.DownloadPreferences) -> Unit,
    state: SheetState = InputUrl,
    onActionPost: (Action) -> Unit = {},
) {
    SealModalBottomSheet(
        sheetState = sheetState,
        contentPadding = PaddingValues(),
        onDismissRequest = { onActionPost(Action.HideSheet) },
    ) {
        DownloadDialogContent(
            modifier = modifier,
            state = state,
            preferences = preferences,
            onPreferencesUpdate = onPreferencesUpdate,
            onActionPost = onActionPost,
        )
    }
}

@Composable
private fun ErrorPage(modifier: Modifier = Modifier, state: Error, onActionPost: (Action) -> Unit) {
    val view = LocalView.current
    val clipboardManager = LocalClipboardManager.current
    val url =
        state.action.run {
            when (this) {
                is Action.FetchFormats -> url
                is Action.FetchPlaylist -> url
                else -> {
                    throw IllegalArgumentException()
                }
            }
        }
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = stringResource(R.string.fetch_info_error_msg),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = state.throwable.message.toString(),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .padding(vertical = 16.dp, horizontal = 20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            maxLines = 20,
            overflow = TextOverflow.Clip,
        )

        Row(modifier = Modifier) {
            FilledTonalButton(onClick = { onActionPost(state.action) }) { Text("Retry") }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    view.longPressHapticFeedback()
                    clipboardManager.setText(
                        AnnotatedString(
                            App.getVersionReport() + "\nURL: ${url}\n${state.throwable.message}"
                        )
                    )
                    ToastUtil.makeToast(R.string.error_copied)
                }
            ) {
                Text(stringResource(R.string.copy_error_report))
            }
        }
    }
}

@Composable
private fun DownloadDialogContent(
    modifier: Modifier = Modifier,
    state: SheetState,
    preferences: DownloadUtil.DownloadPreferences,
    onPreferencesUpdate: (DownloadUtil.DownloadPreferences) -> Unit,
    onActionPost: (Action) -> Unit,
) {
    AnimatedContent(
        modifier = modifier,
        targetState = state,
        label = "",
        transitionSpec = {
            materialSharedAxisX(initialOffsetX = { it / 4 }, targetOffsetX = { -it / 4 })
        },
    ) { state ->
        when (state) {
            is Configure -> {
                check(state.urlList.isNotEmpty())
                ConfigurePage(
                    url = state.urlList.first(),
                    preferences = preferences,
                    settingChips = {
                        AdditionalSettings(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            preference = preferences,
                            onPreferenceUpdate = {
                                onPreferencesUpdate(
                                    DownloadUtil.DownloadPreferences.createFromPreferences()
                                )
                            },
                        )
                    },
                    onActionPost = { onActionPost(it) },
                )
            }

            is Error -> {
                ErrorPage(state = state, onActionPost = onActionPost)
            }

            is Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 120.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            InputUrl -> {
                InputUrlPage(
                    config = Config(),
                    onConfigUpdate = { /* Do nothing */ },
                    onActionPost = onActionPost,
                )
            }
        }
    }
}

@Composable
private fun ConfigurePage(
    modifier: Modifier = Modifier,
    url: String = "",
    preferences: DownloadUtil.DownloadPreferences,
    settingChips: @Composable () -> Unit,
    onActionPost: (Action) -> Unit,
) {

    Column {
        Column(modifier = modifier.padding(horizontal = 20.dp)) {
            Header(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                title = stringResource(R.string.settings_before_download),
                icon = Icons.Filled.DoneAll,
            )
        }
        var expanded by remember { mutableStateOf(false) }
        ExpandableTitle(expanded = expanded, onClick = { expanded = true }) { settingChips() }

        SimplifiedActionButtons(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            onCancel = { onActionPost(Action.HideSheet) },
            onDownloadVideo = {
                onActionPost(
                    Action.DownloadWithPreset(
                        urlList = listOf(url),
                        preferences = preferences.copy(extractAudio = false),
                    )
                )
            },
            onDownloadAudio = {
                onActionPost(
                    Action.DownloadWithPreset(
                        urlList = listOf(url),
                        preferences = preferences.copy(extractAudio = true),
                    )
                )
            },
        )
    }
}


@Composable
private fun AdditionalSettings(
    modifier: Modifier = Modifier,
    preference: DownloadUtil.DownloadPreferences,
    onPreferenceUpdate: () -> Unit,
) {
    with(preference) {
        Row(modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())) {

            // FIX: Removed the "Cookies" chip since the feature was removed.

            VideoFilterChip(
                selected = downloadSubtitle,
                onClick = {
                    SUBTITLE.updateBoolean(!downloadSubtitle)
                    onPreferenceUpdate()
                },
                label = stringResource(id = R.string.download_subtitles),
            )
            VideoFilterChip(
                selected = createThumbnail,
                onClick = {
                    THUMBNAIL.updateBoolean(!createThumbnail)
                    onPreferenceUpdate()
                },
                label = stringResource(R.string.create_thumbnail),
            )
        }

        // FIX: Removed the "CookiesQuickSettingsDialog" since the feature was removed.
    }
}

@Composable
fun ExpandableTitle(
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Column {
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(thickness = Dp.Hairline, modifier = Modifier.padding(horizontal = 20.dp))
        Column(
            modifier =
                modifier
                    .clickable(
                        onClick = onClick,
                        onClickLabel = stringResource(R.string.show_more_actions),
                        enabled = !expanded,
                    )
                    .padding(top = 12.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.width(24.dp))
                Text(
                    text = stringResource(R.string.additional_settings),
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.weight(1f))
                if (!expanded) {
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(32.dp))
                }
            }
            AnimatedVisibility(expanded) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    content()
                }
            }
        }
    }
}


@Composable
internal fun Header(modifier: Modifier = Modifier, icon: ImageVector, title: String) {
    Column(modifier = modifier) {
        Icon(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            imageVector = icon,
            contentDescription = null,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp, bottom = 8.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SimplifiedActionButtons(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onDownloadVideo: () -> Unit,
    onDownloadAudio: () -> Unit,
) {
    // We changed the Row to a Column to stack the buttons vertically.
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally, // Center the buttons horizontally
        verticalArrangement = Arrangement.spacedBy(8.dp) // Add space between each button
    ) {
        // Video Button - Made it wider to look better
        Button(
            onClick = onDownloadVideo,
            modifier = Modifier.fillMaxWidth(0.75f) // Takes up 75% of the available width
        ) {
            Icon(
                imageVector = Icons.Filled.VideoFile,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.video))
        }

        // Audio Button - Made it wider to look better
        Button(
            onClick = onDownloadAudio,
            modifier = Modifier.fillMaxWidth(0.75f) // Takes up 75% of the available width
        ) {
            Icon(
                imageVector = Icons.Filled.AudioFile,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.audio))
        }

        // Cancel Button
        OutlinedButtonWithIcon(
            onClick = onCancel,
            icon = Icons.Outlined.Cancel,
            text = stringResource(R.string.cancel),
        )
    }
}