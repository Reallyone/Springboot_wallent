package com.really.spring_wallent.utils;


import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Utils {
    /**
     * 判断是否是一个数组
     *
     * @param obj
     * @return
     */
    public static boolean isArray(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj.getClass().isArray();
    }

    /**
     * @param str       原字符串
     * @param indexStr  指定字符串
     * @param isInclude 是否包括指定字符串
     * @return 截取后的字符串
     */
    public static String getLastindexStr(String str, String indexStr, boolean isInclude) {
        String result = "";
        int start = str.lastIndexOf(indexStr);
        if (start == -1) {
            return result = "";
        } else {
            if (isInclude) {
                result = str.substring(start);
            } else {
                result = str.substring(start + indexStr.length() + 1);
            }
            return result.substring(0, result.length() - 1);
        }
    }


    /**
     * 根据属性名获取属性值
     */
    public static Object getFieldValueByName(String fieldName, Object o) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = o.getClass().getMethod(getter, new Class[]{});
            Object value = method.invoke(o, new Object[]{});
            return value;
        } catch (Exception e) {
            return null;
        }
    }

    public static Object getValueByKey(Object obj, String key) {
        // 得到类对象
        Class userCla = (Class) obj.getClass();
        /* 得到类中的所有属性集合 */
        Field[] fs = userCla.getDeclaredFields();
        for (int i = 0; i < fs.length; i++) {
            Field f = fs[i];
            f.setAccessible(true); // 设置些属性是可以访问的
            try {
                if (f.getName().endsWith(key)) {
                    return f.get(obj);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        // 没有查到时返回空字符串
        return "";
    }

    public static String subString(String str, String strStart, String strEnd) {

        /* 找出指定的2个字符在 该字符串里面的 位置 */
        int strStartIndex = str.indexOf(strStart);
        int strEndIndex = str.indexOf(strEnd) + 1;

        /* index为负数 即表示该字符串中没有该字符 */
        if (strStartIndex < 0) {
            return "字符串 :" + str + "中不存在 " + strStart + ", 无法截取目标字符串";
        }
        if (strEndIndex < 0) {
            return "字符串 :" + str + "中不存在 " + strEnd + ", 无法截取目标字符串";
        }
        /* 开始截取 */
        String result = str.substring(strStartIndex, strEndIndex);
        return result;
    }


    /**
     * 使用ArrayUtils 判断数组中是否包含某个值
     *
     * @param targetValue
     * @return
     */
    public static boolean array_key_exists(Object[] obj, String targetValue) {
        return ArrayUtils.contains(obj, targetValue);
    }

    public static double round(double v, int number) {
        double a = Math.pow(10, number);
        int b = (int) a;
        //(这里的100就是2位小数点, 如果要其它位, 如4位, 这里两个100改成10000)
        double floatValue = (Math.round(v * 100)) / b;
        return floatValue;
    }

    /**
     * 把一个字符串转换成bean对象
     *
     * @param str
     * @param <T>
     * @return
     */
    public static <T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.length() <= 0 || clazz == null) {
            return null;
        }
        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(str);
        } else if (clazz == String.class) {
            return (T) str;
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(str);
        } else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }


    }

    /**
     * 方法说明：将bean转化为另一种bean实体
     *  
     *
     * @param object
     * @param entityClass
     * @return
     */
    public static <T> T convertBean(Object object, Class<T> entityClass) {
        if (null == object) {
            return null;
        }
        return JSON.parseObject(JSON.toJSONString(object), entityClass);
    }

    /**
     * 字符串转Map
     *
     * @param param {idCard:123, phonenum:1234}
     * @return
     */
    public static Object getValue(String param) {
        Map map = new HashMap();
        String str = "";
        String key = "";
        Object value = "";
        char[] charList = param.toCharArray();
        boolean valueBegin = false;
        for (int i = 0; i < charList.length; i++) {
            char c = charList[i];
            if (c == '{') {
                if (valueBegin == true) {
                    value = getValue(param.substring(i, param.length()));
                    i = param.indexOf('}', i) + 1;
                    map.put(key, value);
                }
            } else if (c == ':') {
                valueBegin = true;
                key = str;
                str = "";
            } else if (c == ',') {
                valueBegin = false;
                value = str;
                str = "";
                map.put(key, value);
            } else if (c == '}') {
                if (str != "") {
                    value = str;
                }
                map.put(key, value);
                return map;
            } else if (c != ' ') {
                str += c;
            }
        }
        return map;
    }


    public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
           Comparator<K> valueComparator = new Comparator<K>() {
            public int compare(K k1, K k2) {
                int compare = map.get(k2).compareTo(map.get(k1));
                if (compare == 0)
                    return 1;
                else
                    return compare;
            }

        };
        Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;

    }

}
