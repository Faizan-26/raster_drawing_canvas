// Save ImageAS ENUM
enum ImageFormat {
  png,
  jpeg,
  jpg,
  webp,
  // Android API 30+ supports HEIF
  heif,
}

enum ImageQuality {
  low, // 30%
  medium, // 60%
  high, // 100%
}

extension ImageQualityValue on ImageQuality {
  int get value {
    switch (this) {
      case ImageQuality.low:
        return 30;
      case ImageQuality.medium:
        return 60;
      case ImageQuality.high:
        return 100;
    }
  }
}
