# Changelog

Все значимые изменения проекта документируются здесь. Формат основан на [Keep a Changelog](https://keepachangelog.com/ru/1.1.0/), версионирование — [Semantic Versioning](https://semver.org/lang/ru/).

## [Unreleased]

### Added — Sprint 2: Цели и задачи (CRUD)

- **Room 2.6.1** — `AimDatabase` (v1) с таблицами `goals` и `tasks` (self-FK), индексы по deleted_at / status / order_index, FK с ON DELETE CASCADE. Type-конвертер для `Recurrence` через kotlinx.serialization JSON. Schema-экспорт в `app/schemas/`.
- **Domain слой:** модели `Goal`, `Task`, `Recurrence` (sealed: Daily/WeeklyOn/Weekly/EveryNDays/Monthly), `TaskStatus`, `GoalStatus`, `GoalFilter`, `TaskNode`, `TrashItem` (sealed: GoalItem/TaskItem).
- **Repository слой:** `GoalRepositoryImpl`, `TaskRepositoryImpl`. В `TaskRepositoryImpl.createTask` — проверка `depth ≤ MAX_DEPTH` через родителя. В `moveTask` — детекция циклов (через рекурсивный CTE `getSubtreeIds`) и проверка глубины поддерева. Все мутации задач — в Room-транзакциях.
- **20+ UseCase'ов** (по одному классу на операцию): для целей CRUD + Archive/Complete/Restore/Reorder; для задач — то же плюс Move (смена родителя), ReorderInParent. Trash: `ObserveTrashUseCase` (combine goals+tasks), `EmptyTrashUseCase`.
- **Дизайн-система пополнилась:**
  - `AimChip` — Material 3 FilterChip с фирменными цветами.
  - `AimEmojiPicker` — bottom sheet с курированным набором ~120 эмодзи по 8 категориям (поиск отложен).
  - `AimDatePickerDialog` — обёртка над M3 DatePicker с локализацией.
  - `AimMarkdownText` / `AimMarkdownEditor` — рендер и редактирование на `compose-richtext` 0.20.0 с переключателем «Редактировать / Превью».
  - `AimAlertDialog` — единый диалог подтверждений с destructive-вариантом.
  - `AimTaskTree` — рекурсивное дерево задач: пружинная анимация чекбокса, разворачивание/сворачивание, drag-n-drop **внутри уровня** через `sh.calvin.reorderable` 2.4.0.
- **Экраны:**
  - `GoalsScreen` — bento-сетка 2 колонки, фильтры (Все / Активные / Завершённые / Архивные) на чипах, FAB «Новая цель», меню с переходом в Архив/Корзину.
  - `GoalDetailScreen` — карточка-шапка цели с emoji/дедлайном/markdown-описанием, дерево задач, FAB «Добавить задачу», меню (редактировать/завершить/архивировать/удалить). Диалог «Также отметить N задач?» при завершении цели с незавершёнными задачами.
  - `TaskDetailScreen` — кликабельные хлебные крошки `Цель → ... → задача`, чекбокс выполнения, markdown-описание, дерево подзадач, FAB.
  - `GoalEditBottomSheet` / `TaskEditBottomSheet` — формы создания/редактирования через Hilt assisted injection (`@HiltViewModel(assistedFactory = ...)`). Emoji picker, дата-пикеры, выбор Recurrence чипами.
  - `TrashScreen` — единый список удалённых целей и задач, действия «Восстановить» / «Удалить навсегда» / «Очистить всё».
  - `ArchiveScreen` — список архивных целей с «Вернуть из архива» / «Удалить».
  - `SettingsScreen` дополнен секцией «Данные» со ссылками на Архив и Корзину.
- **Навигация:** добавлены типизированные маршруты `GoalDetail(goalId)`, `TaskDetail(taskId)`, `Trash`, `Archive`. Slide-анимации для push-destinations.
- **Тесты:** mappers (Goal/Task с Recurrence/scheduledTime), use cases (CreateGoal, CreateTask, ObserveTasksForGoal — построение дерева, ObserveTrash — combine).

### Notes

- **Soft delete без каскада на data-слое** — каждая сущность ведёт свой `deletedAt` независимо. Активные запросы для задач JOIN'ятся с `goals` (чтобы не светить задачи под удалённой целью). Восстановление цели автоматически возвращает её задачи (их `deletedAt` остаётся NULL). См. ADR-0008.
- **Drag-n-drop ограничен братьями одного уровня** — перетаскивание между разными `parentTaskId` отменяется в `onMove` callback библиотеки. Смена родителя — только через будущий «Move to...» action (вне Sprint 2).
- **Recurrence хранится JSON-строкой** в колонке `tasks.recurrence`. Материализация occurrences (Sprint 4) добавит отдельную таблицу.

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
