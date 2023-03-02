/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paymentchain.trasactions.entities;


import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

/**
 *
 * @author sergio
 */
@Entity
@Data
public class Transaction {
  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
   private long id;
   private String reference;
   private String ibanAccount;
   private Date date;
   private double amount;
   private double fee;
   private String description;
   private String status;
   private String channel;
}
