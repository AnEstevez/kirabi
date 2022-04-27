package com.andresestevez.kirabi.di

import android.app.Application
import android.content.Context
import com.andresestevez.kirabi.exoplayer.AudioServiceConnection
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun providesAudioServiceConnection(@ApplicationContext context: Context) =
        AudioServiceConnection(context)
}