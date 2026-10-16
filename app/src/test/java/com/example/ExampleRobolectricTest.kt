package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.MenuRepository
import com.example.data.QrSecurityUtil
import com.example.data.ThermalEscPosUtil
import com.example.model.CartItem
import com.example.model.OrderStatus
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches Al Mokhtar`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Al Mokhtar", appName)
    }

    @Test
    fun `qr signature generation and validation passes for valid table`() {
        val qrData = QrSecurityUtil.generateTableQr(branchId = "mansourah", tableNumber = 4)
        assertTrue(qrData.signature.isNotEmpty())
        assertTrue(qrData.url.contains("b=mansourah&t=4"))

        val isValid = QrSecurityUtil.validateTableSignature("mansourah", 4, qrData.signature)
        assertTrue(isValid)

        // Forged table number should fail
        val isTampered = QrSecurityUtil.validateTableSignature("mansourah", 99, qrData.signature)
        assertFalse(isTampered)
    }

    @Test
    fun `server price recalculation prevents client side price tampering`() {
        val repo = MenuRepository()
        val broastItem = repo.menuItems.value.first { it.id == "broast_normal_4pc" }

        // Client attempts to claim price is 1.0 SAR instead of real 27.0 SAR
        val tamperedItem = broastItem.copy(priceSar = 1.0)
        val cartItems = listOf(CartItem(item = tamperedItem, quantity = 2))

        val result = repo.createServerValidatedOrder(
            branchId = "mansourah",
            tableNumber = 4,
            cartItems = cartItems,
            customerName = "Ali",
            customerPhone = "0501234567",
            customerNotes = "",
            allergyNotes = "",
            idempotencyKey = UUID.randomUUID().toString()
        )

        assertTrue(result.isSuccess)
        val order = result.getOrNull()!!
        // The server recalculated at official 27.0 SAR * 2 = 54.0 SAR!
        assertEquals(54.0, order.totalSar, 0.01)
        assertEquals(OrderStatus.NEW, order.status)
    }

    @Test
    fun `idempotency prevents duplicate order submission`() {
        val repo = MenuRepository()
        val item = repo.menuItems.value.first()
        val cartItems = listOf(CartItem(item = item, quantity = 1))
        val idempotencyKey = "unique_key_12345"

        val res1 = repo.createServerValidatedOrder(
            branchId = "mansourah",
            tableNumber = 2,
            cartItems = cartItems,
            customerName = "Sara",
            customerPhone = "",
            customerNotes = "",
            allergyNotes = "",
            idempotencyKey = idempotencyKey
        )
        assertTrue(res1.isSuccess)
        val order1 = res1.getOrNull()!!

        // Duplicate submission with same key returns the existing order instead of creating a second one
        val res2 = repo.createServerValidatedOrder(
            branchId = "mansourah",
            tableNumber = 2,
            cartItems = cartItems,
            customerName = "Sara",
            customerPhone = "",
            customerNotes = "",
            allergyNotes = "",
            idempotencyKey = idempotencyKey
        )
        assertTrue(res2.isSuccess)
        val order2 = res2.getOrNull()!!
        assertEquals(order1.id, order2.id)
        assertEquals(order1.orderNumber, order2.orderNumber)
    }

    @Test
    fun `thermal esc pos receipt formats ZATCA vat details and arabic header`() {
        val repo = MenuRepository()
        val order = repo.orders.value.first()
        val receiptText = ThermalEscPosUtil.generateCustomerReceiptText(order)

        assertTrue(receiptText.contains("AL MOKHTAR RESTAURANT"))
        assertTrue(receiptText.contains("300124892400003")) // ZATCA Tax ID
        assertTrue(receiptText.contains("SAR"))
    }
}
