package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.DateUtils;

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

	public void handleSeasonalProduct(Product p,  LocalDate today) {
		
		if (today.plusDays(p.getLeadTime()).isAfter(p.getSeasonEndDate())) {
			notificationService.sendOutOfStockNotification(p.getName());
			p.setAvailable(0);
			save(p);
		} else if (p.getSeasonStartDate().isAfter(today)) {
			notificationService.sendOutOfStockNotification(p.getName());
		} else {
			notifyDelay(p.getLeadTime(), p);
		}
	}

	public void processProducts(Set<Product> products) {
		LocalDate today = DateUtils.today();
		for (Product p : products) {
			switch (ProductType.from(p.getType())) {
			case NORMAL -> handleNormal(p);
			case SEASONAL -> handleSeasonal(p, today);
			case EXPIRABLE -> handleExpiredProduct(p, today);
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

	private void handleSeasonal(Product p, LocalDate today) {
		
		boolean inSeason = today.isAfter(p.getSeasonStartDate()) && today.isBefore(p.getSeasonEndDate());

		if (inSeason && isAvailable(p)) {
			decrementAndSave(p);
		} else {
			handleSeasonalProduct(p, today);
		}
	}

	public void handleExpiredProduct(Product p, LocalDate today) {
		if (isAvailable(p) && isNotExpired(p, today)) {
			decrementAndSave(p);
			return;
		}
		notificationService.sendExpirationNotification(p.getName(), p.getExpiryDate());
		p.setAvailable(0);
		save(p);

	}

	private void decrementAndSave(Product p) {
		p.setAvailable(p.getAvailable() - 1);
		save(p);
	}

	private boolean isNotExpired(Product p, LocalDate today) {
		return p.getExpiryDate().isAfter(today);
	}

	private boolean isAvailable(Product p) {
		return p.getAvailable() > 0;
	}

}