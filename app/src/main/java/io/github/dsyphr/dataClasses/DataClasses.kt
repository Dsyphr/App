package io.github.dsyphr.dataClasses


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable

data class User(
    val username: String, val id: Int, val profileImg: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Filled.AccountCircle, contentDescription = username
        )
    }
)

data class MessageItem(val message: String, val sender: User, val receiver: User)




