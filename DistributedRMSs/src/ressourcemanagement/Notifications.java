package ressourcemanagement;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.bson.Document;

public class Notifications {
    private Conn conn; // MongoDB connection class

    public Notifications() {
        this.conn = new Conn();
    }

    /**
     * Fetch and display notifications from the feedback collection when the user clicks "View Notifications."
     */
    public void viewNotifications() {
        try {
            // Connect to MongoDB and fetch data
            MongoDatabase database = conn.getDatabase();
            TableView<NotificationItem> tableView = new TableView<>();
            tableView.setPrefWidth(800);

            // Define table columns
            TableColumn<NotificationItem, String> typeColumn = new TableColumn<>("full_name");
            typeColumn.setCellValueFactory(cell -> cell.getValue().typeProperty());

            TableColumn<NotificationItem, String> messageColumn = new TableColumn<>("email");
            messageColumn.setCellValueFactory(cell -> cell.getValue().messageProperty());
            
            TableColumn<NotificationItem, String> commentColumn = new TableColumn<>("comment");
            commentColumn.setCellValueFactory(cell -> cell.getValue().messageProperty());
            
            TableColumn<NotificationItem, String> requestColumn = new TableColumn<>("request");
            requestColumn.setCellValueFactory(cell -> cell.getValue().messageProperty());

            TableColumn<NotificationItem, String> timestampColumn = new TableColumn<>("Timestamp");
            timestampColumn.setCellValueFactory(cell -> cell.getValue().timestampProperty());

            tableView.getColumns().addAll(typeColumn, messageColumn,commentColumn, requestColumn, timestampColumn);

            // Fetch feedback from the "feedback" collection
            MongoCollection<Document> feedbackCollection = database.getCollection("feedback");
            Document feedbackQuery = new Document();

            try (MongoCursor<Document> feedbackCursor = feedbackCollection.find(feedbackQuery).iterator()) {
                while (feedbackCursor.hasNext()) {
                    Document doc = feedbackCursor.next();
                    String feedbackMessage = doc.getString("feedback_message");
                    String timestamp = doc.getString("timestamp");

                    if (feedbackMessage != null && timestamp != null) {
                        tableView.getItems().add(new NotificationItem("User Feedback", feedbackMessage, timestamp));
                    }
                }
            }

            // Display the table in a new window
            if (tableView.getItems().isEmpty()) {
                showError("No Feedback", "You have no feedback at this time.");
                return;
            }

            VBox vbox = new VBox(tableView);
            vbox.setSpacing(10);
            Scene scene = new Scene(vbox);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("User Feedback");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to fetch feedback. Please try again.");
        }
    }

    /**
     * Displays an error dialog.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

class NotificationItem {
    private final StringProperty type;
    private final StringProperty message;
    private final StringProperty timestamp;

    public NotificationItem(String type, String message, String timestamp) {
        this.type = new SimpleStringProperty(type);
        this.message = new SimpleStringProperty(message);
        this.timestamp = new SimpleStringProperty(timestamp);
    }

    public StringProperty typeProperty() {
        return type;
    }

    public StringProperty messageProperty() {
        return message;
    }

    public StringProperty timestampProperty() {
        return timestamp;
    }
}
