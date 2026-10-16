package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppLanguage
import com.example.ui.theme.MokhtarCharcoal
import com.example.ui.theme.MokhtarCrimson
import com.example.ui.theme.MokhtarCrimsonDark
import com.example.ui.theme.MokhtarGold
import com.example.ui.theme.MokhtarGoldLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: AlMokhtarViewModel
) {
    val language by viewModel.language.collectAsState()
    val navMode by viewModel.navMode.collectAsState()
    val isAdminPortalActive by viewModel.isAdminPortalActive.collectAsState()
    val isAr = language == AppLanguage.ARABIC

    val layoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_restaurant_logo),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.5.dp, MokhtarGold, CircleShape)
                                    .padding(3.dp)
                            )
                            Column {
                                Text(
                                    text = if (isAdminPortalActive) {
                                        if (isAr) "لوحة إدارة مطعم المختار" else "Al Mokhtar Admin Portal"
                                    } else {
                                        if (isAr) "مطعم المختار - الرياض" else "Al Mokhtar Restaurant"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                if (isAdminPortalActive) {
                                    Text(
                                        text = if (isAr) "التحكم بالأسعار والعروض والمطبخ" else "Central Menu, Offers & KDS",
                                        fontSize = 10.sp,
                                        color = MokhtarGoldLight
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        if (isAdminPortalActive) {
                            // Exit Admin Portal Button
                            FilledTonalButton(
                                onClick = { viewModel.exitAdminPortal() },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MokhtarGold,
                                    contentColor = MokhtarCharcoal
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isAr) "عرض العميل" else "Customer View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Language Toggle Switcher (عربي / EN)
                        TextButton(
                            onClick = { viewModel.toggleLanguage() },
                            colors = ButtonDefaults.textButtonColors(contentColor = MokhtarGoldLight),
                            modifier = Modifier.testTag("language_toggle_button")
                        ) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isAr) "English" else "عربي",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (isAdminPortalActive) MokhtarCrimsonDark else MokhtarCrimson,
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                // The bottom navigation bar is ONLY visible to the admin/staff inside Admin Portal!
                // Customers see 100% clean customer ordering interface!
                if (isAdminPortalActive) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        NavigationBarItem(
                            selected = navMode == AppNavMode.KDS_ADMIN,
                            onClick = { viewModel.setNavMode(AppNavMode.KDS_ADMIN) },
                            icon = { Icon(Icons.Default.Kitchen, contentDescription = null) },
                            label = { Text(if (isAr) "المطبخ KDS" else "KDS", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = navMode == AppNavMode.MENU_MGMT,
                            onClick = { viewModel.setNavMode(AppNavMode.MENU_MGMT) },
                            icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                            label = { Text(if (isAr) "الأصناف والعروض" else "Stock & Offers", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = navMode == AppNavMode.QR_GENERATOR,
                            onClick = { viewModel.setNavMode(AppNavMode.QR_GENERATOR) },
                            icon = { Icon(Icons.Default.QrCode, contentDescription = null) },
                            label = { Text(if (isAr) "رموز الطاولات" else "Table QR", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = navMode == AppNavMode.PRINTER_HUB,
                            onClick = { viewModel.setNavMode(AppNavMode.PRINTER_HUB) },
                            icon = { Icon(Icons.Default.Print, contentDescription = null) },
                            label = { Text(if (isAr) "الطابعات" else "Printers", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = navMode == AppNavMode.REPORTS,
                            onClick = { viewModel.setNavMode(AppNavMode.REPORTS) },
                            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                            label = { Text(if (isAr) "التقارير" else "Reports", fontSize = 11.sp) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Crossfade(
                targetState = if (!isAdminPortalActive) AppNavMode.CUSTOMER else navMode,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { mode ->
                when (mode) {
                    AppNavMode.CUSTOMER -> CustomerOrderScreen(viewModel = viewModel)
                    AppNavMode.KDS_ADMIN -> AdminKdsScreen(viewModel = viewModel)
                    AppNavMode.MENU_MGMT -> MenuManagementScreen(viewModel = viewModel)
                    AppNavMode.QR_GENERATOR -> QrGeneratorScreen(viewModel = viewModel)
                    AppNavMode.PRINTER_HUB -> PrinterHubScreen(viewModel = viewModel)
                    AppNavMode.REPORTS -> ReportsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
