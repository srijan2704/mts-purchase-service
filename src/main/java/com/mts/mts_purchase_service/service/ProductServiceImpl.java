package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.entity.Product;
import com.mts.mts_purchase_service.entity.ProductType;
import com.mts.mts_purchase_service.exception.DuplicateResourceException;
import com.mts.mts_purchase_service.exception.ResourceNotFoundException;
import com.mts.mts_purchase_service.models.ProductDTO;
import com.mts.mts_purchase_service.models.ProductVariantDTO;
import com.mts.mts_purchase_service.repository.ProductRepository;
import com.mts.mts_purchase_service.repository.ProductTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles product master operations and optional nested variant creation flow.
 */
@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;
    private final ProductVariantService productVariantService;

    public ProductServiceImpl(ProductRepository productRepository,
                              ProductTypeRepository productTypeRepository,
                              ProductVariantService productVariantService) {
        this.productRepository = productRepository;
        this.productTypeRepository = productTypeRepository;
        this.productVariantService = productVariantService;
    }

    /**
     * Returns products using optional filter params and includeInactive toggle.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProducts(Long typeId, String search, boolean includeInactive, boolean includeVariants) {
        String normalizedSearch = (search == null || search.isBlank()) ? null : search.trim();

        long productsQueryStartNanos = System.nanoTime();
        List<Product> products = productRepository.search(typeId, normalizedSearch, includeInactive);
        long productsQueryMs = (System.nanoTime() - productsQueryStartNanos) / 1_000_000;

        Map<Long, List<ProductVariantDTO>> variantsByProductMutable = Collections.emptyMap();
        long variantsQueryMs = 0L;
        if (includeVariants && !products.isEmpty()) {
            List<Long> productIds = products.stream().map(Product::getProductId).collect(Collectors.toList());

            long variantsQueryStartNanos = System.nanoTime();
            variantsByProductMutable = productVariantService.getVariantsByProductIds(productIds, false).stream()
                    .collect(Collectors.groupingBy(ProductVariantDTO::getProductId));
            variantsQueryMs = (System.nanoTime() - variantsQueryStartNanos) / 1_000_000;
        }
        final Map<Long, List<ProductVariantDTO>> variantsByProduct = variantsByProductMutable;

        if (includeVariants) {
            int variantCount = variantsByProduct.values().stream().mapToInt(List::size).sum();
            log.info(
                    "ORACLE_DB_TIME endpoint=/api/products includeVariants=true productsQueryMs={} variantsQueryMs={} totalDbMs={} productCount={} variantCount={} typeId={} search={} includeInactive={}",
                    productsQueryMs,
                    variantsQueryMs,
                    productsQueryMs + variantsQueryMs,
                    products.size(),
                    variantCount,
                    typeId,
                    normalizedSearch,
                    includeInactive
            );
        }

        return products.stream()
                .map(p -> mapToDTO(
                        p,
                        includeVariants
                                ? variantsByProduct.getOrDefault(p.getProductId(), Collections.emptyList())
                                : Collections.emptyList()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Returns one product with active variants to support product detail usage.
     */
    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        List<ProductVariantDTO> variants = productVariantService.getVariantsByProduct(id, false);
        return mapToDTO(product, variants);
    }

    /**
     * Creates product and optionally persists initial variants in same transaction.
     */
    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        boolean duplicateName = productRepository.existsByNameIgnoreCase(productDTO.getProductName());
        if (duplicateName) {
            throw new DuplicateResourceException("Product with name '" + productDTO.getProductName() + "' already exists");
        }

        ProductType type = productTypeRepository.findById(productDTO.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductType", productDTO.getTypeId()));

        Product product = new Product();
        product.setProductName(productDTO.getProductName());
        product.setType(type);
        product.setDescription(productDTO.getDescription());
        product.setActive(true);

        Product saved = productRepository.save(product);

        List<ProductVariantDTO> savedVariants = new ArrayList<>();
        // Save optional initial variants after product id is available.
        if (productDTO.getVariants() != null && !productDTO.getVariants().isEmpty()) {
            for (ProductVariantDTO variant : productDTO.getVariants()) {
                savedVariants.add(productVariantService.addVariant(saved.getProductId(), variant));
            }
        }

        return mapToDTO(saved, savedVariants);
    }

    /**
     * Updates product-level fields and validates uniqueness/type constraints.
     */
    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        boolean duplicateName = productRepository.existsByNameIgnoreCaseAndIdNot(productDTO.getProductName(), id);
        if (duplicateName) {
            throw new DuplicateResourceException("Another product with name '" + productDTO.getProductName() + "' already exists");
        }

        ProductType type = productTypeRepository.findById(productDTO.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductType", productDTO.getTypeId()));

        existing.setProductName(productDTO.getProductName());
        existing.setType(type);
        existing.setDescription(productDTO.getDescription());

        Product saved = productRepository.save(existing);
        List<ProductVariantDTO> variants = productVariantService.getVariantsByProduct(id, false);
        return mapToDTO(saved, variants);
    }

    /**
     * Soft-deletes product by flipping active flag.
     */
    @Override
    @Transactional
    public void softDeleteProduct(Long id) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        existing.setActive(false);
        productRepository.save(existing);
    }

    /** Maps entity + variant list into response DTO. */
    private ProductDTO mapToDTO(Product product, List<ProductVariantDTO> variants) {
        return new ProductDTO(
                product.getProductId(),
                product.getProductName(),
                product.getType().getTypeId(),
                product.getType().getTypeName(),
                product.getDescription(),
                product.isActive(),
                product.getCreatedAt(),
                variants
        );
    }
}
