# PlayStreak Reference for MyStreak Development

## Purpose

This document describes PlayStreak's existing design and behavior so that a developer building MyStreak can replicate the referenced patterns without access to the PlayStreak codebase. MyStreak's design document repeatedly defers to PlayStreak ("like PlayStreak", "same as PlayStreak") — this document resolves those references into concrete specifications.

---

## Tech Stack

| Item | Value |
|---|---|
| Language | Kotlin |
| UI Pattern | MVVM (Fragments + ViewModels + ViewBinding) |
| Database | Room 2.6.1 |
| Navigation | AndroidX Navigation Component 2.7.6 |
| UI Components | Material Design Components (Material 3) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |
| Dark mode | Supported |
| Architecture | Repository pattern — single `PianoRepository` as the data access layer |

---

## Navigation Structure

PlayStreak uses a **bottom navigation bar**. The full tab set is:

| Tab | Notes |
|---|---|
| Dashboard | Main screen |
| Calendar | Monthly practice grid |
| Suggestions | Recommended pieces to practice |
| Pieces | Full repertoire list |
| Timeline | Reverse-chronological activity log |
| Inactive *(Pro only)* | Pieces not practiced in 30+ days |

**MyStreak uses only 3 tabs:** Dashboard, Calendar, Tasks — there is no Suggestions or Timeline tab. The Tasks tab is MyStreak's equivalent of PlayStreak's Pieces tab.

Settings are accessed from the Dashboard screen (not a separate tab).

---

## Dashboard Screen

### Layout
```
┌────────────────────────────────┐
│  Current Streak: 12 days 🔥    │
│  ──────────────────────────    │
│  Today (3 activities):         │
│    🎵 Chopin Etude  - Level 3  │
│    🎵 Bach Invention - Perf.   │
│    ⚙️ Scales C Major - 15min   │
│                                │
│  Yesterday (2 activities):     │
│    🎵 Moonlight Son. - Level 2 │
│    ⚙️ Arpeggios - 20min        │
│                                │
│  Week Summary                  │
│  [Add Activity button]         │
│  [Settings button]             │
└────────────────────────────────┘
```

### Sections
- **Current Streak** — Large counter showing consecutive days with at least one activity. Milestone emojis appear at certain counts.
- **Today's Activities** — All activities logged today, each showing piece name, type icon, level, and duration. Listed in time-logged order (ascending). Not collapsible — always fully expanded.
- **Yesterday's Activities** — Same format as Today. Always fully expanded.
- **Week Summary** — Summary text showing activity totals for the week.
- **Add Activity button** — Primary action button to initiate logging.
- **Settings button** — Opens the Settings/Configuration screen.

---

## Logging an Activity (Add Activity Flow)

### Entry Points
Logging can be initiated from **two places**:
1. The **Add Activity button on the Dashboard**
2. A **"+" button on each Piece entry in the Pieces tab** (Pro only in PlayStreak; in MyStreak this should be accessible for all Tasks)

### Step-by-Step Flow

**Step 1 — Choose Activity Type**
```
What type of activity?
  [ Practice ]
  [ Performance ]
```

**Step 2 — Select Piece / Technique**
```
  [ + Add New ]

  Favorites:
    • Chopin Etude Op.10
    • Moonlight Sonata

  All Pieces/Techniques:
    • Arpeggios
    • Bach Invention No.8
    • ...
```
The list shows Favorites grouped at the top, then all pieces alphabetically. For Performance, only Pieces (not Techniques) are shown.

**Step 3 — Select Level**

For **Practice:**
```
  ○ Level 1 - Essentials
  ○ Level 2 - Incomplete
  ○ Level 3 - Complete with Review
  ○ Level 4 - Perfect Complete
  [ Continue ]
```

For **Performance:**
```
  ○ Level 1 - Failed
  ○ Level 2 - Unsatisfactory
  ○ Level 3 - Satisfactory
  [ Continue ]
```

**Step 4 — Duration** *(Practice only)*
```
  How many minutes? (Optional)
  [ text field ]
  [ Continue ]
  [ Skip (No Time) ]
```

**Step 5 — Notes** *(Performance only)*
```
  Add Notes (Optional)
  [ multiline text field ]
  [ Continue ]
  [ Skip (No Notes) ]
```

**Step 6 — Summary & Save**
```
  Summary
  Piece:  Chopin Etude Op.10
  Type:   Practice
  Level:  3 - Complete w/Review
  Time:   Not recorded
  Date:   Nov 21, 2024 2:30 PM

  [ Save ]
  [ Cancel ]
```

**MyStreak adaptation notes:**
- Replace "Practice / Performance" with MyStreak's success level selection (Minimum / Medium / High)
- Remove duration and notes steps (MyStreak Activities have no notes or duration)
- The "Piece" selection step becomes "Task" selection
- Show all three success threshold descriptions and let the user tap the one achieved

---

## Retroactive Logging

PlayStreak does not have a dedicated "log for a past date" flow. Instead:

1. Log the activity normally (it is created for the current date/time).
2. Navigate to the **Timeline tab**.
3. Find the activity in the list.
4. **Tap the activity** to open its edit/detail view.
5. Change the **date/time** field to the desired past date.
6. Save.

The activity is then relocated to the target date in all views. Past calendar colors are **not** retroactively updated — they were frozen at midnight when that day ended.

**MyStreak adaptation note:** MyStreak does not have a Timeline tab; the edit flow should be accessible by tapping an activity in Today's Activities, Yesterday's Activities on the Dashboard, or in the Calendar daily listing.

---

## Pieces Tab (MyStreak → Tasks Tab)

### List Display

Each entry in the Pieces list shows:
- Piece/Technique name
- Type icon (🎵 = Piece, ⚙️ = Technique)
- Activity count (total times logged)
- Last practice date
- Favorite star (toggleable inline)

### Sorting
The list has **chip-based sort controls** at the top:
- **Alphabetical** — A → Z by name
- **Date** — Most recently practiced first
- **Activity count** — Most logged first

Default sort is **Alphabetical**.

### Add Piece
A floating action button (Extended FAB) labeled **"Add Piece"** opens the Add Piece screen:
```
  Name:  [ text field ]
  Type:  ○ Piece  ○ Technique
  [ OK ]
```

### Tap Behavior
Tapping a Piece entry opens the **Piece Detail / Edit screen**, which shows:
- Name (editable)
- Type (editable)
- Favorite status
- Statistics (total practice count, performance count, last practice date, etc.)
- Recent activity history for that piece

**MyStreak adaptation:** Tasks replace Pieces. Tasks have more properties (color, priority, success thresholds) so the add/edit screen will be richer. The tap-to-detail-edit pattern is the same.

---

## Calendar Screen

### Layout
```
      November 2024
  Su Mo Tu We Th Fr Sa
            1  2  3
   4  5  6  7  8  9 10
  11 12 13 14 15 16 17
  18 19 20 21 22 23 24
  25 26 27 28 29 30

  [← Previous]  [Next →]
```

- **Standard monthly grid** (Sun → Sat columns, full month rows)
- **Month/Year header** at top
- Navigation via **Previous / Next buttons** — swipe to change months is disabled
- Each day cell shows a color indicator based on activity level

### Tapping a Day
Tapping any day cell shows a list of all Activities logged for that day (sorted by time ascending). Each Activity entry shows the piece name, type, level, and time.

### Calendar Color Freeze
Each day's color is computed and cached the **next time the user opens the app after midnight**. Past days' colors are immutable — edits, deletions, or new activities added retroactively do not update already-frozen colors.

Today's color is computed live and updates in real time as activities are logged.

### PlayStreak Calendar Colors (for reference — MyStreak uses different colors)

PlayStreak uses colors based on *activity count* and whether any performance was logged:

| Color | Condition |
|---|---|
| White / no color | No activity |
| Light Blue `#B3D9FF` | 1–3 practice-only activities |
| Medium Blue `#66B2FF` | 4–8 practice-only activities |
| Dark Blue `#0066CC` | 9+ practice-only activities |
| Light Green `#B3FFB3` | 1–3 activities including at least one performance |
| Medium Green `#66FF66` | 4–8 activities including at least one performance |
| Dark Green `#00CC00` | 9+ activities including at least one performance |

**MyStreak uses a completely different color scheme** based on High Priority task completion (Light Blue / Medium Blue / Dark Blue / Bright Green). These are new colors to be defined for MyStreak — they are not in PlayStreak's existing `colors.xml`.

---

## Streak Calculation

- A day counts toward the streak if **at least one activity was logged that day**.
- The day boundary is **midnight local time**.
- The streak is calculated by the `StreakCalculator` utility class.
- Streak is displayed on the Dashboard as a large number with an emoji (e.g., "12 days 🔥").
- The streak for past days is derived from the **frozen calendar colors** (or equivalently, from the activity data — whichever is simpler to implement for MyStreak).

---

## Import / Export

### Format
PlayStreak uses **JSON format** (as of DevCycle 2021; earlier versions used CSV). The JSON file is exported to device storage via the Android file picker.

### Export Flow
1. User taps "Export" option in Settings.
2. Android file picker opens — user chooses save location and filename.
3. Export runs with a progress indicator.
4. Success or error message is shown.

### Import Flow
1. User taps "Import" option in Settings.
2. **Warning dialog** shown: "Importing will replace all existing data."
3. Android file picker opens — user selects a JSON file.
4. Confirmation dialog shown before import begins.
5. Import validates the file and reports errors.
6. Success message shown with count of imported items.

### JSON Structure (PlayStreak — for format reference only)

PlayStreak's export contains a `pieces` array and an `activities` array. The pieces schema (post-DevCycle 2021) is:

```json
{
  "pieces": [
    {
      "id": 1,
      "name": "Moonlight Sonata",
      "type": "PIECE",
      "isFavorite": true,
      "key": "C# Minor",
      "artist": "Ludwig van Beethoven",
      "notes": "Focus on dynamics",
      "statistics": { }
    }
  ],
  "activities": [
    {
      "id": 1,
      "pieceOrTechniqueId": 1,
      "activityType": "PRACTICE",
      "timestamp": 1700000000000,
      "level": 3,
      "minutes": 30,
      "notes": "Good session"
    }
  ]
}
```

**MyStreak will have a different schema** since it has Tasks (not Pieces) and Activities with different fields. The import/export *approach* (JSON, Android file picker, warning dialog, full-replace semantics) should match.

---

## Color Palette for Task Colors

**PlayStreak pieces do not have per-piece colors** — there is no color picker or color palette for pieces in PlayStreak. When the MyStreak design says "same color palette as PlayStreak," this refers to using the same visual style and Material Design color approach, not a literal existing palette.

### PlayStreak's existing `colors.xml` for reference:

```xml
<!-- Calendar Colors -->
<color name="calendar_practice_light">#B3D9FF</color>
<color name="calendar_practice_medium">#66B2FF</color>
<color name="calendar_practice_dark">#0066CC</color>
<color name="calendar_performance_light">#B3FFB3</color>
<color name="calendar_performance_medium">#66FF66</color>
<color name="calendar_performance_dark">#00CC00</color>

<!-- Utility -->
<color name="calendar_selection_ring">#FFCC99FF</color>
<color name="star_yellow">#FFFFC107</color>

<!-- Material base colors -->
<color name="purple_500">#FF6200EE</color>
<color name="teal_200">#FF03DAC5</color>
```

**For MyStreak task colors:** a new predefined palette must be designed. Recommended approach: define 10–16 distinct, accessible colors in `colors.xml` (similar to how calendar colors are defined) and present them as a color swatch grid in the Task add/edit screen.

---

## Data Model (PlayStreak — for structural reference)

### Room Database

**Pieces Table** (`pieces_techniques`):
```kotlin
@Entity(tableName = "pieces_techniques")
data class PieceOrTechnique(
    val id: Long,
    val name: String,
    val type: ItemType,         // PIECE or TECHNIQUE
    val isFavorite: Boolean,
    val dateCreated: Long,
    val practiceCount: Int,
    val lastPracticeDate: Long?,
    // ... additional statistics fields
    val key: String?,           // Musical key (optional)
    val artist: String?,        // Composer/artist (optional)
    val notes: String?          // Notes (optional)
)
```

**Activities Table** (`activities`):
```kotlin
@Entity
data class Activity(
    val id: Long,
    val pieceOrTechniqueId: Long,   // FK to pieces_techniques
    val activityType: ActivityType,  // PRACTICE or PERFORMANCE
    val timestamp: Long,             // Milliseconds since epoch
    val level: Int,
    val performanceType: String?,
    val minutes: Int,               // -1 for performances
    val notes: String?
)
```

**MyStreak will have a different schema**: Tasks (with color, priority, success thresholds) and Activities (with Task FK, timestamp, success level). The Room + Repository + DAO pattern should be replicated.

---

## Key Utility Classes to Replicate

| PlayStreak Class | Purpose | MyStreak Equivalent Needed |
|---|---|---|
| `StreakCalculator` | Computes consecutive-day streak from activity data | Yes — same logic, different activity source |
| `ProUserManager` | Manages Pro/Free feature gating | Not needed (MyStreak has no free/pro tiers initially) |
| `JsonExporter` | Serializes data to JSON for export | Yes — adapt for Tasks + Activities schema |
| `JsonImporter` | Deserializes JSON and replaces database | Yes — adapt for Tasks + Activities schema |
| `DateUtils` | Date formatting helpers, midnight boundary logic | Yes — same midnight-boundary logic needed |
| `TextNormalizer` | Unicode normalization for piece names | Optional — useful for Task name consistency |

---

## What PlayStreak Does NOT Have (that MyStreak adds)

| Feature | Notes |
|---|---|
| Per-item colors | PlayStreak pieces have no color. MyStreak Tasks need a new color palette. |
| Task priority (High/Low) | No concept in PlayStreak. |
| Success threshold descriptions | PlayStreak has numeric levels (1–4); MyStreak uses user-defined text thresholds. |
| Active/Inactive toggle for items | PlayStreak has no active/inactive state — only deletion. |
| Calendar colors based on priority completion | PlayStreak colors based on activity count; MyStreak colors based on High Priority Task completion. |
| High Priority Outstanding list on Dashboard | New feature in MyStreak. |
| Multiple logs per same item per day as separate entries | PlayStreak allows this but each log is independent. Same pattern applies. |
