package ressourcemanagement;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class UserDashboard extends Application {
    private String username;
    private Notification notify;

    // Constructor to initialize the username
    public UserDashboard(String username) {
        this.username = username; // Initialize username
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("DISTRIBUTED RESOURCE MANAGEMENT SYSTEM - User Dashboard");
        notify = new Notification(username); // Initialize Notification with the username

        // Create a menu bar
        MenuBar menuBar = new MenuBar();

        // Home Menu
        Menu homeMenu = createMenu("Home", Color.BLUE);
        MenuItem homeItem = new MenuItem("Home");
        homeItem.setOnAction(e -> showMessage("Home functionality not implemented."));
        homeMenu.getItems().add(homeItem);

        // Release Resource Menu
        Menu releaseMenu = createMenu("Release Resource", Color.BLUE);
        MenuItem releaseItem = new MenuItem("Release Resource");
        releaseItem.setOnAction(e -> {
            ReleaseResource release = new ReleaseResource();
            release.showReleaseDialog();
        });
        releaseMenu.getItems().add(releaseItem);

        // Notification Menu
        Menu notificationMenu = createMenu("Notification", Color.BLUE);
        MenuItem notificationItem = new MenuItem("View Notifications");
        notificationItem.setOnAction(e -> notify.viewNotifications());
        notificationMenu.getItems().add(notificationItem);

        // Feedback Menu
        Menu feedbackMenu = createMenu("Feedback", Color.BLUE);
        MenuItem feedbackItem = new MenuItem("Submit Feedback");
        feedbackItem.setOnAction(e -> {
            FeedBack feedback = new FeedBack();
            feedback.showFeedBackDialog();
        });
        feedbackMenu.getItems().add(feedbackItem);

        // About Menu
        Menu aboutMenu = createMenu("About", Color.BLUE);
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> {
            About about = new About();
            about.showAboutDialog();
        });
        aboutMenu.getItems().add(aboutItem);

        // Exit Menu
        Menu exitMenu = createMenu("Exit", Color.RED);
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> {
            boolean confirmExit = confirmDialog("Are you sure you want to exit?");
            if (confirmExit) {
                primaryStage.close();
            }
        });
        exitMenu.getItems().add(exitItem);

        // Add menus to the menu bar
        menuBar.getMenus().addAll(homeMenu, releaseMenu, notificationMenu, feedbackMenu, aboutMenu, exitMenu);

        // Create the dashboard content
        Label contentLabel = new Label("DRMS User Dashboard");
        contentLabel.setFont(new Font("Tahoma", 24));
        contentLabel.setTextFill(Color.WHITE);

        VBox contentBox = new VBox(contentLabel);
        contentBox.setStyle("-fx-background-color: #87CEFA; -fx-alignment: center; -fx-padding: 20px;");

        // Set up the layout
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(contentBox);

        // Create the scene and set it on the stage
        Scene scene = new Scene(root, 940, 750);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Menu createMenu(String title, Color color) {
        Menu menu = new Menu(title);
        menu.setStyle("-fx-text-fill: " + toHexString(color) + "; -fx-font-family: 'Tahoma'; -fx-font-weight: bold; -fx-font-size: 20px;");
        return menu;
    }

    private void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirmDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X", 
                (int) (color.getRed() * 255), 
                (int) (color.getGreen() * 255), 
                (int) (color.getBlue() * 255));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
