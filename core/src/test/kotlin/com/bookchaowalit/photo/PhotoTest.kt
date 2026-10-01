package com.bookchaowalit.photo

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PhotoTest {
    @Test
    fun fitWithinNeverUpscales() {
        assertEquals(Size(1080, 810), PhotoGeometry.fitWithin(Size(4000, 3000), Size(1080, 1080)))
        assertEquals(Size(300, 200), PhotoGeometry.fitWithin(Size(300, 200), Size(1080, 1080)))
        assertFailsWith<IllegalArgumentException> { Size(0, 10) }
    }

    @Test
    fun centerCrop() {
        assertEquals(Rect(500, 0, 3000, 3000), PhotoGeometry.centerCrop(Size(4000, 3000), CropRatio.SQUARE))
        assertEquals(Rect(0, 656, 3000, 1688), PhotoGeometry.centerCrop(Size(3000, 3000), CropRatio.LANDSCAPE_16_9))
        assertEquals(Rect(0, 166, 4000, 2667), PhotoGeometry.centerCrop(Size(4000, 3000), CropRatio.CLASSIC_3_2))
        val portrait = PhotoGeometry.centerCrop(Size(4000, 3000), CropRatio.PORTRAIT_4_5)
        assertEquals(2400, portrait.width)
        assertEquals(800, portrait.left)
    }

    @Test
    fun rotateSwapsDimensions() {
        assertEquals(Size(3, 4), PhotoGeometry.rotate(Size(4, 3), 90))
        assertEquals(Size(4, 3), PhotoGeometry.rotate(Size(4, 3), -180))
        assertEquals(Size(3, 4), PhotoGeometry.rotate(Size(4, 3), 270))
        assertFailsWith<IllegalArgumentException> { PhotoGeometry.rotate(Size(4, 3), 45) }
    }

    @Test
    fun justifiedRowsFillContainerWidth() {
        val photos = listOf(Size(400, 300), Size(300, 400), Size(1600, 900), Size(500, 500), Size(640, 480), Size(100, 100))
        val rows = PhotoGeometry.justifiedRows(photos, containerWidth = 1000, targetRowHeight = 250, gap = 4)
        assertEquals(photos.size, rows.sumOf { it.size })
        for (row in rows.dropLast(1)) {
            assertEquals(1000, row.sumOf { it.width } + 4 * (row.size - 1))
            assertTrue(row.all { it.height <= 250 })
            assertEquals(1, row.map { it.height }.toSet().size)
        }
        assertTrue(rows.last().all { it.height == 250 })
    }

    @Test
    fun timelineAndAlbums() {
        val d1 = Instant.parse("2026-09-01T08:00:00Z")
        val photos = listOf(
            Photo("a", d1, Size(1, 1), albums = setOf("Trips")),
            Photo("b", d1.plusSeconds(3600), Size(1, 1), favorite = true),
            Photo("c", d1.plusSeconds(86400), Size(1, 1), favorite = true, albums = setOf("Trips", "Food")),
        )
        val tl = Library.timeline(photos, ZoneOffset.UTC)
        assertEquals(listOf(LocalDate.of(2026, 9, 2), LocalDate.of(2026, 9, 1)), tl.map { it.first })
        assertEquals(listOf("b", "a"), tl[1].second.map { it.id })
        assertEquals(mapOf("Favorites" to 2, "Food" to 1, "Trips" to 2), Library.albumCounts(photos))
        assertEquals(listOf("c", "b"), Library.inAlbum(photos, Library.FAVORITES).map { it.id })
        assertEquals(listOf("c", "a"), Library.inAlbum(photos, "Trips").map { it.id })
    }
}
