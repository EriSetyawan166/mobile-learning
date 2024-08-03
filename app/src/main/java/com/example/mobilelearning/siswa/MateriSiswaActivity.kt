package com.example.mobilelearning.siswa

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.mobilelearning.Config
import com.example.mobilelearning.Kelas
import com.example.mobilelearning.Materi
import com.example.mobilelearning.MateriAdapter
import com.example.mobilelearning.R
import org.json.JSONException

class MateriSiswaActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_materi_siswa)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val kelasId = intent.getStringExtra("KELAS_ID") ?: return
        val KelasJudul = intent.getStringExtra("JUDUL") ?: return
        findViewById<TextView>(R.id.toolbar_title).text = KelasJudul
        setupRecyclerView(kelasId)
    }

    private fun setupRecyclerView(kelasId: String) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = MateriAdapter()
        recyclerView.adapter = adapter

        fetchMateri(kelasId) { materiList ->
            adapter.submitList(materiList)
        }
    }

    private fun fetchMateri(kelasId: String, callback: (List<Materi>) -> Unit) {
        val url = "${Config.BASE_URL}ambilMateriDariKelas.php?kelas_id=$kelasId"  // Update the endpoint as per actual URL
        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val status = response.getString("status")
                    if (status == "success") {
                        val materiJsonArray = response.getJSONArray("data")
                        val materiList = ArrayList<Materi>()
                        for (i in 0 until materiJsonArray.length()) {
                            val materiObject = materiJsonArray.getJSONObject(i)
                            val id = materiObject.getInt("id")
                            val judul = materiObject.getString("judul")
                            val deskripsi = materiObject.getString("deskripsi")
                            val filePath = materiObject.getString("file_path")
                            val imageResId = when (i % 5) {
                                0 -> R.drawable.gambar_1
                                1 -> R.drawable.gambar_2
                                2 -> R.drawable.gambar_3
                                3 -> R.drawable.gambar_4
                                else -> R.drawable.gambar_5
                            }
                            materiList.add(Materi(id, judul, deskripsi, filePath, imageResId))
                        }
                        callback(materiList)
                    } else {
                        val errorMessage = response.getString("message")
                        Toast.makeText(this@MateriSiswaActivity, errorMessage, Toast.LENGTH_LONG)
                            .show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@MateriSiswaActivity,
                        "Error parsing JSON data",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            { error ->
                Toast.makeText(
                    this@MateriSiswaActivity,
                    "Failed to fetch data: $error",
                    Toast.LENGTH_LONG
                ).show()
            })

        requestQueue.add(jsonObjectRequest)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}