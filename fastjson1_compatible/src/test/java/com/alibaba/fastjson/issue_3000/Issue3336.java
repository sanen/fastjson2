package com.alibaba.fastjson.issue_3000;

import com.alibaba.fastjson.JSON;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3336 {
    @Test
    public void test_for_issue() throws Exception {
        String s = "{\"schema\":{\"$ref\":\"#/definitions/URLJumpConfig\"}}";
        assertEquals(s, JSON.parseObject(s)
                .toJSONString());

        String s1 = "{\"schema\":{\"ref\":\"#/definitions/URLJumpConfig\"}}";
        assertEquals(s1, JSON.parseObject(s1)
                .toJSONString());

        String s2 = "{\"schema\":{\"$ref\":\"#/definitions/URLJumpConfig\"}}";
        assertEquals(s2, JSON.parseObject(s2)
                .toJSONString());
    }
}
