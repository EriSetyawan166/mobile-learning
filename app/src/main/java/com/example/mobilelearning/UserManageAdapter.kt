package com.example.mobilelearning

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class UserManageAdapter(
    private val onDelete: (User) -> Unit,
    private val onEdit: (User) -> Unit,
    private var isGroupView: Boolean // Menambahkan parameter untuk menentukan apakah tampilan ini adalah untuk kelompok
) : RecyclerView.Adapter<UserManageAdapter.UserViewHolder>() {

    private var users: List<User> = listOf()

    class UserViewHolder(
        view: View,
        private val onDelete: (User) -> Unit,
        private val onEdit: (User) -> Unit,
        private val isGroupView: Boolean,
        private val getUser: (Int) -> User // Menambahkan lambda untuk mendapatkan user dari adapter
    ) : RecyclerView.ViewHolder(view) {
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
                    val user = adapterPosition.takeIf { it != RecyclerView.NO_POSITION }?.let { getUser(it) }
                    when (menuItem.itemId) {
                        R.id.option_delete -> {
                            Log.d("OptionsPopup", "Delete option selected for ${if (isGroupView) "group" else "user"}: ${user?.name}")
                            user?.let { showDeleteConfirmation(it) }
                            true
                        }
                        R.id.option_edit -> {
                            Log.d("OptionsPopup", "Edit option selected for ${if (isGroupView) "group" else "user"}: ${user?.name}")
                            user?.let { onEdit(it) }
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
                .setMessage("Are you sure you want to delete this ${if (isGroupView) "group" else "user"}?")
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
        return UserViewHolder(view, onDelete, onEdit, isGroupView) { position -> users[position] }
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        Log.d("onBindViewHolder", "Binding ${if (isGroupView) "group" else "user"} at position $position: ${users[position].name}")
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<User>, isGroupView: Boolean) {
        users = newUsers
        this.isGroupView = isGroupView
        Log.d("UserManageAdapter", "Updating data. isGroupView: $isGroupView, Data: ${users.map { it.name }}")
        notifyDataSetChanged()
    }
}




