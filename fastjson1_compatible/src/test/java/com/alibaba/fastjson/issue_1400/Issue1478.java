package com.alibaba.fastjson.issue_1400;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1478 {
    @Test
    public void test_for_issue() throws Exception {
        Model model = new Model();
        model.md5 = "xxx";

        String json = JSON.toJSONString(model);
        assertEquals("{\"MD5\":\"xxx\"}", json);
    }

    public static class Model {
        private String md5;

        public String getMD5() throws Exception {
            return md5;
        }
    }
}
