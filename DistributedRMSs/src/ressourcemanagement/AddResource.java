package ressourcemanagement;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AddResource {

    private TextField resourceNameField, sizeField, typeField;
    private ComboBox<String> hourComboBox, minuteComboBox;
    private Conn conn; // Assuming the Conn class provides the MongoDB connection

    public AddResource() {
        // Initialize the connection object (make sure Conn class handles this)
        conn = new Conn();
    }

    public VBox createForm() {
        // Resource Name field
        resourceNameField = new TextField();
        sizeField = new TextField();
        typeField = new TextField();

        hourComboBox = new ComboBox<>();
        hourComboBox.getItems().addAll("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23");
        hourComboBox.setValue("00");

        minuteComboBox = new ComboBox<>();
        minuteComboBox.getItems().addAll("00", "15", "30", "45");
        minuteComboBox.setValue("00");

        // Form layout
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);

        // Add fields to the grid
        formGrid.add(new Label("Resource Name:"), 0, 0);
        formGrid.add(resourceNameField, 1, 0);
        formGrid.add(new Label("Size:"), 0, 1);
        formGrid.add(sizeField, 1, 1);
        formGrid.add(new Label("Type:"), 0, 2);
        formGrid.add(typeField, 1, 2);
        formGrid.add(new Label("Time:"), 0, 3);
        formGrid.add(new HBox(10, hourComboBox, new Label(":"), minuteComboBox), 1, 3);

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
    public void showAddResourceDialog() {
    Stage dialogStage = new Stage();
    dialogStage.setTitle("Add Resource");
    dialogStage.initModality(Modality.APPLICATION_MODAL); // Makes the dialog modal

    VBox form = createForm();

    // Create scene with the form and set it on the stage
    Scene scene = new Scene(form, 600, 450);
    dialogStage.setScene(scene);
    dialogStage.showAndWait(); // Show dialog and wait for it to be closed
}

 private void handleAddAction() {
    String resourceName = resourceNameField.getText();
    String size = sizeField.getText();
    String type = typeField.getText();
    String time = hourComboBox.getValue() + ":" + minuteComboBox.getValue();

    // Get logged-in username
    String loggedInUsername = UserSession.getInstance().getLoggedInUsername();

    // Validate input
    if (resourceName.isEmpty() || size.isEmpty() || type.isEmpty()) {
        showAlert(Alert.AlertType.ERROR, "Error", "All fields are required. Please fill in the form completely.");
        return;
    }

    if (loggedInUsername == null || loggedInUsername.isEmpty()) {
        showAlert(Alert.AlertType.ERROR, "Error", "User not logged in. Please log in to add a resource.");
        return;
    }

    // Fetch branch and add resource to the appropriate collection
    String branch = fetchBranchForUser(loggedInUsername);
    if (branch == null) {
        showAlert(Alert.AlertType.ERROR, "Error", "Failed to determine the user's branch. Cannot add resource.");
        return;
    }

    // Insert resource into dynamically named collection
    if (insertResourceData(resourceName, size, type, time, branch)) {
        showAlert(Alert.AlertType.INFORMATION, "Success", "Resource added successfully!\n" +
                "Resource: " + resourceName + "\nSize: " + size + "\nType: " + type + "\nTime: " + time);
    } else {
        showAlert(Alert.AlertType.ERROR, "Error", "Error adding resource. Please try again.");
    }
}
private String fetchBranchForUser(String username) {
    try {
        MongoDatabase database = conn.getDatabase();
        MongoCollection<Document> usersCollection = database.getCollection("users");

        // Query for the user document
        Document query = new Document("username", username);
        Document userDocument = usersCollection.find(query).first();

        if (userDocument != null && userDocument.containsKey("branch")) {
            return userDocument.getString("branch");
        } else {
            System.err.println("Branch not found for user: " + username);
            return null; // Branch not found
        }
    } catch (Exception e) {
        System.err.println("Error fetching branch for user: " + e.getMessage());
        e.printStackTrace();
        return null;
    }
}
private boolean insertResourceData(String resourceName, String size, String type, String time, String branch) {
    try {
        MongoDatabase database = conn.getDatabase();

        // General resources collection
        MongoCollection<Document> generalCollection = database.getCollection("resources");

        // Create or update the resource in the general collection
        updateOrInsertResource(generalCollection, resourceName, size, type, time);

        // If a branch is provided, store it in the branch-specific collection as well
        if (branch != null && !branch.isEmpty()) {
            String branchCollectionName = branch + "_resources";
            MongoCollection<Document> branchCollection = database.getCollection(branchCollectionName);

            // Create or update the resource in the branch-specific collection
            updateOrInsertResource(branchCollection, resourceName, size, type, time);
        }

        System.out.println("Resource added or updated successfully.");
        return true;
    } catch (Exception e) {
        System.err.println("Error adding or updating resource: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

private void updateOrInsertResource(MongoCollection<Document> collection, String resourceName, String size, String type, String time) {
    // Find the resource by name
    Document query = new Document("resource_name", resourceName);

    // Check if the resource already exists
    Document existingResource = collection.find(query).first();

    if (existingResource != null) {
        // Parse existing size and new size as integers and add them
        int existingSize = Integer.parseInt(existingResource.getString("size"));
        int newSize = Integer.parseInt(size);
        int updatedSize = existingSize + newSize;

        // Update the size and other fields
        Document updatedFields = new Document("size", String.valueOf(updatedSize))
                .append("type", type)
                .append("time", time);
        Document update = new Document("$set", updatedFields);

        collection.updateOne(query, update);
        System.out.println("Resource size updated (added) in collection: " + collection.getNamespace().getCollectionName());
    } else {
        // If the resource does not exist, insert a new document
        Document resourceDocument = new Document("resource_name", resourceName)
                .append("size", size)
                .append("type", type)
                .append("time", time);

        collection.insertOne(resourceDocument);
        System.out.println("Resource added to collection: " + collection.getNamespace().getCollectionName());
    }
}



    private void handleCancelAction() {
        resourceNameField.clear();
        sizeField.clear();
        typeField.clear();
        hourComboBox.setValue("00");
        minuteComboBox.setValue("00");
    }


    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
