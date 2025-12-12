package io.github.dsyphr.screens.chat.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.dsyphr.TranslationManager
import io.github.dsyphr.dataClasses.MessageItem
import io.github.dsyphr.dataClasses.User
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Composable
fun MessageCard(messageItem: MessageItem, secondUser: User) { // 1
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sentText by remember { mutableStateOf(value = "Sent") }
    var translatedOrOriginal by remember { mutableStateOf(value = messageItem.sender.username == secondUser.username) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = when { // 2
            messageItem.sender.username == secondUser.username -> Alignment.Start
            else -> Alignment.End
        },
    ) {
        Row {
            messageItem.sender.profileImg
            Card(
                modifier = Modifier.widthIn(max = 340.dp).combinedClickable(onLongClick = {translatedOrOriginal = !translatedOrOriginal}, onClick = {}),
                shape = cardShapeFor(messageItem, secondUser), // 3
                colors = CardColors(
                    when {
                        messageItem.sender.username == secondUser.username -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.primaryContainer
                    },
                    contentColor = when {
                        messageItem.sender.username == secondUser.username -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    disabledContainerColor = MaterialTheme.colorScheme.background,
                    disabledContentColor = MaterialTheme.colorScheme.background,
                ),
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    text = if (translatedOrOriginal) messageItem.message else messageItem.originalMessage,
                    color = when {
                        messageItem.sender.username == secondUser.username -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    },
                )
            }
        }
        Row {
            Text(
                text = customLocaleDateFormat(messageItem.seconds ?: 0),
                fontSize = 12.sp,
            )
            Text(text = "•", fontSize = 12.sp, modifier = Modifier.padding(horizontal = 3.dp))
            Text(text = sentText, fontSize = 12.sp)
        }
    }
}

@Composable
fun cardShapeFor(messageItem: MessageItem, secondUser: User): RoundedCornerShape {
    val roundedCorners = RoundedCornerShape(16.dp)
    return when {
        messageItem.sender.username == secondUser.username -> roundedCorners.copy(
            bottomStart = CornerSize(
                0
            )
        )

        else -> roundedCorners.copy(bottomEnd = CornerSize(0))
    }
}

fun customLocaleDateFormat(timestamp: Long): String {
    val time = Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("hh:mm")
    val formattedDate = time.format(formatter)
    return formattedDate
}