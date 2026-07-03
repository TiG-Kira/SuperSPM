package com.kira.superspm.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.kira.superspm.data.database.AppDatabase
import com.kira.superspm.data.database.LocationRecordDao
import com.kira.superspm.data.repository.RecordRepository
import com.kira.superspm.viewmodel.DetailViewModel
import com.kira.superspm.viewmodel.HistoryViewModel
import com.kira.superspm.viewmodel.SettingsViewModel
import com.kira.superspm.viewmodel.SpeedometerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "superspm_db"
        ).build()
    }

    single<LocationRecordDao> { get<AppDatabase>().locationRecordDao() }
    single<RecordRepository> { RecordRepository(get()) }
    single<DataStore<Preferences>> { androidContext().dataStore }

    viewModel { SpeedometerViewModel(get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { DetailViewModel(get()) }
}