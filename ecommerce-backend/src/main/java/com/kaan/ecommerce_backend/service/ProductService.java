package com.kaan.ecommerce_backend.service;

import com.kaan.ecommerce_backend.dto.AdminProductDto;
import com.kaan.ecommerce_backend.dto.ProductDto;
import com.kaan.ecommerce_backend.dto.ProductDetailDto;
import com.kaan.ecommerce_backend.dto.ProductUploadRequestDto;
import com.kaan.ecommerce_backend.dto.ProductUpdateDto;
import com.kaan.ecommerce_backend.dto.ReviewDto;

import com.kaan.ecommerce_backend.entity.Product;
import com.kaan.ecommerce_backend.entity.ProductImage;
import com.kaan.ecommerce_backend.entity.User;
import com.kaan.ecommerce_backend.repository.ProductRepository;
import com.kaan.ecommerce_backend.repository.FavoriteRepository;
import com.kaan.ecommerce_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository,
                          FavoriteRepository favoriteRepository,
                          UserRepository userRepository) {
        this.productRepository = productRepository;
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
    }

    private List<String> parseImages(String imagesStr) {
        if (imagesStr == null || imagesStr.isBlank()) return List.of();
        String cleanStr = imagesStr.trim();
        if (cleanStr.startsWith("['") && cleanStr.endsWith("']")) {
            cleanStr = cleanStr.substring(2, cleanStr.length() - 2);
        } else if (cleanStr.startsWith("[") && cleanStr.endsWith("]")) {
            cleanStr = cleanStr.substring(1, cleanStr.length() - 1);
        }
        return Arrays.stream(cleanStr.split("', '"))
                .map(String::trim)
                .map(s -> s.replace("'", "").replace("\"", ""))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public List<ProductDto> getAllProducts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return productRepository.findAll(pageable).stream().map(product -> {
            ProductDto dto = new ProductDto();
            dto.setAsin(product.getAsin());
            dto.setTitle(product.getTitle());
            dto.setPriceValue(product.getPriceValue());
            dto.setBrandName(product.getBrandName());
            dto.setRatingStars(product.getRatingStars());
            dto.setRatingCount(product.getRatingCount());
            dto.setAllImages(parseImages(product.getAllImages()));

            dto.setDefaultVariant0(product.getDefaultVariant0());
            dto.setDefaultVariant1(product.getDefaultVariant1());

            if (userId != null) {
                dto.setFavorite(favoriteRepository.checkFavorite(userId, product.getAsin()));
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public List<ProductDto> filterProducts(String mainCategory, String subCategory, String brand, String color, String size, String minRating, Long userId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);

        return productRepository.filterProducts(mainCategory, subCategory, brand, color, size, minRating, pageable).stream().map(product -> {
            ProductDto dto = new ProductDto();
            dto.setAsin(product.getAsin());
            dto.setTitle(product.getTitle());
            dto.setPriceValue(product.getPriceValue());
            dto.setBrandName(product.getBrandName());
            dto.setRatingStars(product.getRatingStars());
            dto.setRatingCount(product.getRatingCount());
            dto.setAllImages(parseImages(product.getAllImages()));

            dto.setDefaultVariant0(product.getDefaultVariant0());
            dto.setDefaultVariant1(product.getDefaultVariant1());

            if (userId != null) {
                dto.setFavorite(favoriteRepository.checkFavorite(userId, product.getAsin()));
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public List<ProductDto> searchProducts(String keyword, Long userId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);

        return productRepository.searchProductsByKeyword(keyword, pageable).stream().map(product -> {
            ProductDto dto = new ProductDto();
            dto.setAsin(product.getAsin());
            dto.setTitle(product.getTitle());
            dto.setPriceValue(product.getPriceValue());
            dto.setBrandName(product.getBrandName());
            dto.setRatingStars(product.getRatingStars());
            dto.setRatingCount(product.getRatingCount());
            dto.setAllImages(parseImages(product.getAllImages()));

            dto.setDefaultVariant0(product.getDefaultVariant0());
            dto.setDefaultVariant1(product.getDefaultVariant1());

            if (userId != null) {
                dto.setFavorite(favoriteRepository.checkFavorite(userId, product.getAsin()));
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public ProductDetailDto getProductDetail(String asin, Long userId) {
        Product product = productRepository.findById(asin)
                .orElseThrow(() -> new RuntimeException("Product not found: " + asin));

        ProductDetailDto dto = new ProductDetailDto();
        dto.setAsin(product.getAsin());
        dto.setTitle(product.getTitle());
        dto.setPriceValue(product.getPriceValue());
        dto.setBrandName(product.getBrandName());
        dto.setRatingStars(product.getRatingStars());
        dto.setRatingCount(product.getRatingCount());
        dto.setAllImages(parseImages(product.getAllImages()));

        dto.setAboutItem(product.getAboutItem());
        dto.setAvailability(product.getAvailability());
        dto.setBreadcrumbs(product.getBreadcrumbs());
        dto.setCustomerReviewSummary(product.getCustomerReviewSummary());
        dto.setDeliveryDate(product.getDeliveryDate());
        dto.setFastestDeliveryDate(product.getFastestDeliveryDate());
        dto.setSellerName(product.getSellerName());
        dto.setRatingDistribution1star(product.getRatingDistribution1star());
        dto.setRatingDistribution2star(product.getRatingDistribution2star());
        dto.setRatingDistribution3star(product.getRatingDistribution3star());
        dto.setRatingDistribution4star(product.getRatingDistribution4star());
        dto.setRatingDistribution5star(product.getRatingDistribution5star());

        if (userId != null) {
            boolean isFav = favoriteRepository.checkFavorite(userId, asin);
            dto.setFavorite(isFav);
        } else {
            dto.setFavorite(false);
        }

        return dto;
    }

    public List<ReviewDto> getReviewsByAsin(String asin) {
        Product product = productRepository.findById(asin)
                .orElseThrow(() -> new RuntimeException("Product not found: " + asin));

        if (product.getReviews() == null) return List.of();

        return product.getReviews().stream().map(review -> {
            ReviewDto dto = new ReviewDto();
            dto.setId(review.getId());
            dto.setReviewTitle(review.getReviewtitle());
            dto.setReviewText(review.getReviewtext());
            dto.setRating(review.getRating());
            dto.setVerifiedPurchase(review.getVerifiedpurchase());
            dto.setReviewMetadata(review.getReviewmetadata());
            
            if (review.getUser() != null) {
                dto.setUserName(review.getUser().getFirstName() + " " + review.getUser().getLastName());
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

    public String categorizeExistingProducts() {
        List<Product> products = productRepository.findAll();
        int updatedCount = 0;
        for (Product product : products) {
            if (product.getCategory() == null && product.getBreadcrumbs() != null) {
                String breadcrumbs = product.getBreadcrumbs().toLowerCase();
                if (breadcrumbs.contains("women") || breadcrumbs.contains("girls")) product.setCategory("WOMEN");
                else if (breadcrumbs.contains("men") || breadcrumbs.contains("boys")) product.setCategory("MEN");
                else product.setCategory("OTHER");
                updatedCount++;
            }
        }
        productRepository.saveAll(products);
        return updatedCount + " products successfully categorized!";
    }


    /**
     * Satıcının yeni ürün yüklemesi.
     * ASIN otomatik UUID ile üretilir, ürün satıcıyla ilişkilendirilir.
     */
    @Transactional
    public ProductDto uploadProduct(ProductUploadRequestDto request, Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found: " + sellerId));

        Product product = new Product();
        product.setAsin("SLR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        product.setTitle(request.getTitle());
        product.setBrandName(request.getBrandName());
        product.setPriceValue(request.getPriceValue());
        product.setAboutItem(request.getAboutItem());
        product.setCategory(request.getCategory());
        product.setAvailability(request.getAvailability() != null ? request.getAvailability() : "In Stock");
        product.setStock(request.getStock() != null ? request.getStock() : 100);
        product.setDeliveryDate(request.getDeliveryDate());
        product.setFastestDeliveryDate(request.getFastestDeliveryDate());
        product.setSellerName(seller.getFirstName() + " " + seller.getLastName());

        product.setOwner(seller);

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ProductImage> images = new ArrayList<>();
            for (String imageUrl : request.getImageUrls()) {
                ProductImage img = new ProductImage();
                img.setImageUrl(imageUrl);
                img.setProduct(product);
                images.add(img);
            }
            product.setImages(images);

            product.setAllImages(request.getImageUrls().toString());
        }

        Product savedProduct = productRepository.save(product);

        ProductDto dto = new ProductDto();
        dto.setAsin(savedProduct.getAsin());
        dto.setTitle(savedProduct.getTitle());
        dto.setPriceValue(savedProduct.getPriceValue());
        dto.setBrandName(savedProduct.getBrandName());
        dto.setRatingStars(savedProduct.getRatingStars());
        dto.setRatingCount(savedProduct.getRatingCount());
        dto.setAllImages(parseImages(savedProduct.getAllImages()));
        dto.setDefaultVariant0(savedProduct.getDefaultVariant0());
        dto.setDefaultVariant1(savedProduct.getDefaultVariant1());
        dto.setFavorite(false);

        return dto;
    }


    public List<ProductDto> getSellerProducts(Long sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return productRepository.findByOwnerId(sellerId, pageable).stream().map(product -> {
            ProductDto dto = new ProductDto();
            dto.setAsin(product.getAsin());
            dto.setTitle(product.getTitle());
            dto.setPriceValue(product.getPriceValue());
            dto.setBrandName(product.getBrandName());
            dto.setRatingStars(product.getRatingStars());
            dto.setRatingCount(product.getRatingCount());
            dto.setAllImages(parseImages(product.getAllImages()));
            dto.setDefaultVariant0(product.getDefaultVariant0());
            dto.setDefaultVariant1(product.getDefaultVariant1());
            dto.setFavorite(false);
            return dto;
        }).collect(Collectors.toList());
    }


    /**
     * Admin dashboard: Tüm ürünleri satıcı bilgileriyle döner.
     * Opsiyonel sellerId veya sellerName parametresiyle filtreleme yapılabilir.
     * sellerName kısmi eşleşme destekler (örn. "Zappos" -> firstName veya lastName "Zappos" içeren satıcılar).
     */
    public List<AdminProductDto> getAllProductsForAdmin(Long sellerId, String sellerName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        List<Product> products;
        if (sellerName != null && !sellerName.isBlank()) {
            products = productRepository.findByOwnerNameContaining(sellerName.trim(), pageable);
        } else if (sellerId != null) {
            products = productRepository.findAllBySellerId(sellerId, pageable);
        } else {
            products = productRepository.findAllWithOwner(pageable);
        }

        return products.stream().map(product -> {
            AdminProductDto dto = new AdminProductDto();
            dto.setAsin(product.getAsin());
            dto.setTitle(product.getTitle());
            dto.setBrandName(product.getBrandName());
            dto.setPriceValue(product.getPriceValue());
            dto.setCategory(product.getCategory());
            dto.setRatingStars(product.getRatingStars());
            dto.setRatingCount(product.getRatingCount());
            dto.setAllImages(parseImages(product.getAllImages()));

            User owner = product.getOwner();
            if (owner != null) {
                dto.setOwnerId(owner.getId());
                dto.setOwnerEmail(owner.getEmail());
                dto.setOwnerFullName(owner.getFirstName() + " " + owner.getLastName());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Satıcının kendi ürününü güncellemesi.
     * Güvenlik Kontrolü: Ürünün sahibi olmayan kişi güncelleyemez.
     */
    @Transactional
    public ProductDto updateProduct(String asin, ProductUpdateDto dto, Long sellerId) {
        Product product = productRepository.findByAsin(asin)
                .orElseThrow(() -> new RuntimeException("Product not found: " + asin));

        if (product.getOwner() == null || !product.getOwner().getId().equals(sellerId)) {
            throw new RuntimeException("You are not authorized to update this product!");
        }

        if (dto.getTitle() != null) product.setTitle(dto.getTitle());
        if (dto.getAboutItem() != null) product.setAboutItem(dto.getAboutItem());
        if (dto.getPriceValue() != null) product.setPriceValue(dto.getPriceValue());
        if (dto.getAvailability() != null) product.setAvailability(dto.getAvailability());
        if (dto.getStock() != null) product.setStock(dto.getStock());
        if (dto.getCategory() != null) product.setCategory(dto.getCategory());
        if (dto.getBrandName() != null) product.setBrandName(dto.getBrandName());

        if (dto.getImageUrls() != null) {
            product.getImages().clear();
            List<ProductImage> newImages = new ArrayList<>();
            for (String url : dto.getImageUrls()) {
                ProductImage img = new ProductImage();
                img.setImageUrl(url);
                img.setProduct(product);
                newImages.add(img);
            }
            product.setImages(newImages);
            product.setAllImages(dto.getImageUrls().toString());
        }

        Product savedProduct = productRepository.save(product);

        ProductDto responseDto = new ProductDto();
        responseDto.setAsin(savedProduct.getAsin());
        responseDto.setTitle(savedProduct.getTitle());
        responseDto.setPriceValue(savedProduct.getPriceValue());
        responseDto.setBrandName(savedProduct.getBrandName());
        responseDto.setRatingStars(savedProduct.getRatingStars());
        responseDto.setRatingCount(savedProduct.getRatingCount());
        responseDto.setAllImages(parseImages(savedProduct.getAllImages()));
        responseDto.setFavorite(false);

        return responseDto;
    }


    /**
     * Satıcının kendi ürününü silmesi.
     * Güvenlik Kontrolü: Ürünün sahibi olmayan kişi silemez.
     */
    @Transactional
    public void deleteProduct(String asin, Long sellerId) {
        Product product = productRepository.findByAsin(asin)
                .orElseThrow(() -> new RuntimeException("Product not found: " + asin));

        if (product.getOwner() == null || !product.getOwner().getId().equals(sellerId)) {
            throw new RuntimeException("You are not authorized to delete this product!");
        }

        productRepository.delete(product);
    }
}