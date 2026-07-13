package com.mk.habittracker

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.mk.habittracker.data.dao.HabitDao
import com.mk.habittracker.data.dao.UserDao
import com.mk.habittracker.data.database.AppDatabase
import com.mk.habittracker.data.util.Converters
import com.mk.habittracker.nfc.NfcModule
import java.io.IOException
import javax.inject.Singleton

@Module(
    includes = [
        NfcModule::class,
    ],
)
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        converters: Converters,
    ): AppDatabase =
        Room
            .databaseBuilder(
                context = context,
                klass = AppDatabase::class.java,
                name = "app_db",
            ).addTypeConverter(converters)
            .addCallback(
                object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        seedDatabase(context, db)
                    }
                },
            ).build()

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideHabitDao(database: AppDatabase): HabitDao = database.habitDao()
}

@Suppress("NestedBlockDepth")
private fun seedDatabase(
    context: Context,
    db: SupportSQLiteDatabase,
) {
    try {
        context.assets.open("sampleData.sql").bufferedReader().use { reader ->
            val statements =
                reader
                    .readText()
                    .lines()
                    .filterNot { it.trim().startsWith("--") || it.isBlank() }
                    .joinToString("\n")
                    .split(";")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

            db.beginTransaction()
            try {
                statements.forEach { db.execSQL(it) }
                Log.i("DatabaseModule", "successfully wrote sample data to database")
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    } catch (e: IOException) {
        Log.e("DatabaseModule", "Failed to seed database from assets", e)
    }
}
