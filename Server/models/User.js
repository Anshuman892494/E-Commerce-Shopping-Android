const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  id: { type: String, required: true, unique: true, index: true },
  name: { type: String, required: true },
  email: { type: String, required: true, unique: true, lowercase: true, index: true },
  password: { type: String, required: true },
  token: { type: String, required: true },
  shippingAddress: {
    fullName: { type: String, default: 'Algions Demo Student' },
    street: { type: String, default: '452 Innovation Blvd, Suite 300' },
    city: { type: String, default: 'Bangalore' },
    state: { type: String, default: 'KA' },
    postalCode: { type: String, default: '560100' },
    phone: { type: String, default: '+91 98765 43210' }
  }
}, { timestamps: true });

module.exports = mongoose.model('User', userSchema);
