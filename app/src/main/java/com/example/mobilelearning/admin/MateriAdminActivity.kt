package com.example.mobilelearning.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.mobilelearning.Config
import com.example.mobilelearning.Kelas
import com.example.mobilelearning.Materi
import com.example.mobilelearning.MateriAdapter
import com.example.mobilelearning.MateriAdapterGuru
import com.example.mobilelearning.MultipartRequest
import com.example.mobilelearning.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject

class MateriAdminActivity : AppCompatActivity() {

    companion object {
        private const val PDF_REQUEST_CODE = 1000
    }

    private lateinit var toolbar: Toolbar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MateriAdapterGuru
    private lateinit var fabAddMaterial: FloatingActionButton
    private var selectedFileUri: Uri? = null
    private var selectedClassId: String? = null
    private var kelasId: String? = null
    private var addDialogView: View? = null
    private var editDialogView: View? = null





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_materi_guru)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        kelasId = intent.getStringExtra("KELAS_ID")
        val KelasJudul = intent.getStringExtra("JUDUL") ?: return
        findViewById<TextView>(R.id.toolbar_title).text = KelasJudul
        recyclerView = findViewById(R.id.recyclerView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        setupRecyclerView(kelasId ?: return)
        swipeRefreshLayout.setOnRefreshListener {
            kelasId?.let { fetchMateri(it) { materiList ->  // Safe call on kelasId
                adapter.submitList(materiList)
                swipeRefreshLayout.isRefreshing = false
            }}
        }

        fabAddMaterial = findViewById(R.id.fab_add_materi)
        fabAddMaterial.setOnClickListener {
            showAddMaterialDialog()
        }

        adapter.onDeleteClick = { materi ->
            showDeleteConfirmationDialog(materi)
        }

        adapter.onEditClick = { materi ->
            showEditMaterialDialog(materi)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PDF_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.data
            Log.d("FileSelect", "Selected File URI: $selectedFileUri")

            // Update TextView with selected file name for add dialog
            addDialogView?.findViewById<TextView>(R.id.file_name_text_view)?.let { fileNameTextView ->
                val fileName = selectedFileUri?.let { DocumentFile.fromSingleUri(this, it)?.name }
                fileNameTextView.text = fileName ?: getString(R.string.file_belum_dipilih)
            }

            // Update TextView with selected file name for edit dialog
            editDialogView?.findViewById<TextView>(R.id.file_name_text_view)?.let { fileNameTextView ->
                val fileName = selectedFileUri?.let { DocumentFile.fromSingleUri(this, it)?.name }
                fileNameTextView.text = fileName ?: getString(R.string.file_belum_dipilih)
            }
        }
    }

    private fun setupRecyclerView(kelasId: String) {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MateriAdapterGuru()
        recyclerView.adapter = adapter
        fetchMateri(kelasId) { materiList ->
            adapter.submitList(materiList)
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun showAddMaterialDialog() {
        val inflater = LayoutInflater.from(this)
        addDialogView = inflater.inflate(R.layout.dialog_add_materi, null)

        val titleInput = addDialogView!!.findViewById<TextInputLayout>(R.id.input_judul)
        val descriptionInput = addDialogView!!.findViewById<TextInputLayout>(R.id.input_deskripsi)
        val classDropdown = addDialogView!!.findViewById<MaterialAutoCompleteTextView>(R.id.spinner_kelas)
        val selectFileButton = addDialogView!!.findViewById<Button>(R.id.button_upload_file)
        val fileNameTextView = addDialogView!!.findViewById<TextView>(R.id.file_name_text_view)

        // Reset nama file yang dipilih
        fileNameTextView.text = getString(R.string.file_belum_dipilih)

        fetchClasses(classDropdown)

        selectFileButton.setOnClickListener {
            // Intent to pick file
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            startActivityForResult(intent, PDF_REQUEST_CODE)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(addDialogView)
            .setPositiveButton("Upload", null) // Jangan set listener disini
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val uploadButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            uploadButton.setOnClickListener {
                val title = titleInput.editText?.text.toString()
                val description = descriptionInput.editText?.text.toString()
                val classId = selectedClassId

                // Validasi input
                var isValid = true
                if (title.isEmpty()) {
                    titleInput.error = "Judul tidak boleh kosong"
                    isValid = false
                }
                if (description.isEmpty()) {
                    descriptionInput.error = "Deskripsi tidak boleh kosong"
                    isValid = false
                }
                if (classId.isNullOrEmpty()) {
                    classDropdown.error = "Kelas tidak boleh kosong"
                    isValid = false
                }
                if (selectedFileUri == null) {
                    Toast.makeText(this, "File tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    isValid = false
                }

                if (isValid) {
                    uploadMaterial(title, description, classId!!, selectedFileUri!!)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
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
                                0 -> R.drawable.gambar_materi_1
                                1 -> R.drawable.gambar_materi_2
                                2 -> R.drawable.gambar_materi_3
                                else -> R.drawable.gambar_materi_4
                            }
                            materiList.add(Materi(id, judul, deskripsi, filePath, imageResId))
                        }
                        callback(materiList)
                    } else {
                        val errorMessage = response.getString("message")
                        Toast.makeText(this@MateriAdminActivity, errorMessage, Toast.LENGTH_LONG)
                            .show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@MateriAdminActivity,
                        "Error parsing JSON data",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            { error ->
                Toast.makeText(
                    this@MateriAdminActivity,
                    "Failed to fetch data: $error",
                    Toast.LENGTH_LONG
                ).show()
            })

        requestQueue.add(jsonObjectRequest)
    }

    private fun fetchClasses(classDropdown: MaterialAutoCompleteTextView) {
        val url = "${Config.BASE_URL}ambilSemuaKelas.php"
        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val classesArray = jsonResponse.getJSONArray("data")
                    val classList = ArrayList<Kelas>()
                    for (i in 0 until classesArray.length()) {
                        val classObj = classesArray.getJSONObject(i)
                        classList.add(Kelas(classObj.getString("id"), classObj.getString("judul"), classObj.getString("sub_judul"), classObj.getString("deskripsi"), classObj.getString("kelompok")))
                    }
                    updateClassDropdown(classList, classDropdown, kelasId ?: "")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(this, "Failed to fetch classes: $error", Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(stringRequest)
    }



    private fun updateClassDropdown(classItems: ArrayList<Kelas>, classDropdown: MaterialAutoCompleteTextView, currentClassId: String) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, classItems.map { it.judul })
        classDropdown.setAdapter(adapter)
        val currentPosition = classItems.indexOfFirst { it.id == currentClassId }
        if (currentPosition != -1) {
            classDropdown.setText(classItems[currentPosition].judul, false) // Set text without filtering
            selectedClassId = classItems[currentPosition].id
        }

        classDropdown.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            selectedClassId = classItems[position].id
            Log.d("ClassSelect", "Selected class ID: $selectedClassId")
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun getPathFromUri(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val path = it.getString(columnIndex)
                Log.d("FileSelect", "Extracted Path: $path")
                return path
            }
        }
        Log.d("FileSelect", "Fallback URI Path: ${uri.path}")
        return null
    }


    private fun uploadMaterial(title: String, description: String, classId: String, uri: Uri) {
        val url = "${Config.BASE_URL}tambahMateri.php"
        val inputStream = contentResolver.openInputStream(uri)
        val fileName = DocumentFile.fromSingleUri(this, uri)?.name ?: "default_name.pdf"

        val params = hashMapOf(
            "judul" to title,
            "deskripsi" to description,
            "kelas_id" to classId
        )

        val multipartRequest = MultipartRequest(
            Request.Method.POST,
            url,
            { response ->
                val responseString = String(response.data, Charsets.UTF_8)
                Log.d("UploadDebug", "Server Response: $responseString")
                Log.d("UploadDebug", "Uploading with class ID: $classId")
                refreshMateriList()
                Toast.makeText(this, "Upload successful!", Toast.LENGTH_SHORT).show()
            },
            { error ->
                Toast.makeText(this, "Upload failed: ${error.toString()}", Toast.LENGTH_SHORT).show()
            },
            inputStream!!,
            fileName,
            params
        )


        Volley.newRequestQueue(this).add(multipartRequest)
    }

    private fun refreshMateriList() {
        kelasId?.let {
            fetchMateri(it) { materiList ->
                adapter.submitList(materiList)
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun showEditMaterialDialog(materi: Materi) {
        val inflater = LayoutInflater.from(this)
        editDialogView = inflater.inflate(R.layout.dialog_add_materi, null)

        val titleInput = editDialogView!!.findViewById<TextInputLayout>(R.id.input_judul).editText
        val descriptionInput = editDialogView!!.findViewById<TextInputLayout>(R.id.input_deskripsi).editText
        val classDropdown = editDialogView!!.findViewById<MaterialAutoCompleteTextView>(R.id.spinner_kelas)
        val selectFileButton = editDialogView!!.findViewById<Button>(R.id.button_upload_file)
        val fileNameTextView = editDialogView!!.findViewById<TextView>(R.id.file_name_text_view)

        // Set current data
        titleInput?.setText(materi.judul)
        descriptionInput?.setText(materi.deskripsi)
        fetchClasses(classDropdown)

        // Reset nama file yang dipilih
        fileNameTextView.text = getString(R.string.file_belum_dipilih)

        selectFileButton.setOnClickListener {
            // Intent to pick file
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            startActivityForResult(intent, PDF_REQUEST_CODE)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(editDialogView)
            .setPositiveButton("Update", null)  // Prevent default closing behavior
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val updateButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            updateButton.setOnClickListener {
                val title = titleInput?.text.toString()
                val description = descriptionInput?.text.toString()

                // Clear previous errors
                titleInput?.error = null
                descriptionInput?.error = null
                classDropdown.error = null

                // Validate inputs
                var isValid = true

                if (title.isEmpty()) {
                    titleInput?.error = "Judul tidak boleh kosong"
                    isValid = false
                }
                if (description.isEmpty()) {
                    descriptionInput?.error = "Deskripsi tidak boleh kosong"
                    isValid = false
                }
                if (selectedClassId.isNullOrEmpty()) {
                    classDropdown.error = "Kelas tidak boleh kosong"
                    isValid = false
                }

                if (isValid) {
                    updateMaterial(materi.id, title, description, selectedClassId, selectedFileUri)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }


    private fun showDeleteConfirmationDialog(materi: Materi) {
        AlertDialog.Builder(this).apply {
            setTitle("Konfirmasi Hapus Materi")
            setMessage("Apakah Anda yakin ingin menghapus materi ini?")
            setPositiveButton("Ya") { dialog, which ->
                deleteMateri(materi)
            }
            setNegativeButton("Batal") { dialog, which ->
                dialog.dismiss()
            }
        }.create().show()
    }

    private fun updateMaterial(materiId: Int, title: String, description: String, classId: String?, fileUri: Uri?) {
        val url = "${Config.BASE_URL}editMateri.php"
        val requestQueue = Volley.newRequestQueue(this)

        val params = HashMap<String, String>()
        params["id"] = materiId.toString()
        params["judul"] = title
        params["deskripsi"] = description
        params["kelas_id"] = classId ?: ""

        Log.d("UpdateMaterialParams", "Parameters: $params")


        if (fileUri != null) {
            val inputStream = contentResolver.openInputStream(fileUri)
            val fileName = DocumentFile.fromSingleUri(this, fileUri)?.name ?: "default_name.pdf"

            val multipartRequest = MultipartRequest(
                Request.Method.POST,
                url,
                { response ->
                    val responseString = String(response.data, Charsets.UTF_8)
                    Log.d("UploadDebug", "Server Response: $responseString")
                    refreshMateriList()
                    Toast.makeText(this, "Materi berhasil diupdate!", Toast.LENGTH_SHORT).show()
                },
                { error ->
                    Toast.makeText(this, "Gagal mengupdate materi: ${error.toString()}", Toast.LENGTH_SHORT).show()
                },
                inputStream!!,
                fileName,
                params
            )

            requestQueue.add(multipartRequest)
        } else {
            val stringRequest = object : StringRequest(
                Request.Method.POST, url,
                Response.Listener<String> { response ->
                    try {
                        val jsonResponse = JSONObject(response)
                        if (jsonResponse.getString("status") == "success") {
                            refreshMateriList()
                            Toast.makeText(this, "Materi berhasil diupdate!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(this, "Error parsing JSON: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                Response.ErrorListener { error ->
                    Toast.makeText(this, "Gagal mengupdate materi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getParams(): Map<String, String> {
                    return params
                }
            }

            requestQueue.add(stringRequest)
        }
    }


    private fun deleteMateri(materi: Materi) {
        val requestQueue = Volley.newRequestQueue(this)
        val url = "${Config.BASE_URL}hapusMateri.php"

        val params = HashMap<String, String>()
        params["materi_id"] = materi.id.toString()

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        refreshMateriList()
                        Toast.makeText(this, "Materi berhasil dihapus!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(this, "Error parsing JSON: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Gagal menghapus materi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }

        requestQueue.add(stringRequest)
    }





}