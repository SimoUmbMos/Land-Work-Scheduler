package com.simosc.landworkscheduler.presentation.ui.components.topbar

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.simosc.landworkscheduler.R

@Composable
fun SearchTopAppBar(
    onSearchChange: (String) -> Unit,
    onCloseSearchBar: () -> Unit,
    searchQuery: String,
){
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp,
            ),
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = {
            Text(text = stringResource(R.string.search_placeholder))
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onCloseSearchBar()
            }
        ),
        leadingIcon = {
            IconButton(
                onClick = {
                    if(searchQuery.isNotBlank())
                        onSearchChange("")
                    else
                        onCloseSearchBar()
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.search_clear_close_label)
                )
            }
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    onCloseSearchBar()
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = stringResource(R.string.search_label)
                )
            }
        },
        shape = RoundedCornerShape(32.dp),
        colors = TextFieldDefaults.colors(
            focusedTextColor =
            MaterialTheme.colorScheme.onPrimaryContainer,
            unfocusedTextColor =
            MaterialTheme.colorScheme.onPrimaryContainer,

            focusedContainerColor =
            MaterialTheme.colorScheme.primaryContainer,
            unfocusedContainerColor =
            MaterialTheme.colorScheme.primaryContainer,

            focusedIndicatorColor =
            Color.Transparent,
            unfocusedIndicatorColor =
            Color.Transparent,

            focusedLeadingIconColor =
            MaterialTheme.colorScheme.onPrimaryContainer,
            unfocusedLeadingIconColor =
            MaterialTheme.colorScheme.onPrimaryContainer,

            focusedTrailingIconColor =
            MaterialTheme.colorScheme.onPrimaryContainer,
            unfocusedTrailingIconColor =
            MaterialTheme.colorScheme.onPrimaryContainer,

            focusedPlaceholderColor =
            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
            unfocusedPlaceholderColor =
            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
        )
    )
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun PreviewSearchBarDefault(){
    var searchQuery by remember{
        mutableStateOf("")
    }
    Scaffold(
        topBar = {
            SearchTopAppBar(
                searchQuery = searchQuery,
                onSearchChange = {searchQuery = it},
                onCloseSearchBar = {}
            )
        }
    ){ paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ){ }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun PreviewSearchBarWithQuery(){
    var searchQuery by remember{
        mutableStateOf("Test")
    }
    Scaffold(
        topBar = {
            SearchTopAppBar(
                searchQuery = searchQuery,
                onSearchChange = {searchQuery = it},
                onCloseSearchBar = {}
            )
        }
    ){ paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ){ }
    }
}