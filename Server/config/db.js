const mongoose = require('mongoose');
const initialProducts = require('../data/products');
const Product = require('../models/Product');
const User = require('../models/User');
const Coupon = require('../models/Coupon');

const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://127.0.0.1:27017/ecommerce_db';

/**
 * Seeds default data into MongoDB if collections are empty on first startup.
 */
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

/**
 * Connects to MongoDB database with auto-reconnect support.
 */
async function connectDB() {
  try {
    await mongoose.connect(MONGODB_URI);
    console.log(`=======================================================`);
    console.log(`🍃 Connected to MongoDB: ${MONGODB_URI}`);
    console.log(`=======================================================`);
    await seedDatabaseIfEmpty();
  } catch (err) {
    console.error('❌ Failed to connect to MongoDB:', err.message);
  }
}

module.exports = connectDB;

