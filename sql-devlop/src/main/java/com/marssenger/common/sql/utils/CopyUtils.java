package com.marssenger.common.sql.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class CopyUtils {


    public static void copy(Object source, Object target) {
        //此处处理的是一个对象 如果是Map需自行验证是否可行
        if (source.getClass().equals(target.getClass())) {
            Field[] fields = getAllFields(source.getClass());
            for (Field field : fields) {
                try {
                    //抑制Java对修饰符的检查
                    field.setAccessible(true);
                    Object obj = field.get(source);
                    field.set(target, obj);
                } catch (IllegalAccessException e) {
                    log.warn("field:"+field.getName()+",不能复制！");
                }
            }
        } else {
            throw new RuntimeException("type not match!");
        }
    }


    /**
     * 获取本类以及父类的属性方法
     *
     * @param clazz 当前类对象
     * @return 字段数组
     */

    private static Field[] getAllFields(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        Field[] fields = new Field[fieldList.size()];
        return fieldList.toArray(fields);
    }
}
