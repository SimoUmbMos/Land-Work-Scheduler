package com.simosc.landworkscheduler.presentation.ui.screens.menumain

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.simosc.landworkscheduler.presentation.ui.components.topbar.DefaultTopAppBar


@Composable
fun MainMenuScreen(
    uiState: MainMenuStates,
    onAppSettingsClick: () -> Unit = {},
    onLandsMenuClick: () -> Unit = {},
    onLiveTrackingClick: () -> Unit = {},
    onSchedulesMenuClick: () -> Unit = {},
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            DefaultTopAppBar(
                title = "Land Work Scheduler",
                subTitle = if(uiState is MainMenuStates.Loaded)
                        when(uiState.todayWorksCount) {
                            in Long.MIN_VALUE .. 0L -> "No scheduled jobs for today"
                            1L -> "1 scheduled job for today"
                            in 1000 .. Long.MAX_VALUE -> "999+ scheduled jobs for today"
                            else -> "${uiState.todayWorksCount} scheduled jobs for today"
                        }
                    else
                        null,
                subTitleColor = if(uiState is MainMenuStates.Loaded)
                        if (uiState.todayWorksCount > 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onBackground
                    else
                        null,
                actions = {
                    IconButton(
                        onClick = onAppSettingsClick
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        content = { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                MainMenuLoadedScreenContent(
                    uiState = uiState,
                    onLandsMenuClick = onLandsMenuClick,
                    onLiveTrackingClick = onLiveTrackingClick,
                    onSchedulesMenuClick = onSchedulesMenuClick,
                )
            }
        }
    )
}

@Composable
private fun MainMenuLoadedScreenContent(
    uiState: MainMenuStates,
    onLandsMenuClick: () -> Unit,
    onLiveTrackingClick: () -> Unit,
    onSchedulesMenuClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(
                brush = Brush.linearGradient(
                    0.249f to MaterialTheme.colorScheme.surface,
                    0.25f to MaterialTheme.colorScheme.primary,
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            ).padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        MainMenuLandsCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            onLandsMenuClick = {
                if(uiState != MainMenuStates.Loading)
                    onLandsMenuClick()
            }
        )
        MainMenuSchedulesCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            onSchedulesMenuClick = {
                if (uiState != MainMenuStates.Loading)
                    onSchedulesMenuClick()
            }
        )
        MainMenuTrackingCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            onLiveTrackingMenuClick = {
                if (uiState != MainMenuStates.Loading)
                    onLiveTrackingClick()
            }
        )
    }
}


@Composable
private fun MainMenuLandsCard(
    modifier: Modifier = Modifier,
    onLandsMenuClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        onClick = onLandsMenuClick
    ){
        ListItem(
            headlineContent = {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(align = Alignment.Center),
                    text = "Lands",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            leadingContent = {
                Icon(
                    modifier = Modifier.size(64.dp),
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                )
            },
            trailingContent = {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                )
            },
        )
    }
}

@Composable
private fun MainMenuSchedulesCard(
    modifier: Modifier = Modifier,
    onSchedulesMenuClick: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        onClick = onSchedulesMenuClick
    ){
        ListItem(
            headlineContent = {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(align = Alignment.Center),
                    text = "Schedules",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            leadingContent = {
                Icon(
                    modifier = Modifier.size(64.dp),
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                )
            },
            trailingContent = {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                )
            },
        )
    }
}

@Composable
private fun MainMenuTrackingCard(
    modifier: Modifier = Modifier,
    onLiveTrackingMenuClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        onClick = onLiveTrackingMenuClick
    ){
        ListItem(
            headlineContent = {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(align = Alignment.Center),
                    text = "Location Tracking",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            leadingContent = {
                Icon(
                    modifier = Modifier.size(64.dp),
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                )
            },
            trailingContent = {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                )
            },
        )
    }
}



@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewMainMenuScreenLoading(){
    MainMenuScreen(
        uiState = MainMenuStates.Loading
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewMainMenuScreenLoadedNoData(){
    MainMenuScreen(
        uiState = MainMenuStates.Loaded(
            todayWorksCount = 0
        )
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewMainMenuScreenLoadedSingleData(){
    MainMenuScreen(
        uiState = MainMenuStates.Loaded(
            todayWorksCount = 1
        )
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewMainMenuScreenLoadedMultipleData(){
    MainMenuScreen(
        uiState = MainMenuStates.Loaded(
            todayWorksCount = 5
        )
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewMainMenuScreenLoadedMaxData(){
    MainMenuScreen(
        uiState = MainMenuStates.Loaded(
            todayWorksCount = 1000
        )
    )
}