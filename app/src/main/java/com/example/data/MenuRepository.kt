package com.example.data

import com.example.model.Branch
import com.example.model.CartItem
import com.example.model.MenuCategory
import com.example.model.MenuItem
import com.example.model.OrderItemRecord
import com.example.model.OrderStatus
import com.example.model.PrinterJob
import com.example.model.RestaurantOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class MenuRepository {

    val branches = listOf(
        Branch(
            id = "mansourah",
            nameAr = "فرع المنصورة",
            nameEn = "Al Mansourah Branch",
            addressAr = "٣٢٤٧ طريق الخرج، حي المنصورة، الرياض ١٢٦٩٢، السعودية",
            addressEn = "3247 Al Kharj Rd, Al Mansourah, Riyadh 12692, Saudi Arabia",
            phone = "+966 11 448 2001",
            tableCount = 20,
            printerIp = "192.168.1.100",
            printerPort = 9100,
            isPrinterOnline = true
        ),
        Branch(
            id = "aziziyah",
            nameAr = "فرع العزيزية",
            nameEn = "Al Aziziyah Branch",
            addressAr = "طريق الخرج، حي العزيزية، الرياض ١٤٥١٣، السعودية",
            addressEn = "Al Kharj Rd, Al Aziziyah, Riyadh 14513, Saudi Arabia",
            phone = "+966 11 495 5002",
            tableCount = 25,
            printerIp = "192.168.2.100",
            printerPort = 9100,
            isPrinterOnline = true
        )
    )

    val categories = listOf(
        MenuCategory("broast", "البروستد", "Broast Meals", "restaurant"),
        MenuCategory("msahhab", "مسحب الدجاج", "Boneless Msahhab", "lunch_dining"),
        MenuCategory("fish_shrimp", "سمك وجمبري", "Fish & Shrimp", "set_meal"),
        MenuCategory("sandwiches", "السندويشات والبرجر", "Burgers & Sandwiches", "fastfood"),
        MenuCategory("sides", "المقبلات والصلصات", "Sides & Dips", "tapas"),
        MenuCategory("drinks", "المشروبات المنعشة", "Beverages", "local_drink")
    )

    private val initialMenuItems = listOf(
        MenuItem(
            id = "broast_normal_4pc",
            categoryId = "broast",
            nameAr = "بروستد عادي (٤ قطع)",
            nameEn = "Broast Chicken Meal - Regular (4 pcs)",
            descriptionAr = "٤ قطع دجاج طازج مقلي ذهبي مقرمش مع بطاطس مقلية، خبز طازج، وصلصة ثوم شهية",
            descriptionEn = "4 pcs crispy golden fried chicken with seasoned french fries, bun, and signature garlic dip",
            priceSar = 27.0,
            discountPercent = 0,
            isSpicy = false,
            calories = "810 cal",
            isAvailable = true,
            imageKey = "food_broast"
        ),
        MenuItem(
            id = "broast_spicy_4pc",
            categoryId = "broast",
            nameAr = "بروستد حراق (٤ قطع)",
            nameEn = "Broast Chicken Meal - Spicy (4 pcs)",
            descriptionAr = "٤ قطع دجاج مقرمش بالتتبيلة الحارة الخاصة مع بطاطس، خبز طازج، وصلصة ثوم",
            descriptionEn = "4 pcs spicy marinated crispy chicken with fries, fresh bun, and garlic sauce",
            priceSar = 27.0,
            discountPercent = 10, // 10% Special Offer
            isSpicy = true,
            calories = "810 cal",
            isAvailable = true,
            imageKey = "food_broast"
        ),
        MenuItem(
            id = "msahhab_normal_8pc",
            categoryId = "msahhab",
            nameAr = "مسحب دجاج عادي (٨ قطع)",
            nameEn = "Chicken Msahhab Meal - Regular (8 pcs)",
            descriptionAr = "٨ قطع صدور دجاج فيليه مقرمشة بدون عظم، بطاطس مقلية، خبز، وصلصة ثوم المختار",
            descriptionEn = "8 pcs boneless chicken breast fillets fried to perfection with fries, bun, and garlic sauce",
            priceSar = 27.0,
            discountPercent = 0,
            isSpicy = false,
            calories = "850 cal",
            isAvailable = true,
            imageKey = "food_msahhab"
        ),
        MenuItem(
            id = "msahhab_spicy_8pc",
            categoryId = "msahhab",
            nameAr = "مسحب دجاج حراق (٨ قطع)",
            nameEn = "Chicken Msahhab Meal - Spicy (8 pcs)",
            descriptionAr = "٨ قطع صدور دجاج فيليه حارة ومقرمشة، بطاطس مقلية، خبز، وصلصة ثوم",
            descriptionEn = "8 pcs fiery spicy boneless chicken tenders, french fries, bun, and garlic dip",
            priceSar = 27.0,
            discountPercent = 15, // 15% Offer
            isSpicy = true,
            calories = "850 cal",
            isAvailable = true,
            imageKey = "food_msahhab"
        ),
        MenuItem(
            id = "burger_chicken_normal",
            categoryId = "sandwiches",
            nameAr = "ساندوتش برجر دجاج عادي",
            nameEn = "Chicken Burger Sandwich - Regular",
            descriptionAr = "قطعة دجاج مقرمشة فاخرة، سلطة ملفوف، مخلل، وصلصة المختار في خبز برجر سمسم",
            descriptionEn = "Crispy chicken fillet patty, coleslaw, crunchy pickles, and special sauce in sesame bun",
            priceSar = 18.0,
            discountPercent = 0,
            isSpicy = false,
            calories = "320 cal",
            isAvailable = true,
            imageKey = "food_burger"
        ),
        MenuItem(
            id = "burger_chicken_spicy",
            categoryId = "sandwiches",
            nameAr = "ساندوتش برجر دجاج حراق",
            nameEn = "Chicken Burger Sandwich - Spicy",
            descriptionAr = "قطعة دجاج حارة مقرمشة، صوص حراق، ملفوف، ومخلل بخبز السمسم الطازج",
            descriptionEn = "Spicy fried chicken fillet, spicy dynamite spread, pickles, and coleslaw",
            priceSar = 18.0,
            discountPercent = 10,
            isSpicy = true,
            calories = "330 cal",
            isAvailable = true,
            imageKey = "food_burger"
        ),
        MenuItem(
            id = "jumbo_msahhab_sandwich",
            categoryId = "sandwiches",
            nameAr = "ساندوتش مسحب جامبو",
            nameEn = "Jumbo Msahhab Sandwich",
            descriptionAr = "ساندوتش جامبو كبير محشو بقطع المسحب المقرمشة مع الثوم والمخلل اللذيذ",
            descriptionEn = "Large sub loaded with crispy boneless msahhab chicken strips, garlic sauce, and pickles",
            priceSar = 23.0,
            discountPercent = 0,
            isSpicy = false,
            calories = "520 cal",
            isAvailable = true,
            imageKey = "food_msahhab"
        ),
        MenuItem(
            id = "msahhab_sandwich_single",
            categoryId = "sandwiches",
            nameAr = "ساندوتش مسحب عادي",
            nameEn = "Msahhab Sandwich",
            descriptionAr = "ساندوتش مسحب كلاسيكي بصلصة الثوم والمخلل المقرمش",
            descriptionEn = "Classic chicken strip roll with creamy garlic spread and pickles",
            priceSar = 12.0,
            discountPercent = 0,
            isSpicy = false,
            calories = "290 cal",
            isAvailable = true,
            imageKey = "food_msahhab"
        ),
        MenuItem(
            id = "chicken_roll_special",
            categoryId = "sandwiches",
            nameAr = "رول دجاج المختار المقرمش",
            nameEn = "Al Mokhtar Chicken Roll Wrap",
            descriptionAr = "رول تورتيلا مقرمش محشو بالدجاج المتبل، صوص المختار الخاص، والبطاطس",
            descriptionEn = "Toasted tortilla wrap with tender seasoned chicken, secret sauce, and potato strips",
            priceSar = 14.0,
            discountPercent = 0,
            isSpicy = false,
            calories = "340 cal",
            isAvailable = true,
            imageKey = "food_msahhab"
        ),
        MenuItem(
            id = "fish_fillet_meal",
            categoryId = "fish_shrimp",
            nameAr = "وجبة فيليه سمك عادي",
            nameEn = "Fish Fillet Meal - Regular",
            descriptionAr = "قطعتان فيليه سمك مقرمش متبل، بطاطس مقلية، خبز طازج، وصلصة بحريات وتارتار",
            descriptionEn = "2 pcs crispy battered tender fish fillets, fries, bun, and signature tartar sauce",
            priceSar = 30.0,
            discountPercent = 0,
            isSpicy = false,
            calories = "750 cal",
            isAvailable = true,
            imageKey = "food_broast"
        ),
        MenuItem(
            id = "crispy_jumbo_shrimp",
            categoryId = "fish_shrimp",
            nameAr = "وجبة جمبري مقرمش (٨ قطع)",
            nameEn = "Crispy Jumbo Shrimp Meal (8 pcs)",
            descriptionAr = "٨ حبات روبيان جامبو مقرمشة بالتتبيلة البحرية، بطاطس، خبز، صلصة بحريات",
            descriptionEn = "8 pcs golden crispy breaded jumbo shrimp, fries, bun, and seafood cocktail dip",
            priceSar = 40.0,
            discountPercent = 10,
            isSpicy = false,
            calories = "680 cal",
            isAvailable = true,
            imageKey = "food_shrimp"
        ),
        MenuItem(
            id = "hummus_fresh",
            categoryId = "sides",
            nameAr = "حمص المختار الطازج",
            nameEn = "Fresh Al Mokhtar Hummus",
            descriptionAr = "حمص ناعم متبل بالطحينة الفاخرة وزيت الزيتون البكر",
            descriptionEn = "Silky smooth authentic hummus dip whipped with premium sesame tahini and olive oil",
            priceSar = 7.0,
            discountPercent = 0,
            isSpicy = false,
            calories = "210 cal",
            isAvailable = true,
            imageKey = "hero_banner"
        ),
        MenuItem(
            id = "coleslaw_salad",
            categoryId = "sides",
            nameAr = "سلطة ملفوف (كول سلو)",
            nameEn = "Fresh Coleslaw Salad",
            descriptionAr = "ملفوف مقرمش مبشور طازج مع جزر وتتبيلة كريمية متوازنة",
            descriptionEn = "Crisp shredded cabbage and carrots folded in rich creamy house dressing",
            priceSar = 6.0,
            discountPercent = 0,
            isSpicy = false,
            calories = "140 cal",
            isAvailable = true,
            imageKey = "hero_banner"
        ),
        MenuItem(
            id = "french_fries_crispy",
            categoryId = "sides",
            nameAr = "بطاطس مقلية مقرمشة",
            nameEn = "Crispy French Fries",
            descriptionAr = "أصابع بطاطس مقلية ذهبية ساخنة ومملحة",
            descriptionEn = "Hot, golden, and crispy salted french potato fries",
            priceSar = 8.0,
            discountPercent = 0,
            isSpicy = false,
            calories = "310 cal",
            isAvailable = true,
            imageKey = "food_broast"
        ),
        MenuItem(
            id = "garlic_sauce_extra",
            categoryId = "sides",
            nameAr = "ثومية إضافية",
            nameEn = "Extra Garlic Dip",
            descriptionAr = "صلصة الثوم الشهيرة الخاصة بمطعم المختار",
            descriptionEn = "Al Mokhtar legendary whipped garlic emulsion dip",
            priceSar = 2.5,
            discountPercent = 0,
            isSpicy = false,
            calories = "90 cal",
            isAvailable = true,
            imageKey = "hero_banner"
        ),
        MenuItem(
            id = "kinza_cola",
            categoryId = "drinks",
            nameAr = "مشروب كينزا كولا (٣٣٠ مل)",
            nameEn = "Kinza Cola (330ml)",
            descriptionAr = "مشروب كينزا كولا سعودي مثلج ومنعش",
            descriptionEn = "Chilled and effervescent Saudi Kinza Cola can",
            priceSar = 5.0,
            discountPercent = 0,
            isSpicy = false,
            calories = "140 cal",
            isAvailable = true,
            imageKey = "hero_banner"
        ),
        MenuItem(
            id = "water_mineral",
            categoryId = "drinks",
            nameAr = "مياه شرب نقية",
            nameEn = "Pure Mineral Water",
            descriptionAr = "مياه شرب معبأة نقية باردة",
            descriptionEn = "Pure bottled drinking water",
            priceSar = 1.5,
            discountPercent = 0,
            isSpicy = false,
            calories = "0 cal",
            isAvailable = true,
            imageKey = "hero_banner"
        )
    )

    private val _menuItems = MutableStateFlow(initialMenuItems)
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    private val orderSequence = AtomicInteger(100)
    private val processedIdempotencyKeys = mutableSetOf<String>()

    private val _orders = MutableStateFlow<List<RestaurantOrder>>(emptyList())
    val orders: StateFlow<List<RestaurantOrder>> = _orders.asStateFlow()

    private val _printerJobs = MutableStateFlow<List<PrinterJob>>(emptyList())
    val printerJobs: StateFlow<List<PrinterJob>> = _printerJobs.asStateFlow()

    init {
        seedInitialDemoOrders()
    }

    private fun seedInitialDemoOrders() {
        val now = System.currentTimeMillis()
        val o1 = RestaurantOrder(
            id = UUID.randomUUID().toString(),
            orderNumber = "MKT-MN-2401",
            branchId = "mansourah",
            branchNameAr = "فرع المنصورة",
            branchNameEn = "Al Mansourah Branch",
            tableNumber = 4,
            items = listOf(
                OrderItemRecord(
                    itemId = "broast_normal_4pc",
                    nameAr = "بروستد عادي (٤ قطع)",
                    nameEn = "Broast Chicken Meal - Regular (4 pcs)",
                    quantity = 2,
                    unitPriceSar = 27.0,
                    lineTotalSar = 54.0,
                    spiceOption = "normal",
                    itemNote = "زيادة ثوم"
                ),
                OrderItemRecord(
                    itemId = "hummus_fresh",
                    nameAr = "حمص المختار الطازج",
                    nameEn = "Fresh Al Mokhtar Hummus",
                    quantity = 1,
                    unitPriceSar = 7.0,
                    lineTotalSar = 7.0,
                    spiceOption = "normal"
                )
            ),
            subtotalSar = 61.0,
            vatSar = 7.96,
            totalSar = 61.0,
            customerName = "عبدالرحمن",
            customerNotes = "الرجاء تقديم الوجبة ساخنة جداً",
            allergyNotes = "",
            status = OrderStatus.PREPARING,
            createdAtMillis = now - 12 * 60 * 1000,
            idempotencyKey = "demo_key_1",
            printedToKitchen = true,
            printedAtMillis = now - 11 * 60 * 1000
        )
        _orders.value = listOf(o1)
    }

    fun createServerValidatedOrder(
        branchId: String,
        tableNumber: Int,
        cartItems: List<CartItem>,
        customerName: String,
        customerPhone: String,
        customerNotes: String,
        allergyNotes: String,
        idempotencyKey: String
    ): Result<RestaurantOrder> {
        if (processedIdempotencyKeys.contains(idempotencyKey)) {
            val existing = _orders.value.firstOrNull { it.idempotencyKey == idempotencyKey }
            if (existing != null) {
                return Result.success(existing)
            }
        }

        val branch = branches.firstOrNull { it.id == branchId }
            ?: return Result.failure(IllegalArgumentException("Invalid branch: $branchId"))

        if (tableNumber < 1 || tableNumber > branch.tableCount) {
            return Result.failure(IllegalArgumentException("Invalid table $tableNumber"))
        }

        if (cartItems.isEmpty()) {
            return Result.failure(IllegalArgumentException("Cart is empty"))
        }

        val masterCatalog = _menuItems.value.associateBy { it.id }
        val orderItems = mutableListOf<OrderItemRecord>()
        var calculatedSubtotal = 0.0

        for (cartItem in cartItems) {
            val masterItem = masterCatalog[cartItem.item.id]
                ?: return Result.failure(IllegalArgumentException("Item not found: ${cartItem.item.id}"))

            if (!masterItem.isAvailable) {
                return Result.failure(IllegalStateException("Item sold out: ${masterItem.nameAr}"))
            }

            val truePrice = masterItem.effectivePriceSar
            val lineTotal = truePrice * cartItem.quantity
            calculatedSubtotal += lineTotal

            orderItems.add(
                OrderItemRecord(
                    itemId = masterItem.id,
                    nameAr = masterItem.nameAr,
                    nameEn = masterItem.nameEn,
                    quantity = cartItem.quantity,
                    unitPriceSar = truePrice,
                    lineTotalSar = lineTotal,
                    spiceOption = cartItem.selectedSpice,
                    itemNote = cartItem.itemNote.trim().take(150)
                )
            )
        }

        val vatSar = calculatedSubtotal - (calculatedSubtotal / 1.15)
        val branchCode = if (branch.id == "mansourah") "MN" else "AZ"
        val dateCode = SimpleDateFormat("yyMMdd", Locale.ENGLISH).format(Date())
        val seq = orderSequence.incrementAndGet()
        val orderNum = "MKT-$branchCode-$dateCode-$seq"

        val newOrder = RestaurantOrder(
            id = UUID.randomUUID().toString(),
            orderNumber = orderNum,
            branchId = branch.id,
            branchNameAr = branch.nameAr,
            branchNameEn = branch.nameEn,
            tableNumber = tableNumber,
            items = orderItems,
            subtotalSar = calculatedSubtotal,
            vatSar = Math.round(vatSar * 100.0) / 100.0,
            totalSar = Math.round(calculatedSubtotal * 100.0) / 100.0,
            customerName = customerName.trim().take(50),
            customerPhone = customerPhone.trim().take(20),
            customerNotes = customerNotes.trim().take(250),
            allergyNotes = allergyNotes.trim().take(250),
            status = OrderStatus.NEW,
            createdAtMillis = System.currentTimeMillis(),
            idempotencyKey = idempotencyKey,
            printedToKitchen = false
        )

        processedIdempotencyKeys.add(idempotencyKey)
        _orders.value = listOf(newOrder) + _orders.value
        spoolThermalPrinterJob(newOrder)

        return Result.success(newOrder)
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        _orders.value = _orders.value.map {
            if (it.id == orderId) it.copy(status = newStatus) else it
        }
    }

    fun toggleItemAvailability(itemId: String, isAvailable: Boolean) {
        _menuItems.value = _menuItems.value.map {
            if (it.id == itemId) it.copy(isAvailable = isAvailable) else it
        }
    }

    fun updateItemPriceAndDiscount(itemId: String, newPriceSar: Double, discountPercent: Int, imageKey: String) {
        _menuItems.value = _menuItems.value.map {
            if (it.id == itemId) {
                it.copy(
                    priceSar = Math.max(0.5, Math.round(newPriceSar * 100.0) / 100.0),
                    discountPercent = discountPercent.coerceIn(0, 90),
                    imageKey = imageKey
                )
            } else {
                it
            }
        }
    }

    fun addNewItem(item: MenuItem) {
        _menuItems.value = _menuItems.value + item
    }

    fun spoolThermalPrinterJob(order: RestaurantOrder) {
        val branch = branches.firstOrNull { it.id == order.branchId }
        val ticketText = ThermalEscPosUtil.generateKitchenTicketText(order)
        val job = PrinterJob(
            id = UUID.randomUUID().toString(),
            orderId = order.id,
            branchId = order.branchId,
            printerName = "${branch?.nameEn ?: "Kitchen"} ESC/POS 80mm",
            status = "PRINTED",
            timestampMillis = System.currentTimeMillis(),
            rawEscPosPreview = ticketText
        )
        _printerJobs.value = listOf(job) + _printerJobs.value
        _orders.value = _orders.value.map {
            if (it.id == order.id) it.copy(printedToKitchen = true, printedAtMillis = System.currentTimeMillis()) else it
        }
    }
}
