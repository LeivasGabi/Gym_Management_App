package gym.management.presentation.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WheelTimePickerDialog(
    initialHour: Int = 8,
    initialMinute: Int = 0,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Selecionar Horário",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WheelPicker(
                    items = (0..23).map { it.toString().padStart(2, '0') },
                    initialIndex = initialHour,
                    onIndexSelected = { selectedHour = it },
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = ":",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                WheelPicker(
                    items = (0..59).map { it.toString().padStart(2, '0') },
                    initialIndex = initialMinute,
                    onIndexSelected = { selectedMinute = it },
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedHour, selectedMinute) }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelPicker(
    items: List<String>,
    initialIndex: Int,
    onIndexSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 52.dp,
    visibleItemsCount: Int = 5
) {
    val count = items.size
    val centerOffset = visibleItemsCount / 2
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = count * 500 + initialIndex - centerOffset
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    var currentIndex by remember { mutableStateOf(initialIndex) }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val newIndex = (listState.firstVisibleItemIndex + centerOffset) % count
            currentIndex = newIndex
            onIndexSelected(newIndex)
        }
    }

    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .height(itemHeight * visibleItemsCount)
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                )
        )

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier
                .height(itemHeight * visibleItemsCount)
                .drawWithContent {
                    drawContent()
                    val fadeHeight = size.height * 0.35f
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(surfaceColor, Color.Transparent),
                            startY = 0f,
                            endY = fadeHeight
                        ),
                        size = Size(size.width, fadeHeight)
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, surfaceColor),
                            startY = size.height - fadeHeight,
                            endY = size.height
                        ),
                        topLeft = Offset(0f, size.height - fadeHeight),
                        size = Size(size.width, fadeHeight)
                    )
                }
        ) {
            items(count * 1000) { index ->
                val itemIndex = index % count
                val isSelected = itemIndex == currentIndex
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[itemIndex],
                        style = if (isSelected) MaterialTheme.typography.headlineMedium
                               else MaterialTheme.typography.titleLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}
