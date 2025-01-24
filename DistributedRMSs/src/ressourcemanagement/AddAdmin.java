package ressourcemanagement;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.Random;

public class AddAdmin {

    private TextField nameTextField, fatherNameTextField, timeTextField, branchTextField;
    private Conn conn; // Assuming the Conn class provides the MongoDB connection

    public AddAdmin() {
        // Initialize the connection object
        conn = new Conn();
    }

    public VBox createForm() {
        // UI components
        nameTextField = new TextField();
        fatherNameTextField = new TextField();
        
        // Removed roleComboBox since the role is hardcoded as "admin"
        branchTextField = new TextField();
        timeTextField = new TextField();

        // Form layout
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);

        // Add fields to the grid
        formGrid.add(new Label("Name:"), 0, 0);
        formGrid.add(nameTextField, 1, 0);
        formGrid.add(new Label("Father Name:"), 0, 1);
        formGrid.add(fatherNameTextField, 1, 1);
        formGrid.add(new Label("Branch:"), 0, 2);
        formGrid.add(branchTextField, 1, 2);  // Changed to TextField
        formGrid.add(new Label("Time:"), 0, 3);
        formGrid.add(timeTextField, 1, 3);

        // Buttons
        Button addButton = new Button("Add");
        Button cancelButton = new Button("Cancel");

        addButton.setOnAction(e -> handleAddAction());
        cancelButton.setOnAction(e -> handleCancelAction());

        HBox buttonBox = new HBox(10, addButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox formBox = new VBox(10, formGrid, buttonBox);
        formBox.setPadding(new Insets(20));
        formBox.setStyle("-fx-background-color: lightgray;");

        return formBox;
    }

    public void showAddAdminDialog() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Add Admin");
        dialogStage.initModality(Modality.APPLICATION_MODAL); // Makes the dialog modal

        VBox form = createForm();

        // Create scene with the form and set it on the stage
        Scene scene = new Scene(form, 800, 500);
        dialogStage.setScene(scene);
        dialogStage.showAndWait(); // Show the dialog and wait for it to close
    }

    private void handleAddAction() {
        String name = nameTextField.getText();
        String fatherName = fatherNameTextField.getText();
        String branch = branchTextField.getText();  // Get branch from TextField
        String time = timeTextField.getText();

        // Assign the role directly as "admin"
        String role = "admin";

        // Generate username and password
        String username = generateUsername(name, role);
        String password = generatePassword();

        if (addAdmin(name, fatherName, role, branch, time, username, password)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Admin added successfully! Username: " + username);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Error adding admin. Please try again.");
        }
    }

    private String generateUsername(String name, String role) {
    // Split the name into first and last name parts
    String[] nameParts = name.split(" ");
    String firstName = nameParts[0].toLowerCase();
    String lastName = nameParts.length > 1 ? nameParts[1].toLowerCase() : "";  // Handle case where there's no last name

    // Take the first 3 letters of the first and last names, or fewer if the name is shorter
    String firstNamePart = firstName.length() >= 3 ? firstName.substring(0, 3) : firstName;
    String lastNamePart = lastName.length() >= 3 ? lastName.substring(0, 3) : lastName;

    // Role prefix (first 3 letters of the role, default to "usr" if role is null)
    String rolePrefix = role != null ? role.substring(0, 3).toLowerCase() : "usr";

    // Combine the parts to form the username
    return firstNamePart + lastNamePart + rolePrefix + new Random().nextInt(100); // Add a random number to ensure uniqueness
}


    private String generatePassword() {
        // Generate a random password (e.g., length of 8-12 with letters and digits)
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        int passwordLength = 10 + random.nextInt(3); // Password length between 10 and 12 characters

        for (int i = 0; i < passwordLength; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }

        return password.toString();
    }

    private boolean addAdmin(String name, String fatherName, String role, String branch, String time, String username, String password) {
        try {
            MongoDatabase database = conn.getDatabase(); // Assuming Conn class provides the database
            MongoCollection<Document> adminCollection = database.getCollection("users");

            // Generate ObjectId for admin
            String adminId = new Random().nextInt(1000000) + ""; // Simple example, you may want to handle ObjectId more robustly

            // Create new admin document with username and password
            Document adminDocument = new Document("admin_id", adminId)
                    .append("name", name)
                    .append("father_name", fatherName)
                    .append("role", role)
                    .append("branch", branch)
                    .append("time", time)
                    .append("username", username)
                    .append("password", password); // Store the password (in a hashed form in production)

            // Add admin document to collection
            adminCollection.insertOne(adminDocument);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void handleCancelAction() {
        nameTextField.clear();
        fatherNameTextField.clear();
        branchTextField.clear();  // Clear branch TextField
        timeTextField.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
