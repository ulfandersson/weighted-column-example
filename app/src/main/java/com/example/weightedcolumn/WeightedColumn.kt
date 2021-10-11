package com.example.weightedcolumn

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import kotlin.math.max
import kotlin.math.min

interface WeightedColumnScope {
    @Stable
    fun Modifier.weight(
        @FloatRange(from = 0.0, fromInclusive = false)
        weight: Float,
        allowedToShrink: Boolean = false,
        fill: Boolean = true
    ) = this.then(

        WeightedColumnData(
            weight = weight,
            allowedToShrink = allowedToShrink,
            fill = fill
        )
    )

    companion object : WeightedColumnScope
}

private class WeightedColumnData(
    val weight: Float,
    val allowedToShrink: Boolean,
    val fill: Boolean = true
) : ParentDataModifier {

    override fun Density.modifyParentData(parentData: Any?) = this@WeightedColumnData

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherModifier = other as? WeightedColumnData ?: return false

        return weight == otherModifier.weight &&
                allowedToShrink == otherModifier.allowedToShrink &&
                fill == otherModifier.fill
    }

    override fun hashCode(): Int {
        var result = weight.hashCode()
        result = 31 * result + allowedToShrink.hashCode()
        result = 31 * result + fill.hashCode()
        return result
    }

    override fun toString(): String {
        return "WeightedColumnData(weight=$weight, allowedToShrink=$allowedToShrink, fill=$fill)"
    }
}

private val Measurable.weightData: WeightedColumnData
    get() = parentData as WeightedColumnData

/**
 * A layout composable that places its children in a vertical sequence. Each children is required
 * to have a weight, and at least one children is expected to have allowedToShrink set as true.
 * All children with allowedToShrink = false will be sized according to their weight. If there is
 * enough room all children with allowedToShrink = true will also be sized according to their weight,
 * they will otherwise shrink and divide whatever space left equally among them.
 */
@Composable
fun WeightedColumn(
    modifier: Modifier = Modifier,
    content: @Composable WeightedColumnScope.() -> Unit
) {
    Layout(
        modifier = modifier,
        content = { WeightedColumnScope.content() }
    ) { measurables, constraints ->
        val totalNonShrinkableWeight = measurables.filter { !it.weightData.allowedToShrink }
            .sumOf { it.weightData.weight.toDouble() }
        val totalShrinkableWeight = measurables.filter { it.weightData.allowedToShrink }
            .sumOf { it.weightData.weight.toDouble() }
        val totalWeight = totalNonShrinkableWeight + totalShrinkableWeight
        var maxWidth = 0
        var nonShrinkableHeight = 0
        // Take all children that are allowed to shrink last and divide the space between them if
        // there is not enough height left to fulfill the wanted weight
        val placeables = measurables.asSequence().withIndex()
            .sortedBy { it.value.weightData.allowedToShrink }
            .mapIndexed { _, (originalIndex, measurable) ->
                val data = measurable.parentData as WeightedColumnData
                val weightedMinHeight = (constraints.minHeight * data.weight / totalWeight).toInt()
                val minHeight = if (data.allowedToShrink) {
                    val heightLeft = (constraints.maxHeight - nonShrinkableHeight).coerceAtLeast(0)
                    if (heightLeft < weightedMinHeight) {
                        // Not enough space left, divide what's left according to weight
                        (heightLeft * data.weight / totalShrinkableWeight).toInt()
                    } else {
                        weightedMinHeight
                    }
                } else {
                    val minHeightNonShrinkable =
                        (constraints.minHeight * data.weight / totalNonShrinkableWeight).toInt()
                    val minHeightRequired = try {
                        measurable.minIntrinsicHeight(constraints.minWidth)
                    } catch (e: IllegalStateException) {
                        // Can't get intrinsic height from LazyColumn etc, just ignore here
                        // TODO: Can we avoid calling this method when we know it will fail?
                        0
                    }
                    if (minHeightRequired > weightedMinHeight) {
                        min(minHeightNonShrinkable, minHeightRequired)
                    } else {
                        weightedMinHeight
                    }
                }
                val maxHeight = if (data.allowedToShrink || !data.fill) {
                    max(weightedMinHeight, minHeight)
                } else {
                    (constraints.maxHeight * data.weight / totalNonShrinkableWeight).toInt()
                }
                val placeable = measurable.measure(
                    constraints.copy(
                        minHeight = minHeight,
                        maxHeight = maxHeight,
                        minWidth = 0
                    )
                )
                maxWidth = max(maxWidth, placeable.width)
                if (!data.allowedToShrink) {
                    nonShrinkableHeight += placeable.height
                }
                IndexedValue(originalIndex, placeable)
            }.sortedBy { it.index }.map { it.value }.toList()

        layout(maxWidth, constraints.maxHeight) {
            var yPosition = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(x = 0, y = yPosition)
                yPosition += placeable.height
            }
        }
    }
}

@Composable
private fun StandardColumnPreview() {
    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Color.Red)
                .weight(1f)
        )
        Box(
            Modifier
                .fillMaxWidth()
                .background(Color.Yellow)
                .weight(2f)
        )
        Box(
            Modifier
                .background(Color.Green)
                .weight(1.5f)
        ) {
            Text("Grön", style = MaterialTheme.typography.h2)
        }
        Box(
            Modifier
                .background(Color.Blue)
                .weight(1f)
        ) {
            Text("Blå", style = MaterialTheme.typography.h2)
        }
    }
}

@Composable
private fun WeightedColumnPreview() {
    WeightedColumn(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Color.Red)
                .weight(1f, allowedToShrink = true)
        )
        Box(
            Modifier
                .fillMaxWidth()
                .background(Color.Yellow)
                .weight(2f, allowedToShrink = true)
        )
        Box(
            Modifier
                .background(Color.Green)
                .weight(1.5f, fill = false)
        ) {
            Text("Grön", style = MaterialTheme.typography.h2)
        }
        Box(
            Modifier
                .background(Color.Blue)
                .weight(1f, fill = false)
        ) {
            Text("Blå", style = MaterialTheme.typography.h2)
        }
    }
}

@Preview(heightDp = 400)
@Composable
private fun StandardColumnPreviewLong() {
    StandardColumnPreview()
}

@Preview(heightDp = 180)
@Composable
private fun StandardColumnPreviewShort() {
    StandardColumnPreview()
}

@Preview(heightDp = 80)
@Composable
private fun StandardColumnPreviewTooShort() {
    StandardColumnPreview()
}

@Preview(heightDp = 400)
@Composable
private fun WeightedColumnPreviewLong() {
    WeightedColumnPreview()
}

@Preview(heightDp = 180)
@Composable
private fun WeightedColumnPreviewShort() {
    WeightedColumnPreview()
}


@Preview(heightDp = 80)
@Composable
private fun WeightedColumnPreviewTooShort() {
    WeightedColumnPreview()
}

@Preview(heightDp = 200)
@Composable
private fun WeightedWithLazy() {
    WeightedColumn(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f, allowedToShrink = true)) {
            Text("Hello", style = MaterialTheme.typography.h2)
        }
        Box(
            Modifier
                .weight(1f, fill = true)
                .fillMaxWidth()
        ) {
            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    Text("Hej", style = MaterialTheme.typography.h2)
                }
                item {
                    Text("Alla", style = MaterialTheme.typography.h2)
                }
                item {
                    Text("Glada")
                }
                item {
                    Text("Personer")
                }
            }
        }
    }
}