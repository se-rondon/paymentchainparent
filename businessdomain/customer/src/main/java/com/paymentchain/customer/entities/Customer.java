/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paymentchain.customer.entities;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import lombok.Data;

/**
 *
 * @author sotobotero
 */
@Entity
@Data
public class Customer {
   @Id
   @GeneratedValue(strategy=GenerationType.AUTO)
   private long id;
   private String code;
   private String iban;
   private String address;
   private String name;
   private String surname;
   private String phone;
   
   @OneToMany(fetch = FetchType.LAZY, mappedBy = "customer", cascade = CascadeType.ALL ) 
   private List<CustomerProduct> products;
   @Transient
   private List<?> transactions;
}
