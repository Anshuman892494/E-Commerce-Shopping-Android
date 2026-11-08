const products = [
  {
    id: "prod-1",
    title: "Sony WH-1000XM5 Noise Cancelling Headphones",
    category: "Electronics",
    price: 349.99,
    originalPrice: 399.99,
    rating: 4.8,
    reviewCount: 1420,
    imageUrl: "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80",
    description: "Industry-leading noise cancellation with two processors and 8 microphones. Up to 30-hour battery life with quick charging. Crystal clear hands-free calling with 4 beamforming microphones.",
    variants: {
      colors: [
        { name: "Midnight Black", hex: "#111827" },
        { name: "Platinum Silver", hex: "#E2E8F0" },
        { name: "Navy Blue", hex: "#1E3A8A" }
      ],
      sizes: ["Standard"]
    },
    stock: 12
  },
  {
    id: "prod-2",
    title: "Nike Air Max 270 React Athletic Shoes",
    category: "Footwear",
    price: 159.99,
    originalPrice: 180.00,
    rating: 4.7,
    reviewCount: 890,
    imageUrl: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80",
    description: "The Nike Air Max 270 delivers unmatched, all-day comfort with a bold aesthetic. The Max Air 270 unit provides responsive cushioning underfoot, wrapped in a breathable mesh upper.",
    variants: {
      colors: [
        { name: "Crimson Red", hex: "#DC2626" },
        { name: "Triple Black", hex: "#18181B" },
        { name: "Pure White", hex: "#F8FAFC" }
      ],
      sizes: ["US 8", "US 9", "US 10", "US 11"]
    },
    stock: 2 // Low stock scenario (warning badge)
  },
  {
    id: "prod-3",
    title: "Minimalist Italian Leather Chronograph Watch",
    category: "Accessories",
    price: 199.50,
    originalPrice: 249.00,
    rating: 4.9,
    reviewCount: 654,
    imageUrl: "https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=800&q=80",
    description: "Handcrafted surgical-grade 316L stainless steel case paired with vegetable-tanned genuine Italian leather strap. Water resistant up to 50 meters with sapphire crystal glass.",
    variants: {
      colors: [
        { name: "Cognac Brown", hex: "#78350F" },
        { name: "Classic Black", hex: "#18181B" }
      ],
      sizes: ["40mm", "42mm"]
    },
    stock: 8
  },
  {
    id: "prod-4",
    title: "Apple MacBook Air 15-inch M3 Chip",
    category: "Electronics",
    price: 1299.00,
    originalPrice: 1399.00,
    rating: 4.9,
    reviewCount: 3120,
    imageUrl: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&q=80",
    description: "Strikingly thin design with expansive Liquid Retina display. Powered by the M3 chip for lightning-fast performance and up to 18 hours of battery life.",
    variants: {
      colors: [
        { name: "Space Gray", hex: "#4B5563" },
        { name: "Starlight", hex: "#FEF3C7" },
        { name: "Midnight", hex: "#1E293B" }
      ],
      sizes: ["256GB SSD", "512GB SSD", "1TB SSD"]
    },
    stock: 5
  },
  {
    id: "prod-5",
    title: "Heavyweight Cotton Oversized Streetwear Hoodie",
    category: "Apparel",
    price: 79.99,
    originalPrice: 95.00,
    rating: 4.6,
    reviewCount: 430,
    imageUrl: "https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=800&q=80",
    description: "Crafted from 480 GSM French Terry cotton. Drop-shoulder silhouette with double-layered hood and kangaroo pocket. Pre-shrunk for maximum longevity.",
    variants: {
      colors: [
        { name: "Washed Olive", hex: "#3F6212" },
        { name: "Oatmeal Heather", hex: "#D6D3D1" },
        { name: "Jet Black", hex: "#0F172A" }
      ],
      sizes: ["S", "M", "L", "XL"]
    },
    stock: 18
  },
  {
    id: "prod-6",
    title: "Matte Ceramic Pour-Over Coffee Dripper Set",
    category: "Home",
    price: 45.00,
    originalPrice: 55.00,
    rating: 4.8,
    reviewCount: 289,
    imageUrl: "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=800&q=80",
    description: "Artisan-crafted ceramic dripper engineered for optimal extraction speed and thermal stability. Includes 600ml borosilicate glass server and 40 organic paper filters.",
    variants: {
      colors: [
        { name: "Matte Charcoal", hex: "#334155" },
        { name: "Terracotta", hex: "#9A3412" }
      ],
      sizes: ["Single (1-2 Cups)", "Family (3-4 Cups)"]
    },
    stock: 10
  },
  {
    id: "prod-7",
    title: "Limited Edition Retro Polarized Sunglasses",
    category: "Accessories",
    price: 89.00,
    originalPrice: 110.00,
    rating: 4.5,
    reviewCount: 198,
    imageUrl: "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=800&q=80",
    description: "Handcrafted acetate frame with 100% UV400 polarized scratch-resistant lenses. Classic vintage silhouette inspired by 70s motorsport heritage.",
    variants: {
      colors: [
        { name: "Tortoise Shell", hex: "#713F12" },
        { name: "Glossy Black", hex: "#111827" }
      ],
      sizes: ["Standard"]
    },
    stock: 0 // Out of Stock scenario (red badge, cannot add to cart)
  },
  {
    id: "prod-8",
    title: "Ergonomic Memory Foam Travel & Desk Pillow",
    category: "Home",
    price: 39.99,
    originalPrice: 49.99,
    rating: 4.7,
    reviewCount: 512,
    imageUrl: "https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?w=800&q=80",
    description: "Contoured orthopaedic memory foam with breathable eucalyptus silk cover. Provides therapeutic cervical spine support for long work sessions or travel.",
    variants: {
      colors: [
        { name: "Cloud Grey", hex: "#94A3B8" },
        { name: "Navy Blue", hex: "#1E3A8A" }
      ],
      sizes: ["Standard"]
    },
    stock: 6
  }
];

module.exports = products;

