package com.example.miniyam.Graphics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.geometry.Rect

@Stable
data class GraphicsLayerRecordingState(
    val graphicsLayer: GraphicsLayerScope,
    val region: MutableState<Region>
) {
    sealed class Region {
        data class Rectangle(val rect: Rect) : Region()
    }

    fun setRectRegion(rect: Rect) {
        this.region.value = Region.Rectangle(rect)
    }
}

@Composable
fun rememberGraphicsLayerRecordingState(
    regionState: MutableState<GraphicsLayerRecordingState.Region> = rememberGraphicsLayerRecordingRegion()
): GraphicsLayerRecordingState {
    val graphicsLayer = GraphicsLayerScope()

    return remember(graphicsLayer, regionState) {
        GraphicsLayerRecordingState(
            graphicsLayer = graphicsLayer,
            region = regionState
        )
    }
}

@Composable
fun rememberGraphicsLayerRecordingRegion(
    initialRect: Rect = Rect(0f, 0f, 0f, 0f)
): MutableState<GraphicsLayerRecordingState.Region> {
    return remember {
        mutableStateOf(
            GraphicsLayerRecordingState.Region.Rectangle(
                rect = initialRect
            )
        )
    }
}