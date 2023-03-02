/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paymentchain.customer.controller;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.entities.CustomerProduct;
import com.paymentchain.customer.respository.CustomerRepository;

import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

/**
 *
 * @author sotobotero
 */
@RestController
@RequestMapping("/customer")
public class CustomerRestController {
    
    @Autowired
    CustomerRepository customerRepository;
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${user.role}") //Con esto cojo la propiedad de este nombre del fichero del repositorio
    private String role;
    
    public CustomerRestController(WebClient.Builder webCLieBuilder) {
    	this.webClientBuilder = webCLieBuilder;
    }
    
    //webClient requiere la librería HttpCLient para trabajar de manera apropiada
    HttpClient client = HttpClient.create()
    		//Conexión de timeout: is un periodo el cual la conexión entre el cliente y el servidor de ser establecida
    		.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
    		.option(ChannelOption.SO_KEEPALIVE, true)
    		.option(EpollChannelOption.TCP_KEEPIDLE, 300)
    		.option(EpollChannelOption.TCP_KEEPINTVL, 60)
    		//ResponseTimeout: Maxímo tiempo que esperamos para recibir respuesyadespués de enviar la petición
    		.responseTimeout(Duration.ofSeconds(1))
    		//Read and Wirte timeput: timeout de lectura ocurre cuando no hay datos leídos en un cierto período de tiempo
    		//mientras que el timeout de escritura es cuando una operación de escriura no finaliza en un período de tiempo establecido
    		.doOnConnected(connection -> {
    			connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
    			connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
    		});
    @GetMapping()
    public ResponseEntity<List<Customer>> list() {   
    	System.out.println("el rol es: "+role);
        List <Customer> findAll = customerRepository.findAll();
        if(findAll == null || findAll.isEmpty()) {
        	return ResponseEntity.noContent().build();
        }
        else {
        	return ResponseEntity.ok(findAll);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Customer> get(@PathVariable long id) {
    	return customerRepository.findById(id)
    	        .map(ResponseEntity::ok)
    	        .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/hello")
    public String sayHello() {
    	return "Hola, tu rol es: "+role;
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable long id, @RequestBody Customer input) {
    	Customer find = customerRepository.findById(id).get();   
        if(find != null){     
            find.setCode(input.getCode());
            find.setName(input.getName());
            find.setIban(input.getIban());
            find.setPhone(input.getPhone());
            find.setSurname(input.getSurname());
        }
        Customer save = customerRepository.save(find);
           return ResponseEntity.ok(save);
    }
    
    @PostMapping
    public ResponseEntity<?> post(@RequestBody Customer input) throws /*BussinesRuleException,*/ Exception {
    	for(int i = 0;i < input.getProducts().size(); i++) 
    		input.getProducts().get(i).setCustomer(input);    			
//    	input.getProducts().forEach(x -> x.setCustomer(input)); se podría hacer con lamda
        Customer save = customerRepository.save(input);
        return new ResponseEntity<>(save,HttpStatus.CREATED);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        Optional<Customer> findById = customerRepository.findById(id);
        if(findById.get() != null) {
        	customerRepository.delete(findById.get());
        }
        return ResponseEntity.ok().build();
    }
    
    
    @GetMapping("/full")
    public Customer getByCode(@RequestParam String code) {
       Customer customer = customerRepository.findByCode(code);
       List<CustomerProduct> products = customer.getProducts();
       for (int i = 0; i< products.size(); i++) {
    	   //Cojo el nombre desde el micro de productos, y se lo asigno a los productos del micro del cliente
    	   String productName = getProductName(products.get(i).getId());
    	   products.get(i).setProductName(productName);
       }
//       Con lamnda
//       products.forEach(x -> {
//    	   String productName = getProductName(x.getId());
//    	   x.setProductName(productName);  	   
//       });
       
     //find all transactions that belong this account number
       List<?> transactions = getTransactions(customer.getIban());
        customer.setTransactions(transactions);
       return customer;
    }
    
    
    private String getProductName(long id) {
    	WebClient build = webClientBuilder.clientConnector(new ReactorClientHttpConnector(client))
    			.baseUrl("http://businessdomain-product/product")
    			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    			.defaultUriVariables(Collections.singletonMap("url", "http://businessdomain-product/product"))
    			.build();
    	//Hacemos uso del método get, pero el añadimos a la uri el id del producto
    	JsonNode block = build.method(HttpMethod.GET).uri("/" + id)
    			.retrieve().bodyToMono(JsonNode.class).block(); //block es porque aún no trabajamos con programación reactiva
    	String name = block.get("name").asText();
    	return name;
    }
    
   
	/**
     * Call Transaction Microservice and Find all transaction that belong to the account give
     * @param iban account number of the customer
     * @return All transaction that belong this accountde esto me acuerdo
     * 
     */
    private  List<?> getTransactions(String  iban) { 
        WebClient build = webClientBuilder.clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://businessdomain-transaction/transaction")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)               
                .build();
        
        
         List<?> transactions = build.method(HttpMethod.GET).uri(uriBuilder -> uriBuilder
                .path("/customer/transactions")
                .queryParam("ibanAccount", iban)               
                .build())
                .retrieve().bodyToFlux(Object.class).collectList().block();

      
        return transactions;
    }
}
