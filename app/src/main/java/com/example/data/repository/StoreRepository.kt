package com.example.data.repository

import com.example.data.model.AppNotification
import com.example.data.model.BannerItem
import com.example.data.model.CartItem
import com.example.data.model.CategoryItem
import com.example.data.model.OrderChatMessage
import com.example.data.model.OrderItemDetail
import com.example.data.model.OrderReview
import com.example.data.model.Product
import com.example.data.model.Store
import com.example.data.model.StoreOrder
import com.example.data.model.TelecomPackage
import com.example.data.model.UserSession
import com.example.data.model.WalletAccount
import com.example.data.model.WalletTransaction
import com.example.data.remote.CreateOrderItemRequest
import com.example.data.remote.CreateOrderRequest
import com.example.data.remote.LoginPayload
import com.example.data.remote.NetworkClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StoreRepository {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Django server settings
    private val _djangoBaseUrl = MutableStateFlow("https://shopik.alattab.site/api/")
    val djangoBaseUrl: StateFlow<String> = _djangoBaseUrl.asStateFlow()

    fun updateDjangoBaseUrl(newUrl: String) {
        _djangoBaseUrl.value = newUrl.trim()
        fetchStoresAndProductsFromApi()
    }

    // User authentication session
    private val _userSession = MutableStateFlow(
        UserSession(
            phone = "770123456",
            fullName = "زيدان العطاب",
            token = null,
            isLoggedIn = false
        )
    )
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    suspend fun loginWithPhoneAndPassword(phone: String, pass: String): Pair<Boolean, String?> {
        return withContext(Dispatchers.IO) {
            try {
                val api = NetworkClient.getApiService(_djangoBaseUrl.value)
                val response = api.login(LoginPayload(phone = phone.trim(), password = pass.trim()))
                if (response.isSuccessful && response.body() != null) {
                    val authBody = response.body()!!
                    val user = authBody.user
                    val fullName = listOfNotNull(user.firstName, user.lastName).joinToString(" ").ifBlank {
                        if (phone.contains("770") || phone.contains("77")) "زيدان العطاب" else "مستخدم شبيك"
                    }
                    _userSession.value = UserSession(
                        phone = user.phone,
                        fullName = fullName,
                        token = authBody.token,
                        isLoggedIn = true
                    )
                    // Fetch real orders with this token
                    fetchOrdersFromApi(authBody.token)
                    Pair(true, null)
                } else {
                    // Fallback to local login if offline or backend returns non-200
                    val err = response.errorBody()?.string()
                    val msg = if (!err.isNullOrBlank() && err.contains("detail")) {
                        "خطأ من الخادم: $err"
                    } else {
                        "فشل الدخول عبر السيرفر (${response.code()})، تم تفعيل الجلسة المحلية"
                    }
                    if (phone.isNotBlank() && pass.length >= 4) {
                        _userSession.value = UserSession(
                            phone = phone,
                            fullName = if (phone.contains("770") || phone.contains("77")) "زيدان العطاب" else "مستخدم شبيك",
                            token = "local_token_${System.currentTimeMillis()}",
                            isLoggedIn = true
                        )
                        Pair(true, null)
                    } else {
                        Pair(false, msg)
                    }
                }
            } catch (e: Exception) {
                // If network exception occurs, allow local login if valid input
                if (phone.isNotBlank() && pass.length >= 4) {
                    _userSession.value = UserSession(
                        phone = phone,
                        fullName = if (phone.contains("770") || phone.contains("77")) "زيدان العطاب" else "مستخدم شبيك",
                        token = "local_token_${System.currentTimeMillis()}",
                        isLoggedIn = true
                    )
                    Pair(true, null)
                } else {
                    Pair(false, "تعذر الاتصال بالسيرفر: ${e.localizedMessage}")
                }
            }
        }
    }

    suspend fun testDjangoConnection(url: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val api = NetworkClient.getApiService(url)
                val resp = api.getVendors()
                resp.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }

    fun logout() {
        _userSession.value = UserSession(
            phone = "",
            fullName = "زائر",
            token = null,
            isLoggedIn = false
        )
    }

    // Banners
    val banners = listOf(
        BannerItem(
            id = 1,
            title = "مهرجان إلكترونيات وأجهزة المستقبل",
            subtitle = "أحدث الهواتف والسماعات والساعات الذكية بضمان معتمد",
            discountTag = "خصم حتى 45%",
            ctaText = "تسوق العرض الآن",
            isElectronics = true
        ),
        BannerItem(
            id = 2,
            title = "سوبرماركت وأزياء الموسم وعطور",
            subtitle = "توصيل فوري خلال 30 دقيقة وكاش باك 10% إلى محفظتك",
            discountTag = "شحن مجاني",
            ctaText = "استكشف العروض",
            isElectronics = false
        )
    )

    // Categories
    val categories = listOf(
        CategoryItem("all", "الكل", "grid", 48),
        CategoryItem("electronics", "إلكترونيات", "phone_iphone", 14),
        CategoryItem("supermarket", "سوبرماركت", "shopping_basket", 18),
        CategoryItem("fashion", "أزياء وموضة", "checkroom", 12),
        CategoryItem("perfumes", "عطور وتجميل", "spa", 9),
        CategoryItem("pharmacy", "صحة وصيدليات", "local_pharmacy", 8),
        CategoryItem("home", "منزل وديكور", "chair", 6)
    )

    // Stores (Multi-Store Vendors)
    private val _stores = MutableStateFlow(
        listOf(
            Store(
                id = 1,
                name = "متجر التقنية الذكية للإلكترونيات",
                category = "إلكترونيات",
                rating = 4.9,
                deliveryTime = "25 دقيقة",
                minOrder = "5,000 ر.ي",
                deliveryFee = "مجاني للطلبات فوق 20,000",
                description = "وكيل معتمد للهواتف والملحقات الأصلية والشواحن السريعة"
            ),
            Store(
                id = 2,
                name = "هايبر ماركت البركة المركزي",
                category = "سوبرماركت",
                rating = 4.8,
                deliveryTime = "30 دقيقة",
                minOrder = "3,000 ر.ي",
                deliveryFee = "500 ر.ي",
                description = "أكبر تشكيلة مواد غذائية وخضار وفواكه طازجة يومياً"
            ),
            Store(
                id = 3,
                name = "دار النخبة للأزياء والأناقة",
                category = "أزياء وموضة",
                rating = 4.7,
                deliveryTime = "40 دقيقة",
                minOrder = "10,000 ر.ي",
                deliveryFee = "1,000 ر.ي",
                description = "أحدث الموديلات الرجالية والنسائية وأطقم راقية بجودة عالية"
            ),
            Store(
                id = 4,
                name = "متجر الأندلس للعطور والبخور",
                category = "عطور وتجميل",
                rating = 4.9,
                deliveryTime = "35 دقيقة",
                minOrder = "8,000 ر.ي",
                deliveryFee = "700 ر.ي",
                description = "أفخم العطور الشرقية والفرنسية والعود الطبيعي الفاخر"
            ),
            Store(
                id = 5,
                name = "صيدلية ومستلزمات الحياة",
                category = "صحة وصيدليات",
                rating = 4.8,
                deliveryTime = "20 دقيقة",
                minOrder = "2,000 ر.ي",
                deliveryFee = "مجاني",
                description = "أدوية ومكملات غذائية ومستحضرات عناية معتمدة"
            )
        )
    )
    val stores: StateFlow<List<Store>> = _stores.asStateFlow()

    // Products
    private val _products = MutableStateFlow(
        listOf(
            Product(
                id = 101,
                storeId = 1,
                storeName = "متجر التقنية الذكية",
                name = "سماعة رأس لاسلكية إلغاء الضوضاء Pro Max",
                description = "صوت نقي ثلاثي الأبعاد مع بطارية تدوم 40 ساعة وميكروفون نقي للمكالمات",
                priceYer = 45000.0,
                originalPriceYer = 60000.0,
                category = "electronics",
                rating = 4.9,
                badge = "الأكثر مبيعاً"
            ),
            Product(
                id = 102,
                storeId = 1,
                storeName = "متجر التقنية الذكية",
                name = "ساعة ذكية رياضية مقاومة للماء مع مراقب النبض",
                description = "شاشة AMOLED ملونة، تتبع النشاط الرياضي والنوم، متوافقة مع جميع الأجهزة",
                priceYer = 28000.0,
                originalPriceYer = 35000.0,
                category = "electronics",
                rating = 4.8,
                badge = "خصم 20%"
            ),
            Product(
                id = 103,
                storeId = 1,
                storeName = "متجر التقنية الذكية",
                name = "شاحن سريع 65W GaN متعدد المنافذ",
                description = "شحن فائق السرعة لـ 3 أجهزة في وقت واحد بتقنية الحماية الذكية",
                priceYer = 12500.0,
                originalPriceYer = null,
                category = "electronics",
                rating = 4.7
            ),
            Product(
                id = 201,
                storeId = 2,
                storeName = "هايبر ماركت البركة",
                name = "سلة المواد الغذائية العائلية المتكاملة",
                description = "أرز بسمتي فاخر، زيت طبخ نقي، سكر، حليب، معكرونة ودقيق فاخر",
                priceYer = 38000.0,
                originalPriceYer = 44000.0,
                category = "supermarket",
                rating = 4.9,
                badge = "توفير أسبوعي"
            ),
            Product(
                id = 202,
                storeId = 2,
                storeName = "هايبر ماركت البركة",
                name = "عسل سدر طبيعي فاخر 1 كجم",
                description = "عسل نحل دوعني أصلي 100% غني بالفوائد الصحية والمناعية",
                priceYer = 24000.0,
                originalPriceYer = 30000.0,
                category = "supermarket",
                rating = 5.0,
                badge = "طبيعي 100%"
            ),
            Product(
                id = 301,
                storeId = 3,
                storeName = "دار النخبة للأزياء",
                name = "ثوب فاخر قطن إنجليزي مع ياقة عصرية",
                description = "حياكة راقية وقماش بارد ومريح ومقاوم للتجاعيد للمناسبات",
                priceYer = 22000.0,
                originalPriceYer = 28000.0,
                category = "fashion",
                rating = 4.8
            ),
            Product(
                id = 302,
                storeId = 3,
                storeName = "دار النخبة للأزياء",
                name = "حذاء جلدي رسمي خفيف مريح للقدمين",
                description = "جلد طبيعي مرن مع نعل طبي مانع للانزلاق",
                priceYer = 18500.0,
                originalPriceYer = null,
                category = "fashion",
                rating = 4.6
            ),
            Product(
                id = 401,
                storeId = 4,
                storeName = "متجر الأندلس للعطور",
                name = "عطر ملوك العود الملكي 100 مل",
                description = "مزيج ساحر من دهن العود الفاخر والعنبر والورد الطائفي يدوم 48 ساعة",
                priceYer = 32000.0,
                originalPriceYer = 42000.0,
                category = "perfumes",
                rating = 4.9,
                badge = "مميز"
            ),
            Product(
                id = 501,
                storeId = 5,
                storeName = "صيدلية الحياة",
                name = "مجموعة فيتامينات متعددة ومعادن أوميغا 3",
                description = "دعم متكامل للمناعة والطاقة والنشاط الذهني والبدني",
                priceYer = 9500.0,
                originalPriceYer = 12000.0,
                category = "pharmacy",
                rating = 4.9
            )
        )
    )
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    // Cart
    private val _cart = MutableStateFlow<List<CartItem>>(
        listOf(
            CartItem(
                product = _products.value[0], // Headphones
                quantity = 1
            )
        )
    )
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    fun addToCart(product: Product) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = current[index].quantity + 1)
        } else {
            current.add(CartItem(product, 1))
        }
        _cart.value = current
    }

    fun updateCartQuantity(productId: Int, delta: Int) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val newQty = current[index].quantity + delta
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                current[index] = current[index].copy(quantity = newQty)
            }
            _cart.value = current
        }
    }

    fun removeFromCart(productId: Int) {
        _cart.value = _cart.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    // Favorites
    private val _favorites = MutableStateFlow<Set<Int>>(setOf(101, 401))
    val favorites: StateFlow<Set<Int>> = _favorites.asStateFlow()

    fun toggleFavorite(productId: Int) {
        val current = _favorites.value.toMutableSet()
        if (current.contains(productId)) {
            current.remove(productId)
        } else {
            current.add(productId)
        }
        _favorites.value = current
    }

    // Notifications
    private val _notifications = MutableStateFlow(
        listOf(
            AppNotification(
                id = "n1",
                title = "تم إيداع كاش باك في محفظة جيب",
                message = "حصلت على 2,500 ريال يمني مكافأة تسوق من متجر التقنية الذكية",
                time = "منذ 15 دقيقة"
            ),
            AppNotification(
                id = "n2",
                title = "عروض نهاية الأسبوع",
                message = "تخفيضات تصل إلى 50% على جميع منتجات السوبرماركت والأزياء",
                time = "منذ ساعتين"
            ),
            AppNotification(
                id = "n3",
                title = "تحديث طلبك #9842",
                message = "تم تسليم طلبك بنجاح. شكراً لتسوقك معنا!",
                time = "أمس"
            )
        )
    )
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Jeeb Wallet state
    private val _walletAccount = MutableStateFlow(
        WalletAccount(
            accountNumber = "2498 7701 4452",
            userName = "زيدان العطاب",
            phone = "+967 770 123 456",
            isVerified = true,
            balanceYer = 285400.0,
            balanceSar = 1420.0,
            balanceUsd = 380.0,
            points = 1850,
            savingsPocket = 50000.0
        )
    )
    val walletAccount: StateFlow<WalletAccount> = _walletAccount.asStateFlow()

    // Jeeb Wallet Transactions
    private val _transactions = MutableStateFlow(
        listOf(
            WalletTransaction(
                id = "tx101",
                title = "شراء من متجر التقنية الذكية",
                type = "PURCHASE",
                amount = 45000.0,
                currency = "ر.ي",
                date = "اليوم 10:45 ص",
                isPositive = false
            ),
            WalletTransaction(
                id = "tx102",
                title = "تغذية المحفظة عبر نقطة جيب",
                type = "DEPOSIT",
                amount = 100000.0,
                currency = "ر.ي",
                date = "أمس 04:30 م",
                isPositive = true
            ),
            WalletTransaction(
                id = "tx103",
                title = "كاش باك تسوق سوق بلس",
                type = "CASHBACK",
                amount = 2500.0,
                currency = "ر.ي",
                date = "أمس 11:15 ص",
                isPositive = true
            ),
            WalletTransaction(
                id = "tx104",
                title = "سداد فاتورة يمن نت ADSL",
                type = "BILL",
                amount = 12000.0,
                currency = "ر.ي",
                date = "01 سبتمبر 2026",
                isPositive = false
            ),
            WalletTransaction(
                id = "tx105",
                title = "تحويل لمشترك محفظة جيب",
                type = "TRANSFER",
                amount = 15000.0,
                currency = "ر.ي",
                date = "30 أغسطس 2026",
                isPositive = false
            )
        )
    )
    val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

    // Orders
    private val _orders = MutableStateFlow(
        listOf(
            StoreOrder(
                id = "ORD-9912",
                storeName = "متجر التقنية الذكية",
                totalAmount = 45000.0,
                currency = "ر.ي",
                date = "اليوم، 10:45 ص",
                status = "في الطريق مع المندوب",
                itemsCount = 1,
                items = listOf(
                    OrderItemDetail(
                        productName = "سماعات رأس لاسلكية Pro عازلة للضوضاء",
                        quantity = 1,
                        priceYer = 45000.0,
                        category = "electronics"
                    )
                ),
                deliveryAddress = "صنعاء - شارع حدة - بجوار سيتي ماكس",
                deliveryDriver = "الكابتن أحمد الخولاني",
                driverPhone = "770123456",
                paymentMethod = "محفظة جيب الإلكترونية (مدفوع بالكامل)",
                statusStep = 2,
                rating = null,
                userReview = null,
                reviews = listOf(
                    OrderReview(
                        id = "rev-1",
                        userName = "سالم باوزير",
                        rating = 5.0f,
                        comment = "المنتج أصلي والتوصيل كان سريع جداً، تطبيق شبيك سهل ومريح!",
                        date = "منذ يومين"
                    )
                ),
                chatMessages = listOf(
                    OrderChatMessage(
                        id = "msg-1",
                        senderName = "متجر التقنية الذكية",
                        message = "مرحباً بك! تم استلام طلبك وجاري فحصه وتغليفه بعناية. شبيك لبيك طلبك بين يديك 🌟",
                        time = "10:46 ص",
                        isFromUser = false
                    ),
                    OrderChatMessage(
                        id = "msg-2",
                        senderName = "الكابتن أحمد (المندوب)",
                        message = "السلام عليكم، استلمت الطلب من المتجر وأنا في الطريق إليك الآن عبر شارع الستين ثم حدة 🛵",
                        time = "11:05 ص",
                        isFromUser = false
                    ),
                    OrderChatMessage(
                        id = "msg-3",
                        senderName = "أنت",
                        message = "وعليكم السلام، ممتاز في انتظارك عند عمارة البركة بجوار الصيدلية",
                        time = "11:08 ص",
                        isFromUser = true
                    ),
                    OrderChatMessage(
                        id = "msg-4",
                        senderName = "الكابتن أحمد (المندوب)",
                        message = "تمام سأصل خلال 10 دقائق بإذن الله",
                        time = "11:10 ص",
                        isFromUser = false
                    )
                )
            ),
            StoreOrder(
                id = "ORD-9842",
                storeName = "هايبر ماركت البركة",
                totalAmount = 38000.0,
                currency = "ر.ي",
                date = "28 أغسطس 2026",
                status = "تم التسليم",
                itemsCount = 3,
                items = listOf(
                    OrderItemDetail(
                        productName = "سلة التوفير الغذائية الكبرى",
                        quantity = 1,
                        priceYer = 28000.0,
                        category = "supermarket"
                    ),
                    OrderItemDetail(
                        productName = "زيت نباتي نقي مكرر 4 لتر",
                        quantity = 2,
                        priceYer = 5000.0,
                        category = "supermarket"
                    )
                ),
                deliveryAddress = "صنعاء - حدة المدينة - شارع بيروت",
                deliveryDriver = "الكابتن ياسر الحمادي",
                driverPhone = "777441122",
                paymentMethod = "محفظة جيب الإلكترونية (مدفوع بالكامل)",
                statusStep = 3,
                rating = 5.0f,
                userReview = "خدمة ممتازة وأسعار مطابقة تماماً للمتجر وتم الدفع بالرصيد بسلاسة",
                reviews = listOf(
                    OrderReview(
                        id = "rev-2",
                        userName = "زيدان العطاب",
                        rating = 5.0f,
                        comment = "خدمة ممتازة وأسعار مطابقة تماماً للمتجر وتم الدفع بالرصيد بسلاسة",
                        date = "28 أغسطس 2026"
                    )
                ),
                chatMessages = listOf(
                    OrderChatMessage(
                        id = "msg-prev-1",
                        senderName = "هايبر ماركت البركة",
                        message = "تم تجهيز المواد الغذائية وتغليفها وتبريدها بنجاح.",
                        time = "04:15 م",
                        isFromUser = false
                    ),
                    OrderChatMessage(
                        id = "msg-prev-2",
                        senderName = "الكابتن ياسر",
                        message = "وصلت للموقع وتم تسليم الأغراض بالكامل، شكراً لك!",
                        time = "05:00 م",
                        isFromUser = false
                    )
                )
            )
        )
    )
    val orders: StateFlow<List<StoreOrder>> = _orders.asStateFlow()

    // Telecom Packages for Payment Networks (Yemen Mobile, Sabafon, YOU, Y, Yemen Net)
    val telecomPackages = listOf(
        // Yemen Mobile Packages
        TelecomPackage("ym_1", "باقة مزايا الشهرية (رصيد + نت + رسائل)", "300 دقيقة + 300 رسالة + 1.5 جيجابايت صالحة 30 يوم", 1500.0, "باقات", "yemen_mobile"),
        TelecomPackage("ym_2", "باقة سوبر نت 4G فورجي 12GB", "12 جيجابايت بسرعة الجيل الرابع 4G LTE صالحة لشهر", 3500.0, "باقات", "yemen_mobile"),
        TelecomPackage("ym_3", "باقة هدايا نت 25GB التوفيرية", "25 جيجابايت نت فائق السرعة + استخدام مجاني فيسبوك وواتساب", 6500.0, "باقات", "yemen_mobile"),
        TelecomPackage("ym_4", "رصيد فوري يمن موبايل 1000 ريال", "تغذية رصيد مكالمات وخدمات يمن موبايل", 1000.0, "رصيد", "yemen_mobile"),
        TelecomPackage("ym_5", "رصيد فوري يمن موبايل 3000 ريال", "شحن رصيد مباشر لأي رقم يمن موبايل", 3000.0, "رصيد", "yemen_mobile"),
        TelecomPackage("ym_6", "شحن جملة وكلاء 10,000 ريال", "شحن فئات الجملة المعتمدة بخصم فوري", 9800.0, "جملة", "yemen_mobile"),
        TelecomPackage("ym_7", "باقة ريال موبايل (مكالمات مخفضة)", "رصيد ريال للدفع حسب الاستخدام مع تعرفة منخفضة", 2000.0, "ريال", "yemen_mobile"),

        // Sabafon Packages
        TelecomPackage("sb_1", "باقة شباب سبأفون الشهرية 10GB", "10 جيجابايت إنترنت سريع + 500 دقيقة سبأفون", 3200.0, "باقات", "sabafon"),
        TelecomPackage("sb_2", "باقة ميكس الأسبوعية 3GB", "3 جيجابايت + 150 دقيقة اتصال داخل وخارج الشبكة", 1200.0, "باقات", "sabafon"),
        TelecomPackage("sb_3", "رصيد فوري سبأفون 1000 ريال", "شحن رصيد مباشر فوري لسبأفون شمال وجنوب", 1000.0, "رصيد", "sabafon"),
        TelecomPackage("sb_4", "رصيد فوري سبأفون 2500 ريال", "تغذية رصيد أساسي لجميع باقات سبأفون", 2500.0, "فوري", "sabafon"),
        TelecomPackage("sb_5", "باقات جملة سبأفون المعتمدة", "سداد رصيد جملة وتفعيل باقات للمحلات والوكلاء", 5000.0, "جملة", "sabafon"),

        // YOU Packages
        TelecomPackage("you_1", "باقة يو مكس التوفيرية 15GB", "15 جيجابايت إنترنت الجيل الرابع 4G + 400 دقيقة", 3800.0, "باقات", "you"),
        TelecomPackage("you_2", "باقة يو سمارت اليومية 2GB", "2 جيجابايت إنترنت سريع للاستخدام اليومي", 600.0, "باقات", "you"),
        TelecomPackage("you_3", "رصيد فوري يو 1500 ريال", "شحن رصيد مباشر لخطوط YOU", 1500.0, "رصيد", "you"),
        TelecomPackage("you_4", "شحن جملة يو 10,000 ريال", "شحن رصيد الجملة لخطوط يو بأفضل خصم", 9800.0, "جملة", "you"),

        // Y Telecom
        TelecomPackage("y_1", "باقة واي إنترنت شهرية 8GB", "8 جيجابايت صالحة 30 يوم لشبكة واي Y", 2600.0, "باقات", "y"),
        TelecomPackage("y_2", "رصيد فوري واي 1000 ريال", "تغذية رصيد مباشر لخط واي", 1000.0, "رصيد", "y"),

        // Yemen Net & Fixed
        TelecomPackage("yn_1", "تجديد اشتراك يمن نت ADSL باقة 50GB", "شحن وتجديد رصيد الإنترنت المنزلي فائق السرعة", 4200.0, "باقات", "fixed"),
        TelecomPackage("yn_2", "تجديد يمن نت ADSL فئة 120GB", "باقة الإنترنت العائلي المنزلي بلا حدود", 9600.0, "باقات", "fixed"),
        TelecomPackage("yn_3", "سداد فاتورة الهاتف الثابت المنزلي", "سداد فواتير الهاتف الأرضي والنداء الآلي", 1500.0, "رصيد", "fixed")
    )

    // Sync wallet balance
    fun syncWalletBalance(): Double {
        val current = _walletAccount.value
        // Slight fluctuation or simulated refresh
        return current.balanceYer
    }

    // Feed wallet via Jeeb or any payment gateway
    fun feedWalletViaGateway(sourceName: String, phone: String, amount: Double, code: String): Boolean {
        if (amount <= 0) return false
        val current = _walletAccount.value
        _walletAccount.value = current.copy(balanceYer = current.balanceYer + amount)
        val newTx = WalletTransaction(
            id = "tx_${System.currentTimeMillis()}",
            title = "تغذية حسابي عبر $sourceName",
            type = "DEPOSIT",
            amount = amount,
            currency = "ر.ي",
            date = "الآن",
            isPositive = true
        )
        _transactions.value = listOf(newTx) + _transactions.value
        return true
    }

    // Pay telecom bill or recharge package
    fun payTelecomRecharge(
        phone: String,
        operatorName: String,
        category: String,
        packageName: String,
        amount: Double
    ): Pair<Boolean, String> {
        val current = _walletAccount.value
        if (current.balanceYer < amount) {
            return Pair(false, "عذراً، رصيد حسابك (${current.balanceYer.toInt()} ر.ي) غير كافٍ لسداد هذا المبلغ ($amount ر.ي). يرجى تغذية حسابك أولاً.")
        }

        _walletAccount.value = current.copy(
            balanceYer = current.balanceYer - amount,
            points = current.points + (amount / 200).toInt()
        )

        val txId = "TEL-${(100000..999999).random()}"
        val newTx = WalletTransaction(
            id = txId,
            title = "سداد $operatorName ($packageName) للرقم $phone",
            type = "BILL",
            amount = amount,
            currency = "ر.ي",
            date = "الآن",
            isPositive = false
        )
        _transactions.value = listOf(newTx) + _transactions.value

        val successMsg = "تمت عملية السداد بنجاح!\nالشبكة: $operatorName\nالرقم: $phone\nالخدمة: $packageName\nالمبلغ: $amount ر.ي\nرقم السند: $txId"
        return Pair(true, successMsg)
    }

    // Add message to order chat
    fun addOrderChatMessage(orderId: String, text: String) {
        val currentOrders = _orders.value.toMutableList()
        val index = currentOrders.indexOfFirst { it.id == orderId }
        if (index >= 0) {
            val order = currentOrders[index]
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val currentTime = sdf.format(Date())

            val userMsg = OrderChatMessage(
                id = "msg_${System.currentTimeMillis()}",
                senderName = "أنت",
                message = text,
                time = currentTime,
                isFromUser = true
            )

            // Context-aware auto-reply from the store or courier
            val replyMsg = OrderChatMessage(
                id = "reply_${System.currentTimeMillis() + 1}",
                senderName = if (order.statusStep >= 2) order.deliveryDriver else order.storeName,
                message = when {
                    text.contains("أين") || text.contains("وين") -> "الطلب في الطريق إليك مع الكابتن وسأصل قريباً جداً، يمكنك متابعة موقعي!"
                    text.contains("باب") || text.contains("وصلت") -> "حاضر، أنا أمام الباب الآن ونازل لعندك!"
                    text.contains("شكرا") || text.contains("يعطيك") -> "العفو على الرحب والسعة! في خدمتك دائماً في شبيك 🌟"
                    else -> "أهلاً بك! تم استلام رسالتك وجاري المتابعة معك فوراً لخدمتك بأفضل شكل."
                },
                time = currentTime,
                isFromUser = false
            )

            val updatedOrder = order.copy(
                chatMessages = order.chatMessages + listOf(userMsg, replyMsg)
            )
            currentOrders[index] = updatedOrder
            _orders.value = currentOrders
        }
    }

    // Rate an order
    fun rateOrder(orderId: String, rating: Float, comment: String) {
        val currentOrders = _orders.value.toMutableList()
        val index = currentOrders.indexOfFirst { it.id == orderId }
        if (index >= 0) {
            val order = currentOrders[index]
            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("ar"))
            val currentDate = sdf.format(Date())

            val newReview = OrderReview(
                id = "rev_${System.currentTimeMillis()}",
                userName = _userSession.value.fullName.ifBlank { "مستخدم شبيك" },
                rating = rating,
                comment = comment,
                date = currentDate
            )

            val updatedOrder = order.copy(
                rating = rating,
                userReview = comment,
                reviews = listOf(newReview) + order.reviews
            )
            currentOrders[index] = updatedOrder
            _orders.value = currentOrders
        }
    }

    // Wallet actions (Deposit, Transfer, Pay from Cart)
    fun depositToWallet(amount: Double) {
        val current = _walletAccount.value
        _walletAccount.value = current.copy(balanceYer = current.balanceYer + amount)
        val newTx = WalletTransaction(
            id = "tx_${System.currentTimeMillis()}",
            title = "تغذية المحفظة (إيداع سريع)",
            type = "DEPOSIT",
            amount = amount,
            currency = "ر.ي",
            date = "الآن",
            isPositive = true
        )
        _transactions.value = listOf(newTx) + _transactions.value
    }

    fun transferFromWallet(recipient: String, amount: Double): Boolean {
        val current = _walletAccount.value
        if (current.balanceYer >= amount) {
            _walletAccount.value = current.copy(balanceYer = current.balanceYer - amount)
            val newTx = WalletTransaction(
                id = "tx_${System.currentTimeMillis()}",
                title = "تحويل إلى $recipient",
                type = "TRANSFER",
                amount = amount,
                currency = "ر.ي",
                date = "الآن",
                isPositive = false
            )
            _transactions.value = listOf(newTx) + _transactions.value
            return true
        }
        return false
    }

    fun payOrderWithWallet(total: Double, storeName: String): Boolean {
        val current = _walletAccount.value
        if (current.balanceYer >= total) {
            _walletAccount.value = current.copy(
                balanceYer = current.balanceYer - total,
                points = current.points + (total / 500).toInt()
            )
            val newTx = WalletTransaction(
                id = "tx_${System.currentTimeMillis()}",
                title = "دفع مشتريات: $storeName",
                type = "PURCHASE",
                amount = total,
                currency = "ر.ي",
                date = "الآن",
                isPositive = false
            )
            _transactions.value = listOf(newTx) + _transactions.value

            val cartItems = _cart.value
            val orderItems = cartItems.map {
                OrderItemDetail(
                    productName = it.product.name,
                    quantity = it.quantity,
                    priceYer = it.product.priceYer,
                    category = it.product.category
                )
            }

            val newOrder = StoreOrder(
                id = "ORD-${(1000..9999).random()}",
                storeName = storeName,
                totalAmount = total,
                currency = "ر.ي",
                date = "الآن",
                status = "قيد التجهيز",
                itemsCount = cartItems.sumOf { it.quantity },
                items = orderItems,
                deliveryAddress = "صنعاء - شارع حدة - تقاطع الرويشان",
                deliveryDriver = "الكابتن محمد اليماني",
                driverPhone = "771998877",
                paymentMethod = "محفظة جيب الإلكترونية (مدفوع بالكامل من الرصيد)",
                statusStep = 1,
                chatMessages = listOf(
                    OrderChatMessage(
                        id = "msg_init_1",
                        senderName = storeName,
                        message = "مرحباً بك في تطبيق شبيك! تم استلام طلبك وبدأ المتجر في التجهيز فوراً. شبيك لبيّك طلبك بين يديك 🌟",
                        time = "الآن",
                        isFromUser = false
                    )
                )
            )
            _orders.value = listOf(newOrder) + _orders.value

            // Also attempt to push order to Django backend if logged in
            val token = _userSession.value.token
            if (!token.isNullOrBlank() && !token.startsWith("local_")) {
                coroutineScope.launch {
                    try {
                        val api = NetworkClient.getApiService(_djangoBaseUrl.value)
                        val itemsPayload = cartItems.map {
                            CreateOrderItemRequest(productId = it.product.id, quantity = it.quantity)
                        }
                        api.createOrder(
                            token = "Token $token",
                            request = CreateOrderRequest(
                                paymentMethod = "wallet",
                                items = itemsPayload
                            )
                        )
                    } catch (_: Exception) {}
                }
            }

            clearCart()
            return true
        }
        return false
    }

    fun checkoutCashOnDelivery(total: Double, storeName: String) {
        payOrderWithWallet(total, storeName)
    }

    fun fetchStoresAndProductsFromApi() {
        coroutineScope.launch {
            try {
                val api = NetworkClient.getApiService(_djangoBaseUrl.value)
                
                // 1. Fetch Vendors/Stores
                val vendorResp = api.getVendors()
                if (vendorResp.isSuccessful && vendorResp.body() != null) {
                    val vendorResults = vendorResp.body()!!.results
                    if (vendorResults.isNotEmpty()) {
                        val mappedStores = vendorResults.map { v ->
                            Store(
                                id = v.id,
                                name = v.storeName,
                                category = v.address ?: "عام",
                                rating = 4.8,
                                deliveryTime = "30 دقيقة",
                                minOrder = "2,000 ر.ي",
                                deliveryFee = "مجاني",
                                description = v.description ?: "متجر معتمد على منصة شبيك",
                                phone = v.phone ?: "771234567",
                                logoUrl = v.logoUrl,
                                coverUrl = v.coverUrl
                            )
                        }
                        _stores.value = mappedStores
                    }
                }

                // 2. Fetch Products
                val prodResp = api.getProducts()
                if (prodResp.isSuccessful && prodResp.body() != null) {
                    val productResults = prodResp.body()!!.results
                    if (productResults.isNotEmpty()) {
                        val mappedProducts = productResults.map { p ->
                            val effectivePrice = p.effectivePrice?.toDoubleOrNull() 
                                ?: p.price?.toDoubleOrNull() ?: 1000.0
                            val origPrice = p.price?.toDoubleOrNull()
                            val storeId = p.vendor?.id ?: 1
                            val storeName = p.vendor?.storeName ?: "شبيك ستور"
                            val catName = p.categories.firstOrNull()?.name ?: "الكل"
                            val imgList = mutableListOf<String>()
                            p.mainImageUrl?.let { imgList.add(it) }
                            p.gallery.forEach { g -> if (!imgList.contains(g.url)) imgList.add(g.url) }

                            Product(
                                id = p.id,
                                storeId = storeId,
                                storeName = storeName,
                                name = p.name,
                                description = p.description ?: "منتج أصلي معتمد",
                                priceYer = effectivePrice,
                                originalPriceYer = if (origPrice != null && origPrice > effectivePrice) origPrice else null,
                                category = catName,
                                rating = p.rating?.toDoubleOrNull() ?: 4.8,
                                inStock = (p.stock ?: 1) > 0,
                                badge = if (p.isTrending == true) "الأكثر طلباً" else null,
                                images = imgList,
                                reviewsCount = p.reviewsCount ?: 12
                            )
                        }
                        _products.value = mappedProducts
                    }
                }
            } catch (_: Exception) {
                // Keep default seed data on failure
            }
        }
    }

    fun fetchOrdersFromApi(token: String) {
        coroutineScope.launch {
            try {
                val api = NetworkClient.getApiService(_djangoBaseUrl.value)
                val resp = api.getOrders("Token $token")
                if (resp.isSuccessful && resp.body() != null) {
                    val ordersList = resp.body()!!.results
                    if (ordersList.isNotEmpty()) {
                        val mappedOrders = ordersList.map { o ->
                            val items = o.items?.map { item ->
                                OrderItemDetail(
                                    productName = item.productName ?: "منتج",
                                    quantity = item.quantity,
                                    priceYer = item.unitPrice?.toDoubleOrNull() ?: 0.0,
                                    category = "عام"
                                )
                            } ?: emptyList()

                            StoreOrder(
                                id = o.orderNumber ?: "ORD-${o.id}",
                                storeName = o.items?.firstOrNull()?.vendorName ?: "شبيك ستور",
                                totalAmount = o.total?.toDoubleOrNull() ?: 0.0,
                                currency = o.currency ?: "ر.ي",
                                date = o.createdAt?.take(10) ?: "الآن",
                                status = when (o.status) {
                                    "pending" -> "قيد المراجعة"
                                    "processing" -> "قيد التجهيز"
                                    "shipped" -> "في الطريق إليك"
                                    "delivered" -> "تم التوصيل"
                                    "cancelled" -> "ملغي"
                                    else -> o.status ?: "قيد التجهيز"
                                },
                                itemsCount = items.sumOf { it.quantity },
                                items = items,
                                deliveryAddress = "صنعاء - العنوان المسجل",
                                deliveryDriver = "مندوب التوصيل",
                                driverPhone = "771234567",
                                paymentMethod = o.paymentMethod ?: "الدفع الإلكتروني",
                                statusStep = when (o.status) {
                                    "pending" -> 0
                                    "processing" -> 1
                                    "shipped" -> 2
                                    "delivered" -> 3
                                    else -> 1
                                }
                            )
                        }
                        _orders.value = mappedOrders
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // User Addresses Book
    private val _addresses = MutableStateFlow<List<UserAddress>>(
        listOf(
            UserAddress(
                id = 1,
                title = "المنزل",
                city = "صنعاء",
                district = "حدة - الحي الدبلوماسي",
                street = "شارع طوكيو، عمارة الأمل، شقة 4",
                building = "عمارة 12",
                phone = "770123456",
                isDefault = true
            ),
            UserAddress(
                id = 2,
                title = "العمل / المتجر",
                city = "صنعاء",
                district = "شارع الزبيري",
                street = "برج سبأ، الدور الثالث، مكتب 304",
                building = "برج سبأ",
                phone = "771234567",
                isDefault = false
            )
        )
    )
    val addresses: StateFlow<List<UserAddress>> = _addresses.asStateFlow()

    fun addAddress(address: UserAddress) {
        val updated = if (address.isDefault) {
            _addresses.value.map { it.copy(isDefault = false) } + address
        } else {
            _addresses.value + address
        }
        _addresses.value = updated
    }

    fun setDefaultAddress(id: Int) {
        _addresses.value = _addresses.value.map {
            it.copy(isDefault = (it.id == id))
        }
    }

    fun deleteAddress(id: Int) {
        _addresses.value = _addresses.value.filter { it.id != id }
    }

    // Support Tickets & Inquiries
    private val _supportTickets = MutableStateFlow<List<SupportTicket>>(
        listOf(
            SupportTicket(
                id = "TCK-1082",
                subject = "استفسار بخصوص موعد شحن الطلب",
                category = "شحن وتوصيل",
                status = "تم الرد",
                date = "اليوم 02:15 م",
                lastMessage = "تم تسليم الشحنة لمندوب التوصيل في أمانة العاصمة."
            ),
            SupportTicket(
                id = "TCK-1049",
                subject = "تأكيد تغذية المحفظة عبر الكريمي",
                category = "مشكلة دفع",
                status = "مغلقة",
                date = "أمس 09:30 ص",
                lastMessage = "تم قيد المبلغ بنجاح في رصيد محفظتك المتاح."
            )
        )
    )
    val supportTickets: StateFlow<List<SupportTicket>> = _supportTickets.asStateFlow()

    private val _supportChatMessages = MutableStateFlow<List<SupportChatMessage>>(
        listOf(
            SupportChatMessage("1", "خدمة العملاء", "مرحباً بك في خدمة عملاء متجر شبيك وسوق بلس. كيف يمكننا مساعدتك اليوم؟", "02:10 م", false),
            SupportChatMessage("2", "أنت", "أريد الاستفسار عن كود الخصم وطريقة تفعيله عند الطلب.", "02:12 م", true),
            SupportChatMessage("3", "خدمة العملاء", "أهلاً بك يا أخي! يتم إدراج كود الخصم تلقائياً عند الشراء عبر المحفظة أو من صفحة الترندات والعروض الخاصة.", "02:14 م", false)
        )
    )
    val supportChatMessages: StateFlow<List<SupportChatMessage>> = _supportChatMessages.asStateFlow()

    fun sendSupportMessage(msg: String) {
        val newMsg = SupportChatMessage(
            id = System.currentTimeMillis().toString(),
            sender = "أنت",
            message = msg,
            time = SimpleDateFormat("hh:mm a", Locale("ar")).format(Date()),
            isFromUser = true
        )
        _supportChatMessages.value = _supportChatMessages.value + newMsg

        // Auto reply after delay
        coroutineScope.launch {
            kotlinx.coroutines.delay(1200)
            val reply = SupportChatMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                sender = "خدمة العملاء",
                message = "شكراً لتواصلك معنا، استلمنا رسالتك: '$msg'. الموظف المختص يتابع طلبك الآن.",
                time = SimpleDateFormat("hh:mm a", Locale("ar")).format(Date()),
                isFromUser = false
            )
            _supportChatMessages.value = _supportChatMessages.value + reply
        }
    }

    fun createSupportTicket(subject: String, category: String, details: String) {
        val newTicket = SupportTicket(
            id = "TCK-${(1000..9999).random()}",
            subject = subject,
            category = category,
            status = "قيد المعالجة",
            date = "الآن",
            lastMessage = details
        )
        _supportTickets.value = listOf(newTicket) + _supportTickets.value
        sendSupportMessage("تم فتح تذكرة جديدة [$category]: $subject - $details")
    }

    // Referral Program
    private val _referralCode = MutableStateFlow("SHOP-770123")
    val referralCode: StateFlow<String> = _referralCode.asStateFlow()

    private val _invitedCount = MutableStateFlow(14)
    val invitedCount: StateFlow<Int> = _invitedCount.asStateFlow()

    private val _referralRewardYer = MutableStateFlow(35000.0)
    val referralRewardYer: StateFlow<Double> = _referralRewardYer.asStateFlow()

    // Preferences & Settings
    private val _selectedCurrency = MutableStateFlow("YER")
    val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    val currencyRates: List<CurrencyRate> = listOf(
        CurrencyRate("USD", "YER", "535.00 ر.ي (صنعاء) / 1,900.00 ر.ي (عدن)"),
        CurrencyRate("SAR", "YER", "140.50 ر.ي (صنعاء) / 500.00 ر.ي (عدن)"),
        CurrencyRate("USD", "SAR", "3.75 ر.س")
    )

    fun setSelectedCurrency(curr: String) {
        _selectedCurrency.value = curr
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    // Vendor Portal & Store Management
    private val _vendorFinance = MutableStateFlow(
        VendorFinance(
            vendorName = "روائع العود والعطور",
            walletBalance = 385000.0,
            availableBalance = 310000.0,
            earned = 645000.0,
            paid = 260000.0,
            pending = 75000.0,
            currency = "ر.ي"
        )
    )
    val vendorFinance: StateFlow<VendorFinance> = _vendorFinance.asStateFlow()

    private val _vendorPayouts = MutableStateFlow<List<VendorPayoutRequest>>(
        listOf(
            VendorPayoutRequest(
                id = "PO-9912",
                amount = 150000.0,
                currency = "ر.ي",
                reference = "حوالة الكريمي - فرع حدة (770123456)",
                date = "2026/08/28",
                status = "paid"
            ),
            VendorPayoutRequest(
                id = "PO-9954",
                amount = 110000.0,
                currency = "ر.ي",
                reference = "شبكة النجم للحوالات (770123456)",
                date = "2026/09/01",
                status = "approved"
            ),
            VendorPayoutRequest(
                id = "PO-9988",
                amount = 75000.0,
                currency = "ر.ي",
                reference = "سحب إلى محفظة جيب الإلكترونية",
                date = "2026/09/03",
                status = "pending"
            )
        )
    )
    val vendorPayouts: StateFlow<List<VendorPayoutRequest>> = _vendorPayouts.asStateFlow()

    fun requestVendorPayout(amount: Double, reference: String): Boolean {
        if (amount <= 0 || amount > _vendorFinance.value.availableBalance) return false
        val newReq = VendorPayoutRequest(
            id = "PO-${(1000..9999).random()}",
            amount = amount,
            currency = "ر.ي",
            reference = reference,
            date = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date()),
            status = "pending"
        )
        _vendorPayouts.value = listOf(newReq) + _vendorPayouts.value
        _vendorFinance.value = _vendorFinance.value.copy(
            availableBalance = _vendorFinance.value.availableBalance - amount,
            pending = _vendorFinance.value.pending + amount
        )
        return true
    }

    fun addVendorProduct(
        name: String,
        description: String,
        priceYer: Double,
        category: String,
        stock: Int,
        badge: String?
    ) {
        val newProduct = Product(
            id = (_products.value.maxOfOrNull { it.id } ?: 100) + 1,
            storeId = 1,
            storeName = "روائع العود والعطور",
            name = name,
            description = description,
            priceYer = priceYer,
            originalPriceYer = priceYer * 1.25,
            category = category,
            rating = 5.0,
            inStock = stock > 0,
            badge = badge,
            images = listOf("https://images.unsplash.com/photo-1594035910387-fea47794261f?w=600"),
            reviewsCount = 1
        )
        _products.value = listOf(newProduct) + _products.value
    }

    fun updateOrderStatus(orderId: String, newStatus: String, newStep: Int) {
        _orders.value = _orders.value.map { order ->
            if (order.id == orderId) {
                order.copy(status = newStatus, statusStep = newStep)
            } else {
                order
            }
        }
    }

    init {
        fetchStoresAndProductsFromApi()
    }

    companion object {
        val instance by lazy { StoreRepository() }
    }
}
