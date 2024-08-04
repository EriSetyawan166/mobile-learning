package com.example.mobilelearning

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class ProfileGuruFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_guru, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = arguments?.getString("user_id")
        val role = arguments?.getString("role")

        fetchProfile(userId, role)
    }

    private fun fetchProfile(userId: String?, role: String?) {
        val url = "${Config.BASE_URL}ambilProfil.php?user_id=$userId&role=$role"
        Log.d("FetchProfile", "Requesting profile for user_id: $userId, role: $role")
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                if (response.getString("status") == "success") {
                    val data = response.getJSONObject("data")
                    view?.findViewById<TextView>(R.id.textViewUsername)?.text = data.getString("username")
                    view?.findViewById<TextView>(R.id.textViewKelompok)?.text = data.getString("kelompok")
                    view?.findViewById<TextView>(R.id.textViewNamaLengkap)?.text = data.getString("nama_lengkap")
                    view?.findViewById<TextView>(R.id.textViewNisNip)?.text = data.getString(if (role == "siswa") "nis" else "nip")
                }
            },
            { error ->
                error.printStackTrace()
            })
        Volley.newRequestQueue(context).add(jsonObjectRequest)
    }
}