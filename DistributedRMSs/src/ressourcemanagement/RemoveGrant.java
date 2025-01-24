package ressourcemanagement;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.mongodb.client.model.Filters.*;

public class RemoveGrant {
    private Conn conn; // Assuming the Conn class provides the MongoDB connection

    public RemoveGrant() {
        conn = new Conn();
    }

    void showGrantDialog() {
        
        try {
            MongoDatabase database = conn.getDatabase();
            if (database == null) {
                System.err.println("Database connection is not initialized.");
                return;
            }

            // Step 1: Check resource_requests collection for unreleased resources
            MongoCollection<Document> resourceRequestsCollection = database.getCollection("resource_requests");
            for (Document request : resourceRequestsCollection.find(eq("status", "allocated"))) {
                String resourceName = request.getString("resource_name");
                String username = request.getString("username");
                LocalDateTime releaseTime = LocalDateTime.parse(
                        request.getString("release_time"),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );

                // Step 2: Check if the release time has passed
                if (releaseTime.isBefore(LocalDateTime.now())) {
                    System.out.println("Release time exceeded for resource: " + resourceName);

                    // Step 3: Remove grant and release resource
                    removeGrantFromUser(database, resourceName, username);

                    // Step 4: Update resource collections
                    updateResourceAllocatedSize(database, resourceName);

                    // Step 5: Update resource request status to "removed"
                    resourceRequestsCollection.updateOne(
                            eq("_id", request.getObjectId("_id")),
                            Updates.set("status", "removed")
                    );

                    System.out.println("Grant removed and resource released for: " + resourceName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeGrantFromUser(MongoDatabase database, String resourceName, String username) {
        try {
            // Assuming there is a user collection to track user-resource grants
            MongoCollection<Document> userCollection = database.getCollection("users");

            // Remove the resource grant from the user's record
            userCollection.updateOne(
                    eq("username", username),
                    Updates.pull("granted_resources", resourceName)
            );

            System.out.println("Grant removed from user: " + username);
        } catch (Exception e) {
            System.err.println("Error while removing grant from user: " + e.getMessage());
        }
    }

    private void updateResourceAllocatedSize(MongoDatabase database, String resourceName) {
        try {
            // Fetch all collections that might contain resource allocation
            for (String collectionName : database.listCollectionNames()) {
                if (collectionName.endsWith("_resources")) {
                    MongoCollection<Document> resourceCollection = database.getCollection(collectionName);

                    Document resource = resourceCollection.find(eq("resource_name", resourceName)).first();
                    if (resource != null) {
                        double allocatedSize = resource.getDouble("allocated_size");

                        // Reset allocated size to 0 for the resource
                        Bson update = Updates.set("allocated_size", 0.0);
                        resourceCollection.updateOne(eq("resource_name", resourceName), update);

                        System.out.println("Allocated size reset for resource: " + resourceName + " in collection: " + collectionName);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error while updating resource allocated size: " + e.getMessage());
        }
    }
}
