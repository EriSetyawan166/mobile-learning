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
    private lateinit var kelasAdapter: KelasAdapterGuru
    private lateinit var kelasList: ArrayList<Kelas>
    private lateinit var kelompok: String
    var listener: OnClassAddedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_course_guru, container, false)
        kelompok = arguments?.getString("kelompok") ?: "DefaultGroup"
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        kelasList = ArrayList()
        kelasAdapter = KelasAdapterGuru(kelasList)

        kelasAdapter.onItemClick = { kelas ->
            val intent = Intent(context, DetailKelasGuruActivity::class.java).apply {
                putExtra("KELAS_ID", kelas.id)
                putExtra("JUDUL", kelas.judul)
                putExtra("DESKRIPSI", kelas.deskripsi)
            }
            startActivity(intent)
        }

        kelasAdapter.onDeleteClick = { kelas ->
            showDeleteConfirmationDialog(kelas)
        }

        kelasAdapter.onEditClick = { kelas ->
            showEditClassDialog(kelas)
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
                    deskripsi = item.getString("deskripsi")
                )
                kelasList.add(kelas)
            }
            kelasAdapter.notifyDataSetChanged()
        }
    }

    private fun showDeleteConfirmationDialog(kelas: Kelas) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Konfirmasi Hapus Kelas")
            setMessage("Apakah Anda yakin ingin menghapus kelas ini?")
            setPositiveButton("Ya") { dialog, which ->
                deleteClass(kelas)
            }
            setNegativeButton("Batal") { dialog, which ->
                dialog.dismiss()
            }
        }.create().show()
    }

    private fun deleteClass(kelas: Kelas) {
        val requestQueue = Volley.newRequestQueue(requireContext())
        val url = "${Config.BASE_URL}hapusKelas.php"

        val params = HashMap<String, String>()
        params["id"] = kelas.id

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        kelasAdapter.removeClass(kelas)
                        Toast.makeText(requireContext(), "Kelas berhasil dihapus!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(requireContext(), "Error parsing JSON: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(requireContext(), "Gagal menghapus kelas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }

        requestQueue.add(stringRequest)
    }

    private fun showEditClassDialog(kelas: Kelas) {
        val layoutInflater = LayoutInflater.from(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_add_class, null)

        val judulInput = view.findViewById<TextInputLayout>(R.id.input_judul)
        val subJudulInput = view.findViewById<TextInputLayout>(R.id.input_sub_judul)
        val deskripsiInput = view.findViewById<TextInputLayout>(R.id.input_deskripsi)

        // Mengisi field dengan data kelas yang akan diedit
        judulInput.editText?.setText(kelas.judul)
        subJudulInput.editText?.setText(kelas.sub_judul)
        deskripsiInput.editText?.setText(kelas.deskripsi)

        AlertDialog.Builder(requireContext()).apply {
            setView(view)
            setCancelable(true)
            setPositiveButton("Simpan") { dialog, id ->
                updateClass(kelas.id, judulInput.editText?.text.toString(), subJudulInput.editText?.text.toString(), deskripsiInput.editText?.text.toString())
            }
            setNegativeButton("Batal") { dialog, id ->
                dialog.cancel()
            }
        }.create().show()
    }

    private fun updateClass(id: String, judul: String, subJudul: String, deskripsi: String) {
        Log.d("updateClass", "Updating class with id: $id, judul: $judul, sub_judul: $subJudul, deskripsi: $deskripsi")

        val requestQueue = Volley.newRequestQueue(requireContext())
        val url = "${Config.BASE_URL}/editKelas.php"

        val params = HashMap<String, String>()
        params["id"] = id
        params["judul"] = judul
        params["sub_judul"] = subJudul
        params["deskripsi"] = deskripsi

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        val updatedKelas = Kelas(id, judul, subJudul, deskripsi)
                        kelasAdapter.updateClass(updatedKelas)
                        listener?.onClassUpdated(updatedKelas)
                        Toast.makeText(requireContext(), "Kelas berhasil diupdate!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(requireContext(), "Error parsing JSON: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Log.e("updateClass", "Error response: ${error.message}")
                Toast.makeText(requireContext(), "Gagal mengupdate kelas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }

        requestQueue.add(stringRequest)
    }




    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnClassAddedListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnClassAddedListener")
        }
    }

    interface OnClassAddedListener {
        fun onClassAdded(newClass: Kelas)
        fun onClassUpdated(updatedClass: Kelas)
    }


    fun addClassToAdapter(newClass: Kelas) {
        kelasList.add(newClass)
        kelasAdapter.notifyDataSetChanged() // Or use more specific notifications
    }

    fun updateClassInAdapter(updatedClass: Kelas) {
        kelasAdapter.updateClass(updatedClass)
    }

}