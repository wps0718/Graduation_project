package com.qingyuan.secondhand.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.enums.ProductStatus;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.ReportHandleDTO;
import com.qingyuan.secondhand.dto.ReportSubmitDTO;
import com.qingyuan.secondhand.entity.Product;
import com.qingyuan.secondhand.entity.Report;
import com.qingyuan.secondhand.entity.TradeOrder;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.ProductMapper;
import com.qingyuan.secondhand.mapper.ReportMapper;
import com.qingyuan.secondhand.mapper.TradeOrderMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @AfterEach
    void clearContext() {
        UserContext.removeCurrentUserId();
    }

    @Test
    void testSubmitReport_Success() throws Exception {
        ReportMapper reportMapper = Mockito.mock(ReportMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        ObjectMapper objectMapper = new ObjectMapper();

        UserContext.setCurrentUserId(10001L);
        Product product = buildProduct(1L, 10002L, ProductStatus.ON_SALE.getCode(), 0);
        Mockito.when(productMapper.selectById(1L)).thenReturn(product);
        Mockito.when(reportMapper.selectOne(Mockito.any())).thenReturn(null);
        Mockito.when(reportMapper.insert(Mockito.any(Report.class))).thenReturn(1);

        ReportServiceImpl service = new ReportServiceImpl(reportMapper, productMapper, userMapper, tradeOrderMapper, notificationService, objectMapper);
        ReportSubmitDTO dto = buildSubmitDTO(1L, 1, 2, "虚假商品", List.of("a.png", "b.png"));

        service.submitReport(dto);

        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        Mockito.verify(reportMapper).insert(reportCaptor.capture());
        Report saved = reportCaptor.getValue();
        Assertions.assertEquals(10001L, saved.getReporterId());
        Assertions.assertEquals(1L, saved.getTargetId());
        Assertions.assertEquals(1, saved.getTargetType());
        Assertions.assertEquals(2, saved.getReasonType());
        Assertions.assertEquals("虚假商品", saved.getDescription());
        Assertions.assertEquals(0, saved.getStatus());
        List<String> evidence = objectMapper.readValue(saved.getEvidence(), List.class);
        Assertions.assertEquals(2, evidence.size());
    }

    @Test
    void testSubmitReport_ReportSelf_Product() {
        ReportMapper reportMapper = Mockito.mock(ReportMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        ObjectMapper objectMapper = new ObjectMapper();

        UserContext.setCurrentUserId(10001L);
        Product product = buildProduct(1L, 10001L, ProductStatus.ON_SALE.getCode(), 0);
        Mockito.when(productMapper.selectById(1L)).thenReturn(product);

        ReportServiceImpl service = new ReportServiceImpl(reportMapper, productMapper, userMapper, tradeOrderMapper, notificationService, objectMapper);
        ReportSubmitDTO dto = buildSubmitDTO(1L, 1, 1, "举报自己", null);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.submitReport(dto));
        Assertions.assertEquals("不能举报自己", ex.getMsg());
    }

    @Test
    void testSubmitReport_ReportSelf_User() {
        ReportMapper reportMapper = Mockito.mock(ReportMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        ObjectMapper objectMapper = new ObjectMapper();

        UserContext.setCurrentUserId(10001L);
        ReportServiceImpl service = new ReportServiceImpl(reportMapper, productMapper, userMapper, tradeOrderMapper, notificationService, objectMapper);
        ReportSubmitDTO dto = buildSubmitDTO(10001L, 2, 1, "举报自己", null);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.submitReport(dto));
        Assertions.assertEquals("不能举报自己", ex.getMsg());
    }

    @Test
    void testSubmitReport_Duplicate() {
        ReportMapper reportMapper = Mockito.mock(ReportMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        ObjectMapper objectMapper = new ObjectMapper();

        UserContext.setCurrentUserId(10001L);
        Product product = buildProduct(1L, 10002L, ProductStatus.ON_SALE.getCode(), 0);
        Mockito.when(productMapper.selectById(1L)).thenReturn(product);
        Mockito.when(reportMapper.selectOne(Mockito.any())).thenReturn(new Report());

        ReportServiceImpl service = new ReportServiceImpl(reportMapper, productMapper, userMapper, tradeOrderMapper, notificationService, objectMapper);
        ReportSubmitDTO dto = buildSubmitDTO(1L, 1, 1, "重复举报", null);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.submitReport(dto));
        Assertions.assertEquals("您已举报过该目标", ex.getMsg());
    }

    @Test
    void testSubmitReport_TargetNotExist() {
        ReportMapper reportMapper = Mockito.mock(ReportMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        ObjectMapper objectMapper = new ObjectMapper();

        UserContext.setCurrentUserId(10001L);
        Mockito.when(productMapper.selectById(1L)).thenReturn(null);

        ReportServiceImpl service = new ReportServiceImpl(reportMapper, productMapper, userMapper, tradeOrderMapper, notificationService, objectMapper);
        ReportSubmitDTO dto = buildSubmitDTO(1L, 1, 1, "不存在商品", null);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.submitReport(dto));
        Assertions.assertEquals("举报目标不存在", ex.getMsg());
    }

    @Test
    void testHandleReport_OffShelf() {
        ReportMapper reportMapper = Mockito.mock(ReportMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        ObjectMapper objectMapper = new ObjectMapper();

        Report report = buildReport(1L, 1, 0, 10L);
        Product product = buildProduct(10L, 10002L, ProductStatus.ON_SALE.getCode(), 0);
        Mockito.when(reportMapper.selectById(1L)).thenReturn(report);
        Mockito.when(productMapper.selectById(10L)).thenReturn(product);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);
        Mockito.when(reportMapper.updateById(Mockito.any(Report.class))).thenReturn(1);

        ReportServiceImpl service = new ReportServiceImpl(reportMapper, productMapper, userMapper, tradeOrderMapper, notificationService, objectMapper);
        ReportHandleDTO dto = buildHandleDTO(1L, "off_shelf", null);

        service.handleReport(dto, 9001L);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productMapper).updateById(productCaptor.capture());
        Assertions.assertEquals(ProductStatus.OFF_SHELF.getCode(), productCaptor.getValue().getStatus());

        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        Mockito.verify(reportMapper).updateById(reportCaptor.capture());
        Assertions.assertEquals(1, reportCaptor.getValue().getStatus());
        Assertions.assertEquals("商品已强制下架", reportCaptor.getValue().getHandleResult());
    }

    @Test
    void testHandleReport_Warn() {
        ReportMapper reportMapper = Mockito.mock(ReportMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        ObjectMapper objectMapper = new ObjectMapper();

        Report report = buildReport(1L, 2, 0, 10002L);
        Mockito.when(reportMapper.selectById(1L)).thenReturn(report);
        Mockito.when(reportMapper.updateById(Mockito.any(Report.class))).thenReturn(1);

        ReportServiceImpl service = new ReportServiceImpl(reportMapper, productMapper, userMapper, tradeOrderMapper, notificationService, objectMapper);
        ReportHandleDTO dto = buildHandleDTO(1L, "warn", null);

        service.handleReport(dto, 9001L);

        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        Mockito.verify(reportMapper).updateById(reportCaptor.capture());
        Assertions.assertEquals(1, reportCaptor.getValue().getStatus());
        Assertions.assertEquals("已警告用户", reportCaptor.getValue().getHandleResult());
    }

    @Test
    void testHandleReport_Ban() {
        ReportMapper reportMapper = Mockito.mock(ReportMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        ObjectMapper objectMapper = new ObjectMapper();

        Report report = buildReport(1L, 2, 0, 10002L);
        User user = buildUser(10002L);
        TradeOrder order = buildOrder(1L, 10002L, 10003L, 10L);
        Product product = buildProduct(10L, 10002L, ProductStatus.SOLD.getCode(), 0);

        Mockito.when(reportMapper.selectById(1L)).thenReturn(report);
        Mockito.when(userMapper.selectById(10002L)).thenReturn(user);
        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);
        Mockito.when(userMapper.offShelfAllProducts(10002L)).thenReturn(1);
        Mockito.when(tradeOrderMapper.selectList(Mockito.any())).thenReturn(List.of(order));
        Mockito.when(tradeOrderMapper.updateById(Mockito.any(TradeOrder.class))).thenReturn(1);
        Mockito.when(productMapper.selectById(10L)).thenReturn(product);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);
        Mockito.when(reportMapper.updateById(Mockito.any(Report.class))).thenReturn(1);

        ReportServiceImpl service = new ReportServiceImpl(reportMapper, productMapper, userMapper, tradeOrderMapper, notificationService, objectMapper);
        ReportHandleDTO dto = buildHandleDTO(1L, "ban", "违规");

        service.handleReport(dto, 9001L);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userMapper).updateById(userCaptor.capture());
        Assertions.assertEquals("违规", userCaptor.getValue().getBanReason());

        ArgumentCaptor<TradeOrder> orderCaptor = ArgumentCaptor.forClass(TradeOrder.class);
        Mockito.verify(tradeOrderMapper).updateById(orderCaptor.capture());
        Assertions.assertEquals(5, orderCaptor.getValue().getStatus());

        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        Mockito.verify(reportMapper).updateById(reportCaptor.capture());
        Assertions.assertEquals(1, reportCaptor.getValue().getStatus());
    }

    @Test
    void testHandleReport_Ignore() {
        ReportMapper reportMapper = Mockito.mock(ReportMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        ObjectMapper objectMapper = new ObjectMapper();

        Report report = buildReport(1L, 2, 0, 10002L);
        Mockito.when(reportMapper.selectById(1L)).thenReturn(report);
        Mockito.when(reportMapper.updateById(Mockito.any(Report.class))).thenReturn(1);

        ReportServiceImpl service = new ReportServiceImpl(reportMapper, productMapper, userMapper, tradeOrderMapper, notificationService, objectMapper);
        ReportHandleDTO dto = buildHandleDTO(1L, "ignore", null);

        service.handleReport(dto, 9001L);

        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        Mockito.verify(reportMapper).updateById(reportCaptor.capture());
        Assertions.assertEquals(2, reportCaptor.getValue().getStatus());
        Assertions.assertEquals("举报已忽略", reportCaptor.getValue().getHandleResult());
    }

    @Test
    void testHandleReport_AlreadyHandled() {
        ReportMapper reportMapper = Mockito.mock(ReportMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        ObjectMapper objectMapper = new ObjectMapper();

        Report report = buildReport(1L, 2, 1, 10002L);
        Mockito.when(reportMapper.selectById(1L)).thenReturn(report);

        ReportServiceImpl service = new ReportServiceImpl(reportMapper, productMapper, userMapper, tradeOrderMapper, notificationService, objectMapper);
        ReportHandleDTO dto = buildHandleDTO(1L, "warn", null);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.handleReport(dto, 9001L));
        Assertions.assertEquals("该举报已处理", ex.getMsg());
    }

    private ReportSubmitDTO buildSubmitDTO(Long targetId, Integer targetType, Integer reasonType, String description, List<String> evidence) {
        ReportSubmitDTO dto = new ReportSubmitDTO();
        dto.setTargetId(targetId);
        dto.setTargetType(targetType);
        dto.setReasonType(reasonType);
        dto.setDescription(description);
        dto.setEvidence(evidence);
        return dto;
    }

    private ReportHandleDTO buildHandleDTO(Long reportId, String action, String handleResult) {
        ReportHandleDTO dto = new ReportHandleDTO();
        dto.setReportId(reportId);
        dto.setAction(action);
        dto.setHandleResult(handleResult);
        return dto;
    }

    private Product buildProduct(Long id, Long userId, Integer status, Integer isDeleted) {
        Product product = new Product();
        product.setId(id);
        product.setUserId(userId);
        product.setStatus(status);
        product.setIsDeleted(isDeleted);
        return product;
    }

    private Report buildReport(Long id, Integer targetType, Integer status, Long targetId) {
        Report report = new Report();
        report.setId(id);
        report.setTargetType(targetType);
        report.setStatus(status);
        report.setTargetId(targetId);
        return report;
    }

    private User buildUser(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private TradeOrder buildOrder(Long id, Long buyerId, Long sellerId, Long productId) {
        TradeOrder order = new TradeOrder();
        order.setId(id);
        order.setBuyerId(buyerId);
        order.setSellerId(sellerId);
        order.setProductId(productId);
        return order;
    }
}
