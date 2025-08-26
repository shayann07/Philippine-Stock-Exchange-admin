package com.codingempire.adminpse.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.codingempire.adminpse.models.EarningsModel
import com.codingempire.adminpse.models.InvestmentModel

class Converters {
    private val gson = Gson()

//    @TypeConverter
//    fun fromTimestamp(timestamp: Timestamp?): Long? {
//        return timestamp?.seconds
//    }
//
//    @TypeConverter
//    fun toTimestamp(seconds: Long?): Timestamp? {
//        return seconds?.let { Timestamp(it, 0) }
//    }
    @TypeConverter
    fun fromInvestmentModel(investment: InvestmentModel): String {
        return gson.toJson(investment)
    }

    @TypeConverter
    fun toInvestmentModel(data: String): InvestmentModel {
        return gson.fromJson(data, InvestmentModel::class.java)
    }

    @TypeConverter
    fun fromEarningsModel(earnings: EarningsModel): String {
        return gson.toJson(earnings)
    }

    @TypeConverter
    fun toEarningsModel(data: String): EarningsModel {
        return gson.fromJson(data, EarningsModel::class.java)
    }
}
