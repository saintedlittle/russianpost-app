package ru.russianpost.digitalperiodicals.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.russianpost.digitalperiodicals.data.database.AppDatabase
import ru.russianpost.digitalperiodicals.data.database.BookmarkDao
import ru.russianpost.digitalperiodicals.data.database.EditionsDao
import ru.russianpost.digitalperiodicals.data.network.Network.HTTP_CLIENT_CATALOG
import ru.russianpost.digitalperiodicals.data.network.Network.buildRusPostClient
import ru.russianpost.digitalperiodicals.data.repository.BookmarkRepositoryImpl
import ru.russianpost.digitalperiodicals.data.repository.EditionsRepositoryImpl
import ru.russianpost.digitalperiodicals.downloadManager.DownloadManager
import ru.russianpost.digitalperiodicals.downloadManager.DownloadManagerImpl
import ru.russianpost.digitalperiodicals.downloadManager.downloadManagaerRepositories.DownloadRepository
import ru.russianpost.digitalperiodicals.downloadManager.downloadManagaerRepositories.FileDownloadRepositoryRepository
import ru.russianpost.digitalperiodicals.features.editions.EditionsNetworkRepository
import ru.russianpost.digitalperiodicals.features.editions.EditionsRepository
import ru.russianpost.digitalperiodicals.features.favorite.FavoriteRepository
import ru.russianpost.digitalperiodicals.features.favorite.service.FavoriteService
import ru.russianpost.digitalperiodicals.features.favorite.service.FavoriteServiceImpl
import ru.russianpost.digitalperiodicals.features.mainScreen.PublicationsRepository
import ru.russianpost.digitalperiodicals.features.menu.AuthorizationManager
import ru.russianpost.digitalperiodicals.features.menu.MenuRepository
import ru.russianpost.digitalperiodicals.features.reader.BookmarkRepository
import ru.russianpost.digitalperiodicals.features.reader.ReaderRepository
import ru.russianpost.digitalperiodicals.features.subscriptions.SubscriptionStatusDefiner
import ru.russianpost.digitalperiodicals.features.subscriptions.SubscriptionsRepository
import ru.russianpost.digitalperiodicals.utils.AUTHENTICATION_TOKEN
import ru.russianpost.digitalperiodicals.utils.ENCRYPTED_SHARED_PREFERENCES
import ru.russianpost.digitalperiodicals.utils.UserNameService
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class Module {

    @Provides
    @Singleton
    @Named(HTTP_CLIENT_CATALOG)
    fun provideUpdatesOkHttpClient(@Named(ENCRYPTED_SHARED_PREFERENCES) sharedPreferences: SharedPreferences): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .apply {
                        val token = sharedPreferences.getString(AUTHENTICATION_TOKEN, null)
                        token?.let {
                            addHeader("Authorization", "Bearer $token")
                        }
                    }
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .buildRusPostClient()
            .build()
    }

    @Provides
    @Singleton
    fun provideSubscriptionStatusDefiner(
        @ApplicationContext context: Context,
    ) = SubscriptionStatusDefiner(context)

    @Provides
    @Singleton
    fun provideCatalogAPI(
        retrofitInstance: Retrofit,
    ) = retrofitInstance.create(PublicationsRepository::class.java)

    @Provides
    @Singleton
    fun provideMenuRepository(
        retrofitInstance: Retrofit,
    ) = retrofitInstance.create(MenuRepository::class.java)

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context,
    ) = context.getSharedPreferences("cookies-sp", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideAuthManager(
        @ApplicationContext context: Context,
        @Named(ENCRYPTED_SHARED_PREFERENCES)
        sharedPreferences: SharedPreferences,
    ) = AuthorizationManager(context, sharedPreferences)

    @Provides
    @Singleton
    @Named(ENCRYPTED_SHARED_PREFERENCES)
    fun provideEncryptedSharedPreferences(
        @ApplicationContext context: Context,
    ): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            "AuthorizationToken",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Provides
    @Singleton
    fun provideFavoriteRepository(
        retrofitInstance: Retrofit,
    ) = retrofitInstance.create(FavoriteRepository::class.java)

    @Provides
    @Singleton
    fun subscriptionsRepository(
        retrofitInstance: Retrofit,
    ) = retrofitInstance.create(SubscriptionsRepository::class.java)

    @Provides
    @Singleton
    fun editionsNetworkRepository(
        retrofitInstance: Retrofit,
    ) = retrofitInstance.create(EditionsNetworkRepository::class.java)

    @Provides
    @Singleton
    fun editionsRepository(impl: EditionsRepositoryImpl): EditionsRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideReaderRepository(
        retrofitInstance: Retrofit,
    ) = retrofitInstance.create(ReaderRepository::class.java)

    @Provides
    @Singleton
    fun provideBookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideGsonConverterFactory() =
        GsonConverterFactory.create(GsonBuilder().setLenient().create())

    @Provides
    @Singleton
    fun provideRetrofit(
        @Named(HTTP_CLIENT_CATALOG) okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory,
    ): Retrofit = Retrofit.Builder()
        .addConverterFactory(gsonConverterFactory)
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .build()

    @Provides
    @Singleton
    fun provideDataBase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DB_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideBookmarkDao(appDatabase: AppDatabase): BookmarkDao {
        return appDatabase.bookmarkDao()
    }

    @Provides
    @Singleton
    fun provideEditionsDao(appDatabase: AppDatabase): EditionsDao {
        return appDatabase.editionsDao()
    }

    @Provides
    @Singleton
    fun provideFileDownloadRepository(retrofitInstance: Retrofit): DownloadRepository {
        return retrofitInstance.create(FileDownloadRepositoryRepository::class.java)
    }

    @Provides
    @Singleton
    fun provideUserNameService(sharedPreferences: SharedPreferences) =
        UserNameService(sharedPreferences)

    @Provides
    @Singleton
    fun provideFavoriteService(
        favoriteRepository: FavoriteRepository,
        repository: EditionsRepository,
    ): FavoriteService = FavoriteServiceImpl(
        favoriteRepository = favoriteRepository,
        repository = repository
    )

    @Provides
    @Singleton
    fun provideDownloadManager(
        downloadRepository: DownloadRepository,
        editionsRepository: EditionsRepository,
        @ApplicationContext context: Context,
        userNameService: UserNameService,
    ): DownloadManager {
        return DownloadManagerImpl(downloadRepository, editionsRepository, context, userNameService)
    }

    companion object {
        private const val DB_NAME = "main.db"
        private const val BASE_URL = "https://back.digitalperiodicals.test.russianpost.ru/"
    }
}