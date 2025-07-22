# diploma-project

An Android application to manage personal expenses, set spending limits for specific periods, track transaction history, and generate PDF reports.

## Features
### Authentication
- Login and registration on a single screen
- User data stored locally in SQLite

## Home Screen
- Displays all user transactions
- Shows total expenses
- Spending limit with notification when exceeded
- Ability to set a limit for a specific period
- Add, edit, and delete transactions

## History Screen
- View transactions for a selected date range
- Filter transactions by period
- Import/Export transactions in JSON format
- Generate PDF reports for the selected period

## Settings
- Toggle between light and dark themes
- Logout from the account
- Delete the user and all related transactions

# Tech Stack
- Kotlin – main programming language
- SQLite – local database for users and transactions
- RecyclerView – transaction list display
- MaterialDatePicker – date range selection
- PdfDocument API – PDF report generation
- ActivityResultContracts – file import/export
- SharedPreferences + SQLite – combinedtorage for user preferences

# How to Run
- Clone the repository
git clone https://github.com/BusinessCrab/diploma-project.git
