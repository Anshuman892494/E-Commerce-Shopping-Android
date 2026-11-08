const Product = require('../models/Product');

/**
 * GET /api/products
 * Query Params: category, search, sort
 */
async function getProducts(req, res) {
  try {
    const { category, search, sort } = req.query;
    const filter = {};

    if (category && category !== 'All') {
      filter.category = new RegExp(`^${category}$`, 'i');
    }

    if (search && search.trim()) {
      const q = search.trim();
      filter.$or = [
        { title: { $regex: q, $options: 'i' } },
        { description: { $regex: q, $options: 'i' } },
        { category: { $regex: q, $options: 'i' } }
      ];
    }

    let sortOption = {};
    if (sort === 'price_asc') {
      sortOption = { price: 1 };
    } else if (sort === 'price_desc') {
      sortOption = { price: -1 };
    } else if (sort === 'rating') {
      sortOption = { rating: -1 };
    }

    const products = await Product.find(filter).sort(sortOption).lean();
    const dbCategories = await Product.distinct('category');
    const categories = ['All', ...dbCategories];

    console.log(`[CATALOG] Returned ${products.length} products from MongoDB.`);
    res.json({
      products,
      total: products.length,
      categories
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
}

/**
 * GET /api/products/:id
 */
async function getProductById(req, res) {
  try {
    const product = await Product.findOne({ id: req.params.id }).lean();
    if (!product) {
      return res.status(404).json({ error: 'Product not found.' });
    }
    res.json(product);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
}

module.exports = {
  getProducts,
  getProductById
};

