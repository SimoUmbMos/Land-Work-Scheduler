package com.simosc.landworkscheduler.presentation.ui.components.topbar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String? = null,
    titleColor: Color? = null,
    subTitleColor: Color? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable (RowScope.() -> Unit) = {},
){
    TopAppBar(
        modifier = modifier,
        title = {
            if(!subTitle.isNullOrBlank()){
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = titleColor ?: Color.Unspecified,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = subTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = subTitleColor ?: Color.Unspecified,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }else{
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = titleColor ?: Color.Unspecified,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = navigationIcon,
        actions = actions,
    )
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun PreviewDefaultTopAppBar_OnlyTitle(){
    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = "Title"
            )
        }
    ){
        Surface(modifier = Modifier.padding(it)){

        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun PreviewDefaultTopAppBar_WithSubTitle(){
    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = "Title",
                subTitle = "Sub Title",
            )
        }
    ){
        Surface(modifier = Modifier.padding(it)){

        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun PreviewDefaultTopAppBar_OnlyLargeTitle(){
    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = "Title ".repeat(50)
            )
        }
    ){
        Surface(modifier = Modifier.padding(it)){

        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun PreviewDefaultTopAppBar_WithLargeSubTitle(){
    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = "Title ".repeat(50),
                subTitle = "Sub Title ".repeat(50),
            )
        }
    ){
        Surface(modifier = Modifier.padding(it)){

        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun PreviewDefaultTopAppBar_OnlyTitle_WithBackButton(){
    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = "Title",
                navigationIcon = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ){
        Surface(modifier = Modifier.padding(it)){

        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun PreviewDefaultTopAppBar_WithSubTitle_WithBackButton(){
    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = "Title",
                subTitle = "Sub Title",
                navigationIcon = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ){
        Surface(modifier = Modifier.padding(it)){

        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun PreviewDefaultTopAppBar_OnlyLargeTitle_WithBackButton(){
    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = "Title ".repeat(50),
                navigationIcon = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ){
        Surface(modifier = Modifier.padding(it)){

        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun PreviewDefaultTopAppBar_WithLargeSubTitle_WithBackButton(){
    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = "Title ".repeat(50),
                subTitle = "Sub Title ".repeat(50),
                navigationIcon = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ){
        Surface(modifier = Modifier.padding(it)){

        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun PreviewDefaultTopAppBar_OnlyTitle_WithBackButton_WithActions(){
    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = "Title",
                navigationIcon = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ){
        Surface(modifier = Modifier.padding(it)){

        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun PreviewDefaultTopAppBar_WithSubTitle_WithBackButton_WithActions(){
    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = "Title",
                subTitle = "Sub Title",
                navigationIcon = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ){
        Surface(modifier = Modifier.padding(it)){

        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun PreviewDefaultTopAppBar_OnlyLargeTitle_WithBackButton_WithActions(){
    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = "Title ".repeat(50),
                navigationIcon = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ){
        Surface(modifier = Modifier.padding(it)){

        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun PreviewDefaultTopAppBar_WithLargeSubTitle_WithBackButton_WithActions(){
    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = "Title ".repeat(50),
                subTitle = "Sub Title ".repeat(50),
                navigationIcon = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ){
        Surface(modifier = Modifier.padding(it)){

        }
    }
}