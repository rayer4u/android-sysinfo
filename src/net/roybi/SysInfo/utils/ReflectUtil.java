package net.roybi.SysInfo.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ReflectUtil {
    public static Map<String, Object> GetClassFileds(Object obj) {
        Field[] fs = obj.getClass().getFields();
        Map<String, Object> map = new HashMap<String, Object>();
        for (Field f : fs) {
            if (f.getModifiers() == (Modifier.PUBLIC)) {
                try {
                    map.put(f.getName(), f.get(obj));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }
}
