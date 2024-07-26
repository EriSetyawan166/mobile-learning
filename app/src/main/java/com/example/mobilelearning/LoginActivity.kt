package com.example.mobilelearning

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {
    private lateinit var editTextUsername : EditText
    private lateinit var editTextPassword : EditText
    private lateinit var textInputUsernameLayout : TextInputLayout
    private lateinit var textInputPasswordLayout : TextInputLayout
    private lateinit var buttonLogin : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activty_login)
        editTextUsername = findViewById(R.id.textInputUsername)
        editTextPassword = findViewById(R.id.textInputPassword)
        textInputPasswordLayout = findViewById(R.id.textInputPasswordLayout)
        textInputUsernameLayout = findViewById(R.id.textInputUsernameLayout)
        buttonLogin = findViewById(R.id.buttonLogin)

        buttonLogin.setOnClickListener {
            validateForm()
        }
    }

    private fun login(){
        val url = "http://192.168.100.121/mobile_learning_api/login.php"
        val username = editTextUsername.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        val stringRequest = object : StringRequest(Method.POST, url,
            Response.Listener<String> { response ->
                val jsonResponse = JSONObject(response)
                if (response.contains("success")) {
                    val role = jsonResponse.getString("role")
                    val intent = when (role) {
                        "siswa" -> Intent(this, com.example.mobilelearning.siswa.HomeSiswaActivity::class.java)
                        "guru" -> Intent(this, com.example.mobilelearning.guru.HomeGuruActivity::class.java)
                        "admin" -> Intent(this, com.example.mobilelearning.admin.HomeAdminActivity::class.java)
                        else -> null
                    }
                    intent?.let {
                        startActivity(it)
                        finish()
                    }
                } else {
                    textInputUsernameLayout.error = "Username Salah"
                    textInputPasswordLayout.error = "Password Salah"
                    Toast.makeText(this, "Username atau Password Salah", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { error ->
                error.networkResponse?.let {
                }
                Toast.makeText(this, "Network Error: ${error.message}", Toast.LENGTH_LONG).show()
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["username"] = username
                params["password"] = password
                return params
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun validateForm() {
        var isValid = true
        var errorMessage = ""

        if (editTextUsername.text.toString().trim().isEmpty()) {
            textInputUsernameLayout.error = "Username tidak boleh kosong"
            errorMessage += "Username tidak boleh kosong\n" // Menambahkan pesan ke string kesalahan
            isValid = false
        } else {
            textInputUsernameLayout.error = null
        }

        if (editTextPassword.text.toString().trim().isEmpty()) {
            textInputPasswordLayout.error = "Password tidak boleh kosong"
            errorMessage += "Password tidak boleh kosong\n" // Menambahkan pesan ke string kesalahan
            isValid = false
        } else {
            textInputPasswordLayout.error = null
        }

        if (!isValid) {
            Toast.makeText(this, errorMessage.trim(), Toast.LENGTH_LONG).show()
        } else {
            login()
        }
    }

}