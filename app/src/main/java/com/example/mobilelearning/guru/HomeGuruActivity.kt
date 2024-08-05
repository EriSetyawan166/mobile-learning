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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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

class HomeGuruActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var kelasAdapter: KelasAdapter
    private lateinit var kelasList: ArrayList<Kelas>
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var toolbar: Toolbar
    private lateinit var kelompok: String
    private lateinit var role: String
    private lateinit var user_id: String
    lateinit var bottomNav : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_guru)
        kelompok = intent.getStringExtra("kelompok") ?: ""
        role = intent.getStringExtra("role") ?: ""
        user_id = intent.getStringExtra("user_id") ?: ""
        Log.d("HomeSiswaDebug", "Received role: $role")
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
                    loadFragment(ProfileFragment().apply {
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

    private fun loadFragment(fragment: Fragment, title: String){
        setToolbarTitle(title)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }


    private fun setToolbarTitle(title: String) {
        val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
        toolbarTitle.text = title
    }





}

