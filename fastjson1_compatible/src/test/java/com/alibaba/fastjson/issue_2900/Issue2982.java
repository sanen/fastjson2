package com.alibaba.fastjson.issue_2900;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue2982 {
    @Test
    public void test_for_issue() {
        String jsonStr = "[ { \"activity_type\" : 0, \"activity_id\" : \"***\", \"activity_tip\" : \"***\", \"position\" : \"1\" }, { \"activity_type\" : 0, \"activity_id\" : \"2669\", \"activity_tip\" : \"****\", \"position\" : \"1\" }]";
        assertTrue(JSONArray.isValidArray(jsonStr));
        assertTrue(JSON.isValidArray(jsonStr));
        assertTrue(JSONObject.isValidArray(jsonStr));
    }


}
