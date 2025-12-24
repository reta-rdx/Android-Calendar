package com.example.calendar.util

import android.util.Log
import java.util.Calendar

object LunarLibraryChecker {
    private const val TAG = "LunarLibraryChecker"
    
    fun checkLunarLibraryAvailability(): Boolean {
        return try {
            // 尝试多种可能的类名
            val possibleClassNames = listOf(
                "com.nlf.calendar.Solar",
                "com.github.6tail.lunar.Solar",
                "lunar.Solar",
                "cn.6tail.lunar.Solar"
            )
            
            var foundClass: Class<*>? = null
            var foundClassName = ""
            
            for (className in possibleClassNames) {
                try {
                    foundClass = Class.forName(className)
                    foundClassName = className
                    Log.d(TAG, "找到Solar类: $className")
                    break
                } catch (e: ClassNotFoundException) {
                    Log.d(TAG, "未找到类: $className")
                    continue
                }
            }
            
            if (foundClass == null) {
                Log.e(TAG, "未找到任何Solar类")
                return false
            }
            
            // 尝试调用静态方法
            val fromYmdMethod = foundClass.getMethod("fromYmd", Int::class.java, Int::class.java, Int::class.java)
            Log.d(TAG, "fromYmd方法获取成功: ${fromYmdMethod.name}")
            
            // 尝试创建实例
            val solar = fromYmdMethod.invoke(null, 2024, 12, 21)
            Log.d(TAG, "Solar实例创建成功: $solar")
            
            // 尝试获取Lunar对象
            val getLunarMethod = foundClass.getMethod("getLunar")
            val lunar = getLunarMethod.invoke(solar)
            Log.d(TAG, "Lunar对象获取成功: $lunar")
            
            // 尝试调用农历方法
            val dayMethod = lunar.javaClass.getMethod("getDayInChinese")
            val dayStr = dayMethod.invoke(lunar) as String
            Log.d(TAG, "农历日期获取成功: $dayStr")
            
            Log.d(TAG, "使用的类名: $foundClassName")
            true
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "农历库类未找到", e)
            false
        } catch (e: NoSuchMethodException) {
            Log.e(TAG, "农历库方法未找到", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "农历库调用失败", e)
            false
        }
    }
    
    fun getDetailedLibraryInfo(): String {
        return try {
            val solarClass = Class.forName("com.nlf.calendar.Solar")
            val solar = solarClass.getMethod("fromYmd", Int::class.java, Int::class.java, Int::class.java)
                .invoke(null, 2024, 12, 21)
            val lunar = solarClass.getMethod("getLunar").invoke(solar)
            
            val dayStr = lunar.javaClass.getMethod("getDayInChinese").invoke(lunar) as String
            val monthStr = lunar.javaClass.getMethod("getMonthInChinese").invoke(lunar) as String
            val ganZhi = lunar.javaClass.getMethod("getYearInGanZhi").invoke(lunar) as String
            val zodiac = lunar.javaClass.getMethod("getYearShengXiao").invoke(lunar) as String
            
            "农历库正常工作\n农历日期: ${monthStr}月${dayStr}\n天干地支: $ganZhi\n生肖: $zodiac"
        } catch (e: Exception) {
            "农历库不可用: ${e.message}"
        }
    }
}