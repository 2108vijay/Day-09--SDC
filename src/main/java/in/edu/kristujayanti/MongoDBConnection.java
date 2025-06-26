package in.edu.kristujayanti;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class MongoDBConnection {
    private static MongoClient client;

    public static MongoClient getInstance(Vertx vertx) {
        if (client == null) {
            JsonObject config = new JsonObject()
                    .put("connection_string", "mongodb://localhost:27017")
                    .put("db_name", "ecommerce"); // Change as needed

            client = MongoClient.createShared(vertx, config);
        }
        return client;
    }
}
