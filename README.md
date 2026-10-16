# PaperHound App

**PaperHound** is an Android app for tracking the books you read or listen. Search for a book by title, add it to your shelf, and keep track of what you want to read, what you're reading now, and what you've finished.

> 🚧 Work in progress. The app is in early development.

## Features

### MVP (in progress)
- 🔍 **Book search** by title via Google Books API (with Open Library as a fallback)
- 🖼️ **Automatic covers**: fetched from online sources
- 📖 **Reading statuses**: *Want to read* / *Reading* / *Finished*
- 💾 **Local storage**: your library is saved on the device

### Planned
- ✨ Cover processing: automatically adjust fetched covers to a consistent app style
- 🤖 Book recommendations based on your reading history

## Tech stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture:** MVVM
- **Local database:** Room
- **Networking:** Retrofit
- **Book data:** [Google Books API](https://developers.google.com/books), [Open Library API](https://openlibrary.org/developers/api)

## License

TBD
