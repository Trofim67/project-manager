# Спринт 2. Модель данных и локальное хранение

## Сущности
| Entity | Поля | Примечание |
|---|---|---|
| `Discipline` (`disciplines`) | `id` PK autoGenerate, `name`, `teacherName?` | уникальность названия проверяется в приложении |
| `Grade` (`grades`) | `id` PK autoGenerate, `disciplineId` FK→disciplines.id, `value` Int, `date` Long (epochDay), `workType` String, `comment` String | индекс по `disciplineId`; `onDelete = CASCADE` |

Связь **Discipline 1 — N Grade**. Удаление дисциплины сознательно каскадное: UI показывает диалог «будут удалены все её оценки (N)».
Дата хранится как `epochDay` (целое число дней) — нет проблем с часовыми поясами и форматами, легко группировать по месяцам.

```
disciplines 1 ───< grades
 id PK              id PK
 name               disciplineId FK (CASCADE)
 teacherName?       value, date(epochDay), workType, comment
```

## Классы
- `data/Entities.kt` — Entity.
- `data/Daos.kt` — `DisciplineDao`, `GradeDao`: `observeAll(): Flow`, `insert`, `update`, `delete`, `deleteAll`, `insertAll`.
- `data/AppDatabase.kt` — `RoomDatabase`, version 1, файл `grades.db`, singleton.
- `data/GradeRepository.kt` — единая точка доступа: сохранение (insert/update по `id == 0`), удаление, `clearAll()` и `loadSampleData()` в транзакции.

Чтение — реактивное (`Flow` → `StateFlow` во ViewModel): любое изменение в базе сразу обновляет все экраны.

## Операции (CRUD)
| Операция | Дисциплина | Оценка |
|---|---|---|
| Создание | `saveDiscipline(id=0)` | `saveGrade(id=0)` |
| Чтение | `disciplines: Flow` | `grades: Flow` (сортировка по дате ↓) |
| Изменение | `saveDiscipline(id≠0)` | `saveGrade(id≠0)` |
| Удаление | `deleteDiscipline` (+каскад) | `deleteGrade` |

## Тестовые данные (`SampleData`)
Кнопка «Загрузить тестовые данные» (повторная загрузка не дублирует дисциплины).

| Дисциплина | Оценки | Средний |
|---|---|---|
| Математика | 5, 4, 3, 5 | 4,25 |
| Физика | 3, 3, 4, 3 | 3,25 (подсветка) |
| Информатика | 5, 5, 4, 5 | 4,75 |
| История | 4, 4 | 4,00 |
| Литература | — | «—», пустое состояние |
| **Всего** | 14 оценок, сумма 57 | **4,07** |

Даты распределены по 4 месяцам (3, 2, 1 и 0 месяцев назад от дня запуска) — для проверки динамики.

## Контрольная точка (чек-лист)
- [ ] Создать дисциплину и оценку — записи появились.
- [ ] Изменить оценку — значение обновилось.
- [ ] Удалить оценку и дисциплину — записи исчезли; оценки удалённой дисциплины тоже.
- [ ] Закрыть приложение смахиванием и открыть заново — данные на месте (сценарий T-06 в `test-cases.md`).
