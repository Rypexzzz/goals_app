# Changelog

Все значимые изменения проекта документируются здесь. Формат основан на [Keep a Changelog](https://keepachangelog.com/ru/1.1.0/), версионирование — [Semantic Versioning](https://semver.org/lang/ru/).

## [Unreleased]

### Added — Sprint 1: Foundation

- Инициализация Gradle-проекта (Kotlin DSL, version catalog `libs.versions.toml`, AGP 8.7.3, Kotlin 2.1.10, JDK 17 toolchain).
- Compose-стек: BOM 2025.01.01, Material 3, Material Icons Extended, Navigation Compose 2.8.5 с typesafe-маршрутами.
- DI: Hilt 2.54 с KSP-процессором, модули `DataStoreModule`, `RepositoryModule`.
- DataStore Preferences для пользовательских настроек.
- Слой `domain`: модель `ThemeMode`, интерфейс `ThemeRepository`, use cases `ObserveThemeModeUseCase`, `SetThemeModeUseCase`.
- Слой `data`: `ThemePreferencesDataSource` поверх `DataStore<Preferences>`, реализация `ThemeRepositoryImpl`.
- Дизайн-система:
  - Палитра светлой и тёмной темы (Bold / Bento, тёплый коралл + кремовый фон) в `Color.kt`.
  - Типографика на шрифте Inter через Downloadable Fonts (Google Fonts provider).
  - Скругления `Shapes` 8 / 12 / 16 / 20 / 28 dp.
  - Корневой `AimTheme` с поддержкой переключения system / light / dark.
- Переиспользуемые компоненты: `AimCard`, `AimTopBar`, `AimEmptyState`, `AimFAB` (все с `@PreviewLightDark`).
- Bottom Navigation на 4 вкладки: Сегодня · Цели · Привычки · Дашборд.
- 4 экрана-заглушки с `AimEmptyState` (эмодзи + заголовок + подзаголовок).
- Экран «Настройки» с реальным переключателем темы и `SettingsViewModel` (MVVM + StateFlow).
- Edge-to-edge корневой `MainActivity`, единый `AimApp` host со скрытием bottom bar на маршруте `Settings`.
- Адаптивная иконка приложения с акцентным коралловым цветом.
- Backup / DataExtraction правила (заглушки на Sprint 2).
- Unit-тесты use cases (`ObserveThemeModeUseCaseTest`, `SetThemeModeUseCaseTest`) и `SettingsViewModelTest` (JUnit5 + MockK + Turbine).

### Notes

- `compileSdk` / `targetSdk` = 35, `minSdk` = 33 (Android 13).
- Локализация — только русский (`resourceConfigurations += listOf("ru")`).
- Все строки в `res/values/strings.xml`, все цвета — через `MaterialTheme.colorScheme.*`.
