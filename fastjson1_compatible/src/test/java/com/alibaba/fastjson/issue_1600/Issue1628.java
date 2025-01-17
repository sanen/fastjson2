package com.alibaba.fastjson.issue_1600;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1628 {
    @Test
    public void test_toJSONBytes() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", 1001);
        map.put("b", 2002);
        byte[] bytes = JSON.toJSONBytes(map, new SimplePropertyPreFilter("a"));
        assertEquals("{\"a\":1001}", new String(bytes));
    }

    @Test
    public void test_toJSONBytes_1() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", 1001);
        map.put("b", 2002);
        byte[] bytes = JSON.toJSONBytes(map, new SerializeFilter[] {new SimplePropertyPreFilter("a")});
        assertEquals("{\"a\":1001}", new String(bytes));
    }

    @Test
    public void test_toJSONBytes_2() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", 1001);
        map.put("b", 2002);
        byte[] bytes = JSON.toJSONBytes(map, new SimplePropertyPreFilter("a"));
        assertEquals("{\"a\":1001}", new String(bytes));
    }
}
