const mongoose = require('mongoose');
const seedDatabaseIfEmpty = require('./seeder');

const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://127.0.0.1:27017/ecommerce_db';

/**
 * Connects to MongoDB database with auto-seeding on first run.
 */
async function connectDB() {
  try {
    const conn = await mongoose.connect(MONGODB_URI);
    console.log(`🍃 Connected to MongoDB: ${conn.connection.host}/${conn.connection.name}`);
    await seedDatabaseIfEmpty();
  } catch (err) {
    console.error('❌ Failed to connect to MongoDB:', err.message);
  }
}

module.exports = connectDB;
