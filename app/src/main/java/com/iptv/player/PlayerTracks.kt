package com.iptv.player
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.media3.common.C
import androidx.media3.common.TrackSelectionOverride
import java.util.Locale

fun showTracks(a: PlayerActivity) {
    val p = a.player ?: return
    val groups = p.currentTracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO || it.type == C.TRACK_TYPE_TEXT }
    if (groups.isEmpty()) { Toast.makeText(a, "لا توجد مسارات إضافية", Toast.LENGTH_SHORT).show(); return }
    val names = mutableListOf<String>(); val acts = mutableListOf<() -> Unit>()
    for (g in groups) for (i in 0 until g.length) {
        val f = g.getTrackFormat(i); val lang = f.language?.takeIf { it != "und" }?.let { Locale(it).displayLanguage }
        names.add((if (g.type == C.TRACK_TYPE_AUDIO) "صوت: " else "ترجمة: ") + (f.label ?: lang ?: "مسار ${i + 1}"))
        acts.add { p.trackSelectionParameters = p.trackSelectionParameters.buildUpon().setTrackTypeDisabled(g.type, false).setOverrideForType(TrackSelectionOverride(g.mediaTrackGroup, i)).build() }
    }
    names.add("إيقاف الترجمة"); acts.add { p.trackSelectionParameters = p.trackSelectionParameters.buildUpon().setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true).build() }
    AlertDialog.Builder(a).setTitle("صوت / ترجمة").setItems(names.toTypedArray()) { _, w -> acts[w]() }.show()
}
