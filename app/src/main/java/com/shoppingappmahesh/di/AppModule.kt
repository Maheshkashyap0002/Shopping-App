package com.shoppingappmahesh.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.shoppingappmahesh.data.repository.AddressRepositoryImpl
import com.shoppingappmahesh.data.repository.AuthRepositoryImpl
import com.shoppingappmahesh.data.repository.CartRepositoryImpl
import com.shoppingappmahesh.data.repository.ChatRepositoryImpl
import com.shoppingappmahesh.data.repository.OrderRepositoryImpl
import com.shoppingappmahesh.data.repository.ProductRepositoryImpl
import com.shoppingappmahesh.domain.repository.AddressRepository
import com.shoppingappmahesh.domain.repository.AuthRepository
import com.shoppingappmahesh.domain.repository.CartRepository
import com.shoppingappmahesh.domain.repository.ChatRepository
import com.shoppingappmahesh.domain.repository.OrderRepository
import com.shoppingappmahesh.domain.repository.ProductRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeminiApiKey

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth, database: FirebaseDatabase): AuthRepository {
        return AuthRepositoryImpl(auth, database)
    }

    @Provides
    @Singleton
    fun provideProductRepository(database: FirebaseDatabase): ProductRepository {
        return ProductRepositoryImpl(database)
    }

    @Provides
    @Singleton
    fun provideCartRepository(database: FirebaseDatabase): CartRepository {
        return CartRepositoryImpl(database)
    }

    @Provides
    @Singleton
    fun provideOrderRepository(database: FirebaseDatabase): OrderRepository {
        return OrderRepositoryImpl(database)
    }

    @Provides
    @Singleton
    fun provideAddressRepository(database: FirebaseDatabase): AddressRepository {
        return AddressRepositoryImpl(database)
    }

    @Provides
    @Singleton
    @GeminiApiKey
    fun provideGeminiApiKey(): String = "Paste Here Api Key" // Replace with your Gemini api key

    @Provides
    @Singleton
    fun provideChatRepository(
        productRepository: ProductRepository,
        cartRepository: CartRepository,
        orderRepository: OrderRepository,
        authRepository: AuthRepository,
        @GeminiApiKey apiKey: String
    ): ChatRepository {
        return ChatRepositoryImpl(productRepository, cartRepository, orderRepository, authRepository, apiKey)
    }
}