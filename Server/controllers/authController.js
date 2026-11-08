const crypto = require('crypto');
const User = require('../models/User');

/**
 * GET /api/auth/demo-account
 * Fetches the pre-seeded demo account credentials and default address directly from MongoDB.
 */
async function getDemoAccount(req, res) {
  try {
    const demoUser = await User.findOne({ email: 'demo@algions.com' }).lean();
    if (!demoUser) {
      return res.status(404).json({ error: 'Demo account not found in database.' });
    }
    console.log(`[AUTH] Fetched demo account "${demoUser.email}" from MongoDB.`);
    res.json({
      email: demoUser.email,
      password: demoUser.password,
      name: demoUser.name,
      shippingAddress: demoUser.shippingAddress
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
}

/**
 * POST /api/auth/login
 */
async function login(req, res) {
  try {
    const { email, password } = req.body;
    if (!email || !password) {
      return res.status(400).json({ error: 'Email and password are required.' });
    }

    const user = await User.findOne({ email: email.toLowerCase() });
    if (!user || user.password !== password) {
      return res.status(401).json({ error: 'Invalid email or password.' });
    }

    // Refresh token with fresh cryptographically secure random token
    user.token = crypto.randomBytes(32).toString('hex');
    await user.save();

    console.log(`[AUTH] User "${user.email}" successfully logged in (MongoDB verified).`);
    res.json({
      token: user.token,
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        shippingAddress: user.shippingAddress
      }
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
}

/**
 * POST /api/auth/register
 */
async function register(req, res) {
  try {
    const { name, email, password } = req.body;
    if (!name || !email || !password) {
      return res.status(400).json({ error: 'Name, email, and password are required.' });
    }

    const existing = await User.findOne({ email: email.toLowerCase() });
    if (existing) {
      return res.status(400).json({ error: 'Account with this email already exists.' });
    }

    const newUser = await User.create({
      id: `user-${Date.now()}`,
      name,
      email: email.toLowerCase(),
      password,
      token: crypto.randomBytes(32).toString('hex'),
      shippingAddress: {
        fullName: name,
        street: '123 Tech Park',
        city: 'Bangalore',
        state: 'KA',
        postalCode: '560001',
        phone: '+91 90000 00000'
      }
    });

    console.log(`[AUTH] Registered new user "${newUser.email}" in MongoDB.`);
    res.status(201).json({
      token: newUser.token,
      user: {
        id: newUser.id,
        name: newUser.name,
        email: newUser.email,
        shippingAddress: newUser.shippingAddress
      }
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
}

/**
 * GET /api/auth/me
 */
async function getMe(req, res) {
  res.json({
    user: {
      id: req.user.id,
      name: req.user.name,
      email: req.user.email,
      shippingAddress: req.user.shippingAddress
    }
  });
}

/**
 * PUT /api/auth/profile
 */
async function updateProfile(req, res) {
  try {
    const { name, shippingAddress } = req.body;
    if (name) req.user.name = name;
    if (shippingAddress) req.user.shippingAddress = shippingAddress;

    await req.user.save();
    console.log(`[PROFILE] Updated profile for user "${req.user.email}".`);
    res.json({
      user: {
        id: req.user.id,
        name: req.user.name,
        email: req.user.email,
        shippingAddress: req.user.shippingAddress
      }
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
}

module.exports = {
  getDemoAccount,
  login,
  register,
  getMe,
  updateProfile
};
