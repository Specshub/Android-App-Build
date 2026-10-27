package com.iptv.player
import android.app.Activity
import android.app.Application
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class AurumApp : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(a: Activity) { fitSystemBars(a) }
            override fun onActivityCreated(a: Activity, s: Bundle?) {}
            override fun onActivityResumed(a: Activity) {}
            override fun onActivityPaused(a: Activity) {}
            override fun onActivityStopped(a: Activity) {}
            override fun onActivitySaveInstanceState(a: Activity, s: Bundle) {}
            override fun onActivityDestroyed(a: Activity) {}
        })
    }
}
fun fitSystemBars(a: Activity) {
    if (a is LiveActivity || a is PlayerActivity) return
    val v = a.findViewById<android.view.View>(android.R.id.content) ?: return
    a.window.setBackgroundDrawable(ColorDrawable(0xFF050505.toInt()))
    WindowCompat.getInsetsController(a.window, v).isAppearanceLightStatusBars = false
    ViewCompat.setOnApplyWindowInsetsListener(v) { view, ins ->
        val b = ins.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        view.setPadding(b.left, b.top, b.right, b.bottom); WindowInsetsCompat.CONSUMED
    }
    ViewCompat.requestApplyInsets(v)
}
