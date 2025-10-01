package io.github.dsyphr.screens.chat.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.dsyphr.dataClasses.MessageItem
import io.github.dsyphr.dataClasses.User


@Composable
fun MessageCard(messageItem: MessageItem, secondUser: User) { // 1
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = when { // 2
            messageItem.sender.username == secondUser.username -> Alignment.Start
            else -> Alignment.End
        },
    ) {
        Row {
            messageItem.sender.profileImg
            Card(
                modifier = Modifier.widthIn(max = 340.dp),
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
                    text = messageItem.message,
                    color = when {
                        messageItem.sender.username == secondUser.username -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    },
                )
            }
        }
        Text(
            // 4
            text = when {
                messageItem.sender.username == secondUser.username -> messageItem.sender.username; else -> "You"
            },
            fontSize = 12.sp,
        )
    }
}

@Composable
fun cardShapeFor(messageItem: MessageItem, secondUser: User): RoundedCornerShape {
    val roundedCorners = RoundedCornerShape(16.dp)
    return when {
        messageItem.sender.username == secondUser.username -> roundedCorners.copy(bottomStart = CornerSize(0))
        else -> roundedCorners.copy(bottomEnd = CornerSize(0))
    }
}

