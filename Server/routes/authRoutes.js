const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');
const { verifyAuthToken } = require('../middleware/authMiddleware');

router.post('/login', authController.login);
router.post('/register', authController.register);
router.get('/me', verifyAuthToken, authController.getMe);
router.put('/profile', verifyAuthToken, authController.updateProfile);

module.exports = router;
