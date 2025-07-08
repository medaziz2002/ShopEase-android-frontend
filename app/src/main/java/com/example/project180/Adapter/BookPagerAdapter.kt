package com.example.project180.Adapter

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project180.Model.ProductDto
import com.example.project180.R

class BookPagerAdapter(
    private val context: Context,
    private var products: List<ProductDto>,
    private val onItemClick: (ProductDto) -> Unit
) : RecyclerView.Adapter<BookPagerAdapter.BookPageViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null

    inner class BookPageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val picMain: ImageView = itemView.findViewById(R.id.picMain)
        val picList: RecyclerView = itemView.findViewById(R.id.picList)
        val productCategory: TextView = itemView.findViewById(R.id.txtCategory)
        val productTitle: TextView = itemView.findViewById(R.id.txtTitle)
        val productDescription: TextView = itemView.findViewById(R.id.txtDescription)
        val originalPrice: TextView = itemView.findViewById(R.id.txtOriginalPrice)
        val discountedPrice: TextView = itemView.findViewById(R.id.txtDiscountedPrice)
        val productRating: TextView = itemView.findViewById(R.id.txtRating)
        val productStock: TextView = itemView.findViewById(R.id.txtStock)



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookPageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_produit, parent, false)
        return BookPageViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookPageViewHolder, position: Int) {
        val product = products[position]

        // Charger les images
        product.images?.let { images ->
            if (images.isNotEmpty()) {
                // Charger l'image principale
                loadImage(images[0].imageBase64, holder.picMain)

                // Configurer l'adaptateur pour les miniatures
                val adapter = PicListAdapter(
                    items = images.mapNotNull { it.imageBase64 },
                    mainImageView = holder.picMain
                )
                holder.picList.apply {
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    this.adapter = adapter
                }
            }
        }

        // Remplir les autres champs
        holder.productCategory.text = product.categoryDto?.titre ?: "Catégorie inconnue"
        holder.productTitle.text = product.title ?: "Nom inconnu"
        holder.productDescription.text = product.description ?: "Description non disponible"

        // Gestion des prix
        val price = product.price ?: 0.0
        val discount = product.discountPercentage ?: 0.0

        holder.originalPrice.text = "%.2f€".format(price)
        if (discount > 0) {
            holder.discountedPrice.text = "%.2f€".format(price * (1 - discount/100))
            holder.originalPrice.paintFlags = holder.originalPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            holder.discountedPrice.visibility = View.VISIBLE
        } else {
            holder.discountedPrice.visibility = View.GONE
        }

        holder.productRating.text = product.rating?.toString() ?: "0.0"
        holder.productStock.text = "Stock: ${product.stock ?: 0}"

        holder.productTitle.setOnClickListener {
            onItemClick(product)
        }
    }

    private fun loadImage(imageBase64: String?, imageView: ImageView) {
        imageBase64?.let { image ->
            if (image.startsWith("/9j/") || image.startsWith("iVBORw0")) {
                Glide.with(context)
                    .load("data:image/jpeg;base64,$image")
                    .into(imageView)
            } else {
                Glide.with(context)
                    .load(image)
                    .into(imageView)
            }
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<ProductDto>) {
        products = newProducts
        notifyDataSetChanged()
    }

    fun playPageTurnSound() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, R.raw.page_turn_sound).apply {
            start()
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDetachedFromRecyclerView(recyclerView)
    }
}