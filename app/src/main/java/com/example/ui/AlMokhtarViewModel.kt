package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MenuRepository
import com.example.model.AppLanguage
import com.example.model.CartItem
import com.example.model.MenuItem
import com.example.model.OrderStatus
import com.example.model.RestaurantOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppNavMode(val titleAr: String, val titleEn: String) {
    CUSTOMER("طلب طاولة (العميل)", "Customer QR Order"),
    KDS_ADMIN("شاشة المطبخ KDS", "Kitchen Display (KDS)"),
    MENU_MGMT("إدارة الأصناف والعروض", "Menu, Prices & Offers"),
    QR_GENERATOR("رموز الطاولات QR", "Table QR Generator"),
    PRINTER_HUB("طابعات الفواتير", "Thermal Printers"),
    REPORTS("التقارير والمبيعات", "Reports & CSV")
}

class AlMokhtarViewModel(
    val repository: MenuRepository = MenuRepository()
) : ViewModel() {

    // View separation: Pure Customer View vs Admin Portal
    private val _isAdminPortalActive = MutableStateFlow(false)
    val isAdminPortalActive: StateFlow<Boolean> = _isAdminPortalActive.asStateFlow()

    fun enterAdminPortal() {
        _isAdminPortalActive.value = true
        _navMode.value = AppNavMode.KDS_ADMIN
    }

    fun exitAdminPortal() {
        _isAdminPortalActive.value = false
        _navMode.value = AppNavMode.CUSTOMER
    }

    // Language state
    private val _language = MutableStateFlow(AppLanguage.ARABIC)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.ARABIC) {
            AppLanguage.ENGLISH
        } else {
            AppLanguage.ARABIC
        }
    }

    // Navigation mode inside Admin Panel
    private val _navMode = MutableStateFlow(AppNavMode.CUSTOMER)
    val navMode: StateFlow<AppNavMode> = _navMode.asStateFlow()

    fun setNavMode(mode: AppNavMode) {
        _navMode.value = mode
    }

    // Customer Ordering State
    private val _selectedBranchId = MutableStateFlow("mansourah")
    val selectedBranchId: StateFlow<String> = _selectedBranchId.asStateFlow()

    private val _selectedTableNumber = MutableStateFlow(4)
    val selectedTableNumber: StateFlow<Int> = _selectedTableNumber.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow("broast")
    val selectedCategoryId: StateFlow<String> = _selectedCategoryId.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    val cartTotalSar: StateFlow<Double> = _cart.combine(_cart) { items, _ ->
        items.sumOf { it.lineTotalSar }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartItemCount: StateFlow<Int> = _cart.combine(_cart) { items, _ ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val customerName = MutableStateFlow("")
    val customerPhone = MutableStateFlow("")
    val customerNotes = MutableStateFlow("")
    val allergyNotes = MutableStateFlow("")
    val allergyDisclaimerAgreed = MutableStateFlow(true)

    private val _productInDialog = MutableStateFlow<MenuItem?>(null)
    val productInDialog: StateFlow<MenuItem?> = _productInDialog.asStateFlow()

    private val _isSubmittingOrder = MutableStateFlow(false)
    val isSubmittingOrder: StateFlow<Boolean> = _isSubmittingOrder.asStateFlow()

    private val _orderSubmitError = MutableStateFlow<String?>(null)
    val orderSubmitError: StateFlow<String?> = _orderSubmitError.asStateFlow()

    private val _activeTrackedOrder = MutableStateFlow<RestaurantOrder?>(null)
    val activeTrackedOrder: StateFlow<RestaurantOrder?> = _activeTrackedOrder.asStateFlow()

    // Admin Filters
    private val _adminBranchFilter = MutableStateFlow("all")
    val adminBranchFilter: StateFlow<String> = _adminBranchFilter.asStateFlow()

    private val _adminStatusFilter = MutableStateFlow<OrderStatus?>(null)
    val adminStatusFilter: StateFlow<OrderStatus?> = _adminStatusFilter.asStateFlow()

    private val _selectedReceiptOrder = MutableStateFlow<RestaurantOrder?>(null)
    val selectedReceiptOrder: StateFlow<RestaurantOrder?> = _selectedReceiptOrder.asStateFlow()

    fun bindBranchAndTable(branchId: String, tableNum: Int, signature: String? = null) {
        val validBranch = repository.branches.find { it.id == branchId } != null
        if (validBranch) {
            _selectedBranchId.value = branchId
            _selectedTableNumber.value = tableNum
        }
    }

    fun setCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    fun openProductDialog(item: MenuItem) {
        _productInDialog.value = item
    }

    fun closeProductDialog() {
        _productInDialog.value = null
    }

    fun addToCart(item: MenuItem, quantity: Int = 1, spice: String = "normal", note: String = "") {
        val currentList = _cart.value.toMutableList()
        val existingIndex = currentList.indexOfFirst {
            it.item.id == item.id && it.selectedSpice == spice && it.itemNote == note
        }
        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            currentList[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            currentList.add(CartItem(item, quantity, spice, note))
        }
        _cart.value = currentList
    }

    fun updateCartItemQuantity(index: Int, newQuantity: Int) {
        val currentList = _cart.value.toMutableList()
        if (index in currentList.indices) {
            if (newQuantity <= 0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = currentList[index].copy(quantity = newQuantity)
            }
            _cart.value = currentList
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
        customerNotes.value = ""
        allergyNotes.value = ""
    }

    fun submitCustomerOrder(onSuccess: (RestaurantOrder) -> Unit) {
        if (_cart.value.isEmpty()) {
            _orderSubmitError.value = if (_language.value == AppLanguage.ARABIC) "السلة فارغة" else "Cart is empty"
            return
        }

        _isSubmittingOrder.value = true
        _orderSubmitError.value = null

        viewModelScope.launch {
            kotlinx.coroutines.delay(650)
            val idempotencyKey = UUID.randomUUID().toString()
            val result = repository.createServerValidatedOrder(
                branchId = _selectedBranchId.value,
                tableNumber = _selectedTableNumber.value,
                cartItems = _cart.value,
                customerName = customerName.value,
                customerPhone = customerPhone.value,
                customerNotes = customerNotes.value,
                allergyNotes = allergyNotes.value,
                idempotencyKey = idempotencyKey
            )

            _isSubmittingOrder.value = false

            result.onSuccess { newOrder ->
                _activeTrackedOrder.value = newOrder
                clearCart()
                onSuccess(newOrder)
            }.onFailure { err ->
                _orderSubmitError.value = err.localizedMessage ?: "Failed to submit order"
            }
        }
    }

    fun setAdminBranchFilter(filter: String) {
        _adminBranchFilter.value = filter
    }

    fun setAdminStatusFilter(status: OrderStatus?) {
        _adminStatusFilter.value = status
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        repository.updateOrderStatus(orderId, newStatus)
        if (_activeTrackedOrder.value?.id == orderId) {
            _activeTrackedOrder.value = _activeTrackedOrder.value?.copy(status = newStatus)
        }
    }

    fun reprintKitchenTicket(order: RestaurantOrder) {
        repository.spoolThermalPrinterJob(order)
    }

    fun inspectReceipt(order: RestaurantOrder?) {
        _selectedReceiptOrder.value = order
    }

    fun toggleItemAvailability(itemId: String, isAvailable: Boolean) {
        repository.toggleItemAvailability(itemId, isAvailable)
    }

    fun updateItemPriceAndDiscount(itemId: String, price: Double, discountPercent: Int, imageKey: String) {
        repository.updateItemPriceAndDiscount(itemId, price, discountPercent, imageKey)
    }

    fun addNewItem(item: MenuItem) {
        repository.addNewItem(item)
    }
}
