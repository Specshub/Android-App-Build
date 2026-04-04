package com.iptv.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.fragment.app.Fragment

class MatchScheduleFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_match_schedule, container, false)
        
        val webView = view.findViewById<WebView>(R.id.webView_schedule)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)

        // إعدادات المتصفح الداخلي ليكون سريعاً وسلساً
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // إخفاء دائرة التحميل عند انتهاء ظهور الجدول
                progressBar.visibility = View.GONE
            }
        }

        // سنستخدم مصدر "يلا كورة" أو "في الجول" لأنهما الأفضل والأسرع للجداول العربية
        // يمكنك تغيير الرابط لاحقاً لأي مصدر تفضله
        webView.loadUrl("https://www.yallakora.com/match-center/")

        return view
    }
}
