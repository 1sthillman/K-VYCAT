# MXW01 Printer App

Modern Android application for MXW01 Bluetooth thermal printer.

## Features

- ✅ **Bluetooth Connectivity** - Automatic device scanning and connection
- ✅ **Text Printing** - Multi-line text with auto font sizing
- ✅ **Image Printing** - Print photos and images
- ✅ **Banner Mode** - Large text printing
- ✅ **Material Design 3** - Modern, beautiful UI
- ✅ **Multiple Paper Sizes** - 58mm and 80mm support
- ✅ **Auto-Reconnect** - Remembers last connected device
- ✅ **Print Queue** - Multiple copies support

## Screenshots

[Add screenshots here]

## Installation

### From Releases
1. Download latest APK from [Releases](../../releases)
2. Enable "Install from Unknown Sources" in Android settings
3. Install APK
4. Grant Bluetooth and Location permissions

### Build from Source
```bash
git clone https://github.com/YOUR_USERNAME/MXWPrinter.git
cd MXWPrinter
./gradlew assembleDebug
```

APK will be in `app/build/outputs/apk/debug/`

## Usage

1. **Connect to Printer**
   - Open app
   - Tap "Scan" button
   - Select your MXW01 printer from list
   - Wait for connection

2. **Print Text**
   - Enter text in input field
   - Adjust font size if needed
   - Tap "Print" button

3. **Print Image**
   - Tap "Image" tab
   - Select image from gallery
   - Adjust settings
   - Tap "Print" button

## Supported Printers

- MXW01 (MAC: 48:0F:57:3E:60:77)
- Other models may work but are untested

## Technical Details

### Bluetooth Protocol
- Service UUID: `0000ae00-0000-1000-8000-00805f9b34fb`
- CMD Characteristic: `0000ae01-0000-1000-8000-00805f9b34fb`
- DATA Characteristic: `0000ae03-0000-1000-8000-00805f9b34fb`

### Print Resolution
- 384 pixels width (58mm paper)
- 48 bytes per row
- LSB first encoding

### Implementation
Inspired by [Cat-Printer](https://github.com/NaitLee/Cat-Printer) project.

## Requirements

- Android 8.0 (API 26) or higher
- Bluetooth Low Energy (BLE) support
- Location permission (required for BLE scanning on Android 10+)

## Permissions

- `BLUETOOTH` - Connect to printer
- `BLUETOOTH_ADMIN` - Scan for devices
- `BLUETOOTH_SCAN` - Android 12+ scanning
- `BLUETOOTH_CONNECT` - Android 12+ connection
- `ACCESS_FINE_LOCATION` - Required for BLE scanning
- `ACCESS_COARSE_LOCATION` - Required for BLE scanning

## Troubleshooting

### Printer not found
- Ensure printer is powered on
- Check Bluetooth is enabled
- Grant location permission
- Try increasing scan time in settings

### Connection fails
- Restart printer
- Clear Bluetooth cache in Android settings
- Restart app

### Print quality issues
- Adjust heat level in settings
- Check paper quality
- Clean printer head

## Development

### Project Structure
```
app/
├── src/main/
│   ├── java/com/mxw/printer/
│   │   ├── ble/              # Bluetooth logic
│   │   ├── model/            # Data models
│   │   ├── ui/               # UI components
│   │   └── MainActivity.kt
│   └── res/                  # Resources
├── build.gradle.kts
└── proguard-rules.pro
```

### Key Classes
- `BleManager.kt` - Bluetooth connection and printing
- `BleManagerSimple.kt` - Simplified implementation (Cat-Printer style)
- `PrinterProtocol.kt` - Protocol commands and encoding
- `MainActivity.kt` - Main UI

### Building
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Check code style
./gradlew ktlintCheck
```

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

[Add your license here]

## Credits

- Inspired by [Cat-Printer](https://github.com/NaitLee/Cat-Printer)
- Material Design 3 components
- Kotlin Coroutines for async operations

## Support

For issues and questions:
- Open an [Issue](../../issues)
- Check [Discussions](../../discussions)

## Changelog

### v1.0.0 (Latest)
- Initial release
- Basic text and image printing
- Material Design 3 UI
- Auto-reconnect feature
- Multiple paper sizes

---

Made with ❤️ for MXW01 printer users
