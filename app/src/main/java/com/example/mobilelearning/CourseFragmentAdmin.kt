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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
import com.example.mobilelearning.admin.DetailKelasAdminActivity
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject

class CourseFragmentAdmin : Fragment() {

    interface DataChangeListener {
        fun onDataAdded()
    }
    private var kelompokMap: HashMap<String, String> = hashMapOf()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var kelasAdapter: KelasAdapterAdmin
    private lateinit var kelasList: ArrayList<Kelas>
    private lateinit var kelompok: String
    var listener: OnClassAddedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_course_admin, container, false)
        kelompok = arguments?.getString("kelompok") ?: "DefaultGroup"
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        kelasList = ArrayList()
        kelasAdapter = KelasAdapterAdmin(kelasList, requireContext())

        kelasAdapter.onItemClick = { kelas ->
            val intent = Intent(context, DetailKelasAdminActivity::class.java).apply {
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
        fetchKelompok()

        return view
    }

    private fun fetchClasses() {
        val queue = Volley.newRequestQueue(requireContext())
        val url = "${Config.BASE_URL}ambilSemuaKelas.php" // Menggunakan endpoint baru untuk mengambil semua kelas

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
        val kelompokSpinner = view.findViewById<TextInputLayout>(R.id.input_kelompok).editText as AutoCompleteTextView


        // Set existing values
        judulInput.editText?.setText(kelas.judul)
        subJudulInput.editText?.setText(kelas.sub_judul)
        deskripsiInput.editText?.setText(kelas.deskripsi)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, kelompokMap.keys.toList())
        kelompokSpinner.setAdapter(adapter)
        // Set the spinner text to the kelompok name based on the saved kelompok ID
        kelompokSpinner.setText(kelompokMap.filter { it.value == kelas.kelompok }.keys.firstOrNull(), false)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val judul = judulInput.editText?.text.toString()
                val subJudul = subJudulInput.editText?.text.toString()
                val deskripsi = deskripsiInput.editText?.text.toString()
                val kelompok = kelompokSpinner.text.toString()
                val kelompokId = kelompokMap[kelompok] ?: ""

                judulInput.error = null
                subJudulInput.error = null
                deskripsiInput.error = null
                kelompokSpinner.error = null
                var isValid = true

                if (judul.isEmpty()) {
                    judulInput.error = "Judul tidak boleh kosong"
                    isValid = false
                }

                if (subJudul.isEmpty()) {
                    subJudulInput.error = "Sub judul tidak boleh kosong"
                    isValid = false
                }
                if (deskripsi.isEmpty()) {
                    deskripsiInput.error = "Deskripsi tidak boleh kosong"
                    isValid = false
                }

                if (kelompok.isEmpty()) {
                    deskripsiInput.error = "Kelompok tidak boleh kosong"
                    isValid = false
                }

                if (isValid) {
                    // Log input values
                    Log.d("EditClassDialog", "Judul: $judul, Sub Judul: $subJudul, Deskripsi: $deskripsi")
                    // Update class if validation passes
                    updateClass(kelas.id, judul, subJudul, deskripsi, kelompokId)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }


    fun fetchKelompok() {
        val url = "${Config.BASE_URL}ambilkelompok.php"
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                kelompokMap.clear()
                kelompokMap.putAll(parseKelompokResponse(response))
                // Trigger any UI update or callback if necessary
            },
            { error ->
                Toast.makeText(context, "Failed to fetch data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
        Volley.newRequestQueue(requireContext()).add(jsonObjectRequest)
    }


    private fun parseKelompokResponse(response: JSONObject): HashMap<String, String> {
        val kelompokMap = HashMap<String, String>()
        if (response.has("data")) {
            val dataArray = response.getJSONArray("data")
            for (i in 0 until dataArray.length()) {
                val kelompokObject = dataArray.getJSONObject(i)
                val id = kelompokObject.getString("id")
                val nama = kelompokObject.getString("nama")
                kelompokMap[nama] = id
            }
        } else {
            Log.e("parseKelompokResponse", "No 'data' key found in the JSON response")
        }
        return kelompokMap
    }




    private fun updateClass(id: String, judul: String, subJudul: String, deskripsi: String, kelompok:String) {
        Log.d("updateClass", "Updating class with id: $id, judul: $judul, sub_judul: $subJudul, deskripsi: $deskripsi, kelompok: $kelompok")

        val requestQueue = Volley.newRequestQueue(requireContext())
        val url = "${Config.BASE_URL}/editKelas.php"

        val params = HashMap<String, String>()
        params["id"] = id
        params["judul"] = judul
        params["sub_judul"] = subJudul
        params["deskripsi"] = deskripsi
        params["kelompok"] = kelompok

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                Log.d("updateClass", "Received response: $response")
                try {
                    val jsonResponse = JSONObject(response)
                    Log.d("updateClass", "Parsed JSON response: ${jsonResponse.toString(2)}")
                    if (jsonResponse.getString("status") == "success") {
                        val updatedKelas = Kelas(id, judul, subJudul, deskripsi, kelompok)
                        kelasAdapter.updateClass(updatedKelas)
                        listener?.onClassUpdated(updatedKelas)
                        Toast.makeText(requireContext(), "Kelas berhasil diupdate!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Log.e("updateClass", "Error parsing JSON: ${e.message}")
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