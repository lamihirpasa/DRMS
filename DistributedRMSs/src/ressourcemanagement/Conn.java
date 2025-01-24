package ressourcemanagement;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class Conn {
    
    MongoClient mongoClient;
    MongoDatabase database;

    Conn() {
        try {
            // Connection string to MongoDB (modify if necessary)
            String connectionString = "mongodb://localhost:27017";

            // Create MongoDB client
            mongoClient = MongoClients.create(connectionString);

            // Connect to the specific database
            database = mongoClient.getDatabase("EthRMS");

            System.out.println("Connected to MongoDB database: " + database.getName());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Optionally, provide a method to get the database object
    public MongoDatabase getDatabase() {
        return database;
    }
}
