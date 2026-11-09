<p align="center">
  <img src="docs/sonify-logo.svg" width="180" alt="Sonify logo">
</p>

<h1 align="center">Sonify</h1>

<p align="center">
  A modern Android music player built around Spotify.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Music-Spotify-1DB954?style=for-the-badge&logo=spotify&logoColor=white" alt="Spotify">
  <img src="https://img.shields.io/badge/License-GPL--3.0--or--later-blue?style=for-the-badge" alt="GPL-3.0-or-later">
</p>

<p align="center">
  <a href="#features"><b>Features</b></a> ·
  <a href="#getting-started"><b>Getting started</b></a> ·
  <a href="#building-from-source"><b>Building</b></a> ·
  <a href="#faq"><b>FAQ</b></a>
</p>

---

## About Sonify

Sonify is an Android music player project with a Spotify-focused listening experience. It brings music discovery, playback, your library, lyrics, audio controls and Android integration together in one app.

The project is under active development. Features and setup instructions may change as Sonify evolves.

## Features

### Spotify music

- Connect your Spotify account and access your music.
- Browse and play your playlists, albums, saved tracks and artists.
- Search for music and explore recommendations.
- Continue listening across supported Spotify Connect devices.
- Spotify Premium is required for full music playback.

### Music player

- A full now-playing screen with album artwork and playback controls.
- Queue management and continuous listening.
- Playback speed and pitch controls, plus audio effects.
- Sleep timer and crossfade options where supported by the current build.

### Lyrics

- Synchronized lyrics when available.
- Word-by-word or line-by-line timing, depending on the lyrics source.
- Optional translated lyrics where available.

### Your library

- Browse playlists, albums, artists and liked tracks.
- Organize and manage playlists.
- Discover music and revisit recent searches.

### Offline and downloads

- Download support for content and formats permitted by the app's current implementation.
- Manage downloaded items and listen to supported local files.
- Availability depends on the configured music source and the current build.

### Android integration

- Media controls in the notification shade and on the lock screen.
- Android widgets for quick access to playback.
- Android Auto support where available.
- Sonify-branded playback and download notifications.

### Appearance

- A polished interface with customizable visual effects.
- Language and appearance options supported by the current build.

## Getting started

1. Install a Sonify APK from the project's releases when a release is available.
2. Open Sonify and follow the in-app setup.
3. Connect your Spotify account when prompted.
4. For Spotify developer setup, follow the instructions shown in the app and provide your own Spotify Client ID if required.
5. Start listening.

**Spotify requirements:** Spotify account access and Premium may be required for playback. Spotify developer credentials and available features are subject to Spotify's platform rules and the app's current implementation.

## Download

Official releases will be published on the [Sonify GitHub Releases](https://github.com/nkosanamahungela-dev/Sonify/releases) page.

If no release is available yet, Sonify is still in development. You can build the project from source using the instructions below.

## FAQ

**Do I need Spotify Premium?**

Full Spotify playback through third-party clients generally requires Spotify Premium. The exact behavior depends on Spotify's current platform requirements.

**Do I need a Spotify Developer Client ID?**

Some Spotify integration features may require you to create an app in the Spotify Developer Dashboard and enter its Client ID in Sonify. Follow the in-app setup instructions.

**Is Sonify affiliated with Spotify?**

No. Sonify is an independent project and is not affiliated with, endorsed by, or sponsored by Spotify.

**Where can I report a problem?**

Please use the repository's [Issues](https://github.com/nkosanamahungela-dev/Sonify/issues) page and include your Android version, Sonify version and steps to reproduce the issue.

## Building from source

Sonify is an Android project. Open the repository in Android Studio or use the Gradle wrapper:

```bash
./gradlew assembleDebug
```

For a release build, configure your signing credentials locally and use the appropriate Gradle release task. Never commit signing keys, passwords or private API credentials.

## Disclaimer

Sonify is an independent, community-developed project. Spotify is a trademark of Spotify AB. Sonify is not affiliated with, endorsed by, sponsored by, or officially connected to Spotify.

Use the app in accordance with Spotify's terms, developer policies and applicable laws. Availability of Spotify features may change if Spotify changes its APIs, authentication requirements or platform policies.

## Credits and licences

Sonify builds on open-source software and upstream work. See the repository's [LICENSE](LICENSE) file and the relevant source directories for licence notices and third-party attributions. Preserve those notices when redistributing or modifying the project.

## Contributing

Contributions, bug reports and suggestions are welcome. Please open an issue before starting a large change so the approach can be discussed.

---

<p align="center">
  Made with care. Made for music. <b>Sonify.</b>
</p>
