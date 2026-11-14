## Android app

A native Kotlin/Jetpack Compose client is available in the `android/` directory. It connects to the Node API at `/api/top-traded-fno`, shows the ranked stock list, supports symbol search, and refreshes once per minute.

### Run in Android Studio

1. Start the Node server from the repository root with `npm install` and `npm start`.
2. Open the `android/` directory in Android Studio and let Gradle sync.
3. Run the `app` configuration on an emulator. The default API address `http://10.0.2.2:3000` reaches the host computer from the Android emulator.
4. On a physical phone, use **API setup** in the app to enter your computer's LAN URL (for example `http://192.168.1.5:3000`) or a deployed HTTPS API URL. The phone and computer must be on the same network for a LAN URL.

The app stores the API base URL on the device. It does not contain FYERS credentials; keep those on the Node backend. Plain HTTP is enabled for local development only—use HTTPS for a deployed server.

### Build APK

In Android Studio, select **Build → Build Bundle(s) / APK(s) → Build APK(s)**. The generated debug APK is under `android/app/build/outputs/apk/debug/`.
