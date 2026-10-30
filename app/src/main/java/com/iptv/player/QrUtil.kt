package com.iptv.player
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

fun makeQr(text: String, px: Int): Bitmap {
    val m = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, px, px, mapOf(EncodeHintType.MARGIN to 2, EncodeHintType.CHARACTER_SET to "UTF-8"))
    val pix = IntArray(px * px) { i -> if (m.get(i % px, i / px)) Color.BLACK else Color.WHITE }
    return Bitmap.createBitmap(pix, px, px, Bitmap.Config.ARGB_8888)
}
fun AppCompatActivity.showQr(url: String) {
    val iv = findViewById<ImageView>(R.id.ivQr) ?: return
    val d = resources.displayMetrics; val dp = (minOf(d.widthPixels, d.heightPixels) / d.density * 0.30f).coerceIn(84f, 170f); val px = (dp * d.density).toInt()
    iv.layoutParams = iv.layoutParams.apply { width = px; height = px }
    iv.setImageBitmap(makeQr(url, px))
}
