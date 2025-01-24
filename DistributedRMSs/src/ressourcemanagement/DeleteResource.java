package ressourcemanagement;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.Document;

import java.util.List;
import java.util.ArrayList;

public class DeleteResource {
    private Conn conn; // Assuming the Conn class provides the MongoDB connection

    public DeleteResource() {
        conn = new Conn();
    }

    public void showDeleteResourceDialog() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Delete Resource");
        dialogStage.initModality(Modality.APPLICATION_MODAL); // Makes the dialog modal

        // Create UI components
        ComboBox<String> resourceComboBox = new ComboBox<>();
        ComboBox<String> collectionComboBox = new ComboBox<>();
        
        // Fetch and populate the resource combo box
        resourceComboBox.getItems().addAll(fetchResources());

        // Fetch and populate the collection combo box
        collectionComboBox.getItems().addAll(fetchCollections());

        Button deleteButton = new Button("Delete");
        Button cancelButton = new Button("Cancel");

        deleteButton.setOnAction(e -> {
            String selectedResource = resourceComboBox.getValue();
            String selectedCollection = collectionComboBox.getValue();
            
            if (selectedResource != null && selectedCollection != null) {
                // Check if the resource exists in the selected collection
                if (isResourceInCollection(selectedResource, selectedCollection)) {
                    Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete the resource: " + selectedResource + " from collection: " + selectedCollection + "?", ButtonType.YES, ButtonType.NO);
                    confirmationAlert.setTitle("Confirm Deletion");
                    confirmationAlert.setHeaderText(null);

                    // Handle confirmation
                    confirmationAlert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            deleteResource(selectedResource, selectedCollection);
                            showAlert(Alert.AlertType.INFORMATION, "Success", "Resource '" + selectedResource + "' deleted successfully from collection: " + selectedCollection);
                            dialogStage.close();
                        }
                    });
                } else {
                    showAlert(Alert.AlertType.WARNING, "Warning", "The selected resource does not exist in the selected collection.");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "Please select both a resource and a collection to delete.");
            }
        });

        cancelButton.setOnAction(e -> dialogStage.close());

        // Layout for resource and collection selection (horizontal alignment)
        HBox resourceHBox = new HBox(10, new Label("Select Resource:"), resourceComboBox);
        HBox collectionHBox = new HBox(10, new Label("Select Collection:"), collectionComboBox);
        HBox buttonHBox = new HBox(10, deleteButton, cancelButton);

        // Add padding and set the layout
        resourceHBox.setPadding(new Insets(10));
        collectionHBox.setPadding(new Insets(10));
        buttonHBox.setPadding(new Insets(10));

        // Overall layout (VBox for vertical stacking)
        VBox vbox = new VBox(10, resourceHBox, collectionHBox, buttonHBox);
        vbox.setPadding(new Insets(20));
        Scene scene = new Scene(vbox, 600, 400);
        dialogStage.setScene(scene);
        dialogStage.showAndWait(); // Show the dialog and wait for it to close
    }

    private List<String> fetchResources() {
        List<String> resources = new ArrayList<>();
        try {
            MongoDatabase database = conn.getDatabase();
            // Loop through all collections to find resource names
            List<String> collectionNames = new ArrayList<>();
            for (String collectionName : database.listCollectionNames()) {
                collectionNames.add(collectionName);
            }
            for (String collectionName : collectionNames) {
                MongoCollection<Document> collection = database.getCollection(collectionName);
                for (Document doc : collection.find()) {
                    String resourceName = doc.getString("resource_name");
                    if (resourceName != null && !resources.contains(resourceName)) {
                        resources.add(resourceName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resources;
    }

    private List<String> fetchCollections() {
        List<String> collections = new ArrayList<>();
        try {
            MongoDatabase database = conn.getDatabase();
            // Get all collection names
            for (String collectionName : database.listCollectionNames()) {
                collections.add(collectionName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return collections;
    }

    private boolean isResourceInCollection(String resourceName, String collectionName) {
        try {
            MongoDatabase database = conn.getDatabase();
            MongoCollection<Document> collection = database.getCollection(collectionName);
            // Check if the resource exists in the collection
            long count = collection.countDocuments(new Document("resource_name", resourceName));
            return count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void deleteResource(String resourceName, String collectionName) {
        try {
            MongoDatabase database = conn.getDatabase();
            MongoCollection<Document> collection = database.getCollection(collectionName);
            collection.deleteOne(new Document("resource_name", resourceName));
        } catch (Exception e) {
            e.printStackTrace();
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
