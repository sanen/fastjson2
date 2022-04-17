package com.alibaba.fastjson2.v1issues.issue_3500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import junit.framework.TestCase;

import java.math.BigDecimal;

public class Issue3579 extends TestCase {
    public void test_for_issue() throws Exception {
        assertEquals("1",
                JSON.toJSONString(new BigDecimal("1"))
        );

        assertEquals("1",
                JSON.toJSONString(new BigDecimal("1"), JSONWriter.Feature.WriteClassName)
        );
    }
}