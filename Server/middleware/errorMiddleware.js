/**
 * Global centralized error handling middleware.
 */
function errorHandler(err, req, res, next) {
  console.error(`[ERROR] ${req.method} ${req.originalUrl} -`, err.message);
  const statusCode = res.statusCode === 200 ? 500 : res.statusCode;
  res.status(statusCode).json({
    error: 'SERVER_ERROR',
    message: err.message || 'An unexpected internal server error occurred.'
  });
}

/**
 * 404 Route Not Found handler.
 */
function notFoundHandler(req, res) {
  res.status(404).json({
    error: 'NOT_FOUND',
    message: `Endpoint ${req.method} ${req.originalUrl} not found.`
  });
}

module.exports = {
  errorHandler,
  notFoundHandler
};

