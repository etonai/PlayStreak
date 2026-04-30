# MyStreak — App Design Document

## Overview

MyStreak is an Android activity tracking app modeled after PlayStreak. Where PlayStreak tracks musical pieces practiced over time, MyStreak tracks general **Tasks** and logs each time the user performs them as **Activities**. The app is built around streaks, daily accountability, and prioritization.

---

## Core Definitions

| Term | Definition |
|---|---|
| **Task** | Something the user should do/perform on a recurring basis. Defines the "what." |
| **Activity** | A single instance of a Task being performed at a specific point in time. Defines the "when" and "how well." |

---

## Navigation / Tabs

The app consists of three primary tabs:

| Tab | Description |
|---|---|
| **Dashboard** | At-a-glance view of streak, today's progress, and outstanding priorities |
| **Calendar** | Visual history of activity levels by day |
| **Tasks** | Manage the list of Tasks (analogous to Pieces in PlayStreak) |

---

## Dashboard

The Dashboard provides a daily summary and motivates the user to maintain their streak.

### Sections

- **Current Streak** — Number of consecutive days where at least one Activity was logged. The streak is active for a given day if *any* Activity is performed that day. The day boundary is **midnight local time**. Whether the streak is computed from frozen calendar colors or recalculated from live Activity data is an **implementation decision** — whichever approach is easier to build is acceptable.
- **Today's Activities** — All Activities logged today, sorted by time logged (ascending), always fully expanded. Shown with Task color, time, and success level. Includes Activities from Tasks that are currently inactive.
- **Yesterday's Activities** — All Activities logged yesterday, sorted by time logged (ascending), always fully expanded. Includes Activities from Tasks that are currently inactive.
- **Week Summary** — Shows **total Activities** logged and **total High Priority Activities** logged across the **last 7 rolling days**. Each individual log instance counts separately toward these totals (e.g., logging the same Task 3 times in one day contributes 3 to the count). Only Activities from **currently-active Tasks** are counted — Activities from inactive Tasks are excluded.
- **High Priority Outstanding** — A highlighted list of High Priority Tasks that have *not* yet been performed today. A Task is removed from this list as soon as **any Activity for that Task is logged today**, regardless of success level.

---

## Tasks

Tasks are the core unit of the app. Each Task represents something the user intends to do repeatedly. All active Tasks are considered available every day. Task ordering in the Tasks tab follows the same approach PlayStreak uses for pieces.

### Tasks Tab Entry Display

Each Task entry in the Tasks tab shows:
- Task name
- Task color
- Priority badge (High / Low)
- Today's logged Activity count — raw number of individual log instances for that Task today (e.g., "3" if logged three times)
- Active / Inactive status indicator

**Tapping a Task entry opens the Task detail/edit screen.**

### Task Properties

| Property | Description |
|---|---|
| **Task Name** | Display name for the Task |
| **Task Color** | Color chosen from a **predefined palette** (same palette as PlayStreak), used to visually identify the Task across the app |
| **Task Priority** | `High` or `Low` — determines how the Task factors into streak color and dashboard prominence |
| **Minimum Success** | User-defined text description of the minimum threshold required to log an Activity |
| **Medium Success** | User-defined text description of satisfactory completion |
| **High Success** | User-defined text description of excellent completion |

> Success thresholds are free-text descriptions set by the user. Examples: "Minimum = 15 minutes / Medium = 30 minutes / High = 1 hour" or "Minimum = 0.5 miles / Medium = 1 mile / High = 2 miles." **Minimum is the floor for logging** — the user must have achieved at least the Minimum level to record an Activity at all. If they did not reach Minimum, no Activity is logged.

### Task Editing

All Task properties (name, color, priority, and success threshold descriptions) are editable after creation. Each day's calendar color is **frozen at midnight** — once a day passes, its color is cached and no longer recalculated. This means priority changes only affect today (whose color has not yet been frozen) and future days; past days' frozen colors are unaffected.

### Task Status (Active / Inactive)

Tasks can be toggled between **Active** and **Inactive**:

- **Active** — the Task appears in the logging flow and in the Dashboard's High Priority Outstanding list (if High priority).
- **Inactive** — the Task is hidden from the logging flow and Outstanding list. It cannot be logged against while inactive. Its Activities are excluded from the Week Summary counts.

Inactive Tasks still appear in the Tasks tab (alongside active Tasks), **greyed out** to visually distinguish them from active Tasks, allowing the user to find and reactivate them. Toggling a Task inactive does not delete it or its historical Activities.

### Task Deletion

Deleting a Task shows a **confirmation warning** before proceeding. If confirmed, the Task and all of its historical Activities are permanently removed from the calendar and daily listings.

---

## Logging an Activity

Activity logging follows the same flow as PlayStreak. The user logs an Activity for the current day.

**Success level selection:** When logging, the user is shown all three threshold descriptions (Minimum / Medium / High) and taps the one they achieved. Since Minimum is the threshold for logging, every recorded Activity has a success level of at least Minimum. If the user did not reach Minimum, no Activity is recorded.

**Logging entry point:** Activity logging can be initiated from both the **Dashboard** and the **Tasks tab**, following the same pattern PlayStreak uses.

**Editing an Activity:** An Activity can be edited after logging. Editable fields are:
- Date / time (constrained to **today or earlier** — future dates are not allowed)
- Success level

An Activity carries no notes or free-text field — the only recorded data per Activity is the Task, date/time, and success level.

The **Task association is fixed** — once an Activity is logged for a Task, it cannot be reassigned to a different Task.

**Deleting an Activity:** Activities can be deleted. A **confirmation warning** is shown before the deletion is applied.

**Retroactive logging:** The user logs an Activity for today and then edits its date/time to reflect the actual past date. The Activity is removed from today's entries and added to the target past day's activity listing. However, **the target past day's frozen calendar color is not updated** — only today's live color changes (reflecting the removed Activity). If the moved Activity was the only Activity for today, today's streak credit is lost immediately.

**Multiple logs per day:** The same Task can be logged multiple times in a single day. Each instance is recorded separately and appears independently in the daily listing. However, for calendar color logic, a Task counts as **performed once** for that day regardless of how many times it was logged.

---

## Calendar

The Calendar view gives a visual history of daily activity intensity, using the same layout as PlayStreak. The day boundary is **midnight local time**.

### Activity Level Color Guide

| Color | Condition |
|---|---|
| **Light Blue** | At least one Activity was performed that day |
| **Medium Blue** | At least one High Priority Activity was performed that day |
| **Dark Blue** | At least half (rounded down) of all High Priority Tasks available that day were performed |
| **Bright Green** | All High Priority Tasks available that day were performed |

> **Calendar color freezing:** Each day's calendar color is computed and frozen the next time the user opens the app after midnight. Past days' colors are immutable — priority changes, Task edits, active/inactive toggles, and retroactively added Activities do not alter already-frozen colors. This is why Activities from currently-inactive Tasks still appear in past colors (the color was frozen when those Tasks were active), and why priority changes don't rewrite history. Note: a past day's activity listing (shown when tapping the day) reflects all actual Activities including any retroactively added ones, even if the displayed color was frozen before those Activities were logged. This divergence between color and listing for retroactive entries is an accepted known behavior.
>
> **For today (live):** Today's color is computed dynamically using currently-active High Priority Tasks and their current priorities, updating in real time as Activities are logged.
>
> **For past days (frozen):** The color reflects the Tasks and priorities that were in effect when that day's color was frozen at midnight.
>
> A High Priority Task counts as "performed" for a given day if at least one Activity for that Task was logged on that day. Multiple logs of the same Task still count as one performed Task.

### Daily Activity Listing

Tapping a day on the Calendar shows all Activities logged for that day, sorted by **time logged (ascending)**. Activities from Tasks that are currently inactive are shown. Each entry displays:
- Task color
- Time the Activity was logged
- Level of success (Minimum / Medium / High)

---

## Platform

Android only. Dark mode is supported.

## Data & Storage

Data is stored **locally on the device** (no cloud sync). **Import/Export** is planned using **JSON format**, following the same approach and file format as PlayStreak.

---

## Questions & Answers

1. **Success threshold units** — What do the Minimum, Medium, and High success thresholds measure?
   > Thresholds are free-text descriptions defined by the user. The user sets their own descriptions and self-determines whether they met a threshold when logging. Examples: time-based (15 min / 30 min / 1 hr) or distance-based (0.5 mi / 1 mi / 2 mi). Different Tasks can use entirely different unit types.

2. **Logging an Activity** — How does the user record an Activity?
   > Logging follows the same flow as PlayStreak.

3. **Task availability and scheduling** — Are all Tasks available every day?
   > Yes, all Tasks are available every day.

4. **Retroactive logging** — Can the user log an Activity for a past date/time?
   > The user logs the Activity for today and then edits it to set the correct past date/time, following the same pattern as PlayStreak.

5. **Streak reset time** — When does a day end?
   > Midnight local time, matching PlayStreak behavior.

6. **Historical Task preservation** — If a Task is deleted, are its historical Activities preserved?
   > No — deleting a Task removes all of its historical Activities from the calendar and daily listings.

7. **Activity count per Task per day** — Can the same Task be logged multiple times in one day?
   > Yes. Each instance is recorded separately and appears independently in daily listings.

8. **Week Summary content** — What does the Week Summary section show?
   > Total Activities logged and total High Priority Activities logged for the week.

9. **Notifications / Reminders** — Are reminders planned?
   > No notifications or reminders are planned.

10. **Platform target** — Android only or cross-platform?
    > Android only.

---

## Follow-up Questions & Answers

1. **Success threshold display when logging** — When the user logs an Activity, how are the success thresholds presented?
   > The user sees all three description strings (Minimum / Medium / High) and taps the one they achieved.

2. **Multi-log calendar counting** — When the same High Priority Task is logged multiple times in one day, does it count as "performed once" or multiple times for the calendar color logic?
   > It counts as "performed once" for the calendar color logic.

3. **Week Summary date range** — Does the Week Summary cover a fixed calendar week or a rolling window?
   > The last 7 rolling days.

4. **Edit flow for retroactive activities** — When editing an Activity, what fields can be changed?
   > Date/time and success level. Task association cannot be changed after logging.

5. **Task deletion warning** — Should the app warn before deleting a Task and its history?
   > Yes — show a confirmation warning before deletion.

6. **"Performed" definition for calendar colors** — Does an Activity need to reach a certain success level to count as "performed" for the calendar color logic?
   > Minimum is the minimum level required to log an Activity at all, so any logged Activity automatically counts. A Task is performed if any Activity for it was recorded that day.

---

## Second Follow-up Questions & Answers

1. **Below-minimum performance** — If a user attempts a Task but does not reach the Minimum threshold, can they still record anything?
   > No — if the user did not reach Minimum, no Activity is recorded.

2. **Task reassignment and calendar history** — Can an Activity be edited to change its Task association?
   > No — Task association is fixed at the time of logging and cannot be changed.

3. **"High Priority Outstanding" removal trigger** — What triggers a Task's removal from the Outstanding list?
   > Any logged Activity for that Task today, regardless of success level.

---

## Third Follow-up Questions & Answers

1. **Retroactive edit and calendar update** — When a user edits an Activity's date/time to move it to a past day, does it fully relocate on the calendar?
   > Yes — it is fully relocated. The Activity is removed from the original day's entries and added to the target day's entries, with both days' color ratings updating accordingly.

2. **Week Summary Activity counting** — Does logging the same Task multiple times in one day count as multiple Activities toward the Week Summary totals?
   > Yes — each log instance counts separately (3 logs = count of 3 in the total).

---

## Fourth Follow-up Questions & Answers

1. **Retroactive edit and streak impact** — If a user edits the date/time of their only Activity for today to a past day, does it break today's streak?
   > Yes — the streak breaks immediately with no protection.

2. **Date editing constraints** — Can the user edit an Activity's date forward to a future date?
   > No — date editing is constrained to today and earlier only.

---

## Fifth Follow-up Questions & Answers

1. **Task editing** — Can a Task be edited after it is created? Do priority changes retroactively affect historical calendar colors?
   > Yes and yes — all Task properties are editable, and changing priority retroactively recalculates all historical calendar color ratings.

2. **Task archiving** — Is there a way to make a Task inactive without permanently deleting it and its history?
   > Yes — Tasks can be toggled to inactive. An inactive Task is hidden from the logging flow and Outstanding list but its history is preserved.

3. **Activity sort order** — How are Activities ordered in daily listings?
   > By time logged.

---

## Sixth Follow-up Questions & Answers

1. **Inactive Tasks and historical calendar colors** — Do historical Activities from an inactive Task still count toward past calendar color calculations?
   > Yes — past calendar colors are not affected by a Task's current inactive status. Historical Activities still count.

2. **Inactive Tasks and the "available" count** — Are inactive Tasks counted in the denominator for Dark Blue / Bright Green on current and future days?
   > No — only currently-active Tasks count as "available" for current and future days.

3. **Retroactive priority change and "available" history** — Does the calendar use the Task's current priority or the priority it held at the time of each past day?
   > The priority the Task held at the time. Changing priority today does not alter how past days were calculated — those days retain the priority value in effect when they occurred.

---

## Seventh Follow-up Questions & Answers

1. **Priority history storage** — How does the app know what priority a Task had on any given past day?
   > Each day's calendar color is frozen/cached at midnight. Future priority or status changes only affect today (not yet frozen) and future days.

2. **Inactive Tasks in Dashboard daily listings** — Do Activities from currently-inactive Tasks still appear in Today's/Yesterday's Activities on the Dashboard?
   > Yes.

---

## Eighth Follow-up Questions & Answers

1. **Midnight freeze trigger** — Is the freeze triggered automatically at midnight or on next app open?
   > The freeze happens the next time the user opens the app after midnight.

2. **Dashboard daily listing sort order** — Do Today's/Yesterday's Activities use the same sort as the Calendar daily listing?
   > Yes — same sort order: time logged ascending.

3. **Retroactive Activity and frozen past days** — Does retroactively moving an Activity to a past day update that day's frozen calendar color?
   > No — the frozen color is unchanged. The Activity appears in the past day's activity listing, but the color remains as it was when frozen.

---

## Ninth Follow-up Questions & Answers

1. **Streak and frozen colors** — Is the streak calculated from frozen calendar colors or from live Activity data?
   > Implementation decision — whichever is easier to build is acceptable.

2. **Color vs. listing divergence** — Is it acceptable that a past day's listing may show retroactively added Activities that aren't reflected in the frozen color?
   > Yes — that unintended consequence is acceptable for now.

---

## Tenth Follow-up Questions & Answers

1. **Activity deletion** — Can a logged Activity be deleted? Is a confirmation warning shown?
   > Yes and yes — Activities can be deleted, and a confirmation warning is shown before deletion.

2. **Logging entry point** — Where does the user initiate logging an Activity?
   > From both the Dashboard and the Tasks tab, following the same pattern PlayStreak uses.

3. **Task ordering** — How are Tasks ordered in the Tasks tab?
   > Following the same approach PlayStreak uses for pieces.

---

## Eleventh Follow-up Questions & Answers

1. **Data persistence** — Is data stored locally on the device only, or is cloud backup/sync planned?
   > Local only to start. Import/Export is planned, following PlayStreak's approach.

2. **Task color selection** — Is Task color chosen from a predefined palette or a free color picker?
   > A predefined palette of colors to start.

---

## Twelfth Follow-up Questions & Answers

1. **Import/Export format** — What file format will Import/Export use?
   > Same format as PlayStreak — JSON.

2. **Predefined color palette** — Will the palette be the same as PlayStreak's piece color palette?
   > Yes — same color palette as PlayStreak.

---

## Thirteenth Follow-up Questions & Answers

1. **Activity notes** — Can the user add a free-text note when logging or editing an Activity?
   > No — an Activity records only Task, date/time, and success level.

2. **Task description field** — Does a Task have any additional free-text description beyond the name and three success threshold descriptions?
   > No.

---

## Fourteenth Follow-up Questions & Answers

1. **Tasks tab entry display** — What information does each Task entry show?
   > All of it — name, color, priority badge, today's logged Activity count, and active/inactive status indicator.

2. **Dark mode** — Is dark mode supported?
   > Yes.

---

## Fifteenth Follow-up Questions & Answers

1. **Calendar layout** — What layout does the Calendar use?
   > Same layout as PlayStreak.

2. **App settings screen** — Is there a settings screen in this version?
   > No — not in this version.

---

## Sixteenth Follow-up Questions & Answers

1. **Tasks tab Activity count format** — Does the count show the raw number of logs or a different indicator?
   > Raw number of individual logs (e.g., "3" if logged three times today).

2. **Dashboard activity list behavior** — Are Today's Activities and Yesterday's Activities collapsible?
   > No — always fully expanded.

---

## Seventeenth Follow-up Questions & Answers

1. **Inactive Tasks in the Tasks tab** — How does the user find and reactivate inactive Tasks?
   > Inactive Tasks still appear in the Tasks tab alongside active Tasks.

2. **Week Summary and inactive Tasks** — Does the Week Summary count Activities from inactive Tasks?
   > No — only Activities from currently-active Tasks are counted.

---

## Eighteenth Follow-up Questions & Answers

1. **Inactive Tasks — visual treatment in Tasks tab** — How are inactive Tasks visually distinguished in the Tasks tab?
   > Greyed out.

2. **Calendar daily listing and inactive Tasks** — Are Activities from currently-inactive Tasks shown in the Calendar daily listing?
   > Yes — they are shown.

---

## Nineteenth Follow-up Questions & Answers

1. **Task entry tap behavior** — What happens when the user taps a Task entry in the Tasks tab?
   > Opens the Task detail/edit screen.

---

## Twentieth Follow-up Questions

1. **Logging from the Tasks tab** — Since tapping a Task opens a detail/edit screen (not the logging flow directly), how does the user initiate logging from the Tasks tab? For example: a dedicated "log" button on each Task card in the list, a "log" button inside the Task detail screen, or only via a floating action button?

2. **Task detail screen contents** — What does the Task detail/edit screen show? For example: editable Task properties, the Task's activity history, the active/inactive toggle, a delete option, and/or a log button?