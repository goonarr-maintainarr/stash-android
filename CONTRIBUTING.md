# Contributing to Stash Android

Thank you for your interest in contributing to Stash Android! This document provides guidelines and instructions for contributing.

## Code of Conduct

By participating in this project, you agree to maintain a respectful and inclusive environment. Be kind, constructive, and professional in all interactions.

## Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/stash-android.git
   cd stash-android
   ```
3. **Open in Android Studio** (Ladybug / Jellyfish or newer recommended).
4. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

## Development Guidelines

### Code Style & Architecture

- **Kotlin Best Practices**: Follow official Kotlin coding style (`kotlin.code.style=official`).
- **Compose Principles**:
  - Keep composables stateless where possible and hoist state.
  - Follow unidirectional data flow (Events $\to$ ViewModel $\to$ StateFlow $\to$ Composable).
- **Clean Architecture**:
  - **Features**: Composables, UI State classes, and ViewModels.
  - **Repositories**: Expose Kotlin Flows / suspend functions and coordinate local/remote data.
  - **Core/Network**: Pure DTOs, network interceptors, and Room DAOs.
- **Dependency Injection**: Use Hilt for injecting viewmodels and repositories.

### Testing

- Write unit tests for ViewModels, Mappers, and Room DAOs.
- Run unit tests locally before submitting:
  ```bash
  ./gradlew test
  ```

## Pull Request Process

1. **Update documentation** if your change affects user-facing behavior or configurations.
2. **Add tests** for new logic.
3. **Ensure tests pass** and code compiles cleanly without lint warnings.
4. **Create a Pull Request** with:
   - Clear, descriptive title (using conventional commits, e.g. `feat:`, `fix:`, `refactor:`).
   - Summary of changes and screenshots/GIFs for UI changes.

---

Thank you for contributing! 🎉
