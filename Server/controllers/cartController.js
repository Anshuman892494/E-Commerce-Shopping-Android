const Product = require('../models/Product');

/**
 * POST /api/cart/validate
 * Validates stock for an array of cart items against MongoDB inventory.
 */
async function validateCart(req, res) {
  try {
    const { items } = req.body;
    if (!Array.isArray(items)) {
      return res.status(400).json({ error: 'Items array is required.' });
    }

    const issues = [];
    for (const item of items) {
      const prod = await Product.findOne({ id: item.productId }).lean();
      if (!prod) {
        issues.push({
          productId: item.productId,
          message: 'Item no longer exists in catalog.',
          available: 0
        });
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
}

module.exports = {
  validateCart
};

