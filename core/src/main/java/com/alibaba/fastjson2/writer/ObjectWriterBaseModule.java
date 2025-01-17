package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.modules.ObjectWriterAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.support.money.MoneySupport;
import com.alibaba.fastjson2.util.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;

class ObjectWriterBaseModule implements ObjectWriterModule {
    final ObjectWriterProvider provider;
    final WriterAnnotationProcessor annotationProcessor;

    public ObjectWriterBaseModule(ObjectWriterProvider provider) {
        this.provider = provider;
        this.annotationProcessor = new WriterAnnotationProcessor();
    }

    public ObjectWriterAnnotationProcessor getAnnotationProcessor() {
        return annotationProcessor;
    }

    class WriterAnnotationProcessor implements ObjectWriterAnnotationProcessor {
        @Override
        public void getBeanInfo(BeanInfo beanInfo, Class objectClass) {
            Class superclass = objectClass.getSuperclass();
            if (superclass != Object.class && superclass != null) {
                getBeanInfo(beanInfo, superclass);
            }

            Annotation jsonType1x = null;
            JSONType jsonType = null;
            Annotation[] annotations = objectClass.getAnnotations();
            for (Annotation annotation : annotations) {
                Class annotationType = annotation.annotationType();
                if (annotationType == JSONType.class) {
                    jsonType = (JSONType) annotation;
                    continue;
                }

                switch (annotationType.getName()) {
                    case "com.alibaba.fastjson.annotation.JSONType":
                        jsonType1x = annotation;
                        break;
                    default:
                        break;
                }
            }

            if (jsonType == null) {
                Class mixInSource = provider.mixInCache.get(objectClass);
                if (mixInSource == null) {
                    String typeName = objectClass.getName();
                    switch (typeName) {
                        case "org.apache.commons.lang3.tuple.ImmutablePair":
                            provider.mixIn(objectClass, mixInSource = ApacheLang3Support.PairMixIn.class);
                            break;
                        case "org.apache.commons.lang3.tuple.MutablePair":
                            provider.mixIn(objectClass, mixInSource = ApacheLang3Support.MutablePairMixIn.class);
                            break;
                        default:
                            break;
                    }
                }

                if (mixInSource != null) {
                    jsonType = (JSONType) mixInSource.getAnnotation(JSONType.class);
                }
            }

            if (jsonType != null) {
                Class<?>[] classes = jsonType.seeAlso();
                if (classes.length != 0) {
                    beanInfo.seeAlso = classes;
                }

                String typeKey = jsonType.typeKey();
                if (!typeKey.isEmpty()) {
                    beanInfo.typeKey = typeKey;
                }

                String typeName = jsonType.typeName();
                if (!typeName.isEmpty()) {
                    beanInfo.typeName = typeName;
                }

                for (JSONWriter.Feature feature : jsonType.writeFeatures()) {
                    beanInfo.writerFeatures |= feature.mask;
                }

                beanInfo.namingStrategy =
                        jsonType
                                .naming()
                                .name();

                String[] ignores = jsonType.ignores();
                if (ignores.length > 0) {
                    beanInfo.ignores = ignores;
                }
            } else if (jsonType1x != null) {
                final Annotation annotation = jsonType1x;
                BeanUtils.annatationMethods(jsonType1x.annotationType(), method -> processJSONType1x(beanInfo, annotation, method));
            }

            if (beanInfo.seeAlso != null && beanInfo.seeAlso.length != 0) {
                for (Class seeAlsoClass : beanInfo.seeAlso) {
                    if (seeAlsoClass == objectClass) {
                        beanInfo.typeName = objectClass.getSimpleName();
                    }
                }
            }
        }

        @Override
        public void getFieldInfo(FieldInfo fieldInfo, Class objectType, Field field) {
            Class mixInSource = provider.mixInCache.get(objectType);
            if (objectType == null) {
                String typeName = objectType.getName();
                switch (typeName) {
                    case "org.apache.commons.lang3.tuple.ImmutablePair":
                        provider.mixIn(objectType, mixInSource = ApacheLang3Support.PairMixIn.class);
                        break;
                    default:
                        break;
                }
            }

            if (mixInSource != null && mixInSource != objectType) {
                Field mixInField = null;
                try {
                    mixInField = mixInSource.getDeclaredField(field.getName());
                } catch (Exception ignored) {
                }

                if (mixInField != null) {
                    getFieldInfo(fieldInfo, mixInSource, mixInField);
                }
            }

            int modifiers = field.getModifiers();
            boolean isTransient = Modifier.isTransient(modifiers);
            if (isTransient) {
                fieldInfo.ignore = true;
            }

            JSONField jsonField = null;
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (annotationType == JSONField.class) {
                    jsonField = (JSONField) annotation;
                }
                String annotationTypeName = annotationType.getName();
                switch (annotationTypeName) {
                    case "com.fasterxml.jackson.annotation.JsonIgnore":
                        fieldInfo.ignore = true;
                        break;
                    case "com.alibaba.fastjson.annotation.JSONField":
                        processJSONField1x(fieldInfo, annotation);
                        break;
                    default:
                        break;
                }
            }

            if (jsonField == null) {
                return;
            }

            String jsonFIeldName = jsonField.name();
            if (!jsonFIeldName.isEmpty()) {
                fieldInfo.fieldName = jsonFIeldName;
            }

            String jsonFieldFormat = jsonField.format();
            if (!jsonFieldFormat.isEmpty()) {
                if (jsonFieldFormat.indexOf('T') != -1 && !jsonFieldFormat.contains("'T'")) {
                    jsonFieldFormat = jsonFieldFormat.replaceAll("T", "'T'");
                }

                fieldInfo.format = jsonFieldFormat;
            }

            if (!fieldInfo.ignore) {
                fieldInfo.ignore = !jsonField.write();
            }

            if (jsonField.unwrapped()) {
                fieldInfo.format = "unwrapped";
            }

            for (JSONWriter.Feature feature : jsonField.writeFeatures()) {
                fieldInfo.features |= feature.mask;
            }

            int ordinal = jsonField.ordinal();
            if (ordinal != 0) {
                fieldInfo.ordinal = ordinal;
            }

            Class writeUsing = jsonField.writeUsing();
            if (writeUsing != void.class
                    && writeUsing != Void.class
                    && ObjectWriter.class.isAssignableFrom(writeUsing)
            ) {
                fieldInfo.writeUsing = writeUsing;
            }
        }

        private void processJSONField1x(FieldInfo fieldInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annatationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    switch (name) {
                        case "name": {
                            String value = (String) result;
                            if (!value.isEmpty()) {
                                fieldInfo.fieldName = value;
                            }
                            break;
                        }
                        case "format": {
                            String format = (String) result;
                            if (!format.isEmpty()) {
                                if (format.indexOf('T') != -1 && !format.contains("'T'")) {
                                    format = format.replaceAll("T", "'T'");
                                }

                                fieldInfo.format = format;
                            }
                            break;
                        }
                        case "ordinal": {
                            Integer ordinal = (Integer) result;
                            if (ordinal.intValue() != 0) {
                                fieldInfo.ordinal = ordinal;
                            }
                            break;
                        }
                        case "serialize": {
                            Boolean serialize = (Boolean) result;
                            if (!serialize.booleanValue()) {
                                fieldInfo.ignore = true;
                            }
                            break;
                        }
                        case "unwrapped": {
                            Boolean unwrapped = (Boolean) result;
                            if (!unwrapped.booleanValue()) {
                                fieldInfo.format = "unwrapped";
                            }
                            break;
                        }
                        case "serialzeFeatures": {
                            Enum[] features = (Enum[]) result;
                            applyFeatures(fieldInfo, features);
                            break;
                        }
                        default:
                            break;
                    }
                } catch (Throwable ignored) {

                }
            });
        }

        private void applyFeatures(FieldInfo fieldInfo, Enum[] features) {
            for (Enum feature : features) {
                switch (feature.name()) {
                    case "UseISO8601DateFormat":
                        fieldInfo.format = "iso8601";
                        break;
                    case "WriteMapNullValue":
                        fieldInfo.features |= JSONWriter.Feature.WriteNulls.mask;
                        break;
                    case "WriteNullListAsEmpty":
                    case "WriteNullStringAsEmpty":
                        fieldInfo.features |= JSONWriter.Feature.NullAsDefaultValue.mask;
                        break;
                    case "BrowserCompatible":
                        fieldInfo.features |= JSONWriter.Feature.BrowserCompatible.mask;
                        break;
                    case "WriteClassName":
                        fieldInfo.features |= JSONWriter.Feature.WriteClassName.mask;
                        break;
                    case "WriteNonStringValueAsString":
                        fieldInfo.features |= JSONWriter.Feature.WriteNonStringValueAsString.mask;
                        break;
                    case "WriteEnumUsingToString":
                        fieldInfo.features |= JSONWriter.Feature.WriteEnumUsingToString.mask;
                        break;
                    case "NotWriteRootClassName":
                        fieldInfo.features |= JSONWriter.Feature.NotWriteRootClassName.mask;
                        break;
                    case "IgnoreErrorGetter":
                        fieldInfo.features |= JSONWriter.Feature.IgnoreErrorGetter.mask;
                        break;
                    default:
                        break;
                }
            }
        }

        @Override
        public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Method method) {
            Class mixInSource = provider.mixInCache.get(objectClass);
            String methodName = method.getName();

            if (mixInSource != null && mixInSource != objectClass) {
                Method mixInMethod = null;
                try {
                    mixInMethod = mixInSource.getDeclaredMethod(methodName, method.getParameterTypes());
                } catch (Exception ignored) {
                }

                if (mixInMethod != null) {
                    getFieldInfo(fieldInfo, mixInSource, mixInMethod);
                }
            }

            if (JDKUtils.CLASS_TRANSIENT != null && method.getAnnotation(JDKUtils.CLASS_TRANSIENT) != null) {
                fieldInfo.ignore = true;
            }

            JSONField jsonField = null;
            Annotation[] annotations = method.getAnnotations();
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (annotationType == JSONField.class) {
                    jsonField = (JSONField) annotation;
                }
                String annotationTypeName = annotationType.getName();
                switch (annotationTypeName) {
                    case "com.fasterxml.jackson.annotation.JsonIgnore":
                        fieldInfo.ignore = true;
                        break;
                    default:
                        break;
                }
            }

            if (jsonField != null) {
                String jsonFIeldName = jsonField.name();
                if (!jsonFIeldName.isEmpty()) {
                    fieldInfo.fieldName = jsonFIeldName;
                }

                String jsonFieldFormat = jsonField.format();
                if (!jsonFieldFormat.isEmpty()) {
                    if (jsonFieldFormat.indexOf('T') != -1 && !jsonFieldFormat.contains("'T'")) {
                        jsonFieldFormat = jsonFieldFormat.replaceAll("T", "'T'");
                    }

                    fieldInfo.format = jsonFieldFormat;
                }

                if (!fieldInfo.ignore) {
                    fieldInfo.ignore = !jsonField.write();
                }

                if (jsonField.unwrapped()) {
                    fieldInfo.format = "unwrapped";
                }

                for (JSONWriter.Feature feature : jsonField.writeFeatures()) {
                    fieldInfo.features |= feature.mask;
                }

                int ordinal = jsonField.ordinal();
                if (ordinal != 0) {
                    fieldInfo.ordinal = ordinal;
                }
            }

            String fieldName = BeanUtils.setterName(methodName, null);
            Field declaredField = null;
            try {
                declaredField = objectClass.getDeclaredField(fieldName);
            } catch (Throwable ignored) {
                // skip
            }

            if (declaredField != null) {
                int modifiers = declaredField.getModifiers();
                if ((!Modifier.isPublic(modifiers)) && !Modifier.isStatic(modifiers)) {
                    getFieldInfo(fieldInfo, objectClass, declaredField);
                }
            }
        }
    }

    private void processJSONType1x(BeanInfo beanInfo, Annotation jsonType1x, Method method) {
        try {
            Object result = method.invoke(jsonType1x);
            switch (method.getName()) {
                case "seeAlso": {
                    Class<?>[] classes = (Class[]) result;
                    if (classes.length != 0) {
                        beanInfo.seeAlso = classes;
                    }
                    break;
                }
                case "typeName": {
                    String typeName = (String) result;
                    if (!typeName.isEmpty()) {
                        beanInfo.typeName = typeName;
                    }
                    break;
                }
                case "typeKey": {
                    String typeKey = (String) result;
                    if (!typeKey.isEmpty()) {
                        beanInfo.typeKey = typeKey;
                    }
                    break;
                }
                case "serializeFeatures":
                case "serialzeFeatures": {
                    Enum[] serializeFeatures = (Enum[]) result;
                    for (Enum feature : serializeFeatures) {
                        switch (feature.name()) {
                            case "WriteMapNullValue":
                                beanInfo.writerFeatures |= JSONWriter.Feature.WriteNulls.mask;
                                break;
                            case "WriteNullListAsEmpty":
                            case "WriteNullStringAsEmpty":
                                beanInfo.writerFeatures |= JSONWriter.Feature.NullAsDefaultValue.mask;
                                break;
                            case "BrowserCompatible":
                                beanInfo.writerFeatures |= JSONWriter.Feature.BrowserCompatible.mask;
                                break;
                            case "WriteClassName":
                                beanInfo.writerFeatures |= JSONWriter.Feature.WriteClassName.mask;
                                break;
                            case "WriteNonStringValueAsString":
                                beanInfo.writerFeatures |= JSONWriter.Feature.WriteNonStringValueAsString.mask;
                                break;
                            case "WriteEnumUsingToString":
                                beanInfo.writerFeatures |= JSONWriter.Feature.WriteEnumUsingToString.mask;
                                break;
                            case "NotWriteRootClassName":
                                beanInfo.writerFeatures |= JSONWriter.Feature.NotWriteRootClassName.mask;
                                break;
                            case "IgnoreErrorGetter":
                                beanInfo.writerFeatures |= JSONWriter.Feature.IgnoreErrorGetter.mask;
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                }
                case "serializeEnumAsJavaBean": {
                    boolean serializeEnumAsJavaBean = (Boolean) result;
                    if (serializeEnumAsJavaBean) {
                        beanInfo.writeEnumAsJavaBean = true;
                    }
                    break;
                }
                case "naming": {
                    Enum naming = (Enum) result;
                    beanInfo.namingStrategy = naming.name();
                    break;
                }
                case "ignores": {
                    beanInfo.ignores = (String[]) result;
                    break;
                }
                default:
                    break;
            }
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
    }

    ObjectWriter getExternalObjectWriter(String className, Class objectClass) {
        switch (className) {
            case "java.sql.Time":
                return JdbcSupport.createTimeWriter();
            case "java.sql.Timestamp":
                return JdbcSupport.createTimestampWriter();
            case "org.joda.time.chrono.GregorianChronology":
                return JodaSupport.createGregorianChronologyWriter(objectClass);
            case "org.joda.time.chrono.ISOChronology":
                return JodaSupport.createISOChronologyWriter(objectClass);
            case "org.joda.time.LocalDate":
                return JodaSupport.createLocalDateWriter(objectClass);
            case "org.joda.time.LocalDateTime":
                return JodaSupport.createLocalDateTimeWriter(objectClass);
//            case "com.alibaba.fastjson.JSONObject":
//                return Fastjson1xSupport.createObjectReader();
            default:
                return null;
        }
    }

    public ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
        if (objectType == String.class) {
            return ObjectWriterImplString.INSTANCE;
        }

        if (objectClass == null) {
            if (objectType instanceof Class) {
                objectClass = (Class) objectType;
            } else {
                objectClass = TypeUtils.getMapping(objectType);
            }
        }

        String className = objectClass.getName();
        ObjectWriter externalObjectWriter = getExternalObjectWriter(className, objectClass);
        if (externalObjectWriter != null) {
            return externalObjectWriter;
        }

        switch (className) {
            case "com.google.common.collect.AbstractMapBasedMultimap$RandomAccessWrappedList":
            case "com.google.common.collect.AbstractMapBasedMultimap$WrappedSet":
                return null;
            case "org.javamoney.moneta.internal.JDKCurrencyAdapter":
                return ObjectWriterImplToString.INSTANCE;
            case "org.javamoney.moneta.Money":
                return MoneySupport.createMonetaryAmountWriter();
            case "org.javamoney.moneta.spi.DefaultNumberValue":
                return MoneySupport.createNumberValueWriter();
            default:
                break;
        }

        if (objectType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) objectType;
            Type rawType = parameterizedType.getRawType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            if (rawType == List.class || rawType == ArrayList.class) {
                if (actualTypeArguments.length == 1
                        && actualTypeArguments[0] == String.class) {

                    return ObjectWriterImplListStr.INSTANCE;
                }

                objectType = rawType;
            }
        }

        if (objectType == LinkedList.class) {
            return ObjectWriterImplList.INSTANCE;
        }

        if (objectType == ArrayList.class
                || objectType == List.class
                || List.class.isAssignableFrom(objectClass)) {

            return ObjectWriterImplList.INSTANCE;
        }

        if (Collection.class.isAssignableFrom(objectClass)) {
            return ObjectWriterImplCollection.INSTANCE;
        }

        if (Iterable.class.isAssignableFrom(objectClass)) {
            return ObjectWriterImplIterable.INSTANCE;
        }

        if (Map.class.isAssignableFrom(objectClass)) {
            return ObjectWriterImplMap.of(objectClass);
        }

        if (Map.Entry.class.isAssignableFrom(objectClass)) {
            String objectClassName = objectClass.getName();
            if (!objectClassName.equals("org.apache.commons.lang3.tuple.ImmutablePair") && !objectClassName.equals("org.apache.commons.lang3.tuple.MutablePair")
            ) {
                return ObjectWriterImplMapEntry.INSTANCE;
            }
        }

        if (objectType == Integer.class) {
            return ObjectWriterImplInt32.INSTANCE;
        }

        if (objectType == AtomicInteger.class) {

            return ObjectWriterImplAtomicInteger.INSTANCE;
        }

        if (objectType == Byte.class) {
            return ObjectWriterImplInt8.INSTANCE;
        }

        if (objectType == Short.class) {
            return ObjectWriterImplInt16.INSTANCE;
        }

        if (objectType == Long.class) {
            return ObjectWriterImplInt64.INSTANCE;
        }

        if (objectType == AtomicLong.class) {
            return ObjectWriterImplAtomicLong.INSTANCE;
        }

        if (objectType == Float.class) {
            return ObjectWriterImplFloat.INSTANCE;
        }

        if (objectType == Double.class) {
            return ObjectWriterImplDouble.INSTANCE;
        }

        if (objectType == BigInteger.class) {
            return ObjectWriterBigInteger.INSTANCE;
        }

        if (objectType == BigDecimal.class) {
            return ObjectWriterImplBigDecimal.INSTANCE;
        }

        if (objectType == OptionalInt.class) {
            return ObjectWriterImplOptionalInt.INSTANCE;
        }

        if (objectType == OptionalLong.class) {
            return ObjectWriterImplOptionalLong.INSTANCE;
        }

        if (objectType == OptionalLong.class) {
            return ObjectWriterImplOptionalLong.INSTANCE;
        }

        if (objectType == OptionalDouble.class) {
            return ObjectWriterImplOptionalDouble.INSTANCE;
        }

        if (objectType == Optional.class) {
            return ObjectWriterImplOptional.INSTANCE;
        }

        if (objectType == Boolean.class) {
            return ObjectWriterImplBoolean.INSTANCE;
        }

        if (objectType == AtomicBoolean.class) {
            return ObjectWriterImplAtomicBoolean.INSTANCE;
        }

        if (objectType == AtomicIntegerArray.class) {
            return ObjectWriterImplAtomicIntegerArray.INSTANCE;
        }

        if (objectType == AtomicLongArray.class) {
            return ObjectWriterImplAtomicLongArray.INSTANCE;
        }

        if (objectType == Character.class) {
            return ObjectWriterImplCharacter.INSTANCE;
        }

        if (objectType instanceof Class) {
            Class clazz = (Class) objectType;
            if (clazz.isEnum()) {
                return new ObjectWriterImplEnum(null, clazz, 0);
            }

            if (TimeUnit.class.isAssignableFrom(clazz)) {
                return new ObjectWriterImplEnum(null, TimeUnit.class, 0);
            }

            if (clazz == boolean[].class) {
                return ObjectWriterImplBoolValueArray.INSTANCE;
            }

            if (clazz == char[].class) {
                return ObjectWriterImplCharValueArray.INSTANCE;
            }

            if (clazz == byte[].class) {
                return ObjectWriterImplInt8ValueArray.INSTANCE;
            }

            if (clazz == short[].class) {
                return ObjectWriterImplInt16ValueArray.INSTANCE;
            }

            if (clazz == int[].class) {
                return ObjectWriterImplInt32ValueArray.INSTANCE;
            }

            if (clazz == long[].class) {
                return ObjectWriterImplInt64ValueArray.INSTANCE;
            }

            if (clazz == float[].class) {
                return ObjectWriterImplFloatValueArray.INSTANCE;
            }

            if (clazz == double[].class) {
                return ObjectWriterImplDoubleValueArray.INSTANCE;
            }

            if (clazz == Byte[].class) {
                return ObjectWriterImplInt8Array.INSTANCE;
            }

            if (clazz == Integer[].class) {
                return ObjectWriterImplInt32Array.INSTANCE;
            }

            if (clazz == Long[].class) {
                return ObjectWriterImplInt64Array.INSTANCE;
            }

            if (clazz == AtomicLongArray.class) {
                return ObjectWriterImplAtomicLongArray.INSTANCE;
            }

            if (String[].class == clazz) {
                return ObjectWriterImplStringArray.INSTANCE;
            }

            if (Object[].class.isAssignableFrom(clazz)) {
                if (clazz == Object[].class) {
                    return ObjectWriterArray.INSTANCE;
                } else {
                    return new ObjectWriterArray(clazz.getComponentType());
                }
            }

            if (clazz == UUID.class) {
                return ObjectWriterImplUUID.INSTANCE;
            }

            if (clazz == Locale.class) {
                return ObjectWriterImplLocale.INSTANCE;
            }

            if (clazz == Currency.class) {
                return ObjectWriterImplCurrency.INSTANCE;
            }

            if (TimeZone.class.isAssignableFrom(clazz)) {
                return ObjectWriterImplTimeZone.INSTANCE;
            }

            if (clazz == URI.class
                    || clazz == URL.class
                    || ZoneId.class.isAssignableFrom(clazz)
                    || Charset.class.isAssignableFrom(clazz)) {

                return ObjectWriterImplToString.INSTANCE;
            }

            if (clazz == Inet4Address.class || clazz == Inet4Address.class) {
                return ObjectWriterImplInetAddress.INSTANCE;
            }

            externalObjectWriter = getExternalObjectWriter(clazz.getName(), clazz);
            if (externalObjectWriter != null) {
                return externalObjectWriter;
            }

            if (Date.class.isAssignableFrom(clazz)) {
                return ObjectWriterImplDate.INSTANCE;
            }

            if (Calendar.class.isAssignableFrom(clazz)) {
                return ObjectWriterImplCalendar.INSTANCE;
            }

            if (ZonedDateTime.class == clazz) {
                return ObjectWriterImplZonedDateTime.INSTANCE;
            }

            if (OffsetDateTime.class == clazz) {
                return ObjectWriterImplOffsetDateTime.INSTANCE;
            }

            if (LocalDateTime.class == clazz) {
                return ObjectWriterImplLocalDateTime.INSTANCE;
            }

            if (LocalDate.class == clazz) {
                return ObjectWriterImplLocalDate.INSTANCE;
            }

            if (LocalTime.class == clazz) {
                return ObjectWriterImplLocalTime.INSTANCE;
            }

            if (OffsetTime.class == clazz) {
                return ObjectWriterImplOffsetTime.INSTANCE;
            }

            if (Instant.class == clazz) {
                return ObjectWriterImplInstant.INSTANCE;
            }

            if (StackTraceElement.class == clazz) {
                return ObjectWriterImplStackTraceElement.INSTANCE;
            }

            if (Class.class == clazz) {
                return ObjectWriterImplClass.INSTANCE;
            }

            if (ParameterizedType.class.isAssignableFrom(clazz)) {
                return ObjectWriters.objectWriter(
                        ParameterizedType.class,
                        ObjectWriters.fieldWriter("actualTypeArguments", Type[].class, ParameterizedType::getActualTypeArguments),
                        ObjectWriters.fieldWriter("ownerType", Type.class, ParameterizedType::getOwnerType),
                        ObjectWriters.fieldWriter("rawType", Type.class, ParameterizedType::getRawType)
                        );
            }
        }

        return null;
    }

    static abstract class PrimitiveImpl<T> implements ObjectWriter<T> {
        public void writeArrayMappingJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            writeJSONB(jsonWriter, object, null, null, 0);
        }

        public void writeArrayMapping(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            write(jsonWriter, object, null, null, 0);
        }
    }


}
