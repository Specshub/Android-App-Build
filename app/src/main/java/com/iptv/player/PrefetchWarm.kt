package com.iptv.player
import android.content.Context
import com.iptv.player.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withPermit

fun Prefetch.warm(x: Context, c: LoginCredentials) {
    scope.launch {
        launch { vodCats(x, c)?.filter { !adultCat(it.categoryName) }?.take(12)?.forEach { k -> launch { sem.withPermit { vodCat(x, c, k.categoryId) } } } }
        launch { seriesCats(x, c)?.filter { !adultCat(it.categoryName) }?.take(12)?.forEach { k -> launch { sem.withPermit { seriesCat(x, c, k.categoryId) } } } }
        launch { liveCats(x, c)?.filter { !adultCat(it.categoryName) }?.take(10)?.forEach { k -> launch { sem.withPermit { liveCat(x, c, k.categoryId) } } } }
    }
}
