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
            onEdit = { user -> showEditDialog(user) }
        )
        recyclerView.adapter = adapter
    }

    fun fetchUsers() {
        val url = "${Config.BASE_URL}ambilUser.php"
        fetchUserData(url, true)
    }

    private fun fetchKelompok() {
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
                name = "", // Karena ini kelompok, kita bisa menggunakan string kosong atau nilai default lainnya
                role = "kelompok",
                nis = null,
                nip = null,
                kelompok = item.getString("nama"), // Menyimpan nama kelompok
                kelompok_id = item.getString("id") // Menyimpan ID kelompok
            ))
        }
        allGroups = tempList
        // Log untuk debug
        Log.d("handleKelompokResponse", "Kelompok fetched: ${allGroups.map { it.name }}")
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
        val url = "${Config.BASE_URL}hapusUser.php"
        val requestQueue = Volley.newRequestQueue(context)

        // Creating a JSONObject directly with the user ID
        val requestBody = JSONObject().apply {
            put("user_id", user.id)
        }

        // Log the request body to ensure the parameters are correct
        Log.d("deleteUser", "Request body: $requestBody")

        // Creating the delete request with detailed error handling
        val deleteRequest = JsonObjectRequest(Request.Method.POST, url, requestBody,
            { response ->
                // Log server response
                Log.d("deleteUser", "Server response: $response")
                Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show()
                fetchUsers()  // Refresh user list
            },
            { error ->
                // Log detailed error message
                error.networkResponse?.let {
                    val responseBody = String(it.data)
                    Log.e("deleteUser", "Failed to delete user. Status code: ${it.statusCode}, Error: $responseBody")
                    Toast.makeText(context, "Failed to delete user: $responseBody", Toast.LENGTH_LONG).show()
                } ?: run {
                    Log.e("deleteUser", "Failed to delete user: ${error.message}")
                    Toast.makeText(context, "Failed to delete user: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        )

        requestQueue.add(deleteRequest)
    }


    private fun showEditDialog(user: User) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_user, null)
        val usernameInput = dialogView.findViewById<TextInputLayout>(R.id.input_username).editText as TextInputEditText
        val passwordInputLayout = dialogView.findViewById<TextInputLayout>(R.id.input_password)
        val confirmPasswordInputLayout = dialogView.findViewById<TextInputLayout>(R.id.input_confirm_password)
        val fullNameInput = dialogView.findViewById<TextInputLayout>(R.id.input_full_name).editText as TextInputEditText
        val nipNisInput = dialogView.findViewById<TextInputLayout>(R.id.input_nip_nis).editText as TextInputEditText
        val roleInput = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.dropdown_role)
        val groupInput = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.dropdown_group)

        // Hide password and confirm password fields
        passwordInputLayout.visibility = View.GONE
        confirmPasswordInputLayout.visibility = View.GONE

        // Populate fields with user data
        usernameInput.setText(user.username)
        fullNameInput.setText(user.name)

        // Menggunakan salah satu metode untuk mengisi nipNisInput secara dinamis
        val nipNis = when {
            user.role == "guru" -> user.nip
            user.role == "siswa" -> user.nis
            else -> ""
        }
        nipNisInput.setText(nipNis)

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
            Log.d("EditDialog", "Setting group to: ${groupName}")
        }

        // Create and show the dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Edit User")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val selectedGroupName = groupInput.text.toString()
                val selectedGroupId = groupNameToIdMap[selectedGroupName]
                Log.d("EditDialog", "Selected Group: $selectedGroupName, Group ID: $selectedGroupId")
                val updatedUser = user.copy(
                    username = usernameInput.text.toString(),
                    name = fullNameInput.text.toString(),
                    role = roleInput.text.toString(),
                    nis = if (roleInput.text.toString() == "siswa") nipNisInput.text.toString() else null,
                    nip = if (roleInput.text.toString() == "guru") nipNisInput.text.toString() else null,
                    kelompok_id = selectedGroupId
                )
                Log.d("EditDialog", "Updated User: $updatedUser")
                updateUser(updatedUser)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
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
                kelompok_id = item.optString("kelompok_id", null), // Memastikan ID kelompok diambil
                kelompok = item.optString("kelompok", null) // Nama kelompok jika ada
            ))
        }
        if (isUser) {
            allUsers = tempList
        } else {
            allGroups = tempList
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
        adapter.updateData(allUsers.filter { it.role == role })
    }

    private fun displayGroups() {
        adapter.updateData(allGroups)
    }
}


