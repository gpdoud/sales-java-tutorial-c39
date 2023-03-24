package com.maxtrain.bootcamp.sales.orderline;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.maxtrain.bootcamp.sales.order.Order;
import com.maxtrain.bootcamp.sales.order.OrderRepository;

@CrossOrigin
@RestController
@RequestMapping("/api/orderlines")
public class OrderlineController {
	
	@Autowired
	private OrderlineRepository ordlRepo;
	@Autowired
	private OrderRepository ordRepo;
	
	private boolean recalculateOrderTotal(int orderId) {
		// read the order to be recalculated
		Optional<Order> anOrder = ordRepo.findById(orderId);
		// if not found, return false
		if(anOrder.isEmpty()) {
			return false;
		}
		// get the order
		Order order = anOrder.get();
		// get all orderlines attached to the order
		Iterable<Orderline> orderlines = ordlRepo.findByOrderId(orderId);
		double total = 0;
		for(Orderline ol : orderlines) {
			// for each orderline, multiply the quantity times the price
			// and add it to the total
			total += ol.getQuantity() * ol.getItem().getPrice();
		}
		// update the total in the order
		order.setTotal(total);
		ordRepo.save(order);
		
		return true;
	}
	
	@GetMapping
	public ResponseEntity<Iterable<Orderline>> getOrderlines() {
		Iterable<Orderline> orderlines = ordlRepo.findAll();
		return new ResponseEntity<Iterable<Orderline>>(orderlines, HttpStatus.OK);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("{id}")
	public ResponseEntity<Orderline> getOrderline(@PathVariable int id) {
		Optional<Orderline> orderline = ordlRepo.findById(id);
		if(orderline.isEmpty()) {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Orderline>(orderline.get(), HttpStatus.OK);
	}

	@PostMapping
	public ResponseEntity<Orderline> postOrderline(@RequestBody Orderline orderline) {
		Orderline newOrderline = ordlRepo.save(orderline);
		Optional<Order> order = ordRepo.findById(orderline.getOrder().getId());
		if(!order.isEmpty()) {
			boolean success = recalculateOrderTotal(order.get().getId());
			if(!success) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		return new ResponseEntity<Orderline>(newOrderline, HttpStatus.CREATED);
	}
	
	@SuppressWarnings("rawtypes")
	@PutMapping("{id}")
	public ResponseEntity putOrderline(@PathVariable int id, @RequestBody Orderline orderline) {
		if(orderline.getId() != id) {
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}
		ordlRepo.save(orderline);
		Optional<Order> order = ordRepo.findById(orderline.getOrder().getId());
		if(!order.isEmpty()) {
			boolean success = recalculateOrderTotal(order.get().getId());
			if(!success) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		return new ResponseEntity(HttpStatus.NO_CONTENT);
	}
	
	@SuppressWarnings("rawtypes")
	@DeleteMapping("{id}")
	public ResponseEntity deleteOrderline(@PathVariable int id) {
		Optional<Orderline> orderline = ordlRepo.findById(id);
		if(orderline.isEmpty()) {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
		ordlRepo.delete(orderline.get());
		Optional<Order> order = ordRepo.findById(orderline.get().getOrder().getId());
		if(!order.isEmpty()) {
			boolean success = recalculateOrderTotal(order.get().getId());
			if(!success) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
