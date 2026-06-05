import 'package:flutter_weather/features/domain/entities/forcast_hour.dart';
import 'package:flutter_weather/features/domain/entities/forecast_day.dart';
import 'package:flutter_weather/features/domain/entities/weather.dart';

class WeatherLocalDatasource {
  List<WeatherEntity> getWeather() {
    final mockWeatherList = [
      WeatherEntity(
        city: '서울',
        currentTemp: 23,
        feelsLike: 21,
        condition: '맑음',
        humidity: 55,
        windSpeed: 3.2,
        forecast: [
          ForecastDayEntity(day: '월', condition: '맑음', high: 25, low: 16),
          ForecastDayEntity(day: '화', condition: '구름많음', high: 22, low: 15),
          ForecastDayEntity(day: '수', condition: '비', high: 18, low: 13),
          ForecastDayEntity(day: '목', condition: '흐림', high: 20, low: 14),
          ForecastDayEntity(day: '금', condition: '맑음', high: 26, low: 17),
        ],
        precipitation: 10,
        uvIndex: 6,
        airQuality: '보통',
        hourly: [
          ForecastHourEntity(
            time: '지금',
            condition: '맑음',
            temp: 23,
            precipitation: 5,
          ),
          ForecastHourEntity(
            time: '오후 1시',
            condition: '맑음',
            temp: 24,
            precipitation: 5,
          ),
          ForecastHourEntity(
            time: '오후 2시',
            condition: '맑음',
            temp: 25,
            precipitation: 5,
          ),
          ForecastHourEntity(
            time: '오후 3시',
            condition: '구름많음',
            temp: 24,
            precipitation: 15,
          ),
          ForecastHourEntity(
            time: '오후 4시',
            condition: '구름많음',
            temp: 23,
            precipitation: 20,
          ),
          ForecastHourEntity(
            time: '오후 5시',
            condition: '흐림',
            temp: 21,
            precipitation: 30,
          ),
          ForecastHourEntity(
            time: '오후 6시',
            condition: '비',
            temp: 19,
            precipitation: 70,
          ),
          ForecastHourEntity(
            time: '오후 7시',
            condition: '비',
            temp: 18,
            precipitation: 80,
          ),
        ],
      ),
      WeatherEntity(
        city: '부산',
        currentTemp: 26,
        feelsLike: 28,
        condition: '구름많음',
        humidity: 72,
        windSpeed: 5.1,
        forecast: [
          ForecastDayEntity(day: '월', condition: '구름많음', high: 27, low: 20),
          ForecastDayEntity(day: '화', condition: '비', high: 24, low: 19),
          ForecastDayEntity(day: '수', condition: '비', high: 22, low: 18),
          ForecastDayEntity(day: '목', condition: '맑음', high: 25, low: 19),
          ForecastDayEntity(day: '금', condition: '맑음', high: 28, low: 21),
        ],
        precipitation: 40,
        uvIndex: 4,
        airQuality: '보통',
        hourly: [
          ForecastHourEntity(
            time: '지금',
            condition: '구름많음',
            temp: 26,
            precipitation: 30,
          ),
          ForecastHourEntity(
            time: '오후 1시',
            condition: '구름많음',
            temp: 27,
            precipitation: 35,
          ),
          ForecastHourEntity(
            time: '오후 2시',
            condition: '비',
            temp: 25,
            precipitation: 60,
          ),
          ForecastHourEntity(
            time: '오후 3시',
            condition: '비',
            temp: 24,
            precipitation: 70,
          ),
          ForecastHourEntity(
            time: '오후 4시',
            condition: '비',
            temp: 23,
            precipitation: 65,
          ),
          ForecastHourEntity(
            time: '오후 5시',
            condition: '흐림',
            temp: 22,
            precipitation: 40,
          ),
          ForecastHourEntity(
            time: '오후 6시',
            condition: '흐림',
            temp: 22,
            precipitation: 30,
          ),
          ForecastHourEntity(
            time: '오후 7시',
            condition: '구름많음',
            temp: 21,
            precipitation: 20,
          ),
        ],
      ),
      WeatherEntity(
        city: '제주',
        currentTemp: 28,
        feelsLike: 30,
        condition: '맑음',
        humidity: 68,
        windSpeed: 6.8,
        forecast: [
          ForecastDayEntity(day: '월', condition: '맑음', high: 29, low: 22),
          ForecastDayEntity(day: '화', condition: '맑음', high: 30, low: 23),
          ForecastDayEntity(day: '수', condition: '구름많음', high: 27, low: 21),
          ForecastDayEntity(day: '목', condition: '비', high: 24, low: 20),
          ForecastDayEntity(day: '금', condition: '흐림', high: 25, low: 21),
        ],
        precipitation: 5,
        uvIndex: 9,
        airQuality: '좋음',
        hourly: [
          ForecastHourEntity(
            time: '지금',
            condition: '맑음',
            temp: 28,
            precipitation: 0,
          ),
          ForecastHourEntity(
            time: '오후 1시',
            condition: '맑음',
            temp: 29,
            precipitation: 0,
          ),
          ForecastHourEntity(
            time: '오후 2시',
            condition: '맑음',
            temp: 30,
            precipitation: 5,
          ),
          ForecastHourEntity(
            time: '오후 3시',
            condition: '맑음',
            temp: 30,
            precipitation: 5,
          ),
          ForecastHourEntity(
            time: '오후 4시',
            condition: '맑음',
            temp: 29,
            precipitation: 5,
          ),
          ForecastHourEntity(
            time: '오후 5시',
            condition: '맑음',
            temp: 28,
            precipitation: 10,
          ),
          ForecastHourEntity(
            time: '오후 6시',
            condition: '맑음',
            temp: 27,
            precipitation: 10,
          ),
          ForecastHourEntity(
            time: '오후 7시',
            condition: '맑음',
            temp: 26,
            precipitation: 5,
          ),
        ],
      ),
    ];
    return mockWeatherList;
  }
}
