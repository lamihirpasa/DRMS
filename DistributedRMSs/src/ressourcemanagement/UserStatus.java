package ressourcemanagement;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * This class retrieves and displays user resource information from any collection.
 */
class UserStatus {

    private final Conn conn;

    public UserStatus() {
        conn = new Conn(); // Initialize the MongoDB connection
    }

    public void showUserStatusDialog() {
        try {
            // Use the Conn class to get the database
            MongoDatabase database = conn.getDatabase();

            // Get all collections in the database
            List<String> collectionNames = new ArrayList<>();
            for (String collectionName : database.listCollectionNames()) {
                collectionNames.add(collectionName);
            }

            List<UserResourceInfo> userInfoList = new ArrayList<>();

            // Loop through all collections to fetch user data
            for (String collectionName : collectionNames) {
                MongoCollection<Document> collection = database.getCollection(collectionName);

                try (MongoCursor<Document> cursor = collection.find().iterator()) {
                    while (cursor.hasNext()) {
                        Document doc = cursor.next();

                        // Extract fields from the document
                        String userName = doc.getString("username");
                        String resourceName = doc.getString("resource_name");
                        String resourceType = doc.getString("type");

                        // Handle size as Double or String, and convert to Integer
                        Integer resourceSize = null;
                        Object sizeObject = doc.get("size");
                        if (sizeObject instanceof Double) {
                            resourceSize = ((Double) sizeObject).intValue();
                        } else if (sizeObject instanceof String) {
                            try {
                                resourceSize = Integer.parseInt((String) sizeObject);
                            } catch (NumberFormatException e) {
                                resourceSize = 0;  // Default value or handle as needed
                            }
                        }
                        String region = doc.getString("region");
                        String status = doc.getString("status");

                        // Ensure fields are not null or empty before adding to the list
                        if (userName != null && !userName.isEmpty() && resourceName != null && !resourceName.isEmpty()) {
                            userInfoList.add(new UserResourceInfo(userName, resourceName, resourceType, resourceSize, region, status));
                        }
                    }
                }
            }

            // Display the data in a TableView
            displayUserStatusTable(userInfoList);

        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Error fetching user data", e.getMessage());
        }
    }

    private void displayUserStatusTable(List<UserResourceInfo> userInfoList) {
        // Create a TableView
        TableView<UserResourceInfo> table = new TableView<>();

        // Define columns
        TableColumn<UserResourceInfo, String> nameColumn = new TableColumn<>("User Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));

        TableColumn<UserResourceInfo, String> resourceColumn = new TableColumn<>("Resource Name");
        resourceColumn.setCellValueFactory(new PropertyValueFactory<>("resourceName"));

        TableColumn<UserResourceInfo, String> typeColumn = new TableColumn<>("Resource Type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("resourceType"));

        TableColumn<UserResourceInfo, Integer> sizeColumn = new TableColumn<>("Resource Size");
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("resourceSize"));
        
         TableColumn<UserResourceInfo, Integer> regionColumn = new TableColumn<>("Region of User");
        regionColumn.setCellValueFactory(new PropertyValueFactory<>("region"));

        TableColumn<UserResourceInfo, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add columns to the table
        table.getColumns().addAll(nameColumn, resourceColumn, typeColumn, sizeColumn, regionColumn, statusColumn);

        // Add data to the table
        table.getItems().addAll(userInfoList);

        // Create a layout and scene
        VBox layout = new VBox(table);
        Scene scene = new Scene(layout, 800, 600);

        // Create and display the stage
        Stage stage = new Stage();
        stage.setTitle("User Status");
        stage.setScene(scene);
        stage.show();
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Helper class to represent user resource information.
     */
    public static class UserResourceInfo {
    private final String userName;
    private final String resourceName;
    private final String resourceType;
    private final Integer resourceSize;
    private final String status;
    private final String region; // Add this field

    public UserResourceInfo(String userName, String resourceName, String resourceType, Integer resourceSize, String status, String region) {
        this.userName = userName;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.resourceSize = resourceSize;
        this.status = status;
        this.region = region; // Initialize the region
    }

    public String getUserName() {
        return userName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Integer getResourceSize() {
        return resourceSize;
    }

    public String getStatus() {
        return status;
    }

    public String getRegion() {
        return region; // Add this getter for region
    }
}
}