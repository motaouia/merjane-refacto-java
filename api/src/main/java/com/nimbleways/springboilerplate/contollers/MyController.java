package com.nimbleways.springboilerplate.contollers;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.services.implementations.OrderService;
import com.nimbleways.springboilerplate.services.implementations.ProductService;

@RestController
@RequestMapping("/orders")
public class MyController {
	private final ProductService productService;

	private final OrderService orderService;

	public MyController(ProductService productService, OrderService orderService) {
		super();
		this.productService = productService;
		this.orderService = orderService;
	}

	@PostMapping("{orderId}/processOrder")
	@ResponseStatus(HttpStatus.OK)
	public ProcessOrderResponse processOrder(@PathVariable Long orderId) {
		Order order = orderService.getOrderById(orderId);

		Set<Product> products = order.getItems();
		productService.processProducts(products);

		return new ProcessOrderResponse(order.getId());
	}

	
}
