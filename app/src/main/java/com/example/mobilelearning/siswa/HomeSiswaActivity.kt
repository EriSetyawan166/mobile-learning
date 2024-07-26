package com.example.mobilelearning.siswa

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.mobilelearning.Kelas
import com.example.mobilelearning.KelasAdapter
import com.example.mobilelearning.R
import org.json.JSONObject

class HomeSiswaActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var kelasAdapter: KelasAdapter
    private lateinit var kelasList: ArrayList<Kelas>
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_siswa)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        kelasList = ArrayList()
        kelasAdapter = KelasAdapter(kelasList)
        recyclerView.adapter = kelasAdapter
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            fetchClasses()
        }

        fetchClasses()
    }

    private fun fetchClasses() {
        val queue = Volley.newRequestQueue(this)
        val url = "http://192.168.100.121/mobile_learning_api/ambilKelas.php"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                parseData(response)
                swipeRefreshLayout.isRefreshing = false  // Stop the refreshing indicator
            },
            { error ->
                // Handle error
                error.printStackTrace()
                swipeRefreshLayout.isRefreshing = false  // Stop the refreshing indicator
            }
        )

        queue.add(jsonObjectRequest)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun parseData(response: JSONObject) {
        val status = response.getString("status")
        if (status == "success") {
            kelasList.clear()
            val dataArray = response.getJSONArray("data")
            for (i in 0 until dataArray.length()) {
                val item = dataArray.getJSONObject(i)
                val kelas = Kelas(
                    id = item.getString("id"),
                    judul = item.getString("judul"),
                    sub_judul = item.getString("sub_judul"),
                    deskripsi = item.getString("deskripsi")
                )
                kelasList.add(kelas)
            }
            kelasAdapter.notifyDataSetChanged()
        }
    }
}

