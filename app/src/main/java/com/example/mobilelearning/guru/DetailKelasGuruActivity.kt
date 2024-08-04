package com.example.mobilelearning.guru

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobilelearning.R
import com.example.mobilelearning.siswa.MateriSiswaActivity

class DetailKelasGuruActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    lateinit var button: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_kelas_guru)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val kelas_id = intent.getStringExtra("KELAS_ID") ?: ""
        val judul = intent.getStringExtra("JUDUL") ?: ""
        val deskripsi = intent.getStringExtra("DESKRIPSI") ?: ""

        findViewById<TextView>(R.id.toolbar_title).text = judul
        findViewById<TextView>(R.id.deskripsiTextView).text = deskripsi

        findViewById<Button>(R.id.masukKeMataPelajaranButton).setOnClickListener {
            val intent = Intent(this, MateriGuruActivity::class.java)
            intent.putExtra("KELAS_ID", kelas_id)
            intent.putExtra("JUDUL", judul)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}