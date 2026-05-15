package com.qingyuan.secondhand.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

public class ImageJsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static List<String> parseImages(String imagesJson) {
        if (!StringUtils.hasText(imagesJson)) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static String parseCoverImage(String imagesJson) {
        List<String> images = parseImages(imagesJson);
        return images.isEmpty() ? null : images.get(0);
    }
}
