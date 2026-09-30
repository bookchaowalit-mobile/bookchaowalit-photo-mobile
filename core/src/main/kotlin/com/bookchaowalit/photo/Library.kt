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

    /** Album name -> photo count, sorted by name; "Favorites" is a virtual album. */
    fun albumCounts(photos: List<Photo>): Map<String, Int> {
        val counts = sortedMapOf<String, Int>()
        for (p in photos) {
            for (a in p.albums) counts[a] = (counts[a] ?: 0) + 1
            if (p.favorite) counts[FAVORITES] = (counts[FAVORITES] ?: 0) + 1
        }
        return counts
    }

    fun inAlbum(photos: List<Photo>, album: String): List<Photo> =
        photos.filter { if (album == FAVORITES) it.favorite else album in it.albums }.sortedByDescending { it.takenAt }

    const val FAVORITES = "Favorites"
}
