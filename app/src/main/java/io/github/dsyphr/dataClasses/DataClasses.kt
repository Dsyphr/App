package io.github.dsyphr.dataClasses


import androidx.compose.runtime.Composable

data class User(val username: String, val id: Int, val profile_img: @Composable () -> Unit)
data class MessageItem(val message:String, val sender: User, val receiver: User)




