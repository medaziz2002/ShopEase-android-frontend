/*
package com.example.project180.Adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project180.R
import com.example.project180.databinding.ViewholderPicListBinding
class PicListAdapter(val items: MutableList<String>, var picMain: ImageView) :
    RecyclerView.Adapter<PicListAdapter.Viewholder>() {

    private var selectedPosition = -1
    private var lastSelectedPosition = -1
    private lateinit var context: Context

    inner class Viewholder(val binding: ViewholderPicListBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PicListAdapter.Viewholder {
        context = parent.context
        val binding = ViewholderPicListBinding.inflate(LayoutInflater.from(context), parent, false)
        return Viewholder(binding)
    }


    override fun onBindViewHolder(holder: PicListAdapter.Viewholder, position: Int) {

        Glide.with(holder.itemView.context)
            .load(items[position])
            .into(holder.binding.picList)

        holder.binding.root.setOnClickListener {
            lastSelectedPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(lastSelectedPosition)
            notifyItemChanged(selectedPosition)

            Glide.with(holder.itemView.context)
                .load(items[position])
                .into(picMain)
        }

        if (selectedPosition == position) {
            holder.binding.picLayout.setBackgroundResource(R.drawable.grey_bg_selected)
        } else {
            holder.binding.picLayout.setBackgroundResource(R.drawable.grey_bg)
        }

    }

    override fun getItemCount(): Int = items.size
}
 */
package com.example.project180.Adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.project180.R
import com.example.project180.databinding.ViewholderPicListBinding

class PicListAdapter(
    private val items: List<String>,
    private val mainImageView: ImageView,
    private val defaultImageRes: Int = R.drawable.ic_image_placeholder
) : RecyclerView.Adapter<PicListAdapter.ViewHolder>() {

    private var selectedPosition = 0
    private lateinit var context: Context

    inner class ViewHolder(val binding: ViewholderPicListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ViewholderPicListBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = items.getOrNull(position) ?: ""

        val requestOptions = RequestOptions()
            .placeholder(defaultImageRes)
            .error(defaultImageRes)
            .diskCacheStrategy(DiskCacheStrategy.ALL)

        // Chargement de l'image avec gestion spécifique pour base64
        if (imageUrl.startsWith("data:image")) {
            // Si c'est une image base64 avec préfixe
            Glide.with(context)
                .load(imageUrl)
                .apply(requestOptions)
                .into(holder.binding.picList)
        } else if (imageUrl.startsWith("/9j/") || imageUrl.startsWith("iVBORw0")) {
            // Si c'est une image base64 sans préfixe
            Glide.with(context)
                .load("data:image/jpeg;base64,$imageUrl")
                .apply(requestOptions)
                .into(holder.binding.picList)
        } else {
            // URL normale
            Glide.with(context)
                .load(imageUrl)
                .apply(requestOptions)
                .into(holder.binding.picList)
        }

        // Gestion de la sélection
        holder.binding.picLayout.setBackgroundResource(
            if (position == selectedPosition) R.drawable.grey_bg_selected
            else R.drawable.grey_bg
        )

        holder.itemView.setOnClickListener {
            val previousSelected = selectedPosition
            selectedPosition = position

            notifyItemChanged(previousSelected)
            notifyItemChanged(selectedPosition)

            // Même logique de chargement pour l'image principale
            if (imageUrl.startsWith("data:image")) {
                Glide.with(context)
                    .load(imageUrl)
                    .into(mainImageView)
            } else if (imageUrl.startsWith("/9j/") || imageUrl.startsWith("iVBORw0")) {
                Glide.with(context)
                    .load("data:image/jpeg;base64,$imageUrl")
                    .into(mainImageView)
            } else {
                Glide.with(context)
                    .load(imageUrl)
                    .into(mainImageView)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<String>) {
        (items as? MutableList)?.apply {
            clear()
            addAll(newItems)
        }
        selectedPosition = 0
        notifyDataSetChanged()

        if (newItems.isNotEmpty()) {
            val firstImage = newItems[0]
            if (firstImage.startsWith("data:image")) {
                Glide.with(context).load(firstImage).into(mainImageView)
            } else if (firstImage.startsWith("/9j/") || firstImage.startsWith("iVBORw0")) {
                Glide.with(context).load("data:image/jpeg;base64,$firstImage").into(mainImageView)
            } else {
                Glide.with(context).load(firstImage).into(mainImageView)
            }
        }
    }
}