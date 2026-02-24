package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.entity.Product;
import com.mts.mts_purchase_service.entity.ProductType;
import com.mts.mts_purchase_service.exception.DuplicateResourceException;
import com.mts.mts_purchase_service.exception.ResourceNotFoundException;
import com.mts.mts_purchase_service.models.ProductDTO;
import com.mts.mts_purchase_service.models.ProductVariantDTO;
import com.mts.mts_purchase_service.repository.ProductRepository;
import com.mts.mts_purchase_service.repository.ProductTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles product master operations and optional nested variant creation flow.
 */
@Service
public class ProductServiceImpl implements ProductService {

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

        return productRepository.search(typeId, normalizedSearch, includeInactive).stream()
                .map(p -> mapToDTO(
                        p,
                        includeVariants
                                ? productVariantService.getVariantsByProduct(p.getProductId(), false)
                                : new ArrayList<>()
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
