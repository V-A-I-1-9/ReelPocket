package com.junkfood.seal.ui.page.downloadv2

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.download.Task
import com.junkfood.seal.download.Task.*
import com.junkfood.seal.download.Task.DownloadState.*
import com.junkfood.seal.ui.common.LocalFixedColorRoles
import com.junkfood.seal.ui.component.ActionSheetItem
import com.junkfood.seal.ui.component.ActionSheetPrimaryButton
import com.junkfood.seal.ui.theme.ErrorTonalPalettes
import com.junkfood.seal.util.Format
import com.junkfood.seal.util.toBitrateText
import com.junkfood.seal.util.toDurationText
import com.junkfood.seal.util.toFileSizeText
import com.junkfood.seal.util.toLocalizedString

@Composable
private fun ShareButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = LocalFixedColorRoles.current.secondaryFixed,
        contentColor = LocalFixedColorRoles.current.onSecondaryFixedVariant,
        imageVector = Icons.Rounded.Share,
        text = stringResource(R.string.share),
        onClick = onClick,
    )
}

@Composable
private fun PlayButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = LocalFixedColorRoles.current.primaryFixed,
        contentColor = LocalFixedColorRoles.current.onPrimaryFixedVariant,
        imageVector = Icons.Rounded.PlayArrow,
        text = stringResource(R.string.open_file),
        onClick = onClick,
    )
}

@Composable
private fun ResumeButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = LocalFixedColorRoles.current.tertiaryFixed,
        contentColor = LocalFixedColorRoles.current.onTertiaryFixedVariant,
        imageVector = Icons.Outlined.RestartAlt,
        text = stringResource(R.string.resume),
        onClick = onClick,
    )
}

@Composable
private fun ErrorReportButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = ErrorTonalPalettes.accent1(80.0),
        contentColor = ErrorTonalPalettes.accent1(10.0),
        imageVector = Icons.Outlined.ErrorOutline,
        text = stringResource(R.string.copy_error_report),
        onClick = onClick,
    )
}

@Composable
private fun DeleteButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        imageVector = Icons.Outlined.Delete,
        outlineColor = MaterialTheme.colorScheme.outlineVariant,
        text = stringResource(R.string.delete),
        onClick = onClick,
    )
}

@Composable
private fun CancelButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        imageVector = Icons.Outlined.Cancel,
        text = stringResource(R.string.cancel),
        onClick = onClick,
    )
}

@Composable
private fun DownloadLogButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = LocalFixedColorRoles.current.secondaryFixed,
        contentColor = LocalFixedColorRoles.current.onSecondaryFixedVariant,
        imageVector = Icons.AutoMirrored.Outlined.TextSnippet,
        text = stringResource(R.string.show_logs),
        onClick = onClick,
    )
}

@Composable
private fun CopyURLButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        outlineColor = MaterialTheme.colorScheme.outlineVariant,
        imageVector = Icons.Outlined.ContentCopy,
        text = stringResource(R.string.copy_link),
        onClick = onClick,
    )
}

@Composable
private fun OpenVideoURLButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        outlineColor = MaterialTheme.colorScheme.outlineVariant,
        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
        text = stringResource(R.string.open_url),
        onClick = onClick,
    )
}

@Composable
private fun OpenThumbnailURLButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        outlineColor = MaterialTheme.colorScheme.outlineVariant,
        imageVector = Icons.Outlined.Image,
        text = stringResource(R.string.thumbnail),
        onClick = onClick,
    )
}

@Composable
fun Title(imageModel: Any?, title: String, author: String, downloadState: DownloadState) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.height(IntrinsicSize.Min)) {
            Column(Modifier) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(Modifier.height(8.dp))

            ListItemStateText(downloadState = downloadState)
        }
    }
}

@Composable
fun SheetContent(
    task: Task,
    viewState: ViewState,
    downloadState: DownloadState,
    onDismissRequest: () -> Unit,
    onActionPost: (Task, UiAction) -> Unit,
) {

    LazyColumn {
        item {
            Title(
                imageModel = viewState.thumbnailUrl,
                title = viewState.title,
                author = viewState.uploader,
                downloadState = downloadState,
            )
        }

        item {
            LazyRow(
                modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
            ) {
                ActionButtons(
                    task = task,
                    downloadState = downloadState,
                    viewState = viewState,
                    onDismissRequest = onDismissRequest,
                    onActionPost = onActionPost,
                )
            }
        }

        item { ActionSheetInfo(task = task, viewState = viewState) }
    }
}

fun LazyListScope.ActionButtons(
    task: Task,
    downloadState: DownloadState,
    viewState: ViewState,
    onDismissRequest: () -> Unit,
    onActionPost: (Task, UiAction) -> Unit,
) {
    when (downloadState) {
        is Canceled -> {
            item(key = "ResumeButton") {
                ResumeButton(modifier = Modifier.animateItem()) {
                    onActionPost(task, UiAction.Resume)
                    onDismissRequest()
                }
            }
        }
        is Completed -> {
            item(key = "PlayButton") {
                PlayButton(modifier = Modifier.animateItem()) {
                    onActionPost(task, UiAction.OpenFile(downloadState.filePath))
                    onDismissRequest()
                }
            }
            item(key = "ShareButton") {
                ShareButton(modifier = Modifier.animateItem()) {
                    onActionPost(task, UiAction.ShareFile(downloadState.filePath))
                }
            }
        }
        is Error -> {
            item(key = "ResumeButton") {
                ResumeButton(modifier = Modifier.animateItem()) {
                    onActionPost(task, UiAction.Resume)
                    onDismissRequest()
                }
            }
            item(key = "ErrorReportButton") {
                ErrorReportButton(modifier = Modifier.animateItem()) {
                    onActionPost(task, UiAction.CopyErrorReport(downloadState.throwable))
                }
            }
        }
        is FetchingInfo,
        ReadyWithInfo,
        Idle,
        is Running -> {
            item(key = "CancelButton") {
                CancelButton(modifier = Modifier.animateItem()) {
                    onActionPost(task, UiAction.Cancel)
                    onDismissRequest()
                }
            }
        }
    }
    if (downloadState is DownloadState.Restartable || downloadState is Completed) {
        item(key = "DeleteButton") {
            DeleteButton(modifier = Modifier.animateItem()) {
                onActionPost(task, UiAction.Delete)
                onDismissRequest()
            }
        }
    }
    item(key = "CopyURLButton") {
        CopyURLButton(modifier = Modifier.animateItem()) {
            onActionPost(task, UiAction.CopyVideoURL)
        }
    }
    item(key = "OpenVideoURLButton") {
        OpenVideoURLButton(modifier = Modifier.animateItem()) {
            onActionPost(task, UiAction.OpenVideoURL(viewState.url))
        }
    }
    if (!viewState.thumbnailUrl.isNullOrEmpty()) {
        item(key = "OpenThumbnailURLButton") {
            OpenThumbnailURLButton(modifier = Modifier.animateItem()) {
                onActionPost(task, UiAction.OpenThumbnailURL(viewState.thumbnailUrl))
            }
        }
    }
}


@Composable
fun ActionSheetInfo(modifier: Modifier = Modifier, task: Task, viewState: ViewState) {
    with(viewState) {
        Column(modifier = modifier) {
            HorizontalDivider()
            Text(
                stringResource(R.string.media_info),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
            )
            ActionSheetItem(
                text = {
                    Text(
                        task.timeCreated.toLocalizedString(),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        "${duration.toDurationText()} · ${fileSizeApprox.toFileSizeText()}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.FileDownload, contentDescription = null)
                },
            )

            videoFormats?.forEachIndexed { _index, fmt ->
                val index = _index + 1
                val fileSizeText = (fmt.fileSize ?: fmt.fileSizeApprox).toFileSizeText()
                val bitRateText = fmt.vbr.toBitrateText()
                val codecText = fmt.vcodec?.substringBefore(delimiter = ".") ?: ""

                val title = "${stringResource(R.string.video)} #$index: ${fmt.formatNote}"
                val details =
                    listOf(codecText, fmt.resolution, bitRateText, fileSizeText)
                        .filterNot { it.isNullOrBlank() }
                        .joinToString(separator = " · ")

                ActionSheetItem(
                    text = {
                        Text(title, style = MaterialTheme.typography.titleSmall)
                        Text(details, style = MaterialTheme.typography.bodySmall)
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.VideoFile, contentDescription = null)
                    },
                )
            }

            val audioFormats: List<Format> = buildList {
                videoFormats?.filter { it.containsAudio() }?.let { addAll(it) }
                audioOnlyFormats?.let { addAll(it) }
            }

            audioFormats.forEachIndexed { _index, fmt ->
                val index = _index + 1
                val fileSizeText = (fmt.fileSize ?: fmt.fileSizeApprox).toFileSizeText()
                val bitRateText = fmt.abr.toBitrateText()
                val codecText = fmt.acodec?.substringBefore(delimiter = ".") ?: ""

                val title = "${stringResource(R.string.audio)} #$index: ${fmt.formatNote}"
                val details =
                    listOf(codecText, bitRateText, fileSizeText)
                        .filterNot { it.isBlank() }
                        .joinToString(separator = " · ")

                ActionSheetItem(
                    text = {
                        Text(title, style = MaterialTheme.typography.titleSmall)
                        Text(details, style = MaterialTheme.typography.bodySmall)
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.AudioFile, contentDescription = null)
                    },
                )
            }

            ActionSheetItem(
                text = {
                    Text(text = extractorKey, style = MaterialTheme.typography.titleSmall)
                    Text(text = url, style = MaterialTheme.typography.bodySmall)
                },
                leadingIcon = { Icon(imageVector = Icons.Outlined.Link, contentDescription = null) },
            )
        }
    }
}