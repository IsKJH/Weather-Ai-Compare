package com.example.weathernow

import androidx.compose.ui.graphics.Color

data class SmartInsight(
    val comfortScore: Int,
    val grade: String,
    val emoji: String,
    val headline: String,
    val activityTip: String,
    val outfitTip: String,
    val factors: List<String>
)

fun computeSmartInsight(weather: WeatherData): SmartInsight {
    var score = 78

    when {
        weather.currentTemp in 20..26 -> score += 12
        weather.currentTemp in 16..19 || weather.currentTemp in 27..29 -> score += 4
        weather.currentTemp >= 30 -> score -= 18
        weather.currentTemp <= 10 -> score -= 20
        else -> score -= 6
    }

    when {
        weather.precipitationProbability >= 60 || weather.condition == "비" -> score -= 22
        weather.precipitationProbability >= 30 -> score -= 10
    }

    when (weather.uvIndex) {
        in 8..11 -> score -= 8
        in 6..7 -> score -= 3
    }

    when (weather.airQuality) {
        "나쁨" -> score -= 14
        "매우나쁨" -> score -= 22
        "보통" -> score -= 4
    }

    if (weather.humidity >= 80) score -= 6
    if (weather.windSpeed >= 7.0) score -= 5

    val comfortScore = score.coerceIn(0, 100)
    val grade = when {
        comfortScore >= 80 -> "매우 쾌적"
        comfortScore >= 65 -> "쾌적"
        comfortScore >= 45 -> "보통"
        else -> "주의"
    }

    val emoji = when {
        weather.precipitationProbability >= 60 || weather.condition == "비" -> "🌂"
        comfortScore >= 80 -> "☀️"
        comfortScore >= 65 -> "🚶"
        comfortScore >= 45 -> "🧥"
        else -> "🏠"
    }

    val headline = when {
        weather.precipitationProbability >= 60 || weather.condition == "비" ->
            "우산 챙기고 나가세요"
        weather.currentTemp >= 30 ->
            "무더위 — 실외 활동 자제"
        comfortScore >= 80 ->
            "야외 활동하기 좋은 날"
        comfortScore >= 65 ->
            "가벼운 외출에 적합"
        comfortScore >= 45 ->
            "날씨 변화에 대비하세요"
        else ->
            "실내 활동을 추천해요"
    }

    val activityTip = when {
        weather.precipitationProbability >= 60 || weather.condition == "비" ->
            "실내 카페·전시 위주로 일정을 잡아보세요."
        weather.currentTemp >= 28 && weather.uvIndex >= 7 ->
            "오전·저녁 야외, 한낮은 그늘에서 쉬어가세요."
        comfortScore >= 75 ->
            "공원 산책이나 가벼운 러닝이 잘 맞습니다."
        weather.currentTemp <= 12 ->
            "짧은 외출 후 따뜻한 실내로 돌아오세요."
        else ->
            "무리하지 않는 속도로 이동하세요."
    }

    val outfitTip = when {
        weather.currentTemp >= 28 ->
            "통풍 잘 되는 반팔·모자, 수분 보충 필수"
        weather.currentTemp >= 22 ->
            "얇은 겉옷 하나면 충분해요"
        weather.currentTemp >= 15 ->
            "가벼운 재킷 또는 맨투맨 추천"
        weather.currentTemp >= 5 ->
            "코트·니트 등 보온 레이어"
        else ->
            "패딩·목도리 등 겨울 외출복"
    }

    val factors = buildList {
        add("기온 ${weather.currentTemp}°C · 체감 ${weather.feelsLike}°C")
        add("강수확률 ${weather.precipitationProbability}%")
        if (weather.uvIndex >= 6) add("자외선 지수 ${weather.uvIndex}")
        if (weather.airQuality != "좋음") add("대기질 ${weather.airQuality}")
    }

    return SmartInsight(
        comfortScore = comfortScore,
        grade = grade,
        emoji = emoji,
        headline = headline,
        activityTip = activityTip,
        outfitTip = outfitTip,
        factors = factors
    )
}

fun backgroundColorsFor(temp: Int): List<Color> = when {
    temp >= 30 -> listOf(Color(0xFFFFF1E6), Color(0xFFFFE4CC), Color(0xFFFFF8F0))
    temp >= 26 -> listOf(Color(0xFFECFEFF), Color(0xFFD1FAE5), Color(0xFFE0F2FE))
    temp >= 20 -> listOf(Color(0xFFECFEFF), Color(0xFFFFFFFF), Color(0xFFE0F2FE))
    temp >= 14 -> listOf(Color(0xFFEFF6FF), Color(0xFFF0FDF4), Color(0xFFE2E8F0))
    else -> listOf(Color(0xFFE0E7FF), Color(0xFFF8FAFC), Color(0xFFDBEAFE))
}
