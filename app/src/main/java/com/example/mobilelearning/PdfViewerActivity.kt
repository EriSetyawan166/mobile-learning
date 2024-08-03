package com.example.mobilelearning

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.net.URLEncoder

class PdfViewerActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

        webView = findViewById(R.id.pdfWebView)
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowFileAccessFromFileURLs = true
        webView.settings.allowUniversalAccessFromFileURLs = true

        val pdfFilePath = intent.getStringExtra("PDF_FILE_PATH") ?: ""
        val encodedFilePath = URLEncoder.encode(pdfFilePath, "UTF-8").replace("+", "%20")
        webView.loadUrl("file:///android_asset/pdfjs/web/viewer.html?file=$encodedFilePath")
    }
}