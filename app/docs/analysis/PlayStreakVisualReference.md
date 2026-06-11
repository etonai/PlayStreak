# PlayStreak Visual Reference Request

## Problem

MyStreak is supposed to look like PlayStreak, but the current implementation only resembles it structurally at best. The existing `PlayStreakForMyStreak.md` document describes PlayStreak's layout and behavior in text, but does not capture the actual visual appearance — colors, typography, spacing, component density, drawable styles, or theme configuration. As a result, MyStreak was built using generic Material Design defaults and does not feel like PlayStreak.

## What We Need

Please provide the following files from the PlayStreak project, with full file contents:

### 1. Theme and Color Resources
- `app/src/main/res/values/colors.xml` — all color definitions
- `app/src/main/res/values/themes.xml` — primary theme configuration
- `app/src/main/res/values-night/themes.xml` — dark mode theme (if it exists)
- `app/src/main/res/values/styles.xml` — any custom styles (if it exists)

### 2. Key Layout Files
- `fragment_dashboard.xml` — the main Dashboard screen layout
- `item_activity.xml` (or equivalent) — the list item used in Today's/Yesterday's activity lists on the Dashboard
- `fragment_pieces.xml` (or equivalent) — the Pieces/repertoire list tab
- `item_piece.xml` (or equivalent) — the list item for a Piece entry
- `fragment_calendar.xml` — the Calendar screen layout
- `item_calendar_day.xml` (or equivalent) — the calendar day cell

### 3. Drawables (if any affect visual style)
- Any shape drawables used for list item backgrounds, card backgrounds, color dots, or badges
- The app's launcher icon or any custom icons that establish the visual identity

### 4. Screenshots (most helpful of all)
If possible, screenshots of:
- The Dashboard screen (with some activities logged)
- The Pieces/repertoire tab
- The Calendar screen
- The logging flow (any step)

## Why This Matters

MyStreak's goal is for a user familiar with PlayStreak to feel immediately at home. Right now the two apps look like unrelated Material Design apps. Providing the above files will allow the MyStreak developer to directly replicate the visual language — color palette, typography choices, component styling, and layout density — rather than guessing from text descriptions.
