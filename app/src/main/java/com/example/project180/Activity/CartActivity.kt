package com.example.project180.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project180.Adapter.CartAdapter

import com.example.project180.Helper.ChangeNumberItemsListener
import com.example.project180.Model.CartDto
import com.example.project180.Model.OrderItemDto
import com.example.project180.R
import com.example.project180.Util.UserPreferences
import com.example.project180.ViewModel.CartViewModel
import com.example.project180.ViewModel.OrderViewModel
import com.example.project180.ViewModel.ProductViewModel
import com.example.project180.databinding.ActivityCartBinding
import com.google.android.material.snackbar.Snackbar
import com.paypal.checkout.approve.OnApprove
import com.paypal.checkout.cancel.OnCancel
import com.paypal.checkout.createorder.CreateOrder
import com.paypal.checkout.createorder.CurrencyCode
import com.paypal.checkout.createorder.OrderIntent
import com.paypal.checkout.createorder.UserAction
import com.paypal.checkout.error.OnError
import com.paypal.checkout.order.Amount
import com.paypal.checkout.order.AppContext
import com.paypal.checkout.order.OrderRequest
import com.paypal.checkout.order.PurchaseUnit
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class CartActivity : BaseActivity() {

    private lateinit var binding: ActivityCartBinding
    private val cartViewModel: CartViewModel by viewModels()
    private val orderViewModel: OrderViewModel by viewModels()
    private val productViewModel: ProductViewModel by viewModels()
    private var tax: Double = 0.0
    private var totalAmount: Double = 0.0
    private var isDelivery = false
    private val deliveryCost = 15.0
    private val TAG = "CartActivityPayPal"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setVariable()
        setupObservers()
        loadCartItems()
        productViewModel.fetchSizes()
        productViewModel.fetchWeights()
    }

    private fun setVariable() {
        binding.backBtn.setOnClickListener { finish() }

        binding.proceedToPaymentBtn.setOnClickListener {
            validateAndProceedToPayment()
        }

        binding.deliveryOptions.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.deliveryHome -> {
                    isDelivery = true
                    binding.deliveryAddressContainer.visibility = View.VISIBLE
                    calculateCart(cartViewModel.subTotal.value ?: 0.0)
                }
                R.id.deliveryPickup -> {
                    isDelivery = false
                    binding.deliveryAddressContainer.visibility = View.GONE
                    calculateCart(cartViewModel.subTotal.value ?: 0.0)
                }
            }
        }
    }

    private fun validateAndProceedToPayment() {
        isDelivery = binding.deliveryHome.isChecked

        if (isDelivery && binding.deliveryAddress.text.isNullOrEmpty()) {
            showMessage("Veuillez entrer une adresse de livraison")
            return
        }

        val cartItems = cartViewModel.cartItems.value ?: run {
            showMessage("Votre panier est vide")
            return
        }

        binding.proceedToPaymentBtn.isEnabled = false

        lifecycleScope.launch {
            for (cartItem in cartItems) {
                val isOutOfStock = productViewModel.checkProductStockStatus(cartItem.productId)
                if (isOutOfStock) {
                    val productName = cartItem.productDto?.title ?: "Produit inconnu"
                    showMessage("$productName - Rupture de stock")
                    binding.proceedToPaymentBtn.isEnabled = true
                    return@launch
                }
            }

            binding.proceedToPaymentBtn.visibility = View.GONE
            binding.checkOutBtn.visibility = View.VISIBLE
            setupPayPalButton()
        }
    }

    private fun setupPayPalButton() {
        binding.paymentButtonContainer.setup(
            createOrder = CreateOrder { createOrderActions ->
                val order = OrderRequest(
                    intent = OrderIntent.CAPTURE,
                    appContext = AppContext(userAction = UserAction.PAY_NOW,
                        returnUrl = "com.example.project180://paypalpay"),
                    purchaseUnitList = listOf(
                        PurchaseUnit(
                            amount = Amount(
                                currencyCode = CurrencyCode.EUR,
                                value = "%.2f".format(totalAmount)
                            )
                        )
                    )
                )
                createOrderActions.create(order)
            },
            onApprove = OnApprove { approval ->
                approval.orderActions.capture { captureOrderResult ->
                    Log.d(TAG, "CaptureOrderResult: $captureOrderResult")
                    showMessage("Paiement réussi")
                    UserPreferences.getUserInfo(this)?.id?.let { userId ->
                        createOrderAfterPayment(userId)
                        cartViewModel.clearCart(userId)
                    }
                }
            },
            onCancel = OnCancel {
                Log.d(TAG, "Buyer cancelled the payment")
                showMessage("Paiement annulé")
            },
            onError = OnError { errorInfo ->
                Log.d(TAG, "Error: $errorInfo")
                showMessage("Erreur de paiement: ${errorInfo.error?.message}")
            }
        )
    }

    private fun createOrderAfterPayment(userId: Int) {
        val cartItems = cartViewModel.cartItems.value ?: return
        val address = if (isDelivery) binding.deliveryAddress.text.toString() else null

        val orderItems = cartItems.map { cartItem ->
            val discount = cartItem.productDto?.discountPercentage ?: 0.0
            val basePrice = cartItem.productDto?.price ?: 0.0
            val finalPrice = if (discount > 0) basePrice * (1 - discount / 100) else basePrice

            when (cartItem.productDto?.categoryDto?.titre?.lowercase()) {
                in listOf("vêtements", "vetement", "habillement") -> {
                    OrderItemDto(
                        productId = cartItem.productId,
                        quantity = cartItem.quantity,
                        unitPrice = finalPrice,
                        discount = discount,
                        size = cartItem.size,
                        weight = null
                    )
                }
                in listOf("nourriture", "repas") -> {
                    OrderItemDto(
                        productId = cartItem.productId,
                        quantity = cartItem.quantity,
                        unitPrice = finalPrice,
                        discount = discount,
                        size = null,
                        weight = cartItem.weight
                    )
                }
                else -> {
                    OrderItemDto(
                        productId = cartItem.productId,
                        quantity = cartItem.quantity,
                        unitPrice = finalPrice,
                        discount = discount,
                        size = null,
                        weight = null
                    )
                }
            }
        }

        orderViewModel.orderCreationResult.observe(this) { result ->
            result.onSuccess { order ->
                showMessage("Commande #${order.id} créée avec succès")
                cartViewModel.clearCart(userId)
            }.onFailure {
                showMessage("Erreur lors de la création de la commande")
            }
        }

        orderViewModel.createOrder(
            userId = userId,
            items = orderItems,
            totalAmount = totalAmount,
            isDelivery = isDelivery,
            deliveryAddress = address,
            deliveryCost = if (isDelivery) deliveryCost else 0.0
        )
        binding.deliveryAddress.setText("")
        binding.deliveryPickup.isChecked = true
    }

    private fun loadCartItems() {
        UserPreferences.getUserInfo(this)?.id?.let { userId ->
            cartViewModel.fetchCartItems(userId)
        } ?: run {
            binding.emptyTxt.visibility = View.VISIBLE
            binding.cartView.visibility = View.GONE
        }
    }

    private fun setupObservers() {
        cartViewModel.cartItems.observe(this) { items ->
            if (items.isNullOrEmpty()) {
                binding.emptyTxt.visibility = View.VISIBLE
                binding.cartView.visibility = View.GONE
                binding.paymentButtonContainer.visibility = View.GONE
                binding.checkOutBtn.visibility = View.GONE
                binding.proceedToPaymentBtn.visibility = View.GONE
            } else {
                binding.emptyTxt.visibility = View.GONE
                binding.cartView.visibility = View.VISIBLE
                binding.proceedToPaymentBtn.visibility = View.VISIBLE
                initCartList(items)
            }
        }

        cartViewModel.subTotal.observe(this) { subTotal ->
            calculateCart(subTotal)
        }

        productViewModel.sizes.observe(this) { sizeDto ->
            cartViewModel.cartItems.value?.let { cartItems ->
                initCartList(cartItems)
            }
        }

        productViewModel.weights.observe(this) { weightDto ->
            cartViewModel.cartItems.value?.let { cartItems ->
                initCartList(cartItems)
            }
        }
    }

    private fun calculateCart(subTotal: Double) {
        val percentTax = 0.02
        val tax = BigDecimal(subTotal * percentTax).setScale(2, RoundingMode.HALF_UP).toDouble()
        val itemTotal = BigDecimal(subTotal).setScale(2, RoundingMode.HALF_UP).toDouble()
        val effectiveDeliveryCost = if (isDelivery && subTotal < 100.0) deliveryCost else 0.0
        totalAmount = BigDecimal(subTotal + tax + effectiveDeliveryCost)
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()

        with(binding) {
            totalFeeTxt.text = "%.2f€".format(itemTotal)
            taxTxt.text = "%.2f€".format(tax)
            deliveryTxt.text = if (isDelivery) {
                if (subTotal >= 150.0) "0.00€ (Gratuite)" else "%.2f€".format(deliveryCost)
            } else {
                "0.00€"
            }
            totalTxt.text = "%.2f€".format(totalAmount)
        }
    }

    private fun initCartList(items: List<CartDto>) {
        binding.cartView.layoutManager = LinearLayoutManager(this)

        val itemsWithSpecificOptions = items.map { cartItem ->
            val specificOptions = when {

                !cartItem.size.isNullOrEmpty() -> {
                    cartItem.size // Return the size list (List<String>)
                }
                // Vérification des poids
                !cartItem.weight.isNullOrEmpty() -> {
                    cartItem.weight // Return the weight list (List<String>)
                }
                else -> emptyList<String>() // No size or weight, return an empty list
            }

            // Return the cartItem with the list of sizes or weights
            cartItem to specificOptions
        }


        binding.cartView.adapter = CartAdapter(
            itemsWithSpecificOptions,
            this,
            object : ChangeNumberItemsListener {
                override fun onChanged() {
                    calculateCart(cartViewModel.subTotal.value ?: 0.0)
                }
            },
            onQuantityChanged = { cartId, newQuantity ->
                cartViewModel.updateQuantity(cartId, newQuantity)
            },
            onItemRemoved = { cartId, position ->
                showUndoSnackbar(cartId, position)
                cartViewModel.removeFromCart(cartId)
            }
        )
    }




    private fun showUndoSnackbar(cartId: Int, position: Int) {
        Snackbar.make(binding.root, "Produit supprimé", Snackbar.LENGTH_LONG)
            .setAction("Annuler") {
                UserPreferences.getUserInfo(this)?.id?.let { userId ->
                    cartViewModel.fetchCartItems(userId)
                }
            }
            .show()
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}