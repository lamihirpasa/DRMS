package ressourcemanagement;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.Duration;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Notification {

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Conn conn; // MongoDB connection object
    private String username; // Username for filtering notifications

    public Notification(String username) {
        this.conn = new Conn(); // Initialize the MongoDB connection
        this.username = username; // Set the username for filtering
        startNotificationService();
    }

    /**
     * Starts the notification service to periodically fetch and check notifications.
     */
    private void startNotificationService() {
        scheduler.scheduleAtFixedRate(() -> {
            fetchAndCheckNotifications(username); // Fetch notifications from MongoDB and check reminders
        }, 0, 1, TimeUnit.MINUTES);
    }

    /**
     * Fetch resource requests for the specified username from MongoDB
     * and check for reminders or overdue notifications.
     */
    private void fetchAndCheckNotifications(String username) {
        try {
            MongoDatabase database = conn.getDatabase();
            MongoCollection<Document> collection = database.getCollection("resource_requests");

            // Query to filter by username
            Document query = new Document("username", username);

            MongoCursor<Document> cursor = collection.find(query).iterator();

            while (cursor.hasNext()) {
                Document requestDoc = cursor.next();

                String resourceName = requestDoc.getString("resource_name");
                String size = requestDoc.getString("size");
                String type = requestDoc.getString("type");
                String requestTimeStr = requestDoc.getString("time_requested");
                String releaseTimeStr = requestDoc.getString("time_release");

                // Validate fields
                if (resourceName == null || requestTimeStr == null || releaseTimeStr == null) {
                    System.out.println("Skipping invalid document: Missing required fields");
                    continue;
                }

                try {
                    // Parse the times from the database
                    LocalDateTime requestTime = LocalDateTime.parse(requestTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    LocalDateTime releaseTime = LocalDateTime.parse(releaseTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    LocalDateTime now = LocalDateTime.now();

                    // Check if the resource is overdue
                    if (now.isAfter(releaseTime)) {
                        showNotificationDialog("Resource Release Reminder",
                                "The resource '" + resourceName + "' is overdue for release.");
                    } else {
                        // Calculate minutes left before the release
                        long minutesLeft = java.time.Duration.between(now, releaseTime).toMinutes();
                        if (minutesLeft <= 5 && minutesLeft > 0) {
                            showNotificationDialog("Resource Release Reminder",
                                    "You have " + minutesLeft + " minutes left to release the resource: " + resourceName);
                        }
                    }
                } catch (DateTimeParseException e) {
                    System.out.println("Skipping document due to invalid date format: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showNotificationDialog("Error", "Failed to fetch resource requests for user '" + username + "': " + e.getMessage());
        }
    }

    public void viewNotifications() {
    try {
        MongoDatabase database = conn.getDatabase();
        MongoCollection<Document> collection = database.getCollection("resource_requests");

        // Query to filter by username
        Document query = new Document("username", username);
        MongoCursor<Document> cursor = collection.find(query).iterator();

        // Table setup
        TableView<ResourceRequest> table = new TableView<>();
        table.setPrefWidth(600);  // Set the preferred width of the table to adjust dynamically
        table.setMinWidth(400);   // Set minimum width for resizing

        TableColumn<ResourceRequest, String> resourceNameColumn = new TableColumn<>("Resource");
        resourceNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getResourceName()));

        TableColumn<ResourceRequest, String> sizeColumn = new TableColumn<>("Size");
        sizeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSize()));

        TableColumn<ResourceRequest, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));

        TableColumn<ResourceRequest, String> requestTimeColumn = new TableColumn<>("Requested At");
        requestTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRequestTime()));

        TableColumn<ResourceRequest, String> releaseTimeColumn = new TableColumn<>("Release Time");
        releaseTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReleaseTime()));

        TableColumn<ResourceRequest, String> countdownColumn = new TableColumn<>("Time Remaining");
        countdownColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTimeRemaining()));

        // Add columns to the table
        table.getColumns().addAll(resourceNameColumn, sizeColumn, typeColumn, requestTimeColumn, releaseTimeColumn, countdownColumn);

        // VBox to hold both table and countdown
        VBox mainVBox = new VBox();
        mainVBox.setSpacing(10);  // Add some spacing between components

        // HBox to hold the table and countdown
        HBox hbox = new HBox();
        hbox.setSpacing(20);

        // Label for countdown
        Label countdownLabel = new Label();
        countdownLabel.setStyle("-fx-font-size: 16px;");
        hbox.getChildren().add(countdownLabel);

        // Populate the table with data
        while (cursor.hasNext()) {
            Document requestDoc = cursor.next();

            String resourceName = requestDoc.getString("resource_name");
            String size = requestDoc.getString("size");
            String type = requestDoc.getString("type");
            String requestTimeStr = requestDoc.getString("request_date");
            String releaseTimeStr = requestDoc.getString("release_time");

            LocalDateTime releaseTime = LocalDateTime.parse(releaseTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            ResourceRequest request = new ResourceRequest(resourceName, size, type, requestTimeStr, releaseTimeStr);

            table.getItems().add(request);

            // Start countdown for each request
            startCountdown(request, countdownLabel); // Pass countdownLabel to update it dynamically
        }

        // Add the table and countdown hbox to the main VBox
        mainVBox.getChildren().addAll(table, hbox);

        // Create a scene and show the table and countdown in a new window
        Scene scene = new Scene(mainVBox);
        Stage stage = new Stage();
        stage.setTitle("Resource Notifications for User: " + username);
        
        // Set a preferred size for the stage (Resizable)
        stage.setWidth(800);  // Set width to 800 pixels
        stage.setHeight(600); // Set height to 600 pixels
        stage.setResizable(true); // Allow resizing of the window
        
        // Set the scene and show the stage
        stage.setScene(scene);
        stage.show();
    } catch (Exception e) {
        e.printStackTrace();
        showNotificationDialog("Error", "Failed to retrieve notifications for user '" + username + "': " + e.getMessage());
    }
}

private void startCountdown(ResourceRequest request, Label countdownLabel) {
    // Track the last notification time to prevent frequent popups
    final long[] lastNotificationTime = {0}; // Store as an array to allow modification inside lambda

    // Create a timeline for this resource
    Timeline timeline = new Timeline(
        new KeyFrame(Duration.seconds(1), e -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime releaseTime = request.getReleaseTimeAsLocalDateTime();
            long minutesLeft = java.time.Duration.between(now, releaseTime).toMinutes();

            if (minutesLeft > 0) {
                request.setTimeRemaining(minutesLeft + " minutes");

                // Check if there are 10 minutes left
                if (minutesLeft == 10) {
                    showNotificationDialog("Reminder", "You have 10 minutes left to release the resource: " + request.getResourceName());
                }

                // Check if there are 5 minutes left
                if (minutesLeft == 5) {
                    showNotificationDialog("Reminder", "You have 5 minutes left to release the resource: " + request.getResourceName());
                }
            } else {
                // Set the time remaining to "Time is expired" when the time is up
                request.setTimeRemaining("Time is expired");

                // Update the MongoDB collection to reflect the expiration
                updateTimeRemainingInDatabase(request.getResourceName(), "Time is expired");

                // Check if 5 minutes have passed since the last notification
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - lastNotificationTime[0] >= 5 * 60 * 1000) { // 5 minutes in milliseconds
                    // Show a reminder when the countdown is complete
                    showNotificationDialog("Time's Up!", "Your time is completed. Please release the resource: " + request.getResourceName());

                    // Update the last notification time
                    lastNotificationTime[0] = currentTimeMillis;
                }
            }

            // Update the label in the main interface
            countdownLabel.setText("Time remaining for " + request.getResourceName() + ": " + request.getTimeRemaining());
        })
    );

    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();

    // Stop the timeline when the resource is released
    request.setReleaseAction(() -> {
        timeline.stop(); // Stop the countdown when the resource is released
        request.setTimeRemaining("Released"); // Update the time remaining to indicate the release
        updateTimeRemainingInDatabase(request.getResourceName(), "Released");
        countdownLabel.setText("Resource " + request.getResourceName() + " has been released.");
    });
}

/**
 * Marks the resource as released.
 */
public void releaseResource(ResourceRequest request) {
    // Trigger the release action associated with this resource
    if (request.getReleaseAction() != null) {
        request.getReleaseAction().run();
    } else {
        System.out.println("No release action defined for resource: " + request.getResourceName());
    }
}




private void updateTimeRemainingInDatabase(String resourceName, String newTimeRemaining) {
    try {
        MongoDatabase database = conn.getDatabase();
        MongoCollection<Document> collection = database.getCollection("resource_requests");

        // Query to find the document by resource name
        Document query = new Document("resource_name", resourceName);

        // Create an update document to modify the timeRemaining field
        Document update = new Document("$set", new Document("time_remaining", newTimeRemaining));

        // Update the document in the collection
        collection.updateOne(query, update);

    } catch (Exception e) {
        e.printStackTrace();
        showNotificationDialog("Error", "Failed to update the time remaining for resource: " + resourceName);
    }
}


    public void showNotificationDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Gracefully shuts down the notification service.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

   public class ResourceRequest {
    private String resourceName;
    private String size;
    private String type;
    private String requestTime;
    private String releaseTime;
    private String timeRemaining;
    private Runnable releaseAction; // Action to execute on release

    // Constructor
    public ResourceRequest(String resourceName, String size, String type, String requestTime, String releaseTime) {
        this.resourceName = resourceName;
        this.size = size;
        this.type = type;
        this.requestTime = requestTime;
        this.releaseTime = releaseTime;
        this.timeRemaining = "Calculating...";
    }

    // Getters and setters for fields...

    public Runnable getReleaseAction() {
        return releaseAction;
    }

    public void setReleaseAction(Runnable releaseAction) {
        this.releaseAction = releaseAction;
    }


    public String getResourceName() {
        return resourceName;
    }

    public String getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public String getReleaseTime() {
        return releaseTime;
    }

    // Return releaseTime as LocalDateTime
    public LocalDateTime getReleaseTimeAsLocalDateTime() {
        return LocalDateTime.parse(releaseTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(String timeRemaining) {
        this.timeRemaining = timeRemaining;
    }
}

}
