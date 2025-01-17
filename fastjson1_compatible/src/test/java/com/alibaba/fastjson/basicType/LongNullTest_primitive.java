package com.alibaba.fastjson.basicType;

import com.alibaba.fastjson.JSON;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by wenshao on 10/08/2017.
 */
public class LongNullTest_primitive {
    @Test
    public void test_null() throws Exception {
        Model model = JSON.parseObject("{\"v1\":null,\"v2\":null}", Model.class);
        assertNotNull(model);
        assertEquals(0, model.v1);
        assertEquals(0,model.v2);
    }

    @Test
    public void test_null_1() throws Exception {
        Model model = JSON.parseObject("{\"v1\":null ,\"v2\":null }", Model.class);
        assertNotNull(model);
        assertEquals(0,model.v1);
        assertEquals(0,model.v2);
    }

    @Test
    public void test_null_2() throws Exception {
        Model model = JSON.parseObject("{\"v1\":\"null\",\"v2\":\"null\" }", Model.class);
        assertNotNull(model);
        assertEquals(0,model.v1);
        assertEquals(0,model.v2);
    }

    public static class Model {
        public long v1;
        public long v2;
    }
}
