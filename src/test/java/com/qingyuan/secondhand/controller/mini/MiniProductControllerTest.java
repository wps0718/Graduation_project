package com.qingyuan.secondhand.controller.mini;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.service.ProductCommentService;
import com.qingyuan.secondhand.service.ProductService;
import com.qingyuan.secondhand.vo.ProductDetailVO;
import com.qingyuan.secondhand.vo.ProductListVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MiniProductControllerTest {

    private MockMvc mockMvc;

    private ProductService productService;
    private ProductCommentService productCommentService;

    @BeforeEach
    void setup() {
        productService = mock(ProductService.class);
        productCommentService = mock(ProductCommentService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new MiniProductController(productService, productCommentService)).build();
    }

    @Test
    void testGetProductDetail() throws Exception {
        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(1L);
        vo.setTitle("商品");
        when(productService.getProductDetail(1L)).thenReturn(vo);

        mockMvc.perform(get("/mini/product/detail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    void testGetProductList() throws Exception {
        Page<ProductListVO> page = new Page<>(1, 10);
        page.setTotal(0);
        when(productService.getProductList(1, 10, 1L, 2L, "手机", new BigDecimal("10"), new BigDecimal("100"), "latest"))
                .thenReturn(page);

        mockMvc.perform(get("/mini/product/list")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("campusId", "1")
                        .param("categoryId", "2")
                        .param("keyword", "手机")
                        .param("minPrice", "10")
                        .param("maxPrice", "100")
                        .param("sortBy", "latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    void testGetMyProductList() throws Exception {
        Page<ProductListVO> page = new Page<>(1, 10);
        page.setTotal(0);
        when(productService.getMyProductList(1, 10, 1)).thenReturn(page);

        mockMvc.perform(get("/mini/product/my-list")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }
}
