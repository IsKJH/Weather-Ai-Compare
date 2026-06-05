import 'package:flutter_weather/features/domain/entities/forcast_hour.dart';
import 'package:flutter_weather/features/domain/entities/forecast_day.dart';

class WeatherEntity {
  final String city;
  final int currentTemp;
  final int feelsLike;
  final String condition;
  final int humidity;
  final double windSpeed;
  final List<ForecastDayEntity> forecast;
  final int precipitation;
  final int uvIndex;
  final String airQuality;
  final List<ForecastHourEntity> hourly;

  const WeatherEntity({
    required this.city,
    required this.currentTemp,
    required this.feelsLike,
    required this.condition,
    required this.humidity,
    required this.windSpeed,
    required this.forecast,
    required this.precipitation,
    required this.uvIndex,
    required this.airQuality,
    required this.hourly,
  });
}
