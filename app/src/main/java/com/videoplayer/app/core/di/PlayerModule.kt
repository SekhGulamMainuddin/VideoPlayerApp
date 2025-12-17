package com.videoplayer.app.core.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@InstallIn(ViewModelComponent::class)
@Module
object PlayerModule {

    @ViewModelScoped
    @Provides
    fun provideExoplayer(@ApplicationContext() appContext: Context) = ExoPlayer.Builder(appContext).build()

}