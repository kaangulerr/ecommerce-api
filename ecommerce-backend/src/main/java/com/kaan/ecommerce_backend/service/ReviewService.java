package com.kaan.ecommerce_backend.service;

import com.kaan.ecommerce_backend.dto.ReviewDto;
import com.kaan.ecommerce_backend.dto.ReviewRequest;
import com.kaan.ecommerce_backend.entity.Product;
import com.kaan.ecommerce_backend.entity.ProductReview;
import com.kaan.ecommerce_backend.entity.User;
import com.kaan.ecommerce_backend.repository.ProductRepository;
import com.kaan.ecommerce_backend.repository.ProductReviewRepository;
import com.kaan.ecommerce_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ReviewService(ProductReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Belirli bir ürüne ait yorumları döner.
     * currentUserId null ise (anonim kullanıcı), createdByCurrentUser her zaman false olur.
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByAsin(String asin, Long currentUserId) {
        if (!productRepository.existsById(asin)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + asin);
        }

        return reviewRepository.findByProduct_Asin(asin).stream()
                .map(review -> toDto(review, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * Kullanıcının bir ürüne yorum eklemesini sağlar.
     */
    @Transactional
    public ReviewDto addReview(String asin, Long userId, ReviewRequest reviewRequest) {
        Product product = productRepository.findByAsin(asin)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + asin));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setUser(user);
        review.setProductasin(asin);
        review.setReviewtitle(reviewRequest.getReviewTitle());
        review.setReviewtext(reviewRequest.getReviewText());
        review.setRating(reviewRequest.getRating());
        review.setVerifiedpurchase("false");
        review.setHelpfulvotecount("0");
        review.setReviewid(java.util.UUID.randomUUID().toString());
        review.setReviewurl("");

        ProductReview savedReview = reviewRepository.save(review);
        return toDto(savedReview, userId);
    }



    private ReviewDto toDto(ProductReview review, Long currentUserId) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setReviewTitle(review.getReviewtitle());
        dto.setReviewText(review.getReviewtext());
        dto.setRating(review.getRating());
        dto.setVerifiedPurchase(review.getVerifiedpurchase());
        dto.setReviewMetadata(review.getReviewmetadata());

        if (review.getUser() != null) {
            dto.setUserName(review.getUser().getFirstName() + " " + review.getUser().getLastName());
            dto.setCreatedByCurrentUser(
                    currentUserId != null && currentUserId.equals(review.getUser().getId()));
        }

        return dto;
    }
}