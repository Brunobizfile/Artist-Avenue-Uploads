# Artist Avenue - Media Manager & Integrated Player

**Student Name:** Byron Bruno
**Course:** COP2800 - Java Programming
**Instructor:** Prof. Haque

## Description
Artist Avenue is a JavaFX desktop software application designed for independent musical artists, sound engineers, and producers. It serves as a digital asset database and local audio player, enabling creators to log track metadata (title, artist, genre, duration) and bind local .mp3 or .wav stem files for direct audio auditioning.

## Key Features
- Full Track CRUD: Create, Read, Update, and Delete track records stored in an embedded SQLite database (artist_avenue.db).
- Integrated Audio Player: Play, pause, and stop local .mp3 and .wav audio files using javafx.scene.media.MediaPlayer.
- File System Browser: Select local audio files through a native FileChooser dialog.
- Data Validation & Safety: Catches invalid input formats gracefully with error alerts.
- Apple Silicon Compatibility: Includes a custom Launcher entry point and standalone .command shortcut script for macOS execution.

## Technologies Used
- Language: Java 21 (LTS)
- UI & Media Framework: JavaFX 21 (javafx.controls, javafx.fxml, javafx.media)
- Database: SQLite (JDBC via org.xerial:sqlite-jdbc)
- Logging: SLF4J (slf4j-api-2.0.9, slf4j-simple-2.0.9)

## How to Compile & Run

### Prerequisites
- JDK 21 or newer
- JavaFX 21 SDK installed

### Terminal Commands
javac --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml,javafx.media -cp ".:sqlite-jdbc.jar:slf4j-api-2.0.9.jar:slf4j-simple-2.0.9.jar" MainApp.java

java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml,javafx.media -cp ".:sqlite-jdbc.jar:slf4j-api-2.0.9.jar:slf4j-simple-2.0.9.jar:." Launcher
