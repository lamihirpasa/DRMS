package ressourcemanagement;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class DRMSApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("DISTRIBUTED RESOURCE MANAGEMENT SYSTEM");

        // Create a menu bar
        MenuBar menuBar = new MenuBar();

        // Home Menu
        Menu homeMenu = createMenu("Home", Color.BLUE);
        MenuItem homeItem = new MenuItem("Home");
        homeItem.setOnAction(e -> showMessage("Home functionality not implemented."));
        homeMenu.getItems().add(homeItem);

        // Request Menu
        Menu requestMenu = createMenu("Request", Color.BLUE);
        MenuItem requestResourceItem = new MenuItem("Request for Resource");
        requestResourceItem.setOnAction(e -> {
    RequestResource request = new RequestResource();
    request.showRequestDialog();
});
        requestMenu.getItems().add(requestResourceItem);

        // Register Menu
        Menu registerMenu = createMenu("To Register", Color.BLUE);
        MenuItem signUpItem = new MenuItem("Sign_Up");
        signUpItem.setOnAction(e -> {
    SignUp signup = new SignUp();
    signup.showSignUpDialog();
});
        MenuItem signInItem = new MenuItem("Sign_In");
        signInItem.setOnAction(e -> {
    SignIn signin = new SignIn();
    signin.showSignInDialog();
});
        registerMenu.getItems().addAll(signUpItem, signInItem);

        // About Menu
        Menu aboutMenu = createMenu("About", Color.BLUE);
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> {
    About about = new About();
    about.showAboutDialog();
});
        aboutMenu.getItems().add(aboutItem);

        // Exit Menu
        Menu exitMenu = createMenu("Exit", Color.BLUE);
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> {
            boolean confirmExit = confirmDialog("Are you sure you want to exit?");
            if (confirmExit) {
                primaryStage.close();
            }
        });
        exitMenu.getItems().add(exitItem);

        // Add menus to the menu bar
        menuBar.getMenus().addAll(homeMenu, requestMenu, registerMenu, aboutMenu, exitMenu);

        // Create the dashboard content
        Label contentLabel = new Label("DRMS Dashboard");
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

    // Helper method to create a menu with a specific color
    private Menu createMenu(String title, Color color) {
        Menu menu = new Menu(title);
        menu.setStyle("-fx-text-fill: " + toHexString(color) + "; -fx-font-family: 'Tahoma'; -fx-font-weight: bold; -fx-font-size: 20px;");
        return menu;
    }

    // Helper method to show a message dialog
    private void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Helper method to show a confirmation dialog
    private boolean confirmDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(message);

        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    // Helper method to convert a Color object to a hex string
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
