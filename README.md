# Ollama Mobile

Android GUI for [Ollama](https://ollama.com) when it’s running on the same device (e.g. in **Termux** or on a **Google Pixel** terminal). Works like the desktop Ollama app or ChatGPT: chat UI, model picker, and streaming replies.

## Prerequisites

- **Ollama** running on the device (e.g. in Termux):
  1. Install [Termux](https://termux.dev/) (from F-Droid recommended).
  2. In Termux: `pkg update && pkg install git golang`
  3. Build/install Ollama (see [Run Ollama on Android](https://techlory.com/blog/2025/02/03/run-ollama-llms-on-android/) or [ollama-in-termux](https://github.com/Anon4You/ollama-in-termux)).
  4. Start the server: `ollama serve` (default: `http://127.0.0.1:11434`).

- **Android**: min SDK 26, target 34.

## Build and run

**Note:** All `./gradlew` commands must be run from the project's root directory.

1. Open the project in **Android Studio** (or use the Gradle wrapper from the project root).
2. Sync Gradle, then run on a device or emulator.

```bash
./gradlew assembleDebug
# Install: adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Testing

**Note:** All `./gradlew` commands must be run from the project's root directory.

Run unit tests:
```bash
./gradlew :app:testDebugUnitTest
```

Run Android tests:
```bash
./gradlew :app:connectedDebugAndroidTest
```

## Release

**Note:** All `./gradlew` commands must be run from the project's root directory.

To create a release-ready Android App Bundle, run the following command from the project's root directory:

```bash
./gradlew bundleRelease
```

The generated app bundle will be located at `app/build/outputs/bundle/release/app-release.aab`.
You can then upload this file to the Google Play Console.
Make sure you have configured the signing information in `keystore.properties`.

## Usage

1. **First launch**: The app uses `http://127.0.0.1:11434` by default (Ollama in Termux on the same phone).
2. **Settings** (gear): Change **Ollama server URL** if your server is elsewhere (e.g. another host/port), then **Save**.
3. **Refresh** (↻): Reload the list of models from Ollama.
4. **Model**: Tap **Change** to pick the model for the chat.
5. **New chat** (+): Clear the conversation and start over.
6. Type a message and send; replies stream in as they’re generated.

## Notes

- **Cleartext**: The app allows HTTP to `localhost` / `127.0.0.1` so it can talk to a local Ollama (default port 11434).
- **Small models**: On phones, prefer smaller models (e.g. 1B–3B) for better speed and battery.

## License

MIT.
