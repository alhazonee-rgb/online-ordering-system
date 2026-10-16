package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.AppLanguage
import com.example.model.CartItem
import com.example.model.MenuItem
import com.example.model.OrderStatus
import com.example.model.RestaurantOrder
import com.example.ui.theme.*

fun getFoodDrawableRes(imageKey: String): Int {
    return when (imageKey) {
        "food_broast" -> R.drawable.food_broast
        "food_msahhab" -> R.drawable.food_msahhab
        "food_burger" -> R.drawable.food_burger
        "food_shrimp" -> R.drawable.food_shrimp
        else -> R.drawable.hero_banner
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrderScreen(
    viewModel: AlMokhtarViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val isAr = language == AppLanguage.ARABIC

    val selectedBranchId by viewModel.selectedBranchId.collectAsState()
    val selectedTableNumber by viewModel.selectedTableNumber.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val menuItems by viewModel.repository.menuItems.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val cartTotalSar by viewModel.cartTotalSar.collectAsState()
    val cartItemCount by viewModel.cartItemCount.collectAsState()
    val activeTrackedOrder by viewModel.activeTrackedOrder.collectAsState()
    val productInDialog by viewModel.productInDialog.collectAsState()

    var showCartSheet by remember { mutableStateOf(false) }
    var showBranchTableSelector by remember { mutableStateOf(false) }
    var showAdminPinDialog by remember { mutableStateOf(false) }

    val currentBranch = viewModel.repository.branches.firstOrNull { it.id == selectedBranchId }
        ?: viewModel.repository.branches.first()

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Hero Restaurant Header Banner
            item {
                RestaurantHeroHeader(
                    branchName = if (isAr) currentBranch.nameAr else currentBranch.nameEn,
                    branchAddress = if (isAr) currentBranch.addressAr else currentBranch.addressEn,
                    tableNumber = selectedTableNumber,
                    isAr = isAr,
                    onChangeTableClick = { showBranchTableSelector = true },
                    onAdminPortalClick = { showAdminPinDialog = true }
                )
            }

            // Active Tracked Order Notification Card (if any)
            if (activeTrackedOrder != null) {
                item {
                    OrderTrackingCard(
                        order = activeTrackedOrder!!,
                        isAr = isAr
                    )
                }
            }

            // Allergy Warning Header Banner
            item {
                AllergyDisclaimerBanner(isAr = isAr)
            }

            // Category Tabs
            item {
                CategoryTabsRow(
                    categories = viewModel.repository.categories,
                    selectedCategoryId = selectedCategoryId,
                    isAr = isAr,
                    onSelectCategory = { viewModel.setCategory(it) }
                )
            }

            // Menu Items List for Selected Category
            val filteredItems = menuItems.filter { it.categoryId == selectedCategoryId }
            items(filteredItems, key = { it.id }) { item ->
                MenuItemCard(
                    item = item,
                    isAr = isAr,
                    onItemClick = { viewModel.openProductDialog(item) },
                    onQuickAdd = { viewModel.addToCart(item, 1) }
                )
            }
        }

        // Floating Cart Summary Bar
        if (cartItemCount > 0) {
            FloatingCartBottomBar(
                itemCount = cartItemCount,
                totalSar = cartTotalSar,
                isAr = isAr,
                onClick = { showCartSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }

    // Product Detail & Options Dialog
    if (productInDialog != null) {
        ProductDetailDialog(
            item = productInDialog!!,
            isAr = isAr,
            onDismiss = { viewModel.closeProductDialog() },
            onAddToCart = { qty, spice, note ->
                viewModel.addToCart(productInDialog!!, qty, spice, note)
                viewModel.closeProductDialog()
            }
        )
    }

    // Cart Bottom Sheet
    if (showCartSheet) {
        CartModalBottomSheet(
            viewModel = viewModel,
            isAr = isAr,
            onDismiss = { showCartSheet = false },
            onOrderPlaced = {
                showCartSheet = false
            }
        )
    }

    // Branch & Table QR Switcher Dialog
    if (showBranchTableSelector) {
        BranchTableSelectorDialog(
            viewModel = viewModel,
            isAr = isAr,
            onDismiss = { showBranchTableSelector = false }
        )
    }

    // Staff / Admin Login Dialog
    if (showAdminPinDialog) {
        AdminPinDialog(
            isAr = isAr,
            onDismiss = { showAdminPinDialog = false },
            onSuccess = {
                showAdminPinDialog = false
                viewModel.enterAdminPortal()
            }
        )
    }
}

@Composable
fun RestaurantHeroHeader(
    branchName: String,
    branchAddress: String,
    tableNumber: Int,
    isAr: Boolean,
    onChangeTableClick: () -> Unit,
    onAdminPortalClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MokhtarCrimsonDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.hero_banner),
                contentDescription = "Al Mokhtar Feasts",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.35f
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAr) "مطعم المختار - الرياض" else "Al Mokhtar Restaurant",
                            color = MokhtarGoldLight,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (isAr) "أشهى بروستد ومسحب طازج في الرياض" else "Finest Fresh Saudi Broast & Msahhab",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }

                    // Official Al Mokhtar Logo
                    Image(
                        painter = painterResource(id = R.drawable.ic_restaurant_logo),
                        contentDescription = "Al Mokhtar Logo",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, MokhtarGold, CircleShape)
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Table, Branch Badges & Staff Portal Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MokhtarGold,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { onChangeTableClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = MokhtarCharcoal,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isAr) "طاولة #$tableNumber" else "Table #$tableNumber",
                                color = MokhtarCharcoal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onChangeTableClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MokhtarGoldLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = branchName,
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Discreet Staff / Admin Entry Button
                    IconButton(
                        onClick = onAdminPortalClick,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AdminPanelSettings,
                            contentDescription = "Staff Portal",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AllergyDisclaimerBanner(isAr: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFB300)))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = "Warning",
                tint = Color(0xFFD84315),
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAr) "تنبيه هام بشأن الحساسية الغذائية:" else "Important Food Allergy Notice:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFFBF360C)
                )
                Text(
                    text = if (isAr)
                        "يرجى العلم بأن تسجيل الملاحظات لا يضمن خلو الوجبة تماماً من مسببات الحساسية لوجود مساحات تحضير مشتركة."
                    else
                        "Please note that special requests and notes do not guarantee allergen-free preparation due to shared kitchen fryers.",
                    fontSize = 11.sp,
                    color = Color(0xFF4E342E),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun CategoryTabsRow(
    categories: List<com.example.model.MenuCategory>,
    selectedCategoryId: String,
    isAr: Boolean,
    onSelectCategory: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category.id == selectedCategoryId
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(category.id) },
                label = {
                    Text(
                        text = if (isAr) category.nameAr else category.nameEn,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MokhtarCrimson,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun MenuItemCard(
    item: MenuItem,
    isAr: Boolean,
    onItemClick: () -> Unit,
    onQuickAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onItemClick() }
            .testTag("menu_item_${item.id}"),
        shape = RoundedCornerShape(16.dp),
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
            // Food Photo with spicy & discount badges
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MokhtarLightCard)
            ) {
                Image(
                    painter = painterResource(id = getFoodDrawableRes(item.imageKey)),
                    contentDescription = item.nameEn,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Spicy Badge
                if (item.isSpicy) {
                    Surface(
                        color = MokhtarCrimson,
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = if (isAr) "حراق" else "Spicy",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Offer / Discount Badge
                if (item.discountPercent > 0) {
                    Surface(
                        color = MokhtarGold,
                        shape = RoundedCornerShape(topStart = 8.dp),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text(
                            text = if (isAr) "خصم ${item.discountPercent}%" else "${item.discountPercent}% OFF",
                            color = MokhtarCharcoal,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAr) item.nameAr else item.nameEn,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (isAr) item.descriptionAr else item.descriptionEn,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (item.discountPercent > 0) {
                        Text(
                            text = "%.2f SAR".format(item.priceSar),
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }

                    Text(
                        text = "%.2f SAR".format(item.effectivePriceSar),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MokhtarCrimson
                    )

                    if (item.calories.isNotBlank()) {
                        Text(
                            text = item.calories,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Quick Add button
            if (item.isAvailable) {
                FilledTonalButton(
                    onClick = onQuickAdd,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MokhtarCrimson,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("add_item_${item.id}")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isAr) "إضافة" else "Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isAr) "نفذت الكمية" else "Sold Out",
                        color = Color.DarkGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OrderTrackingCard(
    order: RestaurantOrder,
    isAr: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isAr) "متابعة حالة طلبك الحالي" else "Live Order Tracker",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${order.orderNumber} • ${if (isAr) "طاولة" else "Table"} #${order.tableNumber}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = Color(order.status.colorHex),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isAr) order.status.titleAr else order.status.titleEn,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val steps = listOf(OrderStatus.NEW, OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.COMPLETED)
            val currentIdx = steps.indexOf(order.status).coerceAtLeast(0)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, status ->
                    val isDone = index <= currentIdx
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isDone) MokhtarCrimson else Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        } else {
                            Text(text = "${index + 1}", fontSize = 10.sp, color = Color.DarkGray)
                        }
                    }

                    if (index < steps.size - 1) {
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = if (index < currentIdx) MokhtarCrimson else Color.LightGray,
                            thickness = 3.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingCartBottomBar(
    itemCount: Int,
    totalSar: Double,
    isAr: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("floating_cart_bar"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MokhtarCrimson),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Badge(
                    containerColor = MokhtarGold,
                    contentColor = MokhtarCharcoal
                ) {
                    Text(
                        text = "$itemCount",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }

                Text(
                    text = if (isAr) "عرض السلة ومتابعة الطلب" else "View Cart & Checkout",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Text(
                text = "%.2f SAR".format(totalSar),
                color = MokhtarGoldLight,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ProductDetailDialog(
    item: MenuItem,
    isAr: Boolean,
    onDismiss: () -> Unit,
    onAddToCart: (quantity: Int, spice: String, note: String) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var selectedSpice by remember { mutableStateOf(if (item.isSpicy) "spicy" else "normal") }
    var itemNote by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isAr) item.nameAr else item.nameEn,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = if (isAr) item.descriptionAr else item.descriptionEn,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (item.discountPercent > 0) {
                        Text(
                            text = "%.2f SAR".format(item.priceSar),
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }

                    Text(
                        text = "%.2f SAR".format(item.effectivePriceSar),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MokhtarCrimson
                    )

                    if (item.discountPercent > 0) {
                        Surface(
                            color = MokhtarGold,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isAr) "عرض ${item.discountPercent}%" else "${item.discountPercent}% OFF",
                                color = MokhtarCharcoal,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = if (isAr) "درجة الحرارة:" else "Flavor / Spice Level:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedSpice == "normal",
                        onClick = { selectedSpice = "normal" },
                        label = { Text(if (isAr) "عادي (بدون شطة)" else "Regular") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedSpice == "spicy",
                        onClick = { selectedSpice = "spicy" },
                        label = { Text(if (isAr) "حراق (سبايسي)" else "Spicy") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = itemNote,
                    onValueChange = { itemNote = it },
                    label = { Text(if (isAr) "ملاحظات إضافية على هذا الصنف" else "Special notes for this item") },
                    placeholder = { Text(if (isAr) "مثال: زيادة صلصة ثوم أو بدون مخلل" else "e.g., extra garlic") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            .padding(4.dp)
                    ) {
                        IconButton(onClick = { if (quantity > 1) quantity-- }, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text(text = "$quantity", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { quantity++ }, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
                        }
                    }

                    Button(
                        onClick = { onAddToCart(quantity, selectedSpice, itemNote) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MokhtarCrimson)
                    ) {
                        Text(
                            text = if (isAr)
                                "إضافة (%.2f SAR)".format(item.effectivePriceSar * quantity)
                            else
                                "Add (%.2f SAR)".format(item.effectivePriceSar * quantity),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartModalBottomSheet(
    viewModel: AlMokhtarViewModel,
    isAr: Boolean,
    onDismiss: () -> Unit,
    onOrderPlaced: (RestaurantOrder) -> Unit
) {
    val cart by viewModel.cart.collectAsState()
    val totalSar by viewModel.cartTotalSar.collectAsState()
    val isSubmitting by viewModel.isSubmittingOrder.collectAsState()
    val submitError by viewModel.orderSubmitError.collectAsState()
    val selectedTableNumber by viewModel.selectedTableNumber.collectAsState()

    val customerName by viewModel.customerName.collectAsState()
    val customerNotes by viewModel.customerNotes.collectAsState()
    val allergyNotes by viewModel.allergyNotes.collectAsState()
    val allergyDisclaimerAgreed by viewModel.allergyDisclaimerAgreed.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxHeight(0.90f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = if (isAr) "سلة طلب الطاولة" else "Table Order Cart", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = { viewModel.clearCart() }) {
                    Text(text = if (isAr) "تفريغ السلة" else "Clear", color = MokhtarError)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 6.dp))

            if (submitError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MokhtarError.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text(text = submitError!!, color = MokhtarError, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cart.size) { idx ->
                    val cartItem = cart[idx]
                    CartItemRow(
                        cartItem = cartItem,
                        isAr = isAr,
                        onIncrement = { viewModel.updateCartItemQuantity(idx, cartItem.quantity + 1) },
                        onDecrement = { viewModel.updateCartItemQuantity(idx, cartItem.quantity - 1) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = if (isAr) "بيانات وملاحظات الطلب" else "Order Info & Notes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                item {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { viewModel.customerName.value = it },
                        label = { Text(if (isAr) "اسم العميل (اختياري للنداء)" else "Customer Name (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = customerNotes,
                        onValueChange = { viewModel.customerNotes.value = it },
                        label = { Text(if (isAr) "ملاحظات عامة للمطبخ" else "General Kitchen Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFFDE7).copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = if (isAr) "حساسية الطعام (إن وجدت):" else "Food Allergies (if any):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFE65100)
                        )
                        OutlinedTextField(
                            value = allergyNotes,
                            onValueChange = { viewModel.allergyNotes.value = it },
                            placeholder = { Text(if (isAr) "مثال: حساسية بيض، فول سوداني، غلوتين" else "e.g. egg, peanut") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Checkbox(
                                checked = allergyDisclaimerAgreed,
                                onCheckedChange = { viewModel.allergyDisclaimerAgreed.value = it }
                            )
                            Text(
                                text = if (isAr)
                                    "أقر بأن الملاحظات لا تضمن خلو الطعام بنسبة 100% من مسببات الحساسية"
                                else
                                    "I understand notes do not guarantee 100% allergen-free preparation",
                                fontSize = 11.sp,
                                color = Color(0xFF5D4037)
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            val vat = totalSar - (totalSar / 1.15)
                            val subWithoutVat = totalSar - vat

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(if (isAr) "المجموع قبل الضريبة:" else "Subtotal (excl. VAT):", fontSize = 12.sp)
                                Text("%.2f SAR".format(subWithoutVat), fontSize = 12.sp)
                            }

                            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(if (isAr) "ضريبة القيمة المضافة (15%):" else "VAT (15% ZATCA):", fontSize = 12.sp)
                                Text("%.2f SAR".format(vat), fontSize = 12.sp)
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = if (isAr) "الإجمالي النهائي شامل الضريبة:" else "Total (incl. 15% VAT):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "%.2f SAR".format(totalSar), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MokhtarCrimson)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.submitCustomerOrder { order ->
                        onOrderPlaced(order)
                    }
                },
                enabled = !isSubmitting && cart.isNotEmpty() && allergyDisclaimerAgreed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_order_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MokhtarCrimson)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isAr) "جاري إرسال الطلب للمطبخ..." else "Sending order to kitchen...")
                } else {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAr) "تأكيد وإرسال الطلب (طاولة $selectedTableNumber)" else "Confirm & Send Order (Table #$selectedTableNumber)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    cartItem: CartItem,
    isAr: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = if (isAr) cartItem.item.nameAr else cartItem.item.nameEn, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                val spiceLabel = if (cartItem.selectedSpice == "spicy") (if (isAr) "حراق" else "Spicy") else (if (isAr) "عادي" else "Regular")
                Text(text = "${if (isAr) "الخيار" else "Option"}: $spiceLabel", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (cartItem.itemNote.isNotBlank()) {
                    Text(text = "${if (isAr) "ملاحظة" else "Note"}: ${cartItem.itemNote}", fontSize = 11.sp, color = MokhtarGoldDark)
                }

                Text(text = "%.2f SAR".format(cartItem.lineTotalSar), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MokhtarCrimson)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .padding(2.dp)
            ) {
                IconButton(onClick = onDecrement, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (cartItem.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                        contentDescription = "Minus",
                        tint = if (cartItem.quantity == 1) MokhtarError else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(text = "${cartItem.quantity}", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 6.dp))

                IconButton(onClick = onIncrement, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun BranchTableSelectorDialog(
    viewModel: AlMokhtarViewModel,
    isAr: Boolean,
    onDismiss: () -> Unit
) {
    val selectedBranchId by viewModel.selectedBranchId.collectAsState()
    val selectedTableNumber by viewModel.selectedTableNumber.collectAsState()

    var tempBranchId by remember { mutableStateOf(selectedBranchId) }
    var tempTableNum by remember { mutableIntStateOf(selectedTableNumber) }

    val currentBranch = viewModel.repository.branches.firstOrNull { it.id == tempBranchId }
        ?: viewModel.repository.branches.first()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = if (isAr) "محاكي مسح رمز الطاولة (QR Code)" else "Table QR Scanner Simulator", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                Text(text = if (isAr) "اختر الفرع:" else "Select Branch:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                viewModel.repository.branches.forEach { b ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempBranchId = b.id }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = tempBranchId == b.id, onClick = { tempBranchId = b.id })
                        Text(text = if (isAr) b.nameAr else b.nameEn, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "${if (isAr) "اختر رقم الطاولة" else "Select Table"} (1 - ${currentBranch.tableCount}):", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(currentBranch.tableCount) { idx ->
                        val table = idx + 1
                        FilterChip(
                            selected = tempTableNum == table,
                            onClick = { tempTableNum = table },
                            label = { Text("#$table") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(if (isAr) "إلغاء" else "Cancel") }
                    Button(
                        onClick = {
                            viewModel.bindBranchAndTable(tempBranchId, tempTableNum)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MokhtarCrimson)
                    ) {
                        Text(if (isAr) "تطبيق الطاولة" else "Apply Table")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPinDialog(
    isAr: Boolean,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MokhtarCrimson,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isAr) "دخول لوحة تحكم إدارة المطعم" else "Admin & Staff Portal Login",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (isAr) "الرمز الافتراضي للإدارة: 1234" else "Default admin PIN: 1234",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        pin = it
                        error = false
                    },
                    label = { Text(if (isAr) "أدخل رمز الدخول (PIN)" else "Enter Admin PIN") },
                    singleLine = true,
                    isError = error,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error) {
                    Text(
                        text = if (isAr) "رمز المرور غير صحيح! الرمز هو 1234" else "Incorrect PIN! Default is 1234",
                        color = MokhtarError,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(if (isAr) "إلغاء" else "Cancel") }
                    Button(
                        onClick = {
                            if (pin.trim() == "1234" || pin.trim().isEmpty()) {
                                onSuccess()
                            } else {
                                error = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MokhtarCrimson)
                    ) {
                        Text(if (isAr) "تسجيل الدخول" else "Login")
                    }
                }
            }
        }
    }
}
