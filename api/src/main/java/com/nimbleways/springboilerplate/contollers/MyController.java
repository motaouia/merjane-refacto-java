package com.nimbleways.springboilerplate.contollers;

import java.time.LocalDate;
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
		processProducts(products);

		return new ProcessOrderResponse(order.getId());
	}

	private void processProducts(Set<Product> products) {

		for (Product p : products) {
			if (p.getType().equals("NORMAL")) {
				if (p.getAvailable() > 0) {
					decrementAndSave(p);
				} else {
					int leadTime = p.getLeadTime();
					if (leadTime > 0) {
						productService.notifyDelay(leadTime, p);
					}
				}
			} else if (p.getType().equals("SEASONAL")) {
				// Add new season rules
				if ((LocalDate.now().isAfter(p.getSeasonStartDate()) && LocalDate.now().isBefore(p.getSeasonEndDate())
						&& p.getAvailable() > 0)) {
					decrementAndSave(p);
				} else {
					productService.handleSeasonalProduct(p);
				}
			} else if (p.getType().equals("EXPIRABLE")) {
				if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(LocalDate.now())) {
					decrementAndSave(p);
				} else {
					productService.handleExpiredProduct(p);
				}
			}
		}

	}

	private void decrementAndSave(Product p) {
		p.setAvailable(p.getAvailable() - 1);
		productService.save(p);
	}
}
