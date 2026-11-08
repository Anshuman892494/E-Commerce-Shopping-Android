const mongoose = require('mongoose');

const orderSchema = new mongoose.Schema({
  id: { type: String, required: true, unique: true, index: true },
  userId: { type: String, index: true },
  createdAt: { type: String, required: true },
  status: { type: String, default: 'CONFIRMED' },
  items: [
    {
      productId: { type: String, required: true },
      title: { type: String, required: true },
      price: { type: Number, required: true },
      quantity: { type: Number, required: true },
      selectedColor: { type: String },
      selectedSize: { type: String },
      imageUrl: { type: String }
    }
  ],
  shippingAddress: {
    fullName: String,
    street: String,
    city: String,
    state: String,
    postalCode: String,
    phone: String
  },
  paymentMethod: { type: String, default: 'Credit Card (Sandbox)' },
  subtotal: { type: Number, required: true },
  discount: { type: Number, default: 0 },
  shipping: { type: Number, default: 0 },
  tax: { type: Number, default: 0 },
  grandTotal: { type: Number, required: true },
  couponCode: { type: String },
  estimatedDelivery: { type: String }
}, { timestamps: true });

module.exports = mongoose.model('Order', orderSchema);

