import 'package:flutter_weather/features/domain/entities/weather.dart';

abstract interface class WeatherRepository {
  List<WeatherEntity> getWeather();
}
