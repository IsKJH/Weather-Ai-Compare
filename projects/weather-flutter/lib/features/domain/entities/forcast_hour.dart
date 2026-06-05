class ForecastHourEntity {
  final String time;
  final String condition;
  final int temp;
  final int precipitation;

  const ForecastHourEntity({
    required this.time,
    required this.condition,
    required this.temp,
    required this.precipitation,
  });
}
