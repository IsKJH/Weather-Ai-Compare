import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_weather/features/data/datasources/weather_local_datasource.dart';
import 'package:flutter_weather/features/data/repositories/weather_repositories_impl.dart';
import 'package:flutter_weather/features/domain/entities/weather.dart';

final weatherProvider = Provider<List<WeatherEntity>>((ref) {
  final WeatherLocalDatasource weatherLocalDatasource =
      WeatherLocalDatasource();
  final repository = WeatherRepositoriesImpl(weatherLocalDatasource);
  return repository.getWeather();
});

