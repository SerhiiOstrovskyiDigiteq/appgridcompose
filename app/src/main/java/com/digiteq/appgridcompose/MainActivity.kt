package com.digiteq.appgridcompose

import android.content.ClipData
import android.content.ClipDescription
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.digiteq.appgridcompose.ui.pagergrid.SquareGridPreview
import com.digiteq.appgridcompose.ui.theme.AppGridComposeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

val rnd = Random(13)

val columnCount = 4
val rowCount = 7
val pageSize = rowCount * columnCount

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppGridComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SquareGridPreview()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGrid() {
    val widthDp = LocalConfiguration.current.screenWidthDp
    val heightDp = LocalConfiguration.current.screenHeightDp
    val rowSizeDp = heightDp / rowCount
    val coroutineScope = rememberCoroutineScope()

    val density = LocalDensity.current;
    val configuration = LocalConfiguration.current;
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }
    var pendingScroll = remember {
        false
    }
    val gridCellList = remember {
        mutableStateListOf<AppShortcut>()
    }
    var draggedItemKey: String? = remember {
        null
    }
    gridCellList.addAll(
        (1..100).toList().map { AppShortcut(it, color = Color(rnd.nextLong()).copy(alpha = 1f)) })

    val pagerState = rememberPagerState(pageCount = { gridCellList.chunked(pageSize).size })
    val gridStateList = remember {
        gridCellList.chunked(pageSize).map { LazyGridState() }
    }

    HorizontalPager(
        modifier = Modifier
            .fillMaxSize()
            .dragAndDropTarget(
                shouldStartDragAndDrop = { startEvent ->
                    startEvent
                        .mimeTypes()
                        .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                },
                target = remember {
                    object : DragAndDropTarget {
                        override fun onDrop(event: DragAndDropEvent): Boolean {
                            draggedItemKey = null
                            Log.d(
                                "DnD grid",
                                "dropped ${event.toAndroidDragEvent().clipData.getItemAt(0).text}"
                            )
                            return true
                        }

                        override fun onMoved(event: DragAndDropEvent) {
                            Log.d("DnD moved", event.toAndroidDragEvent().x.toString())
                            when {
                                event.toAndroidDragEvent().x > screenWidthPx - 100 -> {
                                    if (!pendingScroll) {
                                        pendingScroll = true
                                        coroutineScope.launch {
                                            delay(1500)
                                            if (pendingScroll) {
                                                val positionToScroll =
                                                    if (pagerState.currentPage + 1 >= pagerState.pageCount) pagerState.currentPage else pagerState.currentPage + 1
                                                Log.d(
                                                    "DnD positionToScroll",
                                                    positionToScroll.toString()
                                                )
                                                pagerState.animateScrollToPage(positionToScroll)
                                                pendingScroll = false
                                            }
                                        }
                                    }
                                }

                                event.toAndroidDragEvent().x < 100 -> {
                                    if (!pendingScroll) {
                                        pendingScroll = true
                                        coroutineScope.launch {
                                            delay(1500)
                                            if (pendingScroll) {
                                                val positionToScroll =
                                                    if (pagerState.currentPage == 0) 0 else pagerState.currentPage - 1
                                                pagerState.animateScrollToPage(positionToScroll)
                                                pendingScroll = false
                                            }
                                        }
                                    }
                                }

                                else -> pendingScroll = false
                            }
                            val hoveredItem =
                                gridStateList[pagerState.currentPage].layoutInfo.visibleItemsInfo.find {
                                    event.toAndroidDragEvent().x.toInt() in it.offset.x..it.offset.x + it.size.width
                                            && event.toAndroidDragEvent().y.toInt() in it.offset.y..it.offset.y + it.size.height
                                }
                            val hoveredCellIdx =
                                gridCellList.indexOfFirst { it.title == hoveredItem?.key.toString() }
                            val draggedCellIdx =
                                gridCellList.indexOfFirst { it.title == draggedItemKey }
                            Log.d("DnD hoveredOn", "dgd: $draggedCellIdx, hvd: $hoveredCellIdx")
                            if (draggedCellIdx in 0..gridCellList.size && hoveredCellIdx in 0..gridCellList.size && hoveredCellIdx != draggedCellIdx) {
                                Log.d("DnD replaced", "bam!")
                                gridCellList.add(
                                    hoveredCellIdx,
                                    gridCellList.removeAt(draggedCellIdx)
                                )
//                                gridCellList.swapList(gridCellList)
                            }
                            super.onMoved(event)
                        }
                    }
                }
            ),
        state = pagerState,
        verticalAlignment = Alignment.Top
    ) { pageIdx ->
        val page = gridCellList.chunked(pageSize)[pageIdx]
        LazyVerticalGrid(
            modifier = Modifier
                .width(widthDp.dp)
                .padding(horizontal = 30.dp),
            columns = GridCells.Fixed(columnCount),
            userScrollEnabled = false,
            state = gridStateList[pageIdx]
        ) {
            items(page, key = { it.id }) { appShortcut ->
                AppItem(
                    modifier = Modifier
                        .height(rowSizeDp.dp)
                        .dragAndDropSource {
                            detectTapGestures(
                                onLongPress = {
                                    draggedItemKey = appShortcut.title
                                    Log.d(
                                        "DnD start drag",
                                        "dgdKey: $draggedItemKey, ttle: ${appShortcut.title}"
                                    )
                                    startTransfer(
                                        transferData = DragAndDropTransferData(
                                            clipData = ClipData.newPlainText(
                                                "id",
                                                appShortcut.id.toString()
                                            )
                                        )
                                    )
                                }
                            )
                        },
                    appShortcut = appShortcut
                )
            }
        }
    }
}

fun <T> SnapshotStateList<T>.swapList(newList: List<T>){
    clear()
    addAll(newList)
}

fun dpToPx(dp: Int): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}

@Composable
fun AppItem(modifier: Modifier = Modifier, appShortcut: AppShortcut) {
    val clr = Color(rnd.nextInt(0, 255), rnd.nextInt(0, 255), rnd.nextInt(0, 255))
    Text(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .background(appShortcut.color)
            .alpha(if (appShortcut.isDragged) 0.1f else 1f)
            .wrapContentHeight(align = Alignment.CenterVertically),
        text = appShortcut.title,
        textAlign = TextAlign.Center,
    )
}

@Preview(showBackground = true)
@Composable
fun ItemPreview() {
//    AppItem(text = "1")
}