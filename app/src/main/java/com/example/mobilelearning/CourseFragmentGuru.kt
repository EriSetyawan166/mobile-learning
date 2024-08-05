package com.example.mobilelearning

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.mobilelearning.guru.DetailKelasGuruActivity
import com.example.mobilelearning.siswa.DetailKelasSiswaActivity
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject

class CourseFragmentGuru : Fragment() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var kelasAdapter: KelasAdapter
    private lateinit var kelasList: ArrayList<Kelas>
    private lateinit var kelompok: String
    private lateinit var role: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_course, container, false)
        kelompok = arguments?.getString("kelompok") ?: ""
        role  = arguments?.getString("role") ?: ""

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        kelasList = ArrayList()
        kelasAdapter = KelasAdapter(kelasList)

        kelasAdapter.onItemClick = { kelas ->
            val intent = Intent(context, DetailKelasSiswaActivity::class.java).apply {
                putExtra("KELAS_ID", kelas.id)
                putExtra("JUDUL", kelas.judul)
                putExtra("DESKRIPSI", kelas.deskripsi)
            }
            startActivity(intent)
        }

        recyclerView.adapter = kelasAdapter

        swipeRefreshLayout.setOnRefreshListener {
            fetchClasses()
        }

        fetchClasses()

        return view
    }

    private fun fetchClasses() {
        val queue = Volley.newRequestQueue(requireContext())
        val url = "${Config.BASE_URL}ambilKelas.php?kelompok=$kelompok"
        Log.d("FetchClasses", "Requesting classes for kelompok: $kelompok")


        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                parseData(response)
                swipeRefreshLayout.isRefreshing = false
            },
            { error ->
                error.printStackTrace()
                swipeRefreshLayout.isRefreshing = false
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
                    deskripsi = item.getString("deskripsi"),
                    kelompok = item.getString("kelompok")  // Menambahkan kelompok
                )
                kelasList.add(kelas)
            }
            kelasAdapter.notifyDataSetChanged()
        }
    }


}