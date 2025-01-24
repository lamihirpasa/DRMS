package ressourcemanagement;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.*;

public class ResourceStatus {

    private Conn conn; // Assuming the Conn class provides the MongoDB connection

    public ResourceStatus() {
        conn = new Conn();
    }

    public void showResourceStatusDialog() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Resource Status Across Regions");

        // TableView setup
        TableView<Map<String, String>> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Create a list to store all unique resource names
        Set<String> resourceNames = new TreeSet<>();
        Map<String, Map<String, String>> resourceData = new HashMap<>();

        // Fetch all region collections and their resources
        try {
            MongoDatabase database = conn.getDatabase();

            // Identify all region collections dynamically
            List<String> regionCollections = new ArrayList<>();
            for (String collectionName : database.listCollectionNames()) {
                if (collectionName.endsWith("_resources")) {
                    regionCollections.add(collectionName);
                }
            }

            // Populate resource data for each region
            for (String regionCollection : regionCollections) {
                String regionName = regionCollection.replace("_resources", "");
                MongoCollection<Document> collection = database.getCollection(regionCollection);

                for (Document resource : collection.find()) {
                    String resourceName = resource.getString("resource_name");
                    String status = resource.getString("status");
                    double allocatedSize = resource.getDouble("allocated_size");
                    String unit = resource.getString("type");

                    resourceNames.add(resourceName);

                    // Create or update the resource's region-specific details
                    resourceData
                        .computeIfAbsent(resourceName, k -> new HashMap<>())
                        .put(regionName, String.format("Status: %s, Size: %.2f %s", status, allocatedSize, unit));
                }
            }

            // Add columns to the table
            TableColumn<Map<String, String>, String> resourceColumn = new TableColumn<>("Resource Name");
            resourceColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("Resource Name")));
            tableView.getColumns().add(resourceColumn);

            for (String region : regionCollections) {
                String regionName = region.replace("_resources", "");

                TableColumn<Map<String, String>, String> regionColumn = new TableColumn<>(regionName);
                regionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault(regionName, "N/A")));
                tableView.getColumns().add(regionColumn);
            }

            // Populate the table data
            ObservableList<Map<String, String>> tableData = FXCollections.observableArrayList();

            for (String resourceName : resourceNames) {
                Map<String, String> row = new HashMap<>();
                row.put("Resource Name", resourceName);

                for (String region : regionCollections) {
                    String regionName = region.replace("_resources", "");
                    row.put(regionName, resourceData.getOrDefault(resourceName, new HashMap<>()).getOrDefault(regionName, "N/A"));
                }

                tableData.add(row);
            }

            tableView.setItems(tableData);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Layout and scene setup
        VBox layout = new VBox(10, tableView);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 1200, 800);
        dialogStage.setScene(scene);
        dialogStage.show();
    }
}
