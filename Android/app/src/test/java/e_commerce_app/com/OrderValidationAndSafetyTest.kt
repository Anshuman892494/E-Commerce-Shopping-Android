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
            shipping = 0.0,
            tax = 4.0,
            grandTotal = 54.0,
            estimatedDelivery = "Nov 12, 2026",
            isServerConfirmed = true
        )

        assertTrue(order.isServerConfirmed)
        assertEquals("CONFIRMED", order.status)
        assertEquals(54.0, order.grandTotal, 0.01)
    }
}

