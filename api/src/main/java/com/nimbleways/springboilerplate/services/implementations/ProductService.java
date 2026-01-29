package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

@Service
public class ProductService {

	private final ProductRepository productRepository;

	private final NotificationService notificationService;

	public ProductService(ProductRepository productRepository, NotificationService notificationService) {
		super();
		this.productRepository = productRepository;
		this.notificationService = notificationService;
	}

	public Product save(Product product) {
		return productRepository.save(product);
	}

	public void notifyDelay(int leadTime, Product p) {
		notificationService.sendDelayNotification(leadTime, p.getName());
	}

	public void handleSeasonalProduct(Product p) {
		if (LocalDate.now().plusDays(p.getLeadTime()).isAfter(p.getSeasonEndDate())) {
			notificationService.sendOutOfStockNotification(p.getName());
			p.setAvailable(0);
			productRepository.save(p);
		} else if (p.getSeasonStartDate().isAfter(LocalDate.now())) {
			notificationService.sendOutOfStockNotification(p.getName());
			productRepository.save(p);
		} else {
			notifyDelay(p.getLeadTime(), p);
		}
	}

	public void handleExpiredProduct(Product p) {
		if (isAvailable(p) && isNotExpired(p)) {
			decrementAndSave(p);
			return;
		}
		notificationService.sendExpirationNotification(p.getName(), p.getExpiryDate());
		p.setAvailable(0);
		save(p);

	}

	public void processProducts(Set<Product> products) {
		for (Product p : products) {
			switch (p.getType()) {
			case "NORMAL" -> handleNormal(p);
			case "SEASONAL" -> handleSeasonal(p);
			case "EXPIRABLE" -> handleExpirable(p);
			}
		}
	}

	private void handleNormal(Product p) {
		if (p.getAvailable() > 0) {
			decrementAndSave(p);
		} else if (p.getLeadTime() > 0) {
			notifyDelay(p.getLeadTime(), p);
		}
	}

	private void handleSeasonal(Product p) {
		boolean inSeason = LocalDate.now().isAfter(p.getSeasonStartDate())
				&& LocalDate.now().isBefore(p.getSeasonEndDate());

		if (inSeason && isAvailable(p)) {
			decrementAndSave(p);
		} else {
			handleSeasonalProduct(p);
		}
	}

	private void handleExpirable(Product p) {

		if (isAvailable(p) && isNotExpired(p)) {
			decrementAndSave(p);
		} else {
			handleExpiredProduct(p);
		}
	}

	private void decrementAndSave(Product p) {
		p.setAvailable(p.getAvailable() - 1);
		save(p);
	}

	private boolean isNotExpired(Product p) {
		return p.getExpiryDate().isAfter(LocalDate.now());
	}

	private boolean isAvailable(Product p) {
		return p.getAvailable() > 0;
	}
}