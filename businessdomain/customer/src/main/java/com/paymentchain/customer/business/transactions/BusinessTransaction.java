/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paymentchain.customer.business.transactions;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.entities.CustomerProduct;
import com.paymentchain.customer.exception.BusinessRuleException;
import com.paymentchain.customer.respository.CustomerRepository;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

/**
 *
 * @author sotobotero
 */
@Service
public class BusinessTransaction {

	@Autowired
	CustomerRepository customerRepository;

	private final WebClient.Builder webClientBuilder;

	public BusinessTransaction(WebClient.Builder webClientBuilder) {
		this.webClientBuilder = webClientBuilder;
	}

	// define timeout
	TcpClient tcpClient = TcpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
			.doOnConnected(connection -> {
				connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
				connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
			});

	public Customer get(String code) {
		Customer customer = customerRepository.findByCode(code);
		if (customer.getProducts() != null) {
			List<CustomerProduct> products = customer.getProducts();
			products.forEach(dto -> {
				try {
					String productName = getProductName(dto.getProductId());
					dto.setProductName(productName);
				} catch (UnknownHostException ex) {
					Logger.getLogger(BusinessTransaction.class.getName()).log(Level.SEVERE, null, ex);
				}
			});
		}
		customer.setTransactions(getTransacctions(customer.getIban()));
		return customer;
	}

	public Customer save(Customer input) throws BusinessRuleException, UnknownHostException {
		if (input.getProducts() != null) {
			for (Iterator<CustomerProduct> it = input.getProducts().iterator(); it.hasNext();) {
				CustomerProduct dto = it.next();
				String productName = getProductName(dto.getProductId());
				if (productName.isBlank()) {
					BusinessRuleException exception = new BusinessRuleException("1025",
							"Error de validacion, producto no existe", HttpStatus.PRECONDITION_FAILED);
					throw exception;
				} else {
					dto.setCustomer(input);
				}
			}
		}
		Customer save = customerRepository.save(input);
		return save;
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> getTransacctions(String accountIban) {
		List<T> transactions = new ArrayList<>();
		try {
			WebClient client = webClientBuilder
					.clientConnector(new ReactorClientHttpConnector(reactor.netty.http.client.HttpClient.create()
							.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000).doOnConnected(connection -> {
								connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
								connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
							})))
					.baseUrl("http://localhost:8082/transacciones")
					.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.defaultUriVariables(Collections.singletonMap("url", "http://localhost:8082/transacciones"))
					.build();
			
			List<Object> block = client.method(HttpMethod.GET)
					.uri(uriBuilder -> uriBuilder.path("/transactions").queryParam("ibanAccount", accountIban).build())
					.retrieve().bodyToFlux(Object.class).collectList().block();
			transactions = (List<T>) block;
		} catch (Exception e) {
			return transactions;
		}
		return transactions;
	}

	private String getProductName(long id) throws UnknownHostException {
		String name = null;
//		try {
			WebClient client = webClientBuilder
					.clientConnector(new ReactorClientHttpConnector(reactor.netty.http.client.HttpClient.create()
							.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000).doOnConnected(connection -> {
								connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
								connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
							})))
					.baseUrl("http://localhost:8083/productos")
					.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.defaultUriVariables(Collections.singletonMap("url", "http://localhost:8083/productos")).build();

			JsonNode block = client.method(HttpMethod.GET).uri("/" + id).retrieve().bodyToMono(JsonNode.class).block();
			name = block.get("name").asText();
//		} catch (WebClientResponseException e) {
//			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
//				return "";
//			} else {
//				throw new UnknownHostException(e.getMessage());
//			}
//		}
		return name;
	}
}
