package com.simosc.landworkscheduler.core.di

import android.content.Context
import com.google.android.gms.location.LocationServices
import com.simosc.landworkscheduler.data.client.CompassClientImpl
import com.simosc.landworkscheduler.data.client.GeoClientImpl
import com.simosc.landworkscheduler.data.client.LocationClientImpl
import com.simosc.landworkscheduler.domain.client.CompassClient
import com.simosc.landworkscheduler.domain.client.GeoClient
import com.simosc.landworkscheduler.domain.client.LocationClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ClientsProvider {

    @Provides
    fun provideGeoClient(
        @ApplicationContext applicationContext: Context
    ): GeoClient {
        return GeoClientImpl(
            applicationContext
        )
    }

    @Provides
    fun provideCompassClient(
        @ApplicationContext applicationContext: Context
    ): CompassClient {
        return CompassClientImpl(
            applicationContext
        )
    }

    @Provides
    fun provideLocationClient(
        @ApplicationContext applicationContext: Context
    ): LocationClient {
        return LocationClientImpl(
            context = applicationContext,
            client = LocationServices.getFusedLocationProviderClient(
                applicationContext
            )
        )
    }
}