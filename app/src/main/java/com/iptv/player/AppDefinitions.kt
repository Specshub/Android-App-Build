package com.iptv.player

// 1. تعريف الفئة المفقودة التي يشتكي منها CategoryAdapter
data class GenericCategory(
    val id: String,
    val name: String
)

// 2. تعريف الثوابت المفقودة التي تطلبها LoginActivity
const val EXTRA_HOST = "EXTRA_HOST"
const val EXTRA_USERNAME = "EXTRA_USERNAME"
const val EXTRA_PASSWORD = "EXTRA_PASSWORD"
