package com.simosc.landworkscheduler.core.di.viewmodel

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
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
object SensorClientsProvider {

    @Provides
    fun provideGeoClient(@ApplicationContext applicationContext: Context): GeoClient =
        GeoClientImpl(applicationContext)

    @Provides
    fun provideCompassClient(@ApplicationContext applicationContext: Context): CompassClient =
        CompassClientImpl(applicationContext)

    @Provides
    fun provideLocationClient(@ApplicationContext applicationContext: Context): LocationClient =
        LocationClientImpl(
            context = applicationContext,
            client = LocationServices.getFusedLocationProviderClient(
                applicationContext
            )
        )
}