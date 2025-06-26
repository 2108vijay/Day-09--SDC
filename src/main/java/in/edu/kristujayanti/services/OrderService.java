package in.edu.kristujayanti.services;


import in.edu.kristujayanti.models.Order;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class OrderService {
    private List<Order> orders = new ArrayList<>();
    private int orderCounter = 1;

    public Order placeOrder(int userId, int productId, int quantity) {
        Order order = new Order(orderCounter++, userId, productId, quantity, "PLACED", LocalDateTime.now());
        orders.add(order);
        return order;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        for (Order order : orders) {
            if (order.getOrderId() == orderId) {
                order.setStatus(status);
                return true;
            }
        }
        return false;
    }

    public List<Order> getOrderHistoryByUser(int userId) {
        return orders.stream()
                .filter(order -> order.getUserId() == userId)
                .collect(Collectors.toList());
    }

    public int getTotalSalesByProduct(int productId) {
        return orders.stream()
                .filter(order -> order.getProductId() == productId)
                .mapToInt(Order::getQuantity)
                .sum();
    }

    public int getTotalSalesByTimePeriod(LocalDateTime start, LocalDateTime end) {
        return orders.stream()
                .filter(order -> order.getTimestamp().isAfter(start) && order.getTimestamp().isBefore(end))
                .mapToInt(Order::getQuantity)
                .sum();
    }
} 

