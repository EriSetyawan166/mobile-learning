package com.example.mobilelearning

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class KelasAdapter(private val kelasList: MutableList<Kelas>) : RecyclerView.Adapter<KelasAdapter.KelasViewHolder>() {

    var onItemClick: ((Kelas) -> Unit)? = null
    private val gambarKelas = arrayOf(R.drawable.gambar_1, R.drawable.gambar_2, R.drawable.gambar_3, R.drawable.gambar_4, R.drawable.gambar_5, R.drawable.gambar_6)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KelasViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_kelas, parent, false)
        return KelasViewHolder(itemView, onItemClick)
    }

    override fun onBindViewHolder(holder: KelasViewHolder, position: Int) {
        val currentItem = kelasList[position]
        holder.imageView.setImageResource(gambarKelas[position % gambarKelas.size])
        holder.textViewTitle.text = currentItem.judul
        holder.textViewSubtitle.text = currentItem.sub_judul
        holder.itemView.tag = currentItem
    }

    override fun getItemCount() = kelasList.size

    class KelasViewHolder(itemView: View, private val onItemClick: ((Kelas) -> Unit)?) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_kelas)
        val textViewTitle: TextView = itemView.findViewById(R.id.title_kelas)
        val textViewSubtitle: TextView = itemView.findViewById(R.id.subtitle_kelas)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(itemView.tag as Kelas)
            }
        }
    }

    fun addClass(newClass: Kelas) {
        kelasList.add(newClass)
        notifyItemInserted(kelasList.size - 1)
    }


}