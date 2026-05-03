package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.enums.NotificationType;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.ProductPublishDTO;
import com.qingyuan.secondhand.dto.ProductUpdateDTO;
import com.qingyuan.secondhand.entity.CampusAuth;
import com.qingyuan.secondhand.entity.College;
import com.qingyuan.secondhand.entity.Product;
import com.qingyuan.secondhand.entity.TradeOrder;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.CampusAuthMapper;
import com.qingyuan.secondhand.mapper.CollegeMapper;
import com.qingyuan.secondhand.mapper.ProductMapper;
import com.qingyuan.secondhand.mapper.TradeOrderMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.NotificationService;
import com.qingyuan.secondhand.vo.AdminProductPageVO;
import com.qingyuan.secondhand.vo.ProductDetailVO;
import com.qingyuan.secondhand.vo.ProductListVO;
import com.qingyuan.secondhand.vo.PublisherInfoVO;
import com.qingyuan.secondhand.vo.RelatedOrderVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @AfterEach
    void clearContext() {
        UserContext.removeCurrentUserId();
    }

    @Test
    void testPublishProduct_Success() throws Exception {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("[\"url1\",\"url2\"]");
        Mockito.when(productMapper.insert(Mockito.any(Product.class))).thenReturn(1);

        UserContext.setCurrentUserId(10001L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);

        ProductPublishDTO dto = buildPublishDTO();
        LocalDateTime before = LocalDateTime.now();
        service.publishProduct(dto);
        LocalDateTime after = LocalDateTime.now();

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productMapper).insert(captor.capture());
        Product saved = captor.getValue();
        Assertions.assertEquals(10001L, saved.getUserId());
        Assertions.assertEquals(0, saved.getStatus());
        Assertions.assertEquals(0, saved.getIsDeleted());
        Assertions.assertEquals(0, saved.getViewCount());
        Assertions.assertEquals(0, saved.getFavoriteCount());
        Assertions.assertEquals("[\"url1\",\"url2\"]", saved.getImages());
        Assertions.assertNotNull(saved.getAutoOffTime());
        Duration duration = Duration.between(before, saved.getAutoOffTime());
        Assertions.assertTrue(duration.toDays() >= 89 && duration.toDays() <= 90);
        Assertions.assertTrue(saved.getAutoOffTime().isAfter(before.minusSeconds(1)));
        Assertions.assertTrue(saved.getAutoOffTime().isBefore(after.plusDays(90).plusSeconds(1)));
    }

    @Test
    void testPublishProduct_ParamValidationFailed() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            ProductPublishDTO dto = new ProductPublishDTO();
            dto.setTitle("");
            dto.setDescription("");
            dto.setPrice(new BigDecimal("0"));
            dto.setImages(List.of());

            Assertions.assertFalse(validator.validate(dto).isEmpty());
        }
    }

    @Test
    void testPublishProduct_ImagesJsonConversion() throws Exception {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("[\"url1\",\"url2\"]");
        Mockito.when(productMapper.insert(Mockito.any(Product.class))).thenReturn(1);

        UserContext.setCurrentUserId(10001L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);

        ProductPublishDTO dto = buildPublishDTO();
        dto.setImages(List.of("url1", "url2"));
        service.publishProduct(dto);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productMapper).insert(captor.capture());
        Assertions.assertEquals("[\"url1\",\"url2\"]", captor.getValue().getImages());
    }

    @Test
    void testUpdateProduct_Success() throws Exception {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("[\"url1\",\"url2\"]");

        Product existing = new Product();
        existing.setId(1L);
        existing.setUserId(10001L);
        Mockito.when(productMapper.selectById(1L)).thenReturn(existing);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        UserContext.setCurrentUserId(10001L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        ProductUpdateDTO dto = buildUpdateDTO(1L);
        service.updateProduct(dto);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productMapper).updateById(captor.capture());
        Product updated = captor.getValue();
        Assertions.assertEquals(0, updated.getStatus());
        Assertions.assertNull(updated.getRejectReason());
        Assertions.assertNotNull(updated.getAutoOffTime());
    }

    @Test
    void testUpdateProduct_NotOwner() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Product existing = new Product();
        existing.setId(2L);
        existing.setUserId(10002L);
        Mockito.when(productMapper.selectById(2L)).thenReturn(existing);

        UserContext.setCurrentUserId(10001L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        ProductUpdateDTO dto = buildUpdateDTO(2L);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.updateProduct(dto));
        Assertions.assertEquals("无权编辑该商品", ex.getMsg());
        Mockito.verify(productMapper, Mockito.never()).updateById(Mockito.any(Product.class));
    }

    @Test
    void testUpdateProduct_NotFound() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Mockito.when(productMapper.selectById(3L)).thenReturn(null);

        UserContext.setCurrentUserId(10001L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        ProductUpdateDTO dto = buildUpdateDTO(3L);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.updateProduct(dto));
        Assertions.assertEquals("商品不存在", ex.getMsg());
    }

    @Test
    void testUpdatePrice_Success() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Product existing = new Product();
        existing.setId(1L);
        existing.setUserId(10001L);
        Mockito.when(productMapper.selectById(1L)).thenReturn(existing);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        UserContext.setCurrentUserId(10001L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        service.updatePrice(1L, new BigDecimal("99.99"));

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productMapper).updateById(captor.capture());
        Product updated = captor.getValue();
        Assertions.assertEquals(new BigDecimal("99.99"), updated.getPrice());
        Assertions.assertNull(updated.getStatus());
    }

    @Test
    void testUpdatePrice_NotOwner() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Product existing = new Product();
        existing.setId(1L);
        existing.setUserId(10002L);
        Mockito.when(productMapper.selectById(1L)).thenReturn(existing);

        UserContext.setCurrentUserId(10001L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.updatePrice(1L, new BigDecimal("10.00")));
        Assertions.assertEquals("无权修改该商品", ex.getMsg());
    }

    @Test
    void testUpdatePrice_InvalidPrice() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);

        UserContext.setCurrentUserId(10001L);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.updatePrice(1L, new BigDecimal("0")));
        Assertions.assertEquals("价格必须大于0", ex.getMsg());
    }

    @Test
    void testGetProductDetail_LoggedIn() throws Exception {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        ProductDetailVO detail = new ProductDetailVO();
        detail.setId(1L);
        detail.setSellerId(200L);
        detail.setTitle("商品");
        detail.setViewCount(1);
        detail.setFavoriteCount(1);
        detail.setStatus(1);
        detail.setImagesJson("[\"a.png\",\"b.png\"]");
        Mockito.when(productMapper.getProductDetailById(1L)).thenReturn(detail);
        Mockito.when(productMapper.countFavoriteByUserAndProduct(10001L, 1L)).thenReturn(1);
        Mockito.when(productMapper.countActiveOrderByUserAndProduct(10001L, 1L)).thenReturn(0);
        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.setIfAbsent(Mockito.anyString(), Mockito.eq("1"), Mockito.eq(24L), Mockito.any()))
                .thenReturn(true);
        Mockito.when(objectMapper.readValue(Mockito.anyString(), Mockito.any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of("a.png", "b.png"));

        UserContext.setCurrentUserId(10001L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        ProductDetailVO result = service.getProductDetail(1L);

        Assertions.assertFalse(result.getIsOwner());
        Assertions.assertTrue(result.getIsFavorited());
        Assertions.assertFalse(result.getHasActiveOrder());
        Mockito.verify(productAsyncService).asyncUpdateViewCount(1L);
    }

    @Test
    void testGetProductDetail_NotLoggedIn() throws Exception {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        ProductDetailVO detail = new ProductDetailVO();
        detail.setId(1L);
        detail.setSellerId(200L);
        detail.setImagesJson("[\"a.png\"]");
        Mockito.when(productMapper.getProductDetailById(1L)).thenReturn(detail);
        Mockito.when(objectMapper.readValue(Mockito.anyString(), Mockito.any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of("a.png"));

        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        ProductDetailVO result = service.getProductDetail(1L);

        Assertions.assertFalse(result.getIsOwner());
        Assertions.assertFalse(result.getIsFavorited());
        Assertions.assertFalse(result.getHasActiveOrder());
        Mockito.verify(productAsyncService).asyncUpdateViewCount(1L);
    }

    @Test
    void testGetProductDetail_ViewCountIncrement() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        ProductDetailVO detail = new ProductDetailVO();
        detail.setId(1L);
        detail.setSellerId(200L);
        detail.setImagesJson("[\"a.png\"]");
        Mockito.when(productMapper.getProductDetailById(1L)).thenReturn(detail);
        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.setIfAbsent(Mockito.anyString(), Mockito.eq("1"), Mockito.eq(24L), Mockito.any()))
                .thenReturn(true).thenReturn(false);

        UserContext.setCurrentUserId(10001L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        service.getProductDetail(1L);
        service.getProductDetail(1L);

        Mockito.verify(productAsyncService, Mockito.times(1)).asyncUpdateViewCount(1L);
    }

    @Test
    void testGetProductList_MultiCondition() throws Exception {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        ProductListVO vo = new ProductListVO();
        vo.setId(1L);
        vo.setCoverImage("[\"1.png\",\"2.png\"]");
        Page<ProductListVO> pageResult = new Page<>(1, 10);
        pageResult.setRecords(List.of(vo));
        Mockito.when(productMapper.getProductList(Mockito.any(Page.class), Mockito.eq(1L), Mockito.eq(2L), Mockito.eq("手机"),
                Mockito.eq(new BigDecimal("100")), Mockito.eq(new BigDecimal("500")), Mockito.eq("price_asc")))
                .thenReturn(pageResult);
        Mockito.when(objectMapper.readValue(Mockito.anyString(), Mockito.any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of("1.png", "2.png"));

        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        IPage<ProductListVO> result = service.getProductList(1, 10, 1L, 2L, "手机",
                new BigDecimal("100"), new BigDecimal("500"), "price_asc");

        Mockito.verify(productMapper).getProductList(Mockito.any(Page.class), Mockito.eq(1L), Mockito.eq(2L), Mockito.eq("手机"),
                Mockito.eq(new BigDecimal("100")), Mockito.eq(new BigDecimal("500")), Mockito.eq("price_asc"));
        Assertions.assertEquals("1.png", result.getRecords().get(0).getCoverImage());
    }

    @Test
    void testGetProductList_WithKeyword() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Page<ProductListVO> pageResult = new Page<>(1, 10);
        pageResult.setRecords(List.of());
        Mockito.when(productMapper.getProductList(Mockito.any(Page.class), Mockito.isNull(), Mockito.isNull(), Mockito.eq("笔记本"),
                Mockito.isNull(), Mockito.isNull(), Mockito.eq("latest"))).thenReturn(pageResult);

        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        service.getProductList(1, 10, null, null, "笔记本", null, null, "latest");

        Mockito.verify(productAsyncService).asyncRecordSearchKeyword("笔记本");
    }

    @Test
    void testGetMyProductList_WithStatus() throws Exception {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        ProductListVO vo = new ProductListVO();
        vo.setId(1L);
        vo.setCoverImage("[\"1.png\"]");
        vo.setStatus(1); // 在售状态
        Page<ProductListVO> pageResult = new Page<>(1, 10);
        pageResult.setRecords(List.of(vo));
        Mockito.when(productMapper.getMyProductList(Mockito.any(Page.class), Mockito.eq(10001L), Mockito.eq(1),
                Mockito.isNull(), Mockito.isNull(), Mockito.isNull()))
                .thenReturn(pageResult);
        Mockito.when(objectMapper.readValue(Mockito.anyString(), Mockito.any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of("1.png"));

        UserContext.setCurrentUserId(10001L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        IPage<ProductListVO> result = service.getMyProductList(1, 10, 1, null, null, null);

        Mockito.verify(productMapper).getMyProductList(Mockito.any(Page.class), Mockito.eq(10001L), Mockito.eq(1),
                Mockito.isNull(), Mockito.isNull(), Mockito.isNull());
        Assertions.assertEquals("1.png", result.getRecords().get(0).getCoverImage());
        Assertions.assertEquals(1, result.getRecords().get(0).getStatus()); // 验证 status 字段
    }

    @Test
    void testGetMyProductList_AllStatus() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Page<ProductListVO> pageResult = new Page<>(1, 10);
        pageResult.setRecords(List.of());
        Mockito.when(productMapper.getMyProductList(Mockito.any(Page.class), Mockito.eq(10001L), Mockito.isNull(),
                Mockito.isNull(), Mockito.isNull(), Mockito.isNull()))
                .thenReturn(pageResult);

        UserContext.setCurrentUserId(10001L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        service.getMyProductList(1, 10, null, null, null, null);

        Mockito.verify(productMapper).getMyProductList(Mockito.any(Page.class), Mockito.eq(10001L), Mockito.isNull(),
                Mockito.isNull(), Mockito.isNull(), Mockito.isNull());
    }

    @Test
    void testOffShelf_Success() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Product existing = new Product();
        existing.setId(1L);
        existing.setUserId(10001L);
        existing.setStatus(1);
        existing.setIsDeleted(0);
        Mockito.when(productMapper.selectById(1L)).thenReturn(existing);
        Mockito.when(productMapper.countActiveOrderByProduct(1L)).thenReturn(0);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        UserContext.setCurrentUserId(10001L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        service.offShelf(1L);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productMapper).updateById(captor.capture());
        Assertions.assertEquals(2, captor.getValue().getStatus());
    }

    @Test
    void testOffShelf_NotOwner() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Product existing = new Product();
        existing.setId(1L);
        existing.setUserId(10002L);
        existing.setStatus(1);
        existing.setIsDeleted(0);
        Mockito.when(productMapper.selectById(1L)).thenReturn(existing);

        UserContext.setCurrentUserId(10001L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.offShelf(1L));
        Assertions.assertEquals("无权操作该商品", ex.getMsg());
    }

    @Test
    void testOffShelf_HasActiveOrder() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Product existing = new Product();
        existing.setId(1L);
        existing.setUserId(10001L);
        existing.setStatus(1);
        existing.setIsDeleted(0);
        Mockito.when(productMapper.selectById(1L)).thenReturn(existing);
        Mockito.when(productMapper.countActiveOrderByProduct(1L)).thenReturn(1);

        UserContext.setCurrentUserId(10001L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.offShelf(1L));
        Assertions.assertEquals("有进行中的订单，无法下架", ex.getMsg());
    }

    @Test
    void testOffShelf_InvalidStatus() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Product existing = new Product();
        existing.setId(1L);
        existing.setUserId(10001L);
        existing.setStatus(2);
        existing.setIsDeleted(0);
        Mockito.when(productMapper.selectById(1L)).thenReturn(existing);

        UserContext.setCurrentUserId(10001L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.offShelf(1L));
        Assertions.assertEquals("商品状态不允许下架", ex.getMsg());
    }

    @Test
    void testOnShelf_Success() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Product existing = new Product();
        existing.setId(1L);
        existing.setUserId(10001L);
        existing.setStatus(2);
        existing.setIsDeleted(0);
        Mockito.when(productMapper.selectById(1L)).thenReturn(existing);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        UserContext.setCurrentUserId(10001L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        service.onShelf(1L);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productMapper).updateById(captor.capture());
        Assertions.assertEquals(0, captor.getValue().getStatus());
        Assertions.assertNull(captor.getValue().getRejectReason());
    }

    @Test
    void testDeleteProduct_Success() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Product existing = new Product();
        existing.setId(1L);
        existing.setUserId(10001L);
        existing.setStatus(2);
        existing.setIsDeleted(0);
        Mockito.when(productMapper.selectById(1L)).thenReturn(existing);
        Mockito.when(productMapper.countActiveOrderByProduct(1L)).thenReturn(0);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        UserContext.setCurrentUserId(10001L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        service.deleteProduct(1L);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productMapper).updateById(captor.capture());
        Assertions.assertEquals(1, captor.getValue().getIsDeleted());
    }

    @Test
    void testApproveProduct_Success() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);

        Product existing = new Product();
        existing.setId(1L);
        existing.setUserId(200L);
        existing.setStatus(0);
        existing.setIsDeleted(0);
        Mockito.when(productMapper.selectById(1L)).thenReturn(existing);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);
        Mockito.when(tradeOrderMapper.selectCount(Mockito.any())).thenReturn(0L);

        UserContext.setCurrentUserId(900L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, tradeOrderMapper, null, null, null);
        service.approveProduct(1L);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productMapper).updateById(captor.capture());
        Assertions.assertEquals(1, captor.getValue().getStatus());
        Assertions.assertEquals(900L, captor.getValue().getReviewerId());
        Assertions.assertNotNull(captor.getValue().getReviewTime());
        Assertions.assertNull(captor.getValue().getRejectReason());
        Mockito.verify(notificationService).send(Mockito.eq(200L), Mockito.eq(NotificationType.AUDIT_PASS), Mockito.anyMap(), Mockito.eq(1L), Mockito.eq(1), Mockito.eq(2));
    }

    @Test
    void testApproveProduct_InvalidStatus() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Product existing = new Product();
        existing.setId(1L);
        existing.setUserId(200L);
        existing.setStatus(1);
        existing.setIsDeleted(0);
        Mockito.when(productMapper.selectById(1L)).thenReturn(existing);

        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.approveProduct(1L));
        Assertions.assertEquals("商品状态不允许审核", ex.getMsg());
    }

    @Test
    void testRejectProduct_Success() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Product existing = new Product();
        existing.setId(1L);
        existing.setUserId(200L);
        existing.setStatus(0);
        existing.setIsDeleted(0);
        Mockito.when(productMapper.selectById(1L)).thenReturn(existing);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        UserContext.setCurrentUserId(900L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        service.rejectProduct(1L, "违规");

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productMapper).updateById(captor.capture());
        Assertions.assertEquals(4, captor.getValue().getStatus());
        Assertions.assertEquals("违规", captor.getValue().getRejectReason());
        Assertions.assertEquals(900L, captor.getValue().getReviewerId());
        Assertions.assertNotNull(captor.getValue().getReviewTime());
        Mockito.verify(notificationService).send(Mockito.eq(200L), Mockito.eq(NotificationType.AUDIT_REJECT), Mockito.anyMap(), Mockito.eq(1L), Mockito.eq(1), Mockito.eq(2));
    }

    @Test
    void testRejectProduct_EmptyReason() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.rejectProduct(1L, ""));
        Assertions.assertEquals("驳回原因不能为空", ex.getMsg());
    }

    @Test
    void testBatchApproveProducts_Success() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);

        initProductTableInfo();
        Product mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setUserId(100L);
        mockProduct.setStatus(0);
        mockProduct.setIsDeleted(0);
        Mockito.when(productMapper.selectList(Mockito.any(LambdaQueryWrapper.class))).thenReturn(List.of(mockProduct));
        Mockito.when(productMapper.update(Mockito.isNull(), Mockito.any(LambdaUpdateWrapper.class))).thenReturn(2);
        Mockito.when(tradeOrderMapper.selectList(Mockito.any())).thenReturn(List.of());

        UserContext.setCurrentUserId(900L);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, tradeOrderMapper, null, null, null);
        service.batchApproveProducts(List.of(1L, 2L));

        Mockito.verify(productMapper, Mockito.times(1)).update(Mockito.isNull(), Mockito.any(LambdaUpdateWrapper.class));
    }

    @Test
    void testBatchApproveProducts_EmptyList() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.batchApproveProducts(List.of()));
        Assertions.assertEquals("商品ID不能为空", ex.getMsg());
    }

    @Test
    void testForceOffShelf_Success() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);

        Product existing = new Product();
        existing.setId(1L);
        existing.setUserId(200L);
        existing.setStatus(1);
        existing.setIsDeleted(0);
        Mockito.when(productMapper.selectById(1L)).thenReturn(existing);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);
        Mockito.when(tradeOrderMapper.selectCount(Mockito.any())).thenReturn(0L);

        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, tradeOrderMapper, null, null, null);
        service.forceOffShelf(1L);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productMapper).updateById(captor.capture());
        Assertions.assertEquals(2, captor.getValue().getStatus());
        Mockito.verify(notificationService).send(Mockito.eq(200L), Mockito.eq(2), Mockito.eq("商品被强制下架"), Mockito.anyString(), Mockito.eq(1L), Mockito.eq(1), Mockito.eq(2));
    }

    @Test
    void testGetAdminProductPage_WithStatus() throws Exception {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        AdminProductPageVO vo = new AdminProductPageVO();
        vo.setId(1L);
        vo.setCoverImage("[\"a.png\"]");
        Page<AdminProductPageVO> pageResult = new Page<>(1, 10);
        pageResult.setRecords(List.of(vo));
        
        // Mock Mapper 方法：需要完整的 9 个参数
        // (Page, status, categoryId, keyword, minPrice, maxPrice, beginTime, endTime, sortBy)
        Mockito.when(productMapper.getAdminProductPage(
                Mockito.any(Page.class),           // page
                Mockito.eq(1),                     // status = 1
                Mockito.isNull(),                  // categoryId = null
                Mockito.eq(""),                    // keyword = ""
                Mockito.isNull(),                  // minPrice = null
                Mockito.isNull(),                  // maxPrice = null
                Mockito.isNull(),                  // beginTime = null
                Mockito.isNull(),                  // endTime = null
                Mockito.isNull()                   // sortBy = null
        )).thenReturn(pageResult);
        
        Mockito.when(objectMapper.readValue(Mockito.anyString(), Mockito.any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of("a.png"));

        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService, null, null, null, null);
        
        // Service 方法调用：需要完整的 10 个参数
        // (page, pageSize, status, categoryId, keyword, minPrice, maxPrice, beginTime, endTime, sortBy)
        IPage<AdminProductPageVO> result = service.getAdminProductPage(
                1,              // page
                10,             // pageSize
                1,              // status
                null,           // categoryId
                "",             // keyword
                null,           // minPrice
                null,           // maxPrice
                null,           // beginTime (String)
                null,           // endTime (String)
                null            // sortBy
        );

        Assertions.assertEquals("a.png", result.getRecords().get(0).getCoverImage());
    }

    // ==================== 关联订单测试 ====================

    @Test
    void testGetRelatedOrders_Success() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);

        RelatedOrderVO order = new RelatedOrderVO();
        order.setOrderId(1L);
        order.setOrderNo("TD20260419224310001");
        order.setBuyerId(10001L);
        order.setBuyerNickName("kaka");
        order.setPrice(new BigDecimal("69.00"));
        order.setStatus(1);
        order.setCreateTime(LocalDateTime.of(2026, 4, 19, 22, 43, 10));
        Page<RelatedOrderVO> pageResult = new Page<>(1, 10);
        pageResult.setRecords(List.of(order));
        pageResult.setTotal(1);
        Mockito.when(tradeOrderMapper.getRelatedOrdersByProductId(Mockito.any(Page.class), Mockito.eq(1L)))
                .thenReturn(pageResult);

        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate,
                productAsyncService, notificationService, tradeOrderMapper, userMapper, campusAuthMapper, collegeMapper);
        IPage<RelatedOrderVO> result = service.getRelatedOrders(1L, 1, 10);

        Assertions.assertEquals(1, result.getTotal());
        Assertions.assertEquals("TD20260419224310001", result.getRecords().get(0).getOrderNo());
        Assertions.assertEquals("待面交", result.getRecords().get(0).getStatusText());
        Assertions.assertNull(result.getRecords().get(0).getCancelByText());
    }

    @Test
    void testGetRelatedOrders_Cancelled() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);

        RelatedOrderVO order = new RelatedOrderVO();
        order.setOrderId(2L);
        order.setOrderNo("TD20260420000000002");
        order.setStatus(5);
        order.setCancelBy(1);
        Page<RelatedOrderVO> pageResult = new Page<>(1, 10);
        pageResult.setRecords(List.of(order));
        Mockito.when(tradeOrderMapper.getRelatedOrdersByProductId(Mockito.any(Page.class), Mockito.eq(1L)))
                .thenReturn(pageResult);

        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate,
                productAsyncService, notificationService, tradeOrderMapper, userMapper, campusAuthMapper, collegeMapper);
        IPage<RelatedOrderVO> result = service.getRelatedOrders(1L, 1, 10);

        Assertions.assertEquals("已取消", result.getRecords().get(0).getStatusText());
        Assertions.assertEquals("买家取消", result.getRecords().get(0).getCancelByText());
    }

    @Test
    void testGetRelatedOrders_Empty() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);

        Page<RelatedOrderVO> pageResult = new Page<>(1, 10);
        pageResult.setRecords(List.of());
        Mockito.when(tradeOrderMapper.getRelatedOrdersByProductId(Mockito.any(Page.class), Mockito.eq(99L)))
                .thenReturn(pageResult);

        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate,
                productAsyncService, notificationService, tradeOrderMapper, userMapper, campusAuthMapper, collegeMapper);
        IPage<RelatedOrderVO> result = service.getRelatedOrders(99L, 1, 10);

        Assertions.assertEquals(0, result.getRecords().size());
    }

    @Test
    void testGetRelatedOrders_NullProductId() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);

        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate,
                productAsyncService, notificationService, tradeOrderMapper, userMapper, campusAuthMapper, collegeMapper);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.getRelatedOrders(null, 1, 10));
        Assertions.assertEquals("商品ID不能为空", ex.getMsg());
    }

    // ==================== 发布者信息测试 ====================

    @Test
    void testGetPublisherInfo_Success() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);

        Product product = new Product();
        product.setId(1L);
        product.setUserId(10001L);
        product.setIsDeleted(0);
        Mockito.when(productMapper.selectById(1L)).thenReturn(product);

        User user = new User();
        user.setId(10001L);
        user.setNickName("kaka");
        user.setAvatarUrl("http://example.com/avatar.png");
        user.setPhone("13812348888");
        user.setStatus(1);
        user.setAuthStatus(2);
        user.setScore(new BigDecimal("5.0"));
        user.setBio("这个人很懒，什么都没写");
        user.setIpRegion("广东广州");
        user.setCreateTime(LocalDateTime.of(2026, 4, 1, 10, 0, 0));
        Mockito.when(userMapper.selectById(10001L)).thenReturn(user);

        Mockito.when(productMapper.selectCount(Mockito.any(LambdaQueryWrapper.class))).thenReturn(5L);
        Mockito.when(tradeOrderMapper.selectCount(Mockito.any(LambdaQueryWrapper.class))).thenReturn(3L);

        CampusAuth auth = new CampusAuth();
        auth.setId(1L);
        auth.setUserId(10001L);
        auth.setCollegeId(1L);
        auth.setRealName("张小明");
        auth.setStudentNo("2023100101");
        Mockito.when(campusAuthMapper.selectByUserId(10001L)).thenReturn(auth);

        College college = new College();
        college.setId(1L);
        college.setName("轻工学院");
        Mockito.when(collegeMapper.selectById(1L)).thenReturn(college);

        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate,
                productAsyncService, notificationService, tradeOrderMapper, userMapper, campusAuthMapper, collegeMapper);
        PublisherInfoVO result = service.getPublisherInfo(1L);

        Assertions.assertEquals(10001L, result.getUserId());
        Assertions.assertEquals("kaka", result.getNickName());
        Assertions.assertEquals("138****8888", result.getPhone());
        Assertions.assertEquals("正常", result.getAccountStatusText());
        Assertions.assertEquals("已认证", result.getAuthStatusText());
        Assertions.assertEquals(5, result.getProductCount());
        Assertions.assertEquals(3, result.getDealOrderCount());
        Assertions.assertEquals("张**", result.getRealName());
        Assertions.assertEquals("轻工学院", result.getCollegeName());
        Assertions.assertEquals("2023****01", result.getStudentNo());
    }

    @Test
    void testGetPublisherInfo_NoAuth() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);

        Product product = new Product();
        product.setId(1L);
        product.setUserId(10001L);
        product.setIsDeleted(0);
        Mockito.when(productMapper.selectById(1L)).thenReturn(product);

        User user = new User();
        user.setId(10001L);
        user.setNickName("test");
        user.setPhone("13900001111");
        user.setStatus(1);
        user.setAuthStatus(0);
        Mockito.when(userMapper.selectById(10001L)).thenReturn(user);

        Mockito.when(productMapper.selectCount(Mockito.any(LambdaQueryWrapper.class))).thenReturn(2L);
        Mockito.when(tradeOrderMapper.selectCount(Mockito.any(LambdaQueryWrapper.class))).thenReturn(0L);

        Mockito.when(campusAuthMapper.selectByUserId(10001L)).thenReturn(null);

        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate,
                productAsyncService, notificationService, tradeOrderMapper, userMapper, campusAuthMapper, collegeMapper);
        PublisherInfoVO result = service.getPublisherInfo(1L);

        Assertions.assertEquals("未认证", result.getAuthStatusText());
        Assertions.assertEquals("139****1111", result.getPhone());
        Assertions.assertEquals(2, result.getProductCount());
        Assertions.assertEquals(0, result.getDealOrderCount());
        Assertions.assertNull(result.getRealName());
        Assertions.assertNull(result.getCollegeName());
        Assertions.assertNull(result.getStudentNo());
    }

    @Test
    void testGetPublisherInfo_ProductNotFound() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);

        Mockito.when(productMapper.selectById(999L)).thenReturn(null);

        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate,
                productAsyncService, notificationService, tradeOrderMapper, userMapper, campusAuthMapper, collegeMapper);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.getPublisherInfo(999L));
        Assertions.assertEquals("商品不存在", ex.getMsg());
    }

    @Test
    void testGetPublisherInfo_NullProductId() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAsyncService productAsyncService = Mockito.mock(ProductAsyncService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);

        ProductServiceImpl service = new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate,
                productAsyncService, notificationService, tradeOrderMapper, userMapper, campusAuthMapper, collegeMapper);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.getPublisherInfo(null));
        Assertions.assertEquals("商品ID不能为空", ex.getMsg());
    }

    private ProductPublishDTO buildPublishDTO() {
        ProductPublishDTO dto = new ProductPublishDTO();
        dto.setTitle("商品标题");
        dto.setDescription("商品描述");
        dto.setPrice(new BigDecimal("99.99"));
        dto.setOriginalPrice(new BigDecimal("199.99"));
        dto.setCategoryId(1L);
        dto.setConditionLevel(2);
        dto.setCampusId(1L);
        dto.setMeetingPointId(2L);
        dto.setMeetingPointText("教学楼门口");
        dto.setImages(List.of("url1", "url2"));
        return dto;
    }

    private void initProductTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, Product.class);
    }

    private ProductUpdateDTO buildUpdateDTO(Long id) {
        ProductUpdateDTO dto = new ProductUpdateDTO();
        dto.setProductId(id);
        dto.setTitle("更新标题");
        dto.setDescription("更新描述");
        dto.setPrice(new BigDecimal("88.88"));
        dto.setOriginalPrice(new BigDecimal("199.99"));
        dto.setCategoryId(1L);
        dto.setConditionLevel(2);
        dto.setCampusId(1L);
        dto.setMeetingPointId(2L);
        dto.setMeetingPointText("图书馆门口");
        dto.setImages(List.of("url1", "url2"));
        return dto;
    }
}
