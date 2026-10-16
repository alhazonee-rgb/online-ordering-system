package com.example.model

enum class AppLanguage(val code: String, val titleAr: String, val titleEn: String) {
    ARABIC("ar", "العربية", "Arabic"),
    ENGLISH("en", "الإنجليزية", "English")
}

data class Branch(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val addressAr: String,
    val addressEn: String,
    val phone: String,
    val tableCount: Int,
    val printerIp: String,
    val printerPort: Int = 9100,
    val isPrinterOnline: Boolean = true
)

data class MenuCategory(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val iconName: String
)

data class MenuItem(
    val id: String,
    val categoryId: String,
    val nameAr: String,
    val nameEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val priceSar: Double,
    val discountPercent: Int = 0, // e.g. 10%, 15%, 20% discount offer
    val isSpicy: Boolean = false,
    val calories: String = "",
    val isAvailable: Boolean = true,
    val imageKey: String = "food_broast", // "food_broast", "food_msahhab", "food_burger", "food_shrimp", etc.
    val requiresOwnerConfirmation: Boolean = false
) {
    val effectivePriceSar: Double
        get() = if (discountPercent > 0) {
            Math.round(priceSar * (1.0 - (discountPercent / 100.0)) * 100.0) / 100.0
        } else {
            priceSar
        }
}

data class CartItem(
    val item: MenuItem,
    var quantity: Int,
    val selectedSpice: String = "normal", // "normal" or "spicy"
    val itemNote: String = ""
) {
    val lineTotalSar: Double get() = item.effectivePriceSar * quantity
}

enum class OrderStatus(val titleAr: String, val titleEn: String, val colorHex: Long) {
    NEW("طلب جديد", "New", 0xFF0288D1),
    ACCEPTED("تم القبول", "Accepted", 0xFF7B1FA2),
    PREPARING("جاري التحضير", "Preparing", 0xFFED6C02),
    READY("جاهز للتقديم", "Ready", 0xFF2E7D32),
    COMPLETED("مكتمل ومغلق", "Completed", 0xFF455A64),
    CANCELLED("ملغي", "Cancelled", 0xFFD32F2F)
}

data class OrderItemRecord(
    val itemId: String,
    val nameAr: String,
    val nameEn: String,
    val quantity: Int,
    val unitPriceSar: Double,
    val lineTotalSar: Double,
    val spiceOption: String,
    val itemNote: String = ""
)

data class RestaurantOrder(
    val id: String,
    val orderNumber: String,
    val branchId: String,
    val branchNameAr: String,
    val branchNameEn: String,
    val tableNumber: Int,
    val items: List<OrderItemRecord>,
    val subtotalSar: Double,
    val vatSar: Double, // 15% VAT (ZATCA compliant)
    val totalSar: Double,
    val customerName: String = "",
    val customerPhone: String = "",
    val customerNotes: String = "",
    val allergyNotes: String = "",
    val status: OrderStatus = OrderStatus.NEW,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val idempotencyKey: String = "",
    val printedToKitchen: Boolean = false,
    val printedAtMillis: Long? = null
)

data class TableQrData(
    val branchId: String,
    val tableNumber: Int,
    val signature: String,
    val generatedAtMillis: Long,
    val url: String
)

data class PrinterJob(
    val id: String,
    val orderId: String,
    val branchId: String,
    val printerName: String,
    val status: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val rawEscPosPreview: String
)
