package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.OrderCreateDTO;
import com.qingyuan.secondhand.entity.Product;
import com.qingyuan.secondhand.entity.TradeOrder;
import com.qingyuan.secondhand.mapper.ProductMapper;
import com.qingyuan.secondhand.mapper.TradeOrderMapper;
import com.qingyuan.secondhand.service.NotificationService;
import com.qingyuan.secondhand.vo.OrderCreateVO;
import com.qingyuan.secondhand.vo.OrderDetailVO;
import com.qingyuan.secondhand.vo.OrderListVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class TradeOrderServiceImplTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "TradeOrder");
        TableInfoHelper.initTableInfo(assistant, TradeOrder.class);
    }

    @AfterEach
    void clearContext() {
        UserContext.removeCurrentUserId();
    }

    @Test
    void testCreateOrder_Success() throws Exception {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.setIfAbsent(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.any()))
            .thenReturn(true);

        Product product = buildProduct(1L, 20001L, 1);
        Mockito.when(productMapper.selectById(1L)).thenReturn(product);
        Mockito.when(tradeOrderMapper.selectCount(Mockito.any())).thenReturn(0L);

        Mockito.doAnswer(invocation -> {
            TradeOrder order = invocation.getArgument(0);
            order.setId(10001L);
            return 1;
        }).when(tradeOrderMapper).insert(Mockito.any(TradeOrder.class));

        UserContext.setCurrentUserId(10000L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        OrderCreateDTO dto = buildCreateDTO();
        LocalDateTime before = LocalDateTime.now();
        OrderCreateVO vo = service.createOrder(dto);
        LocalDateTime after = LocalDateTime.now();

        Assertions.assertEquals(10001L, vo.getOrderId());
        Assertions.assertTrue(vo.getOrderNo().matches("TD\\d{18}"));
        Assertions.assertNotNull(vo.getExpireTime());

        ArgumentCaptor<TradeOrder> orderCaptor = ArgumentCaptor.forClass(TradeOrder.class);
        Mockito.verify(tradeOrderMapper).insert(orderCaptor.capture());
        TradeOrder saved = orderCaptor.getValue();
        Assertions.assertEquals(1, saved.getStatus());
        Assertions.assertEquals(1L, saved.getProductId());
        Assertions.assertEquals(10000L, saved.getBuyerId());
        Assertions.assertEquals(20001L, saved.getSellerId());
        Assertions.assertEquals(dto.getPrice(), saved.getPrice());
        Assertions.assertEquals(dto.getCampusId(), saved.getCampusId());
        Assertions.assertEquals(dto.getMeetingPoint(), saved.getMeetingPoint());

        Duration expireDuration = Duration.between(before, saved.getExpireTime());
        Assertions.assertTrue(expireDuration.toHours() >= 71 && expireDuration.toHours() <= 73);
        Assertions.assertTrue(saved.getExpireTime().isAfter(before.minusSeconds(1)));
        Assertions.assertTrue(saved.getExpireTime().isBefore(after.plusHours(73)));

        Duration confirmDuration = Duration.between(before, saved.getConfirmDeadline());
        Assertions.assertTrue(confirmDuration.toDays() >= 6 && confirmDuration.toDays() <= 7);

        Mockito.verify(notificationService).sendNotification(20001L, 2, "订单已创建");
        ArgumentCaptor<String> lockValueCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(valueOperations).setIfAbsent(Mockito.anyString(), lockValueCaptor.capture(), Mockito.anyLong(), Mockito.any());
        Assertions.assertTrue(lockValueCaptor.getValue().matches("[0-9a-fA-F-]{36}"));
        Mockito.verify(redisTemplate).execute(Mockito.any(DefaultRedisScript.class), Mockito.anyList(), Mockito.anyString());
    }

    @Test
    void testCreateOrder_ProductNotFound() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Mockito.when(productMapper.selectById(1L)).thenReturn(null);

        UserContext.setCurrentUserId(10000L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.createOrder(buildCreateDTO()));
        Assertions.assertEquals("商品不存在", ex.getMsg());
    }

    @Test
    void testCreateOrder_ProductNotOnSale() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Product product = buildProduct(1L, 20001L, 0);
        Mockito.when(productMapper.selectById(1L)).thenReturn(product);

        UserContext.setCurrentUserId(10000L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.createOrder(buildCreateDTO()));
        Assertions.assertEquals("商品未在售", ex.getMsg());
    }

    @Test
    void testCreateOrder_BuyOwnProduct() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Product product = buildProduct(1L, 10000L, 1);
        Mockito.when(productMapper.selectById(1L)).thenReturn(product);

        UserContext.setCurrentUserId(10000L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.createOrder(buildCreateDTO()));
        Assertions.assertEquals("不能购买自己的商品", ex.getMsg());
    }

    @Test
    void testCreateOrder_ProductHasActiveOrder() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.setIfAbsent(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.any()))
            .thenReturn(true);

        Product product = buildProduct(1L, 20001L, 1);
        Mockito.when(productMapper.selectById(1L)).thenReturn(product);
        Mockito.when(tradeOrderMapper.selectCount(Mockito.any())).thenReturn(1L);

        UserContext.setCurrentUserId(10000L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.createOrder(buildCreateDTO()));
        Assertions.assertEquals("该商品已有进行中的订单", ex.getMsg());
        Mockito.verify(redisTemplate).execute(Mockito.any(DefaultRedisScript.class), Mockito.anyList(), Mockito.anyString());
    }

    @Test
    void testCreateOrder_ConcurrentLock() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.setIfAbsent(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.any()))
            .thenReturn(false);

        Product product = buildProduct(1L, 20001L, 1);
        Mockito.when(productMapper.selectById(1L)).thenReturn(product);

        UserContext.setCurrentUserId(10000L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.createOrder(buildCreateDTO()));
        Assertions.assertEquals("商品正在被其他用户下单，请稍后重试", ex.getMsg());
        Mockito.verify(redisTemplate, Mockito.never()).execute(Mockito.any(DefaultRedisScript.class), Mockito.anyList(), Mockito.anyString());
    }

    @Test
    void testGetOrderList_Buyer() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Page<OrderListVO> page = new Page<>(1, 10);
        OrderListVO item = new OrderListVO();
        item.setId(1L);
        item.setOtherUserId(20001L);
        page.setRecords(List.of(item));
        Mockito.when(tradeOrderMapper.getOrderList(Mockito.any(), Mockito.anyLong(), Mockito.eq("buyer"), Mockito.isNull()))
            .thenReturn(page);

        UserContext.setCurrentUserId(10000L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        IPage<OrderListVO> result = service.getOrderList("buyer", null, 1, 10);
        Assertions.assertEquals(1, result.getRecords().size());
        Assertions.assertEquals(20001L, result.getRecords().get(0).getOtherUserId());
    }

    @Test
    void testGetOrderList_Seller() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Page<OrderListVO> page = new Page<>(1, 10);
        OrderListVO item = new OrderListVO();
        item.setId(2L);
        item.setOtherUserId(10000L);
        page.setRecords(List.of(item));
        Mockito.when(tradeOrderMapper.getOrderList(Mockito.any(), Mockito.anyLong(), Mockito.eq("seller"), Mockito.isNull()))
            .thenReturn(page);

        UserContext.setCurrentUserId(20001L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        IPage<OrderListVO> result = service.getOrderList("seller", null, 1, 10);
        Assertions.assertEquals(1, result.getRecords().size());
        Assertions.assertEquals(10000L, result.getRecords().get(0).getOtherUserId());
    }

    @Test
    void testGetOrderList_WithStatusFilter() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Page<OrderListVO> page = new Page<>(1, 10);
        Mockito.when(tradeOrderMapper.getOrderList(Mockito.any(), Mockito.anyLong(), Mockito.eq("buyer"), Mockito.eq(1)))
            .thenReturn(page);

        UserContext.setCurrentUserId(10000L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        IPage<OrderListVO> result = service.getOrderList("buyer", 1, 1, 10);
        Assertions.assertEquals(0, result.getRecords().size());
    }

    @Test
    void testGetOrderDetail_Success() throws Exception {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        OrderDetailVO detail = buildOrderDetail();
        Mockito.when(tradeOrderMapper.getOrderDetail(1L)).thenReturn(detail);
        Mockito.when(objectMapper.readValue(Mockito.eq(detail.getProductImagesJson()), Mockito.any(TypeReference.class)))
            .thenReturn(List.of("url1", "url2"));

        UserContext.setCurrentUserId(10000L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        OrderDetailVO result = service.getOrderDetail(1L);
        Assertions.assertEquals("buyer", result.getCurrentRole());
        Assertions.assertEquals("138****8000", result.getBuyerPhone());
        Assertions.assertEquals("139****9000", result.getSellerPhone());
        Assertions.assertEquals(List.of("url1", "url2"), result.getProductImages());
    }

    @Test
    void testGetOrderDetail_NotBuyerOrSeller() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        OrderDetailVO detail = buildOrderDetail();
        Mockito.when(tradeOrderMapper.getOrderDetail(1L)).thenReturn(detail);

        UserContext.setCurrentUserId(30000L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.getOrderDetail(1L));
        Assertions.assertEquals("无权限查看该订单", ex.getMsg());
    }

    @Test
    void testConfirmOrder_Success() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 1, 1L);
        Product product = buildProduct(1L, 10002L, 1);
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        Mockito.when(productMapper.selectById(1L)).thenReturn(product);
        Mockito.when(tradeOrderMapper.updateById(Mockito.any(TradeOrder.class))).thenReturn(1);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        UserContext.setCurrentUserId(10001L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        service.confirmOrder(1L);

        ArgumentCaptor<TradeOrder> orderCaptor = ArgumentCaptor.forClass(TradeOrder.class);
        Mockito.verify(tradeOrderMapper).updateById(orderCaptor.capture());
        TradeOrder updated = orderCaptor.getValue();
        Assertions.assertEquals(3, updated.getStatus());
        Assertions.assertNotNull(updated.getCompleteTime());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productMapper).updateById(productCaptor.capture());
        Assertions.assertEquals(3, productCaptor.getValue().getStatus());
        Mockito.verify(notificationService).sendNotification(10002L, 2, "订单已确认收货");
    }

    @Test
    void testConfirmOrder_NotBuyer() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 1, 1L);
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);

        UserContext.setCurrentUserId(10002L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.confirmOrder(1L));
        Assertions.assertEquals("只有买家可以确认收货", ex.getMsg());
    }

    @Test
    void testConfirmOrder_WrongStatus() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 3, 1L);
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);

        UserContext.setCurrentUserId(10001L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.confirmOrder(1L));
        Assertions.assertEquals("订单状态不正确", ex.getMsg());
    }

    @Test
    void testCancelOrder_ByBuyer() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 1, 1L);
        Product product = buildProduct(1L, 10002L, 1);
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        Mockito.when(productMapper.selectById(1L)).thenReturn(product);
        Mockito.when(tradeOrderMapper.updateById(Mockito.any(TradeOrder.class))).thenReturn(1);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        UserContext.setCurrentUserId(10001L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        service.cancelOrder(1L, "不想要了");

        ArgumentCaptor<TradeOrder> orderCaptor = ArgumentCaptor.forClass(TradeOrder.class);
        Mockito.verify(tradeOrderMapper).updateById(orderCaptor.capture());
        TradeOrder updated = orderCaptor.getValue();
        Assertions.assertEquals(5, updated.getStatus());
        Assertions.assertEquals(10001L, updated.getCancelBy());
        Assertions.assertEquals("不想要了", updated.getCancelReason());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productMapper).updateById(productCaptor.capture());
        Assertions.assertEquals(1, productCaptor.getValue().getStatus());
        Mockito.verify(notificationService).sendNotification(10002L, 2, "订单已取消");
    }

    @Test
    void testCancelOrder_BySeller() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 1, 1L);
        Product product = buildProduct(1L, 10002L, 1);
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        Mockito.when(productMapper.selectById(1L)).thenReturn(product);
        Mockito.when(tradeOrderMapper.updateById(Mockito.any(TradeOrder.class))).thenReturn(1);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        UserContext.setCurrentUserId(10002L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        service.cancelOrder(1L, "商品已售出");

        ArgumentCaptor<TradeOrder> orderCaptor = ArgumentCaptor.forClass(TradeOrder.class);
        Mockito.verify(tradeOrderMapper).updateById(orderCaptor.capture());
        TradeOrder updated = orderCaptor.getValue();
        Assertions.assertEquals(5, updated.getStatus());
        Assertions.assertEquals(10002L, updated.getCancelBy());
        Assertions.assertEquals("商品已售出", updated.getCancelReason());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productMapper).updateById(productCaptor.capture());
        Assertions.assertEquals(1, productCaptor.getValue().getStatus());
        Mockito.verify(notificationService).sendNotification(10001L, 2, "订单已取消");
    }

    @Test
    void testCancelOrder_Unauthorized() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 1, 1L);
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);

        UserContext.setCurrentUserId(10003L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.cancelOrder(1L, "测试"));
        Assertions.assertEquals("无权取消该订单", ex.getMsg());
    }

    @Test
    void testCancelOrder_WrongStatus() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 3, 1L);
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);

        UserContext.setCurrentUserId(10001L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.cancelOrder(1L, "测试"));
        Assertions.assertEquals("只有待面交的订单可以取消", ex.getMsg());
    }

    @Test
    void testDeleteOrder_ByBuyer() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 5, 1L);
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        Mockito.when(tradeOrderMapper.updateById(Mockito.any(TradeOrder.class))).thenReturn(1);

        UserContext.setCurrentUserId(10001L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        service.deleteOrder(1L);

        ArgumentCaptor<TradeOrder> orderCaptor = ArgumentCaptor.forClass(TradeOrder.class);
        Mockito.verify(tradeOrderMapper).updateById(orderCaptor.capture());
        Assertions.assertEquals(1, orderCaptor.getValue().getIsDeletedBuyer());
    }

    @Test
    void testDeleteOrder_BySeller() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 4, 1L);
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        Mockito.when(tradeOrderMapper.updateById(Mockito.any(TradeOrder.class))).thenReturn(1);

        UserContext.setCurrentUserId(10002L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        service.deleteOrder(1L);

        ArgumentCaptor<TradeOrder> orderCaptor = ArgumentCaptor.forClass(TradeOrder.class);
        Mockito.verify(tradeOrderMapper).updateById(orderCaptor.capture());
        Assertions.assertEquals(1, orderCaptor.getValue().getIsDeletedSeller());
    }

    @Test
    void testDeleteOrder_WrongStatus() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 1, 1L);
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);

        UserContext.setCurrentUserId(10001L);
        TradeOrderServiceImpl service = new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.deleteOrder(1L));
        Assertions.assertEquals("只有已评价或已取消的订单可以删除", ex.getMsg());
    }

    private OrderCreateDTO buildCreateDTO() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setProductId(1L);
        dto.setPrice(new BigDecimal("199.99"));
        dto.setCampusId(1L);
        dto.setMeetingPoint("A区门口");
        return dto;
    }

    private Product buildProduct(Long id, Long userId, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setUserId(userId);
        product.setStatus(status);
        product.setIsDeleted(0);
        return product;
    }

    private TradeOrder buildOrder(Long id, Long buyerId, Long sellerId, Integer status, Long productId) {
        TradeOrder order = new TradeOrder();
        order.setId(id);
        order.setBuyerId(buyerId);
        order.setSellerId(sellerId);
        order.setStatus(status);
        order.setProductId(productId);
        return order;
    }

    private OrderDetailVO buildOrderDetail() {
        OrderDetailVO detail = new OrderDetailVO();
        detail.setId(1L);
        detail.setBuyerId(10000L);
        detail.setSellerId(20001L);
        detail.setBuyerPhone("13800138000");
        detail.setSellerPhone("13900139000");
        detail.setProductImagesJson("[\"url1\",\"url2\"]");
        return detail;
    }
}
