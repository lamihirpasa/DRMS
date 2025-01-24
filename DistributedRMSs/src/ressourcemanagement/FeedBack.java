package ressourcemanagement;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FeedBack {

    private final Conn conn; // MongoDB connection

    public FeedBack() {
        conn = new Conn(); // Initialize MongoDB connection
    }

    public void showFeedBackDialog() {
        // Create the dialog stage
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Feedback Form");
        dialogStage.initModality(Modality.APPLICATION_MODAL); // Makes the dialog modal

        // Create the form components
        Label fullNameLabel = new Label("Full Name:");
        TextField fullNameField = new TextField();

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();

        Label commentLabel = new Label("Comment:");
        TextArea commentArea = new TextArea();

        Label requestLabel = new Label("Request:");
        TextField requestField = new TextField(); // Changed to TextField

        Button submitButton = new Button("Submit");
        Button cancelButton = new Button("Cancel");

        // Arrange each label and field in an HBox (horizontal layout)
        HBox fullNameBox = new HBox(10, fullNameLabel, fullNameField);
        HBox emailBox = new HBox(10, emailLabel, emailField);
        HBox commentBox = new HBox(10, commentLabel, commentArea);
        HBox requestBox = new HBox(10, requestLabel, requestField);

        // Set padding for the HBox
        fullNameBox.setPadding(new Insets(5));
        emailBox.setPadding(new Insets(5));
        commentBox.setPadding(new Insets(5));
        requestBox.setPadding(new Insets(5));

        // Submit button action
        submitButton.setOnAction(e -> {
            String fullName = fullNameField.getText();
            String email = emailField.getText();
            String comment = commentArea.getText();
            String request = requestField.getText(); // Changed to text field input

            if (fullName.isEmpty() || email.isEmpty() || comment.isEmpty() || request.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Form Incomplete", "Please fill in all fields.");
            } else {
                // Save the feedback to MongoDB
                saveFeedback(fullName, email, comment, request);
                showAlert(Alert.AlertType.INFORMATION, "Feedback Submitted", "Thank you for your feedback!");
                dialogStage.close();
            }
        });

        // Cancel button action
        cancelButton.setOnAction(e -> dialogStage.close());

        // Arrange the submit and cancel buttons horizontally
        HBox buttonBox = new HBox(10, submitButton, cancelButton);
        buttonBox.setPadding(new Insets(10));

        // Create the main layout (VBox) and add the components
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.getChildren().addAll(
            fullNameBox, emailBox, commentBox, requestBox, buttonBox
        );

        // Create the scene and set it to the stage
        Scene scene = new Scene(vbox, 800, 600);
        dialogStage.setScene(scene);

        // Show the dialog and wait for it to close
        dialogStage.showAndWait();
    }

    private void saveFeedback(String fullName, String email, String comment, String request) {
        try {
            MongoDatabase database = conn.getDatabase(); // Get the MongoDB database
            MongoCollection<Document> collection = database.getCollection("feedback"); // Use a collection for feedback

            // Create a new Document with the feedback data
            Document feedbackDoc = new Document()
                    .append("full_name", fullName)
                    .append("email", email)
                    .append("comment", comment)
                    .append("request", request);

            // Insert the feedback document into the collection
            collection.insertOne(feedbackDoc);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while saving feedback.");
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
