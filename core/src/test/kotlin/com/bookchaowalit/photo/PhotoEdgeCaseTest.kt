package com.bookchaowalit.photo

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PhotoEdgeCaseTest {
    private val t0 = Instant.parse("2026-09-30T10:00:00Z")
    private val s = Size(400, 300)

    @Test
    fun favoritePhotoInAUserFavoritesAlbumIsCountedOnce() {
        val photos = listOf(
            Photo("1", t0, s, favorite = true, albums = setOf(Library.FAVORITES, "Trip")),
            Photo("2", t0.minusSeconds(60), s, favorite = false, albums = setOf(Library.FAVORITES)),
            Photo("3", t0.minusSeconds(120), s, favorite = true),
        )
        assertEquals(mapOf("Favorites" to 3, "Trip" to 1), Library.albumCounts(photos))
        assertEquals(listOf("1", "2", "3"), Library.inAlbum(photos, Library.FAVORITES).map { it.id })
        assertEquals(Library.albumCounts(photos)[Library.FAVORITES], Library.inAlbum(photos, Library.FAVORITES).size)
    }

    @Test
    fun emptyLibrary() {
        assertTrue(Library.albumCounts(emptyList()).isEmpty())
        assertTrue(Library.timeline(emptyList(), ZoneOffset.UTC).isEmpty())
        assertTrue(Library.inAlbum(emptyList(), "x").isEmpty())
    }

    @Test
    fun timelineUsesTheViewersZone() {
        val p = Photo("late", Instant.parse("2026-09-30T20:00:00Z"), s)
        assertEquals(LocalDate.of(2026, 9, 30), Library.timeline(listOf(p), ZoneOffset.UTC)[0].first)
        assertEquals(LocalDate.of(2026, 10, 1), Library.timeline(listOf(p), ZoneOffset.ofHours(7))[0].first)
    }

    @Test
    fun sizeAndRotationValidation() {
        assertFailsWith<IllegalArgumentException> { Size(0, 1) }
        assertFailsWith<IllegalArgumentException> { Size(1, -1) }
        assertFailsWith<IllegalArgumentException> { PhotoGeometry.rotate(s, 45) }
        assertEquals(Size(300, 400), PhotoGeometry.rotate(s, -90))
        assertEquals(s, PhotoGeometry.rotate(s, 360))
        assertEquals(Size(300, 400), PhotoGeometry.rotate(s, 450))
    }

    @Test
    fun cropStaysInsideExtremeImages() {
        for (src in listOf(Size(1, 1000), Size(1000, 1), Size(1, 1), Size(4000, 3000))) {
            for (ratio in CropRatio.entries) {
                val r = PhotoGeometry.centerCrop(src, ratio)
                assertTrue(r.left >= 0 && r.top >= 0 && r.width >= 1 && r.height >= 1, "$src $ratio $r")
                assertTrue(r.left + r.width <= src.width && r.top + r.height <= src.height, "$src $ratio $r")
            }
        }
    }

    @Test
    fun fitWithinTinyBoundsKeepsAtLeastOnePixel() {
        assertEquals(Size(1, 1), PhotoGeometry.fitWithin(Size(4000, 10), Size(1, 1)))
        assertEquals(Size(10, 10), PhotoGeometry.fitWithin(Size(10, 10), Size(1000, 1000)))
    }

    @Test
    fun justifiedRowsKeepEveryPhotoInOrderAndRowsExact() {
        val photos = List(23) { i -> Size(300 + (i * 37) % 500, 200 + (i * 53) % 400) }
        val rows = PhotoGeometry.justifiedRows(photos, containerWidth = 1080, targetRowHeight = 220, gap = 6)
        assertEquals(photos.size, rows.sumOf { it.size })
        for (row in rows.dropLast(1)) {
            assertEquals(1080 - 6 * (row.size - 1), row.sumOf { it.width })
            assertEquals(1, row.map { it.height }.distinct().size)
            assertTrue(row[0].height <= 220)
        }
        assertTrue(rows.last().sumOf { it.width } + 6 * (rows.last().size - 1) <= 1080)
        assertTrue(PhotoGeometry.justifiedRows(emptyList(), 100, 100).isEmpty())
        assertFailsWith<IllegalArgumentException> { PhotoGeometry.justifiedRows(photos, 0, 100) }
    }
}
