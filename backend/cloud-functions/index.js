/**
 * AL MOKHTAR RESTAURANT - RIYADH, SAUDI ARABIA
 * Central Server-Side Backend & Cloud Functions
 * Branches: Al Mansourah & Al Aziziyah
 */

const functions = require('firebase-functions');
const admin = require('firebase-admin');
const crypto = require('crypto');

admin.initializeApp();
const db = admin.firestore();

// Centrally stored secret for HMAC-SHA256 Table QR validation
const SERVER_QR_SECRET = process.env.QR_HMAC_SECRET || "AlMokhtar_Riyadh_Secured_QR_Token_2026_x89";

// Allowed branches configuration
const BRANCHES = {
  "mansourah": {
    nameAr: "فرع المنصورة",
    nameEn: "Al Mansourah Branch",
    address: "3247 Al Kharj Rd, Al Mansourah, Riyadh 12692, Saudi Arabia",
    tableCount: 20,
    code: "MN"
  },
  "aziziyah": {
    nameAr: "فرع العزيزية",
    nameEn: "Al Aziziyah Branch",
    address: "Al Kharj Rd, Al Aziziyah, Riyadh 14513, Saudi Arabia",
    tableCount: 25,
    code: "AZ"
  }
};

/**
 * Validates HMAC signature for a branch and table to prevent table forgery
 */
function verifyTableSignature(branchId, tableNumber, signature) {
  if (!signature) return false;
  const hmac = crypto.createHmac('sha256', SERVER_QR_SECRET);
  hmac.update(`${branchId}:${tableNumber}`);
  const expected = hmac.digest('hex').substring(0, 12);
  return expected.toLowerCase() === signature.toLowerCase();
}

/**
 * HTTPS Callable / REST API: createOrder
 * Recalculates prices against database to prevent client price tampering.
 */
exports.createOrder = functions.https.onRequest(async (req, res) => {
  // CORS configuration
  res.set('Access-Control-Allow-Origin', '*');
  res.set('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.set('Access-Control-Allow-Headers', 'Content-Type, Authorization, Idempotency-Key');

  if (req.method === 'OPTIONS') {
    return res.status(204).send('');
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ error: "Method not allowed. Use POST." });
  }

  try {
    const { branchId, tableNumber, signature, items, customerName, customerPhone, customerNotes, allergyNotes } = req.body;
    const idempotencyKey = req.headers['idempotency-key'] || req.body.idempotencyKey;

    if (!branchId || !BRANCHES[branchId]) {
      return res.status(400).json({ error: "Invalid branch identifier." });
    }

    const branch = BRANCHES[branchId];
    const tableNum = parseInt(tableNumber, 10);
    if (isNaN(tableNum) || tableNum < 1 || tableNum > branch.tableCount) {
      return res.status(400).json({ error: `Invalid table number for ${branch.nameEn}.` });
    }

    // Cryptographic Table validation
    if (!verifyTableSignature(branchId, tableNum, signature)) {
      return res.status(403).json({ error: "Invalid or tampered table QR signature. Please rescan table QR." });
    }

    if (!Array.isArray(items) || items.length === 0) {
      return res.status(400).json({ error: "Order items list cannot be empty." });
    }

    // Idempotency verification: prevent duplicate tap charges
    if (idempotencyKey) {
      const existingQuery = await db.collection('orders')
        .where('idempotencyKey', '==', idempotencyKey)
        .limit(1)
        .get();

      if (!existingQuery.empty) {
        const existingOrder = existingQuery.docs[0].data();
        return res.status(200).json({
          status: "ALREADY_PROCESSED",
          orderId: existingQuery.docs[0].id,
          order: existingOrder
        });
      }
    }

    // Server-side price recalculation: fetch authoritative catalog
    const menuSnapshot = await db.collection('menu_items').get();
    const catalogMap = new Map();
    menuSnapshot.forEach(doc => {
      catalogMap.set(doc.id, doc.data());
    });

    let calculatedSubtotal = 0;
    const validatedItems = [];

    for (const clientItem of items) {
      const masterItem = catalogMap.get(clientItem.itemId);
      if (!masterItem) {
        return res.status(400).json({ error: `Item not found in menu: ${clientItem.itemId}` });
      }

      if (masterItem.isAvailable === false) {
        return res.status(400).json({ error: `Item is currently sold out: ${masterItem.nameAr}` });
      }

      const qty = parseInt(clientItem.quantity, 10);
      if (isNaN(qty) || qty < 1 || qty > 50) {
        return res.status(400).json({ error: `Invalid quantity for ${masterItem.nameEn}` });
      }

      const unitPrice = parseFloat(masterItem.priceSar);
      const lineTotal = unitPrice * qty;
      calculatedSubtotal += lineTotal;

      validatedItems.push({
        itemId: clientItem.itemId,
        nameAr: masterItem.nameAr,
        nameEn: masterItem.nameEn,
        quantity: qty,
        unitPriceSar: unitPrice,
        lineTotalSar: Math.round(lineTotal * 100) / 100,
        spiceOption: clientItem.spiceOption === 'spicy' ? 'spicy' : 'normal',
        itemNote: (clientItem.itemNote || '').toString().trim().substring(0, 150)
      });
    }

    // KSA ZATCA 15% VAT calculation (VAT included in standard prices)
    const vatSar = calculatedSubtotal - (calculatedSubtotal / 1.15);
    const finalTotal = calculatedSubtotal;

    // Generate unique order number (e.g. MKT-MN-261016-104)
    const dateStr = new Date().toISOString().slice(2, 10).replace(/-/g, '');
    const randomSuffix = Math.floor(100 + Math.random() * 900);
    const orderNumber = `MKT-${branch.code}-${dateStr}-${randomSuffix}`;

    const orderDoc = {
      orderNumber,
      branchId,
      branchNameAr: branch.nameAr,
      branchNameEn: branch.nameEn,
      tableNumber: tableNum,
      items: validatedItems,
      subtotalSar: Math.round(calculatedSubtotal * 100) / 100,
      vatSar: Math.round(vatSar * 100) / 100,
      totalSar: Math.round(finalTotal * 100) / 100,
      customerName: (customerName || '').toString().trim().substring(0, 50),
      customerPhone: (customerPhone || '').toString().trim().substring(0, 20),
      customerNotes: (customerNotes || '').toString().trim().substring(0, 250),
      allergyNotes: (allergyNotes || '').toString().trim().substring(0, 250),
      status: "NEW",
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      createdAtMillis: Date.now(),
      idempotencyKey: idempotencyKey || null,
      printedToKitchen: false,
      vatRegistrationNumber: "300124892400003"
    };

    const docRef = await db.collection('orders').add(orderDoc);

    // Queue thermal print job for Windows print agent
    await db.collection('print_jobs').add({
      orderId: docRef.id,
      orderNumber,
      branchId,
      status: "QUEUED",
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });

    return res.status(201).json({
      success: true,
      orderId: docRef.id,
      orderNumber,
      totalSar: orderDoc.totalSar,
      status: "NEW"
    });

  } catch (error) {
    console.error("Error creating order:", error);
    return res.status(500).json({ error: "Internal server error occurred while processing order." });
  }
});
