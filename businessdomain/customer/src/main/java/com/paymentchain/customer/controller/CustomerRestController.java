/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paymentchain.customer.controller;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paymentchain.customer.business.transactions.BusinessTransaction;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.exception.BusinessRuleException;
import com.paymentchain.customer.respository.CustomerRepository;

/**
 *
 * @author sotobotero
 */
@RestController
@RequestMapping("/customer")
public class CustomerRestController {
    
    @Autowired
    CustomerRepository customerRepository;
    
    @Autowired
    BusinessTransaction bt;
    
    @Value("${user.role}") //Con esto cojo la propiedad de este nombre del fichero del repositorio
    private String role;
    
    
    @GetMapping("/full")
    public Customer get(@RequestParam String code) {
       Customer customer = bt.get(code);
       return customer;
    }
    
    
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
    public ResponseEntity<?> post(@RequestBody Customer input) throws BusinessRuleException, UnknownHostException {
        Customer save = bt.save(input);
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
}
