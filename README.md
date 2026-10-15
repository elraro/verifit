<h1 align="center">
  <img width="96" src="metadata/logo/icon.svg" alt="Verifit logo"><br>
  Verifit
</h1>

<p align="center">
  <b>A simple, minimalist, open-source workout tracker for progressive overload.</b><br>
  No ads. No tracking. Works offline. Your data stays yours.
</p>

<p align="center">
  <a href="https://github.com/elraro/verifit/actions/workflows/android-build.yaml"><img alt="Build" src="https://github.com/elraro/verifit/actions/workflows/android-build.yaml/badge.svg"></a>
  <a href="LICENSE"><img alt="License: GPL v3" src="https://img.shields.io/badge/license-GPLv3-blue.svg"></a>
  <img alt="Min SDK 24" src="https://img.shields.io/badge/minSdk-24-brightgreen.svg">
  <img alt="Target SDK 36" src="https://img.shields.io/badge/targetSdk-36-brightgreen.svg">
</p>

## Why Verifit?

Verifit replaces the pencil-and-paper logbook. Its UI is inspired by FitNotes and focuses on doing one thing well: logging sets and showing whether you are getting stronger.

- **Open source and ad-free**, so the community can improve it.
- **Offline first**, with optional backups to your own WebDAV server.
- **Simple import/export** of your workout data as CSV, so moving between devices or apps is painless.

## Features

| | |
|---|---|
| **Tracking** | Log sets, reps and weight; create custom exercises; add exercise comments; edit or delete anything |
| **Stats** | Volume, actual 1RM, estimated 1RM, personal records, sets per week and more |
| **Per-exercise graphs** | Pick a metric (volume, weight, reps, estimated 1RM, sets, average weight) and a time range, and see best, average, latest and % change |
| **Charts** | Workouts per year, body-part and exercise distribution |
| **Dark mode** | Follows the system theme, or force light or dark in Settings |
| **Backups** | Import and export from phone storage or a WebDAV server, with automatic WebDAV backups |
| **Extras** | Rest timer and plate calculator |

## Screenshots

<p align="center">
  <img width="200" src="metadata/screenshots/Screenshot2.png">
  <img width="200" src="metadata/screenshots/Screenshot1.png">
  <img width="200" src="metadata/screenshots/Screenshot3.png">
  <img width="200" src="metadata/screenshots/Screenshot4.png">
  <img width="200" src="metadata/screenshots/Screenshot5.png">
  <img width="200" src="metadata/screenshots/Screenshot6.png">
  <img width="200" src="metadata/screenshots/Screenshot7.png">
  <img width="200" src="metadata/screenshots/Screenshot8.png">
</p>

## Install

Grab the latest APK from the [Releases](https://github.com/elraro/verifit/releases) page, or build it yourself (see below).

## Build from source

You need JDK 17 or newer (21 recommended) and the Android SDK (platform 36).

```bash
git clone https://github.com/elraro/verifit.git
cd verifit
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
./gradlew :verifit:assembleDebug
```

The APK is written to `verifit/build/outputs/apk/debug/`.

Pushing a tag such as `v1.0.15` makes the GitHub Actions workflow build the app and publish a release.

## Set up a WebDAV server

Any WebDAV server works. For a quick local one:

```bash
docker run -d -p 80:80 -v /data/webdav:/var/lib/dav \
  -e USERNAME=user -e PASSWORD=password bytemark/webdav
```

Then enter its URL and credentials under **Settings → Webdav Settings**.

## Built with

- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) for charts
- [Sardine](https://github.com/lookfirst/sardine) for WebDAV
- [Gson](https://github.com/google/gson) for JSON
- [Volley](https://github.com/google/volley) for networking

## Roadmap

- [x] Dark mode
- [x] More detailed stats (customized per-exercise graphs)
- [x] Free online accounts

## Contributing

Suggestions, bug reports and pull requests are welcome. Open an issue to discuss bigger changes first.

## Donate

If you enjoy Verifit, you can support its development with a [PayPal donation](https://paypal.me/elraro). Thank you!

## License

Released under the [GNU General Public License v3.0](LICENSE).
