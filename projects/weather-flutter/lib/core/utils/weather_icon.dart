import 'package:flutter_weather/features/domain/entities/weather.dart';

String weatherToIcon(String condition) {
  if (condition.contains('맑음')) return '☀️';
  if (condition.contains('구름')) return '⛅';
  if (condition.contains('흐림')) return '☁️';
  if (condition.contains('비')) return '🌧';
  if (condition.contains('눈')) return '❄️';
  if (condition.contains('뇌우')) return '⛈️';
  if (condition.contains('체감온도')) return '🌡️';
  if (condition.contains('습도')) return '💧';
  if (condition.contains('풍속')) return '💨';
  if (condition.contains('강수확률')) return '🌂';
  if (condition.contains('자외선 지수')) return '🔆';
  if (condition.contains('대기질')) return '🌿';

  return '🌤️';
}

String weatherGradeIcon(WeatherEntity weather) {
  int riskNum = 0;
  if (weather.humidity >= 60) {
    riskNum++;
  }
  if (weather.condition == "비" || weather.precipitation >= 60) {
    riskNum++;
  }
  if (weather.uvIndex >= 8) {
    riskNum++;
  }
  if (weather.currentTemp >= 30) {
    riskNum++;
  }
  if (weather.currentTemp <= 5) {
    riskNum++;
  }

  return riskNum == 1
      ? "😐"
      : riskNum > 1
      ? "😣"
      : "😊";
}

String weatherBriefMessage(WeatherEntity weather) {
  if (weather.humidity >= 60) {
    return "습도가 높아 외출할 때 주의가 필요해요.";
  }
  if (weather.condition.contains("비") || weather.precipitation >= 60) {
    return "비 가능성이 있어 우산을 챙기는 게 좋아요.";
  }
  if (weather.uvIndex >= 8) {
    return "자외선이 강해서 선크림을 바르는 게 좋아요.";
  }
  if (weather.windSpeed >= 6) {
    return "바람이 강해서 외출할 때 주의가 필요해요.";
  }
  if (weather.currentTemp >= 30 || weather.feelsLike >= 30) {
    return "덥게 느껴질 수 있어 물을 챙기는 게 좋아요.";
  }

  return "날씨가 안정적이라 가볍게 외출하기 좋아요.";
}

List<String> weatherActionChips(WeatherEntity weather) {
  final chips = <String>[];
  if (weather.condition.contains("비") || weather.precipitation >= 60) {
    chips.add("☔ 우산");
  }
  if (weather.uvIndex >= 8) {
    chips.add("🧴 선크림");
  }
  if (weather.windSpeed >= 6) {
    chips.add("💨 바람 주의");
  }
  if (weather.currentTemp >= 30 || weather.feelsLike >= 30) {
    chips.add("💧 물 챙기기");
  }

  if (chips.isEmpty) {
    chips.addAll(["🚶 산책 좋음", "🍃 가벼운 외출"]);
  }

  return chips.take(3).toList();
}
