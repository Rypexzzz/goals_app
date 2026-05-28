# Changelog

Все значимые изменения проекта документируются здесь. Формат основан на [Keep a Changelog](https://keepachangelog.com/ru/1.1.0/), версионирование — [Semantic Versioning](https://semver.org/lang/ru/).

## [Unreleased]

### Fixed


- **Виджет «Сегодня»: не работала отметка задач.** Чекбокс был символом `✓`/`○` с вложенным `clickable` внутри кликабельной строки — Glance отдавал тап родителю (открытие приложения), а не переключателю. Заменён на настоящий Glance `CheckBox` (своё действие-переключатель), тап по тексту по-прежнему открывает приложение; выполненные зачёркиваются.
- **Виджет не отражал отметки, сделанные в приложении.** Добавлен `TodayWidgetSync` — пока приложение на переднем плане, наблюдает за списком «Сегодня» и вызывает `updateAll` при изменениях (отметка/редактирование/удаление). В фоне обновляет `WidgetUpdateWorker` (30 мин).
- **Виджет занимал много пустого места при малом числе задач.** Снижены `minWidth`/`minHeight` (180×100 dp) и стартовый размер (4×3) — теперь виджет можно сжать; строки сделаны просторнее.
- **Нельзя было создать вторую цель/задачу/привычку без перезапуска.** Лист редактирования переиспользует keyed-ViewModel, а флаг `saved=true` после первого сохранения оставался в state — при повторном открытии `LaunchedEffect(saved)` мгновенно закрывал лист. Добавлен сброс формы (`onReset()` через `DisposableEffect.onDispose`): для Create — очистка, для Edit — перечитывание из БД. Заодно исчезли «залипшие» старые данные в форме при повторном открытии.
- **Список целей отображался плиткой из квадратов разной высоты.** Заменён `LazyVerticalGrid` (2 колонки) на одноколоночный `LazyColumn`; `GoalCard` переработана в компактную строку одинакового вида (эмодзи + название + дедлайн/статус).

### Added — Sprint 8: Полировка и доп. фичи

- **Onboarding** — 3 экрана при первом запуске (концепция / разрешения / первая цель) на `HorizontalPager` с индикатором страниц и запросом POST_NOTIFICATIONS. Флаг завершения в DataStore (`AppPreferencesRepository`); `RootViewModel` гейтит онбординг vs основное приложение.
- **Экспорт/импорт JSON** (README §6.8) — `BackupRepository` сериализует все таблицы (goals/tasks/occurrences/habits/checkIns) в версионированный `BackupEnvelope`; импорт заменяет данные в одной транзакции (очистка + вставка в FK-порядке). UI в Настройках через SAF (`CreateDocument`/`OpenDocument`) с диалогом подтверждения замены.
- **Настройка «первый день недели»** (Пн/Вс) в DataStore + чипы в Настройках.
- **Haptic feedback** на отметку выполнения в «Сегодня».
- **Тесты:** `BackupSerializationTest` (roundtrip всех сущностей + игнор будущих полей).

### Deferred (осознанно, ADR-0024)

Не вошло в Sprint 8 — требует устройства для отладки или отдельного модуля:
- Shared element transitions (карточка цели ↔ детальный) — экспериментальный `SharedTransitionLayout`.
- Swipe-жесты и drag-n-drop на «Сегодня» (ADR-0018), полный DnD между уровнями дерева.
- Скелетоны/stagger/pulse на всех экранах, размер шрифта в настройках.
- `WidgetConfigurationActivity` (ADR-0023).
- Macrobenchmark + Baseline Profile (нужен отдельный gradle-модуль и реальное устройство).
- Распространение «первого дня недели» во все расчёты дат (сейчас календарь/стрики используют параметр со значением Пн по умолчанию).

### Added — Sprint 7: Виджет

- **Jetpack Glance 1.1.1** — большой виджет «Сегодня» (`TodayWidget : GlanceAppWidget`): дата, сводка (X/Y), список задач/привычек с чекбоксами (до 10 строк).
- **`WidgetEntryPoint`** — Hilt EntryPoint для доступа к use cases из не-Hilt-компонентов (Glance widget / ActionCallback).
- **`ToggleWidgetItemAction`** (`ActionCallback`) — отметка выполнения прямо из виджета без открытия приложения, через `ToggleTodayItemUseCase`; виджет обновляется сразу.
- **`ToggleTodayItemUseCase`** — переключение по `stableKey` (task / task-occ / habit), переиспользуется виджетом.
- **`WidgetUpdateWorker`** — периодическое обновление каждые 30 минут (`updateAll`).
- **`TodayWidgetReceiver`** + `xml/today_widget_info.xml` (4×4 cells, resizable) + manifest receiver.
- Тап по строке открывает приложение (`actionStartActivity<MainActivity>`).

### Notes

- **`WidgetConfigurationActivity` (тема/прозрачность/число строк) отложена в Sprint 8** — виджет работает с дефолтами (до 10 строк, тема системная через GlanceTheme). ADR-0023.
- Glance-виджет читает данные снапшотом в `provideGlance` через EntryPoint; реактивное обновление — через 30-минутный worker и немедленно после действия в виджете.

### Added — Sprint 6: Дашборд

- **5 use cases:** `GetDashboardSummaryUseCase`, `GetActiveStreaksUseCase`, `GetHabitHeatmapsUseCase` (13 недель), `GetGoalProgressUseCase` (% задач 1-го уровня, сортировка по дедлайну), `GetPeriodStatsUseCase` (неделя/месяц/год).
- **Компоненты:** `AimHeatmap` (Canvas-сетка недель × дней с tap-детекцией), `AimSegmentedControl`.
- **DAO-проекции:** `observeFirstLevelCounts` (счётчики задач 1-го уровня по целям), `observeCompletedBetween` (выполненные задачи в диапазоне).
- **Экран `DashboardScreen`** — 5 секций (README §6.5): сводка с кольцом, карусель стриков, тепловые карты привычек, прогресс целей, статистика периода с картой продуктивности. Тапы → детальные экраны.
- **Тест:** `GetGoalProgressUseCaseTest`.

### Notes

- Без геймификации/прогнозов — только факты (README §6.5).
- Карта продуктивности агрегирует выполненные разовые задачи + завершённые экземпляры регулярных + DONE-отметки привычек по дням.

### Added — Sprint 5: Уведомления

- **WorkManager 2.10 + Hilt-Work** — `AimApplication` реализует `Configuration.Provider`, дефолтный WorkManager-инициализатор отключён в манифесте.
- **Настройки уведомлений** — `NotificationSettings` (master-тоггл, пер-тип enabled/время/звук/вибрация, окно «не беспокоить» с корректной обработкой перехода через полночь). Персист в DataStore одной JSON-строкой (`NotificationPreferencesDataSource`), репозиторий + use cases.
- **Каналы** — `AimChannel` (DAILY / TASKS / HABITS / SUMMARY), `NotificationChannelInitializer` создаёт их при старте.
- **`AimNotifier`** — обёртка над `NotificationManagerCompat` с проверкой POST_NOTIFICATIONS, BigText-стилем, действием «Выполнено» через PendingIntent.
- **`AlarmScheduler`** — обёртка над AlarmManager (`setExactAndAllowWhileIdle`) с graceful degradation, если нет права на точные алармы.
- **`NotificationScheduler`** — оркестратор: дайджесты (утренний/вечерний/недельный) — периодические Worker'ы; точные ко времени (первое дело, предупреждение стрика) — алармы; ежедневное обслуживание — `DailyMaintenanceWorker`.
- **Workers:** `MorningBriefWorker`, `EveningCheckinWorker`, `WeeklySummaryWorker`, `DailyMaintenanceWorker` (чистка корзины + дедлайны). Все `@HiltWorker`.
- **`NotificationContentProvider`** — строит контент из доменных данных (через `GetTodayItemsUseCase`/репозитории), уважает master/DND.
- **Receivers:** `NotificationActionReceiver` (отметка «Выполнено» из шторки без открытия приложения), `BootReceiver` (пересоздание расписаний), `AlarmReceiver` (пост + перепланирование следующего срабатывания).
- **`PurgeOldTrashUseCase`** — авто-чистка корзины старше 30 дней (вызывается из DailyMaintenanceWorker).
- **Экран `NotificationSettingsScreen`** — master-тоггл, карточка на каждый из 6 типов (тоггл + время через `AimTimePickerDialog`), окно «не беспокоить». Запрос POST_NOTIFICATIONS при включении (Android 13+).
- **Тесты:** `NotificationSettingsTest` (DND внутри дня / через полночь / отсутствие окна), `PurgeOldTrashUseCaseTest` (порог = now − retentionDays, суммирование).

### Notes

- Точные алармы (Android 12+): при отсутствии `SCHEDULE_EXACT_ALARM` `AlarmScheduler` падает на неточный `setAndAllowWhileIdle` — уведомления приходят, но без секундной точности. ADR-0020.
- Распределение Worker vs Alarm: дайджесты не требуют секундной точности → WorkManager (переживает reboot сам); точные ко времени → AlarmManager + BootReceiver. ADR-0021.

### Added — Sprint 4: Чек-лист «Сегодня»

- **Database v3** — миграция v2 → v3: таблица `task_occurrences(task_id, date, status, completed_at)` для материализованных экземпляров регулярных задач (UNIQUE по task_id+date, FK CASCADE).
- **Чистые матчеры (тестируемые без Android):**
  - `RecurrenceMatcher.occursOn(recurrence, date, anchor)` — все варианты `Recurrence` (Daily / WeeklyOn / Weekly / EveryNDays / Monthly с клампом к последнему дню короткого месяца).
  - `HabitScheduler.isDueOn(frequency, date, checkIns)` — актуальность привычки на день (README §6.4).
- **`GetTodayItemsUseCase`** — собирает разовые задачи (`scheduledFor == today`), экземпляры регулярных задач, привычки на сегодня и просроченные задачи в `TodaySnapshot`. Сортировка задачи → привычки, внутри по времени/orderIndex; разделение todo / «выполнено сегодня».
- **Lazy-материализация occurrences** (`SetTaskOccurrenceUseCase`) и **`RescheduleTaskUseCase`** (snooze на завтра / +3 дня / дата / снять).
- **`AimProgressRing`** — круговой Canvas-индикатор с анимацией заполнения.
- **Экран `TodayScreen`:** шапка с датой и сводкой (кольцо + «X из Y»), сворачиваемый баннер просроченных, секции «Нужно сделать» / «Выполнено сегодня», пружинная анимация чекбокса, меню элемента (snooze/удалить для задач, «Сорвался» для привычек).
- **Тесты:** `RecurrenceMatcherTest` (7), `HabitSchedulerTest` (6), `GetTodayItemsUseCaseTest` (5).

### Notes

- **Drag-n-drop и swipe на «Сегодня» отложены в Sprint 8.** Действия — через явное меню «⋮» (надёжно, без жестов). ADR-0018.
- Регулярная задача не дублируется: `observeScheduledFor` исключает `recurrence IS NOT NULL`.

### Added — Sprint 3: Привычки

- **Database v2** — миграция v1 → v2: новые таблицы `habits` и `habit_check_ins` с индексами (date, habit_id+date unique), FK на goals с `ON DELETE SET NULL` и на habits с `ON DELETE CASCADE`. Schema-снапшот в `app/schemas/`.
- **Domain слой:** модели `Habit`, `HabitCheckIn`, `HabitStats`, `CheckInStatus`, `HabitFrequency` (sealed: Daily / TimesPerWeek(n) / TimesPerMonth(n) / SpecificDays(days)). Кастомный `DayOfWeekSerializer` для kotlinx.serialization.
- **Repository:** `HabitRepositoryImpl` — CRUD привычек + check-ins upsert/delete + наблюдение по диапазону дат для календаря.
- **15 UseCase'ов** (CRUD + check-in/uncheck + ObserveCheckIns + ObserveHabitsForGoal + Archive/Restore/Delete) + **`CalculateStreakUseCase`** — чистая функция для **четырёх типов** `HabitFrequency`, с current и best streak метриками.
- **`GetHabitStatsUseCase`** — комбинирует streak + total counts + % выполнения относительно дедлайна привязанной цели.
- **UI-компонент `AimHabitCalendar`** — месячный календарь с HorizontalPager между месяцами, кружки с цветами по статусу (DONE/FAILED/none), обводка сегодняшнего дня с пружинной анимацией.
- **Экраны:**
  - `HabitsScreen` — список с эмодзи, частотой и значком стрика 🔥, FAB «Новая привычка».
  - `HabitDetailScreen` — большая шапка со статистикой (текущий/лучший стрик, % выполнения), квик-кнопки «Воздержался» / «Сорвался», календарь с тапом по дню для смены отметки (циклически: нет → DONE → FAILED → нет), меню (редактировать / архив / удалить).
  - `HabitEditBottomSheet` — Hilt assisted injection (`@HiltViewModel(assistedFactory = ...)`), выбор `HabitFrequency` чипами с под-конфигурацией: для TimesPer{Week,Month} — `+/−` контрол, для SpecificDays — переключение дней недели через локализованные пилюли.
- **Связь с целью:** в `GoalDetailScreen` под деревом задач появилась секция «Привычки цели» — горизонтальный список привычек с тапом для перехода и кнопкой «Привязать привычку».
- **Навигация:** `AimRoute.HabitDetail(habitId)`, slide-анимация push-destination.
- **Тесты:** `CalculateStreakUseCaseTest` — 14 кейсов с покрытием всех 4 видов частоты + edge cases (FAILED ломает streak, пропуски не ломают Daily, неделя успешна без FAILED даже при done < n). `HabitMapperTest` — roundtrip для каждого варианта HabitFrequency + fallback при битом JSON.

### Notes

- Все привычки **негативные** (воздержание) — UX-тексты: «Воздержался» / «Сорвался», README §6.3.
- Стрик-логика по README §6.3 точно: missing-день не ломает Daily, неделя с `failed ≥ 1 AND done < n` ломает TimesPerWeek, SpecificDays требует все заданные дни как DONE.
- Тап по дню в календаре циклически переключает: нет → DONE → FAILED → нет.
- При архивировании цели её привязанные привычки остаются (FK `ON DELETE SET NULL` не срабатывает при soft delete) — связь сохраняется через `habits.goal_id`.

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
