package com.example.mobilelearning

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class KelasAdapterGuru(private val kelasList: MutableList<Kelas>) : RecyclerView.Adapter<KelasAdapterGuru.KelasViewHolder>() {

    var onItemClick: ((Kelas) -> Unit)? = null
    var onDeleteClick: ((Kelas) -> Unit)? = null
    var onEditClick: ((Kelas) -> Unit)? = null
    private val gambarKelas = arrayOf(R.drawable.gambar_1, R.drawable.gambar_2, R.drawable.gambar_3, R.drawable.gambar_4, R.drawable.gambar_5, R.drawable.gambar_6)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KelasViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_kelas_guru, parent, false)
        return KelasViewHolder(itemView, onItemClick, onDeleteClick, onEditClick)
    }

    override fun onBindViewHolder(holder: KelasViewHolder, position: Int) {
        val currentItem = kelasList[position]
        holder.imageView.setImageResource(gambarKelas[position % gambarKelas.size])
        holder.textViewTitle.text = currentItem.judul
        holder.textViewSubtitle.text = currentItem.sub_judul
        holder.itemView.tag = currentItem
    }

    override fun getItemCount() = kelasList.size

    class KelasViewHolder(itemView: View, private val onItemClick: ((Kelas) -> Unit)?, private val onDeleteClick: ((Kelas) -> Unit)?, private val onEditClick: ((Kelas) -> Unit)?) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_kelas)
        val textViewTitle: TextView = itemView.findViewById(R.id.title_kelas)
        val textViewSubtitle: TextView = itemView.findViewById(R.id.subtitle_kelas)
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