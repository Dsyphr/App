package io.github.dsyphr.screens.home.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSearchBar(modifier: Modifier = Modifier) {

    var searchText by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    SearchBar(
        modifier = Modifier.then(
            if (active) {
                Modifier.fillMaxWidth()
            } else {
                Modifier.padding(horizontal = 16.dp)
            }
        ),
        query = searchText, active = active,
        onQueryChange = { it: String ->
            searchText = it
        },
        onSearch = {
            active = false
        },
        onActiveChange = {
            active = it
        },
        placeholder = {
            Text(text = "Search a contact")
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search, contentDescription = "Search a contact"
            )
        },
        windowInsets = WindowInsets(top = 0.dp),
    ) {}
}


