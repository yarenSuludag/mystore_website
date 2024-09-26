package com.bmt.MyStore.services;

import com.bmt.MyStore.models.Product;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@EnableJpaRepositories
public interface ProductsRepository extends JpaRepository<Product, Integer> {


    List<Product> findByNameContaining(String name);
    List<Product> findByBrandContaining(String brand);
    List<Product> findByCategoryContaining(String category);
    @Query("SELECT DISTINCT p.category FROM Product p")
    List<String> findAllCategories();

    @Query("SELECT DISTINCT p.colorVariants FROM Product p")
    List<String> findAllColorVariants();

    @Query("SELECT DISTINCT p.brand FROM Product p")
    List<String> findAllBrands();

    List<Product> findAll(Specification<Product> spec);





}
