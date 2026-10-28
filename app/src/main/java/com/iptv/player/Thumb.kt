package com.iptv.player

private val tmdbSize = Regex("(image\\.tmdb\\.org/t/p/)[^/]+/")
fun thumb(u: String?): String? = if (u.isNullOrBlank()) u else tmdbSize.replace(u) { it.groupValues[1] + "w342/" }
