package com.example.mobilelearning.guru

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.Volley
import com.example.mobilelearning.CourseFragment
import com.example.mobilelearning.CourseFragmentGuru
import com.example.mobilelearning.LoginActivity
import com.example.mobilelearning.ProfileFragment
import com.example.mobilelearning.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONObject
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilelearning.Config
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.example.mobilelearning.Kelas
import com.example.mobilelearning.KelasAdapter
import com.example.mobilelearning.ProfileGuruFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONException

class HomeGuruActivity : AppCompatActivity(), CourseFragmentGuru.OnClassAddedListener {
    private lateinit var kelompok: String
    private lateinit var role: String
    private lateinit var user_id: String
    private lateinit var toolbar: Toolbar
    lateinit var bottomNav : BottomNavigationView
    lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_guru)
        kelompok = intent.getStringExtra("kelompok") ?: "DefaultGroup"
        role = intent.getStringExtra("role") ?: ""
        user_id = intent.getStringExtra("user_id") ?: ""
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        loadFragment(CourseFragmentGuru().apply {
            arguments = Bundle().apply {
                putString("kelompok", kelompok)
                putString("role", role)
                putString("user_id", user_id)
            }
        }, "Kelas Anda")
        bottomNav = findViewById(R.id.bottomNav) as BottomNavigationView
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.kelas -> {
                    loadFragment(CourseFragmentGuru().apply {
                        arguments = Bundle().apply {
                            putString("kelompok", kelompok)
                            putString("role", role)
                            putString("user_id", user_id)
                        }
                    }, "Kelas Anda")
                    true
                }
                R.id.profile -> {
                    loadFragment(ProfileGuruFragment().apply {
                        arguments = Bundle().apply {
                            putString("kelompok", kelompok)
                            putString("role", role)
                            putString("user_id", user_id)
                        }
                    }, "Profil Anda")
                    true
                }
                R.id.logout -> {
                    showLogoutConfirmation()
                    true
                }
                else -> false
            }
        }

        val fab: FloatingActionButton = findViewById(R.id.fab_add_class)
        fab.setOnClickListener {
            showAddClassDialog()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this).apply {
            setTitle("Konfirmasi Logout")
            setMessage("Apakah anda yakin ingin logout?")
            setPositiveButton("Ya") { dialog, which ->
                // Logout dan buka LoginActivity
                val intent = Intent(this@HomeGuruActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            setNegativeButton("Batal") { dialog, which ->
                // Tutup dialog dan tidak melakukan apa-apa
                dialog.dismiss()
            }
            show()
        }
    }



    private fun showAddClassDialog() {
        val layoutInflater = LayoutInflater.from(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_class, null)

        val judulInput = view.findViewById<TextInputLayout>(R.id.input_judul)
        val subJudulInput = view.findViewById<TextInputLayout>(R.id.input_sub_judul)
        val deskripsiInput = view.findViewById<TextInputLayout>(R.id.input_deskripsi)

        AlertDialog.Builder(this).apply {
            setView(view)
            setCancelable(true)
            setPositiveButton("Tambah", DialogInterface.OnClickListener { dialog, id ->
                submitClass(
                    judulInput.editText?.text.toString(),
                    subJudulInput.editText?.text.toString(),
                    deskripsiInput.editText?.text.toString()
                )
            })
            setNegativeButton("Batal", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })
        }.create().show()
    }

    private fun submitClass(judul: String, subJudul: String, deskripsi: String) {
        val requestQueue = Volley.newRequestQueue(this)
        val url = "${Config.BASE_URL}/tambahKelas.php"

        val params = HashMap<String, String>()
        params["judul"] = judul
        params["sub_judul"] = subJudul
        params["deskripsi"] = deskripsi

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        val kelasId = jsonResponse.getString("id")
                        val newKelas = Kelas(kelasId, judul, subJudul, deskripsi)
                        onClassAdded(newKelas)  // Memanggil interface method
                        Toast.makeText(this, "Kelas berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(this, "Error parsing JSON: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Gagal menambahkan kelas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }

        requestQueue.add(stringRequest)
    }


    private fun loadFragment(fragment: Fragment, title: String){
        setToolbarTitle(title)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()

        val fab: FloatingActionButton = findViewById(R.id.fab_add_class)
        if (fragment is CourseFragmentGuru) {
            fab.show()
        } else {
            fab.hide()
        }
    }

    override fun onClassAdded(newClass: Kelas) {
        val fragment = supportFragmentManager.findFragmentById(R.id.container) as? CourseFragmentGuru
        fragment?.addClassToAdapter(newClass)
    }

    override fun onClassUpdated(updatedClass: Kelas) {
        val fragment = supportFragmentManager.findFragmentById(R.id.container) as? CourseFragmentGuru
        fragment?.updateClassInAdapter(updatedClass)
    }

    private fun setToolbarTitle(title: String) {
        val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
        toolbarTitle.text = title
    }

}