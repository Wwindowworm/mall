package com.mall.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.config.DeepSeekProperties;
import com.mall.entity.Product;
import com.mall.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ProductRecommendAgent {

    private static final Logger log = LoggerFactory.getLogger(ProductRecommendAgent.class);

    @Autowired
    private RestTemplate deepSeekRestTemplate;
    @Autowired
    private DeepSeekProperties deepSeekProperties;
    @Autowired
    private ProductMapper productMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 智能商品推荐 - 基于用户行为 + DeepSeek 大模型
     */
    public List<Long> recommend(Long userId, List<Long> viewedProducts, List<Long> purchasedProducts, int limit) {
        log.info("AI 推荐请求，userId={}, viewed={}, purchased={}, limit={}", userId, viewedProducts, purchasedProducts, limit);

        try {
            // 1. 获取所有商品信息，供大模型参考
            List<Product> allProducts = productMapper.selectList(null);
            if (allProducts == null || allProducts.isEmpty()) {
                return fallbackRecommend(limit);
            }

            // 2. 构建商品列表文本
            StringBuilder productContext = new StringBuilder("商品列表：\n");
            for (Product p : allProducts) {
                productContext.append(String.format("- ID:%d 【%s】价格:¥%.2f 销量:%d\n",
                        p.getId(), p.getName(), p.getPrice(), p.getSalesCount()));
            }

            // 3. 构建推荐 Prompt
            String viewedStr = viewedProducts == null ? "无" : viewedProducts.toString();
            String purchasedStr = purchasedProducts == null ? "无" : purchasedProducts.toString();

            String prompt = String.format("""
                你是一个专业的电商推荐系统。用户信息：userId=%d
                用户已浏览商品ID：%s
                用户已购买商品ID：%s

                %s

                请从以上商品列表中，推荐 %d 个最适合该用户的商品ID（返回纯JSON数组，如 [1,2,3]，不要返回其他文字）。
                规则：
                1. 不推荐用户已购买或已浏览的商品
                2. 优先推荐高销量、热门商品
                3. 考虑商品多样性，不要全是同类商品
                4. ID 必须来自上述商品列表
                """, userId, viewedStr, purchasedStr, productContext, limit);

            // 4. 调用 DeepSeek
            String response = callDeepSeek(prompt);
            log.info("DeepSeek 推荐响应: {}", response);

            // 5. 解析返回的 JSON 数组
            List<Long> recommendations = parseRecommendations(response, allProducts);
            if (recommendations.isEmpty()) {
                return fallbackRecommend(limit);
            }
            return recommendations;

        } catch (Exception e) {
            log.error("DeepSeek 推荐失败，降级到默认推荐: {}", e.getMessage());
            return fallbackRecommend(limit);
        }
    }

    /**
     * 生成个性化推荐理由
     */
    public String generateRecommendReason(Long userId, Long productId) {
        try {
            Product product = productMapper.selectById(productId);
            if (product == null) return "热门商品，值得一试！";

            String prompt = String.format("""
                用户(userId=%d)刚刚浏览/购买了商品【%s】，请用一句话为用户推荐另一款相关商品。
                要求：亲切自然，不超过30字，突出商品特点和用户利益。
                """, userId, product.getName());

            String response = callDeepSeek(prompt);
            if (response != null && !response.isBlank()) {
                return response.trim().replaceAll("[\"\\n]", "").substring(0, Math.min(response.trim().length(), 50));
            }
        } catch (Exception e) {
            log.warn("生成推荐理由失败: {}", e.getMessage());
        }
        return "根据您的购物偏好，为您精选推荐，品质优良，性价比超高！";
    }

    /**
     * 调用 DeepSeek API
     */
    private String callDeepSeek(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", deepSeekProperties.getModel());
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", prompt));
        requestBody.put("messages", messages);

        String url = deepSeekProperties.getBaseUrl() + "/chat/completions";

        String response = deepSeekRestTemplate.postForObject(url, requestBody, String.class);
        return extractContent(response);
    }

    /**
     * 从 DeepSeek 响应中提取内容
     */
    private String extractContent(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                return choices.get(0).get("message").get("content").asText();
            }
        } catch (Exception e) {
            log.error("解析 DeepSeek 响应失败: {}", e.getMessage());
        }
        return "";
    }

    /**
     * 解析推荐结果（兼容多种返回格式）
     */
    private List<Long> parseRecommendations(String response, List<Product> allProducts) {
        List<Long> result = new ArrayList<>();
        Set<Long> validIds = new HashSet<>();
        for (Product p : allProducts) validIds.add(p.getId());

        try {
            // 尝试直接解析 JSON 数组
            String json = response.trim();
            int start = json.indexOf('[');
            int end = json.lastIndexOf(']');
            if (start != -1 && end != -1) {
                json = json.substring(start, end + 1);
                JsonNode array = objectMapper.readTree(json);
                for (JsonNode node : array) {
                    Long id = node.asLong();
                    if (validIds.contains(id) && !result.contains(id)) {
                        result.add(id);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析推荐 JSON 失败，尝试文本提取: {}", e.getMessage());
            // 降级：从文本中提取数字 ID
            for (Product p : allProducts) {
                if (response.contains(String.valueOf(p.getId())) && !result.contains(p.getId())) {
                    result.add(p.getId());
                    if (result.size() >= 5) break;
                }
            }
        }
        return result;
    }

    /**
     * 降级推荐：返回热门商品
     */
    private List<Long> fallbackRecommend(int limit) {
        List<Product> hotProducts = productMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
                        .orderByDesc(Product::getSalesCount)
                        .last("LIMIT " + limit)
        );
        List<Long> ids = new ArrayList<>();
        for (Product p : hotProducts) ids.add(p.getId());
        return ids;
    }
}
