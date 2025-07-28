@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.dsyphr.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.dsyphr.screens.home.components.ChatSearchBar
import io.github.dsyphr.screens.home.components.ContactListItem


@Composable
fun HomeScreen() {
    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(bottom = 10.dp)) {
                TopAppBar(
                    title = { Text("Dsyphr", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                    ),

                    actions = {
                        IconButton(onClick = { /* do something */ }) {
                            Icon(
                                imageVector = Icons.Filled.Settings, contentDescription = "Settings"
                            )
                        }
                    },
                    navigationIcon = {
                        // logo
                    })

                ChatSearchBar()
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary, onClick = {
                    // do something
                }) {
                Icon(Icons.Filled.Create, contentDescription = "Create a chat")
            }
        },

        ) { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding)) {
            LazyColumn {
                items(50) {
                    ContactListItem()
                    //HorizontalDivider()
                }
            }
        }


    }

}
