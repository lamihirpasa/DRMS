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
import com.mongodb.client.MongoIterable;
import com.mongodb.client.result.UpdateResult;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class RequestResource {

     private TextField usernameField, resourceNameField, sizeField, typeField, fullNameField, regionField;
    private ComboBox<String> hourComboBox, minuteComboBox, resourceNameComboBox;
    private Conn conn; // MongoDB connection

    public RequestResource() {
        conn = new Conn(); // Initialize the connection object
    }

    public VBox createRequestForm() {
        // Initialize text fields
        usernameField = new TextField();  // Username input field
        resourceNameField = new TextField();
        sizeField = new TextField();
        typeField = new TextField();
        fullNameField = new TextField();  // Full Name input field
        regionField = new TextField();    // Region input field

        hourComboBox = new ComboBox<>();
        hourComboBox.getItems().addAll("00", "01", "02", "03", "04", "05",
                                         "06", "07", "08", "09", "10", "11",
                                         "12", "13", "14", "15", "16", "17",
                                         "18", "19", "20", "21", "22", "23");
        hourComboBox.setValue("00");

        minuteComboBox = new ComboBox<>();
        minuteComboBox.getItems().addAll("00", "15", "30", "45");
        minuteComboBox.setValue("00");
        
      resourceNameComboBox = new ComboBox<>();
    resourceNameComboBox.setPromptText("Select Resource");
    populateResourceNames();

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.add(new Label("Username:"), 0, 0); // Username label
        formGrid.add(usernameField, 1, 0); // Username field
        formGrid.add(new Label("Full Name:"), 0, 1); // Full Name label
        formGrid.add(fullNameField, 1, 1); // Full Name field
        formGrid.add(new Label("Region:"), 0, 2); // Region label
        formGrid.add(regionField, 1, 2); // Region field
        formGrid.add(new Label("Resource Name:"), 0, 3);
        formGrid.add(resourceNameComboBox, 1, 3);
        formGrid.add(new Label("Size:"), 0, 4);
        formGrid.add(sizeField, 1, 4);
        formGrid.add(new Label("Type:"), 0, 5);
        formGrid.add(typeField, 1, 5);
        formGrid.add(new Label("Time:"), 0, 6);
        formGrid.add(new HBox(10, hourComboBox, new Label(":"), minuteComboBox), 1, 6);

        Button sendButton = new Button("Send Request");
        Button cancelButton = new Button("Cancel");

        sendButton.setOnAction(e -> handleSendRequestAction());
        cancelButton.setOnAction(e -> handleCancelAction());

        HBox buttonBox = new HBox(10, sendButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox formBox = new VBox(10, formGrid, buttonBox);
        formBox.setPadding(new Insets(20));
        formBox.setStyle("-fx-background-color: lightgray;");

        return formBox;
    }


    public void showRequestDialog() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Request a Resource");
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        VBox form = createRequestForm();
        Scene scene = new Scene(form, 800, 550); // Increased height to accommodate new fields
        dialogStage.setScene(scene);
        dialogStage.showAndWait(); 
    }
private void populateResourceNames() {
    try {
        MongoDatabase database = conn.getDatabase();
        MongoCollection<Document> resourceCollection = database.getCollection("resources");

        // Clear any existing items in the ComboBox
        resourceNameComboBox.getItems().clear();

        // Query the database to fetch resource names
        for (Document document : resourceCollection.find()) {
            String resourceName = document.getString("resource_name");
            if (resourceName != null && !resourceName.isEmpty()) {
                resourceNameComboBox.getItems().add(resourceName);
            }
        }

        // Check if no resources were found
        if (resourceNameComboBox.getItems().isEmpty()) {
            resourceNameComboBox.setPromptText("No Resources Available");
        }
    } catch (Exception e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Error", "Failed to load resource names.");
    }
}
   private void handleSendRequestAction() {
    String username = usernameField.getText();
    String fullName = fullNameField.getText();
    String region = regionField.getText();
    String resourceName = resourceNameComboBox.getValue(); // Use ComboBox value
    String size = sizeField.getText();
    String type = typeField.getText();
    String time = hourComboBox.getValue() + ":" + minuteComboBox.getValue();

    // Check user role
    String role = checkUserRole(username);

    // If the role is null or undefined, assign 'user' as default
    if (role == null) {
        role = "user";
    }

    // Request resource and check if it was successful
    if (requestResource(resourceName, size, type, time, username, role, fullName, region)) {
        showAlert(Alert.AlertType.INFORMATION,
                  "Resource Requested",
                  "Resource Request Sent!\nResource: " + resourceName + 
                  "\nSize: " + size + "\nType: " + type + "\nTime: " + time);
        navigateToDashboard(role, username); // Pass username to dashboard
    } else {
        // Alert if there was an issue with the resource request
        showAlert(Alert.AlertType.ERROR, "Request Error", "Failed to request resource.");
    }
}
   
private double convertSizeToResourceType(double size, String type, String targetType) {
    double convertedSize = -1;
    
    // Handle invalid type upfront
    if (type == null || targetType == null) {
        return -1;  // Invalid type
    }

    // Normalize input types to uppercase for consistency
    type = type.toUpperCase();
    targetType = targetType.toUpperCase();

    switch (type) {
        case "KB":
            if (targetType.equals("TB")) {
                convertedSize = size / (1024 * 1024 * 1024); // Convert KB to TB
            }
            break;
        case "MB":
            if (targetType.equals("TB")) {
                convertedSize = size / (1024 * 1024); // Convert MB to TB
            }
            break;
        case "GB":
            if (targetType.equals("TB")) {
                convertedSize = size / 1024; // Convert GB to TB
            }
            break;
        case "TB":
            if (targetType.equals("TB")) {
                convertedSize = size; // No conversion needed if the type is already TB
            }
            break;
        default:
            System.out.println("Invalid resource type: " + type);
            return -1; // Invalid type
    }
    
    // If we still have an invalid result after the conversion, return -1
    return convertedSize >= 0 ? convertedSize : -1;
}

private boolean requestResource(String resourceName, String size, String type, String time, 
                                String username, String role, String fullName, String region) {
    try {
        // Validate inputs
        if (username == null || username.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Username cannot be empty.");
            return false;
        }

        if (resourceName == null || resourceName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Resource name cannot be empty.");
            return false;
        }

        // Validate size input
        double requestedSize;
        try {
            requestedSize = Double.parseDouble(size);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid size value. Please enter a valid number.");
            return false;
        }

        if (requestedSize <= 0) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Size must be greater than 0.");
            return false;
        }

        // Validate time input
        String[] timeParts = time.split(":");
        if (timeParts.length != 2) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid time format. Please use HH:mm.");
            return false;
        }

        int requestedHour, requestedMinute;
        try {
            requestedHour = Integer.parseInt(timeParts[0]);
            requestedMinute = Integer.parseInt(timeParts[1]);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid time values. Please enter valid hours and minutes.");
            return false;
        }

        if (requestedHour < 0 || requestedHour > 23 || requestedMinute < 0 || requestedMinute > 59) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Time values are out of range. Hours should be 0-23 and minutes 0-59.");
            return false;
        }

        MongoDatabase database = conn.getDatabase();
        if (database == null) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Database connection is not initialized.");
            return false;
        }

        MongoCollection<Document> collection = database.getCollection("resource_requests");
        MongoCollection<Document> resourceCollection = database.getCollection("resources");

        if (collection == null || resourceCollection == null) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Collection does not exist.");
            return false;
        }

        // Get current time and calculate release time
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime releaseTime = currentTime.plusHours(requestedHour).plusMinutes(requestedMinute);
        String formattedReleaseTime = releaseTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Create the resource request document
        Document requestDoc = new Document("username", username)
                .append("full_name", fullName) // Store full name
                .append("region", region) // Store region
                .append("resource_name", resourceName)
                .append("size", size)
                .append("type", type)
                .append("time", time)
                .append("role", role)
                .append("release_time", formattedReleaseTime)
                .append("request_date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("status", "allocated");  // Add status field

        // Insert the request document
        collection.insertOne(requestDoc);
        System.out.println("Resource request inserted into database.");

        // First, check the region-specific resources collection
        MongoCollection<Document> regionResourceCollection = database.getCollection(region + "_resources");
        boolean resourceFulfilled = checkAndUpdateResource(regionResourceCollection, resourceName, requestedSize, type);

        if (!resourceFulfilled) {
            // If region resources are not sufficient, check the main resources collection
            resourceFulfilled = checkAndUpdateResource(resourceCollection, resourceName, requestedSize, type);
        }

        if (!resourceFulfilled) {
            // If neither region-specific nor main resources fulfill, check other region collections dynamically
            MongoIterable<String> collectionsIterable = database.listCollectionNames();
            List<String> collections = new ArrayList<>();
            for (String collectionName : collectionsIterable) {
                collections.add(collectionName);
            }

            List<String> resourceCollections = new ArrayList<>();
            for (String collectionName : collections) {
                if (collectionName.endsWith("_resources") && !collectionName.equals(region + "_resources")) {
                    resourceCollections.add(collectionName);
                }
            }

            // Check other region resources
            for (String otherRegion : resourceCollections) {
                MongoCollection<Document> otherRegionCollection = database.getCollection(otherRegion);
                resourceFulfilled = checkAndUpdateResource(otherRegionCollection, resourceName, requestedSize, type);
                if (resourceFulfilled) {
                    break;
                }
            }
        }

        if (!resourceFulfilled) {
            showAlert(Alert.AlertType.ERROR, "Resource Allocation Error", "Resource request could not be fulfilled.");
            return false;
        }

        // Update the resource allocation status to "allocated" in the request
        collection.updateOne(new Document("username", username)
            .append("resource_name", resourceName),
            new Document("$set", new Document("status", "allocated")));

        return true;

    } catch (Exception e) {
        showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}


private boolean checkAndUpdateResource(MongoCollection<Document> collection, String resourceName, double requestedSize, String type) {
    Document resourceDoc = collection.find(new Document("resource_name", resourceName)).first();

    if (resourceDoc != null) {
        // Get the available size and allocated size from the document
        double availableSize = getResourceSize(resourceDoc, "size");
        double allocatedSize = getResourceSize(resourceDoc, "allocated_size");

        // Convert requested size to TB
        double requestedSizeInTB = convertSizeToResourceType(requestedSize, type, "TB");
        if (requestedSizeInTB == -1) {
            showAlert(Alert.AlertType.ERROR, "Size Conversion Error", "Invalid size conversion.");
            return false;
        }

        double availableSizeInTB = convertSizeToResourceType(availableSize, resourceDoc.getString("type"), "TB");

        // Check if there is enough available space
        if (allocatedSize + requestedSizeInTB <= availableSizeInTB) {
            // Update the allocated size
            double newAllocatedSize = allocatedSize + requestedSizeInTB;
            collection.updateOne(new Document("resource_name", resourceName),
                    new Document("$set", new Document("allocated_size", newAllocatedSize)));
            System.out.println("Resource allocated size updated.");
            return true;
        }
    }
    return false;
}

private double getResourceSize(Document resourceDoc, String field) {
    Object sizeObj = resourceDoc.get(field);
    if (sizeObj instanceof Double) {
        return (Double) sizeObj;
    } else if (sizeObj instanceof String) {
        try {
            return Double.parseDouble((String) sizeObj);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Data", field + " is not a valid number.");
        }
    }
    return 0.0;
}


    private String checkUserRole(String username) {
        try {
            MongoDatabase database = conn.getDatabase();
            MongoCollection<Document> userCollection = database.getCollection("users");
            Document userDoc = userCollection.find(new Document("username", username)).first();

            if (userDoc != null) {
                return userDoc.getString("role");
            } else {
                System.out.println("User not found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to retrieve user role: " + e.getMessage());
        }
        return null; 
    }

    private void navigateToDashboard(String role, String username) {
        switch (role.toLowerCase()) {
            case "admin":
                System.out.println("Navigating to Admin Dashboard");
                // Implement navigation logic to Admin Dashboard
                break;
            case "manager":
                System.out.println("Navigating to Manager Dashboard");
                // Implement navigation logic to Manager Dashboard
                break;
            case "customer":
                CustomerDashboard customerDashboard = new CustomerDashboard();
                Stage customerDashboardStage = new Stage();
                try {
                    customerDashboard.start(customerDashboardStage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "user":
                // Pass the username to the UserDashboard constructor
                UserDashboard userDashboard = new UserDashboard(username);
                Stage userDashboardStage = new Stage();
                try {
                    userDashboard.start(userDashboardStage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("Unrecognized role. Navigating to User Dashboard.");
                break;
        }
    }

    private void handleCancelAction() {
        usernameField.clear();
        resourceNameField.clear();
        sizeField.clear();
        typeField.clear();
        hourComboBox.getSelectionModel().clearSelection();
        minuteComboBox.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
}
