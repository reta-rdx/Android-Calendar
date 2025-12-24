package com.example.calendar.util

import android.util.LruCache
import java.util.Calendar

object LunarCalendarUtil {
    
    // 添加LRU缓存，缓存农历计算结果
    private val lunarCache = LruCache<String, String>(100) // 缓存100个结果
    private val solarTermCache = LruCache<String, String?>(50) // 缓存50个节气结果
    
    // 生成缓存键
    private fun getCacheKey(year: Int, month: Int, day: Int): String {
        return "$year-$month-$day"
    }
    
    fun getFullLunarInfo(calendar: Calendar): String {
        val (year, month, day) = getDateComponents(calendar)
        val cacheKey = "full-${getCacheKey(year, month, day)}"
        
        // 先检查缓存
        lunarCache.get(cacheKey)?.let { return it }
        
        return try {
            val (solar, lunar) = getSolarAndLunar(year, month, day)
            
            // 获取天干地支年
            val ganZhiYear = try {
                val method = lunar.javaClass.getMethod("getYearInGanZhi")
                method.invoke(lunar) as String
            } catch (e: Exception) {
                return "农历信息获取失败"
            }
            
            // 获取生肖
            val zodiac = try {
                val method = lunar.javaClass.getMethod("getYearShengXiao")
                method.invoke(lunar) as String
            } catch (e: Exception) {
                return "农历信息获取失败"
            }
            
            // 获取农历日期
            val lunarDate = try {
                val monthMethod = lunar.javaClass.getMethod("getMonthInChinese")
                val dayMethod = lunar.javaClass.getMethod("getDayInChinese")
                val monthStr = monthMethod.invoke(lunar) as String
                val dayStr = dayMethod.invoke(lunar) as String
                "${monthStr}月${dayStr}"
            } catch (e: Exception) {
                return "农历信息获取失败"
            }
            
            val result = "${ganZhiYear}年(${zodiac}年) $lunarDate"
            lunarCache.put(cacheKey, result) // 缓存结果
            result
        } catch (e: Exception) {
            val fallback = "农历信息获取失败"
            lunarCache.put(cacheKey, fallback)
            fallback
        }
    }
    
    fun getLunarDateString(calendar: Calendar): String {
        val (year, month, day) = getDateComponents(calendar)
        val cacheKey = "date-${getCacheKey(year, month, day)}"
        
        // 先检查缓存
        lunarCache.get(cacheKey)?.let { return it }
        
        return try {
            val (_, lunar) = getSolarAndLunar(year, month, day)
            
            val monthMethod = lunar.javaClass.getMethod("getMonthInChinese")
            val dayMethod = lunar.javaClass.getMethod("getDayInChinese")
            val monthStr = monthMethod.invoke(lunar) as String
            val dayStr = dayMethod.invoke(lunar) as String
            
            val result = "${monthStr}月${dayStr}"
            lunarCache.put(cacheKey, result)
            result
        } catch (e: Exception) {
            val fallback = "农历"
            lunarCache.put(cacheKey, fallback)
            fallback
        }
    }
    
    fun getGanZhiYear(calendar: Calendar): String {
        val (year, month, day) = getDateComponents(calendar)
        val cacheKey = "ganzhi-${getCacheKey(year, month, day)}"
        
        lunarCache.get(cacheKey)?.let { return it }
        
        return try {
            val (_, lunar) = getSolarAndLunar(year, month, day)
            val method = lunar.javaClass.getMethod("getYearInGanZhi")
            val result = method.invoke(lunar) as String
            lunarCache.put(cacheKey, result)
            result
        } catch (e: Exception) {
            val fallback = ""
            lunarCache.put(cacheKey, fallback)
            fallback
        }
    }
    
    fun getZodiac(calendar: Calendar): String {
        val (year, month, day) = getDateComponents(calendar)
        val cacheKey = "zodiac-${getCacheKey(year, month, day)}"
        
        lunarCache.get(cacheKey)?.let { return it }
        
        return try {
            val (_, lunar) = getSolarAndLunar(year, month, day)
            val method = lunar.javaClass.getMethod("getYearShengXiao")
            val result = method.invoke(lunar) as String
            lunarCache.put(cacheKey, result)
            result
        } catch (e: Exception) {
            val fallback = ""
            lunarCache.put(cacheKey, fallback)
            fallback
        }
    }
    
    fun getSolarTerm(calendar: Calendar): String? {
        val (year, month, day) = getDateComponents(calendar)
        val cacheKey = getCacheKey(year, month, day)
        
        // 先检查缓存
        solarTermCache.get(cacheKey)?.let { return it }
        
        return try {
            val (solar, _) = getSolarAndLunar(year, month, day)
            
            // 尝试获取当天的节气
            try {
                // 方法1: 直接获取节气
                val jieQiMethod = solar.javaClass.getMethod("getJieQi")
                val result = jieQiMethod.invoke(solar) as? String
                if (!result.isNullOrEmpty()) {
                    solarTermCache.put(cacheKey, result)
                    return result
                }
            } catch (e: Exception) {
                // 继续尝试其他方法
            }
            
            try {
                // 方法2: 获取节气列表，检查当天是否是节气
                val jieQiListMethod = solar.javaClass.getMethod("getJieQiList")
                val jieQiList = jieQiListMethod.invoke(solar) as? List<*>
                
                // 检查当天是否在节气列表中
                jieQiList?.forEach { jieQiObj ->
                    try {
                        val nameMethod = jieQiObj?.javaClass?.getMethod("getName")
                        val solarMethod = jieQiObj?.javaClass?.getMethod("getSolar")
                        
                        val name = nameMethod?.invoke(jieQiObj) as? String
                        val jieQiSolar = solarMethod?.invoke(jieQiObj)
                        
                        if (jieQiSolar != null && name != null) {
                            val yearMethod = jieQiSolar.javaClass.getMethod("getYear")
                            val monthMethod = jieQiSolar.javaClass.getMethod("getMonth")
                            val dayMethod = jieQiSolar.javaClass.getMethod("getDay")
                            
                            val jieQiYear = yearMethod.invoke(jieQiSolar) as Int
                            val jieQiMonth = monthMethod.invoke(jieQiSolar) as Int
                            val jieQiDay = dayMethod.invoke(jieQiSolar) as Int
                            
                            if (jieQiYear == year && jieQiMonth == month && jieQiDay == day) {
                                solarTermCache.put(cacheKey, name)
                                return name
                            }
                        }
                    } catch (e: Exception) {
                        // 继续检查下一个
                    }
                }
            } catch (e: Exception) {
                // 继续尝试其他方法
            }
            
            try {
                // 方法3: 通过Lunar对象获取节气
                val (_, lunar) = getSolarAndLunar(year, month, day)
                val jieQiMethod = lunar.javaClass.getMethod("getJieQi")
                val result = jieQiMethod.invoke(lunar) as? String
                if (!result.isNullOrEmpty()) {
                    solarTermCache.put(cacheKey, result)
                    return result
                }
            } catch (e: Exception) {
                // 继续尝试其他方法
            }
            
            solarTermCache.put(cacheKey, null)
            null
        } catch (e: Exception) {
            solarTermCache.put(cacheKey, null)
            null
        }
    }
    
    fun getLunarFestival(calendar: Calendar): List<String>? {
        return try {
            val (year, month, day) = getDateComponents(calendar)
            val (_, lunar) = getSolarAndLunar(year, month, day)
            
            val method = lunar.javaClass.getMethod("getFestivals")
            @Suppress("UNCHECKED_CAST")
            val festivals = method.invoke(lunar) as? List<String>
            if (festivals?.isNotEmpty() == true) festivals else null
        } catch (e: Exception) {
            null
        }
    }
    
    fun getSolarFestival(calendar: Calendar): List<String>? {
        return try {
            val (year, month, day) = getDateComponents(calendar)
            val (solar, _) = getSolarAndLunar(year, month, day)
            
            val method = solar.javaClass.getMethod("getFestivals")
            @Suppress("UNCHECKED_CAST")
            val festivals = method.invoke(solar) as? List<String>
            if (festivals?.isNotEmpty() == true) festivals else null
        } catch (e: Exception) {
            null
        }
    }
    
    fun getConstellation(calendar: Calendar): String {
        val (year, month, day) = getDateComponents(calendar)
        val cacheKey = "constellation-${getCacheKey(year, month, day)}"
        
        lunarCache.get(cacheKey)?.let { return it }
        
        return try {
            val (solar, _) = getSolarAndLunar(year, month, day)
            val method = solar.javaClass.getMethod("getXingZuo")
            val result = method.invoke(solar) as String
            lunarCache.put(cacheKey, result)
            result
        } catch (e: Exception) {
            val fallback = ""
            lunarCache.put(cacheKey, fallback)
            fallback
        }
    }
    
    // 获取简洁的农历日期，用于在日历视图中显示 - 优化版本
    fun getSimpleLunarDate(calendar: Calendar): String {
        val (year, month, day) = getDateComponents(calendar)
        val cacheKey = "simple-${getCacheKey(year, month, day)}"
        
        // 先检查缓存
        lunarCache.get(cacheKey)?.let { return it }
        
        return try {
            // 直接获取农历日期，不检查节气
            val (_, lunar) = getSolarAndLunar(year, month, day)
            
            val dayMethod = lunar.javaClass.getMethod("getDayInChinese")
            val dayStr = dayMethod.invoke(lunar) as String
            
            // 如果是初一，显示月份，否则只显示日期
            val result = if (dayStr == "初一") {
                val monthMethod = lunar.javaClass.getMethod("getMonthInChinese")
                val monthStr = monthMethod.invoke(lunar) as String
                "${monthStr}月"
            } else {
                dayStr
            }
            
            android.util.Log.d("LunarCalendarUtil", "成功获取农历日期: $result (${year}-${month}-${day})")
            lunarCache.put(cacheKey, result)
            result
        } catch (e: Exception) {
            // 记录详细错误信息
            android.util.Log.e("LunarCalendarUtil", "农历库调用失败 (${year}-${month}-${day}): ${e.javaClass.simpleName} - ${e.message}", e)
            val fallback = "农历"
            lunarCache.put(cacheKey, fallback)
            fallback
        }
    }
    
    // 批量预计算农历信息，用于月视图优化
    fun precomputeLunarInfoForMonth(year: Int, month: Int): Map<String, String> {
        val results = mutableMapOf<String, String>()
        val calendar = Calendar.getInstance()
        
        // 计算当月的所有日期
        calendar.set(year, month - 1, 1) // month是1-12，Calendar.MONTH是0-11
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        for (day in 1..daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val cacheKey = getCacheKey(year, month, day)
            
            // 如果缓存中没有，则计算
            if (lunarCache.get("simple-$cacheKey") == null) {
                val lunarText = getSimpleLunarDate(calendar)
                results[cacheKey] = lunarText
            }
        }
        
        return results
    }
    
    // 清理缓存的方法（可选）
    fun clearCache() {
        lunarCache.evictAll()
        solarTermCache.evictAll()
    }
    
    // 提取的公共方法
    private fun getDateComponents(calendar: Calendar): Triple<Int, Int, Int> {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return Triple(year, month, day)
    }
    
    private fun getSolarAndLunar(year: Int, month: Int, day: Int): Pair<Any, Any> {
        try {
            // 使用6tail lunar库的正确类名
            val solarClass = Class.forName("com.nlf.calendar.Solar")
            val fromYmdMethod = solarClass.getMethod("fromYmd", Int::class.java, Int::class.java, Int::class.java)
            val solar = fromYmdMethod.invoke(null, year, month, day)
            
            val getLunarMethod = solarClass.getMethod("getLunar")
            val lunar = getLunarMethod.invoke(solar)
            
            return Pair(solar, lunar)
        } catch (e: Exception) {
            android.util.Log.e("LunarCalendarUtil", "农历库调用失败: ${e.message}", e)
            throw RuntimeException("农历库调用失败: ${e.message}", e)
        }
    }
}