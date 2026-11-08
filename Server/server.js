const express = require('express');
const cors = require('cors');
const initialProducts = require('./data/products');

const app = express();
const PORT = process.env.PORT || 5000;
const SECURE_API_KEY = process.env.API_KEY || 'algions-secure-api-key-2026';

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

// In-memory application state
let products = JSON.parse(JSON.stringify(initialProducts));
let orders = [];
let users = [
  {
    id: "user-1",
    name: "Algions Demo Student",
    email: "demo@algions.com",
    password: "Password123!",
    token: "mock-jwt-token-algions-2026-auth-session"
  }
];

function getBearerToken(req) {
  const authHeader = req.headers['authorization'];
  if (!authHeader) return null;
  const parts = authHeader.split(' ');
  return parts.length === 2 && parts[0] === 'Bearer' ? parts[1] : null;
}

// -------------------------------------------------------------
// 1. HEALTH CHECK & RESET
// -------------------------------------------------------------
app.get('/api/health', (req, res) => {
  res.json({
    status: 'healthy',
    timestamp: new Date().toISOString(),
    service: 'Algions E-Commerce Live Backend',
    productsCount: products.length,
    ordersCount: orders.length
  });
});

app.post('/api/reset', (req, res) => {
  products = JSON.parse(JSON.stringify(initialProducts));
  orders = [];
  res.json({ message: 'Catalog inventory and orders reset to initial seed state.' });
});

// -------------------------------------------------------------
// 2. AUTHENTICATION
// -------------------------------------------------------------
app.post('/api/auth/login', (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ error: 'Email and password are required.' });
  }

  const user = users.find(u => u.email.toLowerCase() === email.toLowerCase());
  if (!user || user.password !== password) {
    return res.status(401).json({ error: 'Invalid email or password.' });
  }

  console.log(`[AUTH] User "${user.email}" successfully logged in.`);
  res.json({
    token: user.token,
    user: { id: user.id, name: user.name, email: user.email }
  });
});

app.post('/api/auth/register', (req, res) => {
  const { name, email, password } = req.body;
  if (!name || !email || !password) {
    return res.status(400).json({ error: 'Name, email, and password are required.' });
  }

  const existing = users.find(u => u.email.toLowerCase() === email.toLowerCase());
  if (existing) {
    return res.status(400).json({ error: 'Account with this email already exists.' });
  }

  const newUser = {
    id: `user-${Date.now()}`,
    name,
    email,
    password,
    token: `token-${Date.now()}-${Math.random().toString(36).substring(2)}`
  };
  users.push(newUser);

  console.log(`[AUTH] Registered new user "${newUser.email}".`);
  res.status(201).json({
    token: newUser.token,
    user: { id: newUser.id, name: newUser.name, email: newUser.email }
  });
});

app.get('/api/auth/me', (req, res) => {
  const token = getBearerToken(req);
  if (!token) return res.status(401).json({ error: 'Missing authorization token.' });

  const user = users.find(u => u.token === token);
  if (!user) return res.status(401).json({ error: 'Session expired or invalid token.' });

  res.json({ user: { id: user.id, name: user.name, email: user.email } });
});

// -------------------------------------------------------------
// 3. PRODUCT CATALOG
// -------------------------------------------------------------
app.get('/api/products', (req, res) => {
  let result = [...products];
  const { category, search, sort } = req.query;

  if (category && category !== 'All') {
    result = result.filter(p => p.category.toLowerCase() === category.toLowerCase());
  }

  if (search && search.trim()) {
    const q = search.trim().toLowerCase();
    result = result.filter(p =>
      p.title.toLowerCase().includes(q) ||
      p.description.toLowerCase().includes(q) ||
      p.category.toLowerCase().includes(q)
    );
  }

  if (sort === 'price_asc') {
    result.sort((a, b) => a.price - b.price);
  } else if (sort === 'price_desc') {
    result.sort((a, b) => b.price - a.price);
  } else if (sort === 'rating') {
    result.sort((a, b) => b.rating - a.rating);
  }

  const categories = ['All', 'Electronics', 'Footwear', 'Apparel', 'Accessories', 'Home'];

  console.log(`[CATALOG] Returned ${result.length} products to client.`);
  res.json({
    products: result,
    total: result.length,
    categories
  });
});

app.get('/api/products/:id', (req, res) => {
  const product = products.find(p => p.id === req.params.id);
  if (!product) {
    return res.status(404).json({ error: 'Product not found.' });
  }
  res.json(product);
});

// -------------------------------------------------------------
// 4. CART & STOCK VALIDATION
// -------------------------------------------------------------
app.post('/api/cart/validate', (req, res) => {
  const { items } = req.body;
  if (!Array.isArray(items)) {
    return res.status(400).json({ error: 'Items array is required.' });
  }

  const issues = [];
  for (const item of items) {
    const prod = products.find(p => p.id === item.productId);
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
});

// -------------------------------------------------------------
// 5. ORDERS
// -------------------------------------------------------------
app.post('/api/orders', (req, res) => {
  const { items, shippingAddress, paymentDetails, simulationMode } = req.body;

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

  // Stock check
  for (const item of items) {
    const prod = products.find(p => p.id === item.productId);
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
  const shipping = subtotal > 100 ? 0 : 9.99;
  const tax = Number((subtotal * 0.08).toFixed(2));
  const grandTotal = Number((subtotal + shipping + tax).toFixed(2));

  // Deduct stock on server
  for (const item of items) {
    const prod = products.find(p => p.id === item.productId);
    if (prod) {
      prod.stock = Math.max(0, prod.stock - item.quantity);
      console.log(`[INVENTORY] Deducted ${item.quantity} units of "${prod.title}". New stock: ${prod.stock}`);
    }
  }

  // Create order
  const orderId = `ORD-${Date.now().toString().slice(-6)}`;
  const newOrder = {
    id: orderId,
    createdAt: new Date().toISOString(),
    status: 'CONFIRMED',
    items,
    shippingAddress,
    paymentMethod: paymentDetails?.method || 'Credit Card (Sandbox)',
    subtotal,
    shipping,
    tax,
    grandTotal,
    estimatedDelivery: new Date(Date.now() + 4 * 24 * 60 * 60 * 1000).toDateString()
  };

  orders.unshift(newOrder);

  console.log(`[ORDER] Created Order #${orderId} for $${grandTotal}. Server Confirmed.`);
  return res.status(201).json({
    success: true,
    message: 'Order placed and confirmed successfully.',
    order: newOrder
  });
});

app.get('/api/orders', (req, res) => {
  res.json({
    orders,
    total: orders.length
  });
});

app.get('/api/orders/:id', (req, res) => {
  const order = orders.find(o => o.id === req.params.id);
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
});

// Start listening on 0.0.0.0
app.listen(PORT, '0.0.0.0', () => {
  console.log(`=======================================================`);
  console.log(`🚀 Algions E-Commerce Backend Server running on port ${PORT}`);
  console.log(`   - Secure Key Enabled: ${SECURE_API_KEY}`);
  console.log(`   - Local:    http://localhost:${PORT}/api/products`);
  console.log(`   - Android:  http://10.0.2.2:${PORT}/api/products`);
  console.log(`=======================================================`);
});
