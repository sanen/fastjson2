package com.alibaba.fastjson.issue_1500;

import com.alibaba.fastjson.JSON;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1529 {
    @Test
    public void test_for_issue() throws Exception {
        String text = "{\"isId\":false,\"Id\":138042533,\"name\":\"example\",\"height\":172}";
        Person person = JSON.parseObject(text, Person.class);
        assertEquals(138042533, person.Id);
        assertEquals("example", person.name);
        assertEquals(172.0D, person.height);
    }

    public static class Person {
        private int Id;
        public String name;
        public double height;

        public int getId() {
            return Id;
        }

        public void setId(int id) {
            if (id <= 1) {
                throw new IllegalArgumentException();
            }
            Id = id;
        }
    }
}
