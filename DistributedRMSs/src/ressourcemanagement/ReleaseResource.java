package ressourcemanagement;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.result.UpdateResult;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class ReleaseResource {

    private Conn conn; // MongoDB connection
    private TextField usernameField, regionField;

    public ReleaseResource() {
        conn = new Conn(); // Initialize the connection object
    }

    public void showReleaseDialog() {
        // Create the username and region input fields
        usernameField = new TextField();
        regionField = new TextField();

        // Create a button to submit the release request
        Button releaseButton = new Button("Release Resource");
        Button cancelButton = new Button("Cancel");

        // Action to handle release
        releaseButton.setOnAction(e -> handleReleaseAction());

        // Cancel button action
        cancelButton.setOnAction(e -> handleCancelAction());

        // Layout for the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Region:"), 0, 1);
        grid.add(regionField, 1, 1);

        // Buttons box
        HBox buttonBox = new HBox(10, releaseButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Main layout
        VBox vBox = new VBox(10, grid, buttonBox);
        vBox.setPadding(new Insets(20));

        // Show the dialog
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Release Resource");
        dialogStage.initModality(Modality.APPLICATION_MODAL); // Blocks interaction with other windows
        Scene scene = new Scene(vBox, 800, 550);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();  // Show dialog and wait for user action
    }

    private void handleReleaseAction() {
        String username = usernameField.getText();
        String region = regionField.getText();

        if (username.isEmpty() || region.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "All fields are required.");
            return;
        }

        // Fetch the resources requested by this user
        fetchUserRequestedResources(username, region);
    }

    private void handleCancelAction() {
        // Clear fields on cancel
        usernameField.clear();
        regionField.clear();
    }

public void fetchUserRequestedResources(String username, String region) {
    try {
        MongoDatabase database = conn.getDatabase();
        if (database == null) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Database connection is not initialized.");
            return;
        }

        // Access the "resource_requests" collection directly
        MongoCollection<Document> resourceRequestsCollection = database.getCollection("resource_requests");

        // Fetch the user-specific resources based on both username and region
        List<Document> userResources = resourceRequestsCollection.find(
                new Document("username", username).append("region", region)
        ).into(new ArrayList<>());

        // Check if no resources were found for the user
        if (userResources.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Resources", "No resources found for the user in the specified region.");
            return;
        }

        // Prepare a list to display resources with the correct size and unit
        List<String> formattedResources = new ArrayList<>();

        // Loop through each resource and format the output to include size and unit
        for (Document resource : userResources) {
            String resourceName = resource.getString("resource_name");
            Object sizeObj = resource.get("size");
            Object unitObj = resource.get("type");

            // Ensure both size and unit exist
            if (sizeObj != null && unitObj != null) {
                String size = sizeObj.toString();
                String unit = unitObj.toString();

                // Create a formatted string for each resource with name, size, and unit
                formattedResources.add(resourceName + " - " + size + " " + unit);
            }
        }

        // Show the resources in the dialog for release confirmation
        showReleaseConfirmationDialog(formattedResources, region, userResources);

    } catch (MongoException e) {
        showAlert(Alert.AlertType.ERROR, "Database Error", "MongoDB error: " + e.getMessage());
        e.printStackTrace();
    } catch (Exception e) {
        showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while fetching user resources: " + e.getMessage());
        e.printStackTrace();
    }
}


public void showReleaseConfirmationDialog(List<String> formattedResources, String region, List<Document> userResources) {
    // Create the list view to display the resources
    ListView<String> resourcesListView = new ListView<>();
    List<String> resourceNames = new ArrayList<>();

    // Adding resources with their name, size, and unit to the list
    for (Document resource : userResources) {
        String resourceName = resource.getString("resource_name");
        String sizeStr = resource.getString("size");
        String unit = resource.getString("type");

        // Parse size to double to handle conversion
        double size = Double.parseDouble(sizeStr);

        // Format the resource name, size, and unit
        resourceNames.add(resourceName + " - " + size + " " + unit);
    }

    resourcesListView.getItems().addAll(resourceNames);

    // Button to confirm release
    Button releaseButton = new Button("Release Selected Resource");
    Button cancelButton = new Button("Cancel");

    // Action to handle release confirmation
    releaseButton.setOnAction(e -> {
        String selectedResource = resourcesListView.getSelectionModel().getSelectedItem();
        if (selectedResource != null) {
            // Extract resource details from the selected resource string
            String[] parts = selectedResource.split(" - ");
            String resourceName = parts[0];
            double requestedSize = Double.parseDouble(parts[1].split(" ")[0]);
            String requestedUnit = parts[1].split(" ")[1];

            // Now we need to check if the selected unit is the same as the requested unit
            // Convert the requested size to the unit of the resource if necessary
            for (Document resource : userResources) {
                if (resource.getString("resource_name").equals(resourceName)) {
                    double availableSize = Double.parseDouble(resource.getString("size"));
                    String availableUnit = resource.getString("type");

                    // If the units are different, convert the requested size to match the available unit
                    if (!requestedUnit.equals(availableUnit)) {
                        requestedSize = convertSize(requestedSize, requestedUnit, availableUnit);
                    }

                    // Proceed with releasing the resource
                    handleReleaseResource(resource, requestedSize, availableUnit, region);
                    break;
                }
            }
        }
    });

    // Cancel button action
    cancelButton.setOnAction(e -> handleCancelAction());

    // Layout for the confirmation dialog
    VBox dialogLayout = new VBox(10, resourcesListView, releaseButton, cancelButton);
    dialogLayout.setPadding(new Insets(20));

    // Show the confirmation dialog
    Stage dialogStage = new Stage();
    dialogStage.setTitle("Confirm Release");
    dialogStage.initModality(Modality.APPLICATION_MODAL); // Blocks interaction with other windows
    Scene scene = new Scene(dialogLayout, 400, 250);
    dialogStage.setScene(scene);
    dialogStage.showAndWait();  // Show dialog and wait for user action
}

private double convertSize(double size, String fromUnit, String toUnit) {
    if (fromUnit.equals("GB") && toUnit.equals("TB")) {
        return size / 1024;  // GB to TB (divide by 1024)
    } else if (fromUnit.equals("TB") && toUnit.equals("GB")) {
        return size * 1024;  // TB to GB (multiply by 1024)
    }
    return size; // If units are the same, no conversion needed
}

public void handleReleaseResource(Document resource, double sizeToRelease, String unit, String region) {
    // Start releasing the resource from the region-specific collections first
    boolean releaseSuccessful = releaseResourceFromRegion(region, resource.getString("resource_name"), sizeToRelease, unit);

    // Release from other collections if the release was successful in the region
    if (releaseSuccessful) {
        releaseResourceFromOtherCollections(resource.getString("resource_name"), sizeToRelease, unit);
        
        // After successfully releasing the resource, update the status in resource_requests collection
        MongoDatabase database = conn.getDatabase();
        MongoCollection<Document> requestCollection = database.getCollection("resource_requests");
        requestCollection.updateOne(
                new Document("resource_name", resource.getString("resource_name")),
                new Document("$set", new Document("status", "released"))
        );

        showAlert(Alert.AlertType.INFORMATION, "Success", "Resource has been released and status updated.");
    } else {
        // If release fails, update the status as "not released"
        MongoDatabase database = conn.getDatabase();
        MongoCollection<Document> requestCollection = database.getCollection("resource_requests");
        requestCollection.updateOne(
                new Document("resource_name", resource.getString("resource_name")),
                new Document("$set", new Document("status", "not released"))
        );

        showAlert(Alert.AlertType.ERROR, "Error", "Failed to release resource.");
    }
}

private boolean releaseResourceFromRegion(String region, String resourceName, double sizeToRelease, String unit) {
    try {
        // Validate input
        if (resourceName == null || resourceName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Resource name cannot be empty.");
            return false;
        }
        if (sizeToRelease <= 0) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Size to release must be greater than 0.");
            return false;
        }

        MongoDatabase database = conn.getDatabase();
        if (database == null) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Database connection is not initialized.");
            return false;
        }

        // Convert the release size to a consistent unit (e.g., TB)
        double remainingToRelease = convertSize(sizeToRelease, unit, "TB");

        // Check in the specified region collection
        MongoCollection<Document> regionResourceCollection = database.getCollection(region + "_resources");
        if (regionResourceCollection == null) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Region resource collection does not exist.");
            return false;
        }

        Document resource = regionResourceCollection.find(new Document("resource_name", resourceName)).first();
        if (resource != null) {
            double allocatedSize = resource.getDouble("allocated_size");

            // If the region collection has enough resources to fulfill the request, release directly
            if (allocatedSize >= remainingToRelease) {
                double newAllocatedSize = allocatedSize - remainingToRelease;

                // Update region collection
                regionResourceCollection.updateOne(
                        new Document("resource_name", resourceName),
                        new Document("$set", new Document("allocated_size", newAllocatedSize))
                );

                updateResourceRequestStatus(resourceName, "released");
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                          "Released " + remainingToRelease + " TB of resource: " + resourceName + " from region: " + region);
                return true;
            }
        }

        // If not enough in the region collection, check the global collection
        MongoCollection<Document> globalResourcesCollection = database.getCollection("resources");
        if (globalResourcesCollection != null) {
            resource = globalResourcesCollection.find(new Document("resource_name", resourceName)).first();
            if (resource != null) {
                double allocatedSize = resource.getDouble("allocated_size");

                if (allocatedSize >= remainingToRelease) {
                    double newAllocatedSize = allocatedSize - remainingToRelease;

                    // Update global collection
                    globalResourcesCollection.updateOne(
                            new Document("resource_name", resourceName),
                            new Document("$set", new Document("allocated_size", newAllocatedSize))
                    );

                    updateResourceRequestStatus(resourceName, "released");
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                              "Released " + remainingToRelease + " TB of resource: " + resourceName + " from global resources.");
                    return true;
                }
            }
        }

        // If not enough in the region and global collections, check other region collections
        MongoIterable<String> collections = database.listCollectionNames();
        List<String> otherRegionCollections = new ArrayList<>();
        for (String collectionName : collections) {
            if (collectionName.endsWith("_resources") && !collectionName.equals(region + "_resources")) {
                otherRegionCollections.add(collectionName);
            }
        }

        for (String otherRegionCollection : otherRegionCollections) {
            MongoCollection<Document> collection = database.getCollection(otherRegionCollection);
            Document resourceInOtherRegion = collection.find(new Document("resource_name", resourceName)).first();

            if (resourceInOtherRegion != null) {
                double allocatedSize = resourceInOtherRegion.getDouble("allocated_size");

                if (allocatedSize >= remainingToRelease) {
                    double newAllocatedSize = allocatedSize - remainingToRelease;

                    // Update the other region collection
                    collection.updateOne(
                            new Document("resource_name", resourceName),
                            new Document("$set", new Document("allocated_size", newAllocatedSize))
                    );

                    updateResourceRequestStatus(resourceName, "released");
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                              "Released " + remainingToRelease + " TB of resource: " + resourceName + " from " + otherRegionCollection + ".");
                    return true;
                }
            }
        }

        // If none of the collections can fulfill the request, show an error
        showAlert(Alert.AlertType.ERROR, "Release Error", 
                  "Not enough resources available to release the requested size from any collection.");
        return false;

    } catch (Exception e) {
        showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while releasing the resource: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

private void updateResourceRequestStatus(String resourceName, String status) {
    try {
        MongoDatabase database = conn.getDatabase();
        MongoCollection<Document> resourceRequestsCollection = database.getCollection("resource_requests");

        // Update the resource request status
        UpdateResult requestResult = resourceRequestsCollection.updateOne(
                new Document("resource_name", resourceName).append("status", "allocated"),
                new Document("$set", new Document("status", status))
        );

        if (requestResult.getModifiedCount() == 0) {
            showAlert(Alert.AlertType.WARNING, "Update Warning", "Could not update resource request status.");
        }
    } catch (Exception e) {
        showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while updating resource request status: " + e.getMessage());
        e.printStackTrace();
    }
}

public void releaseResourceFromOtherCollections(String resourceName, double sizeToRelease, String unit) {
    try {
        MongoDatabase database = conn.getDatabase();
        if (database == null) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Database connection is not initialized.");
            return;
        }

        MongoIterable<String> collections = database.listCollectionNames();
        List<String> resourceCollections = new ArrayList<>();
        
        for (String collectionName : collections) {
            if (collectionName.endsWith("_resources")) {
                resourceCollections.add(collectionName);
            }
        }

        // Loop through each collection and release resources accordingly
        for (String resourceCollection : resourceCollections) {
            MongoCollection<Document> collection = database.getCollection(resourceCollection);
            Document resourceDoc = collection.find(new Document("resource_name", resourceName)).first();

            if (resourceDoc != null) {
                // Check if allocated_size exists and is a valid number
                if (!resourceDoc.containsKey("allocated_size")) {
                    System.err.println("Error: 'allocated_size' field is missing in resource document.");
                    continue;
                }

                Object allocatedSizeObj = resourceDoc.get("allocated_size");
                if (allocatedSizeObj == null) {
                    System.err.println("Error: 'allocated_size' is null.");
                    continue;
                }

                double allocatedSize = 0;
                try {
                    allocatedSize = Double.parseDouble(allocatedSizeObj.toString());
                } catch (NumberFormatException ex) {
                    System.err.println("Error: 'allocated_size' is not a valid number.");
                    continue;
                }

                // Convert the release size to a consistent unit (e.g., TB)
                double convertedSize = convertSize(sizeToRelease, unit, "TB");

                if (allocatedSize >= convertedSize) {
                    double newAllocatedSize = allocatedSize - convertedSize;
                    collection.updateOne(
                            new Document("resource_name", resourceName),
                            new Document("$set", new Document("allocated_size", newAllocatedSize))
                    );

                    showAlert(Alert.AlertType.INFORMATION, "Success", "Resource released from " + resourceCollection + ".");
                }
            } else {
                System.err.println("Error: Resource not found in collection: " + resourceCollection);
            }
        }
    } catch (Exception e) {
        showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while releasing resource from other collections: " + e.getMessage());
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
