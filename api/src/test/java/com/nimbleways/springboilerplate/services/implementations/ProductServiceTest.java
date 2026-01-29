package com.nimbleways.springboilerplate.services.implementations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private NotificationService notificationService;

	@InjectMocks
	private ProductService productService;

	@Test
	void notifyDelay_should_send_delay_notification_only() {
		Product p = new Product(null, 15, 0, "NORMAL", "RJ45 Cable", null, null, null);

		productService.notifyDelay(15, p);

		verify(notificationService, times(1)).sendDelayNotification(15, "RJ45 Cable");
		verifyNoInteractions(productRepository);
		assertEquals(0, p.getAvailable());
		assertEquals(15, p.getLeadTime());
	}

	@Test
	void processProducts_normal_available_gt_0_should_decrement_and_save() {
		Product p = new Product(null, 10, 2, "NORMAL", "RJ45 Cable", null, null, null);
		Mockito.when(productRepository.save(p)).thenReturn(p);

		productService.processProducts(Set.of(p));

		assertEquals(1, p.getAvailable());
		verify(productRepository, times(1)).save(p);
		verifyNoInteractions(notificationService);
	}

	@Test
	void processProducts_normal_out_of_stock_with_leadTime_should_notify_delay() {
		Product p = new Product(null, 7, 0, "NORMAL", "RJ45 Cable", null, null, null);

		productService.processProducts(Set.of(p));

		verify(notificationService, times(1)).sendDelayNotification(7, "RJ45 Cable");
		verifyNoInteractions(productRepository);
		assertEquals(0, p.getAvailable());
	}

	@Test
	void processProducts_seasonal_in_season_and_available_should_decrement_and_save() {
		LocalDate today = LocalDate.now();
		Product p = new Product(null, 3, 5, "SEASONAL", "Strawberries", null, today.minusDays(1), today.plusDays(10));

		Mockito.when(productRepository.save(p)).thenReturn(p);

		productService.processProducts(Set.of(p));

		assertEquals(4, p.getAvailable());
		verify(productRepository, times(1)).save(p);
		verifyNoInteractions(notificationService);
	}

	@Test
	void handleSeasonalProduct_restock_after_season_end_should_notify_out_of_stock_set_available_0_and_save() {
		LocalDate today = LocalDate.now();

		Product p = new Product(null, 10,
				0, 
				"SEASONAL", "Strawberries", null, today.minusDays(5),
				today.plusDays(2)
		);

		Mockito.when(productRepository.save(p)).thenReturn(p);

		productService.handleSeasonalProduct(p);

		assertEquals(0, p.getAvailable());
		verify(notificationService, times(1)).sendOutOfStockNotification("Strawberries");
		verify(productRepository, times(1)).save(p);
		verify(notificationService, never()).sendDelayNotification(anyInt(), anyString());
	}

}
