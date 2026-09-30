package com.bookchaowalit.photo

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class Photo(val id: String, val takenAt: Instant, val size: Size, val favorite: Boolean = false, val albums: Set<String> = emptySet())

object Library {
    /** Timeline sections: newest day first, photos newest first within a day. */
    fun timeline(photos: List<Photo>, zone: ZoneId): List<Pair<LocalDate, List<Photo>>> =
        photos.sortedByDescending { it.takenAt }
            .groupBy { it.takenAt.atZone(zone).toLocalDate() }
            .toList()

    /**
     * Album name -> photo count, sorted by name. "Favorites" is a virtual album
     * (photos marked favorite); a user album with the same name is merged into
     * it, and each photo is counted at most once per album.
     */
    fun albumCounts(photos: List<Photo>): Map<String, Int> {
        val counts = sortedMapOf<String, Int>()
        for (p in photos) {
            for (a in albumsOf(p)) counts[a] = (counts[a] ?: 0) + 1
        }
        return counts
    }

    fun inAlbum(photos: List<Photo>, album: String): List<Photo> =
        photos.filter { album in albumsOf(it) }.sortedByDescending { it.takenAt }

    private fun albumsOf(p: Photo): Set<String> = if (p.favorite) p.albums + FAVORITES else p.albums

    const val FAVORITES = "Favorites"
}
