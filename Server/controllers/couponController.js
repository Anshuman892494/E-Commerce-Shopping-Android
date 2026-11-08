const Coupon = require('../models/Coupon');

/**
 * POST /api/coupons/validate
 */
async function validateCoupon(req, res) {
  try {
    const { code } = req.body;
    if (!code) {
      return res.status(400).json({ valid: false, message: 'Coupon code required.' });
    }

    const coupon = await Coupon.findOne({
      code: code.toUpperCase().trim(),
      isActive: true
    }).lean();

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
}

module.exports = {
  validateCoupon
};
