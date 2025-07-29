package io.github.dsyphr.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.dsyphr.enums.ReadStatus

@Composable
fun ContactListItem(
    modifier: Modifier = Modifier,
    name: String = "Default Contact",
    lastMessageTime: String = "12:35 pm",
    lastMessage: String = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam interdum gravida feugiat. In tincidunt sem porttitor convallis eleifend. Lorem ipsum dolor sit amet, consectetur adipiscing.",
    readStatus: Enum<ReadStatus> = ReadStatus.RECEIVED
) {
    ListItem(modifier = modifier.padding(vertical = 2.dp), headlineContent = { Text(name) }, supportingContent = {
        Text(
            softWrap = true, maxLines = 1, overflow = TextOverflow.Ellipsis, text = lastMessage
        )
    }, leadingContent = {
        Icon(
            Icons.Filled.AccountCircle, contentDescription = null, modifier = Modifier.size(60.dp)
        )
    }, trailingContent = {
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.SpaceEvenly) {
            Text(lastMessageTime)
            when (readStatus) {
                ReadStatus.RECEIVED -> {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    Text(
                        "1", modifier = Modifier.padding(10.dp).drawBehind {
                            drawCircle(
                                radius = this.size.maxDimension / 1.5f, color = primaryColor
                            )
                        }, color = MaterialTheme.colorScheme.onPrimary )
                }

                ReadStatus.RECEIVED_READ -> {
                }

                else -> {
                    Icon(
                        if (readStatus == ReadStatus.SENT) Icons.Filled.Done else Icons.Filled.DoneAll,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (readStatus == ReadStatus.READ) Color.Cyan else Color.Gray
                    )
                }
            }
        }
    })
}