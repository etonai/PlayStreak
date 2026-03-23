# PlayStreak 🎵

An Android application for tracking music practice activities, performances, and progress. Built using Kotlin and AndroidX libraries.

**Current Version**: 1.0.8.15-beta

**Download on Google Play**: [PlayStreak on Google Play](https://play.google.com/store/apps/details?id=com.pseddev.playstreak)

**AI-Assisted Development**: This project was developed collaboratively with Claude Opus (initial development), Claude Sonnet (bug fixes & refinements), ChatGPT and Cursor (design consultation).

> **Project Origins**: See [`app/docs/initial-prompt.md`](app/docs/initial-prompt.md) for the original project requirements and design concepts that guided this application's development.

## Known Issues & Planned Features

⚠️ **Known Issue**: Calendar month swiping is partially disabled but may still work inconsistently. Month navigation should only use Previous/Next buttons. See Bug #1 in [`app/docs/bugs.md`](app/docs/bugs.md) for details.

🚧 **Planned Feature**: Google Drive sync integration for seamless data backup and synchronization across devices.

## Features

### 📊 **Dashboard Overview**
- Daily and monthly activity summaries
- Practice streak tracking
- Smart practice suggestions based on favorites and activity history
- Recent activity timeline for today and yesterday

### 📅 **Calendar View**
- Visual calendar with color-coded activity indicators
- Activity level visualization (light to dark based on frequency)
- Date selection to view detailed daily activities
- Month navigation with Previous/Next buttons
- Smart date formatting (Today/Yesterday/Month Day)

### 🎵 **Activity Tracking**
- **Practice Sessions**: Track with 4 difficulty levels
  - Level 1: Essentials
  - Level 2: Incomplete
  - Level 3: Complete with Review
  - Level 4: Perfect Complete
- **Performance Sessions**: Record with quality ratings
  - Level 1: Failed
  - Level 2: Unsatisfactory  
  - Level 3: Satisfactory
  - Support for Online and Live performance types
- Time tracking in minutes
- Notes and observations for each session

### 🎼 **Music Management**
- Support for both **Pieces** and **Techniques**
- Favorites system for prioritizing practice items
- Automatic categorization during CSV import
- Comprehensive piece/technique database

### 📈 **Progress Tracking**
- **Timeline Tab**: Chronological view of all activities with delete functionality
- **Pieces Tab**: Sortable list with activity counts and last practice dates
- **Abandoned Pieces**: Track pieces that haven't been practiced recently
- Activity statistics and trends

### 📤 **Data Management**
- **CSV Export**: Export all activity data with timestamps
- **CSV Import**: Import existing practice data with Unicode normalization
- **Favorites Preservation**: Maintains favorite status during import/export
- Last export time tracking
- **User Limits**:
  - Free users: Up to 3000 activities and 500 pieces
  - Pro users: Up to 4000 activities and 550 pieces

## Screenshots

*Screenshots would go here showing the main tabs and features*

## Technical Details

### Architecture
- **MVVM Pattern**: ViewModel + LiveData + Repository pattern
- **Room Database**: Local SQLite database with DAOs
- **Navigation Component**: Single-activity architecture with fragments
- **Material Design**: Modern UI following Material Design guidelines

### Key Technologies
- **Language**: Kotlin 2.0.21
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Database**: Room with SQLite
- **Calendar Library**: Kizitonwose CalendarView
- **Build System**: Gradle with Kotlin DSL

### Project Structure
```
app/src/main/java/com/pseddev/practicetracker/
├── data/
│   ├── entities/          # Room database entities
│   ├── daos/             # Data Access Objects
│   └── repository/       # Repository pattern implementation
├── ui/
│   ├── addactivity/      # Add activity flow fragments
│   ├── favorites/        # Manage favorites
│   ├── importexport/     # Data import/export
│   ├── main/            # Settings and main navigation
│   ├── pieces/          # Add pieces functionality
│   └── progress/        # Dashboard, Calendar, Timeline, Pieces tabs
└── utils/               # Utility classes (CSV, text normalization, etc.)
```

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 34+
- Kotlin plugin

### Installation
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd music-practice-tracker
   ```

2. Open the project in Android Studio

3. Sync the project and download dependencies

4. Run the app on an emulator or physical device

### Building
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Run all checks (tests + lint)
./gradlew check
```

## Usage

### Getting Started
1. **Add Your First Piece**: Use the "Add Activity" button to record your first practice session
2. **Set Favorites**: Mark frequently practiced pieces as favorites for priority suggestions
3. **Track Progress**: View your activities in the Timeline and monitor progress in the Calendar
4. **Export Data**: Backup your practice data using the CSV export feature

### Best Practices
- **Consistent Logging**: Record sessions immediately after practice for accuracy
- **Use Levels Appropriately**: Be honest about practice quality to track real progress
- **Add Notes**: Include observations about technique, problem areas, or breakthroughs
- **Review Calendar**: Use the visual calendar to identify practice patterns and gaps

### User Limits
- Free users are limited to 3000 activities and 500 pieces
- Pro users are limited to 4000 activities and 550 pieces

## Data Import/Export

### CSV Format
The app supports CSV import/export with the following format:
- Timestamp, Piece Name, Activity Type, Level, Performance Type, Minutes, Notes
- Automatic Unicode normalization handles different apostrophe characters
- Preserves favorites during import operations
- Import will respect user limits (3000/4000 activities, 500/550 pieces depending on account type)

### Backup Strategy
- Regular CSV exports for data backup
- Import functionality for data migration
- Last export time tracking for backup reminders

## Contributing

### Documentation
- **Bug Reports**: Bug tracking is maintained in `app/docs/bugs.md`
- **Project Origins**: See `app/docs/initial-prompt.md` for the original project requirements and design concepts

### Bug Reports
When reporting issues:
1. Check existing bugs first
2. Provide clear reproduction steps
3. Include device and Android version information
4. Use the bug report template provided

### Development Setup
1. Follow the installation steps above
2. Review the codebase architecture in `CLAUDE.md`
3. Run tests before submitting changes: `./gradlew check`
4. Follow existing code style and patterns

## Version History

- **v1.0.7.4** - Latest features and bug fixes
- **v1.0.8** - Target version for current development

## License

Copyright (c) 2025 PlayStreak. All rights reserved.

This software is proprietary and confidential. See the [COPYRIGHT](COPYRIGHT) file for full licensing terms.

## Development

This project was developed with the assistance of AI tools:
- **Initial Development**: Claude Opus handled the core application architecture and feature implementation
- **Bug Fixes & Refinements**: Claude Sonnet provided bug fixes, optimizations, and feature enhancements
- **Design Consultation**: ChatGPT and Cursor assisted with design decisions, provided feedback on user experience, and asked insightful questions that guided design choices

The combination of AI-assisted development and human oversight resulted in a robust, well-structured Android application following modern development best practices.

## Acknowledgments

- **Kizitonwose CalendarView** - Excellent calendar library for Android
- **Material Design** - Google's design system
- **AndroidX Libraries** - Modern Android development components
- **AI Development Tools** - Claude Opus, Claude Sonnet, ChatGPT, and Cursor for development assistance

## Support

For questions, issues, or feature requests:
1. Check the existing documentation
2. Review `app/docs/bugs.md` for known issues
3. Create a new issue with detailed information

---

**Music Practice Tracker** - Your comprehensive companion for tracking music practice and performance progress. 🎵