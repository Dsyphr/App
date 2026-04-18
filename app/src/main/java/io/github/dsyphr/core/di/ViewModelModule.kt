package io.github.dsyphr.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.dsyphr.presentation.viewmodel.LoginViewModel
import io.github.dsyphr.presentation.viewmodel.SignupViewModel
import io.github.dsyphr.presentation.viewmodel.HomeViewModel
import io.github.dsyphr.presentation.viewmodel.ChatViewModel
import io.github.dsyphr.presentation.viewmodel.SettingsViewModel
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ViewModelModule {

    @Provides
    @Singleton
    fun provideLoginViewModel(
        authRepository: io.github.dsyphr.core.repository.AuthRepository
    ): LoginViewModel {
        return LoginViewModel(authRepository)
    }

    @Provides
    @Singleton
    fun provideSignupViewModel(
        authRepository: io.github.dsyphr.core.repository.AuthRepository
    ): SignupViewModel {
        return SignupViewModel(authRepository)
    }

    @Provides
    @Singleton
    fun provideHomeViewModel(
        chatRepository: io.github.dsyphr.core.repository.ChatRepository,
        userRepository: io.github.dsyphr.core.repository.UserRepository
    ): HomeViewModel {
        return HomeViewModel(chatRepository, userRepository)
    }

    @Provides
    @Singleton
    fun provideChatViewModel(
        translationEngine: io.github.dsyphr.core.translation.TranslationEngine,
        messageRepository: io.github.dsyphr.core.repository.MessageRepository
    ): ChatViewModel {
        return ChatViewModel(translationEngine, messageRepository)
    }

    @Provides
    @Singleton
    fun provideSettingsViewModel(
        authRepository: io.github.dsyphr.core.repository.AuthRepository,
        userRepository: io.github.dsyphr.core.repository.UserRepository
    ): SettingsViewModel {
        return SettingsViewModel(authRepository, userRepository)
    }
}
