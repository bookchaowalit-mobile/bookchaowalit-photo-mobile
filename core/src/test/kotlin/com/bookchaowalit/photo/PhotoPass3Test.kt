package com.bookchaowalit.photo

import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PhotoPass3Test {
    private val t = Instant.parse("2026-03-01T10:00:00Z")
    private val s = Size(4, 3)

    @Test
    fun duplicatePhotoIdsAreCountedAndListedOnce() {
        // The same photo synced twice must not inflate counts or show twice.
        val p = Photo("p1", t, s, favorite = true, albums = setOf("Trip"))
        val photos = listOf(p, p.copy(), Photo("p2", t.plusSeconds(1), s))
        assertEquals(mapOf("Favorites" to 1, "Trip" to 1), Library.albumCounts(photos))
        assertEquals(listOf("p1"), Library.inAlbum(photos, "Trip").map { it.id })
        assertEquals(listOf("p2", "p1"), Library.timeline(photos, ZoneOffset.UTC).single().second.map { it.id })
    }

    @Test
    fun blankAlbumNamesAreIgnoredAndNamesAreTrimmed() {
        val photos = listOf(
            Photo("a", t, s, albums = setOf("", "  ", "​", " Beach ")),
            Photo("b", t, s, albums = setOf("Beach")),
        )
        assertEquals(mapOf("Beach" to 2), Library.albumCounts(photos))
        assertEquals(listOf("a", "b"), Library.inAlbum(photos, "Beach").map { it.id }.sorted())
    }

    @Test
    fun albumsAreListedInCaseInsensitiveOrder() {
        val photos = listOf(Photo("a", t, s, albums = setOf("beach", "Zoo", "Alps")))
        assertEquals(listOf("Alps", "beach", "Zoo"), Library.albumCounts(photos).keys.toList())
    }

    @Test
    fun justifiedRowsNeverOverflowWhenGapsEatTheContainer() {
        val tall = Size(1, 100)
        val rows = PhotoGeometry.justifiedRows(List(6) { tall }, containerWidth = 10, targetRowHeight = 5, gap = 4)
        assertEquals(6, rows.sumOf { it.size })
        for (row in rows) {
            val width = row.sumOf { it.width } + 4 * (row.size - 1)
            assertTrue(width <= 10, "row $row is $width px wide")
        }
    }
}
