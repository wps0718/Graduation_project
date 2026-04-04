package com.qingyuan.secondhand.config;

import lombok.Data;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "wx")
public class WxConfig {

    private String appId;
    private String appSecret;

    @Bean
    @Primary
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder.build();
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        List<org.springframework.http.MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(org.springframework.http.MediaType.TEXT_PLAIN);
        supportedMediaTypes.add(org.springframework.http.MediaType.APPLICATION_JSON);
        jacksonConverter.setSupportedMediaTypes(supportedMediaTypes);
        restTemplate.getMessageConverters().add(jacksonConverter);
        return restTemplate;
    }
}
