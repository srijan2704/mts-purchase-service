package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.entity.ProductType;
import com.mts.mts_purchase_service.exception.DuplicateResourceException;
import com.mts.mts_purchase_service.exception.ResourceNotFoundException;
import com.mts.mts_purchase_service.models.ProductTypeDTO;
import com.mts.mts_purchase_service.repository.ProductTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for product type master data.
 */
@Service
public class ProductTypeServiceImpl implements ProductTypeService {

    private final ProductTypeRepository productTypeRepository;

    /** Injects repository dependency for product type operations. */
    public ProductTypeServiceImpl(ProductTypeRepository productTypeRepository) {
        this.productTypeRepository = productTypeRepository;
    }

    /** Returns all product types sorted by name. */
    @Override
    @Transactional(readOnly = true)
    public List<ProductTypeDTO> getAllProductTypes() {
        return productTypeRepository.findAllByOrderByTypeNameAsc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /** Returns one product type by id. */
    @Override
    @Transactional(readOnly = true)
    public ProductTypeDTO getProductTypeById(Long id) {
        ProductType productType = productTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductType", id));
        return mapToDTO(productType);
    }

    /** Creates a new product type after duplicate-name validation. */
    @Override
    @Transactional
    public ProductTypeDTO createProductType(ProductTypeDTO productTypeDTO) {
        boolean exists = productTypeRepository.existsByTypeNameIgnoreCase(productTypeDTO.getTypeName());
        if (exists) {
            throw new DuplicateResourceException("Product type with name '" + productTypeDTO.getTypeName() + "' already exists");
        }

        ProductType saved = productTypeRepository.save(mapToEntity(productTypeDTO));
        return mapToDTO(saved);
    }

    /** Updates existing product type after duplicate-name validation. */
    @Override
    @Transactional
    public ProductTypeDTO updateProductType(Long id, ProductTypeDTO productTypeDTO) {
        ProductType existing = productTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductType", id));

        boolean clash = productTypeRepository.existsByTypeNameIgnoreCaseAndIdNot(productTypeDTO.getTypeName(), id);
        if (clash) {
            throw new DuplicateResourceException("Another product type with name '" + productTypeDTO.getTypeName() + "' already exists");
        }

        existing.setTypeName(productTypeDTO.getTypeName());
        existing.setDescription(productTypeDTO.getDescription());

        ProductType saved = productTypeRepository.save(existing);
        return mapToDTO(saved);
    }

    /** Deletes product type by id after existence validation. */
    @Override
    @Transactional
    public void deleteProductType(Long id) {
        if (!productTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("ProductType", id);
        }
        productTypeRepository.deleteById(id);
    }

    /** Maps entity to API DTO. */
    private ProductTypeDTO mapToDTO(ProductType productType) {
        return new ProductTypeDTO(productType.getTypeId(), productType.getTypeName(), productType.getDescription());
    }

    /** Maps create/update request DTO to entity. */
    private ProductType mapToEntity(ProductTypeDTO dto) {
        ProductType productType = new ProductType();
        productType.setTypeName(dto.getTypeName());
        productType.setDescription(dto.getDescription());
        return productType;
    }
}
