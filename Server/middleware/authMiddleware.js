const User = require('../models/User');

const SECURE_API_KEY = process.env.API_KEY || 'algions-secure-api-key-2026';

/**
 * Validates the x-api-key header on protected endpoints.
 */
function validateApiKey(req, res, next) {
  // Allow health check without API key
  if (req.path === '/api/health') return next();

  const apiKey = req.headers['x-api-key'];
  if (!apiKey || apiKey !== SECURE_API_KEY) {
    console.warn(`[AUTH REJECTED] Missing or invalid x-api-key from ${req.ip}`);
    return res.status(401).json({
      error: 'UNAUTHORIZED',
      message: 'Invalid or missing secure x-api-key header.'
    });
  }
  next();
}

/**
 * Extracts and verifies Bearer token from Authorization header.
 */
async function verifyAuthToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  if (!authHeader) {
    return res.status(401).json({ error: 'Missing authorization token.' });
  }

  const parts = authHeader.split(' ');
  const token = parts.length === 2 && parts[0] === 'Bearer' ? parts[1] : null;

  if (!token) {
    return res.status(401).json({ error: 'Malformed authorization header. Expected Bearer <token>.' });
  }

  try {
    const user = await User.findOne({ token });
    if (!user) {
      return res.status(401).json({ error: 'Session expired or invalid token.' });
    }
    req.user = user;
    next();
  } catch (err) {
    res.status(500).json({ error: 'Token verification failed: ' + err.message });
  }
}

module.exports = {
  validateApiKey,
  verifyAuthToken,
  SECURE_API_KEY
};
