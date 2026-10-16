package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ThermalEscPosUtil
import com.example.model.AppLanguage
import com.example.model.RestaurantOrder
import com.example.ui.theme.MokhtarCrimson
import com.example.ui.theme.MokhtarSuccess

@Composable
fun PrinterHubScreen(
    viewModel: AlMokhtarViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val isAr = language == AppLanguage.ARABIC
    val branches = viewModel.repository.branches
    val printerJobs by viewModel.repository.printerJobs.collectAsState()
    val orders by viewModel.repository.orders.collectAsState()

    var testPrintNotice by remember { mutableStateOf<String?>(null) }

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
                        text = if (isAr) "مركز إدارة الطابعات الحرارية ووكيل الطباعة (ESC/POS)" else "Thermal Printing Hub & Windows Agent",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (isAr)
                            "مراقبة اتصال طابعات الفواتير بالمطابخ (المنصورة والعزيزية) وإرسال أوامر الطباعة المباشرة عبر الشبكة أو USB."
                        else
                            "Monitor kitchen receipt printers across branches and manage automatic ESC/POS spooling.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        if (testPrintNotice != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MokhtarSuccess.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MokhtarSuccess)
                        Text(text = testPrintNotice!!, color = MokhtarSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Branch Printers Status Cards
        item {
            Text(
                text = if (isAr) "طابعات الفروع المتصلة:" else "Configured Branch Printers:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        items(branches) { branch ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAr) branch.nameAr else branch.nameEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "IP: ${branch.printerIp}:${branch.printerPort} (ESC/POS 80mm)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(MokhtarSuccess, RoundedCornerShape(4.dp))
                            )
                            Text(
                                text = if (isAr) "متصل وجاهز للطباعة الفورية" else "Online & Ready",
                                fontSize = 11.sp,
                                color = MokhtarSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val demoOrder = orders.firstOrNull { it.branchId == branch.id } ?: orders.firstOrNull()
                            if (demoOrder != null) {
                                viewModel.reprintKitchenTicket(demoOrder)
                                testPrintNotice = if (isAr)
                                    "تم إرسال تذكرة اختبار ناجحة إلى ${branch.nameAr}"
                                else
                                    "Test print dispatched successfully to ${branch.nameEn}"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MokhtarCrimson),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isAr) "طباعة تجريبية" else "Test Print", fontSize = 12.sp)
                    }
                }
            }
        }

        // Live Print Jobs Queue
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isAr) "سجل مهام الطباعة الحديثة (${printerJobs.size}):" else "Recent Print Spooler Jobs (${printerJobs.size}):",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        items(printerJobs.take(10)) { job ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = job.printerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Surface(color = MokhtarSuccess.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = job.status,
                                color = MokhtarSuccess,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Job ID: ${job.id.take(8)} • Order: ${job.orderId.take(8)}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
