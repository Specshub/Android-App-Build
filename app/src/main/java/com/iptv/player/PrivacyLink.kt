package com.iptv.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

fun openPrivacy(c: Context) {
    try { c.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.PRIVACY_URL)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    catch (e: Exception) { Toast.makeText(c, Constants.PRIVACY_URL, Toast.LENGTH_LONG).show() }
}

// Small underlined link used on the activation screen.
fun privacyLinkView(c: Context): TextView {
    val t = TextView(c)
    t.text = c.getString(R.string.privacy_policy); t.setTextColor(0xB3FFFFFF.toInt()); t.textSize = 12f; t.gravity = Gravity.CENTER
    t.paintFlags = t.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
    val d = (c.resources.displayMetrics.density * 10).toInt(); t.setPadding(d, d, d, d)
    t.isClickable = true; t.isFocusable = true; t.setOnClickListener { openPrivacy(c) }
    t.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    return t
}

// Settings row that looks like the existing "theme" button (copies its look).
fun addPrivacyRow(a: android.app.Activity, paneId: Int, template: TextView) {
    val pane = a.findViewById<ViewGroup>(paneId) ?: return
    val t = TextView(a)
    t.text = a.getString(R.string.privacy_policy)
    template.background?.constantState?.newDrawable()?.let { t.background = it }
    t.setPadding(template.paddingLeft, template.paddingTop, template.paddingRight, template.paddingBottom)
    t.gravity = template.gravity; t.setTextColor(template.currentTextColor); t.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, template.textSize)
    t.isClickable = true; t.isFocusable = true; t.setOnClickListener { openPrivacy(a) }
    val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    lp.topMargin = (a.resources.displayMetrics.density * 6).toInt(); t.layoutParams = lp
    pane.addView(t)
}
