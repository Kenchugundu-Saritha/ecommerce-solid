package com.example.ecommerce_solid.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.ecommerce_solid.dto.ProductRequest;
import com.example.ecommerce_solid.exception.ProductNotFoundException;
import com.example.ecommerce_solid.model.Product;
import com.example.ecommerce_solid.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // CREATE
    public Product createProduct(ProductRequest request) {

        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(request.getCategory());

        return productRepository.save(product);
    }

    // GET ALL
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // GET BY ID
    public Product getProductById(Long id) {
    	return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    // UPDATE
    public Product updateProduct(Long id, ProductRequest request) {

    	Product existingProduct =
    	        productRepository.findById(id)
    	                .orElseThrow(() -> new ProductNotFoundException(id));

        if (existingProduct == null) {
            return null;
        }

        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setQuantity(request.getQuantity());
        existingProduct.setCategory(request.getCategory());

        return productRepository.save(existingProduct);
    }

    // DELETE
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}