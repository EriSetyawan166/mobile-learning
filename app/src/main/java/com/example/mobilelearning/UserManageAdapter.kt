package com.example.mobilelearning

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class UserManageAdapter(private val onDelete: (User) -> Unit, private val onEdit: (User) -> Unit) : RecyclerView.Adapter<UserManageAdapter.UserViewHolder>() {
    private var users: List<User> = listOf()

    class UserViewHolder(view: View, val onDelete: (User) -> Unit, val onEdit: (User) -> Unit, val getUser: (Int) -> User) : RecyclerView.ViewHolder(view) {
        private val nameTextView: TextView = view.findViewById(R.id.textViewName)
        private val optionsButton: ImageView = view.findViewById(R.id.button_option)

        init {
            optionsButton.setOnClickListener {
                showOptionsPopup()
            }
        }

        private fun showOptionsPopup() {
            PopupMenu(itemView.context, optionsButton).apply {
                menuInflater.inflate(R.menu.menu_option_user, this.menu)
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.option_delete -> {
                            val user = getUser(adapterPosition)
                            if (adapterPosition != RecyclerView.NO_POSITION) {
                                showDeleteConfirmation(user)
                            }
                            true
                        }
                        R.id.option_edit -> {
                            val user = getUser(adapterPosition)
                            if (adapterPosition != RecyclerView.NO_POSITION) {
                                onEdit(user)
                            }
                            true
                        }
                        else -> false
                    }
                }
                show()
            }
        }

        private fun showDeleteConfirmation(user: User) {
            AlertDialog.Builder(itemView.context)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Delete") { dialog, _ ->
                    onDelete(user)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        fun bind(user: User) {
            nameTextView.text = user.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view, onDelete, onEdit, { position -> users[position] })
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}



