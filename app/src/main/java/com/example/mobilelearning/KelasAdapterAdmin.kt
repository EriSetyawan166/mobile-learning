package com.example.mobilelearning

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class KelasAdapterAdmin(private val kelasList: MutableList<Kelas>, private val context: Context) : RecyclerView.Adapter<KelasAdapterAdmin.KelasViewHolder>() {

    var onItemClick: ((Kelas) -> Unit)? = null
    var onDeleteClick: ((Kelas) -> Unit)? = null
    var onEditClick: ((Kelas) -> Unit)? = null
    private var kelompokMap: Map<String, String> = emptyMap()
    private val gambarKelas = arrayOf(R.drawable.gambar_1, R.drawable.gambar_2, R.drawable.gambar_3, R.drawable.gambar_4, R.drawable.gambar_5, R.drawable.gambar_6)

    init {
        fetchKelompok()
    }

    private fun fetchKelompok() {
        val url = "${Config.BASE_URL}ambilkelompok.php"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                kelompokMap = parseKelompokResponse(response)
                notifyDataSetChanged() // Notify the adapter to refresh the views
            },
            { error ->
                Toast.makeText(context, "Failed to fetch data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
        Volley.newRequestQueue(context).add(jsonObjectRequest)
    }

    private fun parseKelompokResponse(response: JSONObject): Map<String, String> {
        val kelompokMap = HashMap<String, String>()
        if (response.has("data")) {
            val dataArray = response.getJSONArray("data")
            for (i in 0 until dataArray.length()) {
                val kelompokObject = dataArray.getJSONObject(i)
                val id = kelompokObject.getString("id")
                val nama = kelompokObject.getString("nama")
                kelompokMap[id] = nama
            }
        } else {
            Log.e("parseKelompokResponse", "No 'data' key found in the JSON response")
        }
        return kelompokMap
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KelasViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_kelas_admin, parent, false)
        return KelasViewHolder(itemView, onItemClick, onDeleteClick, onEditClick)
    }

    override fun onBindViewHolder(holder: KelasViewHolder, position: Int) {
        val currentItem = kelasList[position]
        holder.imageView.setImageResource(gambarKelas[position % gambarKelas.size])
        holder.textViewTitle.text = currentItem.judul
        holder.textViewSubtitle.text = currentItem.sub_judul
        val kelompokName = kelompokMap[currentItem.kelompok]
        holder.textViewKelompok.text = kelompokName ?: "Unknown"
        holder.itemView.tag = currentItem
    }

    override fun getItemCount() = kelasList.size

    class KelasViewHolder(itemView: View, private val onItemClick: ((Kelas) -> Unit)?, private val onDeleteClick: ((Kelas) -> Unit)?, private val onEditClick: ((Kelas) -> Unit)?) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_kelas)
        val textViewTitle: TextView = itemView.findViewById(R.id.title_kelas)
        val textViewSubtitle: TextView = itemView.findViewById(R.id.subtitle_kelas)
        val textViewKelompok: TextView = itemView.findViewById(R.id.kelompok_kelas)
        val menuMore: ImageView = itemView.findViewById(R.id.menu_more)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(itemView.tag as Kelas)
            }

            menuMore.setOnClickListener {
                showPopupMenu(menuMore, itemView.tag as Kelas)
            }
        }

        private fun showPopupMenu(view: View, kelas: Kelas) {
            val popup = PopupMenu(view.context, view)
            popup.inflate(R.menu.menu_option_kelas)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_delete -> {
                        onDeleteClick?.invoke(kelas)
                        true
                    }
                    R.id.action_edit -> {
                        onEditClick?.invoke(kelas)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    fun addClass(newClass: Kelas) {
        kelasList.add(newClass)
        notifyItemInserted(kelasList.size - 1)
    }

    fun removeClass(kelas: Kelas) {
        val position = kelasList.indexOf(kelas)
        if (position >= 0) {
            kelasList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateClass(updatedClass: Kelas) {
        val position = kelasList.indexOfFirst { it.id == updatedClass.id }
        if (position >= 0) {
            kelasList[position] = updatedClass
            notifyItemChanged(position)
        }
    }




}