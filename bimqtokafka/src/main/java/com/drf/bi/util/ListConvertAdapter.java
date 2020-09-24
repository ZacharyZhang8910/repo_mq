package com.drf.bi.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * List转换获取参数值适配器
 *
 * @param <T> 返回的数据类型
 * @param <V> 数据源类型
 * @author jian.zhang
 * DateTime: 2019-06-29 20:53:13
 */
public class ListConvertAdapter<T, V> {
    /**
     * 生成类属性对象List集合
     */
    private List<V> objects;

    /**
     * 需要生成List的类属性名称
     */
    private String propertyName;

    /**
     * 构造方法： 适配器在创建时，必须传List参数和生成属性名称
     *
     * @param objects      List集合
     * @param propertyName 需要生成List的类属性名称
     */
    public ListConvertAdapter(List<V> objects, String propertyName) {
        this.objects = objects;
        this.propertyName = propertyName;

        if (!validConvertParams()) {
            throw new RuntimeException("传入参数为空，objects=" + objects + ", propertyName=" + propertyName);
        }
    }

    /**
     * List集合对象的某一个属性转换为Object[]集合
     *
     * <p>获取属性list去重复，返回数组</p>
     *
     * @return Object[]
     */
    public Object[] getUnRepeatElementsArray() {
        Set<T> objectPropertyElements = getUnRepeatElements();
        if (CollectionUtils.isEmpty(objectPropertyElements)) {
            return null;
        }
        return objectPropertyElements.toArray();
    }

    /**
     * List集合对象的某一个属性转换为Set集合
     *
     * <p>获取属性list去重复，返回HashSet</p>
     *
     * @return HashSet
     */
    private Set<T> getUnRepeatElements() {
        List<T> objectPropertyElements = getElements();
        if (CollectionUtils.isEmpty(objectPropertyElements)) {
            return new HashSet<>(0);
        }
        Set<T> objectPropertyElementSet = Sets.newHashSet();
        objectPropertyElementSet.addAll(objectPropertyElements);
        return objectPropertyElementSet;
    }

    /**
     * List集合对象的某一个属性转换为Object[]集合
     *
     * @return Object[]
     */
    public Object[] getElementsArray() {
        List<T> objectPropertyElements = getElements();
        if (CollectionUtils.isEmpty(objectPropertyElements)) {
            return null;
        }
        return objectPropertyElements.toArray();
    }

    /**
     * List集合对象的某一个属性转换为List集合
     *
     * <p>获取属性list不去重复，返回ArrayList</p>
     *
     * @return List<T>
     */
    @SuppressWarnings("unchecked")
    private List<T> getElements() {
        List<T> objectPropertyElements = Lists.newArrayList();
        for (V v : objects) {
            Class<?> clazz = v.getClass();
            // 查询属性在类中存不存在
            // private方法查询
            Field field = null;
            try {
                field = clazz.getDeclaredField(propertyName);
            } catch (NoSuchFieldException e) {
                //ignore
            }
            // 查询不到找public方法
            if (field == null) {
                try {
                    field = clazz.getField(propertyName);
                } catch (NoSuchFieldException e) {
                    //ignore
                }
            }
            // 还是为空直接返回
            if (field == null) return getEmptyValues();

            // 获取方法名称
            StringBuilder nameBuffer = new StringBuilder();
            nameBuffer.append(ElementsMethod.GET.getMethodHeadCode()).append(propertyName);

            // 找出对应方法
            Method getPropertyNameMethod = null;
            Method[] methods = clazz.getMethods();
            if (ArrayUtils.isEmpty(methods)) {
                return getEmptyValues();
            }
            for (Method method : methods) {
                if (method.getName().toUpperCase().equals(nameBuffer.toString().toUpperCase())) {
                    getPropertyNameMethod = method;
                    break;
                }
            }

            // 找不到对应属性的GET方法
            if (getPropertyNameMethod == null) return getEmptyValues();

            try {
                objectPropertyElements.add((T) getPropertyNameMethod.invoke(v));
            } catch (IllegalAccessException | InvocationTargetException ex) {
                return getEmptyValues();
            }
        }

        return objectPropertyElements;
    }

    /**
     * 验证需要转换参数是否符合转换逻辑
     *
     * @return 符合转换逻辑返回true, 否则返回false
     */
    private boolean validConvertParams() {
        // 属性名称为空
        if (StringUtils.isBlank(propertyName)) {
            return false;
        }
        // 传入参数集合为空直接返回空list
        if (CollectionUtils.isEmpty(objects)) {
            return false;
        }
        for (V object : objects) {
            if (object == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * 验证不符合逻辑时，返回空List
     *
     * @return List<T>
     */
    private List<T> getEmptyValues() {
        return new ArrayList<>(0);
    }

    /**
     * 常用获取数组常量，参数取值可以扩展
     */
    public interface CommonPropertyAware {
        String RT_ITEM_NO = "rtItemNo";
        String RT_CATEGORY_ID = "rtCategoryId";
        String FF_CATEGORY_ID = "ffCategoryId";
        String ORDER_ITEM_ID = "orderItemId";
    }

    /**
     * 类属性方法Head枚举
     */
    protected enum ElementsMethod {

        /**
         * get方法
         */
        GET("get"),

        /**
         * boolean方法
         */
        IS("is"),

        /**
         * set方法
         */
        SET("set");

        /**
         * 方法头参数
         */
        private String methodHeadCode;

        /**
         * 构造方法
         *
         * @param methodHeadCode 对象中方法类型
         */
        ElementsMethod(String methodHeadCode) {
            this.methodHeadCode = methodHeadCode;
        }

        /**
         * 获取方法Head枚举
         *
         * @return String
         */
        private String getMethodHeadCode() {
            return methodHeadCode;
        }
    }
}
