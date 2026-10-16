package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.AppLanguage
import com.example.model.MenuItem
import com.example.ui.theme.MokhtarCharcoal
import com.example.ui.theme.MokhtarCrimson
import com.example.ui.theme.MokhtarGold
import com.example.ui.theme.MokhtarLightCard
import com.example.ui.theme.MokhtarSuccess
import java.util.UUID

@Composable
fun MenuManagementScreen(
    viewModel: AlMokhtarViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val isAr = language == AppLanguage.ARABIC
    val menuItems by viewModel.repository.menuItems.collectAsState()

    var editingItem by remember { mutableStateOf<MenuItem?>(null) }
    var showAddNewItemDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isAr) "إدارة الأصناف، الأسعار ونسب الخصم" else "Menu, Prices & Offers Management",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (isAr)
                            "تعديل أسعار الوجبات، إضافة عروض الخصم %، تشغيل أو إيقاف الوجبات، وتغيير الصور"
                        else
                            "Manage food photos, SAR prices, discount offer %, and stock on/off",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddNewItemDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MokhtarCrimson)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isAr) "صنف جديد" else "New Item", fontSize = 12.sp)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(menuItems, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Food Thumbnail
                        Image(
                            painter = painterResource(id = getFoodDrawableRes(item.imageKey)),
                            contentDescription = item.nameEn,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Item Details
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAr) item.nameAr else item.nameEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (item.discountPercent > 0) {
                                    Text(
                                        text = "%.2f SAR".format(item.priceSar),
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                }

                                Text(
                                    text = "%.2f SAR".format(item.effectivePriceSar),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = MokhtarCrimson
                                )

                                if (item.discountPercent > 0) {
                                    Surface(
                                        color = MokhtarGold,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "${item.discountPercent}% OFF",
                                            color = MokhtarCharcoal,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Edit Price & Offer Button
                        FilledTonalIconButton(
                            onClick = { editingItem = item },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MokhtarCrimson.copy(alpha = 0.1f))
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MokhtarCrimson)
                        }

                        // In-Stock / Sold-Out Switch
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Switch(
                                checked = item.isAvailable,
                                onCheckedChange = { viewModel.toggleItemAvailability(item.id, it) }
                            )
                            Text(
                                text = if (item.isAvailable) {
                                    if (isAr) "متوفر" else "In Stock"
                                } else {
                                    if (isAr) "موقف" else "Sold Out"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isAvailable) MokhtarSuccess else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit Item Price, Discount %, and Photo Modal
    if (editingItem != null) {
        val item = editingItem!!
        var priceInput by remember { mutableStateOf(item.priceSar.toString()) }
        var discountInput by remember { mutableIntStateOf(item.discountPercent) }
        var selectedImageKey by remember { mutableStateOf(item.imageKey) }

        Dialog(onDismissRequest = { editingItem = null }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isAr) "تعديل بيانات الصنف والعرض" else "Edit Item Price & Discount Offer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (isAr) item.nameAr else item.nameEn,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Base Price
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text(if (isAr) "السعر الأساسي (SAR)" else "Base Price (SAR)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Discount Offer %
                    Text(
                        text = "${if (isAr) "نسبة الخصم / العرض" else "Discount Offer %"}: $discountInput%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(listOf(0, 5, 10, 15, 20, 25, 30, 50)) { pct ->
                            FilterChip(
                                selected = discountInput == pct,
                                onClick = { discountInput = pct },
                                label = { Text(if (pct == 0) (if (isAr) "بدون خصم" else "0%") else "$pct% OFF") }
                            )
                        }
                    }

                    // Live calculated discounted price
                    val currentBase = priceInput.toDoubleOrNull() ?: item.priceSar
                    val calculatedEffective = currentBase * (1.0 - discountInput / 100.0)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isAr) "السعر النهائي للعميل:" else "Customer Final Price:", fontSize = 12.sp)
                            Text(
                                text = "%.2f SAR".format(calculatedEffective),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = MokhtarCrimson
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Photo Selector
                    Text(text = if (isAr) "اختر صورة الوجبة:" else "Select Dish Photo:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    val photoKeys = listOf("food_broast", "food_msahhab", "food_burger", "food_shrimp")
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(photoKeys) { key ->
                            val isSelected = selectedImageKey == key
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(if (isSelected) 3.dp else 1.dp, if (isSelected) MokhtarCrimson else Color.LightGray, RoundedCornerShape(8.dp))
                                    .clickable { selectedImageKey = key }
                            ) {
                                Image(
                                    painter = painterResource(id = getFoodDrawableRes(key)),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { editingItem = null }) { Text(if (isAr) "إلغاء" else "Cancel") }
                        Button(
                            onClick = {
                                val newP = priceInput.toDoubleOrNull()
                                if (newP != null && newP > 0) {
                                    viewModel.updateItemPriceAndDiscount(item.id, newP, discountInput, selectedImageKey)
                                }
                                editingItem = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MokhtarCrimson)
                        ) {
                            Text(if (isAr) "حفظ التعديلات" else "Save Changes")
                        }
                    }
                }
            }
        }
    }

    // Add New Dish Modal
    if (showAddNewItemDialog) {
        var nameAr by remember { mutableStateOf("") }
        var nameEn by remember { mutableStateOf("") }
        var priceInput by remember { mutableStateOf("25.0") }
        var selectedCat by remember { mutableStateOf("broast") }
        var selectedPhoto by remember { mutableStateOf("food_broast") }

        Dialog(onDismissRequest = { showAddNewItemDialog = false }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = if (isAr) "إضافة وجبة جديدة للمنيو" else "Add New Dish to Menu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = nameAr,
                        onValueChange = { nameAr = it },
                        label = { Text(if (isAr) "اسم الوجبة بالعربي" else "Dish Name (Arabic)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text(if (isAr) "اسم الوجبة بالإنجليزي" else "Dish Name (English)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text(if (isAr) "السعر بالريال السعودي (SAR)" else "Price in SAR") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddNewItemDialog = false }) { Text(if (isAr) "إلغاء" else "Cancel") }
                        Button(
                            onClick = {
                                val p = priceInput.toDoubleOrNull() ?: 20.0
                                val newItem = MenuItem(
                                    id = "custom_" + UUID.randomUUID().toString().take(6),
                                    categoryId = selectedCat,
                                    nameAr = nameAr.ifBlank { "وجبة جديدة" },
                                    nameEn = nameEn.ifBlank { "New Special Dish" },
                                    descriptionAr = "وجبة طازجة وشهية من مطعم المختار",
                                    descriptionEn = "Fresh and delicious dish prepared by Al Mokhtar",
                                    priceSar = p,
                                    discountPercent = 0,
                                    isAvailable = true,
                                    imageKey = selectedPhoto
                                )
                                viewModel.addNewItem(newItem)
                                showAddNewItemDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MokhtarCrimson)
                        ) {
                            Text(if (isAr) "إضافة الصنف" else "Add Dish")
                        }
                    }
                }
            }
        }
    }
}
