package com.example.mobilelearning

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject

class UserManageFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserManageAdapter
    private lateinit var buttonGroup: MaterialButtonToggleGroup
    private var allUsers: List<User> = listOf()
    private var allGroups: List<User> = listOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_manage, container, false)
        setupRecyclerView(view)
        setupButtonGroup(view)

        fetchUsers()
        fetchKelompok()
        return view
    }

    private fun setupButtonGroup(view: View) {
        buttonGroup = view.findViewById(R.id.toggleButtonGroup)
        buttonGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.buttonSiswa -> displayUsers("siswa")
                    R.id.buttonGuru -> displayUsers("guru")
                    R.id.buttonKelompok -> displayGroups()
                }
            }
        }
        // Manually check the 'siswa' button to set it as selected by default
        buttonGroup.check(R.id.buttonSiswa)
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewUsers)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = UserManageAdapter(
            onDelete = { user -> deleteUser(user) },
            onEdit = { user -> showEditDialog(user) },
            isGroupView = false // Default to user view
        )
        recyclerView.adapter = adapter
    }

    fun fetchUsers() {
        val url = "${Config.BASE_URL}ambilUser.php"
        fetchUserData(url, true)
    }

    fun fetchKelompok() {
        val url = "${Config.BASE_URL}ambilkelompok.php"
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                handleKelompokResponse(response)
            },
            { error ->
                Toast.makeText(context, "Failed to fetch data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
        Volley.newRequestQueue(requireContext()).add(jsonObjectRequest)
    }

    private fun handleKelompokResponse(response: JSONObject) {
        val data = response.getJSONArray("data")
        val tempList = ArrayList<User>()
        for (i in 0 until data.length()) {
            val item = data.getJSONObject(i)
            tempList.add(User(
                id = item.getString("id"),
                username = "",  // Karena ini kelompok, kita bisa menggunakan string kosong atau nilai default lainnya
                name = item.getString("nama"), // Karena ini kelompok, kita bisa menggunakan string kosong atau nilai default lainnya
                role = "kelompok",
                nis = null,
                nip = null,
                kelompok = item.getString("nama"), // Menyimpan nama kelompok
                kelompok_id = item.getString("id") // Menyimpan ID kelompok
            ))
        }
        allGroups = tempList
        // Log untuk debug
        Log.d("handleKelompokResponse", "Kelompok fetched: ${allGroups.map { it.kelompok }}")
        displayGroups() // Pastikan displayGroups() dipanggil untuk memperbarui tampilan
    }





    private fun fetchUserData(url: String, isUser: Boolean) {
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                Log.d("fetchUserData", "Response: $response")
                handleResponse(response, isUser)
            },
            { error ->
                Log.e("fetchUserData", "Failed to fetch data: ${error.message}")
                error.networkResponse?.let {
                    val responseBody = String(it.data)
                    Log.e("fetchUserData", "Error response body: $responseBody")
                }
                Toast.makeText(context, "Failed to fetch data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
        Volley.newRequestQueue(requireContext()).add(jsonObjectRequest)
    }

    private fun deleteUser(user: User) {
        val url = "${Config.BASE_URL}${if (user.role == "kelompok") "hapusKelompok.php" else "hapusUser.php"}"
        val requestQueue = Volley.newRequestQueue(context)
        val requestBody = JSONObject().apply {
            put("user_id", user.id)
        }
        val deleteRequest = JsonObjectRequest(Request.Method.POST, url, requestBody,
            { response ->
                Toast.makeText(context, if (user.role == "kelompok") "Group deleted successfully" else "User deleted successfully", Toast.LENGTH_SHORT).show()
                if (user.role == "kelompok") fetchKelompok() else fetchUsers()
            },
            { error ->
                Toast.makeText(context, "Failed to delete ${if (user.role == "kelompok") "group" else "user"}: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
        requestQueue.add(deleteRequest)
    }


    private fun showEditDialog(user: User) {
        if (user.role == "kelompok") {
            Log.d("EditDialog", "Opening Group Edit Dialog for group: ${user.name}")
            showEditGroupDialog(user)
        } else {
            Log.d("EditDialog", "Opening User Edit Dialog for user: ${user.username}")
            showEditUserDialog(user)
        }
    }


    private fun showEditGroupDialog(group: User) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_group, null)
        val groupNameInput = dialogView.findViewById<TextInputLayout>(R.id.input_group_name)

        groupNameInput.editText?.setText(group.kelompok)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Group")
            .setView(dialogView)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val groupName = groupNameInput.editText?.text.toString()

                // Validate input
                var isValid = true
                if (groupName.isEmpty()) {
                    groupNameInput.error = "Nama kelompok tidak boleh kosong"
                    isValid = false
                }

                if (isValid) {
                    val updatedGroup = group.copy(kelompok = groupName)
                    updateGroup(updatedGroup)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }



    private fun updateGroup(group: User) {
        val url = "${Config.BASE_URL}editKelompok.php"
        val requestQueue = Volley.newRequestQueue(context)

        val requestBody = JSONObject().apply {
            put("user_id", group.id)
            put("nama", group.kelompok)
        }

        val updateRequest = JsonObjectRequest(Request.Method.POST, url, requestBody,
            { response ->
                Toast.makeText(context, "Group updated successfully", Toast.LENGTH_SHORT).show()
                fetchKelompok() // Panggil fetchKelompok() setelah pembaruan berhasil
            },
            { error ->
                error.networkResponse?.let {
                    val responseBody = String(it.data)
                    Toast.makeText(context, "Failed to update group: $responseBody", Toast.LENGTH_LONG).show()
                } ?: run {
                    Toast.makeText(context, "Failed to update group: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        )

        requestQueue.add(updateRequest)
    }

    private fun showEditUserDialog(user: User) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_user, null)
        val usernameInput = dialogView.findViewById<TextInputLayout>(R.id.input_username)
        val passwordInputLayout = dialogView.findViewById<TextInputLayout>(R.id.input_password)
        val confirmPasswordInputLayout = dialogView.findViewById<TextInputLayout>(R.id.input_confirm_password)
        val fullNameInput = dialogView.findViewById<TextInputLayout>(R.id.input_full_name)
        val nipNisInput = dialogView.findViewById<TextInputLayout>(R.id.input_nip_nis)
        val roleInput = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.dropdown_role)
        val groupInput = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.dropdown_group)

        // Hide password and confirm password fields
        passwordInputLayout.visibility = View.GONE
        confirmPasswordInputLayout.visibility = View.GONE

        // Populate fields with user data
        usernameInput.editText?.setText(user.username)
        fullNameInput.editText?.setText(user.name)

        // Menggunakan salah satu metode untuk mengisi nipNisInput secara dinamis
        val nipNis = when {
            user.role == "guru" -> user.nip
            user.role == "siswa" -> user.nis
            else -> ""
        }
        nipNisInput.editText?.setText(nipNis)

        roleInput.setText(user.role, false)

        // Log untuk debug
        Log.d("EditDialog", "User ID: ${user.id}, Username: ${user.username}, NIP: ${user.nip}, NIS: ${user.nis}, Role: ${user.role}, Kelompok ID: ${user.kelompok_id}")

        // Populate role dropdown
        val roles = arrayOf("guru", "siswa")
        val roleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, roles)
        roleInput.setAdapter(roleAdapter)

        // Populate group dropdown
        val groupNames = allGroups.map { it.kelompok }
        val groupAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, groupNames)
        groupInput.setAdapter(groupAdapter)

        // Create a map of group names to IDs
        val groupNameToIdMap = allGroups.associate { it.kelompok to it.kelompok_id }

        // Set the correct group after fetching
        val kelompokId = user.kelompok_id
        groupInput.post {
            val groupName = allGroups.find { it.kelompok_id == kelompokId }?.kelompok ?: ""
            groupInput.setText(groupName, false)
            Log.d("EditDialog", "Setting group to: $groupName")
        }

        // Create and show the dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit User")
            .setView(dialogView)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val selectedGroupName = groupInput.text.toString()
                val selectedGroupId = groupNameToIdMap[selectedGroupName]

                // Validate inputs
                var isValid = true
                if (usernameInput.editText?.text.isNullOrEmpty()) {
                    usernameInput.error = "Username tidak boleh kosong"
                    isValid = false
                }
                if (fullNameInput.editText?.text.isNullOrEmpty()) {
                    fullNameInput.error = "Nama lengkap tidak boleh kosong"
                    isValid = false
                }
                if (nipNisInput.editText?.text.isNullOrEmpty()) {
                    nipNisInput.error = if (roleInput.text.toString() == "guru") "NIP tidak boleh kosong" else "NIS tidak boleh kosong"
                    isValid = false
                }

                if (isValid) {
                    val updatedUser = user.copy(
                        username = usernameInput.editText?.text.toString(),
                        name = fullNameInput.editText?.text.toString(),
                        role = roleInput.text.toString(),
                        nis = if (roleInput.text.toString() == "siswa") nipNisInput.editText?.text.toString() else null,
                        nip = if (roleInput.text.toString() == "guru") nipNisInput.editText?.text.toString() else null,
                        kelompok_id = selectedGroupId
                    )
                    Log.d("EditDialog", "Updated User: $updatedUser")
                    updateUser(updatedUser)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }


    private fun updateUser(user: User) {
        val url = "${Config.BASE_URL}editUser.php"
        val requestQueue = Volley.newRequestQueue(context)

        val requestBody = JSONObject().apply {
            put("user_id", user.id)
            put("username", user.username)
            put("nama", user.name)
            put("nip_nis", user.nip ?: user.nis)
            put("kelompok_id", user.kelompok_id) // Mengirimkan ID kelompok
        }

        // Log untuk memastikan requestBody
        Log.d("updateUser", "Request Body: $requestBody")

        val updateRequest = JsonObjectRequest(Request.Method.POST, url, requestBody,
            { response ->
                Log.d("updateUser", "Server response: $response")
                Toast.makeText(context, "User updated successfully", Toast.LENGTH_SHORT).show()
                fetchUsers()
            },
            { error ->
                error.networkResponse?.let {
                    val responseBody = String(it.data)
                    Log.e("updateUser", "Failed to update user. Status code: ${it.statusCode}, Error: $responseBody")
                    Toast.makeText(context, "Failed to update user: $responseBody", Toast.LENGTH_LONG).show()
                } ?: run {
                    Log.e("updateUser", "Failed to update user: ${error.message}")
                    Toast.makeText(context, "Failed to update user: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        )

        requestQueue.add(updateRequest)
    }








    private fun handleResponse(response: JSONObject, isUser: Boolean) {
        val data = response.getJSONArray("data")
        val tempList = ArrayList<User>()
        for (i in 0 until data.length()) {
            val item = data.getJSONObject(i)
            Log.d("handleResponse", "Item: $item")
            tempList.add(User(
                id = item.getString("id"),
                username = item.getString("username"),
                name = item.getString("nama"),
                role = item.optString("role", "kelompok"),
                nis = item.optString("nis", null),
                nip = item.optString("nip", null),
                kelompok_id = item.optString("kelompok_id", null),
                kelompok = item.optString("kelompok", null)
            ))
        }
        if (isUser) {
            allUsers = tempList
            Log.d("handleResponse", "Loaded Users: ${allUsers.map { it.name }}")
        } else {
            allGroups = tempList
            Log.d("handleResponse", "Loaded Groups: ${allGroups.map { it.name }}")
        }
        // Default display for first load or refresh
        if (buttonGroup.checkedButtonId == R.id.buttonSiswa) {
            displayUsers("siswa")
        } else if (buttonGroup.checkedButtonId == R.id.buttonGuru) {
            displayUsers("guru")
        } else {
            displayGroups()
        }
    }







    private fun displayUsers(role: String) {
        Log.d("displayUsers", "Displaying users with role: $role")
        adapter.updateData(allUsers.filter { it.role == role }, isGroupView = false)
    }

    private fun displayGroups() {
        Log.d("displayGroups", "Displaying groups")
        adapter.updateData(allGroups, isGroupView = true)
    }
}


