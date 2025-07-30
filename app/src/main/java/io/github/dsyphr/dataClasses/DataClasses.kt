package io.github.dsyphr.dataClasses


import android.graphics.drawable.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class User(
    val username: String, val id: Int, val profileImg: ImageVector = Icons.Filled.AccountCircle,
)

data class MessageItem(val message: String, val sender: User, val receiver: User)




