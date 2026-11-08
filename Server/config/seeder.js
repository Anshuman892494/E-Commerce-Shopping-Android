const Product = require('../models/Product');
const User = require('../models/User');
const Coupon = require('../models/Coupon');
const initialProducts = require('../data/products');

const DEMO_USER = {
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
};

const DEFAULT_COUPONS = [
  { code: 'ALGIONS20', discountPercent: 20, description: '20% off entire order for Algions candidates' },
  { code: 'FREESHIP', discountPercent: 10, description: '10% shipping courtesy discount' },
  { code: 'SUPER50', discountPercent: 50, description: '50% flash celebration discount' }
];

/**
 * Seeds default catalog, demo account, and coupons if collections are empty.
 */
async function seedDatabaseIfEmpty() {
  try {
    if (await Product.countDocuments() === 0) {
      await Product.insertMany(initialProducts);
      console.log(`🌱 Seeded ${initialProducts.length} products into MongoDB.`);
    }

    if (await User.countDocuments() === 0) {
      await User.create(DEMO_USER);
      console.log('🌱 Seeded demo user account (demo@algions.com).');
    }

    if (await Coupon.countDocuments() === 0) {
      await Coupon.insertMany(DEFAULT_COUPONS);
      console.log('🌱 Seeded promotional coupons.');
    }
  } catch (err) {
    console.error('❌ Database Seeding Error:', err.message);
  }
}

module.exports = seedDatabaseIfEmpty;
