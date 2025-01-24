package ressourcemanagement;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class AdminDashboard extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("DISTRIBUTED RESOURCE MANAGEMENT SYSTEM - Admin Dashboard");

        // Create a menu bar
        MenuBar menuBar = new MenuBar();

        // Home Menu
        Menu homeMenu = createMenu("Home", Color.BLUE);
        MenuItem homeItem = new MenuItem("Home");
        homeItem.setOnAction(e -> showMessage("Home functionality not implemented."));
        homeMenu.getItems().add(homeItem);

        // Add Menu
        Menu addMenu = createMenu("Add", Color.BLUE);
        MenuItem addResourceItem = new MenuItem("Add Resource");
        addResourceItem.setOnAction(e -> {
            AddResource addResource = new AddResource();
            addResource.showAddResourceDialog();
        });
        MenuItem addAdminItem = new MenuItem("Add Admin");
        addAdminItem.setOnAction(e -> {
            AddAdmin addAdmin = new AddAdmin();
            addAdmin.showAddAdminDialog();
        });
        addMenu.getItems().addAll(addResourceItem, addAdminItem);

        // Check Status Menu
        Menu checkMenu = createMenu("Check Status", Color.BLUE);
        MenuItem resourceStatusItem = new MenuItem("Check Resource Status");
        resourceStatusItem.setOnAction(e ->{
            ResourceStatus Resource = new ResourceStatus();
            Resource.showResourceStatusDialog();
        });
        MenuItem userStatusItem = new MenuItem("Check User Status");
        userStatusItem.setOnAction(e ->{
            UserStatus status = new UserStatus();
            status.showUserStatusDialog();
        });
        checkMenu.getItems().addAll(resourceStatusItem, userStatusItem);

        // Delete Resource Menu
        Menu deleteMenu = createMenu("Delete Resource", Color.BLUE);
        MenuItem deleteResourceItem = new MenuItem("Delete Resource");
        deleteResourceItem.setOnAction(e -> {
            DeleteResource delete = new DeleteResource();
            delete.showDeleteResourceDialog();
        });
        deleteMenu.getItems().add(deleteResourceItem);

        // Remove Grant Menu
        Menu removeGrantMenu = createMenu("Remove Grant", Color.BLUE);
        MenuItem removeGrantItem = new MenuItem("Remove Grant");
        removeGrantItem.setOnAction(e -> {
            RemoveGrant remove = new RemoveGrant();
            remove.showGrantDialog();
        });
        removeGrantMenu.getItems().add(removeGrantItem);

        // Notifications Menu
        Menu notificationsMenu = createMenu("Notifications", Color.BLUE);
        MenuItem viewNotificationsItem = new MenuItem("View Notifications");
        viewNotificationsItem.setOnAction(e -> {
            Notifications notifications = new Notifications();
            notifications.viewNotifications();  // Opens a new window to view notifications
        });
        notificationsMenu.getItems().add(viewNotificationsItem);

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
        menuBar.getMenus().addAll(homeMenu, addMenu, checkMenu, deleteMenu, removeGrantMenu, notificationsMenu, exitMenu);

        // Create the dashboard content
        Label contentLabel = new Label("DRMS Admin Dashboard");
        contentLabel.setFont(new Font("Tahoma", 24));
        contentLabel.setTextFill(Color.BLACK);

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
