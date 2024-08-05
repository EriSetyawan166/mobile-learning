package com.example.mobilelearning.admin

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.mobilelearning.Config
import com.example.mobilelearning.CourseFragmentAdmin
import com.example.mobilelearning.CourseFragmentGuru
import com.example.mobilelearning.Group
import com.example.mobilelearning.Kelas
import com.example.mobilelearning.LoginActivity
import com.example.mobilelearning.ProfileGuruFragment
import com.example.mobilelearning.R
import com.example.mobilelearning.UserManageFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject
class HomeAdminActivity : AppCompatActivity(), CourseFragmentAdmin.OnClassAddedListener {
    private lateinit var kelompok: String
    private lateinit var role: String
    private lateinit var user_id: String
    private lateinit var toolbar: Toolbar
    lateinit var bottomNav : BottomNavigationView
    lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_admin)
        kelompok = intent.getStringExtra("kelompok") ?: "DefaultGroup"
        role = intent.getStringExtra("role") ?: ""
        user_id = intent.getStringExtra("user_id") ?: ""
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        loadFragment(CourseFragmentAdmin().apply {
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
                    loadFragment(CourseFragmentAdmin().apply {
                        arguments = Bundle().apply {
                            putString("kelompok", kelompok)
                            putString("role", role)
                            putString("user_id", user_id)
                        }
                    }, "Kelas Anda")
                    true
                }
                R.id.user_manage -> {
                    loadFragment(UserManageFragment(), "Manajemen Pengguna")
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
                val intent = Intent(this@HomeAdminActivity, LoginActivity::class.java)
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
        if (fragment is CourseFragmentAdmin || fragment is UserManageFragment) {
            fab.show()
            fab.setOnClickListener {
                if (fragment is CourseFragmentAdmin) {
                    showAddClassDialog()
                } else if (fragment is UserManageFragment) {
                    showAddUserOrGroupDialog()
                }
            }
        } else {
            fab.hide()
        }
    }

    private fun showAddUserOrGroupDialog() {
        // This dialog asks whether to add a student, teacher, or group
        val options = arrayOf("Add Student", "Add Teacher", "Add Group")
        AlertDialog.Builder(this)
            .setTitle("Select Type")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> showAddUserDialog()  // Siswa means student
                    1 -> showAddUserDialog()   // Guru means teacher
                    2 -> showAddGroupDialog()        // Handle group addition
                }
            }.show()
    }

    private fun showAddUserDialog() {
        val layoutInflater = LayoutInflater.from(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_user, null)

        val usernameInput = view.findViewById<TextInputLayout>(R.id.input_username)
        val passwordInput = view.findViewById<TextInputLayout>(R.id.input_password)
        val passwordConfirmInput = view.findViewById<TextInputLayout>(R.id.input_confirm_password)
        val roleDropdown = view.findViewById<MaterialAutoCompleteTextView>(R.id.dropdown_role)
        val nameInput = view.findViewById<TextInputLayout>(R.id.input_full_name)
        val nipNisInput = view.findViewById<TextInputLayout>(R.id.input_nip_nis)
        val groupDropdown = view.findViewById<MaterialAutoCompleteTextView>(R.id.dropdown_group)

        var selectedGroupId: String? = null

        // Populate roles
        val roles = arrayOf("guru", "siswa") // Teacher and Student
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        roleDropdown.setAdapter(roleAdapter)
        roleDropdown.setOnItemClickListener { _, _, position, _ ->
            nipNisInput.hint = if (position == 0) "NIP" else "NIS"  // Change hint based on role
        }

        // Fetch and populate groups
        fetchGroups { groups ->
            val groupAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, groups.map { it.name })
            groupDropdown.setAdapter(groupAdapter)
            groupDropdown.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                selectedGroupId = groups[position].id
                Log.d("GroupSelect", "Selected group ID: $selectedGroupId")
            }
        }

        AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("Add") { dialog, _ ->
                val username = usernameInput.editText?.text.toString()
                val password = passwordInput.editText?.text.toString()
                val confirmPassword = passwordConfirmInput.editText?.text.toString()
                val role = roleDropdown.text.toString()
                val fullName = nameInput.editText?.text.toString()
                val nipNis = nipNisInput.editText?.text.toString()
                val group = selectedGroupId ?: ""

                // Log input values
                Log.d("AddUserDialog", "Username: $username, Password: $password, Role: $role, Full Name: $fullName, NIP/NIS: $nipNis, Group: $group")

                if (password == confirmPassword) {
                    addUser(username, password, role, fullName, nipNis, group)
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun fetchGroups(callback: (List<Group>) -> Unit) {
        val url = "${Config.BASE_URL}ambilKelompok.php"
        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    if (response.getString("status") == "success") {
                        val groupJsonArray = response.getJSONArray("data")
                        val groups = ArrayList<Group>()
                        for (i in 0 until groupJsonArray.length()) {
                            val groupObject = groupJsonArray.getJSONObject(i)
                            val id = groupObject.getString("id")
                            val name = groupObject.getString("nama")
                            groups.add(Group(id, name))
                        }
                        Log.d("fetchGroups", "Groups fetched: $groups")
                        callback(groups)
                    } else {
                        Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e("fetchGroups", "JSON parsing error", e)
                }
            },
            { error ->
                Toast.makeText(this, "Failed to fetch groups: ${error.toString()}", Toast.LENGTH_LONG).show()
                Log.e("fetchGroups", "Failed to fetch data: ${error.message}", error)
            }
        )

        requestQueue.add(jsonObjectRequest)
    }




    private fun addUser(username: String, password: String, role: String, fullName: String, nipNis: String, group: String) {
        val url = "${Config.BASE_URL}/tambahUser.php"
        val requestQueue = Volley.newRequestQueue(this)

        val params = HashMap<String, String>()
        params["username"] = username
        params["password"] = password
        params["role"] = role
        params["nama_lengkap"] = fullName
        params["nip_nis"] = nipNis
        params["kelompok"] = group

        Log.d("addUser", "Params: $params")

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                try {
                    Log.d("addUser", "Response: $response")
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show()
                        updateRecyclerView()
                    } else {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Log.e("addUser", "JSON parsing error: ${e.message}", e)
                    Toast.makeText(this, "Error parsing JSON: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Log.e("addUser", "Volley error: ${error.message}", error)
                Toast.makeText(this, "Failed to add user: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }

        requestQueue.add(stringRequest)
    }

    fun updateRecyclerView() {
        val fragment = supportFragmentManager.findFragmentById(R.id.container) as? UserManageFragment
        fragment?.fetchUsers()
    }

    private fun showAddGroupDialog() {
        val layoutInflater = LayoutInflater.from(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_group, null) // You need to create this layout

        val groupNameInput = view.findViewById<TextInputLayout>(R.id.input_group_name)

        AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("Add", DialogInterface.OnClickListener { dialog, id ->
                // Implement group addition logic here
            })
            .setNegativeButton("Cancel", null)
            .show()
    }



    override fun onClassAdded(newClass: Kelas) {
        val fragment = supportFragmentManager.findFragmentById(R.id.container) as? CourseFragmentAdmin
        fragment?.addClassToAdapter(newClass)
    }

    override fun onClassUpdated(updatedClass: Kelas) {
        val fragment = supportFragmentManager.findFragmentById(R.id.container) as? CourseFragmentAdmin
        fragment?.updateClassInAdapter(updatedClass)
    }

    private fun setToolbarTitle(title: String) {
        val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
        toolbarTitle.text = title
    }

}