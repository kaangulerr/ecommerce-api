package com.kaan.ecommerce_backend.service;

import com.kaan.ecommerce_backend.entity.*;
import com.kaan.ecommerce_backend.repository.*;
import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class DataSeederService {

    private static final Logger logger = LoggerFactory.getLogger(DataSeederService.class);

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final ProductReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final SystemAnnouncementRepository systemAnnouncementRepository;

    /**
     * Satıcı ismi → User nesnesi eşleştirmesi.
     * Aynı satıcı için tekrar DB sorgusu yapmamak adına cache olarak kullanılır.
     */
    private final Map<String, User> sellerCache = new HashMap<>();

    public DataSeederService(ProductRepository productRepository,
            ProductImageRepository imageRepository,
            ProductReviewRepository reviewRepository,
            UserRepository userRepository,
            SystemAnnouncementRepository systemAnnouncementRepository) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.systemAnnouncementRepository = systemAnnouncementRepository;
    }

    @PostConstruct
    public void seedData() {
        if (systemAnnouncementRepository.count() == 0) {
            SystemAnnouncement defaultAnnouncement = new SystemAnnouncement();
            defaultAnnouncement
                    .setMessage("Welcome to our marketplace! Explore the latest arrivals from top global brands.");
            defaultAnnouncement.setActive(true);
            systemAnnouncementRepository.save(defaultAnnouncement);
            logger.info("Default announcement created");
        }

        if (productRepository.count() == 0) {
            try {
                logger.info("Seeding started (Safe Mode)...");
                loadProducts();
                loadReviews();
                logger.info("Seeding complete");
                logger.info("Total SELLER accounts created: {}", sellerCache.size());
            } catch (Exception e) {
                logger.error("Critical error during seeding: {}", e.getMessage());
            }
        } else {
            assignOwnersToOrphanProducts();
        }
    }


    /**
     * Veritabanındaki tüm ürünleri tarar.
     * owner == null olan ürünlerin brandName/sellerName bilgisinden
     * SELLER hesabı bulur veya oluşturur ve ürüne bağlar.
     */
    private void assignOwnersToOrphanProducts() {
        List<Product> allProducts = productRepository.findAll();

        int orphanCount = 0;
        int linkedCount = 0;

        for (Product product : allProducts) {
            if (product.getOwner() != null) {
                continue;
            }

            orphanCount++;

            String sellerName = product.getSellerName();
            if (sellerName == null || sellerName.isBlank()) {
                sellerName = product.getBrandName();
            }
            if (sellerName == null || sellerName.isBlank()) {
                continue;
            }

            User seller = findOrCreateSeller(sellerName.trim());
            if (seller != null) {
                product.setOwner(seller);
                productRepository.save(product);
                linkedCount++;
            }
        }

        if (orphanCount > 0) {
            logger.info("Orphan product scan complete:");
            logger.info("  Orphan products found: {}", orphanCount);
            logger.info("  Linked to seller: {}", linkedCount);
            logger.info("  SELLER accounts created/found: {}", sellerCache.size());
        } else {
            logger.info("All products have owners, no action needed");
        }
    }


    /**
     * Verilen satıcı ismine göre User (SELLER) bulur veya oluşturur.
     * - Önce cache'e bakar (aynı seed oturumunda tekrar sorgu yapmaz)
     * - Cache'te yoksa DB'den email ile arar
     * - DB'de de yoksa yeni SELLER hesabı oluşturur
     *
     * Email formatı: satici-adi@ecommerce.com (küçük harf, boşluklar tire ile)
     */
    private User findOrCreateSeller(String sellerName) {
        if (sellerName == null || sellerName.isBlank()) {
            return null;
        }

        String trimmedName = sellerName.trim();

        if (sellerCache.containsKey(trimmedName)) {
            return sellerCache.get(trimmedName);
        }

        String emailSlug = trimmedName
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (emailSlug.isEmpty()) {
            emailSlug = "seller-" + System.nanoTime();
        }

        String email = emailSlug + "@ecommerce.com";

        User existingUser = userRepository.findByEmail(email).orElse(null);
        if (existingUser != null) {
            sellerCache.put(trimmedName, existingUser);
            return existingUser;
        }

        User newSeller = new User();
        newSeller.setEmail(email);
        newSeller.setFirstName(extractFirstName(trimmedName));
        newSeller.setLastName(extractLastName(trimmedName));
        newSeller.setAuthProvider("SYSTEM");
        newSeller.setRole("SELLER");

        User savedSeller = userRepository.save(newSeller);
        sellerCache.put(trimmedName, savedSeller);

        logger.info("[SELLER] New account created: {} -> {}", trimmedName, email);
        return savedSeller;
    }

    /**
     * Satıcı isminden firstName çıkarır.
     * "Nike" → "Nike", "Calvin Klein" → "Calvin"
     */
    private String extractFirstName(String fullName) {
        String[] parts = fullName.split("\\s+", 2);
        return parts[0];
    }

    /**
     * Satıcı isminden lastName çıkarır.
     * "Nike" → "Store", "Calvin Klein" → "Klein"
     */
    private String extractLastName(String fullName) {
        String[] parts = fullName.split("\\s+", 2);
        return parts.length > 1 ? parts[1] : "Store";
    }

    /**
     * Bir ürün kaydından en uygun satıcı ismini belirler.
     * Öncelik: seller_name > brand_name
     */
    private String resolveSellerName(CSVRecord record) {
        String sellerName = getValue(record, "seller_name");
        if (!sellerName.isBlank()) {
            return sellerName;
        }
        String brandName = getValue(record, "brand_name");
        if (!brandName.isBlank()) {
            return brandName;
        }
        return null;
    }


    public void loadProducts() throws IOException {
        InputStream is = getClass().getResourceAsStream("/products.csv");
        if (is == null) {
            logger.error("products.csv not found");
            return;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        CSVParser csvParser = new CSVParser(reader,
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());

        int linkedCount = 0;
        int totalCount = 0;

        for (CSVRecord record : csvParser) {
            try {
                Product p = new Product();
                p.setSNo(getValue(record, "s.no"));
                p.setAboutItem(getValue(record, "about_item"));
                p.setAsin(getValue(record, "asin"));
                p.setAvailability(getValue(record, "availability"));
                p.setBestSellersRank(getValue(record, "best_sellers_rank"));
                p.setBrandName(getValue(record, "brand_name"));
                p.setBrandPageUrl(getValue(record, "brand_page_url"));
                p.setBreadcrumbs(getValue(record, "breadcrumbs"));
                p.setCustomerReviewSummary(getValue(record, "customer_review_summary"));
                p.setDefaultVariant0(getValue(record, "default_variant0"));
                p.setDefaultVariant1(getValue(record, "default_variant1"));
                p.setDeliveryDate(getValue(record, "delivery_date"));
                p.setFastestDeliveryDate(getValue(record, "fastest_delivery_date"));
                p.setPriceValue(getValue(record, "price_value"));
                p.setProductUrl(getValue(record, "product_url"));
                p.setRatingCount(getValue(record, "rating_count"));
                p.setRatingDistribution1star(getValue(record, "rating_distribution_1star"));
                p.setRatingDistribution2star(getValue(record, "rating_distribution_2star"));
                p.setRatingDistribution3star(getValue(record, "rating_distribution_3star"));
                p.setRatingDistribution4star(getValue(record, "rating_distribution_4star"));
                p.setRatingDistribution5star(getValue(record, "rating_distribution_5star"));
                p.setRatingStars(getValue(record, "rating_stars"));
                p.setRecentPurchases(getValue(record, "recent_purchases"));
                p.setScrapeTime(getValue(record, "scrape_time"));
                p.setSellerName(getValue(record, "seller_name"));
                p.setTitle(getValue(record, "title"));
                p.setAllImages(getValue(record, "all_images"));
                p.setRank1(getValue(record, "rank_1"));

                String resolvedSeller = resolveSellerName(record);
                if (resolvedSeller != null) {
                    User seller = findOrCreateSeller(resolvedSeller);
                    if (seller != null) {
                        p.setOwner(seller);
                        linkedCount++;
                    }
                }

                Product savedProduct = productRepository.save(p);
                totalCount++;

                String rawImgs = getValue(record, "all_images");
                if (!rawImgs.isEmpty() && rawImgs.contains("http")) {
                    String cleanImgs = rawImgs.replaceAll("[\\[\\]'\" ]", "");
                    String[] imgUrls = cleanImgs.split(",");
                    for (String url : imgUrls) {
                        if (url.startsWith("http")) {
                            ProductImage pi = new ProductImage();
                            pi.setImageUrl(url.trim());
                            pi.setProduct(savedProduct);
                            imageRepository.save(pi);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to save product. ASIN: {} Error: {}", getValue(record, "asin"), e.getMessage());
            }
        }
        csvParser.close();

        logger.info("Products loaded: {}", totalCount);
        logger.info("Products linked to seller: {}", linkedCount);
    }


    public void loadReviews() throws IOException {
        InputStream is = getClass().getResourceAsStream("/reviews.csv");
        if (is == null) {
            logger.error("reviews.csv not found");
            return;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        CSVParser csvParser = new CSVParser(reader,
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());

        for (CSVRecord record : csvParser) {
            try {
                ProductReview r = new ProductReview();
                r.setSNo(getValue(record, "s.no"));
                r.setHelpfulvotecount(getValue(record, "helpfulvotecount"));
                r.setProductasin(getValue(record, "productasin"));
                String rawRating = getValue(record, "rating");
                if (rawRating != null && !rawRating.isBlank()) {
                    try {
                        double dRating = Double.parseDouble(rawRating);
                        r.setRating((int) Math.round(dRating));
                    } catch (NumberFormatException e) {
                        r.setRating(0);
                    }
                }
                r.setReviewid(getValue(record, "reviewid"));
                r.setReviewmetadata(getValue(record, "reviewmetadata"));
                r.setReviewposition(getValue(record, "reviewposition"));
                r.setReviewtext(getValue(record, "reviewtext"));
                r.setReviewtitle(getValue(record, "reviewtitle"));
                r.setReviewurl(getValue(record, "reviewurl"));
                r.setVerifiedpurchase(getValue(record, "verifiedpurchase"));
                r.setCleanedReviewText(getValue(record, "cleaned_review_text"));
                r.setSentimentScore(getValue(record, "sentiment_score"));

                productRepository.findByAsin(getValue(record, "productasin")).ifPresent(r::setProduct);

                reviewRepository.save(r);
            } catch (Exception e) {
                logger.warn("Failed to save review. ID: {} Error: {}", getValue(record, "reviewid"), e.getMessage());
            }
        }
        csvParser.close();
    }

    private String getValue(CSVRecord record, String column) {
        try {
            if (record.isMapped(column) && record.get(column) != null) {
                return record.get(column);
            }
        } catch (Exception e) {
        }
        return "";
    }
}