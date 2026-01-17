package com.cinego.server.domain.promotion.service;

import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.common.exception.BadRequestException;
import com.cinego.server.common.exception.ConflictException;
import com.cinego.server.common.exception.ResourceNotFoundException;
import com.cinego.server.common.util.PageUtil;
import com.cinego.server.domain.promotion.dto.CreatePromotionRequest;
import com.cinego.server.domain.promotion.dto.PromotionDTO;
import com.cinego.server.domain.promotion.dto.PromotionSearchRequest;
import com.cinego.server.domain.promotion.dto.UpdatePromotionRequest;
import com.cinego.server.domain.promotion.entity.Promotion;
import com.cinego.server.domain.promotion.mapper.PromotionMapper;
import com.cinego.server.domain.promotion.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;

    public PromotionDTO createPromotion(CreatePromotionRequest request) {
        log.info("Creating promotion with code: {}", request.getCode());

        // Kiểm tra code đã tồn tại
        if (promotionRepository.findByCode(request.getCode()).isPresent()) {
            throw new ConflictException("Mã khuyến mãi đã tồn tại");
        }

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        // Validate discount value
        if (request.getDiscountType().equals("PERCENTAGE") && 
            request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException("Phần trăm giảm giá không được vượt quá 100%");
        }

        // Tạo promotion
        Promotion promotion = new Promotion();
        promotion.setCode(request.getCode().toUpperCase());
        promotion.setName(request.getName());
        promotion.setDescription(request.getDescription());
        promotion.setDiscountType(Promotion.DiscountType.valueOf(request.getDiscountType().toUpperCase()));
        promotion.setDiscountValue(request.getDiscountValue());
        promotion.setMinPurchaseAmount(request.getMinPurchaseAmount());
        promotion.setMaxDiscountAmount(request.getMaxDiscountAmount());
        promotion.setUsageLimit(request.getUsageLimit());
        promotion.setUsedCount(0);
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setIsActive(true);

        // Convert applicable movies list to array
        if (request.getApplicableMovies() != null && !request.getApplicableMovies().isEmpty()) {
            promotion.setApplicableMovies(request.getApplicableMovies().toArray(new UUID[0]));
        }

        LocalDateTime now = LocalDateTime.now();
        promotion.setCreatedAt(now);
        promotion.setUpdatedAt(now);

        Promotion savedPromotion = promotionRepository.save(promotion);
        promotionRepository.flush();

        log.info("Promotion created successfully with id: {}", savedPromotion.getId());
        return promotionMapper.toDTO(savedPromotion);
    }

    @Transactional(readOnly = true)
    public PromotionDTO getPromotionById(UUID id) {
        log.info("Getting promotion by id: {}", id);
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));
        return promotionMapper.toDTO(promotion);
    }

    @Transactional(readOnly = true)
    public PromotionDTO getPromotionByCode(String code) {
        log.info("Getting promotion by code: {}", code);
        Promotion promotion = promotionRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "code", code));
        return promotionMapper.toDTO(promotion);
    }

    @Transactional(readOnly = true)
    public List<PromotionDTO> getAllActivePromotions() {
        log.info("Getting all active promotions");
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotions = promotionRepository.findAll().stream()
                .filter(p -> p.getIsActive() &&
                        p.getStartDate().isBefore(now) &&
                        p.getEndDate().isAfter(now) &&
                        (p.getUsageLimit() == null || p.getUsedCount() < p.getUsageLimit()))
                .collect(Collectors.toList());
        return promotions.stream()
                .map(promotionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<PromotionDTO> searchPromotions(PromotionSearchRequest request, int page, int size, String sortBy, String sortDirection) {
        log.info("Searching promotions with filters");

        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Specification<Promotion> spec = buildSpecification(request);

        Page<Promotion> promotionPage = promotionRepository.findAll(spec, pageable);
        List<PromotionDTO> content = promotionPage.getContent().stream()
                .map(promotionMapper::toDTO)
                .collect(Collectors.toList());

        return PageResponse.<PromotionDTO>builder()
                .content(content)
                .page(promotionPage.getNumber())
                .size(promotionPage.getSize())
                .totalElements(promotionPage.getTotalElements())
                .totalPages(promotionPage.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<PromotionDTO> getAllPromotions(int page, int size, String sortBy, String sortDirection) {
        log.info("Getting all promotions with pagination - page: {}, size: {}", page, size);
        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<Promotion> promotionPage = promotionRepository.findAll(pageable);
        return PageUtil.toPageResponse(promotionPage.map(promotionMapper::toDTO));
    }

    public PromotionDTO updatePromotion(UUID id, UpdatePromotionRequest request) {
        log.info("Updating promotion with id: {}", id);
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));

        // Cập nhật name
        if (request.getName() != null && !request.getName().isBlank()) {
            promotion.setName(request.getName());
        }

        // Cập nhật description
        if (request.getDescription() != null) {
            promotion.setDescription(request.getDescription());
        }

        // Cập nhật discount value
        if (request.getDiscountValue() != null) {
            if (promotion.getDiscountType() == Promotion.DiscountType.PERCENTAGE &&
                request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BadRequestException("Phần trăm giảm giá không được vượt quá 100%");
            }
            promotion.setDiscountValue(request.getDiscountValue());
        }

        // Cập nhật min purchase amount
        if (request.getMinPurchaseAmount() != null) {
            promotion.setMinPurchaseAmount(request.getMinPurchaseAmount());
        }

        // Cập nhật max discount amount
        if (request.getMaxDiscountAmount() != null) {
            promotion.setMaxDiscountAmount(request.getMaxDiscountAmount());
        }

        // Cập nhật usage limit
        if (request.getUsageLimit() != null) {
            if (request.getUsageLimit() < promotion.getUsedCount()) {
                throw new BadRequestException("Giới hạn sử dụng không được nhỏ hơn số lần đã sử dụng");
            }
            promotion.setUsageLimit(request.getUsageLimit());
        }

        // Cập nhật dates
        if (request.getStartDate() != null) {
            promotion.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            if (promotion.getStartDate() != null && request.getEndDate().isBefore(promotion.getStartDate())) {
                throw new BadRequestException("Ngày kết thúc phải sau ngày bắt đầu");
            }
            promotion.setEndDate(request.getEndDate());
        }

        // Cập nhật isActive
        if (request.getIsActive() != null) {
            promotion.setIsActive(request.getIsActive());
        }

        // Cập nhật applicable movies
        if (request.getApplicableMovies() != null) {
            if (request.getApplicableMovies().isEmpty()) {
                promotion.setApplicableMovies(null);
            } else {
                promotion.setApplicableMovies(request.getApplicableMovies().toArray(new UUID[0]));
            }
        }

        promotion.setUpdatedAt(LocalDateTime.now());
        Promotion updatedPromotion = promotionRepository.save(promotion);
        promotionRepository.flush();

        log.info("Promotion updated successfully with id: {}", updatedPromotion.getId());
        return promotionMapper.toDTO(updatedPromotion);
    }

    public void deletePromotion(UUID id) {
        log.info("Deleting promotion with id: {}", id);
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));

        // Kiểm tra xem có booking nào đang dùng promotion này không
        if (!promotion.getBookingPromotions().isEmpty()) {
            throw new ConflictException("Không thể xóa khuyến mãi đang được sử dụng");
        }

        promotionRepository.delete(promotion);
        promotionRepository.flush();
        log.info("Promotion deleted successfully with id: {}", id);
    }

    private Specification<Promotion> buildSpecification(PromotionSearchRequest request) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            if (request.getCode() != null && !request.getCode().isBlank()) {
                predicates.add(cb.like(cb.upper(root.get("code")), 
                        "%" + request.getCode().toUpperCase() + "%"));
            }

            if (request.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), request.getIsActive()));
            }

            if (request.getMovieId() != null) {
                // Tìm promotion có applicableMovies chứa movieId hoặc null (áp dụng cho tất cả)
                var movieIdPredicate = cb.or(
                        cb.isNull(root.get("applicableMovies")),
                        cb.isMember(request.getMovieId(), root.get("applicableMovies"))
                );
                predicates.add(movieIdPredicate);
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
