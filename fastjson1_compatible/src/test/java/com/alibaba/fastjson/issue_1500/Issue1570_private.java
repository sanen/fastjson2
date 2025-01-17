package com.alibaba.fastjson.issue_1500;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1570_private {
    @Test
    public void test_for_issue() throws Exception {
        Model model = new Model();
        assertEquals("{}", JSON.toJSONString(model));
        assertEquals("{\"value\":\"\"}", JSON.toJSONString(model, SerializerFeature.WriteNullStringAsEmpty));
    }

    @Test
    public void test_for_issue_int() throws Exception {
        ModelInt model = new ModelInt();
        assertEquals("{}", JSON.toJSONString(model));
        assertEquals("{\"value\":0}", JSON.toJSONString(model, SerializerFeature.WriteNullNumberAsZero));
    }

    @Test
    public void test_for_issue_long() throws Exception {
        ModelLong model = new ModelLong();
        assertEquals("{}", JSON.toJSONString(model));
        assertEquals("{\"value\":0}", JSON.toJSONString(model, SerializerFeature.WriteNullNumberAsZero));
    }

    @Test
    public void test_for_issue_bool() throws Exception {
        ModelBool model = new ModelBool();
        assertEquals("{}", JSON.toJSONString(model));
        assertEquals("{\"value\":false}", JSON.toJSONString(model, SerializerFeature.WriteNullBooleanAsFalse));
    }

    @Test
    public void test_for_issue_list() throws Exception {
        ModelList model = new ModelList();
        assertEquals("{}", JSON.toJSONString(model));
        assertEquals("{\"value\":[]}", JSON.toJSONString(model, SerializerFeature.WriteNullListAsEmpty));
    }

    private static class Model {
        public String value;
    }

    private static class ModelInt {
        public Integer value;
    }

    private static class ModelLong {
        public Long value;
    }

    private static class ModelBool {
        public Boolean value;
    }

    private static class ModelList {
        public List value;
    }
}
