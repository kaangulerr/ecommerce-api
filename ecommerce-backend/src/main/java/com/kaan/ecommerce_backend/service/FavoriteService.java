package com.kaan.ecommerce_backend.service;

import com.kaan.ecommerce_backend.dto.FavoriteDto;
import com.kaan.ecommerce_backend.entity.Favorite;
import com.kaan.ecommerce_backend.entity.Product;
import com.kaan.ecommerce_backend.entity.User;
import com.kaan.ecommerce_backend.repository.FavoriteRepository;
import com.kaan.ecommerce_backend.repository.ProductRepository;
import com.kaan.ecommerce_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public String toggleFavorite(String email, String asin) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        Product product = productRepository.findByAsin(asin)
                .orElseThrow(() -> new RuntimeException("Product not found!"));

        Optional<Favorite> existingFavorite = favoriteRepository.findByUserAndProduct(user, product);

        if (existingFavorite.isPresent()) {
            favoriteRepository.delete(existingFavorite.get());
            return "Product removed from favorites.";
        } else {
            Favorite newFavorite = new Favorite();
            newFavorite.setUser(user);
            newFavorite.setProduct(product);
            favoriteRepository.save(newFavorite);
            return "Product added to favorites.";
        }
    }

    public List<FavoriteDto> getUserFavorites(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        List<Favorite> favorites = favoriteRepository.findByUser(user);

        return favorites.stream().map(fav -> {
            FavoriteDto dto = new FavoriteDto();
            dto.setFavoriteId(fav.getId());
            dto.setProductAsin(fav.getProduct().getAsin());
            dto.setTitle(fav.getProduct().getTitle());
            dto.setPriceValue(fav.getProduct().getPriceValue());

            String images = fav.getProduct().getAllImages();
            if (images != null && !images.isEmpty()) {
                String cleanStr = images.replace("['", "").replace("']", "").replace("'", "");
                String[] imageArray = cleanStr.split(",");
                dto.setImageUrl(imageArray[0].trim());
            }

            return dto;
        }).collect(Collectors.toList());
    }
}