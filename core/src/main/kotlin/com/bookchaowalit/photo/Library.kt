package com.bookchaowalit.photo

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class Photo(val id: String, val takenAt: Instant, val size: Size, val favorite: Boolean = false, val albums: Set<String> = emptySet())

object Library {
    /** Timeline sections: newest day first, photos newest first within a day. */
    fun timeline(photos: List<Photo>, zone: ZoneId): List<Pair<LocalDate, List<Photo>>> =
        unique(photos).sortedByDescending { it.takenAt }
            .groupBy { it.takenAt.atZone(zone).toLocalDate() }
            .toList()

    /**
     * Album name -> photo count, sorted by name (case-insensitive). "Favorites"
     * is a virtual album (photos marked favorite); a user album with the same
     * name is merged into it, and each photo (by id) is counted at most once
     * per album. Album names are trimmed; blank names are ignored.
     */
    fun albumCounts(photos: List<Photo>): Map<String, Int> {
        val counts = sortedMapOf<String, Int>(String.CASE_INSENSITIVE_ORDER.thenComparing(naturalOrder()))
        for (p in unique(photos)) {
            for (a in albumsOf(p)) counts[a] = (counts[a] ?: 0) + 1
        }
        return counts
    }

    fun inAlbum(photos: List<Photo>, album: String): List<Photo> {
        val name = cleanAlbumName(album) ?: return emptyList()
        return unique(photos).filter { name in albumsOf(it) }.sortedByDescending { it.takenAt }
    }

    /** The same photo synced/imported twice must not be listed or counted twice; first copy wins. */
    private fun unique(photos: List<Photo>): List<Photo> = photos.distinctBy { it.id }

    private fun albumsOf(p: Photo): Set<String> {
        val named = p.albums.mapNotNullTo(mutableSetOf(), ::cleanAlbumName)
        return if (p.favorite) named + FAVORITES else named
    }

    /** Trims the name; null for names that are blank or only zero-width characters. */
    private fun cleanAlbumName(raw: String): String? {
        val name = raw.filterNot { it in INVISIBLE }.trim()
        return name.ifEmpty { null }
    }

    private val INVISIBLE = setOf('\u200B', '\u200C', '\u200D', '\u2060', '\uFEFF')

    const val FAVORITES = "Favorites"
}
