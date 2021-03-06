package br.com.siecola.aws_project01.controller;

import br.com.siecola.aws_project01.entity.Product;
import br.com.siecola.aws_project01.entity.UrlResponse;
import br.com.siecola.aws_project01.enums.EventType;
import br.com.siecola.aws_project01.exceptions.ProductNotFoundException;
import br.com.siecola.aws_project01.repository.ProductRepository;
import br.com.siecola.aws_project01.service.ProductPublisherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductPublisherService productPublisherService;

    public ProductController(ProductRepository productRepository, ProductPublisherService productPublisherService) {
        this.productRepository = productRepository;
        this.productPublisherService = productPublisherService;
    }


    @GetMapping
    public Iterable<Product> findAll() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> findById(@PathVariable long id) {
        Optional<Product> optProduct = productRepository.findById(id);

        return optProduct
                .map(product -> new ResponseEntity<>(product, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Product> saveProduct(@RequestBody Product product) {
        Product productCreated = productRepository.save(product);

        productPublisherService.publishProductEvent(product, EventType.PRODUCT_CREATED, "Eric-create");

        return new ResponseEntity<Product>(productCreated,
                HttpStatus.CREATED);
    }

    //Maneira diferente de fazer retornando exceção e não um ResponseEntity
    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Product updateProduct(@RequestBody Product product, @PathVariable("id") long id) {
        if (productRepository.existsById(id)) {
            product.setId(id);

            Product productUpdated = productRepository.save(product);

            productPublisherService.publishProductEvent(productUpdated, EventType.PRODUCT_UPDATE, "Eric-update");

            return productUpdated;

        } else {
            throw new ProductNotFoundException(id);
        }
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Product> deleteProduct(@PathVariable("id") long id) {
        Optional<Product> optProduct = productRepository.findById(id);
        if (optProduct.isPresent()) {
            Product product = optProduct.get();

            productRepository.delete(product);

            productPublisherService.publishProductEvent(product, EventType.PRODUCT_DELETED, "Eric-deleted");

            return new ResponseEntity<Product>(product, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(path = "/bycode")
    public ResponseEntity<Product> findByCode(@RequestParam String code) {
        Optional<Product> optProduct = productRepository.findByCode(code);
        if (optProduct.isPresent()) {
            return new ResponseEntity<Product>(optProduct.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
