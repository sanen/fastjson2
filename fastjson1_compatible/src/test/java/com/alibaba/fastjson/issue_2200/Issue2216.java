package com.alibaba.fastjson.issue_2200;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2216 {
    @Test
    public void test_for_issue() throws Exception {
        Model model = JSON.parseObject("{\"value\":\"20181229162849000+0800\"}", Model.class);
        assertNotNull(model);
        assertNotNull(model.value);
        assertEquals(1546072129000L, model.value.getTime());
    }

    @Test
    public void test_for_issue_2() throws Exception {
        Model model = JSON.parseObject("{\"value\":\"20181229162849000+0800\"}").toJavaObject(Model.class);
        assertNotNull(model);
        assertNotNull(model.value);
        assertEquals(1546072129000L, model.value.getTime());
    }

    public static class Model {
        @JSONField(format = "yyyyMMddHHmmssSSSZ")
        public Date value;
    }
}
