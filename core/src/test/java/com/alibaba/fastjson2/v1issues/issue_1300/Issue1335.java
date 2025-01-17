package com.alibaba.fastjson2.v1issues.issue_1300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 22/07/2017.
 */
public class Issue1335 {
    @Test
    public void test_for_issue() throws Exception {
        String json = "{\n" +
                "\"id\": \"21496a63f5\",\n" +
                "\"title\": \"\",\n" +
                "\"url\": \"http://hl-img.peco.uodoo.com/hubble-test/app/sm/e9b884c1dcd671f128bac020e070e273.jpg;,,JPG;3,208x\",\n" +
                "\"type\": \"JPG\",\n" +
                "\"optimal_width\": 400,\n" +
                "\"optimal_height\": 267,\n" +
                "\"original_save_url\": \"http://hl-img.peco.uodoo.com/hubble-test/app/sm/e9b884c1dcd671f128bac020e070e273.jpg\",\n" +
                "\"phash\": \"62717D190987A7AE\"\n" +
                "                            }";
        Image image = JSON.parseObject(json, Image.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals("21496a63f5", image.id);
        assertEquals("http://hl-img.peco.uodoo.com/hubble-test/app/sm/e9b884c1dcd671f128bac020e070e273.jpg;,,JPG;3,208x", image.url);
        assertEquals("", image.title);
        assertEquals("JPG", image.type);
        assertEquals(400, image.optimalWidth);
        assertEquals(267, image.optimalHeight);
        assertEquals("http://hl-img.peco.uodoo.com/hubble-test/app/sm/e9b884c1dcd671f128bac020e070e273.jpg", image.original_save_url);
        assertEquals("62717D190987A7AE", image.phash);
    }

    public static class Image {
        public String id;
        public String title;
        public String url;
        public String type;
        public int optimalWidth;
        public int optimalHeight;
        public String original_save_url;
        public String phash;
    }
}
