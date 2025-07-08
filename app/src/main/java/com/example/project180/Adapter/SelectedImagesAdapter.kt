package com.example.project180.Adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project180.databinding.ItemSelectedImageBinding
import java.io.ByteArrayInputStream

class SelectedImagesAdapter(
    private val images: MutableList<Uri>,
    private val onDeleteClick: (Int) -> Unit,
    private val onReplaceClick: (Int) -> Unit
) : RecyclerView.Adapter<SelectedImagesAdapter.ViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    inner class ViewHolder(val binding: ItemSelectedImageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSelectedImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uri = images[position]

        if (uri.toString().startsWith("data:image")) {
            try {
                val base64String = uri.toString().substringAfter("base64,")
                val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                holder.binding.imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.binding.imageView.setImageResource(com.example.project180.R.drawable.ic_image_placeholder)
            }
        } else {
            holder.binding.imageView.setImageURI(uri)
        }

        if (position == selectedPosition) {
            holder.binding.deleteButton.visibility = View.VISIBLE
        } else {
            holder.binding.deleteButton.visibility = View.GONE
        }

        holder.binding.imageView.setOnClickListener {
            val previousSelectedPosition = selectedPosition
            selectedPosition = holder.adapterPosition

            if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousSelectedPosition)
            }
            notifyItemChanged(selectedPosition)
        }

        holder.binding.deleteButton.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                onDeleteClick(currentPosition)
                if (selectedPosition == currentPosition) {
                    selectedPosition = RecyclerView.NO_POSITION
                }
            }
        }

    }

    override fun getItemCount(): Int = images.size

    fun addImage(uri: Uri) {
        images.add(uri)
        notifyItemInserted(images.size - 1)
    }

    fun removeImage(position: Int) {
        if (position in images.indices) {
            images.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}