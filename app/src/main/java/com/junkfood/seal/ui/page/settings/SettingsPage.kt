package com.junkfood.seal.ui.page.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SettingsApplications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.Route
import com.junkfood.seal.ui.common.intState
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.SettingItem
import com.junkfood.seal.util.SHOW_SPONSOR_MSG

@SuppressLint("BatteryLife")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(onNavigateBack: () -> Unit, onNavigateTo: (String) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val showSponsorMessage by SHOW_SPONSOR_MSG.intState

    val typography = MaterialTheme.typography

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val overrideTypography =
                remember(typography) { typography.copy(headlineMedium = typography.displaySmall) }

            MaterialTheme(typography = overrideTypography) {
                LargeTopAppBar(
                    title = { Text(text = stringResource(id = R.string.settings)) },
                    navigationIcon = { BackButton(onNavigateBack) },
                    scrollBehavior = scrollBehavior,
                    expandedHeight = TopAppBarDefaults.LargeAppBarExpandedHeight + 24.dp,
                )
            }
        },
    ) {
        LazyColumn(modifier = Modifier, contentPadding = it) {
            // REMOVED: Battery configuration card and all related logic

            // REMOVED: Sponsor message card for a cleaner look
            /*
            if (!showBatteryHint && showSponsorMessage > 30)
                item {
                    PreferencesHintCard(
                        title = stringResource(id = R.string.sponsor),
                        icon = Icons.Rounded.VolunteerActivism,
                        description = stringResource(id = R.string.sponsor_desc),
                    ) {
                        onNavigateTo(Route.DONATE)
                    }
                }
            */
            item {
                SettingItem(
                    title = stringResource(id = R.string.general_settings),
                    description = stringResource(id = R.string.general_settings_desc),
                    icon = Icons.Rounded.SettingsApplications,
                ) {
                    onNavigateTo(Route.GENERAL_DOWNLOAD_PREFERENCES)
                }
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.download_directory),
                    description = stringResource(id = R.string.download_directory_desc),
                    icon = Icons.Rounded.Folder,
                ) {
                    onNavigateTo(Route.DOWNLOAD_DIRECTORY)
                }
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.look_and_feel),
                    description = stringResource(id = R.string.display_settings),
                    icon = Icons.Rounded.Palette,
                ) {
                    onNavigateTo(Route.APPEARANCE)
                }
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.about),
                    description = stringResource(id = R.string.about_page),
                    icon = Icons.Rounded.Info,
                ) {
                    onNavigateTo(Route.ABOUT)
                }
            }
        }
    }
}