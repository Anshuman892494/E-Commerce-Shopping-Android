const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');

const initialProducts = require('./data/products');
const Product = require('./models/Product');
const User = require('./models/User');
const Order = require('./models/Order');
const Coupon = require('./models/Coupon');

const app = express();
const PORT = process.env.PORT || 5000;
const SECURE_API_KEY = process.env.API_KEY || 'algions-secure-api-key-2026';
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://127.0.0.1:27017/ecommerce_db';

// Enable CORS & JSON parsing
app.use(cors());
app.use(express.json());

// Request logger
app.use((req, res, next) => {
  console.log(`[HTTP] ${req.method} ${req.originalUrl}`);
  next();
});

// Secure API Key Validation Middleware
app.use((req, res, next) => {
  // Allow health check without key
  if (req.path === '/api/health') return next();

  const apiKey = req.headers['x-api-key'];
  if (!apiKey || apiKey !== SECURE_API_KEY) {
    console.warn(`[AUTH REJECTED] Missing or invalid x-api-key from ${req.ip}`);
    return res.status(401).json({
      error: 'UNAUTHORIZED',
      message: 'Invalid or missing secure x-api-key header.'
    });
  }
  next();
});

function getBearerToken(req) {
  const authHeader = req.headers['authorization'];
  if (!authHeader) return null;
  const parts = authHeader.split(' ');
  return parts.length === 2 && parts[0] === 'Bearer' ? parts[1] : null;
}

// -------------------------------------------------------------
// MONGODB CONNECTION & AUTO-SEEDING
// -------------------------------------------------------------
async function seedDatabaseIfEmpty() {
  try {
    const productCount = await Product.countDocuments();
    if (productCount === 0) {
      console.log('🌱 Seeding MongoDB Product collection with initial catalog...');
      await Product.insertMany(initialProducts);
      console.log(`✅ Seeded ${initialProducts.length} products into MongoDB.`);
    }

    const userCount = await User.countDocuments();
    if (userCount === 0) {
      console.log('🌱 Seeding MongoDB User collection with demo account...');
      await User.create({
        id: 'user-1',
        name: 'Algions Demo Student',
        email: 'demo@algions.com',
        password: 'Password123!',
        token: 'mock-jwt-token-algions-2026-auth-session',
        shippingAddress: {
          fullName: 'Algions Demo Student',
          street: '452 Innovation Blvd, Suite 300',
          city: 'Bangalore',
          state: 'KA',
          postalCode: '560100',
          phone: '+91 98765 43210'
        }
      });
      console.log('✅ Seeded demo user: demo@algions.com (Password123!).');
    }

    const couponCount = await Coupon.countDocuments();
    if (couponCount === 0) {
      console.log('🌱 Seeding MongoDB Coupons...');
      await Coupon.insertMany([
        { code: 'ALGIONS20', discountPercent: 20, description: '20% off entire order for Algions candidates' },
        { code: 'FREESHIP', discountPercent: 10, description: '10% shipping courtesy discount' },
        { code: 'SUPER50', discountPercent: 50, description: '50% flash celebration discount' }
      ]);
      console.log('✅ Seeded promotional coupons.');
    }
  } catch (err) {
    console.error('❌ Error during database seeding:', err.message);
  }
}

mongoose.connect(MONGODB_URI)
  .then(async () => {
    console.log(`=======================================================`);
    console.log(`🍃 Connected to MongoDB: ${MONGODB_URI}`);
    console.log(`=======================================================`);
    await seedDatabaseIfEmpty();
  })
  .catch(err => {
    console.error('❌ Failed to connect to MongoDB:', err.message);
  });

// -------------------------------------------------------------
// 1. HEALTH CHECK & RESET
// -------------------------------------------------------------
app.get('/api/health', async (req, res) => {
  const pCount = await Product.countDocuments().catch(() => 0);
  const oCount = await Order.countDocuments().catch(() => 0);
  res.json({
    status: 'healthy',
    timestamp: new Date().toISOString(),
    service: 'Algions E-Commerce Live Backend (MongoDB)',
    database: mongoose.connection.readyState === 1 ? 'connected' : 'disconnected',
    productsCount: pCount,
    ordersCount: oCount
  });
});

app.post('/api/reset', async (req, res) => {
  try {
    await Product.deleteMany({});
    await Product.insertMany(initialProducts);
    await Order.deleteMany({});
    console.log('[RESET] Catalog and orders reset in MongoDB.');
    res.json({ message: 'Catalog inventory and orders reset to initial seed state in MongoDB.' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// -------------------------------------------------------------
// 2. AUTHENTICATION & PROFILE
// -------------------------------------------------------------
app.post('/api/auth/login', async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) {
      return res.status(400).json({ error: 'Email and password are required.' });
    }

    const user = await User.findOne({ email: email.toLowerCase() });
    if (!user || user.password !== password) {
      return res.status(401).json({ error: 'Invalid email or password.' });
    }

    console.log(`[AUTH] User "${user.email}" successfully logged in (MongoDB verified).`);
    res.json({
      token: user.token,
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        shippingAddress: user.shippingAddress
      }
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.post('/api/auth/register', async (req, res) => {
  try {
    const { name, email, password } = req.body;
    if (!name || !email || !password) {
      return res.status(400).json({ error: 'Name, email, and password are required.' });
    }

    const existing = await User.findOne({ email: email.toLowerCase() });
    if (existing) {
      return res.status(400).json({ error: 'Account with this email already exists.' });
    }

    const newUser = await User.create({
      id: `user-${Date.now()}`,
      name,
      email: email.toLowerCase(),
      password,
      token: `token-${Date.now()}-${Math.random().toString(36).substring(2)}`,
      shippingAddress: {
        fullName: name,
        street: '123 Tech Park',
        city: 'Bangalore',
        state: 'KA',
        postalCode: '560001',
        phone: '+91 90000 00000'
      }
    });

    console.log(`[AUTH] Registered new user "${newUser.email}" in MongoDB.`);
    res.status(201).json({
      token: newUser.token,
      user: {
        id: newUser.id,
        name: newUser.name,
        email: newUser.email,
        shippingAddress: newUser.shippingAddress
      }
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/auth/me', async (req, res) => {
  try {
    const token = getBearerToken(req);
    if (!token) return res.status(401).json({ error: 'Missing authorization token.' });

    const user = await User.findOne({ token });
    if (!user) return res.status(401).json({ error: 'Session expired or invalid token.' });

    res.json({
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        shippingAddress: user.shippingAddress
      }
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.put('/api/auth/profile', async (req, res) => {
  try {
    const token = getBearerToken(req);
    if (!token) return res.status(401).json({ error: 'Missing authorization token.' });

    const user = await User.findOne({ token });
    if (!user) return res.status(401).json({ error: 'Session expired or invalid token.' });

    const { name, shippingAddress } = req.body;
    if (name) user.name = name;
    if (shippingAddress) user.shippingAddress = shippingAddress;

    await user.save();
    console.log(`[PROFILE] Updated profile for user "${user.email}".`);
    res.json({
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        shippingAddress: user.shippingAddress
      }
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// -------------------------------------------------------------
// 3. PRODUCT CATALOG
// -------------------------------------------------------------
app.get('/api/products', async (req, res) => {
  try {
    const { category, search, sort } = req.query;
    const filter = {};

    if (category && category !== 'All') {
      filter.category = new RegExp(`^${category}$`, 'i');
    }

    if (search && search.trim()) {
      const q = search.trim();
      filter.$or = [
        { title: { $regex: q, $options: 'i' } },
        { description: { $regex: q, $options: 'i' } },
        { category: { $regex: q, $options: 'i' } }
      ];
    }

    let sortOption = {};
    if (sort === 'price_asc') {
      sortOption = { price: 1 };
    } else if (sort === 'price_desc') {
      sortOption = { price: -1 };
    } else if (sort === 'rating') {
      sortOption = { rating: -1 };
    }

    const products = await Product.find(filter).sort(sortOption).lean();
    const categories = ['All', 'Electronics', 'Footwear', 'Apparel', 'Accessories', 'Home'];

    console.log(`[CATALOG] Returned ${products.length} products from MongoDB.`);
    res.json({
      products,
      total: products.length,
      categories
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/products/:id', async (req, res) => {
  try {
    const product = await Product.findOne({ id: req.params.id }).lean();
    if (!product) {
      return res.status(404).json({ error: 'Product not found.' });
    }
    res.json(product);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// -------------------------------------------------------------
// 4. CART & STOCK VALIDATION
// -------------------------------------------------------------
app.post('/api/cart/validate', async (req, res) => {
  try {
    const { items } = req.body;
    if (!Array.isArray(items)) {
      return res.status(400).json({ error: 'Items array is required.' });
    }

    const issues = [];
    for (const item of items) {
      const prod = await Product.findOne({ id: item.productId }).lean();
      if (!prod) {
        issues.push({ productId: item.productId, message: 'Item no longer exists in catalog.', available: 0 });
      } else if (item.quantity > prod.stock) {
        issues.push({
          productId: item.productId,
          title: prod.title,
          requested: item.quantity,
          available: prod.stock,
          message: `Only ${prod.stock} unit(s) available.`
        });
      }
    }

    res.json({
      valid: issues.length === 0,
      issues
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// -------------------------------------------------------------
// 5. COUPON VALIDATION
// -------------------------------------------------------------
app.post('/api/coupons/validate', async (req, res) => {
  try {
    const { code } = req.body;
    if (!code) {
      return res.status(400).json({ valid: false, message: 'Coupon code required.' });
    }

    const coupon = await Coupon.findOne({ code: code.toUpperCase().trim(), isActive: true }).lean();
    if (!coupon) {
      return res.status(404).json({
        valid: false,
        message: 'Invalid or expired promotional code.'
      });
    }

    res.json({
      valid: true,
      code: coupon.code,
      discountPercent: coupon.discountPercent,
      description: coupon.description,
      message: `Coupon applied: ${coupon.discountPercent}% discount!`
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// -------------------------------------------------------------
// 6. ORDERS
// -------------------------------------------------------------
app.post('/api/orders', async (req, res) => {
  try {
    const { items, shippingAddress, paymentDetails, simulationMode, couponCode } = req.body;

    if (!items || !items.length) {
      return res.status(400).json({ error: 'Cart is empty. Cannot place order.' });
    }

    if (!shippingAddress || !shippingAddress.fullName || !shippingAddress.street) {
      return res.status(400).json({ error: 'Complete delivery address is required.' });
    }

    // Simulation checks for assessment testing
    if (simulationMode === 'TIMEOUT') {
      console.log('[SIMULATION] Intentionally delaying response...');
      return setTimeout(() => {
        res.status(504).json({ error: 'GATEWAY_TIMEOUT', message: 'Payment gateway timed out.' });
      }, 12000);
    }

    if (simulationMode === 'CARD_DECLINED') {
      console.log('[SIMULATION] Simulating card decline error (HTTP 402)...');
      return res.status(402).json({
        error: 'PAYMENT_DECLINED',
        message: 'Card was declined: Insufficient funds or invalid CVV.'
      });
    }

    if (simulationMode === 'OUT_OF_STOCK') {
      console.log('[SIMULATION] Simulating out-of-stock race condition (HTTP 409)...');
      return res.status(409).json({
        error: 'OUT_OF_STOCK',
        message: 'One or more items in your cart became out of stock during checkout.'
      });
    }

    // Stock check against MongoDB
    for (const item of items) {
      const prod = await Product.findOne({ id: item.productId });
      if (!prod || prod.stock < item.quantity) {
        return res.status(409).json({
          error: 'OUT_OF_STOCK',
          message: `Item "${prod ? prod.title : item.productId}" does not have enough stock available.`
        });
      }
    }

    // Calculate totals
    let subtotal = 0;
    for (const item of items) {
      subtotal += item.price * item.quantity;
    }

    let discount = 0;
    if (couponCode) {
      const cp = await Coupon.findOne({ code: couponCode.toUpperCase().trim(), isActive: true });
      if (cp) {
        discount = Number(((subtotal * cp.discountPercent) / 100).toFixed(2));
      }
    }

    const discountedSubtotal = Math.max(0, subtotal - discount);
    const shipping = discountedSubtotal > 100 ? 0 : 9.99;
    const tax = Number((discountedSubtotal * 0.08).toFixed(2));
    const grandTotal = Number((discountedSubtotal + shipping + tax).toFixed(2));

    // Atomically deduct stock in MongoDB
    for (const item of items) {
      const updated = await Product.findOneAndUpdate(
        { id: item.productId, stock: { $gte: item.quantity } },
        { $inc: { stock: -item.quantity } },
        { new: true }
      );
      if (updated) {
        console.log(`[INVENTORY] Deducted ${item.quantity} units of "${updated.title}" in MongoDB. Remaining: ${updated.stock}`);
      }
    }

    // Create and save Order in MongoDB
    const orderId = `ORD-${Date.now().toString().slice(-6)}`;
    const newOrder = await Order.create({
      id: orderId,
      createdAt: new Date().toISOString(),
      status: 'CONFIRMED',
      items,
      shippingAddress,
      paymentMethod: paymentDetails?.method || 'Credit Card (Sandbox)',
      subtotal: Number(subtotal.toFixed(2)),
      discount,
      shipping,
      tax,
      grandTotal,
      couponCode: couponCode || null,
      estimatedDelivery: new Date(Date.now() + 4 * 24 * 60 * 60 * 1000).toDateString()
    });

    console.log(`[ORDER] Created Order #${orderId} for $${grandTotal} in MongoDB. Confirmed.`);
    return res.status(201).json({
      success: true,
      message: 'Order placed and confirmed successfully.',
      order: newOrder
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/orders', async (req, res) => {
  try {
    const orders = await Order.find({}).sort({ createdAt: -1 }).lean();
    res.json({
      orders,
      total: orders.length
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/orders/:id', async (req, res) => {
  try {
    const order = await Order.findOne({ id: req.params.id }).lean();
    if (!order) {
      return res.status(404).json({ error: 'Order not found.' });
    }

    const ageSeconds = (Date.now() - new Date(order.createdAt).getTime()) / 1000;
    let dynamicStatus = order.status;
    if (order.status !== 'FAILED') {
      if (ageSeconds > 60) {
        dynamicStatus = 'SHIPPED';
      } else if (ageSeconds > 25) {
        dynamicStatus = 'PROCESSING';
      } else {
        dynamicStatus = 'CONFIRMED';
      }
    }

    res.json({
      ...order,
      status: dynamicStatus
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Start listening on 0.0.0.0
app.listen(PORT, '0.0.0.0', () => {
  console.log(`=======================================================`);
  console.log(`🚀 Algions E-Commerce Backend Server running on port ${PORT}`);
  console.log(`   - Storage Engine: MongoDB (Mongoose)`);
  console.log(`   - Secure Key Enabled: ${SECURE_API_KEY}`);
  console.log(`   - Local:    http://localhost:${PORT}/api/products`);
  console.log(`   - Android:  http://10.0.2.2:${PORT}/api/products`);
  console.log(`=======================================================`);
});
