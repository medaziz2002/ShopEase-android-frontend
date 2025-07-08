package com.example.project180.Adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project180.Model.CategoryDto
import com.example.project180.databinding.ViewholderCategoryBinding

class CategoryAdapter(var items: MutableList<CategoryDto>) :
    RecyclerView.Adapter<CategoryAdapter.Viewholder>() {

    var onItemClick: ((CategoryDto) -> Unit)? = null
    var selectedPosition = -1
    private lateinit var context: Context

    inner class Viewholder(val binding: ViewholderCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val previousSelected = selectedPosition
                selectedPosition = adapterPosition
                if (previousSelected != -1) notifyItemChanged(previousSelected)
                notifyItemChanged(selectedPosition)
                onItemClick?.invoke(items[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        context = parent.context
        val binding = ViewholderCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = items[position]
        holder.binding.titleCat.text = item.titre

        if (position == selectedPosition) {
            holder.binding.root.setBackgroundResource(com.example.project180.R.drawable.category_selected_bg)
        } else {
            holder.binding.root.setBackgroundResource(com.example.project180.R.drawable.category_default_bg)
        }

        try {
            val base64String = item.imageDto?.imageBase64
            if (!base64String.isNullOrBlank()) {
                val cleanBase64 = base64String.substringAfterLast(",")
                val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                holder.binding.picCat.setImageBitmap(bitmap)
            } else {
                holder.binding.picCat.setImageResource(com.example.project180.R.drawable.ic_image_placeholder)
            }
        } catch (e: Exception) {
            Log.e("ImageError", "Erreur décodage image", e)
            holder.binding.picCat.setImageResource(com.example.project180.R.drawable.ic_image_placeholder)
        }
    }

    override fun getItemCount(): Int = items.size

    fun setSelectedCategoryById(categoryId: Int?) {
        if (categoryId != null) {
            val position = items.indexOfFirst { it.id == categoryId }
            if (position != -1) {
                val oldPosition = selectedPosition
                selectedPosition = position
                if (oldPosition != -1) notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
            }
        }
    }
}