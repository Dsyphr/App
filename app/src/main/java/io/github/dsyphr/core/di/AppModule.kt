package io.github.dsyphr.core.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.dsyphr.core.repository.AuthRepository
import io.github.dsyphr.core.repository.ChatRepository
import io.github.dsyphr.core.repository.MessageRepository
import io.github.dsyphr.core.repository.UserRepository
import io.github.dsyphr.core.translation.TranslationEngine
import io.github.dsyphr.data.firebase.FirebaseAuthRepository
import io.github.dsyphr.data.firebase.FirebaseChatRepository
import io.github.dsyphr.data.firebase.FirebaseMessageRepository
import io.github.dsyphr.data.firebase.FirebaseUserRepository
import io.github.dsyphr.data.translation.MlkitTranslationEngine
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): DatabaseReference {
        return FirebaseDatabase.getInstance().reference
    }

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository {
        return FirebaseAuthRepository(auth)
    }

    @Provides
    @Singleton
    fun provideUserRepository(database: DatabaseReference): UserRepository {
        return FirebaseUserRepository(database)
    }

    @Provides
    @Singleton
    fun provideMessageRepository(
        database: DatabaseReference,
        userRepository: UserRepository,
        auth: FirebaseAuth
    ): MessageRepository {
        return FirebaseMessageRepository(database, userRepository, auth)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        database: DatabaseReference,
        auth: FirebaseAuth
    ): ChatRepository {
        return FirebaseChatRepository(database, auth)
    }

    @Provides
    @Singleton
    fun provideTranslationEngine(@ApplicationContext context: Context): TranslationEngine {
        return MlkitTranslationEngine(context)
    }
}
