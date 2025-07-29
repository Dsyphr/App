package io.github.dsyphr.screens.chat.components

import androidx.compose.foundation.layout.Column
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


data class MessageItem(val message: String, val sender: String, val isMine: Boolean)

@Composable
fun MessageCard(messageItem: MessageItem) { // 1
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = when { // 2
            messageItem.isMine -> Alignment.End
            else -> Alignment.Start
        },
    ) {
        Card(
            modifier = Modifier.widthIn(max = 340.dp),
            shape = cardShapeFor(messageItem), // 3
            colors = CardColors(
                when {
                    messageItem.isMine -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.secondaryContainer
                },
                contentColor = when {
                    messageItem.isMine -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                },
                disabledContainerColor = MaterialTheme.colorScheme.background,
                disabledContentColor = MaterialTheme.colorScheme.background,
            ),
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = messageItem.message,
                color = when {
                    messageItem.isMine -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                },
            )
        }
        Text( // 4
            text = messageItem.sender,
            fontSize = 12.sp,
        )
    }
}

@Composable
fun cardShapeFor(message: MessageItem): RoundedCornerShape {
    val roundedCorners = RoundedCornerShape(16.dp)
    return when {
        message.isMine -> roundedCorners.copy(bottomEnd = CornerSize(0))
        else -> roundedCorners.copy(bottomStart = CornerSize(0))
    }
}

