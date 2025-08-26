package com.codingempire.adminpse.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.codingempire.adminpse.Dao.PlanDao
import com.codingempire.adminpse.Dao.UserDao
import com.codingempire.adminpse.Dao.WithdrawDao
import com.codingempire.adminpse.models.AccountModel
import com.codingempire.adminpse.models.PlanModel
import com.codingempire.adminpse.models.TeamSettings
import com.codingempire.adminpse.models.UserModel
import com.codingempire.adminpse.models.WithdrawModel
import com.codingempire.adminpse.models.WithdrawWithUserName
import com.codingempire.adminpse.utils.Converters
import com.codingempire.adminpse.utils.TimestampConverter
import com.codingempire.adminpse.utils.WithdrawModelConverter


@Database(
    entities = [
        UserModel::class, AccountModel::class, PlanModel::class,
        WithdrawModel::class, WithdrawWithUserName::class, TeamSettings::class
    ],
    version = 4,                // ↑ bump from 2 → 3
    exportSchema = false
)
@TypeConverters(TimestampConverter::class, WithdrawModelConverter::class, Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun planDao(): PlanDao
    abstract fun userDao(): UserDao
    abstract fun withdrawDao(): WithdrawDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Define the getInstance method
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "investment_database"
                )
                    .fallbackToDestructiveMigration()  // Prevents crashes due to schema changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
