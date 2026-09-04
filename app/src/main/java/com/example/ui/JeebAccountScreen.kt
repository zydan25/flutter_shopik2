package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoreOrder
import com.example.data.model.UserSession
import com.example.data.model.WalletAccount
import com.example.data.model.WalletTransaction
import kotlinx.coroutines.launch

/**
 * واجهة حسابي الرقمية الشاملة
 * خالية تماماً من كلمة "محفظة جيب"
 * تحتوي على: المنيو الجانبي (Drawer)، زر المزامنة، أيقونات (التقارير، شبكة السداد، الخدمات، كروت الشبكات، شحن الألعاب، شحن البرامج)،
 * وواجهة تغذية الحساب برقم الهاتف والمبلغ والكود.
 */
@Composable
fun JeebAccountScreen(
    userSession: UserSession,
    wallet: WalletAccount,
    transactions: List<WalletTransaction>,
    orders: List<StoreOrder>,
    djangoUrl: String,
    formatMoney: (Double) -> String,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDepositClick: () -> Unit,
    onTransferClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onDjangoSettingsClick: () -> Unit,
    onOpenPaymentNetwork: () -> Unit = {},
    onSyncBalance: () -> Unit = {},
    onFeedAccountSubmit: (phone: String, amount: Double, code: String) -> Unit = { _, _, _ -> },
    onOpenAddresses: () -> Unit = {},
    onOpenInvite: () -> Unit = {},
    onOpenSupport: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenVendorPortal: () -> Unit = {},
    onOpenAdminPortal: () -> Unit = {},
    onOpenTrends: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onOpenServices: () -> Unit = {},
    onOpenNetworkCards: () -> Unit = {},
    onOpenGames: () -> Unit = {},
    onOpenPrograms: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isBalanceVisible by remember { mutableStateOf(true) }
    var selectedCurrencyIndex by remember { mutableStateOf(0) }
    var quickServiceMessage by remember { mutableStateOf<String?>(null) }
    var showReportsDialog by remember { mutableStateOf(false) }
    var showFeedAccountModal by remember { mutableStateOf(false) }
    var showContactUsDialog by remember { mutableStateOf(false) }
    var showDigitalServiceDialog by remember { mutableStateOf<String?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight(),
                drawerContainerColor = Color.White
            ) {
                // Side Drawer Header with Profile
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            IconButton(onClick = {
                                coroutineScope.launch { drawerState.close() }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق المنيو",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (userSession.isLoggedIn) userSession.fullName else "زائر مرحب بك",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (userSession.isLoggedIn) userSession.phone else "تطبيق سوق بلس & سداد كاش",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "حساب موثق رسمي",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFFFD54F),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Drawer Menu Items
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            label = { Text("الملف الشخصي والبيانات", fontWeight = FontWeight.SemiBold) },
                            selected = false,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                quickServiceMessage = "الملف الشخصي: الاسم ${userSession.fullName} - رقم الحساب ${wallet.accountNumber} - حالة التوثيق: معتمد ومفعل"
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }

                    item {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color(0xFF2E7D32)) },
                            label = { Text("تواصل معنا والدعم الفني", fontWeight = FontWeight.SemiBold) },
                            selected = false,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                showContactUsDialog = true
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }

                    item {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Assessment, contentDescription = null, tint = Color(0xFF0288D1)) },
                            label = { Text("التقارير وسجل العمليات", fontWeight = FontWeight.SemiBold) },
                            selected = false,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                showReportsDialog = true
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }

                    item {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFFC62828)) },
                            label = { Text("شبكة سداد الاتصالات", fontWeight = FontWeight.SemiBold) },
                            selected = false,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                onOpenPaymentNetwork()
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }

                    item {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color(0xFF7B1FA2)) },
                            label = { Text("الأسئلة الشائعة والمساعدة", fontWeight = FontWeight.SemiBold) },
                            selected = false,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                quickServiceMessage = "المساعدة: يمكنك تغذية حسابك واستخدامه في سداد اتصالات يمن موبايل، سبأفون، يو، والشراء من جميع المتاجر بضمان كامل."
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }

                    item {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Dns, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            label = { Text("إعدادات خادم جانغو (Django API)", fontWeight = FontWeight.SemiBold) },
                            selected = false,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                onDjangoSettingsClick()
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    if (userSession.isLoggedIn) {
                        item {
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                label = { Text("تسجيل الخروج", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                                selected = false,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    onLogoutClick()
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    } else {
                        item {
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Login, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                label = { Text("تسجيل الدخول", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                                selected = false,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    onLoginClick()
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                        item {
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                label = { Text("إنشاء حساب جديد", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold) },
                                selected = false,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    onRegisterClick()
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    }
                }
            }
        }
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9FC)),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Account Top Bar with Side Drawer Menu Button
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Side Drawer Menu Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch { drawerState.open() }
                                },
                                modifier = Modifier.testTag("account_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "المنيو الجانبي",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Text(
                                text = "حسابي الرقمي",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Right: Contact us shortcut & Login/Logout
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(onClick = { showContactUsDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.SupportAgent,
                                    contentDescription = "تواصل معنا",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (userSession.isLoggedIn) {
                                OutlinedButton(
                                    onClick = onLogoutClick,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = "خروج",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("خروج", style = MaterialTheme.typography.labelSmall)
                                }
                            } else {
                                Button(
                                    onClick = onLoginClick,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("دخول", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }
            }

            // 1.5 Login / Register banner when not logged in
            if (!userSession.isLoggedIn) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Login,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "تسجيل الدخول أو إنشاء حساب",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "سجل حسابك للربط المباشر مع السيرفر والاطلاع على رصيدك الحقيقي ونقاطك",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onLoginClick,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("تسجيل الدخول", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = onRegisterClick,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("إنشاء حساب جديد", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    // User info banner showing real governorate and points
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "الحساب الموثق: ${userSession.fullName}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                )
                                Text(
                                    text = "الهاتف: ${userSession.phone} • المحافظة: ${userSession.governorate.ifBlank { "صنعاء" }}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF2E7D32))
                                )
                            }
                            Surface(
                                color = Color(0xFF2E7D32),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${userSession.pointsBalance} نقطة ولاء",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Digital Account Card with Sync Button
            item {
                DigitalAccountCard(
                    wallet = wallet,
                    isBalanceVisible = isBalanceVisible,
                    selectedCurrencyIndex = selectedCurrencyIndex,
                    onCurrencySelected = { selectedCurrencyIndex = it },
                    onToggleVisibility = { isBalanceVisible = !isBalanceVisible },
                    onSyncBalance = onSyncBalance,
                    formatMoney = formatMoney,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // 3. User Requested Core Icons Grid:
            // "التقارير" | "شبكة السداد" | "الخدمات" | "كروت الشبكات" | "شحن الألعاب" | "شحن البرامج"
            item {
                UserRequestedServicesSection(
                    ordersCount = orders.size,
                    onOpenReports = { showReportsDialog = true },
                    onOpenPaymentNetwork = onOpenPaymentNetwork,
                    onOpenServices = onOpenServices,
                    onOpenNetworkCards = onOpenNetworkCards,
                    onOpenGames = onOpenGames,
                    onOpenApps = onOpenPrograms,
                    onFeedAccountClick = { showFeedAccountModal = true },
                    onTransferClick = onTransferClick,
                    onOrdersClick = onOrdersClick
                )
            }

            // 4. Quick Account Actions (تغذية الحساب، تحويل مالي، طلباتي)
            item {
                AccountQuickButtonsBar(
                    onFeedAccountClick = { showFeedAccountModal = true },
                    onTransferClick = onTransferClick,
                    onOrdersClick = onOrdersClick
                )
            }

            // 5. Recent Wallet Transactions Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "كشف الحساب والعمليات الأخيرة",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = { showReportsDialog = true }) {
                        Text("عرض التقرير الكامل", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            items(transactions) { tx ->
                WalletTransactionRow(
                    tx = tx,
                    formatMoney = formatMoney,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }

    // Modal: Feed Account via Gateway / Code (مطلوب: بها رقم الهاتف والمبلغ والكود وزر غذي حسابك)
    if (showFeedAccountModal) {
        FeedAccountDialog(
            onDismiss = { showFeedAccountModal = false },
            onConfirm = { phone, amount, code ->
                onFeedAccountSubmit(phone, amount, code)
                showFeedAccountModal = false
            }
        )
    }

    // Modal: Reports and Statement
    if (showReportsDialog) {
        ReportsDialog(
            wallet = wallet,
            transactions = transactions,
            orders = orders,
            formatMoney = formatMoney,
            onDismiss = { showReportsDialog = false }
        )
    }

    // Modal: Contact Us
    if (showContactUsDialog) {
        ContactUsDialog(onDismiss = { showContactUsDialog = false })
    }

    // Modal: Digital Services (كروت الشبكات، شحن الألعاب، شحن البرامج، الخدمات)
    if (showDigitalServiceDialog != null) {
        DigitalServiceActionDialog(
            serviceTitle = showDigitalServiceDialog ?: "",
            onDismiss = { showDigitalServiceDialog = null },
            onActionSelected = { serviceItem ->
                quickServiceMessage = "تم اختيار خدمة $serviceItem. يمكنك السداد الفوري من رصيد حسابك المتاح!"
                showDigitalServiceDialog = null
            }
        )
    }

    // Quick Alert Dialog
    if (quickServiceMessage != null) {
        AlertDialog(
            onDismissRequest = { quickServiceMessage = null },
            title = {
                Text(text = "إشعار الخدمة", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = quickServiceMessage ?: "")
            },
            confirmButton = {
                Button(onClick = { quickServiceMessage = null }) {
                    Text("حسناً")
                }
            }
        )
    }
}

/**
 * البطاقة الرقمية للحساب مع زر المزامنة الصريح
 */
@Composable
fun DigitalAccountCard(
    wallet: WalletAccount,
    isBalanceVisible: Boolean,
    selectedCurrencyIndex: Int,
    onCurrencySelected: (Int) -> Unit,
    onToggleVisibility: () -> Unit,
    onSyncBalance: () -> Unit,
    formatMoney: (Double) -> String,
    modifier: Modifier = Modifier
) {
    var rotationAngle by remember { mutableStateOf(0f) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0F172A), // Modern Midnight Slate
                            Color(0xFF1E293B),
                            Color(0xFF0369A1)  // Ocean Blue
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Row: Brand & Sync Button & Eye toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "حسابي الرقمي المعتمد",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Verified Smart Account",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }

                    // Action buttons: Sync Button + Eye Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // زر المزامنة الصريح المطلوب بالاسم
                        val animatedRotation by animateFloatAsState(
                            targetValue = rotationAngle,
                            animationSpec = tween(durationMillis = 600),
                            label = "card_sync"
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier
                                .size(34.dp)
                                .clickable {
                                    rotationAngle += 360f
                                    onSyncBalance()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "مزامنة الرصيد",
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .rotate(animatedRotation)
                                )
                            }
                        }

                        IconButton(
                            onClick = onToggleVisibility,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "إظهار/إخفاء الرصيد",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Balance Label & Amount
                Text(
                    text = "الرصيد المتاح للاستخدام الفوري:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                val (currentAmount, currentCurrency) = when (selectedCurrencyIndex) {
                    1 -> Pair(wallet.balanceSar, "ريال سعودي")
                    2 -> Pair(wallet.balanceUsd, "دولار أمريكي")
                    else -> Pair(wallet.balanceYer, "ريال يمني")
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isBalanceVisible) formatMoney(currentAmount) else "••••••••",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = currentCurrency,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Currency switcher chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ر.ي (يمني)", "ر.س (سعودي)", "دولار ($)").forEachIndexed { index, label ->
                        val isSelected = selectedCurrencyIndex == index
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.1f),
                            modifier = Modifier
                                .clickable { onCurrencySelected(index) }
                                .border(
                                    width = if (isSelected) 1.dp else 0.dp,
                                    color = if (isSelected) Color(0xFFFFD54F) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom: Account Number & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "رقم الحساب:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        )
                        Text(
                            text = wallet.accountNumber,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 2.sp
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "جاهز للسداد والتسوق",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * شبكة الأيقونات المطلوبة بالاسم من المستخدم:
 * "التقارير" | "شبكة السداد" | "الخدمات" | "كروت الشبكات" | "شحن الألعاب" | "شحن البرامج"
 */
@Composable
fun UserRequestedServicesSection(
    ordersCount: Int,
    onOpenReports: () -> Unit,
    onOpenPaymentNetwork: () -> Unit,
    onOpenServices: () -> Unit,
    onOpenNetworkCards: () -> Unit,
    onOpenGames: () -> Unit,
    onOpenApps: () -> Unit,
    onFeedAccountClick: () -> Unit,
    onTransferClick: () -> Unit,
    onOrdersClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "الخدمات والمدفوعات الإلكترونية",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // الصف الأول: شبكة السداد | التقارير | الخدمات
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AccountFeatureGridItem(
                    icon = Icons.Default.Payments,
                    title = "شبكة السداد",
                    tint = Color(0xFFC62828), // Telecom red
                    onClick = onOpenPaymentNetwork
                )

                AccountFeatureGridItem(
                    icon = Icons.Default.Assessment,
                    title = "التقارير",
                    tint = Color(0xFF0288D1), // Cyan/Blue
                    onClick = onOpenReports
                )

                AccountFeatureGridItem(
                    icon = Icons.Default.Apps,
                    title = "الخدمات",
                    tint = Color(0xFF7B1FA2), // Purple
                    onClick = onOpenServices
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // الصف الثاني: كروت الشبكات | شحن الألعاب | شحن البرامج
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AccountFeatureGridItem(
                    icon = Icons.Default.Wifi,
                    title = "كروت الشبكات",
                    tint = Color(0xFF00796B), // Teal
                    onClick = onOpenNetworkCards
                )

                AccountFeatureGridItem(
                    icon = Icons.Default.SportsEsports,
                    title = "شحن الألعاب",
                    tint = Color(0xFFE65100), // Orange
                    onClick = onOpenGames
                )

                AccountFeatureGridItem(
                    icon = Icons.Default.Phone,
                    title = "شحن البرامج",
                    tint = Color(0xFFC2185B), // Pink
                    onClick = onOpenApps
                )
            }
        }
    }
}

@Composable
fun AccountFeatureGridItem(
    icon: ImageVector,
    title: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(tint.copy(alpha = 0.12f))
                .border(1.dp, tint.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            ),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * شريط الإجراءات السريعة (تغذية الحساب، تحويل، طلباتي)
 */
@Composable
fun AccountQuickButtonsBar(
    onFeedAccountClick: () -> Unit,
    onTransferClick: () -> Unit,
    onOrdersClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // تغذية الحساب
        Button(
            onClick = onFeedAccountClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            Icon(imageVector = Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("تغذية الحساب", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        }

        // تحويل مالي
        OutlinedButton(
            onClick = onTransferClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("تحويل مالي", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        }

        // طلباتي
        OutlinedButton(
            onClick = onOrdersClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("طلباتي", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/**
 * حوار تغذية الحساب التفاعلي (مطلوب: رقم الهاتف والمبلغ والكود وزر غذي حسابك ويرسل طلب وتظهر النتيجة)
 */
@Composable
fun FeedAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (phone: String, amount: Double, code: String) -> Unit
) {
    var phone by remember { mutableStateOf("770123456") }
    var amountText by remember { mutableStateOf("50000") }
    var code by remember { mutableStateOf("1234") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(text = "تغذية حسابي الإلكتروني", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "أدخل بيانات الإيداع عبر رقم الهاتف والمبلغ والكود لتغذية رصيد حسابك فورياً:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                )

                // 1. رقم الهاتف
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف المسجل") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. المبلغ
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("المبلغ المطلوب إيداعه (ر.ي)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. الكود السري / كود الإيداع
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("كود المحفظة / رمز التحقق") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
                    )
                }

                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("جاري إرسال طلب التغذية واعتماد الرصيد...")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (phone.isBlank()) {
                        errorMessage = "يرجى كتابة رقم الهاتف"
                    } else if (amt <= 0) {
                        errorMessage = "يرجى إدخال مبلغ صحيح"
                    } else if (code.isBlank()) {
                        errorMessage = "يرجى إدخال كود التحقق"
                    } else {
                        isLoading = true
                        onConfirm(phone, amt, code)
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("غذي حسابك", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("إلغاء")
            }
        }
    )
}

/**
 * حوار التقارير وسجل العمليات وكشف الحساب
 */
@Composable
fun ReportsDialog(
    wallet: WalletAccount,
    transactions: List<WalletTransaction>,
    orders: List<StoreOrder>,
    formatMoney: (Double) -> String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(text = "تقرير الحساب والمصروفات", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // Summary card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("ملخص الرصيد والعمليات:", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• الرصيد الإجمالي: ${formatMoney(wallet.balanceYer)} ر.ي")
                            Text("• إجمالي العمليات المسجلة: ${transactions.size} عملية")
                            Text("• إجمالي طلبات المتاجر: ${orders.size} طلبات")
                        }
                    }
                }

                item {
                    Text(
                        text = "تفاصيل العمليات المسجلة:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                items(transactions) { tx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = tx.title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = tx.date, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                        }
                        Text(
                            text = "${if (tx.isPositive) "+" else "-"}${formatMoney(tx.amount)} ر.ي",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (tx.isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("إغلاق التقرير")
            }
        }
    )
}

/**
 * حوار تواصل معنا
 */
@Composable
fun ContactUsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32)
                )
                Text(text = "تواصل معنا - خدمة العملاء", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("نحن هنا لخدمتك على مدار الساعة عبر القنوات التالية:")

                Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text("الاتصال المباشر: 771234567 / 01445566", fontWeight = FontWeight.SemiBold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                            Text("واتساب الدعم السريع: +967 770123456", fontWeight = FontWeight.SemiBold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(18.dp))
                            Text("البريد: support@souqplus.ye", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("حسناً")
            }
        }
    )
}

/**
 * حوار تفاعلي لاختيار خدمات "كروت الشبكات" و "شحن الألعاب" و "شحن البرامج" و "الخدمات"
 */
@Composable
fun DigitalServiceActionDialog(
    serviceTitle: String,
    onDismiss: () -> Unit,
    onActionSelected: (String) -> Unit
) {
    val items = when (serviceTitle) {
        "كروت الشبكات" -> listOf(
            "كارت وايفاي محلي 100 ر.ي (ساعة)",
            "كارت وايفاي فئة 300 ر.ي (يومي 2GB)",
            "كارت وايفاي أسبوعي 1000 ر.ي (7GB)",
            "شحن رصيد شبكات المايكروتيك 2000 ر.ي"
        )
        "شحن الألعاب" -> listOf(
            "ببجي موبايل 60 شدة UC (650 ر.ي)",
            "ببجي موبايل 325 شدة UC (3200 ر.ي)",
            "فري فاير 100+10 جوهرة (550 ر.ي)",
            "روبلوكس 800 روبكس (5400 ر.ي)"
        )
        "شحن البرامج" -> listOf(
            "شحن تيك توك 70 عملة (700 ر.ي)",
            "شحن بيجو لايف 100 ماسة (1200 ر.ي)",
            "اشتراك تلجرام بريميوم شهر (2800 ر.ي)",
            "شحن لايكي Likee 150 ماسة (1400 ر.ي)"
        )
        else -> listOf(
            "سداد فواتير الكهرباء والمياه",
            "سداد رسوم الجامعات والتعليم",
            "استخراج بطاقات الدفع الإلكتروني",
            "خدمات التحويل السريع للشركات"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "قائمة $serviceTitle", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("اختر الباقة أو الفئة المطلوبة للسداد الفوري من رصيدك:")
                items.forEach { itemText ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onActionSelected(itemText) }
                    ) {
                        Text(
                            text = itemText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

/**
 * صف لعرض العملية المالية في كشف الحساب
 */
@Composable
fun WalletTransactionRow(
    tx: WalletTransaction,
    formatMoney: (Double) -> String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (tx.isPositive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tx.isPositive) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (tx.isPositive) Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = tx.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = tx.date,
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                    )
                }
            }
            Text(
                text = "${if (tx.isPositive) "+" else "-"}${formatMoney(tx.amount)} ${tx.currency}",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (tx.isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            )
        }
    }
}

