const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const connectDB = require('./config/db');

// Middlewares
const { validateApiKey, SECURE_API_KEY } = require('./middleware/authMiddleware');
const { errorHandler, notFoundHandler } = require('./middleware/errorMiddleware');

// Route Modules
const authRoutes = require('./routes/authRoutes');
const productRoutes = require('./routes/productRoutes');
const cartRoutes = require('./routes/cartRoutes');
const orderRoutes = require('./routes/orderRoutes');
const couponRoutes = require('./routes/couponRoutes');

// Models (for health check & admin reset)
const Product = require('./models/Product');
const Order = require('./models/Order');
const initialProducts = require('./data/products');

const app = express();
const PORT = process.env.PORT || 5000;

// Connect to MongoDB
connectDB();

// Global Middlewares
app.use(cors());
app.use(express.json());

// Request logger
app.use((req, res, next) => {
  console.log(`[HTTP] ${req.method} ${req.originalUrl}`);
  next();
});

// Secure API Key Validation
app.use(validateApiKey);

// -------------------------------------------------------------
// Health Check & Reset Endpoints
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
// Mount Modular Routes
// -------------------------------------------------------------
app.use('/api/auth', authRoutes);
app.use('/api/products', productRoutes);
app.use('/api/cart', cartRoutes);
app.use('/api/orders', orderRoutes);
app.use('/api/coupons', couponRoutes);

// Error Handling Middlewares
app.use(notFoundHandler);
app.use(errorHandler);

// Start listening on 0.0.0.0
app.listen(PORT, '0.0.0.0', () => {
  console.log(`=======================================================`);
  console.log(`🚀 Algions E-Commerce Backend Server running on port ${PORT}`);
  console.log(`   - Architecture: Modular Express (Config/Routes/Controllers/Models)`);
  console.log(`   - Storage Engine: MongoDB (Mongoose ODM)`);
  console.log(`   - Secure Key: ${SECURE_API_KEY}`);
  console.log(`   - Local:    http://localhost:${PORT}/api/products`);
  console.log(`   - Android:  http://10.0.2.2:${PORT}/api/products`);
  console.log(`=======================================================`);
});
