package e_commerce_app.com

import e_commerce_app.com.data.model.*
import e_commerce_app.com.ui.screens.checkout.CheckoutUiState
import org.junit.Assert.*
import org.junit.Test

class OrderValidationAndSafetyTest {

    @Test
    fun checkoutUiState_addressValidationRequiresAllFields() {
        val validState = CheckoutUiState(
            fullName = "Anshul",
            street = "123 Main St",
            city = "New Delhi",
            postalCode = "110001",
            phone = "9876543210"
        )
        assertTrue(validState.isAddressValid)

        val missingStreet = validState.copy(street = "")
        assertFalse(missingStreet.isAddressValid)

        val missingName = validState.copy(fullName = "   ")
        assertFalse(missingName.isAddressValid)

        val missingPostal = validState.copy(postalCode = "")
        assertFalse(missingPostal.isAddressValid)
    }

    @Test
    fun simulationModes_supportAllRequiredTestScenarios() {
        assertEquals("NORMAL", SimulationMode.NORMAL.name)
        assertEquals("CARD_DECLINED", SimulationMode.CARD_DECLINED.name)
        assertEquals("OUT_OF_STOCK", SimulationMode.OUT_OF_STOCK.name)
        assertEquals("TIMEOUT", SimulationMode.TIMEOUT.name)
    }

    @Test
    fun orderObject_preservesServerConfirmationStatus() {
        val order = Order(
            id = "ORD-99999",
            createdAt = "2026-11-08T10:00:00Z",
            status = "CONFIRMED",
            items = emptyList(),
            shippingAddress = ShippingAddress("John", "St", "City", "12345", "9999"),
            paymentMethod = "Sandbox Card",
            subtotal = 50.0,
            discount = 10.0,
            shipping = 0.0,
            tax = 3.20,
            grandTotal = 43.20,
            estimatedDelivery = "Nov 12, 2026",
            isServerConfirmed = true
        )

        assertTrue(order.isServerConfirmed)
        assertEquals("CONFIRMED", order.status)
        assertEquals(43.20, order.grandTotal, 0.01)
        assertEquals(10.0, order.discount, 0.01)
    }

    @Test
    fun checkoutUiState_calculatesCouponDiscountCorrectly() {
        val item = CartItem(
            productId = "p1",
            title = "Sneakers",
            price = 100.0,
            imageUrl = "",
            quantity = 1
        )
        // 20% discount on $100 -> $20 off -> discountedSubtotal $80
        // Shipping: $9.99 (since discountedSubtotal <= $100)
        // Tax: 8% of $80 = $6.40
        // Grand Total: 80 + 9.99 + 6.40 = $96.39
        val stateWithCoupon = CheckoutUiState(
            items = listOf(item),
            appliedCoupon = "ALGIONS20",
            discountPercent = 20.0
        )

        assertEquals(100.0, stateWithCoupon.subtotal, 0.01)
        assertEquals(20.0, stateWithCoupon.discountAmount, 0.01)
        assertEquals(80.0, stateWithCoupon.discountedSubtotal, 0.01)
        assertEquals(9.99, stateWithCoupon.shipping, 0.01)
        assertEquals(6.40, stateWithCoupon.tax, 0.01)
        assertEquals(96.39, stateWithCoupon.grandTotal, 0.01)
    }
}
