/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paymentchain.product.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymentchain.product.entities.Product;
import com.paymentchain.product.respository.ProductRepository;

/**
 *
 * @author sotobotero
 */
@RestController
@RequestMapping("/product")
public class ProductRestController {
    
    @Autowired
    ProductRepository productRepository;
    
    @Value("${user.role}") //Con esto cojo la propiedad de este nombre del fichero del repositorio
    private String role;
    
    @GetMapping()
    public List<Product> list() {
    	System.out.println("el rol es: "+role);
        return productRepository.findAll();
    }
    
    @GetMapping("/{id}")
    public Product get(@PathVariable long id) {
        return productRepository.findById(id).get();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable long id, @RequestBody Product input) {
        Product save = productRepository.save(input);
        return ResponseEntity.ok(save);
    }
    
    @PostMapping
    public ResponseEntity<?> post(@RequestBody Product input) {
        Product save = productRepository.save(input);
        return ResponseEntity.ok(save);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        Optional<Product> findById = productRepository.findById(id);
        if(findById.get() != null) {
        	productRepository.delete(findById.get());
        }
        return ResponseEntity.ok().build();
    }
    
}
