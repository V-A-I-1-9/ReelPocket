package com.junkfood.seal.ui.page.settings.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.junkfood.seal.App
import com.junkfood.seal.App.Companion.packageInfo
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.util.ToastUtil


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPage(
    onNavigateBack: () -> Unit,
    onNavigateToCreditsPage: () -> Unit,
    onNavigateToUpdatePage: () -> Unit, // Parameter is kept for compatibility, but not used
    onNavigateToDonatePage: () -> Unit, // Parameter is kept for compatibility, but not used
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(),
            canScroll = { true },
        )
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val info = App.getVersionReport()
    val versionName = packageInfo.versionName

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(modifier = Modifier, text = stringResource(id = R.string.about)) },
                navigationIcon = { BackButton { onNavigateBack() } },
                scrollBehavior = scrollBehavior,
            )
        },
        content = {
            LazyColumn(modifier = Modifier.padding(it)) {

                // ADDED: A new section for your project attribution.
                // TODO: Change "[Your Name]" to your actual name.
                item {
                    PreferenceItem(
                        title = "Modified by",
                        description = "Vaibhav MS for Mini-Project",
                        icon = Icons.Outlined.AccountCircle
                    ) {
                        // No action needed on click
                    }
                }

                /*
                // REMOVED: README, links to original project
                item {
                    PreferenceItem(
                        title = stringResource(R.string.readme),
                        description = stringResource(R.string.readme_desc),
                        icon = Icons.Outlined.Description,
                    ) {
                        openUrl(repoUrl)
                    }
                }
                */

                /*
                // REMOVED: Latest Release, links to original project
                item {
                    PreferenceItem(
                        title = stringResource(R.string.release),
                        description = stringResource(R.string.release_desc),
                        icon = Icons.Outlined.NewReleases,
                    ) {
                        openUrl(releaseURL)
                    }
                }
                */

                /*
                // REMOVED: Sponsor, links to original project
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.sponsor),
                        description = stringResource(id = R.string.sponsor_desc),
                        icon = Icons.Outlined.VolunteerActivism,
                    ) {
                        onNavigateToDonatePage()
                    }
                }
                */

                /*
                // REMOVED: Telegram Channel, links to original project
                item {
                    PreferenceItem(
                        title = stringResource(R.string.telegram_channel),
                        description = telegramChannelUrl,
                        icon = painterResource(id = R.drawable.icons8_telegram_app),
                    ) {
                        openUrl(telegramChannelUrl)
                    }
                }
                */

                /*
                // REMOVED: Matrix Space, links to original project
                item {
                    PreferenceItem(
                        title = stringResource(R.string.matrix_space),
                        description = matrixSpaceUrl,
                        icon = painterResource(id = R.drawable.icons8_matrix),
                    ) {
                        openUrl(matrixSpaceUrl)
                    }
                }
                */

                // KEPT: Credits are mandatory to give credit to original authors.
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.credits),
                        description = stringResource(id = R.string.credits_desc),
                        icon = Icons.Outlined.AutoAwesome,
                    ) {
                        onNavigateToCreditsPage()
                    }
                }

                /*
                // REMOVED: Auto update, not functional for your version of the app.
                item {
                    PreferenceSwitchWithDivider(
                        title = stringResource(R.string.auto_update),
                        description = stringResource(R.string.check_for_updates_desc),
                        icon =
                        if (isAutoUpdateEnabled) Icons.Outlined.Update
                        else Icons.Outlined.UpdateDisabled,
                        isChecked = isAutoUpdateEnabled,
                        isSwitchEnabled = !App.isFDroidBuild(),
                        onClick = onNavigateToUpdatePage,
                        onChecked = {
                            isAutoUpdateEnabled = !isAutoUpdateEnabled
                            PreferenceUtil.updateValue(AUTO_UPDATE, isAutoUpdateEnabled)
                        },
                    )
                }
                */

                // KEPT: Version is good practice.
                item {
                    PreferenceItem(
                        title = stringResource(R.string.version),
                        description = versionName,
                        icon = Icons.Outlined.Info,
                    ) {
                        clipboardManager.setText(AnnotatedString(info))
                        ToastUtil.makeToast(R.string.info_copied)
                    }
                }

                // KEPT: Package name is useful for reference.
                item {
                    PreferenceItem(title = "Package name", description = context.packageName) {
                        clipboardManager.setText(AnnotatedString(context.packageName))
                        ToastUtil.makeToast(R.string.info_copied)
                    }
                }
            }
        },
    )
}