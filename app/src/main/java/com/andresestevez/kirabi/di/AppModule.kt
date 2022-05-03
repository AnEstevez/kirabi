package com.andresestevez.kirabi.di

import android.content.Context
import com.andresestevez.kirabi.R
import com.andresestevez.kirabi.exoplayer.AudioServiceConnection
import com.andresestevez.kirabi.presentation.adapters.SwipeMediaAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideAudioServiceConnection(@ApplicationContext context: Context) =
        AudioServiceConnection(context)

    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context,
    ): RequestManager {
        val shimmer = Shimmer.AlphaHighlightBuilder()
            .setDuration(1500)
            .setBaseAlpha(0.9f)
            .setHighlightAlpha(1f)
            .setDirection(Shimmer.Direction.TOP_TO_BOTTOM)
            .setAutoStart(true)
            .build()
        val shimmerDrawable = ShimmerDrawable().apply {
            setShimmer(shimmer)
        }

        return Glide.with(context).setDefaultRequestOptions(
            RequestOptions.fitCenterTransform()
                .placeholder(shimmerDrawable)
                .error(R.drawable.ic_error)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
        )
    }

    @Singleton
    @Provides
    fun provideSwipeMediaAdapter() : SwipeMediaAdapter = SwipeMediaAdapter()
}



