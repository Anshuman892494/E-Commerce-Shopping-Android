package e_commerce_app.com.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import e_commerce_app.com.data.model.CartItem
import e_commerce_app.com.data.model.Order
import e_commerce_app.com.data.model.Product

/**
 * SQLite Local Database for offline caching and state persistence.
 * Persists:
 * 1. Catalog Products (for offline browsing)
 * 2. Active Cart items (across app restarts)
 * 3. Order History (offline receipts and order status)
 */
class LocalDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val gson = Gson()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_PRODUCTS (
                id TEXT PRIMARY KEY,
                category TEXT,
                json_data TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_CART (
                variant_key TEXT PRIMARY KEY,
                product_id TEXT,
                json_data TEXT,
                quantity INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_ORDERS (
                id TEXT PRIMARY KEY,
                created_at TEXT,
                status TEXT,
                json_data TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_WISHLIST (
                product_id TEXT PRIMARY KEY,
                json_data TEXT,
                added_at INTEGER
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS $TABLE_WISHLIST (
                    product_id TEXT PRIMARY KEY,
                    json_data TEXT,
                    added_at INTEGER
                )
                """.trimIndent()
            )
        }
    }

    // ---------------------------------------------------------
    // PRODUCTS CACHE
    // ---------------------------------------------------------
    fun saveProducts(products: List<Product>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_PRODUCTS, null, null)
            for (product in products) {
                val values = ContentValues().apply {
                    put("id", product.id)
                    put("category", product.category)
                    put("json_data", gson.toJson(product))
                }
                db.insertWithOnConflict(TABLE_PRODUCTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getCachedProducts(): List<Product> {
        val list = mutableListOf<Product>()
        val db = readableDatabase
        val cursor = db.query(TABLE_PRODUCTS, arrayOf("json_data"), null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val json = it.getString(0)
                val product = gson.fromJson(json, Product::class.java)
                list.add(product)
            }
        }
        return list
    }

    // ---------------------------------------------------------
    // CART PERSISTENCE (Preserved across app restarts)
    // ---------------------------------------------------------
    fun saveCart(cartItems: List<CartItem>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_CART, null, null)
            for (item in cartItems) {
                val values = ContentValues().apply {
                    put("variant_key", item.variantKey)
                    put("product_id", item.productId)
                    put("quantity", item.quantity)
                    put("json_data", gson.toJson(item))
                }
                db.insertWithOnConflict(TABLE_CART, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getCachedCart(): List<CartItem> {
        val list = mutableListOf<CartItem>()
        val db = readableDatabase
        val cursor = db.query(TABLE_CART, arrayOf("json_data"), null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val json = it.getString(0)
                val item = gson.fromJson(json, CartItem::class.java)
                list.add(item)
            }
        }
        return list
    }

    fun clearCart() {
        val db = writableDatabase
        db.delete(TABLE_CART, null, null)
    }

    // ---------------------------------------------------------
    // ORDERS CACHE
    // ---------------------------------------------------------
    fun saveOrders(orders: List<Order>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (order in orders) {
                val values = ContentValues().apply {
                    put("id", order.id)
                    put("created_at", order.createdAt)
                    put("status", order.status)
                    put("json_data", gson.toJson(order))
                }
                db.insertWithOnConflict(TABLE_ORDERS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun saveSingleOrder(order: Order) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", order.id)
            put("created_at", order.createdAt)
            put("status", order.status)
            put("json_data", gson.toJson(order))
        }
        db.insertWithOnConflict(TABLE_ORDERS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getCachedOrders(): List<Order> {
        val list = mutableListOf<Order>()
        val db = readableDatabase
        val cursor = db.query(TABLE_ORDERS, arrayOf("json_data"), null, null, null, null, "created_at DESC")
        cursor.use {
            while (it.moveToNext()) {
                val json = it.getString(0)
                val order = gson.fromJson(json, Order::class.java)
                list.add(order)
            }
        }
        return list
    }

    // ---------------------------------------------------------
    // WISHLIST PERSISTENCE
    // ---------------------------------------------------------
    fun saveWishlistItem(product: Product) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("product_id", product.id)
            put("json_data", gson.toJson(product))
            put("added_at", System.currentTimeMillis())
        }
        db.insertWithOnConflict(TABLE_WISHLIST, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun removeWishlistItem(productId: String) {
        val db = writableDatabase
        db.delete(TABLE_WISHLIST, "product_id = ?", arrayOf(productId))
    }

    fun isInWishlist(productId: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(TABLE_WISHLIST, arrayOf("product_id"), "product_id = ?", arrayOf(productId), null, null, null)
        cursor.use {
            return it.count > 0
        }
    }

    fun getWishlist(): List<Product> {
        val list = mutableListOf<Product>()
        val db = readableDatabase
        val cursor = db.query(TABLE_WISHLIST, arrayOf("json_data"), null, null, null, null, "added_at DESC")
        cursor.use {
            while (it.moveToNext()) {
                val json = it.getString(0)
                val prod = gson.fromJson(json, Product::class.java)
                list.add(prod)
            }
        }
        return list
    }

    companion object {
        private const val DATABASE_NAME = "ecommerce_app.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_PRODUCTS = "cached_products"
        private const val TABLE_CART = "cart_items"
        private const val TABLE_ORDERS = "cached_orders"
        private const val TABLE_WISHLIST = "wishlist_items"
    }
}

