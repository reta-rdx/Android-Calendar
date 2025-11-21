package com.example.calendar.util

import java.util.Calendar

object LunarCalendar {
    // 农历月份名称
    private val lunarMonthNames = arrayOf(
        "正", "二", "三", "四", "五", "六",
        "七", "八", "九", "十", "冬", "腊"
    )
    
    // 农历日期名称
    private val lunarDayNames = arrayOf(
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )
    
    // 天干
    private val gan = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    
    // 地支
    private val zhi = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
    
    // 生肖
    private val zodiac = arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")
    
    /**
     * 获取农历日期字符串（简化版本）
     * 注意：这是一个简化实现，实际农历转换需要复杂的算法
     */
    fun getLunarDateString(calendar: Calendar): String {
        // 这里使用简化算法，实际应该使用完整的农历转换算法
        // 为了演示，我们使用一个简单的映射
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        // 简化版本：使用一个简单的公式来模拟农历
        // 实际应用中应该使用完整的农历转换库
        val lunarMonth = ((month - 1) % 12)
        val lunarDay = ((day - 1) % 30)
        
        return "${lunarMonthNames[lunarMonth]}月${lunarDayNames[lunarDay]}"
    }
    
    /**
     * 获取天干地支年份
     */
    fun getGanZhiYear(calendar: Calendar): String {
        val year = calendar.get(Calendar.YEAR)
        // 1984年是甲子年
        val baseYear = 1984
        val offset = (year - baseYear) % 60
        val ganIndex = offset % 10
        val zhiIndex = offset % 12
        return "${gan[ganIndex]}${zhi[zhiIndex]}"
    }
    
    /**
     * 获取生肖
     */
    fun getZodiac(calendar: Calendar): String {
        val year = calendar.get(Calendar.YEAR)
        // 1984年是鼠年
        val baseYear = 1984
        val offset = (year - baseYear) % 12
        return zodiac[offset]
    }
    
    /**
     * 获取完整的农历信息
     */
    fun getFullLunarInfo(calendar: Calendar): String {
        val ganZhi = getGanZhiYear(calendar)
        val zodiacStr = getZodiac(calendar)
        val lunarDate = getLunarDateString(calendar)
        return "${ganZhi}年($zodiacStr) $lunarDate"
    }
}

