package com.example.mobilelearning

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserManageAdapter(private val onEdit: (User) -> Unit, private val onDelete: (User) -> Unit) : RecyclerView.Adapter<UserManageAdapter.UserViewHolder>() {

    private var users: List<User> = listOf()

    class UserViewHolder(view: View, val onEdit: (User) -> Unit, val onDelete: (User) -> Unit) : RecyclerView.ViewHolder(view) {
        private val nameTextView: TextView = view.findViewById(R.id.textViewName)
        private val editButton: Button = view.findViewById(R.id.buttonEdit)
        private val deleteButton: Button = view.findViewById(R.id.buttonDelete)

        fun bind(user: User) {
            nameTextView.text = user.name
            editButton.setOnClickListener { onEdit(user) }
            deleteButton.setOnClickListener { onDelete(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view, onEdit, onDelete)
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

