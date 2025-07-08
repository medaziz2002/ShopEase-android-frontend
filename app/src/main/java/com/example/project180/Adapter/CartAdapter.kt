package com.example.project180.Adapter

import SizeListAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project180.Activity.ManipulateCartActivity
import com.example.project180.Helper.ChangeNumberItemsListener
import com.example.project180.Model.CartDto
import com.example.project180.R
import com.example.project180.databinding.ViewholderCartBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class CartAdapter(
    private val items: List<Pair<CartDto, List<String>>>,
    private val context: Context,
    private val changeNumberItemsListener: ChangeNumberItemsListener,
    private val onQuantityChanged: (cartId: Int, newQuantity: Int) -> Unit,
    private val onItemRemoved: (cartId: Int, position: Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (item, options) = items[position]
        val product = item.productDto

        with(holder.binding) {
            TitleTxt.text = product?.title ?: "Produit inconnu"

            val price = product?.price ?: 0.0
            val discountedPrice = product?.discountPercentage ?: 0.0
            numberItemTxt.text = item.quantity.toString()
            stockTxt.text = "Stock: ${product?.stock ?: "--"}"
            feeEachItem.text = "${price}€"

            if (discountedPrice > 0 && discountedPrice < price) {
                feeEachItem.paintFlags = feeEachItem.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                totalEachItem.text = "${price * (1 - discountedPrice / 100) * item.quantity}€"
            } else {
                totalEachItem.text = "${price * item.quantity}€"
            }

            // Afficher les options (tailles/poids) si disponibles
            if (options.isNotEmpty()) {
                textViewSizesTitle.visibility = View.VISIBLE
                sizeList.visibility = View.VISIBLE

                val adapter = SizeListAdapter(options,allowMultipleSelection = true)
                adapter.setSelectedItems(options)
                sizeList.adapter = adapter
                sizeList.layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

                // Définir le titre selon la catégorie
                when (product?.categoryDto?.titre?.lowercase()) {
                    in listOf("vêtements", "vetement", "habillement") -> {
                        textViewSizesTitle.text = "Taille sélectionnée :"
                    }
                    in listOf("nourriture", "repas") -> {
                        textViewSizesTitle.text = "Poids sélectionné :"
                    }
                    else -> {
                        textViewSizesTitle.visibility = View.GONE
                        sizeList.visibility = View.GONE
                    }
                }
            } else {
                textViewSizesTitle.visibility = View.GONE
                sizeList.visibility = View.GONE
            }

            try {
                val base64String = product?.images?.get(0)?.imageBase64
                if (!base64String.isNullOrBlank()) {
                    val cleanBase64 = base64String.substringAfterLast(",")
                    val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    picCart.setImageBitmap(bitmap)
                } else {
                    picCart.setImageResource(R.drawable.ic_image_placeholder)
                }
            } catch (e: Exception) {
                Log.e("CartAdapter", "Erreur de décodage image", e)
                picCart.setImageResource(R.drawable.ic_image_placeholder)
            }

            /*
            plusCartBtn.setOnClickListener {
                val newQuantity = item.quantity + 1
                onQuantityChanged(item.id ?: 0, newQuantity)
                changeNumberItemsListener.onChanged()
            }
             */


            plusCartBtn.setOnClickListener {
                val newQuantity = item.quantity + 1
                onQuantityChanged(item.id ?: 0, newQuantity)
                changeNumberItemsListener.onChanged()
                when (product?.categoryDto?.titre?.lowercase()) {
                    in listOf("vêtements", "vetement", "habillement") -> {
                        val intent = Intent(context, ManipulateCartActivity::class.java).apply {
                            putExtra("category_titre", item.productDto?.categoryDto?.titre)
                            putExtra("cart_id", item.id) // Assuming item.id holds the cartId
                        }
                        context.startActivity(intent)
                    }
                    in listOf("nourriture", "repas") -> {
                        val intent = Intent(context, ManipulateCartActivity::class.java).apply {
                            putExtra("category_titre", item.productDto?.categoryDto?.titre)
                            putExtra("cart_id", item.id) // Assuming item.id holds the cartId
                        }
                        context.startActivity(intent)
                    }
                }


            }



            minusCartBtn.setOnClickListener {
                if (item.quantity > 1) {
                    val newQuantity = item.quantity - 1
                    onQuantityChanged(item.id ?: 0, newQuantity)
                } else {
                    val currentPosition = holder.adapterPosition
                    onItemRemoved(item.id ?: 0, currentPosition)
                }
                changeNumberItemsListener.onChanged()
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Pair<CartDto, List<String>>>) {
        (items as? MutableList)?.let {
            it.clear()
            it.addAll(newItems)
            notifyDataSetChanged()
        }
    }





}