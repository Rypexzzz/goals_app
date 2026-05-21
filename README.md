# 🎯 Aim — Персональный трекер целей и привычек

> **Aim** — нативное Android-приложение для трекинга жизненных целей, задач и привычек. Один пользователь, одно устройство, локальное хранилище, без облака. Фокус на премиальном UX, продуманных микро-анимациях и эстетике "bold/bento" дизайна.

> ⚠️ **Альтернативные имена на выбор:** Aim · Compass · Zenith · Pursuit · NorthStar. Финальное имя владельца проекта — заменить по проекту глобально (`grep -ril "Aim" .`).

---

## 📋 Оглавление

1. [Концепция и принципы](#1-концепция-и-принципы)
2. [Технологический стек](#2-технологический-стек)
3. [Архитектура](#3-архитектура)
4. [Структура проекта](#4-структура-проекта)
5. [Модели данных](#5-модели-данных)
6. [Функциональные требования](#6-функциональные-требования)
7. [Дизайн-система](#7-дизайн-система)
8. [Уведомления](#8-уведомления)
9. [Виджет](#9-виджет)
10. [Навигация](#10-навигация)
11. [План разработки по спринтам](#11-план-разработки-по-спринтам)
12. [Инструкции для Claude Code](#12-инструкции-для-claude-code)
13. [Acceptance Criteria](#13-acceptance-criteria)

---

## 1. Концепция и принципы

### Ключевая идея
Приложение помогает пользователю достигать жизненных целей через декомпозицию (большая цель → задачи → подзадачи) и параллельно работать над выработкой привычек (через ежедневные отметки и стрики). Все цели/задачи на сегодня собираются в один чек-лист, который можно вывести виджетом на главный экран.

### Принципы продукта
- **Локально и приватно** — никакого облака, аккаунтов, аналитики, телеметрии. База данных на устройстве.
- **Премиальный UX** — приложение должно вызывать желание им пользоваться. Качество анимаций, типографики и сетки — критично.
- **Без геймификации** — ни очков, ни уровней, ни бейджей. Мотивация через визуализацию прогресса (стрики, тепловые карты).
- **Гибкая иерархия** — глубокая вложенность задач для крупных целей, но без лимита по числу задач.
- **Полная настраиваемость уведомлений** — пользователь решает, когда и какие напоминания получать.

### Целевая аудитория
Один пользователь (владелец). Локализация на старте — только русский, но i18n-инфраструктура (`strings.xml`, ресурс-стратегия) закладывается с первого спринта.

---

## 2. Технологический стек

| Слой | Технология | Версия | Назначение |
|------|-----------|--------|-----------|
| Язык | Kotlin | 2.0+ | Основной язык |
| UI | Jetpack Compose | BOM 2025.+ | Декларативный UI |
| Дизайн | Material 3 (Material You) | latest | Базовая дизайн-система |
| Архитектура | MVVM + Clean Architecture | — | Слои `data` / `domain` / `presentation` |
| DI | Hilt | latest | Внедрение зависимостей |
| БД | Room | 2.6+ | Локальное хранилище |
| Settings | DataStore (Preferences) | latest | Пользовательские настройки |
| Async | Coroutines + Flow | latest | Реактивность |
| Навигация | Navigation Compose | latest | Типизированная навигация |
| Расписания | WorkManager | latest | Регулярные напоминания |
| Точные алармы | AlarmManager (`setExactAndAllowWhileIdle`) | — | Уведомления в точное время |
| Виджеты | Jetpack Glance | latest | Виджеты на Compose-стиле |
| Изображения | Coil 3 | latest | Опционально |
| Логи | Timber | latest | Логирование |
| Тесты | JUnit5, Turbine, MockK, Compose UI Test | — | Покрытие критичных модулей |

### Минимальные требования
- `minSdk = 33` (Android 13)
- `targetSdk = 35` (Android 15)
- `compileSdk = 35`
- Gradle Kotlin DSL (`build.gradle.kts`)
- Version Catalog (`libs.versions.toml`)

### Permissions
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.VIBRATE" />
```

---

## 3. Архитектура

### Слои (Clean Architecture)

```
┌─────────────────────────────────────────┐
│         presentation (UI слой)          │
│  Compose screens, ViewModels, States    │
└────────────────┬────────────────────────┘
                 │ uses
┌────────────────▼────────────────────────┐
│            domain (бизнес-логика)        │
│   UseCases, доменные модели, Repository │
│              интерфейсы                  │
└────────────────┬────────────────────────┘
                 │ implements
┌────────────────▼────────────────────────┐
│              data (хранилище)            │
│  Room DAO, Repository impl, DataStore,  │
│         маппинг entity↔domain           │
└─────────────────────────────────────────┘
```

### Паттерны
- **MVVM** на уровне UI: `Screen` ← `ViewModel` (через `StateFlow<UiState>`)
- **Repository** — единственная точка доступа к данным для domain-слоя
- **UseCase** — одна доменная операция = один класс с `operator fun invoke(...)`
- **UDF (Unidirectional Data Flow)** — состояние течёт из ViewModel в UI, события — из UI в ViewModel

### Каждый экран
- `XxxScreen.kt` — composable
- `XxxViewModel.kt` — состояние и события
- `XxxUiState.kt` — sealed/data class состояния
- `XxxEvent.kt` — sealed events (опционально)

---

## 4. Структура проекта

```
app/
├── build.gradle.kts
└── src/main/
    ├── AndroidManifest.xml
    ├── java/com/aim/app/
    │   ├── AimApplication.kt                  // @HiltAndroidApp
    │   ├── MainActivity.kt                     // single-activity
    │   ├── core/
    │   │   ├── di/                             // Hilt modules
    │   │   ├── util/                           // extensions, helpers
    │   │   ├── notification/                   // notification manager
    │   │   ├── alarm/                          // alarm scheduler
    │   │   └── work/                           // WorkManager workers
    │   ├── data/
    │   │   ├── local/
    │   │   │   ├── db/                         // AimDatabase, DAO
    │   │   │   ├── entity/                     // Room entities
    │   │   │   └── preferences/                // DataStore
    │   │   ├── mapper/                         // entity ↔ domain
    │   │   └── repository/                     // impl
    │   ├── domain/
    │   │   ├── model/                          // pure Kotlin models
    │   │   ├── repository/                     // interfaces
    │   │   └── usecase/
    │   │       ├── goal/
    │   │       ├── task/
    │   │       ├── habit/
    │   │       └── notification/
    │   ├── presentation/
    │   │   ├── theme/                          // Color, Type, Theme.kt
    │   │   ├── components/                     // переиспользуемые
    │   │   ├── navigation/                     // NavHost, маршруты
    │   │   └── screens/
    │   │       ├── today/
    │   │       ├── goals/
    │   │       ├── goaldetail/
    │   │       ├── taskdetail/
    │   │       ├── habits/
    │   │       ├── habitdetail/
    │   │       ├── dashboard/
    │   │       ├── settings/
    │   │       ├── archive/
    │   │       └── trash/
    │   └── widget/
    │       ├── TodayWidget.kt                  // GlanceAppWidget
    │       └── TodayWidgetReceiver.kt
    └── res/
        ├── drawable/                           // иконки, иллюстрации
        ├── values/
        │   ├── strings.xml                     // ru
        │   ├── colors.xml                      // base palette
        │   └── themes.xml                      // bare minimum
        └── xml/
            └── today_widget_info.xml
```

---

## 5. Модели данных

### Доменные модели (Kotlin)

```kotlin
// domain/model/Goal.kt
data class Goal(
    val id: Long,
    val title: String,
    val description: String?,           // markdown
    val emoji: String?,                 // напр. "🎯"
    val deadline: LocalDate?,
    val status: GoalStatus,
    val createdAt: Instant,
    val completedAt: Instant?,
    val archivedAt: Instant?,
    val deletedAt: Instant?             // soft delete
)

enum class GoalStatus { IN_PROGRESS, COMPLETED }

// domain/model/Task.kt
data class Task(
    val id: Long,
    val goalId: Long,                   // корневая цель
    val parentTaskId: Long?,            // null = задача первого уровня внутри цели
    val title: String,
    val description: String?,
    val emoji: String?,
    val deadline: LocalDate?,
    val scheduledFor: LocalDate?,       // когда показывать в "Сегодня"
    val scheduledTime: LocalTime?,      // опц. время на этот день
    val status: TaskStatus,
    val depth: Int,                     // 0..4 — для UI ограничения
    val orderIndex: Int,                // ручная сортировка внутри родителя
    val recurrence: Recurrence?,        // null = разовая, иначе регулярная
    val createdAt: Instant,
    val completedAt: Instant?,
    val deletedAt: Instant?
)

enum class TaskStatus { IN_PROGRESS, COMPLETED }

// domain/model/Recurrence.kt
sealed class Recurrence {
    object Daily : Recurrence()
    data class WeeklyOn(val days: Set<DayOfWeek>) : Recurrence()
    data class EveryNDays(val n: Int) : Recurrence()
    object Weekly : Recurrence()       // раз в неделю, любой день
    object Monthly : Recurrence()
}

// domain/model/Habit.kt
data class Habit(
    val id: Long,
    val goalId: Long?,                  // опц. привязка к цели
    val title: String,
    val description: String?,
    val emoji: String?,
    val frequency: HabitFrequency,
    val createdAt: Instant,
    val archivedAt: Instant?,
    val deletedAt: Instant?
)

sealed class HabitFrequency {
    object Daily : HabitFrequency()
    data class TimesPerWeek(val times: Int) : HabitFrequency()     // напр. 4/нед
    data class TimesPerMonth(val times: Int) : HabitFrequency()
    data class SpecificDays(val days: Set<DayOfWeek>) : HabitFrequency()
}

// domain/model/HabitCheckIn.kt
data class HabitCheckIn(
    val id: Long,
    val habitId: Long,
    val date: LocalDate,
    val status: CheckInStatus,
    val checkedAt: Instant              // когда фактически отмечено (может быть задним числом)
)

enum class CheckInStatus { 
    DONE,           // выполнил (для негативных — "воздержался")
    FAILED          // не выполнил (для негативных — "сорвался"; обнуляет стрик)
    // отсутствие записи = не отмечено (не обнуляет стрик)
}
```

### Room Entities

Все доменные модели имеют соответствующие `@Entity`-классы в `data/local/entity/`. Маппинг `Entity ↔ Domain` — через extension-функции в `data/mapper/`.

**Самореференция в Task:** одна таблица `tasks` с `parent_task_id: Long?` (FK на ту же таблицу). Цикл предотвращается на уровне use case при создании/перемещении. Каскадное удаление детей при удалении родителя — `ON DELETE CASCADE`.

### Soft Delete и Архив

- **Архив** — для целей, которые завершены или отложены, но не удаляются. Доступен в отдельной секции. Архивные цели не показываются в основном списке.
- **Soft delete** — удаление с проставлением `deletedAt`. Видно в "Корзине". Авто-чистка записей старше 30 дней через ежедневный `Worker`.

### Миграции
Все изменения схемы — через `Migration` объекты. Никакого `fallbackToDestructiveMigration` в production-сборке.

---

## 6. Функциональные требования

### 6.1 Цели

**Создание цели:**
- Поля: название (обязательно), описание (markdown, опционально), эмодзи/иконка (опционально, выбор из набора + поиск по эмодзи), дедлайн (опционально).
- При создании — пустое состояние "добавьте первую задачу".

**Просмотр цели:**
- Шапка цели: эмодзи (если есть, большой), название, дедлайн, описание.
- Кнопка "Добавить задачу" — открывает форму создания задачи внутри этой цели.
- Дерево задач: иерархический список с возможностью раскрытия/сворачивания узлов. Visual indent для глубины.
- Привязанные привычки — отдельная секция под деревом задач.
- Кнопки: редактировать, архивировать, удалить.
- Кнопка "Отметить выполненной" — переводит цель в `COMPLETED`. Это **не** автоматически отмечает все задачи внутри (но показывает диалог "Также отметить N незавершённых задач?").

**Список целей:**
- Bento-сетка карточек. Каждая карточка показывает: эмодзи, название, дедлайн (если есть, с подсветкой если близко), счётчик "X из Y задач выполнено" (только верхний уровень задач).
- Pull-to-refresh не нужен (нет сети).
- FAB для создания новой цели.
- Drag-n-drop для ручной сортировки.
- Фильтры: все / активные / завершённые / архивные.

### 6.2 Задачи и подзадачи

**Иерархия:**
- Максимальная глубина — **5 уровней** (Цель → Уровень 1 → 2 → 3 → 4 → 5). На уровне UI скрываем кнопку "Добавить подзадачу" на глубине 4 (так как уровень 5 — последний).
- На уровне БД через `parent_task_id` (бесконечная вложенность, но ограничение в use case).

**Поля задачи:**
- Название, описание (markdown), эмодзи (опц), дедлайн (опц), запланировано на дату (для попадания в "Сегодня"), время (опц), повторяемость (опц), статус.

**Регулярные задачи (Recurrence):**
- Тип повторения — см. `Recurrence` sealed class.
- При создании регулярной задачи — порождается серия "виртуальных" экземпляров: на лету генерируем `scheduledFor` на следующие N дней (например, на 30 вперёд) и пересчитываем при отметке выполнения. Альтернатива: материализуем экземпляры лениво при первом показе.
- **Реализация:** хранить шаблон в одной записи + таблицу `task_occurrences(task_id, date, status)` для отметок по конкретным датам.

**Действия над задачей:**
- Чекбокс выполнения (с анимацией strike-through).
- Свайп вправо — отметить выполненной.
- Свайп влево — меню (изменить дату / отложить / удалить).
- Long press — выделение для batch-операций.
- Tap — открыть детали.

**Дерево внутри цели:**
- Раскрытие/сворачивание (стрелка слева).
- Indent — 16dp на уровень + соединительные линии (тонкие, цвет `outlineVariant`).
- Drag-n-drop внутри одного уровня и между уровнями (с автопрокруткой при перетаскивании к краям).

### 6.3 Привычки

**Создание привычки:**
- Поля: название (обязательно), описание (markdown, опц), эмодзи (опц), частота (см. `HabitFrequency`), привязка к цели (опц).
- Все привычки — **негативные** (воздержание). UX-текст это отражает: "не курил сегодня", "не пил кофе".

**Отметка (Check-in):**
- Кнопка/жест "✓ воздержался" — `DONE`.
- Кнопка/жест "✗ сорвался" — `FAILED` (обнуляет стрик).
- Отсутствие отметки — **не** обнуляет стрик (например, пользователь уснул и не отметился).
- Возможность отметить за прошлые дни (через календарь привычки).

**Стрик:**
- Для `Daily` — последовательность дней с `DONE`, прерывается только при `FAILED`.
- Для `TimesPerWeek(n)` — последовательность недель, в каждой из которых ≥ n отметок `DONE`. Прерывается, если в неделе ≥1 `FAILED` И <n `DONE`.
- Для `TimesPerMonth(n)` — аналогично, по месяцам.
- Для `SpecificDays` — последовательность периодов (неделя), где все заданные дни отмечены `DONE`.

**Калькуляция стриков** — в `domain/usecase/habit/CalculateStreakUseCase.kt`. Покрыть unit-тестами на все варианты `HabitFrequency`.

**Календарь привычки:**
- Месячный календарь (с пейджингом по месяцам).
- Каждый день — кружок/квадрат:
  - 🟢 `DONE` (зелёный)
  - 🔴 `FAILED` (красный/коралловый)
  - ⚪ нет отметки (серый/прозрачный)
  - сегодняшний день — обводка
- Тап по дню — отметить/изменить отметку.
- Сверху календаря — крупно: текущий стрик, лучший стрик, % выполнения за всё время.

**% прогресса (для целей вроде "не курить год"):**
- Если привычка привязана к цели с дедлайном — показываем процент выполнения от создания привычки до дедлайна цели.
- Расчёт: `(дней DONE) / (всего ожидаемых дней) * 100`.

### 6.4 Чек-лист «Сегодня»

**Что попадает:**
- Задачи с `scheduledFor == today` (включая виртуальные экземпляры регулярных задач).
- Привычки на сегодня:
  - `Daily` — каждый день.
  - `SpecificDays(days)` — если сегодня входит в `days`.
  - `TimesPerWeek(n)` — каждый день, пока не выполнено `n` за текущую неделю.
  - `TimesPerMonth(n)` — каждый день, пока не выполнено `n` за текущий месяц.
- **Не попадает:** задачи без `scheduledFor`, просроченные (но есть отдельная секция/баннер "У вас N просроченных").

**Сортировка:**
1. По типу — сначала задачи, затем привычки (или наоборот, см. настройки).
2. Внутри типа — по времени (если задано), затем по `orderIndex`.
3. Ручная перестановка через drag-n-drop (изменяет `orderIndex`).

**Действия:**
- Тап на чекбокс — отметить выполненной (анимация: галочка появляется с пружинкой, текст зачёркивается, элемент уезжает вниз в раздел "Выполнено сегодня" через 1 сек).
- Свайп влево на задаче — меню: "Отложить на завтра" / "+ 3 дня" / "Изменить дату" / "Удалить".
- Свайп вправо — быстрое выполнение.
- Long press — режим выделения.

**Просроченные:**
- Баннер сверху "У вас N просроченных задач" с тапом на раскрытие.
- В раскрытом виде — список с кнопками "Перенести на сегодня" / "Перенести на завтра" / "Удалить".

### 6.5 Дашборд

Главный экран статистики. **Без геймификации, без трендов**, только факты.

**Секции (в порядке вертикальной прокрутки):**

1. **Сводка дня** — большая карточка с круговым прогрессом (выполнено / всего на сегодня), количество активных привычек на сегодня, текущий день/дата.

2. **Активные стрики** — горизонтальный карусель карточек привычек, отсортированных по длине стрика убыванию. Каждая карточка: эмодзи, название, число стрика, мини-визуализация.

3. **Тепловая карта по привычкам** — для каждой активной привычки: горизонтальная линия "GitHub contributions" за последние ~13 недель. Зелёный — DONE, красный — FAILED, серый — нет данных. Тап → открыть привычку.

4. **Прогресс по целям** — список активных целей с прогресс-баром (% задач первого уровня выполнено). Сортировка: с ближайшим дедлайном сверху.

5. **Статистика за период** — переключатель неделя / месяц / год:
   - Всего выполнено задач
   - Всего отметок привычек (DONE / FAILED)
   - Лучший и средний стрик дня (по числу выполненных в день)
   - Тепловая карта общей продуктивности (день — оттенок по числу выполнений)

**Что НЕ делаем:** прогнозы, инсайты типа "ты лучше работаешь по понедельникам", сравнения с другими, рейтинги.

### 6.6 Корзина и архив

**Архив:**
- Отдельный экран (доступен из настроек или из меню списка целей).
- Содержит цели со статусом `COMPLETED` или с `archivedAt != null`.
- Действия: восстановить, удалить.

**Корзина:**
- Отдельный экран (доступен из настроек).
- Содержит все сущности с `deletedAt != null` за последние 30 дней.
- Действия: восстановить, очистить.
- Авто-очистка ежедневным `Worker`.

### 6.7 Поиск
**Не реализуем в MVP.** Список целей не превысит десятков элементов — фильтров достаточно.

### 6.8 Экспорт / Импорт
- **MVP:** не включаем.
- **Поздний спринт:** экспорт всей БД в JSON-файл через системный `ACTION_CREATE_DOCUMENT`. Импорт — обратная операция с подтверждением "заменить текущие данные?".

---

## 7. Дизайн-система

### 7.1 Общая стилистика

**Направление: Bold / Bento + сдержанный Glass.** Большие карточки, явные акценты, много воздуха, крупная типографика, мягкие тени (для светлой) или подсветки (для тёмной), скругления 20–28dp на крупных контейнерах.

**Анимации:**
- Spring-based для микро-взаимодействий (галочки, чекбоксы, разворачивание).
- Shared element transitions между экраном списка целей и детальным экраном (`SharedElementTransition` API в Compose).
- Stagger-анимация появления элементов списка (каждый со сдвигом ~40мс).
- Скелетоны при первой загрузке (не индикатор прогресса).
- Haptic feedback на критичных действиях (отметка выполненной, ошибка, удаление).

### 7.2 Цветовая палитра

**Светлая тема (`Light`):**
```kotlin
// presentation/theme/Color.kt
val LightPrimary       = Color(0xFFEC6A3C)   // тёплый коралл — основной акцент
val LightOnPrimary     = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFFFE0D4)
val LightSecondary     = Color(0xFF3D5A6C)   // приглушённый синий
val LightTertiary      = Color(0xFF7B6F50)   // тёплая охра
val LightBackground    = Color(0xFFFAF7F2)   // кремовый
val LightSurface       = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF2EDE5)
val LightOutline       = Color(0xFFD4CCC0)
val LightOutlineVariant = Color(0xFFE8E2D6)
val LightOnBackground  = Color(0xFF1F1B16)
val LightOnSurface     = Color(0xFF1F1B16)
val LightOnSurfaceVariant = Color(0xFF55514A)
val LightError         = Color(0xFFBA1A1A)
val LightSuccess       = Color(0xFF4F8A4F)   // спокойный зелёный
```

**Тёмная тема (`Dark`):**
```kotlin
val DarkPrimary        = Color(0xFFFF8A60)   // светлее коралл для контраста
val DarkOnPrimary      = Color(0xFF1F1B16)
val DarkPrimaryContainer = Color(0xFF5A2A14)
val DarkSecondary      = Color(0xFFA8C0CE)
val DarkTertiary       = Color(0xFFD4C5A0)
val DarkBackground     = Color(0xFF12110F)   // глубокий тёплый чёрный
val DarkSurface        = Color(0xFF1A1815)
val DarkSurfaceVariant = Color(0xFF24211D)
val DarkOutline        = Color(0xFF3D3833)
val DarkOutlineVariant = Color(0xFF2A2724)
val DarkOnBackground   = Color(0xFFEFEAE0)
val DarkOnSurface      = Color(0xFFEFEAE0)
val DarkOnSurfaceVariant = Color(0xFFB4AEA3)
val DarkError          = Color(0xFFFFB4AB)
val DarkSuccess        = Color(0xFF8FCD8F)
```

**Семантические цвета:**
- `success` — отметка выполнено, DONE
- `error` — FAILED, удаление, дедлайн просрочен
- `warning` (генерируем из tertiary) — дедлайн близко
- Стрики: градиент от `primary` к `tertiary` по длине

### 7.3 Типографика

Использовать Material 3 `Typography` с кастомным шрифтом. **Шрифт: Inter** (доступен бесплатно с поддержкой кириллицы, Variable Font).

```kotlin
val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)
```

**Шкала (адаптированная):**
- `displayLarge`: 48sp / Bold — заголовки секций дашборда, цифры стриков
- `headlineLarge`: 32sp / SemiBold — заголовок экрана
- `headlineMedium`: 24sp / SemiBold — заголовки карточек целей
- `titleLarge`: 20sp / Medium — заголовки задач
- `titleMedium`: 16sp / Medium — заголовки в чек-листе
- `bodyLarge`: 16sp / Regular — основной текст
- `bodyMedium`: 14sp / Regular — описания
- `labelLarge`: 14sp / Medium — кнопки
- `labelSmall`: 11sp / Medium — мета-инфо, даты

### 7.4 Компоненты (переиспользуемые)

**Все в `presentation/components/`:**

- `AimCard` — базовая карточка (rounded 20dp, elevation 1dp на свету / тонкий бордер 1dp `outlineVariant` на тёмной)
- `AimChecklistItem` — элемент чек-листа с чекбоксом и swipe-действиями
- `AimCheckbox` — кастомный чекбокс с spring-анимацией галочки
- `AimSegmentedControl` — переключатель неделя/месяц/год
- `AimEmojiPicker` — bottom sheet с поиском эмодзи
- `AimDatePicker` — обёртка над Material DatePicker
- `AimEmptyState` — для пустых списков: эмодзи, заголовок, подзаголовок, CTA
- `AimFAB` — расширяющийся FAB (иконка + текст при scrollUp)
- `AimChip` — для тегов статусов, фильтров
- `AimSnackbar` — для undo действий (удаление, отметка)
- `AimProgressRing` — круговой прогресс
- `AimHeatmap` — тепловая карта привычек
- `AimTaskTree` — дерево задач с разворачиванием
- `AimSwipeContainer` — обёртка для swipe-actions
- `AimMarkdownText` — рендер markdown в read-only режиме
- `AimMarkdownEditor` — поле ввода с подсветкой markdown
- `AimTopBar` — стандартный topbar с заголовком и actions

### 7.5 Иконография
- **Material Symbols** (rounded стиль). Подключить через `androidx.compose.material.icons.extended` или скачать SVG из Material Symbols.
- **Эмодзи** — Unicode стандартные (рендерится системным шрифтом).

### 7.6 Сетка и отступы
- Базовый отступ: **16dp** от краёв экрана.
- Отступ между карточками: **12dp**.
- Внутренние отступы карточек: **20dp**.
- Bento-сетка: 2 колонки, отношения 1:1 или 2:1 для крупных карточек.

### 7.7 Spec для Claude Code

> При создании каждого экрана: сначала определи **скелет состояний** (`Loading` / `Empty` / `Error` / `Content`), затем рендери. **Не пропускай пустые состояния** — каждый список должен иметь продуманный `AimEmptyState`. **Не используй стандартные тостеры/диалоги без обёртки** — все snackbars через `AimSnackbar`, все диалоги через `AimAlertDialog`. **Анимации обязательны** на действиях с чекбоксами, удалении, перетаскивании.

---

## 8. Уведомления

### 8.1 Типы уведомлений

| ID | Название | Дефолт время | Поведение |
|----|---------|---------|----------|
| `morning_brief` | Утренний brief | 08:00 | "Доброе утро! Сегодня у вас N задач и M привычек." Тап → экран Сегодня |
| `first_thing` | Первое дело дня | 09:00 | Заголовок самой приоритетной задачи. Только если в задачах сегодня есть `isPriority` |
| `task_reminder` | Напоминание по задаче | за `N` мин до `scheduledTime` | Заголовок задачи. Действия: "✓ выполнено", "Отложить" |
| `evening_checkin` | Вечерний check-in | 21:30 | "Время отметить привычки за день". Действия: "Открыть" |
| `streak_warning` | Предупреждение стрика | 22:30 | Только если стрик ≥ 7 дней под угрозой (не отмечена и не сорвана) |
| `weekly_summary` | Итог недели | вс 20:00 | Краткая статистика недели |
| `deadline_approaching` | Дедлайн близко | за N дней (наст. 1, 3) | "Дедлайн цели «...» через 3 дня" |

### 8.2 Настройки уведомлений (экран)

Для каждого типа:
- Toggle on/off.
- Выбор времени (где применимо).
- Дни недели (где применимо).
- Звук / без звука.
- Вибрация.
- Приоритет (тихие / стандартные / срочные → Channel importance).

**Глобальные:**
- Включить все уведомления (мастер-тоггл).
- Не беспокоить с XX:XX до XX:XX.
- Запрос разрешения `POST_NOTIFICATIONS` при первом включении.

### 8.3 Каналы
```kotlin
// core/notification/NotificationChannels.kt
enum class AimChannel(val id: String, val nameRes: Int, val importance: Int) {
    DAILY("aim_daily", R.string.channel_daily, IMPORTANCE_DEFAULT),
    TASKS("aim_tasks", R.string.channel_tasks, IMPORTANCE_HIGH),
    HABITS("aim_habits", R.string.channel_habits, IMPORTANCE_DEFAULT),
    SUMMARY("aim_summary", R.string.channel_summary, IMPORTANCE_LOW)
}
```

### 8.4 Реализация
- Точные напоминания (`task_reminder`, `first_thing`, `streak_warning`, `evening_checkin`) — `AlarmManager.setExactAndAllowWhileIdle`.
- Регулярные (`morning_brief`, `weekly_summary`) — `WorkManager.PeriodicWorkRequest`.
- Дедлайны — пересчёт на ежедневном `Worker`, постановка алярмов на нужные даты.
- BroadcastReceiver на `BOOT_COMPLETED` → пересоздание всех алярмов.
- Actions в уведомлении (`Mark as done`, `Snooze`) — через PendingIntent в BroadcastReceiver.

---

## 9. Виджет

### 9.1 Размер
- **Только большой**: `5×4` cells (`250dp × 250dp` минимум, `360dp × 360dp` предпочтительно). На Samsung One UI это будет занимать ~половину экрана.

### 9.2 Содержание
Сверху вниз:
1. **Шапка**: текущая дата ("Среда, 21 мая"), кнопка-иконка обновления.
2. **Сводка**: "X из Y выполнено" + кольцо прогресса справа.
3. **Список задач/привычек на сегодня** — до 7–10 элементов:
   - Эмодзи (если есть) + название + чекбокс
   - Тап на чекбокс — отмечает выполненной (виджет обновляется сразу).
   - Тап на элемент — открывает приложение на этой задаче.
   - Если строк больше — внизу "ещё N" с тапом на открытие приложения.
4. **Полоска привычек** — горизонтальная строка эмодзи привычек на сегодня с маленьким индикатором стрика.

### 9.3 Реализация
- Jetpack Glance (`GlanceAppWidget`).
- Действия через `ActionRunCallback` для отметки.
- Обновление: на старте, после изменения данных (через `GlanceAppWidgetManager.update`), и каждые 30 минут через `WorkManager` (для пересчёта "сегодня" в полночь).
- В тёмной теме — отдельные цвета через `GlanceTheme.colors`.
- Поддержка прозрачного фона (опция в настройках виджета — фон / без фона).

### 9.4 Конфигурация виджета
При добавлении виджета — экран `WidgetConfigurationActivity`:
- Тема (auto / light / dark).
- Прозрачность фона (0/30/60/90%).
- Показывать привычки (toggle).
- Максимум строк (5/7/10).

---

## 10. Навигация

### Структура (Bottom Navigation)
```
[Сегодня] [Цели] [Привычки] [Дашборд]
                    │
                    └── Настройки (иконка в TopBar)
```

### Маршруты (typesafe Navigation Compose)
```kotlin
sealed interface AimRoute {
    @Serializable object Today : AimRoute
    @Serializable object Goals : AimRoute
    @Serializable object Habits : AimRoute
    @Serializable object Dashboard : AimRoute
    @Serializable object Settings : AimRoute
    @Serializable data class GoalDetail(val goalId: Long) : AimRoute
    @Serializable data class TaskDetail(val taskId: Long) : AimRoute
    @Serializable data class HabitDetail(val habitId: Long) : AimRoute
    @Serializable object Archive : AimRoute
    @Serializable object Trash : AimRoute
    @Serializable object NotificationSettings : AimRoute
    @Serializable object ThemeSettings : AimRoute
}
```

### Глубинные навигационные паттерны
- Хлебные крошки в детальном экране задачи (Цель → Задача → Подзадача → ...) — кликабельные.
- Single Activity, Compose Navigation.

---

## 11. План разработки по спринтам

> Каждый спринт = автономный шаг, который оставляет приложение в рабочем (запускаемом) состоянии. После каждого спринта — `git tag` с версией, ручное тестирование.

---

### 🟦 Спринт 1 — Foundation (Каркас) [3-5 дней]

**Цель:** запускаемое приложение с навигацией, темами и дизайн-системой.

**Задачи:**
1. Инициализация проекта: Gradle KTS, version catalog, Hilt, Compose BOM, Navigation, DataStore.
2. Базовая структура папок (см. раздел 4).
3. `AimApplication` с `@HiltAndroidApp`.
4. `MainActivity` с `setContent { AimTheme { AimApp() } }`.
5. Theme: цвета (light/dark), типографика, shapes. Switch через системную тему + ручной override в DataStore.
6. Bottom Navigation с 4 пунктами + 4 экрана-заглушки.
7. Базовые компоненты: `AimCard`, `AimTopBar`, `AimEmptyState`, `AimFAB`.
8. Подключение шрифта Inter.
9. Экран настроек с переключателем темы (system/light/dark).

**Acceptance:**
- Приложение запускается, переключается между вкладками.
- Темы переключаются плавно.
- Все экраны показывают `AimEmptyState`.

---

### 🟦 Спринт 2 — Цели и задачи (CRUD) [5-7 дней]

**Цель:** полноценное создание и редактирование целей, задач, подзадач.

**Задачи:**
1. Room: `AimDatabase`, entities `goals`, `tasks` (с self-FK), DAO, миграции.
2. Domain модели и репозитории (`GoalRepository`, `TaskRepository`).
3. UseCases: `CreateGoal`, `UpdateGoal`, `DeleteGoal`, `ArchiveGoal`, `GetGoals`, `GetGoalById`, аналогично для Task.
4. Экран `GoalsScreen`: список с bento-сеткой карточек, FAB, фильтры.
5. Экран `GoalDetailScreen`: шапка цели, дерево задач (`AimTaskTree` с разворачиванием), кнопка "добавить задачу".
6. Экран `TaskDetailScreen`: детали задачи с подзадачами, хлебные крошки.
7. Формы создания/редактирования (Bottom Sheet или отдельный экран).
8. `AimEmojiPicker` — bottom sheet с эмодзи.
9. `AimMarkdownEditor` / `AimMarkdownText` — простой markdown (заголовки, **bold**, *italic*, списки, чекбоксы, ссылки). Библиотека: `markwon` или `compose-richtext`.
10. Soft delete + Корзина (экран `TrashScreen`).
11. Архив + экран `ArchiveScreen`.
12. Ограничение глубины 5 уровней (в UI).
13. Drag-n-drop для сортировки внутри уровня.

**Acceptance:**
- Можно создать цель, добавить задачи, подзадачи до 5 уровней.
- Удалить → попадает в корзину → восстановить.
- Архивировать цель → исчезает из активных, видна в архиве.

---

### 🟦 Спринт 3 — Привычки [5-7 дней]

**Цель:** полный цикл работы с привычками, отметки, стрики, календарь.

**Задачи:**
1. Entities: `habits`, `habit_check_ins`, DAO.
2. Domain: `Habit`, `HabitFrequency`, `HabitCheckIn`.
3. UseCases: CRUD + `CheckInHabit`, `UncheckHabit`, `CalculateStreak`, `GetHabitStats`.
4. Экран `HabitsScreen`: список привычек с превью стрика и эмодзи.
5. Экран `HabitDetailScreen`:
   - Шапка: эмодзи, название, стрик (большой), лучший стрик, % выполнения.
   - Месячный календарь с цветными днями (`AimHabitCalendar`).
   - Возможность отметки за любой день (DONE/FAILED/UNDO) через тап.
   - Кнопки: редактировать, архивировать, удалить.
6. Форма создания привычки с выбором `HabitFrequency`.
7. Расчёт стриков (`CalculateStreakUseCase`) — unit тесты на все варианты.
8. Привязка к цели (опц).

**Acceptance:**
- Создание привычки разных типов.
- Отметка DONE/FAILED влияет на стрик согласно правилам.
- Календарь корректно отображает прошлые отметки.
- Привязка к цели сохраняется и показывается в детальном экране цели.

---

### 🟦 Спринт 4 — Чек-лист «Сегодня» [4-5 дней]

**Цель:** главный экран приложения с задачами и привычками на день.

**Задачи:**
1. UseCase `GetTodayItemsUseCase` — собирает задачи + привычки на сегодня, включая раскрытие регулярных задач.
2. Реализация регулярных задач: материализация occurrences. Таблица `task_occurrences(task_id, date, status, completed_at)`.
3. Экран `TodayScreen`:
   - Шапка с датой и сводкой (X/Y выполнено + кольцо).
   - Баннер просроченных (сворачиваемый).
   - Список задач + привычек, сортировка по правилам (см. 6.4).
   - Drag-n-drop для ручной сортировки.
   - Swipe actions (вправо — выполнено, влево — меню).
4. Анимации: пружинка чекбокса, fade-out выполненной задачи и переход в секцию "Выполнено сегодня".
5. Snooze: "перенести на завтра" / "+3 дня" / выбрать дату.

**Acceptance:**
- На "Сегодня" попадают только релевантные элементы.
- Отметка выполнения работает с анимацией, не сбрасывается при сворачивании приложения.
- Drag-n-drop сохраняется в БД.

---

### 🟦 Спринт 5 — Уведомления [5-6 дней]

**Цель:** все типы уведомлений работают, настраиваются.

**Задачи:**
1. `NotificationChannels` — создание каналов при первом запуске.
2. `NotificationManager` wrapper с фабриками для каждого типа.
3. `AlarmScheduler` — обёртка над `AlarmManager`.
4. `WorkManager` workers:
   - `MorningBriefWorker` — daily
   - `EveningCheckinWorker` — daily
   - `WeeklySummaryWorker` — weekly
   - `DailyMaintenanceWorker` — ежедневная чистка корзины, пересчёт дедлайнов
5. `BootReceiver` — пересоздание алярмов после перезагрузки.
6. Actions в уведомлениях: "Выполнено" (через PendingIntent → BroadcastReceiver → UseCase → обновление БД → отмена уведомления).
7. Экран `NotificationSettingsScreen` — все типы, тоггл, время, дни, do-not-disturb окно.
8. Запрос `POST_NOTIFICATIONS` (первый запуск + при включении первого уведомления).
9. Запрос `SCHEDULE_EXACT_ALARM` (для Android 14+ — `USE_EXACT_ALARM`).

**Acceptance:**
- Все 7 типов уведомлений срабатывают в заданное время.
- Действие "Выполнено" из уведомления отмечает задачу без открытия приложения.
- После перезагрузки устройства уведомления продолжают приходить.
- Настройки уведомлений сохраняются и применяются.

---

### 🟦 Спринт 6 — Дашборд [4-5 дней]

**Цель:** экран статистики с тепловыми картами и стриками.

**Задачи:**
1. UseCases: `GetDashboardSummary`, `GetActiveStreaks`, `GetHeatmapData`, `GetPeriodStats`.
2. Компонент `AimHeatmap` — кастомный Compose-канвас, рисующий ячейки 13 недель × 7 дней.
3. Компонент `AimProgressRing` — круговой прогресс с анимацией.
4. Экран `DashboardScreen` со всеми секциями (см. 6.5).
5. Сегментный переключатель неделя/месяц/год.
6. Тапы на элементы → переход на соответствующие экраны (привычка → детальный, цель → детальный).

**Acceptance:**
- Все секции дашборда отображают корректные данные.
- Тепловая карта реагирует на тап (показывает дату и количество).
- Переключатель периода работает мгновенно.

---

### 🟦 Спринт 7 — Виджет [3-4 дня]

**Цель:** работающий виджет на главном экране с возможностью отметки.

**Задачи:**
1. Glance dependencies.
2. `TodayWidget : GlanceAppWidget` с layout согласно 9.2.
3. `TodayWidgetReceiver : GlanceAppWidgetReceiver`.
4. `ActionRunCallback` для отметки выполнения из виджета.
5. Обновление виджета при изменении данных в БД (через `GlanceAppWidgetManager.update`).
6. `WidgetUpdateWorker` — обновление в полночь и каждые 30 минут.
7. `WidgetConfigurationActivity` — настройка темы, прозрачности, числа строк.
8. Манифест: `<receiver>` + meta-data XML.

**Acceptance:**
- Виджет добавляется на главный экран.
- Показывает актуальный список на сегодня.
- Тап на чекбокс отмечает выполненной без открытия приложения.
- При смене темы системы — виджет обновляется.
- В полночь список сегодня обновляется автоматически.

---

### 🟦 Спринт 8 — Полировка, анимации, доп. фичи [5-7 дней]

**Цель:** довести UX до "вау"-уровня.

**Задачи:**
1. **Анимации:**
   - Shared element transitions: карточка цели ↔ детальный экран.
   - Stagger-появление элементов списка.
   - Pulse-эффект на стрике в день, когда он растёт.
   - Скелетоны на всех экранах с данными.
2. **Микро-UX:**
   - Haptic feedback на критичные действия.
   - Toast-Snackbar с "Undo" на каждое удаление и отметку.
   - Pull-to-undo на свайпах.
3. **Onboarding:** 3 экрана при первом запуске (концепция, разрешения, первая цель).
4. **Экспорт/импорт JSON** через `ACTION_CREATE_DOCUMENT` / `ACTION_OPEN_DOCUMENT`.
5. **Accessibility:** contentDescription для всех иконок, минимальный touch target 48dp, semantics для кастомных компонентов, поддержка TalkBack.
6. **Производительность:**
   - LazyColumn с `key` параметром.
   - Profile с Macrobenchmark на критичных экранах.
   - Baseline Profile.
7. **Settings extra:** размер шрифта (системный/малый/средний/крупный), переключатель "первый день недели".

**Acceptance:**
- Каждая анимация ощущается уместной, не перегружает.
- TalkBack корректно читает все экраны.
- Скорость прокрутки 60fps на средних устройствах.
- Onboarding не появляется при повторных запусках.

---

## 12. Инструкции для Claude Code

> 📌 Эта секция — приоритетный контекст для Claude Code Opus 4.7. Прочитать перед началом работы и периодически перечитывать (особенно при старте нового спринта).

### 12.1 Глобальные правила работы

1. **Один спринт = одна или несколько PR-веток.** Не смешивай задачи разных спринтов в одном изменении.
2. **Всегда сначала пишешь use case + unit тест → потом UI.** UseCase — атомарная единица бизнес-логики, должна работать без UI.
3. **При неоднозначности — задай вопрос перед реализацией.** Лучше уточнить раз, чем переделывать.
4. **Не используй `runBlocking` в production-коде.** Только `suspend` + coroutines, обёрнутые в `viewModelScope` / `WorkerScope`.
5. **Все строки UI — в `strings.xml`**, никаких хардкодов.
6. **Все цвета и размеры — из темы (`MaterialTheme.colorScheme.xxx`, `MaterialTheme.typography.xxx`).** Никаких хардкодов цветов в Composable.
7. **Каждый Composable, принимающий `Modifier`, ставит его первым параметром после обязательных.**
8. **Не используй `LiveData`** — только `StateFlow` / `Flow`.

### 12.2 Стиль кода

- Kotlin official code style (`ktlint` / `detekt`).
- Имена composable-функций — PascalCase, начинающиеся с существительного (`GoalCard`, не `RenderGoalCard`).
- `@Preview` для каждого нетривиального Composable.
- KDoc на каждом публичном UseCase и Repository.

### 12.3 Тестирование

- **Обязательно покрывать unit-тестами:** все UseCase, особенно `CalculateStreakUseCase`, `GetTodayItemsUseCase`, маппинги.
- Compose UI тесты на критичных экранах (`TodayScreen`, `HabitDetailScreen`).
- Тесты на миграции Room (`MigrationTestHelper`).

### 12.4 Что делать при ошибке

1. Никогда не глотать exceptions молча. Логировать через `Timber.e(e, "context")`.
2. На UI — `AimSnackbar` с понятным сообщением.
3. Критичные ошибки БД — fallback на пустое состояние + лог.

### 12.5 Чек-лист перед коммитом

- [ ] Код собирается без warnings.
- [ ] `./gradlew lint detekt test` проходит.
- [ ] Превью Compose рендерятся.
- [ ] Ничего не хардкожено (цвета, строки, размеры).
- [ ] Темы (light/dark) проверены визуально.
- [ ] Обновлён ChangeLog в README, если фича пользовательская.

### 12.6 Промпт-практики для самопроверки

Перед коммитом каждого крупного куска — прогоняй себя через эти вопросы:
1. **Архитектурно** — нет ли утечки слоя? (UI → не должен знать про Room. Domain → не должен знать про Compose).
2. **Тестируемо ли** — могу ли я протестировать этот UseCase без Android-зависимостей?
3. **Что произойдёт, если** — `null`, пустой список, длинная строка (1000 символов), отрицательное число, прошедшая дата?
4. **Тёмная тема** — выглядит ли это так же хорошо, как светлая?
5. **TalkBack** — поймёт ли пользователь экрана, что здесь происходит?
6. **Не закладываю ли я "потом исправлю"** — если да, оформляю как TODO с трекером.

### 12.7 Запрещено

- ❌ Облачные сервисы любого вида (Firebase, AdMob, Crashlytics).
- ❌ Аналитика и телеметрия.
- ❌ Любые SDK, требующие сетевого доступа.
- ❌ Манифест-разрешения сверх перечисленных в разделе 2.
- ❌ Любая монетизация / реклама / IAP.
- ❌ `runtime`-permissions, не указанные в ТЗ.

---

## 13. Acceptance Criteria (финальные)

Приложение считается готовым к релизу, когда:

- [ ] Все 8 спринтов завершены.
- [ ] На устройстве с Android 13–15 приложение работает без крашей минимум 1 неделю активного использования.
- [ ] Холодный старт < 800ms.
- [ ] Все экраны проходят TalkBack-тест.
- [ ] Виджет работает на Samsung One UI 6+.
- [ ] Светлая и тёмная темы выглядят целостно.
- [ ] APK размером < 15 МБ.
- [ ] Базу данных можно бэкапить через системный backup Android.
- [ ] При смене языка системы (если будут добавлены другие локали) — переключается корректно.

---

## 📦 Дальнейшее развитие (post-MVP)

Идеи, которые **не** входят в текущий план, но могут быть рассмотрены:
- Виджеты других размеров (2×2, 4×2).
- Импорт/экспорт через файл.
- Веб-версия / iOS.
- Шаблоны целей.
- Markdown-чеклисты внутри описаний.
- Голосовой ввод задачи через `RecognizerIntent`.

---

**Версия документа:** 1.0  
**Дата:** 21 мая 2026  
**Автор:** владелец проекта + Claude (Anthropic) в режиме совместного планирования
