package com.example.project180.Adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.project180.Model.OrderDto
import com.example.project180.Model.OrderItemDto
import com.example.project180.R
import com.example.project180.Util.UserPreferences
import com.example.project180.databinding.ItemOrderBinding
import com.example.project180.databinding.ItemOrderProductBinding
import java.text.SimpleDateFormat
import java.util.*

class OrdersAdapter : ListAdapter<OrderDto, OrdersAdapter.OrderViewHolder>(OrderDiffCallback()) {

    companion object {
        private const val STATUT_EN_ATTENTE = "En_attente_de_confirmation"
        private const val STATUT_CONFIRMÉE = "Commande_confirmée"
        private const val STATUT_EXPÉDIÉE = "Commande_expédiée"
        private const val STATUT_LIVRÉE = "Commande_livrée"
        private const val DATE_FORMAT = "dd/MM/yyyy"
    }


    var onStatusUpdateListener: ((Int, String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: OrderDto) {
            binding.apply {
                setupStatusSpinner(order)
                bindOrderDetails(order)
                setupProductsRecyclerView(order.items)
            }
        }

        private fun ItemOrderBinding.setupStatusSpinner(order: OrderDto) {
            val statuses = root.context.resources.getStringArray(R.array.order_statuses)
            val adapter = ArrayAdapter(
                root.context,
                android.R.layout.simple_spinner_item,
                statuses
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            statusSpinner.adapter = adapter

            val currentStatus = when (order.status) {
                STATUT_EN_ATTENTE -> 0
                STATUT_CONFIRMÉE -> 1
                STATUT_EXPÉDIÉE -> 2
                STATUT_LIVRÉE -> 3
                else -> 0
            }
            statusSpinner.setSelection(currentStatus)

            val isSeller = UserPreferences.getUserInfo(root.context)?.role == "Vendeur"
            statusSpinner.isEnabled = isSeller
            confirmStatusBtn.visibility = if (isSeller) View.VISIBLE else View.GONE

            confirmStatusBtn.setOnClickListener {
                val newStatus = when (statusSpinner.selectedItemPosition) {
                    0 -> STATUT_EN_ATTENTE
                    1 -> STATUT_CONFIRMÉE
                    2 -> STATUT_EXPÉDIÉE
                    3 -> STATUT_LIVRÉE
                    else -> order.status
                }
                order.id?.let { it1 -> onStatusUpdateListener?.invoke(it1, newStatus) }
            }
        }

        private fun ItemOrderBinding.bindOrderDetails(order: OrderDto) {
            orderIdText.text = root.context.getString(R.string.order_id_format, order.id)

            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            orderDateText.text = dateFormat.format(order.orderDate ?: Date())

            deliveryAddressText.text = order.deliveryAddress ?: ""
            deliveryCostText.text = root.context.getString(R.string.price_format, order.deliveryCost)
            totalAmountText.text = root.context.getString(R.string.price_format, order.totalAmount)
        }

        private fun ItemOrderBinding.setupProductsRecyclerView(items: List<OrderItemDto>) {
            productsRecyclerView.layoutManager = LinearLayoutManager(root.context)
            val productsAdapter = OrderProductsAdapter()
            productsRecyclerView.adapter = productsAdapter
            productsAdapter.submitList(items)
        }
    }

    inner class OrderProductsAdapter : ListAdapter<OrderItemDto, OrderProductsAdapter.ProductViewHolder>(ProductDiffCallback()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val binding = ItemOrderProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ProductViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class ProductViewHolder(private val binding: ItemOrderProductBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(item: OrderItemDto) {
                binding.apply {
                    val product = item.productDto
                    productTitleText.text = product?.title ?: " "

                    val categoryTitle = product?.categoryDto?.titre
                    txtCategory.text = categoryTitle ?: " "
                    txtCategory.visibility = View.VISIBLE

                    loadProductImage(product?.images?.get(0)?.imageBase64)

                    productQuantityText.text = root.context.getString(
                        R.string.quantity_format,
                        item.quantity
                    )
                }
            }

            private fun ItemOrderProductBinding.loadProductImage(base64String: String?) {
                try {
                    if (!base64String.isNullOrBlank()) {
                        val cleanBase64 = base64String.substringAfterLast(",")
                        val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        productImage.setImageBitmap(bitmap)
                    } else {
                        productImage.setImageResource(R.drawable.ic_image_placeholder)
                    }
                } catch (e: Exception) {
                    Log.e("OrdersAdapter", "Image decoding error", e)
                    productImage.setImageResource(R.drawable.ic_image_placeholder)
                }
            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<OrderItemDto>() {
        override fun areItemsTheSame(oldItem: OrderItemDto, newItem: OrderItemDto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: OrderItemDto, newItem: OrderItemDto): Boolean {
            return oldItem == newItem
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<OrderDto>() {
        override fun areItemsTheSame(oldItem: OrderDto, newItem: OrderDto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: OrderDto, newItem: OrderDto): Boolean {
            return oldItem == newItem
        }
    }
}