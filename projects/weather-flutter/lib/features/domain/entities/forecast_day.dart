class ForecastDayEntity {
  final String day;
  final String condition;
  final int high;
  final int low;

  const ForecastDayEntity({
    required this.day,
    required this.condition,
    required this.high,
    required this.low,
  });
}