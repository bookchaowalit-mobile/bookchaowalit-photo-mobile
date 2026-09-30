package com.bookchaowalit.photo

import kotlin.math.roundToInt

data class Size(val width: Int, val height: Int) {
    init { require(width > 0 && height > 0) { "size must be positive: ${width}x$height" } }
    val aspect: Double get() = width.toDouble() / height
}

data class Rect(val left: Int, val top: Int, val width: Int, val height: Int)

/** Named crop ratios offered in the editor. */
enum class CropRatio(val w: Int, val h: Int) { SQUARE(1, 1), PORTRAIT_4_5(4, 5), LANDSCAPE_16_9(16, 9), CLASSIC_3_2(3, 2) }

object PhotoGeometry {
    /** Scale [source] to fit inside [bounds] without upscaling, keeping aspect ratio. */
    fun fitWithin(source: Size, bounds: Size): Size {
        val scale = minOf(bounds.width.toDouble() / source.width, bounds.height.toDouble() / source.height, 1.0)
        return Size(maxOf(1, (source.width * scale).roundToInt()), maxOf(1, (source.height * scale).roundToInt()))
    }

    /** Largest centred crop of [source] with the given ratio. */
    fun centerCrop(source: Size, ratio: CropRatio): Rect {
        val target = ratio.w.toDouble() / ratio.h
        return if (source.aspect > target) {
            val w = (source.height * target).roundToInt()
            Rect((source.width - w) / 2, 0, w, source.height)
        } else {
            val h = (source.width / target).roundToInt()
            Rect(0, (source.height - h) / 2, source.width, h)
        }
    }

    /** Size after rotating by [degrees] (multiples of 90 only). */
    fun rotate(size: Size, degrees: Int): Size {
        require(degrees % 90 == 0) { "only right-angle rotations are supported" }
        return if ((degrees / 90) % 2 == 0) size else Size(size.height, size.width)
    }

    /**
     * Justified gallery rows (Google Photos / Flickr style): photos keep their
     * aspect ratio, each full row is scaled to exactly [containerWidth] minus
     * gaps, and rows aim for [targetRowHeight]. The last row is not stretched.
     * Returns, per row, the laid-out sizes in input order.
     */
    fun justifiedRows(photos: List<Size>, containerWidth: Int, targetRowHeight: Int, gap: Int = 4): List<List<Size>> {
        require(containerWidth > 0 && targetRowHeight > 0 && gap >= 0)
        val rows = mutableListOf<List<Size>>()
        var row = mutableListOf<Size>()
        var aspectSum = 0.0
        for (p in photos) {
            // With narrow containers the gaps alone can use up the width; close the
            // row while every photo can still get at least 1px instead of overflowing.
            if (row.isNotEmpty() && containerWidth - gap * row.size < row.size + 1) {
                val available = containerWidth - gap * (row.size - 1)
                rows += scaleRow(row, available, available / aspectSum)
                row = mutableListOf(); aspectSum = 0.0
            }
            row += p
            aspectSum += p.aspect
            val available = containerWidth - gap * (row.size - 1)
            val height = available / aspectSum
            if (height <= targetRowHeight) {
                rows += scaleRow(row, available, height)
                row = mutableListOf(); aspectSum = 0.0
            }
        }
        if (row.isNotEmpty()) rows += row.map { Size(maxOf(1, (targetRowHeight * it.aspect).roundToInt()), targetRowHeight) }
        return rows
    }

    private fun scaleRow(row: List<Size>, available: Int, height: Double): List<Size> {
        val h = maxOf(1, height.roundToInt())
        val widths = row.map { (it.aspect * height).roundToInt().coerceAtLeast(1) }.toMutableList()
        // Absorb rounding error in the last photo so the row is exactly `available` wide.
        widths[widths.lastIndex] = maxOf(1, widths.last() + available - widths.sum())
        return widths.map { Size(it, h) }
    }
}
