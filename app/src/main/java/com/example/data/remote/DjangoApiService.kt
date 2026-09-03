package com.example.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Django REST Framework API Service for shopik.alattab.site
 */
interface DjangoApiService {

    // 1. Vendors (Stores)
    @GET("vendors/")
    suspend fun getVendors(): Response<VendorResponse>

    // 2. Products
    @GET("products/")
    suspend fun getProducts(
        @Query("vendor") vendorId: Int? = null,
        @Query("search") search: String? = null,
        @Query("category") category: String? = null
    ): Response<ProductResponse>

    // 3. Auth
    @POST("auth/login/")
    suspend fun login(
        @Body request: LoginPayload
    ): Response<AuthLoginResponse>

    @POST("auth/register/")
    suspend fun register(
        @Body request: LoginPayload
    ): Response<AuthLoginResponse>

    @GET("auth/me/")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<UserDto>

    // 4. Orders
    @GET("orders/")
    suspend fun getOrders(
        @Header("Authorization") token: String
    ): Response<OrderResponse>

    @POST("orders/")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body request: CreateOrderRequest
    ): Response<OrderDto>
}

data class LoginPayload(
    val phone: String,
    val password: String
)
