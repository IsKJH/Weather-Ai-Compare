import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_weather/core/utils/weather_icon.dart';
import 'package:flutter_weather/features/presentation/favorite_city_provider.dart';
import 'package:flutter_weather/features/presentation/selected_city_provider.dart';
import 'package:flutter_weather/features/presentation/weather_provider.dart';

class WeatherScreen extends ConsumerStatefulWidget {
  const WeatherScreen({super.key});

  @override
  ConsumerState createState() => _WeatherScreenState();
}

class _WeatherScreenState extends ConsumerState<WeatherScreen> {
  DateTime realTime = DateTime.now();
  final GlobalKey<RefreshIndicatorState> _refreshKey = GlobalKey();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final weatherList = ref.read(weatherProvider);
      ref.read(selectedCityProvider.notifier).state = weatherList[0];
    });
  }

  void updateTime() async {
    await Future.delayed(Duration(seconds: 2));

    setState(() {
      realTime = DateTime.now();
    });
  }

  @override
  Widget build(BuildContext context) {
    final weatherList = ref.watch(weatherProvider);
    final favoriteCity = ref.watch(favoriteCityProvider);
    final selectedCity = ref.watch(selectedCityProvider) ?? weatherList[0];

    return Scaffold(
      backgroundColor: Color(0xFF2277CF),
      body: SafeArea(
        child: RefreshIndicator(
          key: _refreshKey,
          onRefresh: () async => updateTime(),
          child: SingleChildScrollView(
            child: Padding(
              padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 10),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  Row(
                    children: [
                      IconButton(
                        onPressed: () {
                          if (favoriteCity == selectedCity.city) {
                            ref.read(favoriteCityProvider.notifier).state =
                                null;
                          } else {
                            ref.read(favoriteCityProvider.notifier).state =
                                selectedCity.city;
                          }
                        },
                        icon: Icon(
                          favoriteCity == selectedCity.city
                              ? Icons.favorite
                              : Icons.favorite_border,
                          color: Colors.white,
                        ),
                      ),
                      Expanded(
                        child: Center(
                          child: Container(
                            decoration: BoxDecoration(
                              color: Color(0xFF4789d1),
                              borderRadius: BorderRadius.circular(32),
                            ),
                            child: Padding(
                              padding: const EdgeInsets.all(8),
                              child: Row(
                                mainAxisSize: MainAxisSize.min,
                                children: weatherList.map((weather) {
                                  final isSelected = selectedCity == weather;

                                  return GestureDetector(
                                    onTap: () =>
                                        ref
                                                .read(
                                                  selectedCityProvider.notifier,
                                                )
                                                .state =
                                            weather,
                                    child: AnimatedContainer(
                                      duration: Duration(milliseconds: 200),
                                      padding: EdgeInsets.symmetric(
                                        horizontal: 20,
                                        vertical: 12,
                                      ),
                                      decoration: BoxDecoration(
                                        color: isSelected
                                            ? Colors.white
                                            : Colors.transparent,
                                        borderRadius: BorderRadius.circular(20),
                                      ),
                                      child: Text(
                                        weather.city,
                                        style: TextStyle(
                                          color: isSelected
                                              ? Colors.blue
                                              : Colors.white,
                                          fontWeight: isSelected
                                              ? FontWeight.bold
                                              : null,
                                          fontSize: 16,
                                        ),
                                      ),
                                    ),
                                  );
                                }).toList(),
                              ),
                            ),
                          ),
                        ),
                      ),
                      IconButton(
                        onPressed: _refreshKey.currentState?.show,
                        icon: Icon(Icons.refresh, color: Colors.white),
                      ),
                    ],
                  ),
                  SizedBox(height: 12),
                  Text(
                    '마지막 업데이트: ${realTime.hour.toString().padLeft(2, '0')}:${realTime.minute.toString().padLeft(2, '0')}',
                    style: TextStyle(color: Colors.white70, fontSize: 14),
                  ),
                  SizedBox(height: 24),
                  Text(
                    selectedCity.city,
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  Text(
                    weatherToIcon(selectedCity.condition),
                    style: TextStyle(fontSize: 100),
                  ),
                  Text(
                    "${selectedCity.currentTemp}°C",
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 48,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  Text(
                    selectedCity.condition,
                    style: TextStyle(color: Colors.white, fontSize: 22),
                  ),
                  SizedBox(height: 24),
                  Container(
                    width: double.infinity,
                    padding: EdgeInsets.symmetric(vertical: 12, horizontal: 20),
                    decoration: BoxDecoration(
                      color: Color(0xFF589fe3),
                      borderRadius: BorderRadius.circular(22),
                    ),
                    child: Column(
                      spacing: 4,
                      children: [
                        Text(
                          "지금 나갈까? ${weatherGradeIcon(selectedCity)}",
                          style: TextStyle(
                            color: Colors.white,
                            fontSize: 22,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        Text(
                          weatherBriefMessage(selectedCity),
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            color: Colors.white70,
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        SizedBox(height: 4),
                        Wrap(
                          spacing: 8,
                          alignment: WrapAlignment.center,
                          children: weatherActionChips(selectedCity)
                              .map(
                                (chip) => Container(
                                  padding: EdgeInsets.symmetric(
                                    horizontal: 12,
                                    vertical: 7,
                                  ),
                                  decoration: BoxDecoration(
                                    color: Colors.white.withValues(alpha: 0.18),
                                    borderRadius: BorderRadius.circular(999),
                                    border: Border.all(
                                      color: Colors.white.withValues(
                                        alpha: 0.24,
                                      ),
                                    ),
                                  ),
                                  child: Text(
                                    chip,
                                    style: TextStyle(
                                      color: Colors.white,
                                      fontSize: 13,
                                      fontWeight: FontWeight.w600,
                                    ),
                                  ),
                                ),
                              )
                              .toList(),
                        ),
                      ],
                    ),
                  ),
                  SizedBox(height: 24),
                  Row(
                    spacing: 12,
                    children: [
                      Expanded(
                        child: Container(
                          padding: EdgeInsets.symmetric(
                            vertical: 12,
                            horizontal: 20,
                          ),
                          decoration: BoxDecoration(
                            color: Color(0xFF589fe3),
                            borderRadius: BorderRadius.circular(22),
                          ),
                          child: Column(
                            spacing: 4,
                            children: [
                              Text(
                                weatherToIcon("체감온도"),
                                style: TextStyle(fontSize: 32),
                              ),
                              Text(
                                "${selectedCity.feelsLike}°C",
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 16,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              Text(
                                "체감온도",
                                style: TextStyle(
                                  color: Colors.white70,
                                  fontSize: 12,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                      Expanded(
                        child: Container(
                          padding: EdgeInsets.symmetric(
                            vertical: 12,
                            horizontal: 20,
                          ),
                          decoration: BoxDecoration(
                            color: Color(0xFF589fe3),
                            borderRadius: BorderRadius.circular(22),
                          ),
                          child: Column(
                            spacing: 4,
                            children: [
                              Text(
                                weatherToIcon("습도"),
                                style: TextStyle(fontSize: 32),
                              ),
                              Text(
                                "${selectedCity.humidity}%",
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 16,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              Text(
                                "습도",
                                style: TextStyle(
                                  color: Colors.white70,
                                  fontSize: 12,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                      Expanded(
                        child: Container(
                          padding: EdgeInsets.symmetric(
                            vertical: 12,
                            horizontal: 20,
                          ),
                          decoration: BoxDecoration(
                            color: Color(0xFF589fe3),
                            borderRadius: BorderRadius.circular(22),
                          ),
                          child: Column(
                            spacing: 4,
                            children: [
                              Text(
                                weatherToIcon("풍속"),
                                style: TextStyle(fontSize: 32),
                              ),
                              Text(
                                "${selectedCity.windSpeed}m/s",
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 16,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              Text(
                                "풍속",
                                style: TextStyle(
                                  color: Colors.white70,
                                  fontSize: 12,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                  SizedBox(height: 12),
                  Row(
                    spacing: 12,
                    children: [
                      Expanded(
                        child: Container(
                          padding: EdgeInsets.symmetric(
                            vertical: 12,
                            horizontal: 20,
                          ),
                          decoration: BoxDecoration(
                            color: Color(0xFF589fe3),
                            borderRadius: BorderRadius.circular(22),
                          ),
                          child: Column(
                            spacing: 4,
                            children: [
                              Text(
                                weatherToIcon("강수확률"),
                                style: TextStyle(fontSize: 32),
                              ),
                              Text(
                                "${selectedCity.precipitation}%",
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 16,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              Text(
                                "강수확률",
                                style: TextStyle(
                                  color: Colors.white70,
                                  fontSize: 12,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                      Expanded(
                        child: Container(
                          padding: EdgeInsets.symmetric(
                            vertical: 12,
                            horizontal: 20,
                          ),
                          decoration: BoxDecoration(
                            color: Color(0xFF589fe3),
                            borderRadius: BorderRadius.circular(22),
                          ),
                          child: Column(
                            spacing: 4,
                            children: [
                              Text(
                                weatherToIcon("자외선 지수"),
                                style: TextStyle(fontSize: 32),
                              ),
                              Text(
                                "${selectedCity.uvIndex}",
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 16,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              Text(
                                "자외선 지수",
                                style: TextStyle(
                                  color: Colors.white70,
                                  fontSize: 12,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                      Expanded(
                        child: Container(
                          padding: EdgeInsets.symmetric(
                            vertical: 12,
                            horizontal: 20,
                          ),
                          decoration: BoxDecoration(
                            color: Color(0xFF589fe3),
                            borderRadius: BorderRadius.circular(22),
                          ),
                          child: Column(
                            spacing: 4,
                            children: [
                              Text(
                                weatherToIcon("대기질"),
                                style: TextStyle(fontSize: 32),
                              ),
                              Text(
                                selectedCity.airQuality,
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 16,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              Text(
                                "대기질",
                                style: TextStyle(
                                  color: Colors.white70,
                                  fontSize: 12,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                  SizedBox(height: 24),
                  Container(
                    padding: EdgeInsets.symmetric(vertical: 12, horizontal: 20),
                    decoration: BoxDecoration(
                      color: Color(0xFF589fe3),
                      borderRadius: BorderRadius.circular(22),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          "시간별 예보",
                          style: TextStyle(
                            color: Colors.white70,
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        Divider(),
                        SingleChildScrollView(
                          scrollDirection: Axis.horizontal,
                          child: Row(
                            children: selectedCity.hourly
                                .map(
                                  (hour) => Row(
                                    children: [
                                      Column(
                                        spacing: 8,
                                        children: [
                                          Text(
                                            hour.time,
                                            style: TextStyle(
                                              color: Colors.white,
                                              fontSize: 16,
                                            ),
                                          ),
                                          Text(
                                            weatherToIcon(hour.condition),
                                            style: TextStyle(
                                              color: Colors.white,
                                              fontSize: 16,
                                            ),
                                          ),
                                          Text(
                                            "${hour.temp}°",
                                            style: TextStyle(
                                              color: Colors.white,
                                              fontSize: 16,
                                            ),
                                          ),
                                        ],
                                      ),
                                      SizedBox(width: 12),
                                    ],
                                  ),
                                )
                                .toList(),
                          ),
                        ),
                      ],
                    ),
                  ),
                  SizedBox(height: 24),
                  Container(
                    padding: EdgeInsets.symmetric(vertical: 12, horizontal: 20),
                    decoration: BoxDecoration(
                      color: Color(0xFF589fe3),
                      borderRadius: BorderRadius.circular(22),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          "5일 예보",
                          style: TextStyle(
                            color: Colors.white70,
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        Divider(),
                        ListView.separated(
                          shrinkWrap: true,
                          physics: NeverScrollableScrollPhysics(),
                          itemCount: selectedCity.forecast.length,
                          separatorBuilder: (context, idx) =>
                              SizedBox(height: 8),
                          itemBuilder: (context, index) => Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Expanded(
                                flex: 1,
                                child: Text(
                                  selectedCity.forecast[index].day,
                                  style: TextStyle(
                                    color: Colors.white,
                                    fontSize: 16,
                                  ),
                                ),
                              ),
                              Expanded(
                                flex: 3,
                                child: Row(
                                  children: [
                                    Text(
                                      weatherToIcon(
                                        selectedCity.forecast[index].condition,
                                      ),
                                      style: TextStyle(
                                        color: Colors.white,
                                        fontSize: 16,
                                      ),
                                    ),
                                    Text(
                                      selectedCity.forecast[index].condition,
                                      style: TextStyle(
                                        color: Colors.white,
                                        fontSize: 16,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                              Text(
                                "${selectedCity.forecast[index].high}°/${selectedCity.forecast[index].low}°",
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 16,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
