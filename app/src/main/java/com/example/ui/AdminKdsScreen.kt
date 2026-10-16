package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ThermalEscPosUtil
import com.example.model.AppLanguage
import com.example.model.OrderStatus
import com.example.model.RestaurantOrder
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminKdsScreen(
    viewModel: AlMokhtarViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val isAr = language == AppLanguage.ARABIC

    val orders by viewModel.repository.orders.collectAsState()
    val branchFilter by viewModel.adminBranchFilter.collectAsState()
    val statusFilter by viewModel.adminStatusFilter.collectAsState()
    val selectedReceiptOrder by viewModel.selectedReceiptOrder.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // Filter orders
    val filteredOrders = orders.filter { order ->
        val matchesBranch = branchFilter == "all" || order.branchId == branchFilter
        val matchesStatus = statusFilter == null || order.status == statusFilter
        val matchesSearch = searchQuery.isBlank() ||
                order.orderNumber.contains(searchQuery, ignoreCase = true) ||
                order.tableNumber.toString() == searchQuery.trim() ||
                order.customerName.contains(searchQuery, ignoreCase = true)
        matchesBranch && matchesStatus && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Filter Bar: Branch Switcher & Search
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Branch Tabs
                    FilterChip(
                        selected = branchFilter == "all",
                        onClick = { viewModel.setAdminBranchFilter("all") },
                        label = { Text(if (isAr) "كافة الفروع" else "All Branches") }
                    )
                    FilterChip(
                        selected = branchFilter == "mansourah",
                        onClick = { viewModel.setAdminBranchFilter("mansourah") },
                        label = { Text(if (isAr) "فرع المنصورة" else "Al Mansourah") }
                    )
                    FilterChip(
                        selected = branchFilter == "aziziyah",
                        onClick = { viewModel.setAdminBranchFilter("aziziyah") },
                        label = { Text(if (isAr) "فرع العزيزية" else "Al Aziziyah") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isAr) "بحث برقم الطلب، رقم الطاولة، أو اسم العميل..." else "Search order #, table #, or customer...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status Filter Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = statusFilter == null,
                            onClick = { viewModel.setAdminStatusFilter(null) },
                            label = { Text(if (isAr) "الكل (${orders.size})" else "All (${orders.size})") }
                        )
                    }
                    items(OrderStatus.values()) { status ->
                        val count = orders.count { it.status == status }
                        val isSelected = statusFilter == status
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setAdminStatusFilter(if (isSelected) null else status) },
                            label = {
                                Text("${if (isAr) status.titleAr else status.titleEn} ($count)")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(status.colorHex),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Live Queue Content
        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MokhtarSuccess,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isAr) "لا توجد طلبات تطابق الفلتر المحدد" else "No orders matching current filter",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    KdsOrderCard(
                        order = order,
                        isAr = isAr,
                        onUpdateStatus = { newStatus -> viewModel.updateOrderStatus(order.id, newStatus) },
                        onPrintSlip = { viewModel.reprintKitchenTicket(order) },
                        onViewReceipt = { viewModel.inspectReceipt(order) }
                    )
                }
            }
        }
    }

    // Thermal Receipt / Ticket Dialog Preview
    if (selectedReceiptOrder != null) {
        ReceiptPreviewDialog(
            order = selectedReceiptOrder!!,
            isAr = isAr,
            onDismiss = { viewModel.inspectReceipt(null) },
            onPrint = {
                viewModel.reprintKitchenTicket(selectedReceiptOrder!!)
                viewModel.inspectReceipt(null)
            }
        )
    }
}

@Composable
fun KdsOrderCard(
    order: RestaurantOrder,
    isAr: Boolean,
    onUpdateStatus: (OrderStatus) -> Unit,
    onPrintSlip: () -> Unit,
    onViewReceipt: () -> Unit
) {
    val statusColor = Color(order.status.colorHex)
    val timeAgo = formatTimeAgo(order.createdAtMillis, isAr)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kds_card_${order.orderNumber}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Table #, Branch, Order #, Status & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Big Table Badge
                    Surface(
                        color = MokhtarGold,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isAr) "طاولة #${order.tableNumber}" else "TABLE #${order.tableNumber}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = MokhtarCharcoal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Column {
                        Text(
                            text = order.orderNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${if (isAr) order.branchNameAr else order.branchNameEn} • $timeAgo",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge
                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(10.dp)
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

            Divider(modifier = Modifier.padding(vertical = 10.dp))

            // Allergy Alerts if present
            if (order.allergyNotes.isNotBlank()) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MokhtarError)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Warning, contentDescription = null, tint = MokhtarError, modifier = Modifier.size(18.dp))
                        Text(
                            text = "${if (isAr) "تنبيه حساسية: " else "Allergy: "}${order.allergyNotes}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MokhtarError
                        )
                    }
                }
            }

            // Customer notes if present
            if (order.customerNotes.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "${if (isAr) "ملاحظة العميل: " else "Customer note: "}${order.customerNotes}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Items List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${item.quantity}x",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = if (isAr) item.nameAr else item.nameEn,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                val spiceText = if (item.spiceOption == "spicy") {
                                    if (isAr) "حراق" else "Spicy"
                                } else {
                                    if (isAr) "عادي" else "Regular"
                                }
                                Text(
                                    text = spiceText,
                                    fontSize = 11.sp,
                                    color = if (item.spiceOption == "spicy") MokhtarCrimson else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (item.itemNote.isNotBlank()) {
                                    Text(
                                        text = item.itemNote,
                                        fontSize = 11.sp,
                                        color = MokhtarGoldDark
                                    )
                                }
                            }
                        }

                        Text(
                            text = "%.2f SAR".format(item.lineTotalSar),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp))

            // Total & Print Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (order.printedToKitchen) Icons.Default.Check else Icons.Default.PrintDisabled,
                        contentDescription = null,
                        tint = if (order.printedToKitchen) MokhtarSuccess else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (order.printedToKitchen) {
                            if (isAr) "تمت الطباعة للطابعة الحرارية" else "Printed to ESC/POS"
                        } else {
                            if (isAr) "في انتظار الطباعة" else "Pending print"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${if (isAr) "الإجمالي: " else "Total: "}%.2f SAR".format(order.totalSar),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MokhtarCrimson
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Workflow Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Secondary Print & Receipt buttons
                IconButton(onClick = onPrintSlip, modifier = Modifier.size(38.dp)) {
                    Icon(imageVector = Icons.Outlined.Print, contentDescription = "Print Slip", tint = MokhtarCharcoal)
                }
                IconButton(onClick = onViewReceipt, modifier = Modifier.size(38.dp)) {
                    Icon(imageVector = Icons.Outlined.Receipt, contentDescription = "View Receipt", tint = MokhtarCharcoal)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Primary Next-Step Action Button
                when (order.status) {
                    OrderStatus.NEW -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.ACCEPTED) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(OrderStatus.ACCEPTED.colorHex)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isAr) "قبول الطلب" else "Accept Order")
                        }
                    }
                    OrderStatus.ACCEPTED -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.PREPARING) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(OrderStatus.PREPARING.colorHex)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isAr) "بدء التحضير في المطبخ" else "Start Preparing")
                        }
                    }
                    OrderStatus.PREPARING -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.READY) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(OrderStatus.READY.colorHex)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isAr) "جاهز للتقديم" else "Mark Ready")
                        }
                    }
                    OrderStatus.READY -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.COMPLETED) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(OrderStatus.COMPLETED.colorHex)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isAr) "اكتمل وتم التقديم" else "Complete & Served")
                        }
                    }
                    OrderStatus.COMPLETED -> {
                        OutlinedButton(
                            onClick = { onUpdateStatus(OrderStatus.PREPARING) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isAr) "إعادة فتح" else "Reopen")
                        }
                    }
                    OrderStatus.CANCELLED -> {
                        Text(
                            text = if (isAr) "طلب ملغي" else "Cancelled",
                            color = MokhtarError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptPreviewDialog(
    order: RestaurantOrder,
    isAr: Boolean,
    onDismiss: () -> Unit,
    onPrint: () -> Unit
) {
    val receiptText = remember(order) {
        ThermalEscPosUtil.generateCustomerReceiptText(order)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isAr) "معاينة الفاتورة الحرارية (80mm ESC/POS)" else "80mm Thermal Receipt Preview",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 6.dp))

                // Scrollable Monospace Thermal Paper Simulator
                Surface(
                    color = Color.White,
                    border = CardDefaults.outlinedCardBorder(),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LazyColumn(modifier = Modifier.padding(12.dp)) {
                        item {
                            Text(
                                text = receiptText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color.Black,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isAr) "إغلاق" else "Close")
                    }
                    Button(
                        onClick = onPrint,
                        colors = ButtonDefaults.buttonColors(containerColor = MokhtarCrimson),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isAr) "طباعة حرارية" else "Thermal Print")
                    }
                }
            }
        }
    }
}

fun formatTimeAgo(timeMillis: Long, isAr: Boolean): String {
    val diffSec = (System.currentTimeMillis() - timeMillis) / 1000
    return when {
        diffSec < 60 -> if (isAr) "الآن" else "just now"
        diffSec < 3600 -> {
            val min = diffSec / 60
            if (isAr) "منذ $min دقيقة" else "$min min ago"
        }
        else -> {
            val hours = diffSec / 3600
            if (isAr) "منذ $hours ساعة" else "$hours hr ago"
        }
    }
}
