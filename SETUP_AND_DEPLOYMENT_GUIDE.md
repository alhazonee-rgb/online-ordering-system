# AL MOKHTAR RESTAURANT, RIYADH — PRODUCTION SETUP & DEPLOYMENT GUIDE
## دليل التشغيل والنشر الشامل لنظام مطعم المختار (فرع المنصورة وفرع العزيزية)

---

### 1. نظرة عامة على النظام (System Architecture)
تم تصميم النظام ليعمل كمنظومة موحدة مركزية تخدم فرعي مطعم المختار بالرياض:
1. **فرع المنصورة / Al Mansourah Branch**:
   - العنوان: ۳۲٤۷ طريق الخرج، حي المنصورة، الرياض ۱۲٦۹۲، المملكة العربية السعودية.
2. **فرع العزيزية / Al Aziziyah Branch**:
   - العنوان: طريق الخرج، حي العزيزية، الرياض ١٤٥١٣، المملكة العربية السعودية.

يتكون النظام من:
- **تطبيق الأندرويد المركزي المتكامل (Android POS, KDS & Manager App)**:
  - شاشة المطبخ المباشرة (Kitchen Display System - KDS).
  - محاكي ومولد رموز الطاولات المشفرة (QR Generator).
  - مركز إدارة طابعات الإيصالات الحرارية (Thermal Printer Hub).
  - إدارة الأصناف وتوفر المخزون وتعديل الأسعار.
  - تقارير المبيعات وتصدير ملفات CSV المحاسبية.
- **موقع وتطبيق الويب للعملاء (Customer Ordering Web App & PWA)**:
  - ثنائي اللغة (عربي / إنجليزي) مع دعم كامل للاتجاه RTL.
  - قراءة رقم الطاولة والفرع تلقائياً من رمز الاستجابة السريعة QR المشفر.
  - تنبيهات الحساسية الغذائية الإلزامية وخيارات الحرارة (عادي / حراق).
- **الخادم السحابي وقواعد البيانات (Secure Backend & Firestore)**:
  - إعادة احتساب الأسعار والمجاميع من الخادم مباشرة لمنع التلاعب في أسعار الوجبات.
  - التحقق من توقيع الطاولة الرقمي (HMAC-SHA256).
  - احتساب ضريبة القيمة المضافة (15% ZATCA) وإصدار أرقام فواتير فريدة.
- **وكيل الطباعة الحرارية لنظام ويندوز (Windows Thermal Print Agent)**:
  - يستقبل الطلبات فورياً ويطبعها على طابعات المطبخ الحرارية 80mm ESC/POS (USB أو Network LAN).

---

### 2. خطوات التثبيت السحابي للمبتدئين خطوة بخطوة (Beginner Setup Steps)

#### الخطوة 1: إعداد مشروع Firebase
1. توجه إلى [Firebase Console](https://console.firebase.google.com) وسجل الدخول بحساب Google.
2. اضغط على **"Add Project"** وسمّ المشروع `almokhtar-restaurant-riyadh`.
3. قم بتفعيل الخطة المدفوعة **Blaze Plan (Pay as you go)** لتفعيل Cloud Functions. (التكلفة مجانية تقريباً ضمن الاستهلاك الأولي).

#### الخطوة 2: تفعيل قواعد بيانات Cloud Firestore
1. من القائمة الجانبية في Firebase، اختر **Firestore Database** واضغط **Create Database**.
2. اختر الموقع الجغرافي الأقرب للرياض: `me-central1` (Dammam/Saudi Arabia) أو `me-west1` لتقليل زمن الاستجابة.
3. انشر ملف قواعد الأمان الموجود في مجلد المشروع:
   ```bash
   firebase deploy --only firestore:rules
   ```

#### الخطوة 3: إنشاء أول حساب مدير بصلاحيات آمنة (First Admin Provisioning)
لمنع أي شخص من تسجيل حساب عادي وأخذ صلاحيات إدارة، يتم إنشاء حساب المدير عبر Firebase Admin Script كالتالي:
```bash
# إنشاء حساب المدير وتعيين الدور المخصص
node -e '
const admin = require("firebase-admin");
admin.initializeApp();
async function createAdmin() {
  const user = await admin.auth().createUser({
    email: "manager@almokhtar.sa",
    password: "SecureAdminPassword2026!",
    displayName: "Al Mokhtar General Manager"
  });
  await admin.auth().setCustomUserClaims(user.uid, { role: "admin" });
  await admin.firestore().collection("admins").doc(user.uid).set({
    email: "manager@almokhtar.sa",
    role: "admin",
    active: true,
    createdAt: admin.firestore.FieldValue.serverTimestamp()
  });
  console.log("Admin created successfully:", user.uid);
}
createAdmin();
'
```

#### الخطوة 4: نشر دوال الخادم السحابية (Cloud Functions)
```bash
cd backend/cloud-functions
npm install
firebase deploy --only functions
```

#### الخطوة 5: نشر موقع الويب للعملاء (Web Hosting)
```bash
firebase deploy --only hosting
```

---

### 3. إعداد وكيل طابعة المطبخ على كمبيوتر الفرع (Windows Print Agent Setup)

في كل فرع (فرع المنصورة وفرع العزيزية):
1. قم بتوصيل طابعة الفواتير الحرارية 80mm (مثل Epson TM-T20، Xprinter، أو Bixolon) بشبكة الكاشير وتثبيت عنوان IP ثابت لها (مثلاً `192.168.1.100`).
2. قم بتثبيت بايثون على جهاز الكاشير (Windows):
   ```bash
   pip install pywin32 requests
   ```
3. افتح ملف `print-agent/al_mokhtar_printer_agent.py` وتأكد من ضبط الفرع وعنوان الطابعة:
   ```python
   CONFIG = {
       "BRANCH_ID": "mansourah",  # أو "aziziyah"
       "PRINTER_TYPE": "NETWORK",
       "PRINTER_IP": "192.168.1.100",
       "PRINTER_PORT": 9100
   }
   ```
4. تشغيل البرنامج كخدمة خلفية دائمة:
   ```bash
   python al_mokhtar_printer_agent.py
   ```

---

### 4. دراسة التكاليف والقدرة الاستيعابية (Cost & Capacity: 1,000–1,500 Orders/Day)

عند تشغيل ۱,٠٠٠ إلى ۱,٥٠٠ طلب يومياً:
- **عمليات القراءة (Reads)**:
  - قائمة الطعام مخزنة محلياً (Cache) على متصفح العميل بواسطة Service Worker والذاكرة المؤقتة.
  - متوسط القراءات اليومية: ~15,000 قراءة (ضمن الحصة المجانية اليومية 50,000 قراءة/يوم).
- **عمليات الكتابة (Writes)**:
  - 1,500 طلب × 2 وثيقة (طلب + مهمة طباعة) = 3,000 كتابة يومياً (ضمن الحصة المجانية 20,000 كتابة/يوم).
- **استهلاك البيانات (Network Egress)**:
  - حجم تذكرة الطلب ~2 KB × 1,500 = 3 MB يومياً (أقل بكثير من 10 GB شهرياً المجانية).
- **التكلفة التقديرية الإجمالية**:
  - **من ٠ إلى ٥ دولارات أمريكية شهرياً فقط** (أقل من ۲۰ ريال سعودي شهرياً) بفضل التصميم المعماري الأمثل الذي يعتمد على الذاكرة المؤقتة للكتالوج والتحقق السحابي خفيف الوزن.

---

### 5. طباعة ولصق رموز الطاولات (Table QR Stickers)
1. افتح شاشة **"رموز الطاولات"** من تطبيق الأندرويد.
2. اختر الفرع ورقم الطاولة (مثلاً فرع المنصورة - طاولة ٤).
3. اطبع بطاقة الطاولة مع شعار المطعم وتعليمات المسح المباشر بكاميرا الجوال.
4. الرمز يحتوي على رابط مباشر يحمل توقيع HMAC رقمي فريد ومحمي من التلاعب.
