package com.example.mobilelearning

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButtonToggleGroup
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
        recyclerView = view.findViewById(R.id.recyclerViewUsers)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = UserManageAdapter(
            onEdit = { user -> /* Handle edit action */ },
            onDelete = { user -> /* Handle delete action */ }
        )
        recyclerView.adapter = adapter

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
            onEdit = { user -> /* Handle edit action */ },
            onDelete = { user -> /* Handle delete action */ }
        )
        recyclerView.adapter = adapter
    }

    fun fetchUsers() {
        val url = "${Config.BASE_URL}ambilUser.php"
        fetchUserData(url, true)
    }

    private fun fetchKelompok() {
        val url = "${Config.BASE_URL}ambilkelompok.php"
        fetchUserData(url, false)
    }

    private fun fetchUserData(url: String, isUser: Boolean) {
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                handleResponse(response, isUser)
            },
            { error ->
                Toast.makeText(context, "Failed to fetch data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
        Volley.newRequestQueue(requireContext()).add(jsonObjectRequest)
    }


    private fun handleResponse(response: JSONObject, isUser: Boolean) {
        val data = response.getJSONArray("data")
        val tempList = ArrayList<User>()
        for (i in 0 until data.length()) {
            val item = data.getJSONObject(i)
            tempList.add(User(
                id = item.getString("id"),
                name = item.getString("nama"),
                role = item.optString("role", "kelompok"),
                nis = item.optString("nis", null),
                nip = item.optString("nip", null),
                kelompok = item.optString("kelompok", null)
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


