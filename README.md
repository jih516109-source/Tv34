# Smart TV Player (Android + Android TV)

A starter Android Studio project for playing user-supplied HLS/M3U8 streams.
The installed app name is **Smart TV Player**.

## Features
- Android phone and Android TV launcher support
- ExoPlayer / Media3 HLS playback
- Add, edit, delete and save multiple stream URLs
- Favorites
- TV remote / D-pad focusable controls
- Keeps the screen awake during playback

## Build an APK in GitHub (no local Android Studio required)
1. Create a GitHub repository and upload the contents of this project to its `main` branch.
2. Open the repository's **Actions** tab.
3. Select **Build Smart TV Player APK** and press **Run workflow** (or push a commit to `main`).
4. When the workflow completes, open its run and download the `Smart-TV-Player-debug-apk` artifact.
5. Extract the downloaded artifact; it contains `app-debug.apk`, which can be installed on a compatible Android device.

The workflow builds a **debug APK** for testing. A Play Store release needs a separately signed release build.

## Build locally
1. Install Android Studio (current stable) and Android SDK Platform 35.
2. Open this folder in Android Studio.
3. Allow Gradle sync and dependency downloads.
4. Choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

## Add a channel
Press **+ Add channel**, enter a display name and a full `https://...m3u8` or `http://...m3u8` URL, then Save. The starter list includes a sample stream; replace it with a stream you own or are authorized to watch. If a stream requires a token, special headers, geo-access or an active subscription, playback may fail until the provider supplies a valid accessible URL.

## Notes
- Only add streams you own or are authorized to watch.
- This project is source code, not a prebuilt APK. The GitHub Actions workflow builds the APK when run in your repository.
- Some Android TV devices have limited RAM or codecs; playback depends on the stream's format and device support.
