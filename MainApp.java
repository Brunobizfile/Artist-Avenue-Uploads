import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}

public class MainApp extends Application {

    private final DatabaseHelper dbHelper = new DatabaseHelper();
    private final ObservableList<Track> trackList = FXCollections.observableArrayList();
    private final TableView<Track> tableView = new TableView<>();

    private final TextField titleField = new TextField();
    private final TextField artistField = new TextField();
    private final TextField genreField = new TextField();
    private final TextField durationField = new TextField();
    private final TextField filePathField = new TextField();

    private Track selectedTrack = null;
    private MediaPlayer mediaPlayer = null;
    private Label statusLabel = new Label("No track loaded.");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.setTitle("Artist Avenue - Media Manager & Player");

            TableColumn<Track, Integer> idColumn = new TableColumn<>("ID");
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            idColumn.setPrefWidth(40);

            TableColumn<Track, String> titleColumn = new TableColumn<>("Title");
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            titleColumn.setPrefWidth(140);

            TableColumn<Track, String> artistColumn = new TableColumn<>("Artist");
            artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
            artistColumn.setPrefWidth(120);

            TableColumn<Track, String> genreColumn = new TableColumn<>("Genre");
            genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
            genreColumn.setPrefWidth(100);

            TableColumn<Track, Double> durationColumn = new TableColumn<>("Duration");
            durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
            durationColumn.setPrefWidth(70);

            TableColumn<Track, String> fileColumn = new TableColumn<>("Audio File Path");
            fileColumn.setCellValueFactory(new PropertyValueFactory<>("filePath"));
            fileColumn.setPrefWidth(160);

            tableView.getColumns().addAll(idColumn, titleColumn, artistColumn, genreColumn, durationColumn, fileColumn);
            tableView.setItems(trackList);

            tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedTrack = newSelection;
                    titleField.setText(newSelection.getTitle());
                    artistField.setText(newSelection.getArtist());
                    genreField.setText(newSelection.getGenre());
                    durationField.setText(String.valueOf(newSelection.getDuration()));
                    filePathField.setText(newSelection.getFilePath());
                }
            });

            GridPane formGrid = new GridPane();
            formGrid.setHgap(10);
            formGrid.setVgap(10);
            formGrid.setPadding(new Insets(10));

            Button browseButton = new Button("Browse...");
            browseButton.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav")
                );
                File initialDir = new File(System.getProperty("user.home") + "/Documents/ArtistAvenue/music");
                if (initialDir.exists()) {
                    fileChooser.setInitialDirectory(initialDir);
                }
                File selectedFile = fileChooser.showOpenDialog(primaryStage);
                if (selectedFile != null) {
                    filePathField.setText(selectedFile.getAbsolutePath());
                }
            });

            formGrid.add(new Label("Title:"), 0, 0);
            formGrid.add(titleField, 1, 0);
            formGrid.add(new Label("Artist:"), 0, 1);
            formGrid.add(artistField, 1, 1);
            formGrid.add(new Label("Genre:"), 0, 2);
            formGrid.add(genreField, 1, 2);
            formGrid.add(new Label("Duration:"), 0, 3);
            formGrid.add(durationField, 1, 3);
            formGrid.add(new Label("Audio File:"), 0, 4);

            HBox fileBox = new HBox(5, filePathField, browseButton);
            formGrid.add(fileBox, 1, 4);

            Button addButton = new Button("Add Track");
            Button updateButton = new Button("Update Track");
            Button deleteButton = new Button("Delete Track");
            Button clearButton = new Button("Clear Fields");

            addButton.setOnAction(e -> addTrack());
            updateButton.setOnAction(e -> updateTrack());
            deleteButton.setOnAction(e -> deleteTrack());
            clearButton.setOnAction(e -> clearFields());

            HBox buttonBox = new HBox(10, addButton, updateButton, deleteButton, clearButton);

            Button playButton = new Button("▶ Play");
            Button pauseButton = new Button("⏸ Pause");
            Button stopButton = new Button("⏹ Stop");

            playButton.setOnAction(e -> playAudio());
            pauseButton.setOnAction(e -> pauseAudio());
            stopButton.setOnAction(e -> stopAudio());

            HBox playerBox = new HBox(10, playButton, pauseButton, stopButton, statusLabel);
            playerBox.setPadding(new Insets(10, 0, 0, 0));

            VBox rightPane = new VBox(10, formGrid, buttonBox, new Separator(), playerBox);
            rightPane.setPadding(new Insets(10));

            BorderPane mainLayout = new BorderPane();
            mainLayout.setCenter(tableView);
            mainLayout.setRight(rightPane);

            refreshTable();

            Scene scene = new Scene(mainLayout, 980, 520);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playAudio() {
        if (selectedTrack == null || selectedTrack.getFilePath().isEmpty()) {
            showAlert("No Audio File", "Please select a track that has an assigned audio file path.");
            return;
        }

        File audioFile = new File(selectedTrack.getFilePath());
        if (!audioFile.exists()) {
            showAlert("File Not Found", "The audio file does not exist at: " + selectedTrack.getFilePath());
            return;
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        Media media = new Media(audioFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
        statusLabel.setText("Playing: " + selectedTrack.getTitle());
    }

    private void pauseAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            statusLabel.setText("Paused.");
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            statusLabel.setText("Stopped.");
        }
    }

    private void refreshTable() {
        trackList.clear();
        trackList.addAll(dbHelper.getAllTracks());
    }

    private void addTrack() {
        try {
            String title = titleField.getText();
            String artist = artistField.getText();
            String genre = genreField.getText();
            double duration = Double.parseDouble(durationField.getText());
            String path = filePathField.getText();

            if (!title.isEmpty() && !artist.isEmpty()) {
                dbHelper.addTrack(title, artist, genre, duration, path);
                refreshTable();
                clearFields();
            }
        } catch (NumberFormatException ex) {
            showAlert("Invalid Input", "Please enter a valid numeric value for duration.");
        }
    }

    private void updateTrack() {
        if (selectedTrack == null) {
            showAlert("No Selection", "Please select a track from the table to update.");
            return;
        }

        try {
            String title = titleField.getText();
            String artist = artistField.getText();
            String genre = genreField.getText();
            double duration = Double.parseDouble(durationField.getText());
            String path = filePathField.getText();

            dbHelper.updateTrack(selectedTrack.getId(), title, artist, genre, duration, path);
            refreshTable();
            clearFields();
        } catch (NumberFormatException ex) {
            showAlert("Invalid Input", "Please enter a valid numeric value for duration.");
        }
    }

    private void deleteTrack() {
        if (selectedTrack == null) {
            showAlert("No Selection", "Please select a track from the table to delete.");
            return;
        }

        dbHelper.deleteTrack(selectedTrack.getId());
        refreshTable();
        clearFields();
    }

    private void clearFields() {
        selectedTrack = null;
        titleField.clear();
        artistField.clear();
        genreField.clear();
        durationField.clear();
        filePathField.clear();
        tableView.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class Track {
        private final int id;
        private final String title;
        private final String artist;
        private final String genre;
        private final double duration;
        private final String filePath;

        public Track(int id, String title, String artist, String genre, double duration, String filePath) {
            this.id = id;
            this.title = title;
            this.artist = artist;
            this.genre = genre;
            this.duration = duration;
            this.filePath = filePath != null ? filePath : "";
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getArtist() { return artist; }
        public String getGenre() { return genre; }
        public double getDuration() { return duration; }
        public String getFilePath() { return filePath; }
    }

    public static class DatabaseHelper {
        private static final String DB_URL = "jdbc:sqlite:artist_avenue.db";

        public DatabaseHelper() {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                System.err.println("Driver not found.");
            }

            try (Connection conn = DriverManager.getConnection(DB_URL);
                 Statement stmt = conn.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS tracks (" +
                             "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                             "title TEXT NOT NULL, " +
                             "artist TEXT NOT NULL, " +
                             "genre TEXT, " +
                             "duration REAL, " +
                             "file_path TEXT)";
                stmt.execute(sql);

                try {
                    stmt.execute("ALTER TABLE tracks ADD COLUMN file_path TEXT");
                } catch (SQLException ignored) {
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public List<Track> getAllTracks() {
            List<Track> list = new ArrayList<>();
            String sql = "SELECT * FROM tracks";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new Track(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("genre"),
                        rs.getDouble("duration"),
                        rs.getString("file_path")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return list;
        }

        public void addTrack(String title, String artist, String genre, double duration, String path) {
            String sql = "INSERT INTO tracks(title, artist, genre, duration, file_path) VALUES(?, ?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, title);
                pstmt.setString(2, artist);
                pstmt.setString(3, genre);
                pstmt.setDouble(4, duration);
                pstmt.setString(5, path);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void updateTrack(int id, String title, String artist, String genre, double duration, String path) {
            String sql = "UPDATE tracks SET title = ?, artist = ?, genre = ?, duration = ?, file_path = ? WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, title);
                pstmt.setString(2, artist);
                pstmt.setString(3, genre);
                pstmt.setDouble(4, duration);
                pstmt.setString(5, path);
                pstmt.setInt(6, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void deleteTrack(int id) {
            String sql = "DELETE FROM tracks WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
