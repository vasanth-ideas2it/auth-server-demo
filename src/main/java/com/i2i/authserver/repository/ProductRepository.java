package com.i2i.authserver.repository;

import com.i2i.authserver.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
} 