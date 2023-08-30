package com.simosc.landworkscheduler.core.di.viewmodel

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
object SharedPreferencesProvider {

    @Provides
    fun provideSharedPreferences(@ApplicationContext applicationContext: Context): SharedPreferences =
        applicationContext.getSharedPreferences(
            "com.simosc.landworkscheduler_preferences",
            Context.MODE_PRIVATE
        )

}