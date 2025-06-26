package in.edu.kristujayanti.handlers;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class OrderHandler extends AbstractVerticle {

    private final MongoClient mongoClient;

    public OrderHandler(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.post("/order").handler(this::placeOrder);
        router.put("/order/:id/status").handler(this::updateOrderStatus);
        router.get("/orders/user/:userId").handler(this::getOrderHistory);
        router.get("/sales/product/:productId").handler(this::getTotalSalesByProduct);
        router.get("/sales/time").handler(this::getTotalSalesByTime);

        vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
            if (http.succeeded()) {
                startPromise.complete();
                System.out.println("HTTP server started on port 8888");
            } else {
                startPromise.fail(http.cause());
            }
        });
    }

    private void placeOrder(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        body.put("status", "PLACED");
        body.put("timestamp", System.currentTimeMillis());

        mongoClient.insert("orders", body, res -> {
            if (res.succeeded()) {
                ctx.response().setStatusCode(201).end("Order placed with ID: " + res.result());
            } else {
                ctx.response().setStatusCode(500).end("Failed to place order");
            }
        });
    }

    private void updateOrderStatus(RoutingContext ctx) {
        String orderId = ctx.pathParam("id");
        String status = ctx.getBodyAsJson().getString("status");

        JsonObject query = new JsonObject().put("_id", orderId);
        JsonObject update = new JsonObject().put("$set", new JsonObject().put("status", status));

        mongoClient.updateCollection("orders", query, update, res -> {
            if (res.succeeded()) {
                ctx.response().end("Order status updated");
            } else {
                ctx.response().setStatusCode(500).end("Update failed");
            }
        });
    }

    private void getOrderHistory(RoutingContext ctx) {
        int userId = Integer.parseInt(ctx.pathParam("userId"));

        JsonObject query = new JsonObject().put("userId", userId);
        mongoClient.find("orders", query, res -> {
            if (res.succeeded()) {
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(res.result().toString());
            } else {
                ctx.response().setStatusCode(500).end("Failed to retrieve order history");
            }
        });
    }

    private void getTotalSalesByProduct(RoutingContext ctx) {
        int productId = Integer.parseInt(ctx.pathParam("productId"));
        JsonObject query = new JsonObject().put("productId", productId);

        mongoClient.find("orders", query, res -> {
            if (res.succeeded()) {
                int total = res.result().stream()
                        .mapToInt(doc -> doc.getInteger("quantity", 0))
                        .sum();
                ctx.response().end("Total sales: " + total);
            } else {
                ctx.response().setStatusCode(500).end("Failed to calculate total sales");
            }
        });
    }

    private void getTotalSalesByTime(RoutingContext ctx) {
        long start = Long.parseLong(ctx.request().getParam("start"));
        long end = Long.parseLong(ctx.request().getParam("end"));

        JsonObject query = new JsonObject()
                .put("timestamp", new JsonObject()
                        .put("$gte", start)
                        .put("$lte", end));

        mongoClient.find("orders", query, res -> {
            if (res.succeeded()) {
                int total = res.result().stream()
                        .mapToInt(doc -> doc.getInteger("quantity", 0))
                        .sum();
                ctx.response().end("Total sales in period: " + total);
            } else {
                ctx.response().setStatusCode(500).end("Failed to get sales data");
            }
        });
    }
}
