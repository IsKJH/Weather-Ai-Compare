import 'package:flutter_riverpod/legacy.dart';
import 'package:flutter_weather/features/domain/entities/weather.dart';

final selectedCityProvider = StateProvider<WeatherEntity?>((ref) => null);
