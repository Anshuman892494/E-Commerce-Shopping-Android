package e_commerce_app.com

import e_commerce_app.com.data.model.CartItem
import e_commerce_app.com.data.model.Product
import e_commerce_app.com.ui.screens.cart.CartUiState
import org.junit.Assert.*
import org.junit.Test

class CartLogicTest {

    private val sampleProduct = Product(
        id = "prod-test-1",
        title = "Wireless Noise Cancelling Headphones",
        category = "Electronics",
        price = 100.0,
        originalPrice = 120.0,
        rating = 4.8,
        reviewCount = 100,
        imageUrl = "https://example.com/image.jpg",
        description = "Test description",
        stock = 3
    )

    @Test
    fun cartUiState_calculatesSubtotalCorrectly() {
        val item1 = CartItem(
            productId = "p1",
            title = "Item 1",
            price = 50.0,
            imageUrl = "",
            quantity = 2
        )
        val item2 = CartItem(
            productId = "p2",
            title = "Item 2",
            price = 30.0,
            imageUrl = "",
            quantity = 1
        )

        val state = CartUiState(items = listOf(item1, item2))

        assertEquals(130.0, state.subtotal, 0.01)
    }

    @Test
    fun cartUiState_freeShippingAbove100Dollars() {
        val expensiveItem = CartItem(
            productId = "p1",
            title = "Expensive Item",
            price = 120.0,
            imageUrl = "",
            quantity = 1
        )
        val cheapItem = CartItem(
            productId = "p2",
            title = "Cheap Item",
            price = 40.0,
            imageUrl = "",
            quantity = 1
        )

        val stateFreeShipping = CartUiState(items = listOf(expensiveItem))
        val statePaidShipping = CartUiState(items = listOf(cheapItem))

        assertEquals(0.0, stateFreeShipping.shipping, 0.01)
        assertEquals(9.99, statePaidShipping.shipping, 0.01)
    }

    @Test
    fun cartUiState_calculatesTaxAndGrandTotalAccurately() {
        val item = CartItem(
            productId = "p1",
            title = "Test Item",
            price = 100.0,
            imageUrl = "",
            quantity = 1
        )

        // Subtotal: 100.0, Shipping: 9.99 (since not > 100.0), Tax: 8.0 (8%)
        val state = CartUiState(items = listOf(item))

        assertEquals(100.0, state.subtotal, 0.01)
        assertEquals(9.99, state.shipping, 0.01)
        assertEquals(8.0, state.tax, 0.01)
        assertEquals(117.99, state.grandTotal, 0.01)
    }

    @Test
    fun cartItem_variantKeyDifferentiatesVariantCombinations() {
        val itemBlackM = CartItem(
            productId = "prod-1",
            title = "Hoodie",
            price = 50.0,
            imageUrl = "",
            selectedColor = "Black",
            selectedSize = "M",
            quantity = 1
        )

        val itemBlackL = CartItem(
            productId = "prod-1",
            title = "Hoodie",
            price = 50.0,
            imageUrl = "",
            selectedColor = "Black",
            selectedSize = "L",
            quantity = 1
        )

        assertNotEquals(itemBlackM.variantKey, itemBlackL.variantKey)
        assertEquals("prod-1-Black-M", itemBlackM.variantKey)
        assertEquals("prod-1-Black-L", itemBlackL.variantKey)
    }
}

