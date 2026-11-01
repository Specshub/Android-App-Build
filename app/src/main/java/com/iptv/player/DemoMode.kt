package com.iptv.player
import com.iptv.player.data.model.LoginCredentials

// Built-in demo provider hosted on our site (open-licensed sample media), used by the "OK" button before a playlist is added.
const val DEMO_HOST = "https://tajplayer.com/demo"
fun demoCreds() = LoginCredentials(DEMO_HOST, "demo", "demo")
// The demo live channels are HLS (.m3u8); real providers use .ts
fun liveExt(host: String): String = if (host.startsWith(DEMO_HOST)) "m3u8" else "ts"
