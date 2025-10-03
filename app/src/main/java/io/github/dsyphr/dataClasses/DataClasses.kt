package io.github.dsyphr.dataClasses


import android.graphics.drawable.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.Timestamp
import com.google.firebase.database.ServerValue
import java.util.Date

data class User(
    val username: String, val uid: String, val profileImg: ImageVector = Icons.Filled.AccountCircle,
)

data class UserProfile(val username: String, val email: String)

data class MessageItem(
    val message: String,
    val sender: User,
    val seconds: Long?=0,
)

data class DatabaseMessageItem(
    val message: String,
    val senderID: String,
    val timestamp: Timestamp? = null
)




