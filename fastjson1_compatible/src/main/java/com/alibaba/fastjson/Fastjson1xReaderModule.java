package com.alibaba.fastjson;


import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.lang.reflect.*;

public class Fastjson1xReaderModule implements ObjectReaderModule {
    final ObjectReaderProvider provider;

    public Fastjson1xReaderModule(ObjectReaderProvider provider) {
        this.provider = provider;
    }

    public ObjectReader getObjectReader(ObjectReaderProvider provider, Type type) {
        if (type == JSON.class) {
            return new JSONImpl();
        }
        return null;
    }

    static class JSONImpl implements ObjectReader {
        public Object readObject(JSONReader jsonReader, long features) {
            if (jsonReader.isObject()) {
                return jsonReader.read(JSONObject.class);
            }
            if (jsonReader.isArray()) {
                return jsonReader.read(JSONArray.class);
            }

            throw new JSONException("read json error");
        }
    }
}
