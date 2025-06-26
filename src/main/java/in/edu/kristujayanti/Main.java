package in.edu.kristujayanti;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import in.edu.kristujayanti.handlers.OrderHandler;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        // MongoDB config
        JsonObject mongoConfig = new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "ecommerce");

        // Create MongoDB client
        MongoClient mongoClient = MongoClient.createShared(vertx, new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "your-database-name"));

        vertx.deployVerticle(new OrderHandler(mongoClient));


        // Deploy your verticle, pass MongoClient as needed
        vertx.deployVerticle(new OrderHandler(mongoClient), res -> {
            if (res.succeeded()) {
                System.out.println("OrderHandler deployed successfully!");
            } else {
                System.out.println("Deployment failed: " + res.cause());
            }
        });
    }
}
