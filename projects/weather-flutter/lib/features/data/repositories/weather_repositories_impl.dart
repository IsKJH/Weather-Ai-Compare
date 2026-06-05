import 'package:flutter_weather/features/data/datasources/weather_local_datasource.dart';
import 'package:flutter_weather/features/domain/entities/weather.dart';
import 'package:flutter_weather/features/domain/repositories/weather_repository.dart';

class WeatherRepositoriesImpl implements WeatherRepository {
  final WeatherLocalDatasource weatherLocalDatasource;

  WeatherRepositoriesImpl(this.weatherLocalDatasource);

  @override
  List<WeatherEntity> getWeather() {
    return weatherLocalDatasource.getWeather();
  }
}
