package com.example.project180.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Base64
import android.graphics.BitmapFactory
import android.util.Log
import com.example.project180.Model.CategoryDto
import com.example.project180.R

class CategoryAdapterList(
    private var isAdmin: Boolean = false,
    private val onItemClick: (CategoryDto) -> Unit
) : RecyclerView.Adapter<CategoryAdapterList.CategoryViewHolder>() {

    private var originalList: List<CategoryDto> = listOf()
    private var filteredList: List<CategoryDto> = listOf()

    val currentList: List<CategoryDto>
        get() = filteredList

    fun updateAdminStatus(isAdmin: Boolean) {
        this.isAdmin = isAdmin
        notifyDataSetChanged()
    }

    fun submitList(newList: List<CategoryDto>) {
        originalList = newList
        filteredList = newList
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            originalList
        } else {
            originalList.filter {
                it.titre?.contains(query, ignoreCase = true) == true
            }
        }
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.categoryTitle)
        private val image: ImageView = itemView.findViewById(R.id.categoryImage)

        init {
            itemView.setOnClickListener {
                if (!isAdmin) return@setOnClickListener
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(filteredList[position])
                }
            }
        }

        fun bind(category: CategoryDto) {
            title.text = category.titre ?: ""

            try {
                val base64String = category.imageDto?.imageBase64 ?: run {
                    image.setImageResource(R.drawable.ic_image_placeholder)
                    return
                }

                if (base64String.isBlank()) {
                    image.setImageResource(R.drawable.ic_image_placeholder)
                    return
                }

                val cleanBase64 = base64String.substringAfterLast(",")
                val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                if (bitmap != null) {
                    image.setImageBitmap(bitmap)
                } else {
                    image.setImageResource(R.drawable.ic_image_placeholder)
                    Log.e("ImageError", "Failed to decode bitmap")
                }
            } catch (e: Exception) {
                Log.e("ImageError", "Error decoding image", e)
                image.setImageResource(R.drawable.ic_image_placeholder)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    override fun getItemCount(): Int = filteredList.size
}