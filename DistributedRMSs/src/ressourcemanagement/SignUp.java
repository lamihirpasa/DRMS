package ressourcemanagement;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class SignUp {

    private TextField nameField, usernameField, regionField;
    private PasswordField passwordField, confirmPasswordField;
    private Conn conn; // Assuming the Conn class provides the MongoDB connection

    public SignUp() {
        conn = new Conn(); // Initialize the connection object
    }

    public VBox createForm() {
        // Initialize text fields
        nameField = new TextField();
        usernameField = new TextField();
        regionField = new TextField(); // New field for Region
        passwordField = new PasswordField();
        confirmPasswordField = new PasswordField();

        // Form layout
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);

        // Add fields to the grid
        formGrid.add(new Label("Name:"), 0, 0);
        formGrid.add(nameField, 1, 0);
        formGrid.add(new Label("Username:"), 0, 1);
        formGrid.add(usernameField, 1, 1);
        formGrid.add(new Label("Region:"), 0, 2); // New label
        formGrid.add(regionField, 1, 2);          // New field
        formGrid.add(new Label("Password:"), 0, 3);
        formGrid.add(passwordField, 1, 3);
        formGrid.add(new Label("Confirm Password:"), 0, 4);
        formGrid.add(confirmPasswordField, 1, 4);

        // Buttons
        Button signUpButton = new Button("Sign-Up");
        Button cancelButton = new Button("Cancel");

        signUpButton.setOnAction(e -> handleSignUpAction());
        cancelButton.setOnAction(e -> handleCancelAction());

        HBox buttonBox = new HBox(10, signUpButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox formBox = new VBox(10, formGrid, buttonBox);
        formBox.setPadding(new Insets(20));
        formBox.setStyle("-fx-background-color: lightgray;");

        return formBox;
    }

    public void showSignUpDialog() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Sign-Up Form");
        dialogStage.initModality(Modality.APPLICATION_MODAL); // Makes the dialog modal

        VBox form = createForm();

        // Create scene with the form and set it on the stage
        Scene scene = new Scene(form, 900, 600); // Adjusted height to fit the new field
        dialogStage.setScene(scene);
        dialogStage.showAndWait(); // Show dialog and wait for it to be closed
    }

    private void handleSignUpAction() {
        String name = nameField.getText();
        String username = usernameField.getText();
        String region = regionField.getText(); // Get region input
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (name.isEmpty() || username.isEmpty() || region.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required!");
        } else if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match!");
        } else {
            if (registerUser(name, username, region, password)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Sign-Up Successful! Welcome, " + name);
                // Optionally, close the dialog if needed
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Username already exists!");
            }
        }
    }

    private void handleCancelAction() {
        nameField.clear();
        usernameField.clear();
        regionField.clear(); // Clear region field
        passwordField.clear();
        confirmPasswordField.clear();
    }

    private boolean registerUser(String name, String username, String region, String password) {
        try {
            MongoDatabase database = conn.getDatabase(); // Get the database from the Conn class
            MongoCollection<Document> collection = database.getCollection("users"); // Get the "users" collection

            // Check if the username already exists
            Document existingUser = collection.find(new Document("username", username)).first();
            if (existingUser != null) {
                return false; // Username already exists
            }

            // Create a new user document
            Document newUser = new Document("name", name)
                    .append("username", username)
                    .append("region", region) // Store the region
                    .append("password", password)
                    .append("role", "customer"); // Assign the role as "customer"

            // Insert the new user into the collection
            collection.insertOne(newUser);
            return true; // User registered successfully
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Return false if there is an issue with the database
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
