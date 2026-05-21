# Архитектурные решения

Здесь фиксируются нетривиальные решения, по которым ТЗ молчит или допускает несколько интерпретаций. Формат — лёгкий ADR.

---

## ADR-0001 — Имя проекта: Aim

**Дата:** 2026-05-21
**Статус:** принято
**Контекст:** README §0 перечисляет альтернативы (Aim · Compass · Zenith · Pursuit · NorthStar).
**Решение:** Aim. `applicationId = com.aim.app`, package `com.aim.app`, displayName «Aim».
**Последствия:** все будущие пакеты и ресурсы используют префикс `com.aim.app`. Переименование позже потребует глобального grep + Android Studio refactor.

---

## ADR-0002 — Шрифт Inter через Downloadable Fonts

**Дата:** 2026-05-21
**Статус:** принято
**Контекст:** README §7.3 требует Inter с весами Regular / Medium / SemiBold / Bold и поддержкой кириллицы. Альтернативы — bundled `.ttf` или Variable Font.
**Решение:** Downloadable Fonts через Google Fonts provider (`com.google.android.gms.fonts`). Зависимость `androidx.compose.ui:ui-text-google-fonts`. Сертификаты GMS — `res/values/font_certs.xml` (канонический Apache 2.0 файл из AOSP).
**Последствия:**
- APK не растёт от веса шрифтов.
- На первый запуск без интернета — fallback на системный san-serif.
- Требует наличие Google Play Services (приемлемо для Android 13+).

---

## ADR-0003 — Дефолт темы: Light

**Дата:** 2026-05-21
**Статус:** принято
**Контекст:** README §11 спринт 1 №5 — «переключатель system/light/dark», но не указан начальный.
**Решение:** при первом запуске тема `LIGHT`. Пользователь меняет в Настройках.
**Последствия:** на устройстве с тёмной системной темой пользователь увидит светлую тему до первого переключения. Если в фокус-группе будут жалобы, легко поменять `DEFAULT_THEME` в `ThemePreferencesDataSource`.

---

## ADR-0004 — Material Icons Extended

**Дата:** 2026-05-21
**Статус:** принято
**Контекст:** иконки для bottom navigation и будущих экранов.
**Решение:** зависимость `androidx.compose.material:material-icons-extended` целиком, без оптимизаций.
**Последствия:** в release-сборке R8/shrinker выкинут неиспользуемые иконки. Если итоговый APK превысит лимит из ТЗ §13 (15 МБ), мигрировать на точечный набор SVG в `res/drawable/`.

---

## ADR-0005 — JUnit5 для unit-тестов

**Дата:** 2026-05-21
**Статус:** принято
**Контекст:** README §2 указывает JUnit5 + Turbine + MockK. Android по-умолчанию использует JUnit4.
**Решение:** включаем JUnit Jupiter через `testOptions.unitTests.all { it.useJUnitPlatform() }` для unit-тестов в `src/test/`. Compose UI / instrumented-тесты в `src/androidTest/` продолжат использовать JUnit4 (это требование AndroidX Test).
**Последствия:** разделение между unit и instrumented тестами по runner'ам. В Sprint 2+ при появлении Compose UI тестов — отдельная зависимость `androidx-compose-ui-test-junit4`.

---

## ADR-0006 — Hilt + KSP вместо KAPT

**Дата:** 2026-05-21
**Статус:** принято
**Контекст:** Hilt 2.54 имеет стабильную поддержку KSP.
**Решение:** используем KSP (`com.google.devtools.ksp`) для генерации Hilt-кода. KAPT в проекте не подключаем.
**Последствия:** быстрее сборка, меньше шансов на конфликты аннотационных процессоров.

---

## ADR-0007 — Layered architecture для тривиальной фичи (тема)

**Дата:** 2026-05-21
**Статус:** принято
**Контекст:** Тема — простая фича: чтение/запись одной строки в DataStore. Можно было бы обойтись прямым доступом из ViewModel к DataStore.
**Решение:** проводим всю цепочку `DataStore → DataSource → Repository → UseCase → ViewModel`, даже для одного параметра.
**Последствия:** немного больше boilerplate сейчас, но это эталон для всех будущих фич. Слои `data` и `domain` готовы к расширению в Sprint 2+ без рефакторинга.
