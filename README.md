# <img width="256" height="256" alt="medmonkey1" src="https://github.com/user-attachments/assets/9fe468f5-8897-4daa-9dab-edf79c47b1d1" /> MedMonkey

**MedMonkey** is an Android application developed in **Java** that helps users manage their prescription medications.  
It follows an **offline-first architecture** using Room Database and provides secure data access to external applications via a custom `ContentProvider`.  
The app also supports background task scheduling and doctor location visualization using Google Maps.

---

## Features

- Add, view, update, and delete prescription drugs  
- Mark daily intake of medications  
- Automatically verify daily intake using `WorkManager`  
- Display doctor’s location on an interactive Google Map  
- Export active medications to a local HTML file  
- Interoperability with other apps via a secure `ContentProvider`  
- Modular UI design with `Fragments` and `RecyclerView`  

---

## Technology Stack

- **Java** – Main development language  
- **Room Database** – Local data persistence  
- **WorkManager** – Background job scheduling  
- **Google Maps SDK** – Map integration  
- **ContentProvider** – Cross-app data sharing  
- **XML** – UI layouts  

---

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or newer  
- Android SDK version 24 or higher  
- Google Play Services (for Maps)  
- Internet connection (for map functionality)  

### Installation

1. Clone the repository:
   git clone git@github.com:YiannisChionas/MedMonkey.git
2. Open the project in Android Studio
3. Sync Gradle and build the project
4. Run the app on an Android emulator or physical device

### Export Functionality

You can export active prescription data from the Export section of the app.
The data is saved as an HTML file at:
  /Android/data/com.example.medmonkey/files/Download/ActiveDrugsExport.html
After exporting, the file can be opened with any browser or file viewer that supports HTML.

### Content Provider Integration

MedMonkey includes a custom ContentProvider that supports the following operations:

Query – Retrieve all or individual prescription drugs

Insert – Add a new drug using ContentValues

Update – Modify a drug’s description using its unique ID

Delete – Remove a drug by ID or using filters

A dedicated fragment, TestContentProviderFragment, is included in the app to demonstrate these operations through a simple test interface.

### Project Structure

com.example.medmonkey
├── background/                  # WorkManager worker classes
├── data/
│   ├── local/
│   │   ├── dao/                 # DAO interfaces for Room
│   │   ├── entitiy/             # Room entity classes
│   │   └── AppDatabase.java     # Room database singleton
│   └── provider/                # ContentProvider implementation
├── ui/
│   ├── adapter/                 # RecyclerView adapters
│   ├── fragments/               # UI Fragments (Add, List, Export, TestCP)
│   ├── AddDrugFragment.java
│   ├── DrugDetailFragment.java
│   ├── DrugListFragment.java
│   ├── ExportFragment.java
│   └── TestContentProviderFragment.java
├── MapPickerActivity.java       # Activity for selecting doctor's location
└── MainActivity.java            # Root activity with navigation

### License

This project is licensed under the MIT License.
You may freely use, modify, and distribute the software, provided that the original license file and author attribution are preserved.

### Author

Yiannis Chionas
GitHub: YiannisChionas
