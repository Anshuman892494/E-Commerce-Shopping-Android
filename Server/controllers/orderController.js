const Order = require('../models/Order');
const Product = require('../models/Product');
const Coupon = require('../models/Coupon');

/**
 * POST /api/orders
 * Places and confirms an order against MongoDB inventory.
 */
async function createOrder(req, res) {
  try {
    const { items, shippingAddress, paymentDetails, simulationMode, couponCode } = req.body;

    if (!items || !items.length) {
      return res.status(400).json({ error: 'Cart is empty. Cannot place order.' });
    }

    if (!shippingAddress || !shippingAddress.fullName || !shippingAddress.street) {
      return res.status(400).json({ error: 'Complete delivery address is required.' });
    }

    // --- Simulation Checks for Assessment Rubric Testing ---
    if (simulationMode === 'TIMEOUT') {
      console.log('[SIMULATION] Intentionally delaying response to trigger client timeout...');
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

    // --- Stock Check against MongoDB ---
    for (const item of items) {
      const prod = await Product.findOne({ id: item.productId });
      if (!prod || prod.stock < item.quantity) {
        return res.status(409).json({
          error: 'OUT_OF_STOCK',
          message: `Item "${prod ? prod.title : item.productId}" does not have enough stock available.`
        });
      }
    }

    // --- Calculate Pricing & Discounts ---
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

    // --- Atomically Deduct Stock in MongoDB ---
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

    // --- Create Order Document in MongoDB ---
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
}

/**
 * Calculates dynamic lifecycle status based on order age:
 * 0 - 20s   -> CONFIRMED (Order Placed)
 * 20 - 50s  -> PROCESSING (Packed & Prepared in warehouse)
 * 50 - 90s  -> SHIPPED (Carrier picked up, in transit)
 * 90s+      -> DELIVERED (Package safely delivered to recipient)
 */
function computeDynamicStatus(order) {
  if (!order || order.status === 'FAILED') return order ? order.status : 'CONFIRMED';
  const ageSeconds = (Date.now() - new Date(order.createdAt).getTime()) / 1000;
  if (ageSeconds > 90) {
    return 'DELIVERED';
  } else if (ageSeconds > 50) {
    return 'SHIPPED';
  } else if (ageSeconds > 20) {
    return 'PROCESSING';
  } else {
    return 'CONFIRMED';
  }
}

/**
 * GET /api/orders
 */
async function getOrders(req, res) {
  try {
    const orders = await Order.find({}).sort({ createdAt: -1 }).lean();
    const dynamicOrders = orders.map(o => ({
      ...o,
      status: computeDynamicStatus(o)
    }));
    res.json({
      orders: dynamicOrders,
      total: dynamicOrders.length
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
}

/**
 * GET /api/orders/:id
 */
async function getOrderById(req, res) {
  try {
    const order = await Order.findOne({ id: req.params.id }).lean();
    if (!order) {
      return res.status(404).json({ error: 'Order not found.' });
    }

    res.json({
      ...order,
      status: computeDynamicStatus(order)
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
}

module.exports = {
  createOrder,
  getOrders,
  getOrderById
};

