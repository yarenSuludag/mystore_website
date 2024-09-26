package com.bmt.MyStore.controllers;

import com.bmt.MyStore.models.Product;
import com.bmt.MyStore.models.ProductDto;
import com.bmt.MyStore.services.ProductsRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.*;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductsController {

  @Autowired
  private ProductsRepository repo;

@GetMapping({"","/"})
  public String showProductList(Model model){
      List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC,"id"));
      model.addAttribute("products", products);
    // Kategori, renk ve türlerin listelenmesi
    List<String> categories = repo.findAllCategories();
    List<String> colorVariants = repo.findAllColorVariants();
    List<String> brands = repo.findAllBrands();

    model.addAttribute("categories", categories);
    model.addAttribute("colorVariants", colorVariants);
    model.addAttribute("brands", brands);
      return "products/index";
  }


  @GetMapping("/create")
    public String  showCreatePage(Model model) {
      ProductDto productDto = new ProductDto();
      model.addAttribute("productDto", productDto);
      return "products/CreateProduct";

  }

@PostMapping("/create")
    public String createProduct (
            @Valid @ModelAttribute ProductDto productDto,
            BindingResult result
)
{
    if(productDto.getImageFile().isEmpty()){
        result.addError(new FieldError("productDto","imageFile","The image file is required"));

    }

    if(result.hasErrors()){
        return "products/CreateProduct";
    }

    //save image file
    MultipartFile image = productDto.getImageFile();
    Date createdAt = new Date();
    String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();


    try {
        String uploadDir = "public/images/";
        Path uploadPath = (Path) Paths.get(uploadDir);

        if (!Files.exists((java.nio.file.Path) uploadPath)) {
            Files.createDirectories((java.nio.file.Path) uploadPath);
        }

        try (InputStream inputStream = image.getInputStream()) {
            Files.copy(inputStream, ((java.nio.file.Path) uploadPath).resolve(storageFileName), StandardCopyOption.REPLACE_EXISTING);
        }
    } catch (Exception ex) {
        System.out.println("Exception: " + ex.getMessage());
    }


    Product product = new Product();
    product.setName(productDto.getName());
    product.setBrand(productDto.getBrand());
    product.setCategory(productDto.getCategory());
    product.setPrice(productDto.getPrice());
    product.setDescription(productDto.getDescription());
    product.setCreatedAt(createdAt);
    product.setImageFileName(storageFileName);
    product.setStock(productDto.getStock());  // Stok bilgisi eklendi
    product.setColorVariants(productDto.getColorVariants());  // Renk çeşitleri eklendi



    repo.save(product);


    return "redirect:/products";
}

@GetMapping("/edit")
public String showEditPage(
        Model model,
        @RequestParam int id
){
    try {

Product product = repo.findById(id).get();
model.addAttribute("product", product);

ProductDto productDto =  new ProductDto();
productDto.setName(product.getName());
productDto.setBrand(product.getBrand());
productDto.setCategory(product.getCategory());
productDto.setPrice(product.getPrice());
productDto.setDescription(product.getDescription());
product.setStock(productDto.getStock());  // Stok bilgisi güncellendi
product.setColorVariants(productDto.getColorVariants());  // Renk çeşitleri güncellendi



model.addAttribute("productDto", productDto);

    }
catch(Exception ex){
        System.out.println("Exception: " + ex.getMessage());
        return "redirect:/products";
}

return "products/EditProduct";

}

@PostMapping("/edit")
    public String updateProduct(
            Model model,
            @RequestParam int id,
            @Valid @ModelAttribute ProductDto productDto,
            BindingResult result
){
    try {

        Product product = repo.findById(id).get();
        model.addAttribute("product",product);

        if(result.hasErrors()){
            return "products/EditProduct";
        }

        if(!productDto.getImageFile().isEmpty()) {
            //delete old image
            String uploadDir = "public/images/";
            Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());

            try{
                Files.delete(oldImagePath);
            }
            catch(Exception ex){
                System.out.println("Exception: " + ex.getMessage());
            }

            //save new file
            MultipartFile image = productDto.getImageFile();
            Date createdAt = new Date();
            String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

            try(InputStream inputStream = image.getInputStream()){
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                  StandardCopyOption.REPLACE_EXISTING);
            }

            product.setImageFileName(storageFileName);
        }

        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setStock(productDto.getStock());  // Stok bilgisi güncellendi
        product.setColorVariants(productDto.getColorVariants());  // Renk çeşitleri güncellendi


        repo.save(product);

    }
    catch (Exception ex){
        System.out.println("Exception: " + ex.getMessage());
    }



    return "redirect:/products";
}

@GetMapping("/delete")
public String deleteProduct (
        @RequestParam int id
)    {

    try {
        Product product = repo.findById(id).get();

        //delete product image
        Path imagePath = Paths.get("public/images/" + product.getImageFileName());

        try {
            Files.delete(imagePath);
        }
        catch (Exception ex){
            System.out.println("Exception: " + ex.getMessage());
        }

        repo.delete(product);

    }
    catch (Exception ex) {
        System.out.println("Exception: " + ex.getMessage());
    }

    return "redirect:/products";
}

    // Ürün arama ve filtreleme
    @GetMapping("/search")
    public String searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String colorVariants,
            @RequestParam(required = false) Integer stock,
            Model model) {

        Specification<Product> spec = (root, query, criteriaBuilder) -> {
            if (name != null && !name.isEmpty()) {
                return criteriaBuilder.like(root.get("name"), "%" + name + "%");
            } else if (brand != null && !brand.isEmpty()) {
                return criteriaBuilder.like(root.get("brand"), "%" + brand + "%");
            } else if (category != null && !category.isEmpty()) {
                return criteriaBuilder.like(root.get("category"), "%" + category + "%");
            } else if (colorVariants != null && !colorVariants.isEmpty()) {
                return criteriaBuilder.like(root.get("colorVariants"), "%" + colorVariants + "%");
            } else if (stock != null) {
                return criteriaBuilder.equal(root.get("stock"), stock);
            }
            return criteriaBuilder.conjunction();
        };

        List<Product> products = repo.findAll(spec);
        model.addAttribute("products", products);

        // Kategori, renk ve türlerin listelenmesi
        List<String> categories = repo.findAllCategories();
        List<String> colorVariantsList = repo.findAllColorVariants();
        List<String> brands = repo.findAllBrands();

        model.addAttribute("categories", categories);
        model.addAttribute("colorVariants", colorVariantsList);
        model.addAttribute("brands", brands);

        return "products/index";
    }
    // Ürün sıralama
    @GetMapping("/sort")
    public String sortProducts(
            @RequestParam String field,
            @RequestParam(required = false, defaultValue = "ASC") String direction,
            Model model) {

        Sort sort = Sort.by(Sort.Direction.fromString(direction), field);
        List<Product> products = repo.findAll(sort);
        model.addAttribute("products", products);
        return "products/index";
    }

    // Ürün fiyat ve stok filtreleme
    @GetMapping("/filter")
    public String filterProducts(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Integer maxStock,
            Model model) {

        Specification<Product> spec = (root, query, criteriaBuilder) -> {
            if (minPrice != null && maxPrice != null) {
                return criteriaBuilder.between(root.get("price"), minPrice, maxPrice);
            } else if (minPrice != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
            } else if (maxPrice != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
            } else if (minStock != null && maxStock != null) {
                return criteriaBuilder.between(root.get("stock"), minStock, maxStock);
            } else if (minStock != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("stock"), minStock);
            } else if (maxStock != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("stock"), maxStock);
            }
            return criteriaBuilder.conjunction();
        };

        List<Product> products = repo.findAll(spec);
        model.addAttribute("products", products);
        return "products/index";
    }



}
