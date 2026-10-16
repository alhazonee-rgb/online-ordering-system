package com.example.data

import com.example.model.RestaurantOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ThermalEscPosUtil {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

    /**
     * Generates standard ESC/POS 80mm thermal receipt bytes representation
     * and human-readable text preview for kitchen and customer slips.
     */
    fun generateKitchenTicketText(order: RestaurantOrder): String {
        val dateStr = dateFormat.format(Date(order.createdAtMillis))
        val sb = StringBuilder()

        sb.appendLine("================================================")
        sb.appendLine("           مطعم المختار - تذكرة المطبخ            ")
        sb.appendLine("         AL MOKHTAR RESTAURANT - KITCHEN        ")
        sb.appendLine("================================================")
        sb.appendLine("الفرع: ${order.branchNameAr}")
        sb.appendLine("Branch: ${order.branchNameEn}")
        sb.appendLine("------------------------------------------------")
        sb.appendLine("رقم الطاولة / TABLE: # ${order.tableNumber}")
        sb.appendLine("رقم الطلب   / ORDER: ${order.orderNumber}")
        sb.appendLine("التاريخ والوقت / DATE: $dateStr")
        sb.appendLine("------------------------------------------------")
        sb.appendLine("الكمية | الصنف                           | الخيار")
        sb.appendLine("QTY  | ITEM                           | OPTION")
        sb.appendLine("------------------------------------------------")

        for (item in order.items) {
            val spice = if (item.spiceOption == "spicy") "[حراق / SPICY]" else "[عادي / REG]"
            val name = "${item.nameAr} (${item.nameEn})"
            sb.appendLine("%-4d | %-30s | %s".format(item.quantity, item.nameAr.take(28), spice))
            if (item.itemNote.isNotBlank()) {
                sb.appendLine("     * ملاحظة: ${item.itemNote}")
            }
        }

        sb.appendLine("------------------------------------------------")
        if (order.allergyNotes.isNotBlank()) {
            sb.appendLine("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            sb.appendLine("!!! تنبيه حساسية مهم / ALLERGY ALERT:         !!!")
            sb.appendLine(">>> ${order.allergyNotes}")
            sb.appendLine("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        }
        if (order.customerNotes.isNotBlank()) {
            sb.appendLine("ملاحظات العميل: ${order.customerNotes}")
        }
        sb.appendLine("------------------------------------------------")
        sb.appendLine("الحالة: ${order.status.titleAr} (${order.status.titleEn})")
        sb.appendLine("================================================")
        sb.appendLine("               [قطع الورقة / CUT]               \n\n")

        return sb.toString()
    }

    fun generateCustomerReceiptText(order: RestaurantOrder): String {
        val dateStr = dateFormat.format(Date(order.createdAtMillis))
        val sb = StringBuilder()

        sb.appendLine("================================================")
        sb.appendLine("                 مطعم المختار                   ")
        sb.appendLine("            AL MOKHTAR RESTAURANT               ")
        sb.appendLine("        الرياض - المملكة العربية السعودية        ")
        sb.appendLine("================================================")
        sb.appendLine("الفرع: ${order.branchNameAr}")
        sb.appendLine("الرقم الضريبي (VAT ID): 300124892400003")
        sb.appendLine("فاتورة ضريبية مبسطة / Simplified Tax Invoice")
        sb.appendLine("------------------------------------------------")
        sb.appendLine("رقم الطلب / Order #: ${order.orderNumber}")
        sb.appendLine("طاولة / Table: ${order.tableNumber}")
        sb.appendLine("التاريخ / Date: $dateStr")
        if (order.customerName.isNotBlank()) {
            sb.appendLine("العميل / Customer: ${order.customerName}")
        }
        sb.appendLine("------------------------------------------------")
        sb.appendLine("الصنف                   | الكمية | السعر | الإجمالي")
        sb.appendLine("------------------------------------------------")

        for (item in order.items) {
            val spice = if (item.spiceOption == "spicy") " (حراق)" else ""
            val line = "%-20s | %2d  | %5.2f | %6.2f SAR".format(
                (item.nameAr + spice).take(20),
                item.quantity,
                item.unitPriceSar,
                item.lineTotalSar
            )
            sb.appendLine(line)
        }

        sb.appendLine("------------------------------------------------")
        sb.appendLine("المجموع الفرعي (غير شامل الضريبة): %8.2f SAR".format(order.subtotalSar - order.vatSar))
        sb.appendLine("ضريبة القيمة المضافة (15%% VAT):   %8.2f SAR".format(order.vatSar))
        sb.appendLine("------------------------------------------------")
        sb.appendLine("الإجمالي الصافي النهائي:        %8.2f SAR".format(order.totalSar))
        sb.appendLine("TOTAL PAYABLE:                  %8.2f SAR".format(order.totalSar))
        sb.appendLine("================================================")
        sb.appendLine("        شكراً لزيارتكم مطعم المختار بالرياض      ")
        sb.appendLine("         Thank you for choosing Al Mokhtar      ")
        sb.appendLine("================================================")
        sb.appendLine("\n\n")

        return sb.toString()
    }

    /**
     * Converts raw text to ESC/POS byte sequence format for ESC/POS network/USB printers.
     * (ESC @ [Init], ESC a 1 [Center], GS V 66 0 [Cut])
     */
    fun createEscPosCommandHex(receiptText: String): String {
        val initCmd = "1B 40 " // ESC @
        val centerCmd = "1B 61 01 " // Center
        val cutCmd = "1D 56 42 00 " // GS V 66 0
        val textBytesHex = receiptText.toByteArray(Charsets.UTF_8).joinToString(" ") { "%02X".format(it) }
        return "$initCmd$centerCmd$textBytesHex $cutCmd"
    }
}
