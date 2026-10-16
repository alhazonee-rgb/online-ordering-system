/**
 * AL MOKHTAR RESTAURANT - RIYADH, SAUDI ARABIA
 * Client-side Web Ordering Application Logic
 */

let currentLang = 'ar';
let currentBranchId = 'mansourah';
let currentTableNum = 4;
let currentSig = '';
let currentCategory = 'broast';
let cart = [];
let modalItem = null;
let modalQty = 1;
let modalSpice = 'normal';

const BRANCHES = {
  mansourah: {
    nameAr: "فرع المنصورة - طريق الخرج",
    nameEn: "Al Mansourah Branch - Al Kharj Rd",
    address: "3247 Al Kharj Rd, Al Mansourah, Riyadh"
  },
  aziziyah: {
    nameAr: "فرع العزيزية - طريق الخرج",
    nameEn: "Al Aziziyah Branch - Al Kharj Rd",
    address: "Al Kharj Rd, Al Aziziyah, Riyadh"
  }
};

const CATEGORIES = [
  { id: 'broast', nameAr: 'البروستد المقرمش', nameEn: 'Crispy Broast' },
  { id: 'msahhab', nameAr: 'مسحب الدجاج', nameEn: 'Boneless Msahhab' },
  { id: 'fish_shrimp', nameAr: 'سمك وجمبري', nameEn: 'Fish & Shrimp' },
  { id: 'sandwiches', nameAr: 'السندويشات', nameEn: 'Sandwiches' },
  { id: 'sides', nameAr: 'المقبلات والصلصات', nameEn: 'Sides & Dips' },
  { id: 'drinks', nameAr: 'المشروبات', nameEn: 'Beverages' }
];

const MENU_ITEMS = [
  {
    id: "broast_normal_4pc",
    categoryId: "broast",
    nameAr: "بروستد عادي (٤ قطع)",
    nameEn: "Broast Chicken Meal - Regular (4 pcs)",
    descAr: "٤ قطع دجاج مقلي ذهبي مقرمش مع بطاطس مقلية، خبز طازج، وصلصة ثوم شهية",
    descEn: "4 pcs crispy golden fried chicken with seasoned french fries, bun, and signature garlic dip",
    priceSar: 27.0,
    isSpicy: false,
    calories: "810 cal",
    icon: "🍗"
  },
  {
    id: "broast_spicy_4pc",
    categoryId: "broast",
    nameAr: "بروستد حراق (٤ قطع)",
    nameEn: "Broast Chicken Meal - Spicy (4 pcs)",
    descAr: "٤ قطع دجاج مقرمش بالتتبيلة الحارة الخاصة مع بطاطس، خبز طازج، وصلصة ثوم",
    descEn: "4 pcs spicy marinated crispy chicken with fries, fresh bun, and garlic sauce",
    priceSar: 27.0,
    isSpicy: true,
    calories: "810 cal",
    icon: "🍗"
  },
  {
    id: "msahhab_normal_8pc",
    categoryId: "msahhab",
    nameAr: "مسحب دجاج عادي (٨ قطع)",
    nameEn: "Chicken Msahhab Meal - Regular (8 pcs)",
    descAr: "٨ قطع صدور دجاج فيليه مقرمشة بدون عظم، بطاطس مقلية، خبز، وصلصة ثوم المختار",
    descEn: "8 pcs boneless chicken breast fillets fried to perfection with fries, bun, and garlic sauce",
    priceSar: 27.0,
    isSpicy: false,
    calories: "850 cal",
    icon: "🥢"
  },
  {
    id: "msahhab_spicy_8pc",
    categoryId: "msahhab",
    nameAr: "مسحب دجاج حراق (٨ قطع)",
    nameEn: "Chicken Msahhab Meal - Spicy (8 pcs)",
    descAr: "٨ قطع صدور دجاج فيليه حارة ومقرمشة، بطاطس مقلية، خبز، وصلصة ثوم",
    descEn: "8 pcs fiery spicy boneless chicken tenders, french fries, bun, and garlic dip",
    priceSar: 27.0,
    isSpicy: true,
    calories: "850 cal",
    icon: "🥢"
  },
  {
    id: "burger_chicken_normal",
    categoryId: "sandwiches",
    nameAr: "ساندوتش برجر دجاج عادي",
    nameEn: "Chicken Burger Sandwich - Regular",
    descAr: "قطعة دجاج مقرمشة فاخرة، سلطة ملفوف، مخلل، وصلصة المختار في خبز برجر سمسم",
    descEn: "Crispy chicken fillet patty, coleslaw, crunchy pickles, and special sauce in sesame bun",
    priceSar: 18.0,
    isSpicy: false,
    calories: "320 cal",
    icon: "🍔"
  },
  {
    id: "burger_chicken_spicy",
    categoryId: "sandwiches",
    nameAr: "ساندوتش برجر دجاج حراق",
    nameEn: "Chicken Burger Sandwich - Spicy",
    descAr: "قطعة دجاج حارة مقرمشة، صوص حراق، ملفوف، ومخلل بخبز السمسم الطازج",
    descEn: "Spicy fried chicken fillet, spicy dynamite spread, pickles, and coleslaw",
    priceSar: 18.0,
    isSpicy: true,
    calories: "330 cal",
    icon: "🍔"
  },
  {
    id: "jumbo_msahhab_sandwich",
    categoryId: "sandwiches",
    nameAr: "ساندوتش مسحب جامبو",
    nameEn: "Jumbo Msahhab Sandwich",
    descAr: "ساندوتش جامبو كبير محشو بقطع المسحب المقرمشة مع الثوم والمخلل اللذيذ",
    descEn: "Large sub loaded with crispy boneless msahhab chicken strips, garlic sauce, and pickles",
    priceSar: 23.0,
    isSpicy: false,
    calories: "520 cal",
    icon: "🥖"
  },
  {
    id: "msahhab_sandwich_single",
    categoryId: "sandwiches",
    nameAr: "ساندوتش مسحب عادي",
    nameEn: "Msahhab Sandwich",
    descAr: "ساندوتش مسحب كلاسيكي بصلصة الثوم والمخلل المقرمش",
    descEn: "Classic chicken strip roll with creamy garlic spread and pickles",
    priceSar: 12.0,
    isSpicy: false,
    calories: "290 cal",
    icon: "🥖"
  },
  {
    id: "chicken_roll_special",
    categoryId: "sandwiches",
    nameAr: "رول دجاج المختار المقرمش",
    nameEn: "Al Mokhtar Chicken Roll Wrap",
    descAr: "رول تورتيلا مقرمش محشو بالدجاج المتبل، صوص المختار الخاص، والبطاطس",
    descEn: "Toasted tortilla wrap with tender seasoned chicken, secret sauce, and potato strips",
    priceSar: 14.0,
    isSpicy: false,
    calories: "340 cal",
    icon: "🌯"
  },
  {
    id: "fish_fillet_meal",
    categoryId: "fish_shrimp",
    nameAr: "وجبة فيليه سمك عادي",
    nameEn: "Fish Fillet Meal - Regular",
    descAr: "قطعتان فيليه سمك مقرمش متبل، بطاطس مقلية، خبز طازج، وصلصة بحريات وتارتار",
    descEn: "2 pcs crispy battered tender fish fillets, fries, bun, and signature tartar sauce",
    priceSar: 30.0,
    isSpicy: false,
    calories: "750 cal",
    icon: "🐟"
  },
  {
    id: "crispy_jumbo_shrimp",
    categoryId: "fish_shrimp",
    nameAr: "وجبة جمبري مقرمش (٨ قطع)",
    nameEn: "Crispy Jumbo Shrimp Meal (8 pcs)",
    descAr: "٨ حبات روبيان جامبو مقرمشة بالتتبيلة البحرية، بطاطس، خبز، صلصة بحريات",
    descEn: "8 pcs golden crispy breaded jumbo shrimp, fries, bun, and seafood cocktail dip",
    priceSar: 40.0,
    isSpicy: false,
    calories: "680 cal",
    icon: "🍤"
  },
  {
    id: "hummus_fresh",
    categoryId: "sides",
    nameAr: "حمص المختار الطازج",
    nameEn: "Fresh Al Mokhtar Hummus",
    descAr: "حمص ناعم متبل بالطحينة الفاخرة وزيت الزيتون البكر",
    descEn: "Silky smooth authentic hummus dip whipped with premium sesame tahini and olive oil",
    priceSar: 7.0,
    isSpicy: false,
    calories: "210 cal",
    icon: "🥣"
  },
  {
    id: "coleslaw_salad",
    categoryId: "sides",
    nameAr: "سلطة ملفوف (كول سلو)",
    nameEn: "Fresh Coleslaw Salad",
    descAr: "ملفوف مقرمش مبشور طازج مع جزر وتتبيلة كريمية متوازنة",
    descEn: "Crisp shredded cabbage and carrots folded in rich creamy house dressing",
    priceSar: 6.0,
    isSpicy: false,
    calories: "140 cal",
    icon: "🥗"
  },
  {
    id: "french_fries_crispy",
    categoryId: "sides",
    nameAr: "بطاطس مقلية مقرمشة",
    nameEn: "Crispy French Fries",
    descAr: "أصابع بطاطس مقلية ذهبية ساخنة ومملحة",
    descEn: "Hot, golden, and crispy salted french potato fries",
    priceSar: 8.0,
    isSpicy: false,
    calories: "310 cal",
    icon: "🍟"
  },
  {
    id: "garlic_sauce_extra",
    categoryId: "sides",
    nameAr: "ثومية إضافية",
    nameEn: "Extra Garlic Dip",
    descAr: "صلصة الثوم الشهيرة الخاصة بمطعم المختار",
    descEn: "Al Mokhtar legendary whipped garlic emulsion dip",
    priceSar: 2.5,
    isSpicy: false,
    calories: "90 cal",
    icon: "🧄"
  },
  {
    id: "kinza_cola",
    categoryId: "drinks",
    nameAr: "مشروب كينزا كولا (٣٣٠ مل)",
    nameEn: "Kinza Cola (330ml)",
    descAr: "مشروب كينزا كولا سعودي مثلج ومنعش",
    descEn: "Chilled and effervescent Saudi Kinza Cola can",
    priceSar: 5.0,
    isSpicy: false,
    calories: "140 cal",
    icon: "🥤"
  },
  {
    id: "kinza_citrus",
    categoryId: "drinks",
    nameAr: "مشروب كينزا حمضيات",
    nameEn: "Kinza Citrus",
    descAr: "مشروب كينزا بنكهة الحمضيات المنعشة",
    descEn: "Sparkling zesty citrus soda",
    priceSar: 5.0,
    isSpicy: false,
    calories: "140 cal",
    icon: "🍋"
  },
  {
    id: "water_mineral",
    categoryId: "drinks",
    nameAr: "مياه شرب نقية",
    nameEn: "Pure Mineral Water",
    descAr: "مياه شرب معبأة نقية باردة",
    descEn: "Pure bottled drinking water",
    priceSar: 1.5,
    isSpicy: false,
    calories: "0 cal",
    icon: "💧"
  }
];

// Initialize on page load
document.addEventListener("DOMContentLoaded", () => {
  parseUrlParameters();
  renderCategories();
  renderMenuItems();
  updateLabels();
});

function parseUrlParameters() {
  const params = new URLSearchParams(window.location.search);
  const b = params.get('b') || params.get('branch');
  const t = params.get('t') || params.get('table');
  const sig = params.get('sig');

  if (b && BRANCHES[b]) {
    currentBranchId = b;
  }
  if (t) {
    const num = parseInt(t, 10);
    if (!isNaN(num) && num > 0) {
      currentTableNum = num;
    }
  }
  if (sig) {
    currentSig = sig;
  }

  updateBranchTableBadges();
}

function updateBranchTableBadges() {
  const branch = BRANCHES[currentBranchId] || BRANCHES.mansourah;
  document.getElementById('table-badge').innerText = (currentLang === 'ar') ? `طاولة #${currentTableNum}` : `Table #${currentTableNum}`;
  document.getElementById('branch-badge').innerText = `📍 ${(currentLang === 'ar') ? branch.nameAr : branch.nameEn}`;
}

function toggleLanguage() {
  currentLang = (currentLang === 'ar') ? 'en' : 'ar';
  document.documentElement.lang = currentLang;
  document.documentElement.dir = (currentLang === 'ar') ? 'rtl' : 'ltr';
  document.getElementById('lang-toggle-btn').innerText = (currentLang === 'ar') ? 'English' : 'عربي';

  updateLabels();
  updateBranchTableBadges();
  renderCategories();
  renderMenuItems();
  renderCartItems();
}

function updateLabels() {
  const isAr = currentLang === 'ar';
  document.getElementById('brand-title').innerText = isAr ? "مطعم المختار" : "Al Mokhtar Restaurant";
  document.getElementById('brand-subtitle').innerText = isAr ? "الرياض - بروستد ومسحب طازج" : "Riyadh - Fresh Broast & Msahhab";
  document.getElementById('allergy-title').innerText = isAr ? "تنبيه هام بشأن الحساسية الغذائية:" : "Important Food Allergy Notice:";
  document.getElementById('allergy-desc').innerText = isAr ? 
    "يرجى العلم بأن تسجيل الملاحظات لا يضمن خلو الوجبة تماماً من مسببات الحساسية لوجود أسطح تحضير ومقالي مشتركة." :
    "Please note special kitchen notes do not guarantee allergen-free preparation due to shared kitchen fryers.";
  document.getElementById('cart-bar-label').innerText = isAr ? "عرض السلة ومتابعة الطلب" : "View Cart & Checkout";
  document.getElementById('cart-title').innerText = isAr ? "سلة طلب الطاولة" : "Table Order Cart";
  document.getElementById('lbl-cust-name').innerText = isAr ? "اسم العميل (اختياري للنداء):" : "Customer Name (optional):";
  document.getElementById('lbl-kitchen-note').innerText = isAr ? "ملاحظات عامة للمطبخ:" : "General Kitchen Notes:";
  document.getElementById('lbl-allergy-note').innerText = isAr ? "حساسية الطعام (إن وجدت):" : "Food Allergies (if any):";
  document.getElementById('allergy-agree-text').innerText = isAr ? 
    "أقر بأن الملاحظات لا تضمن خلو الطعام بنسبة 100% من مسببات الحساسية." :
    "I understand notes do not guarantee 100% allergen-free preparation.";
  document.getElementById('lbl-subtotal').innerText = isAr ? "المجموع قبل الضريبة:" : "Subtotal (excl. VAT):";
  document.getElementById('lbl-vat').innerText = isAr ? "ضريبة القيمة المضافة (15% ZATCA):" : "VAT (15% ZATCA):";
  document.getElementById('lbl-final-total').innerText = isAr ? "الإجمالي النهائي شامل الضريبة:" : "Total (incl. 15% VAT):";
  document.getElementById('btn-submit-order').innerText = isAr ? "تأكيد وإرسال الطلب للمطبخ" : "Confirm & Send Order to Kitchen";
}

function renderCategories() {
  const container = document.getElementById('categories-container');
  container.innerHTML = '';

  CATEGORIES.forEach(cat => {
    const btn = document.createElement('button');
    btn.className = `cat-btn ${cat.id === currentCategory ? 'active' : ''}`;
    btn.innerText = (currentLang === 'ar') ? cat.nameAr : cat.nameEn;
    btn.onclick = () => {
      currentCategory = cat.id;
      renderCategories();
      renderMenuItems();
    };
    container.appendChild(btn);
  });
}

function renderMenuItems() {
  const grid = document.getElementById('menu-items-grid');
  grid.innerHTML = '';

  const filtered = MENU_ITEMS.filter(it => it.categoryId === currentCategory);
  filtered.forEach(item => {
    const card = document.createElement('div');
    card.className = 'menu-card';
    card.onclick = (e) => {
      if (e.target.tagName !== 'BUTTON') {
        openProductModal(item);
      }
    };

    const isAr = currentLang === 'ar';
    const name = isAr ? item.nameAr : item.nameEn;
    const desc = isAr ? item.descAr : item.descEn;

    card.innerHTML = `
      <div class="item-thumb-box">
        <span>${item.icon}</span>
        ${item.isSpicy ? `<span class="spicy-badge">${isAr ? 'حراق' : 'Spicy'}</span>` : ''}
      </div>
      <div class="item-info">
        <h3 class="item-title">${name}</h3>
        <p class="item-desc">${desc}</p>
        <div class="item-footer">
          <span class="item-price">${item.priceSar.toFixed(2)} SAR</span>
          <button class="add-btn" onclick="quickAdd('${item.id}', event)">${isAr ? 'إضافة +' : 'Add +'}</button>
        </div>
      </div>
    `;

    grid.appendChild(card);
  });
}

function quickAdd(itemId, event) {
  event.stopPropagation();
  const item = MENU_ITEMS.find(it => it.id === itemId);
  if (!item) return;

  addToCart(item, 1, item.isSpicy ? 'spicy' : 'normal', '');
}

function openProductModal(item) {
  modalItem = item;
  modalQty = 1;
  modalSpice = item.isSpicy ? 'spicy' : 'normal';

  const isAr = currentLang === 'ar';
  document.getElementById('modal-product-name').innerText = isAr ? item.nameAr : item.nameEn;
  document.getElementById('modal-product-desc').innerText = isAr ? item.descAr : item.descEn;
  document.getElementById('modal-product-price').innerText = `${item.priceSar.toFixed(2)} SAR`;
  document.getElementById('modal-product-cal').innerText = item.calories;
  document.getElementById('modal-qty').innerText = modalQty;
  document.getElementById('modal-item-note').value = '';

  selectSpice(modalSpice);

  document.getElementById('product-modal').style.display = 'flex';
}

function closeProductModal() {
  document.getElementById('product-modal').style.display = 'none';
  modalItem = null;
}

function selectSpice(spice) {
  modalSpice = spice;
  document.getElementById('btn-spice-normal').className = `spice-btn ${spice === 'normal' ? 'active' : ''}`;
  document.getElementById('btn-spice-spicy').className = `spice-btn ${spice === 'spicy' ? 'active' : ''}`;
}

function incrementModalQty() {
  modalQty++;
  document.getElementById('modal-qty').innerText = modalQty;
}

function decrementModalQty() {
  if (modalQty > 1) {
    modalQty--;
    document.getElementById('modal-qty').innerText = modalQty;
  }
}

function confirmAddToCart() {
  if (!modalItem) return;
  const note = document.getElementById('modal-item-note').value.trim();
  addToCart(modalItem, modalQty, modalSpice, note);
  closeProductModal();
}

function addToCart(item, qty, spice, note) {
  const existing = cart.find(ci => ci.item.id === item.id && ci.spice === spice && ci.note === note);
  if (existing) {
    existing.qty += qty;
  } else {
    cart.push({
      item,
      qty,
      spice,
      note
    });
  }
  updateCartUI();
}

function updateCartUI() {
  const totalCount = cart.reduce((sum, i) => sum + i.qty, 0);
  const totalSar = cart.reduce((sum, i) => sum + (i.item.priceSar * i.qty), 0);

  const cartBar = document.getElementById('floating-cart-bar');
  if (totalCount > 0) {
    cartBar.style.display = 'flex';
    document.getElementById('cart-item-count').innerText = totalCount;
    document.getElementById('cart-bar-total').innerText = `${totalSar.toFixed(2)} SAR`;
  } else {
    cartBar.style.display = 'none';
  }

  // Update modal totals
  const vat = totalSar - (totalSar / 1.15);
  const sub = totalSar - vat;
  document.getElementById('cart-subtotal-val').innerText = `${sub.toFixed(2)} SAR`;
  document.getElementById('cart-vat-val').innerText = `${vat.toFixed(2)} SAR`;
  document.getElementById('cart-final-total-val').innerText = `${totalSar.toFixed(2)} SAR`;

  renderCartItems();
}

function renderCartItems() {
  const container = document.getElementById('cart-items-container');
  container.innerHTML = '';

  const isAr = currentLang === 'ar';

  cart.forEach((ci, idx) => {
    const row = document.createElement('div');
    row.style.display = 'flex';
    row.style.justifyContent = 'space-between';
    row.style.alignItems = 'center';
    row.style.padding = '8px 0';
    row.style.borderBottom = '1px solid #EEE';

    const spiceText = ci.spice === 'spicy' ? (isAr ? 'حراق' : 'Spicy') : (isAr ? 'عادي' : 'Regular');
    const name = isAr ? ci.item.nameAr : ci.item.nameEn;

    row.innerHTML = `
      <div>
        <div style="font-weight: 700; font-size: 13px;">${name}</div>
        <div style="font-size: 11px; color: #666;">${isAr ? 'الخيار' : 'Option'}: ${spiceText} ${ci.note ? `• ${ci.note}` : ''}</div>
        <div style="font-weight: 800; color: #8B0E14; font-size: 13px;">${(ci.item.priceSar * ci.qty).toFixed(2)} SAR</div>
      </div>
      <div class="stepper">
        <button class="step-btn" onclick="updateItemQty(${idx}, ${ci.qty - 1})">-</button>
        <span class="step-value">${ci.qty}</span>
        <button class="step-btn" onclick="updateItemQty(${idx}, ${ci.qty + 1})">+</button>
      </div>
    `;

    container.appendChild(row);
  });
}

function updateItemQty(index, newQty) {
  if (newQty <= 0) {
    cart.splice(index, 1);
  } else {
    cart[index].qty = newQty;
  }
  updateCartUI();
}

function openCartModal() {
  document.getElementById('cart-modal').style.display = 'flex';
}

function closeCartModal() {
  document.getElementById('cart-modal').style.display = 'none';
}

function submitOrder() {
  if (cart.length === 0) return;

  const allergyAgreed = document.getElementById('allergy-agreed').checked;
  if (!allergyAgreed) {
    alert(currentLang === 'ar' ? 'يرجى الإقرار بتنبيه الحساسية الغذائية للمتابعة.' : 'Please acknowledge the allergy notice to proceed.');
    return;
  }

  const submitBtn = document.getElementById('btn-submit-order');
  submitBtn.disabled = true;
  submitBtn.innerText = currentLang === 'ar' ? 'جاري إرسال الطلب للمطبخ...' : 'Sending order...';

  // Simulated server call with unique order generation
  setTimeout(() => {
    const branchCode = (currentBranchId === 'mansourah') ? 'MN' : 'AZ';
    const dateCode = new Date().toISOString().slice(2, 10).replace(/-/g, '');
    const randNum = Math.floor(100 + Math.random() * 900);
    const orderNumber = `MKT-${branchCode}-${dateCode}-${randNum}`;

    document.getElementById('success-order-num').innerText = orderNumber;
    closeCartModal();
    cart = [];
    updateCartUI();

    document.getElementById('order-success-modal').style.display = 'flex';
    submitBtn.disabled = false;
    submitBtn.innerText = currentLang === 'ar' ? 'تأكيد وإرسال الطلب للمطبخ' : 'Confirm & Send Order to Kitchen';
  }, 800);
}

function closeSuccessModal() {
  document.getElementById('order-success-modal').style.display = 'none';
}
