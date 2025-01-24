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

public class SignIn {

    private TextField usernameField;
    private PasswordField passwordField;
    private Conn conn; // MongoDB connection class

    public SignIn() {
        conn = new Conn(); // Initialize the MongoDB connection
    }

    public VBox createForm() {
        // Initialize text fields
        usernameField = new TextField();
        passwordField = new PasswordField();

        // Form layout
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);

        // Add fields to the grid
        formGrid.add(new Label("Username:"), 0, 0);
        formGrid.add(usernameField, 1, 0);
        formGrid.add(new Label("Password:"), 0, 1);
        formGrid.add(passwordField, 1, 1);

        // Buttons
        Button signInButton = new Button("Sign-In");
        Button cancelButton = new Button("Cancel");

        signInButton.setOnAction(e -> handleSignInAction());
        cancelButton.setOnAction(e -> handleCancelAction());

        HBox buttonBox = new HBox(10, signInButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox formBox = new VBox(10, formGrid, buttonBox);
        formBox.setPadding(new Insets(20));
        formBox.setStyle("-fx-background-color: lightgray;");

        return formBox;
    }

    public void showSignInDialog() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Sign-In Form");
        dialogStage.initModality(Modality.APPLICATION_MODAL); // Makes the dialog modal

        VBox form = createForm();

        // Create scene with the form and set it on the stage
        Scene scene = new Scene(form, 800, 550);
        dialogStage.setScene(scene);
        dialogStage.showAndWait(); // Show dialog and wait for it to be closed
    }

    private void handleSignInAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Both fields are required!");
        } else {
            if (authenticateUser(username, password)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Sign-In Successful! Welcome, " + username);
                // Optionally, close the dialog or redirect based on the role
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid username or password!");
            }
        }
    }

    private void handleCancelAction() {
        usernameField.clear();
        passwordField.clear();
    }

    private boolean authenticateUser(String username, String password) {
    try {
        MongoDatabase database = conn.getDatabase(); // Get the database from the Conn class
        MongoCollection<Document> collection = database.getCollection("users"); // Get the "users" collection

        // Query the collection for the user with the given username and password
        Document query = new Document("username", username).append("password", password);
        Document user = collection.find(query).first();

        if (user != null) {
            String role = user.getString("role");

            // Store the username in UserSession
            UserSession.getInstance().setLoggedInUsername(username);

            // Redirect based on user role
            if (role.equalsIgnoreCase("Customer")) {
                showAlert(Alert.AlertType.INFORMATION, "Customer Dashboard", "Welcome, Customer!");
                showCustomerDashboard(); // Open the Customer Dashboard
            } else if (role.equalsIgnoreCase("Admin")) {
                showAlert(Alert.AlertType.INFORMATION, "Admin Dashboard", "Welcome, Admin!");
                showAdminDashboard(); // Show the Admin Dashboard after successful login
            }
            return true; // Successful authentication
        } else {
            return false; // User not found or incorrect password
        }
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}


    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // New method to show the Admin Dashboard after successful authentication
    private void showAdminDashboard() {
        AdminDashboard adminDashboard = new AdminDashboard();
        Stage adminStage = new Stage();
        adminDashboard.start(adminStage);  // Start and show the Admin Dashboard
    }
    private void showCustomerDashboard() {
        CustomerDashboard customer = new CustomerDashboard();
        Stage customerStage = new Stage();
        customer.start(customerStage);  // Start and show the Admin Dashboard
    }
}
