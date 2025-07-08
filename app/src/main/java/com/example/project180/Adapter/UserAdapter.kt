package com.example.project180.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project180.Model.UserRequest
import com.example.project180.R
import com.example.project180.databinding.ItemUserCardBinding

class UserAdapter : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var userList = listOf<UserRequest>()
    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onMessageClick(user: UserRequest)
        fun onCallClick(user: UserRequest)
    }

    fun setUsers(users: List<UserRequest>) {
        this.userList = users
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    class UserViewHolder(val binding: ItemUserCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        // Set user data
        holder.binding.userFullName.text = "${user.nom} ${user.prenom}"
        holder.binding.userEmail.text = user.email
        holder.binding.userRole.text = user.role

        // Set click listeners
        holder.binding.msgToSellerBtn.setOnClickListener {
            listener?.onMessageClick(user)
        }

        holder.binding.calToSellerBtn.setOnClickListener {
            listener?.onCallClick(user)
        }

        // Load user image
        val imageView = holder.binding.img
        val context = imageView.context
        val base64 = user.imageDto?.imageBase64

        if (!base64.isNullOrEmpty()) {
            val imageBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(
                imageBytes,
                0,
                imageBytes.size
            )
            imageView.setImageBitmap(bitmap)
        } else {
            // Load default avatar
            Glide.with(context)
                .load(R.drawable.user_image)
                .into(imageView)
        }
    }

    override fun getItemCount(): Int = userList.size
}