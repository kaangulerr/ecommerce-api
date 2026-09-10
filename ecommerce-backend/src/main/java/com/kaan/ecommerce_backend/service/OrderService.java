package com.kaan.ecommerce_backend.service;

import com.kaan.ecommerce_backend.entity.*;
import com.kaan.ecommerce_backend.repository.CartRepository;
import com.kaan.ecommerce_backend.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final DiscountService discountService;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, DiscountService discountService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.discountService = discountService;
    }

    @Transactional
    public Order checkout(User user) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found!"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Your cart is empty, cannot place order!");
        }

        double finalPrice = cart.getTotalPrice();
        String appliedCoupon = cart.getAppliedCouponCode();

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalAmount(finalPrice);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());

            double priceAtPurchase = 0.0;
            if (cartItem.getDiscountedPrice() != null) {
                priceAtPurchase = cartItem.getDiscountedPrice();
            } else if (cartItem.getProduct() != null && cartItem.getProduct().getPriceValue() != null) {
                try {
                    priceAtPurchase = Double.parseDouble(cartItem.getProduct().getPriceValue());
                } catch (NumberFormatException ignored) {}
            }
            orderItem.setPriceAtPurchase(priceAtPurchase);

            orderItems.add(orderItem);
        }
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        if (appliedCoupon != null && !appliedCoupon.trim().isEmpty()) {
            discountService.markCouponAsUsed(appliedCoupon);
        }

        cart.getItems().clear();
        cart.setAppliedCouponCode(null);
        cartRepository.save(cart);

        return savedOrder;
    }

    public List<Order> getMyOrders(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    public List<Order> getAllOrdersForAdmin(String sellerName) {
        if (sellerName != null && !sellerName.trim().isEmpty()) {
            return orderRepository.findBySellerName(sellerName.trim());
        }
        return orderRepository.findAllByOrderByOrderDateDesc();
    }
}