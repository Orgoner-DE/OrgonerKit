# Samsung UE32J4570 Android Remote

Debug APK: built by GitHub Actions. App supports Samsung legacy TCP :55000 and Samsung WebSocket :8001, plus IR if the Android phone contains an infrared emitter. TV and phone must be on the same home network. The UE32J4570 2015 J4xxx is not in Samsung's documented Smart View SDK compatible 2015 range (J5500+), so network remote control may not work. Samsung documentation: https://developer.samsung.com/smarttv/develop/extension-libraries/smart-view-sdk/supported-device/supported-tvs.html

The app displays whether it found a reachable network remote protocol. It does not send Bluetooth signals. A successful TCP connection alone does not confirm actual TV control. Powering on over Wi-Fi is firmware/hardware dependent. No TV hardware tests performed.
