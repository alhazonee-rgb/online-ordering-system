package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AppLanguage
import com.example.ui.theme.MokhtarCrimson
import com.example.ui.theme.MokhtarGold
import com.example.ui.theme.MokhtarGoldLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: AlMokhtarViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val isAr = language == AppLanguage.ARABIC
    val orders by viewModel.repository.orders.collectAsState()

    val totalRevenueSar = orders.sumOf { it.totalSar }
    val totalOrdersCount = orders.size
    val avgOrderValue = if (totalOrdersCount > 0) totalRevenueSar / totalOrdersCount else 0.0

    val mansourahOrders = orders.filter { it.branchId == "mansourah" }
    val aziziyahOrders = orders.filter { it.branchId == "aziziyah" }

    var showCsvModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isAr) "تقارير المبيعات والأداء المالي" else "Sales & Branch Analytics",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (isAr)
                            "نظرة شاملة ومقارنة بين فرعي المنصورة والعزيزية بالرياض مع إمكانية تصدير ملفات CSV المحاسبية."
                        else
                            "Comprehensive multi-branch revenue analytics and CSV export.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        // Summary KPI Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = if (isAr) "إجمالي المبيعات" else "Total Revenue",
                    value = "%.2f SAR".format(totalRevenueSar),
                    color = MokhtarCrimson,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = if (isAr) "عدد الطلبات" else "Total Orders",
                    value = "$totalOrdersCount",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = if (isAr) "متوسط الطلب" else "Avg Order",
                    value = "%.2f SAR".format(avgOrderValue),
                    color = MokhtarGold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Branch Breakdown Comparison
        item {
            Text(
                text = if (isAr) "المبيعات حسب الفرع:" else "Sales by Branch:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Mansourah
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = if (isAr) "فرع المنصورة (طريق الخرج)" else "Al Mansourah Branch", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "${mansourahOrders.size} ${if (isAr) "طلبات" else "orders"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = "%.2f SAR".format(mansourahOrders.sumOf { it.totalSar }),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = MokhtarCrimson
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    // Aziziyah
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = if (isAr) "فرع العزيزية (طريق الخرج)" else "Al Aziziyah Branch", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "${aziziyahOrders.size} ${if (isAr) "طلبات" else "orders"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = "%.2f SAR".format(aziziyahOrders.sumOf { it.totalSar }),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = MokhtarCrimson
                        )
                    }
                }
            }
        }

        // CSV Export Button
        item {
            Button(
                onClick = { showCsvModal = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MokhtarCrimson)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAr) "توليد وتصدير ملف التقرير المحاسبي (CSV)" else "Generate & Export Accounting CSV",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showCsvModal) {
        val csvContent = remember(orders) {
            val sb = StringBuilder()
            sb.appendLine("OrderNumber,BranchID,BranchName,TableNumber,CreatedAt,ItemsCount,SubtotalSAR,VATSAR,TotalSAR,Status,CustomerName,AllergyNotes")
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            for (o in orders) {
                sb.appendLine(
                    "${o.orderNumber},${o.branchId},\"${o.branchNameEn}\",${o.tableNumber},\"${df.format(Date(o.createdAtMillis))}\",${o.items.size},${o.subtotalSar},${o.vatSar},${o.totalSar},${o.status.name},\"${o.customerName}\",\"${o.allergyNotes}\""
                )
            }
            sb.toString()
        }

        Dialog(onDismissRequest = { showCsvModal = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isAr) "ملف التقرير المحاسبي (CSV Data)" else "Orders Accounting CSV Data",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Surface(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        LazyColumn(modifier = Modifier.padding(8.dp)) {
                            item {
                                Text(
                                    text = csvContent,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showCsvModal = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MokhtarCrimson),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isAr) "إغلاق" else "Close")
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = color
            )
        }
    }
}
