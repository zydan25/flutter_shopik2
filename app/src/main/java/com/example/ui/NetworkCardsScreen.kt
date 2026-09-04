package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserSession
import kotlin.random.Random

data class TelecomNetwork(
    val id: String,
    val name: String,
    val enName: String,
    val prefix: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val icon: ImageVector,
    val description: String
)

data class NetworkCardPackage(
    val id: String,
    val networkId: String,
    val category: String, // "كروت شحن", "باقات فورجي 4G", "باقات اتصال", "تسديد مباشر"
    val title: String,
    val description: String,
    val priceYer: Double,
    val validity: String = "صالحة لمدة شهر",
    val bonus: String? = null
)

/**
 * واجهة كروت الشبكات لاختيار الشبكة ثم إظهار الفئات التابعة لها
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkCardsScreen(
    userSession: UserSession,
    onBackClick: () -> Unit,
    formatMoney: (Double) -> String,
    onPurchaseCard: (cardName: String, price: Double, targetPhone: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val networks = remember {
        listOf(
            TelecomNetwork(
                id = "yemen_mobile",
                name = "يمن موبايل",
                enName = "Yemen Mobile",
                prefix = "77 / 78",
                primaryColor = Color(0xFFC62828),
                secondaryColor = Color(0xFFB71C1C),
                icon = Icons.Default.PhoneAndroid,
                description = "الشبكة الوطنية الرائدة - 4G VoLTE و CDMA"
            ),
            TelecomNetwork(
                id = "you_telecom",
                name = "يو للاتصالات",
                enName = "YOU Telecom",
                prefix = "73",
                primaryColor = Color(0xFFF57C00),
                secondaryColor = Color(0xFFE65100),
                icon = Icons.Default.SignalCellularAlt,
                description = "شبكة يو - باقات فورجي وعروض سمارت"
            ),
            TelecomNetwork(
                id = "sabafon",
                name = "سبأفون",
                enName = "SabaFon",
                prefix = "71",
                primaryColor = Color(0xFF1565C0),
                secondaryColor = Color(0xFF0D47A1),
                icon = Icons.Default.Language,
                description = "شبكة سبأفون - أصالة وتواصل في كل اليمن"
            ),
            TelecomNetwork(
                id = "y_telecom",
                name = "واي للاتصالات",
                enName = "Y Telecom",
                prefix = "70",
                primaryColor = Color(0xFF6A1B9A),
                secondaryColor = Color(0xFF4A148C),
                icon = Icons.Default.Speed,
                description = "شبكة واي 4G إنترنت فائق السرعة"
            ),
            TelecomNetwork(
                id = "yemen_net",
                name = "يمن نت ADSL / 4G",
                enName = "YemenNet",
                prefix = "01 / 02...",
                primaryColor = Color(0xFF00838F),
                secondaryColor = Color(0xFF006064),
                icon = Icons.Default.Router,
                description = "الإنترنت المنزلي ADSL ويمن فورجي وفايبر"
            ),
            TelecomNetwork(
                id = "local_wifi",
                name = "شبكات الوايفاي المحلية",
                enName = "Local Wi-Fi",
                prefix = "حارات اليمن",
                primaryColor = Color(0xFF2E7D32),
                secondaryColor = Color(0xFF1B5E20),
                icon = Icons.Default.Wifi,
                description = "كروت مايكروتك وشبكات الوايفاي في جميع المحافظات"
            )
        )
    }

    var selectedNetwork by remember { mutableStateOf(networks[0]) }

    val networkCategories = listOf("الكل", "كروت شحن", "باقات فورجي 4G", "باقات اتصال", "تسديد مباشر")
    var selectedCategoryFilter by remember { mutableStateOf("الكل") }

    val allPackages = remember {
        listOf(
            // Yemen Mobile
            NetworkCardPackage("ym_1", "yemen_mobile", "كروت شحن", "كرت شحن 200 ريال", "رصيد أساسي فوري مع فترة صلاحية", 220.0, "صلاحية شهر"),
            NetworkCardPackage("ym_2", "yemen_mobile", "كروت شحن", "كرت شحن 500 ريال", "رصيد أساسي يمن موبايل فوري", 550.0, "صلاحية 60 يوم"),
            NetworkCardPackage("ym_3", "yemen_mobile", "كروت شحن", "كرت شحن 1,000 ريال", "كرت شحن رقمي فوري مع كود التعبئة", 1100.0, "صلاحية 90 يوم", "رصيد إضافي 5%"),
            NetworkCardPackage("ym_4", "yemen_mobile", "كروت شحن", "كرت شحن 2,000 ريال", "كرت الفئة الكبرى رصيد مباشر", 2200.0, "صلاحية 120 يوم"),
            NetworkCardPackage("ym_5", "yemen_mobile", "باقات فورجي 4G", "باقة مزايا مكس الشهرية", "400 دقيقة اتصال + 400 رسالة + 2 جيجا إنترنت", 2400.0, "30 يوم", "الأكثر طلباً"),
            NetworkCardPackage("ym_6", "yemen_mobile", "باقات فورجي 4G", "باقة فورجي 12 جيجا الشهرية", "إنترنت فائق السرعة 4G VoLTE مع ترحيل الرصيد", 4800.0, "30 يوم"),
            NetworkCardPackage("ym_7", "yemen_mobile", "باقات فورجي 4G", "باقة فورجي 25 جيجا العملاقة", "تصفح وتحميل غير محدود وتغطية شاملة", 8500.0, "30 يوم", "عرض التوفير"),
            NetworkCardPackage("ym_8", "yemen_mobile", "باقات اتصال", "باقة هدايا دقائق 300 دقيقة", "اتصال لجميع شبكات يمن موبايل الداخلية", 1200.0, "15 يوم"),
            NetworkCardPackage("ym_9", "yemen_mobile", "تسديد مباشر", "شحن رصيد فوري مباشر (أي مبلغ)", "تسديد برقم الهاتف مباشرة مع إشعار SMS فوري", 1000.0, "فوري"),

            // YOU Telecom
            NetworkCardPackage("you_1", "you_telecom", "كروت شحن", "كرت شحن يو 500 ريال", "كرت رقمي فوري لشبكة يو", 550.0, "60 يوم"),
            NetworkCardPackage("you_2", "you_telecom", "كروت شحن", "كرت شحن يو 1,000 ريال", "كرت شحن يو فئة ألف ريال", 1100.0, "90 يوم"),
            NetworkCardPackage("you_3", "you_telecom", "كروت شحن", "كرت شحن يو 2,000 ريال", "كرت الفئة الذهبية لرصيد يو", 2200.0, "120 يوم"),
            NetworkCardPackage("you_4", "you_telecom", "باقات فورجي 4G", "باقة يو سمارت 6 جيجا", "إنترنت 4G سريع + 300 دقيقة داخل الشبكة", 2600.0, "30 يوم"),
            NetworkCardPackage("you_5", "you_telecom", "باقات فورجي 4G", "باقة يو ماكس 18 جيجا", "إنترنت فورجي مفتوح مع ترحيل الرصيد المتبقي", 6200.0, "30 يوم", "عرض خاص"),
            NetworkCardPackage("you_6", "you_telecom", "باقات اتصال", "باقة يو كلام لا ينتهي 500 دقيقة", "مكالمات غير محدودة داخل شبكة يو", 1500.0, "30 يوم"),
            NetworkCardPackage("you_7", "you_telecom", "تسديد مباشر", "تسديد فواتير ورصيد يو فوري", "شحن رصيد مباشر لخطوط الدفع المسبق والفواتير", 1000.0, "فوري"),

            // SabaFon
            NetworkCardPackage("sb_1", "sabafon", "كروت شحن", "كرت سبأفون 500 وحدة", "كرت شحن وحدات سبأفون الأصلية", 550.0, "60 يوم"),
            NetworkCardPackage("sb_2", "sabafon", "كروت شحن", "كرت سبأفون 1,000 وحدة", "كرت الشحن الفوري مع الكود", 1100.0, "90 يوم"),
            NetworkCardPackage("sb_3", "sabafon", "باقات فورجي 4G", "باقة سبأنت 4G الشهرية 8 جيجا", "إنترنت سريع وتصفح فوري", 3400.0, "30 يوم"),
            NetworkCardPackage("sb_4", "sabafon", "باقات اتصال", "باقة سبأفون تواصل 400 دقيقة", "دقائق اتصال ورسائل مجانية داخل الشبكة", 1600.0, "30 يوم"),
            NetworkCardPackage("sb_5", "sabafon", "تسديد مباشر", "تسديد مباشر لخطوط سبأفون", "تسديد فوري للرصيد والفواتير", 1000.0, "فوري"),

            // Y Telecom
            NetworkCardPackage("y_1", "y_telecom", "كروت شحن", "كرت شحن واي 1,000 ريال", "كرت رصيد واي الرقمي", 1100.0, "90 يوم"),
            NetworkCardPackage("y_2", "y_telecom", "باقات فورجي 4G", "باقة واي فورجي 10 جيجا", "باقة البيانات الفائقة", 3200.0, "30 يوم"),
            NetworkCardPackage("y_3", "y_telecom", "باقات فورجي 4G", "باقة واي فورجي 30 جيجا", "أكبر سعة تحميل مع راوتر وماي فاي", 8000.0, "30 يوم"),

            // YemenNet
            NetworkCardPackage("yn_1", "yemen_net", "كروت شحن", "كرت شحن سوبر نت ADSL فئة 1,000", "كرت تعبئة رصيد يمن نت المنزلي", 1050.0, "فوري"),
            NetworkCardPackage("yn_2", "yemen_net", "كروت شحن", "كرت شحن سوبر نت ADSL فئة 2,500", "كرت تعبئة رسمي بالرقم السري", 2600.0, "فوري"),
            NetworkCardPackage("yn_3", "yemen_net", "كروت شحن", "كرت شحن سوبر نت ADSL فئة 5,000", "كرت شحن سعة عالية ADSL", 5200.0, "فوري"),
            NetworkCardPackage("yn_4", "yemen_net", "باقات فورجي 4G", "تجديد باقة يمن فورجي 4G الشهرية", "تجديد فوري لحساب يمن فورجي برقم الهاتف الثابت", 4500.0, "30 يوم", "رسمي ومباشر"),

            // Local Wi-Fi
            NetworkCardPackage("wf_1", "local_wifi", "كروت شحن", "كرت وايفاي 1 جيجا (200 ريال)", "كرت شبكات ميكروتيك المحلية", 200.0, "24 ساعة"),
            NetworkCardPackage("wf_2", "local_wifi", "كروت شحن", "كرت وايفاي 3 جيجا (500 ريال)", "كرت تصفح عالي السرعة", 500.0, "3 أيام"),
            NetworkCardPackage("wf_3", "local_wifi", "كروت شحن", "كرت وايفاي أسبوعي مفتوح (1,000 ريال)", "تصفح مفتوح للشبكة المحلية", 1000.0, "7 أيام"),
            NetworkCardPackage("wf_4", "local_wifi", "كروت شحن", "كرت وايفاي شهري منزلي (3,000 ريال)", "اشتراك شهري في شبكة الحارة", 3000.0, "30 يوم")
        )
    }

    var selectedPackageToBuy by remember { mutableStateOf<NetworkCardPackage?>(null) }
    var targetPhoneNumber by remember { mutableStateOf(if (userSession.isLoggedIn) userSession.phone else "") }
    var generatedCardResult by remember { mutableStateOf<Pair<NetworkCardPackage, String>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "كروت وباقات الشبكات",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Header Info Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(selectedNetwork.primaryColor, selectedNetwork.secondaryColor)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = selectedNetwork.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = selectedNetwork.name,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Surface(
                                    color = Color.White.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = selectedNetwork.prefix,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedNetwork.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            )
                        }
                    }
                }
            }

            // Network Selector (Horizontal Tabs with Network Badges)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "اختر الشبكة:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(networks) { net ->
                            val isSelected = (net.id == selectedNetwork.id)
                            Card(
                                onClick = {
                                    selectedNetwork = net
                                    selectedCategoryFilter = "الكل"
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) net.primaryColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(
                                        if (isSelected) listOf(net.primaryColor, net.secondaryColor)
                                        else listOf(Color.LightGray.copy(alpha = 0.5f), Color.LightGray.copy(alpha = 0.5f))
                                    )
                                ),
                                modifier = Modifier
                                    .width(130.dp)
                                    .testTag("network_tab_${net.id}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) net.primaryColor else Color.LightGray.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = net.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Text(
                                        text = net.name,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) net.primaryColor else MaterialTheme.colorScheme.onSurface
                                        ),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Category Filter Chips for the Selected Network
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "فئات باقات وكروت ${selectedNetwork.name}:",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(networkCategories) { cat ->
                        val isSelected = (selectedCategoryFilter == cat)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = selectedNetwork.primaryColor,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // List of Cards / Packages for the selected network and category
            val filteredPackages = allPackages.filter {
                it.networkId == selectedNetwork.id && (selectedCategoryFilter == "الكل" || it.category == selectedCategoryFilter)
            }

            if (filteredPackages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد باقات متوفرة في هذا التصنيف حالياً",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            } else {
                items(filteredPackages) { pkg ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(selectedNetwork.primaryColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (pkg.category) {
                                        "كروت شحن" -> Icons.Default.CreditCard
                                        "باقات فورجي 4G" -> Icons.Default.Speed
                                        "باقات اتصال" -> Icons.Default.Call
                                        else -> Icons.Default.PhoneAndroid
                                    },
                                    contentDescription = null,
                                    tint = selectedNetwork.primaryColor,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = pkg.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    pkg.bonus?.let { b ->
                                        Surface(
                                            color = Color(0xFFE8F5E9),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = b,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color(0xFF2E7D32),
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = pkg.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${formatMoney(pkg.priceYer)} ر.ي",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "• ${pkg.validity}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    selectedPackageToBuy = pkg
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = selectedNetwork.primaryColor),
                                modifier = Modifier.testTag("buy_package_btn_${pkg.id}")
                            ) {
                                Text(
                                    text = "شراء الآن",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Purchase Dialog
    selectedPackageToBuy?.let { pkg ->
        AlertDialog(
            onDismissRequest = { selectedPackageToBuy = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = selectedNetwork.primaryColor
                    )
                    Text(
                        text = "تأكيد شراء ${pkg.title}",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "الشبكة: ${selectedNetwork.name} - ${pkg.category}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "السعر الإجمالي: ${formatMoney(pkg.priceYer)} ريال يمني",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    OutlinedTextField(
                        value = targetPhoneNumber,
                        onValueChange = { targetPhoneNumber = it },
                        label = { Text("رقم الهاتف / الحساب المراد الشحن له") },
                        placeholder = { Text("مثال: 770123456") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "سيتم خصم المبلغ من رصيد محفظتك، وتوليد كود الكرت الرقمي وإرساله في إشعار فوري.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pin = (1000..9999).random().toString() + "-" +
                                  (1000..9999).random().toString() + "-" +
                                  (1000..9999).random().toString() + "-" +
                                  (10..99).random().toString()
                        onPurchaseCard(pkg.title, pkg.priceYer, targetPhoneNumber.ifBlank { "رقمي" })
                        generatedCardResult = Pair(pkg, pin)
                        selectedPackageToBuy = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = selectedNetwork.primaryColor)
                ) {
                    Text("تأكيد الشراء الفوري", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedPackageToBuy = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Success Scratch Card Dialog
    generatedCardResult?.let { (pkg, pin) ->
        AlertDialog(
            onDismissRequest = { generatedCardResult = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32)
                    )
                    Text("تمت العملية وتوليد الكود بنجاح", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "تم شراء ${pkg.title} بنجاح لشبكة ${selectedNetwork.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "رقم الكرت السري (PIN CODE):",
                                style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray)
                            )
                            Text(
                                text = pin,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = selectedNetwork.primaryColor,
                                    letterSpacing = 2.sp
                                )
                            )
                            Text(
                                text = "طريقة الشحن: اتصل على *333* الكود ثم #",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { generatedCardResult = null }) {
                    Text("تم وحفظ الكود")
                }
            }
        )
    }
}
