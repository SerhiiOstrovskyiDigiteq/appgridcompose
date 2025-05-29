package com.digiteq.appgridcompose.ui.pagergrid

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.gestures.snapping.snapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun PagerGrid(
    modifier: Modifier = Modifier,
    rows: Int,
    columns: Int,
    visibleWidth: Int,
    distanceBetweenColumns: Dp = 0.dp,
    distanceBetweenRows: Dp = 0.dp,
    distanceBetweenPages: Dp = 0.dp,
    contentPaddingHorizontal: Dp = 0.dp,
    contentPaddingVertical: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    if (visibleWidth == 0)
        return

    val state = rememberScrollState()
    Layout(
        modifier = modifier
        .horizontalScroll(
            state = state,
            flingBehavior = remember { PagerGridFlingBehaviour(state, visibleWidth) }
        ),
//            .scrollable(
//            state = rememberScrollState(),
//            orientation = Orientation.Horizontal
//        ),
        content = content,
    ) { measurables, constraints ->
        val distanceBetweenColumnsPx = distanceBetweenColumns.roundToPx()
        val distanceBetweenRowsPx = distanceBetweenRows.roundToPx()
        val distanceBetweenPagesPx = distanceBetweenPages.roundToPx()
        val contentPaddingHorizontalPx = contentPaddingHorizontal.roundToPx()
        val contentPaddingVerticalPx = contentPaddingVertical.roundToPx()

        val pageSize = columns * rows
        val pageCount = measurables.chunked(pageSize).size

        // Calculate the size of each item based on the available space and the number of rows and columns
        val itemHeight =
            ((constraints.maxHeight - contentPaddingVerticalPx * 2) - (distanceBetweenRowsPx * (rows - 1))) / rows
        val itemWidth =
            ((visibleWidth - contentPaddingHorizontalPx * 2) - (distanceBetweenColumnsPx * (columns - 1))) / columns

        // Create new constraints for the grid items
        val itemConstraints = Constraints(
            minWidth = itemWidth,
            minHeight = itemHeight,
            maxWidth = itemWidth,
            maxHeight = itemHeight
        )

        // Measure the items with the new constraints
        val placeables = measurables.map { measurable ->
            measurable.measure(itemConstraints)
        }

        // Calculate the total grid (page) size. visibleWidth is the value of grid (page) width.
        val gridPageHeight =
            itemHeight * rows + distanceBetweenRowsPx * (rows - 1) + contentPaddingVerticalPx * 2
        val gridPageWidth =
            itemWidth * columns + distanceBetweenColumnsPx * (columns - 1) + contentPaddingHorizontalPx * 2
        Log.d("PagerGrid", "gridPageHeight = $gridPageHeight")

        // Set the size of the Layout to be the total grid size
        layout(
            gridPageWidth * pageCount + distanceBetweenPagesPx * (pageCount - 1),
            gridPageHeight
        ) {
            var startX = contentPaddingHorizontalPx
            var x = contentPaddingHorizontalPx
            var y = contentPaddingVerticalPx

            // Place each item within the grid
            placeables.forEach { placeable ->
                placeable.placeRelative(x, y)
                x += itemWidth + distanceBetweenColumnsPx
                if (x >= startX + (gridPageWidth - contentPaddingHorizontalPx * 2)) {
                    x = startX
                    y += itemHeight + distanceBetweenRowsPx
                    if (y >= gridPageHeight - contentPaddingVerticalPx) {
                        startX += gridPageWidth + distanceBetweenPagesPx
                        x = startX
                        y = contentPaddingVerticalPx
                    }
                }
            }
        }
    }
}

private class PagerGridScrollState(
    private val value: ScrollState
) {

}

private class PagerGridFlingBehaviour(
    private val scrollState: ScrollState,
    private val pageWidth: Int
): FlingBehavior {
    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        val scope: ScrollScope = this
        return if (abs(initialVelocity) >= 0f) {
            //var distanceToScroll = 0f
            Log.d("PagerGridFlingBehaviour", "initialVelocity = $initialVelocity; pageWidth: $pageWidth")
            scrollBy(pageWidth.toFloat() - scrollState.value)// + 400f
            initialVelocity
        } else initialVelocity
    }
}

@Preview(showBackground = true)
@Composable
fun SquareGridPreview() {
    MaterialTheme {
        var visibleWidth by remember { mutableIntStateOf(0) }
        val random = Random(1624)
        Box(
            Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    Log.d("SquareGridPreview", "Box Constraints: ${it.size}")
                    visibleWidth = it.size.width
                }) {
            Log.d("SquareGridPreview", "visibleWidth $visibleWidth")
//            Box(modifier = Modifier.fillMaxHeight().width(10.dp).background(Color.White))
            PagerGrid(
                modifier = Modifier
                    .fillMaxSize(),
                rows = 6,
                columns = 4,
                distanceBetweenRows = 2.dp,
                distanceBetweenColumns = 2.dp,
                distanceBetweenPages = 50.dp,
                contentPaddingHorizontal = 30.dp,
                contentPaddingVertical = 30.dp,
                visibleWidth = visibleWidth
            ) {
                repeat(90) { index ->
                    Text(
                        modifier = Modifier
                            .background(Color(random.nextLong()).copy(alpha = 1f))
                            .wrapContentHeight(),
                        text = index.toString(),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}