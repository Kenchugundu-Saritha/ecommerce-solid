package com.example.ecommerce_solid.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce_solid.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}