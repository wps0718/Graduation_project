package com.qingyuan.secondhand.integration;

import com.qingyuan.secondhand.common.context.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.dto.OrderCreateDTO;
import com.qingyuan.secondhand.dto.ReportHandleDTO;
import com.qingyuan.secondhand.dto.ReviewSubmitDTO;
import com.qingyuan.secondhand.entity.CampusAuth;
import com.qingyuan.secondhand.entity.Favorite;
import com.qingyuan.secondhand.entity.Product;
import com.qingyuan.secondhand.entity.Report;
import com.qingyuan.secondhand.entity.Review;
import com.qingyuan.secondhand.entity.TradeOrder;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.CampusAuthMapper;
import com.qingyuan.secondhand.mapper.CollegeMapper;
import com.qingyuan.secondhand.mapper.FavoriteMapper;
import com.qingyuan.secondhand.mapper.ProductMapper;
import com.qingyuan.secondhand.mapper.ReportMapper;
import com.qingyuan.secondhand.mapper.ReviewMapper;
import com.qingyuan.secondhand.mapper.TradeOrderMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.CampusAuthService;
import com.qingyuan.secondhand.service.FavoriteService;
import com.qingyuan.secondhand.service.NotificationService;
import com.qingyuan.secondhand.service.ProductService;
import com.qingyuan.secondhand.service.ReportService;
import com.qingyuan.secondhand.service.ReviewService;
import com.qingyuan.secondhand.service.TradeOrderService;
import com.qingyuan.secondhand.service.impl.CampusAuthServiceImpl;
import com.qingyuan.secondhand.service.impl.FavoriteServiceImpl;
import com.qingyuan.secondhand.service.impl.ProductServiceImpl;
import com.qingyuan.secondhand.service.impl.ProductAsyncService;
import com.qingyuan.secondhand.service.impl.ReportServiceImpl;
import com.qingyuan.secondhand.service.impl.ReviewServiceImpl;
import com.qingyuan.secondhand.service.impl.TradeOrderServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest(classes = NotificationIntegrationTest.TestApp.class)
class NotificationIntegrationTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class,
            WebMvcAutoConfiguration.class
    })
    @Import(TestConfig.class)
    static class TestApp {
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        CampusAuthService campusAuthService(CampusAuthMapper campusAuthMapper,
                                            CollegeMapper collegeMapper,
                                            UserMapper userMapper,
                                            NotificationService notificationService) {
            return new CampusAuthServiceImpl(campusAuthMapper, collegeMapper, userMapper, notificationService);
        }

        @Bean
        ProductService productService(ProductMapper productMapper,
                                      ObjectMapper objectMapper,
                                      StringRedisTemplate stringRedisTemplate,
                                      ProductAsyncService productAsyncService,
                                      NotificationService notificationService) {
            return new ProductServiceImpl(productMapper, objectMapper, stringRedisTemplate, productAsyncService, notificationService);
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        FavoriteService favoriteService(FavoriteMapper favoriteMapper,
                                        ProductMapper productMapper,
                                        ObjectMapper objectMapper,
                                        NotificationService notificationService) {
            return new FavoriteServiceImpl(favoriteMapper, productMapper, objectMapper, notificationService);
        }

        @Bean
        TradeOrderService tradeOrderService(TradeOrderMapper tradeOrderMapper,
                                            ProductMapper productMapper,
                                            StringRedisTemplate redisTemplate,
                                            NotificationService notificationService,
                                            ObjectMapper objectMapper) {
            return new TradeOrderServiceImpl(tradeOrderMapper, productMapper, redisTemplate, notificationService, objectMapper);
        }

        @Bean
        ReviewService reviewService(ReviewMapper reviewMapper,
                                    TradeOrderMapper tradeOrderMapper,
                                    UserMapper userMapper,
                                    NotificationService notificationService) {
            return new ReviewServiceImpl(reviewMapper, tradeOrderMapper, userMapper, notificationService);
        }

        @Bean
        ReportService reportService(ReportMapper reportMapper,
                                    ProductMapper productMapper,
                                    UserMapper userMapper,
                                    TradeOrderMapper tradeOrderMapper,
                                    NotificationService notificationService,
                                    ObjectMapper objectMapper) {
            return new ReportServiceImpl(reportMapper, productMapper, userMapper, tradeOrderMapper, notificationService, objectMapper);
        }
    }

    private static final int RELATED_TYPE_PRODUCT = 1;
    private static final int RELATED_TYPE_TRADE_ORDER = 2;
    private static final int RELATED_TYPE_CAMPUS_AUTH = 3;
    private static final int RELATED_TYPE_REPORT = 4;

    @Autowired
    private CampusAuthService campusAuthService;

    @Autowired
    private ProductService productService;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private TradeOrderService tradeOrderService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReportService reportService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private CampusAuthMapper campusAuthMapper;

    @MockBean
    private CollegeMapper collegeMapper;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private ProductMapper productMapper;

    @MockBean
    private FavoriteMapper favoriteMapper;

    @MockBean
    private TradeOrderMapper tradeOrderMapper;

    @MockBean
    private ReviewMapper reviewMapper;

    @MockBean
    private ReportMapper reportMapper;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private ProductAsyncService productAsyncService;

    @MockBean
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void clearInvocations() {
        Mockito.clearInvocations(notificationService);
    }

    @AfterEach
    void clearContext() {
        UserContext.removeCurrentUserId();
    }

    @Test
    void testCampusAuthApproveNotification() {
        CampusAuth auth = new CampusAuth();
        auth.setId(11L);
        auth.setUserId(20001L);
        auth.setStatus(0);
        Mockito.when(campusAuthMapper.selectById(11L)).thenReturn(auth);
        Mockito.when(campusAuthMapper.updateById(Mockito.any(CampusAuth.class))).thenReturn(1);
        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);

        UserContext.setCurrentUserId(90001L);
        campusAuthService.approveAuth(11L);

        SendArgs args = captureSendArgs();
        Assertions.assertEquals(auth.getUserId(), args.userId());
        Assertions.assertEquals(8, args.type());
        Assertions.assertEquals("校园认证通过", args.title());
        Assertions.assertTrue(args.content().contains("校园认证已通过"));
        Assertions.assertEquals(auth.getId(), args.relatedId());
        Assertions.assertEquals(RELATED_TYPE_CAMPUS_AUTH, args.relatedType());
        Assertions.assertEquals(2, args.category());
    }

    @Test
    void testCampusAuthRejectNotification() {
        CampusAuth auth = new CampusAuth();
        auth.setId(12L);
        auth.setUserId(20002L);
        auth.setStatus(0);
        Mockito.when(campusAuthMapper.selectById(12L)).thenReturn(auth);
        Mockito.when(campusAuthMapper.updateById(Mockito.any(CampusAuth.class))).thenReturn(1);
        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);

        UserContext.setCurrentUserId(90002L);
        campusAuthService.rejectAuth(12L, "学号不符");

        SendArgs args = captureSendArgs();
        Assertions.assertEquals(auth.getUserId(), args.userId());
        Assertions.assertEquals(9, args.type());
        Assertions.assertEquals("校园认证被驳回", args.title());
        Assertions.assertTrue(args.content().contains("学号不符"));
        Assertions.assertEquals(auth.getId(), args.relatedId());
        Assertions.assertEquals(RELATED_TYPE_CAMPUS_AUTH, args.relatedType());
        Assertions.assertEquals(2, args.category());
    }

    @Test
    void testProductApproveNotification() {
        Product product = new Product();
        product.setId(21L);
        product.setUserId(30001L);
        product.setTitle("相机");
        product.setStatus(0);
        product.setIsDeleted(0);
        Mockito.when(productMapper.selectById(21L)).thenReturn(product);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        UserContext.setCurrentUserId(90003L);
        productService.approveProduct(21L);

        SendArgs args = captureSendArgs();
        Assertions.assertEquals(product.getUserId(), args.userId());
        Assertions.assertEquals(3, args.type());
        Assertions.assertEquals("商品审核通过", args.title());
        Assertions.assertTrue(args.content().contains(product.getTitle()));
        Assertions.assertEquals(product.getId(), args.relatedId());
        Assertions.assertEquals(RELATED_TYPE_PRODUCT, args.relatedType());
        Assertions.assertEquals(2, args.category());
    }

    @Test
    void testProductRejectNotification() {
        Product product = new Product();
        product.setId(22L);
        product.setUserId(30002L);
        product.setTitle("耳机");
        product.setStatus(0);
        product.setIsDeleted(0);
        Mockito.when(productMapper.selectById(22L)).thenReturn(product);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        UserContext.setCurrentUserId(90004L);
        productService.rejectProduct(22L, "图片不清晰");

        SendArgs args = captureSendArgs();
        Assertions.assertEquals(product.getUserId(), args.userId());
        Assertions.assertEquals(4, args.type());
        Assertions.assertEquals("商品审核驳回", args.title());
        Assertions.assertTrue(args.content().contains("图片不清晰"));
        Assertions.assertEquals(product.getId(), args.relatedId());
        Assertions.assertEquals(RELATED_TYPE_PRODUCT, args.relatedType());
        Assertions.assertEquals(2, args.category());
    }

    @Test
    void testProductForceOffNotification() {
        Product product = new Product();
        product.setId(23L);
        product.setUserId(30003L);
        product.setTitle("书籍");
        product.setStatus(1);
        product.setIsDeleted(0);
        Mockito.when(productMapper.selectById(23L)).thenReturn(product);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        productService.forceOffShelf(23L);

        SendArgs args = captureSendArgs();
        Assertions.assertEquals(product.getUserId(), args.userId());
        Assertions.assertEquals(2, args.type());
        Assertions.assertEquals("商品被强制下架", args.title());
        Assertions.assertTrue(args.content().contains(product.getTitle()));
        Assertions.assertEquals(product.getId(), args.relatedId());
        Assertions.assertEquals(RELATED_TYPE_PRODUCT, args.relatedType());
        Assertions.assertEquals(2, args.category());
    }

    @Test
    void testFavoriteAddNotification() {
        Product product = new Product();
        product.setId(31L);
        product.setUserId(40001L);
        product.setTitle("键盘");
        product.setIsDeleted(0);
        product.setFavoriteCount(0);
        Mockito.when(productMapper.selectById(31L)).thenReturn(product);
        Mockito.when(favoriteMapper.selectCount(Mockito.any())).thenReturn(0L);
        Mockito.when(favoriteMapper.insert(Mockito.any(Favorite.class))).thenReturn(1);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        UserContext.setCurrentUserId(10001L);
        favoriteService.addFavorite(31L);

        SendArgs args = captureSendArgs();
        Assertions.assertEquals(product.getUserId(), args.userId());
        Assertions.assertEquals(6, args.type());
        Assertions.assertEquals("您的商品被收藏了", args.title());
        Assertions.assertTrue(args.content().contains(product.getTitle()));
        Assertions.assertEquals(product.getId(), args.relatedId());
        Assertions.assertEquals(RELATED_TYPE_PRODUCT, args.relatedType());
        Assertions.assertEquals(1, args.category());
    }

    @Test
    void testOrderCreateNotification() {
        Product product = new Product();
        product.setId(41L);
        product.setUserId(50001L);
        product.setTitle("显示器");
        product.setStatus(1);
        product.setIsDeleted(0);
        Mockito.when(productMapper.selectById(41L)).thenReturn(product);
        Mockito.when(tradeOrderMapper.selectCount(Mockito.any())).thenReturn(0L);
        Mockito.when(tradeOrderMapper.insert(Mockito.any(TradeOrder.class))).thenAnswer(invocation -> {
            TradeOrder order = invocation.getArgument(0);
            order.setId(41001L);
            return 1;
        });
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.setIfAbsent(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.any()))
                .thenReturn(true);

        UserContext.setCurrentUserId(60001L);
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setProductId(41L);
        dto.setPrice(new BigDecimal("99.00"));
        dto.setCampusId(1L);
        dto.setMeetingPoint("图书馆");
        tradeOrderService.createOrder(dto);

        SendArgs args = captureSendArgs();
        Assertions.assertEquals(product.getUserId(), args.userId());
        Assertions.assertEquals(2, args.type());
        Assertions.assertEquals("您有新的订单", args.title());
        Assertions.assertTrue(args.content().contains(product.getTitle()));
        Assertions.assertEquals(41001L, args.relatedId());
        Assertions.assertEquals(RELATED_TYPE_TRADE_ORDER, args.relatedType());
        Assertions.assertEquals(1, args.category());
    }

    @Test
    void testOrderConfirmNotification() {
        TradeOrder order = new TradeOrder();
        order.setId(51L);
        order.setOrderNo("NO-51");
        order.setProductId(511L);
        order.setBuyerId(70001L);
        order.setSellerId(70002L);
        order.setStatus(1);
        Product product = new Product();
        product.setId(511L);
        product.setIsDeleted(0);
        Mockito.when(tradeOrderMapper.selectById(51L)).thenReturn(order);
        Mockito.when(productMapper.selectById(511L)).thenReturn(product);
        Mockito.when(tradeOrderMapper.updateById(Mockito.any(TradeOrder.class))).thenReturn(1);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        UserContext.setCurrentUserId(70001L);
        tradeOrderService.confirmOrder(51L);

        SendArgs args = captureSendArgs();
        Assertions.assertEquals(order.getSellerId(), args.userId());
        Assertions.assertEquals(1, args.type());
        Assertions.assertEquals("订单已确认收货", args.title());
        Assertions.assertTrue(args.content().contains(order.getOrderNo()));
        Assertions.assertEquals(order.getId(), args.relatedId());
        Assertions.assertEquals(RELATED_TYPE_TRADE_ORDER, args.relatedType());
        Assertions.assertEquals(1, args.category());
    }

    @Test
    void testOrderCancelNotificationByBuyer() {
        TradeOrder order = new TradeOrder();
        order.setId(61L);
        order.setOrderNo("NO-61");
        order.setProductId(611L);
        order.setBuyerId(80001L);
        order.setSellerId(80002L);
        order.setStatus(1);
        Product product = new Product();
        product.setId(611L);
        product.setIsDeleted(0);
        Mockito.when(tradeOrderMapper.selectById(61L)).thenReturn(order);
        Mockito.when(productMapper.selectById(611L)).thenReturn(product);
        Mockito.when(tradeOrderMapper.updateById(Mockito.any(TradeOrder.class))).thenReturn(1);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        UserContext.setCurrentUserId(80001L);
        tradeOrderService.cancelOrder(61L, "不方便面交");

        SendArgs args = captureSendArgs();
        Assertions.assertEquals(order.getSellerId(), args.userId());
        Assertions.assertEquals(7, args.type());
        Assertions.assertEquals("订单已取消", args.title());
        Assertions.assertTrue(args.content().contains(order.getOrderNo()));
        Assertions.assertTrue(args.content().contains("不方便面交"));
        Assertions.assertEquals(order.getId(), args.relatedId());
        Assertions.assertEquals(RELATED_TYPE_TRADE_ORDER, args.relatedType());
        Assertions.assertEquals(1, args.category());
    }

    @Test
    void testOrderCancelNotificationBySeller() {
        TradeOrder order = new TradeOrder();
        order.setId(62L);
        order.setOrderNo("NO-62");
        order.setProductId(612L);
        order.setBuyerId(80101L);
        order.setSellerId(80102L);
        order.setStatus(1);
        Product product = new Product();
        product.setId(612L);
        product.setIsDeleted(0);
        Mockito.when(tradeOrderMapper.selectById(62L)).thenReturn(order);
        Mockito.when(productMapper.selectById(612L)).thenReturn(product);
        Mockito.when(tradeOrderMapper.updateById(Mockito.any(TradeOrder.class))).thenReturn(1);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        UserContext.setCurrentUserId(80102L);
        tradeOrderService.cancelOrder(62L, null);

        SendArgs args = captureSendArgs();
        Assertions.assertEquals(order.getBuyerId(), args.userId());
        Assertions.assertEquals(7, args.type());
        Assertions.assertEquals("订单已取消", args.title());
        Assertions.assertTrue(args.content().contains(order.getOrderNo()));
        Assertions.assertEquals(order.getId(), args.relatedId());
        Assertions.assertEquals(RELATED_TYPE_TRADE_ORDER, args.relatedType());
        Assertions.assertEquals(1, args.category());
    }

    @Test
    void testReviewSubmitNotification() {
        TradeOrder order = new TradeOrder();
        order.setId(71L);
        order.setOrderNo("NO-71");
        order.setBuyerId(90001L);
        order.setSellerId(90002L);
        order.setStatus(3);
        order.setCompleteTime(LocalDateTime.now().minusDays(1));
        Mockito.when(tradeOrderMapper.selectById(71L)).thenReturn(order);
        Mockito.when(reviewMapper.selectOne(Mockito.any())).thenReturn(null);
        Mockito.when(reviewMapper.insert(Mockito.any(Review.class))).thenReturn(1);
        Mockito.when(reviewMapper.selectCount(Mockito.any())).thenReturn(1L);
        Mockito.when(reviewMapper.selectList(Mockito.any())).thenReturn(List.of(buildReview(1L, 90001L, 90002L)));
        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);

        UserContext.setCurrentUserId(90001L);
        ReviewSubmitDTO dto = new ReviewSubmitDTO();
        dto.setOrderId(71L);
        dto.setScoreDesc(5);
        dto.setScoreAttitude(5);
        dto.setScoreExperience(5);
        dto.setContent("很好");
        reviewService.submitReview(dto);

        SendArgs args = captureSendArgs();
        Assertions.assertEquals(order.getSellerId(), args.userId());
        Assertions.assertEquals(10, args.type());
        Assertions.assertEquals("您收到了新的评价", args.title());
        Assertions.assertTrue(args.content().contains(order.getOrderNo()));
        Assertions.assertEquals(order.getId(), args.relatedId());
        Assertions.assertEquals(RELATED_TYPE_TRADE_ORDER, args.relatedType());
        Assertions.assertEquals(1, args.category());
    }

    @Test
    void testReportHandleNotifications() {
        Report reportOff = new Report();
        reportOff.setId(81L);
        reportOff.setTargetType(1);
        reportOff.setTargetId(811L);
        reportOff.setStatus(0);
        Product product = new Product();
        product.setId(811L);
        product.setTitle("自行车");
        product.setUserId(91001L);
        product.setIsDeleted(0);
        Mockito.when(reportMapper.selectById(81L)).thenReturn(reportOff);
        Mockito.when(productMapper.selectById(811L)).thenReturn(product);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);
        Mockito.when(reportMapper.updateById(Mockito.any(Report.class))).thenReturn(1);

        ReportHandleDTO offDto = new ReportHandleDTO();
        offDto.setReportId(81L);
        offDto.setAction("off_shelf");
        reportService.handleReport(offDto, 99001L);

        SendArgs offArgs = captureSendArgs();
        Assertions.assertEquals(product.getUserId(), offArgs.userId());
        Assertions.assertEquals(2, offArgs.type());
        Assertions.assertEquals("商品被举报下架", offArgs.title());
        Assertions.assertTrue(offArgs.content().contains(product.getTitle()));
        Assertions.assertEquals(product.getId(), offArgs.relatedId());
        Assertions.assertEquals(RELATED_TYPE_PRODUCT, offArgs.relatedType());
        Assertions.assertEquals(2, offArgs.category());

        Mockito.clearInvocations(notificationService);
        Report reportWarn = new Report();
        reportWarn.setId(82L);
        reportWarn.setTargetType(2);
        reportWarn.setTargetId(91002L);
        reportWarn.setStatus(0);
        Mockito.when(reportMapper.selectById(82L)).thenReturn(reportWarn);

        ReportHandleDTO warnDto = new ReportHandleDTO();
        warnDto.setReportId(82L);
        warnDto.setAction("warn");
        reportService.handleReport(warnDto, 99002L);

        SendArgs warnArgs = captureSendArgs();
        Assertions.assertEquals(reportWarn.getTargetId(), warnArgs.userId());
        Assertions.assertEquals(2, warnArgs.type());
        Assertions.assertEquals("您收到了警告", warnArgs.title());
        Assertions.assertTrue(warnArgs.content().contains("警告"));
        Assertions.assertEquals(reportWarn.getId(), warnArgs.relatedId());
        Assertions.assertEquals(RELATED_TYPE_REPORT, warnArgs.relatedType());
        Assertions.assertEquals(2, warnArgs.category());

        Mockito.clearInvocations(notificationService);
        Report reportBan = new Report();
        reportBan.setId(83L);
        reportBan.setTargetType(2);
        reportBan.setTargetId(91003L);
        reportBan.setStatus(0);
        Mockito.when(reportMapper.selectById(83L)).thenReturn(reportBan);
        User user = new User();
        user.setId(91003L);
        Mockito.when(userMapper.selectById(91003L)).thenReturn(user);
        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);
        Mockito.when(tradeOrderMapper.selectList(Mockito.any())).thenReturn(List.of());

        ReportHandleDTO banDto = new ReportHandleDTO();
        banDto.setReportId(83L);
        banDto.setAction("ban");
        banDto.setHandleResult("严重违规");
        reportService.handleReport(banDto, 99003L);

        SendArgs banArgs = captureSendArgs();
        Assertions.assertEquals(reportBan.getTargetId(), banArgs.userId());
        Assertions.assertEquals(2, banArgs.type());
        Assertions.assertEquals("账号已被封禁", banArgs.title());
        Assertions.assertTrue(banArgs.content().contains("严重违规"));
        Assertions.assertEquals(reportBan.getId(), banArgs.relatedId());
        Assertions.assertEquals(RELATED_TYPE_REPORT, banArgs.relatedType());
        Assertions.assertEquals(2, banArgs.category());

        Mockito.clearInvocations(notificationService);
        Report reportIgnore = new Report();
        reportIgnore.setId(84L);
        reportIgnore.setTargetType(2);
        reportIgnore.setTargetId(91004L);
        reportIgnore.setStatus(0);
        Mockito.when(reportMapper.selectById(84L)).thenReturn(reportIgnore);
        ReportHandleDTO ignoreDto = new ReportHandleDTO();
        ignoreDto.setReportId(84L);
        ignoreDto.setAction("ignore");
        reportService.handleReport(ignoreDto, 99004L);

        Mockito.verify(notificationService, Mockito.never()).send(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    private SendArgs captureSendArgs() {
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Integer> typeCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> relatedIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Integer> relatedTypeCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> categoryCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(notificationService).send(
                userIdCaptor.capture(),
                typeCaptor.capture(),
                titleCaptor.capture(),
                contentCaptor.capture(),
                relatedIdCaptor.capture(),
                relatedTypeCaptor.capture(),
                categoryCaptor.capture()
        );
        return new SendArgs(
                userIdCaptor.getValue(),
                typeCaptor.getValue(),
                titleCaptor.getValue(),
                contentCaptor.getValue(),
                relatedIdCaptor.getValue(),
                relatedTypeCaptor.getValue(),
                categoryCaptor.getValue()
        );
    }

    private Review buildReview(Long id, Long reviewerId, Long targetId) {
        Review review = new Review();
        review.setId(id);
        review.setReviewerId(reviewerId);
        review.setTargetId(targetId);
        review.setScoreDesc(5);
        review.setScoreAttitude(5);
        review.setScoreExperience(5);
        review.setCreateTime(LocalDateTime.now());
        return review;
    }

    private record SendArgs(Long userId, Integer type, String title, String content, Long relatedId, Integer relatedType,
                            Integer category) {
    }
}
