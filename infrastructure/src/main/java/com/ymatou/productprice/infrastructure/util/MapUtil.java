package com.ymatou.productprice.infrastructure.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Maps;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ymatou.productprice.infrastructure.util.Utils.DEFAULT_DATE_FORMAT;

/**
 * Map Util
 * Created by chenpengxuan on 2017/2/7.
 */
public class MapUtil {
    /**
     * 将mapList转换成json字符串格式 例如：{xx:oo},{yy:xx}
     *
     * @param mapList
     * @return
     * @throws IllegalArgumentException
     */
    public static String makeJsonStringFromMap(List<Map<String, Object>> mapList) throws IllegalArgumentException {
        if (mapList == null || mapList.isEmpty()) {
            throw new IllegalArgumentException("mongo 待操作数据不能为空");
        }
        return String.join(",", mapList.stream().map(x -> makeJsonStringFromMap(x)).toArray(String[]::new));
    }

    /**
     * 将map转换成json字符串格式
     *
     * @return
     */
    public static String makeJsonStringFromMap(Map<String, Object> map) throws IllegalArgumentException {
        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException("mongo 待操作数据不能为空");
        }
        JSON.DEFFAULT_DATE_FORMAT = DEFAULT_DATE_FORMAT;
        return JSON.toJSONString(map, SerializerFeature.QuoteFieldNames);
    }

    /**
     * 将map转换成json字符串格式
     *
     * @return
     */
    public static String makeJsonStringFromMapForJongo(Map<String, Object> map) throws IllegalArgumentException {
        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException("mongo 待操作数据不能为空");
        }
        JSON.DEFFAULT_DATE_FORMAT = DEFAULT_DATE_FORMAT;
        return JSON.toJSONString(map, SerializerFeature.QuoteFieldNames).replaceAll("\"#\"","#");
    }

    /**
     * 将map转换成obj格式
     *
     * @param map
     * @return
     * @throws IllegalArgumentException
     */
    public static Object makeObjFromMap(Map<String, Object> map) throws IllegalArgumentException {
        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException("mongo 待操作数据不能为空");
        }
        JSON.DEFFAULT_DATE_FORMAT = DEFAULT_DATE_FORMAT;
        return JSON.toJSON(map);
    }

    /**
     * 将map转换成obj格式
     *
     * @param mapList
     * @return
     * @throws IllegalArgumentException
     */
    public static Object[] makeObjFromMap(List<Map<String, Object>> mapList) throws IllegalArgumentException {
        if (mapList == null || mapList.isEmpty()) {
            throw new IllegalArgumentException("mongo 待操作数据不能为空");
        }
        JSON.DEFFAULT_DATE_FORMAT = DEFAULT_DATE_FORMAT;
        return mapList.stream().map(x -> JSON.toJSON(x)).toArray();
    }

    /**
     * Map转换为list
     *
     * @param map
     * @return
     */
    public static List<Map<String, Object>> mapToList(Map<String, Object> map) {
        return Stream.of(map).collect(Collectors.toList());
    }


    /**
     * 将maplist中的字符串转换为数组
     *
     * @param map
     * @param field
     * @param seperator
     */
    public static void mapFieldToStringArray(Map<String, Object> map, String field, String seperator) {
        if (map != null && map.containsKey(field) && map.get(field) != null) {
            map.replace(field, map.get(field), map.get(field).toString().split(seperator));
        }
    }

    /**
     * 将maplist中的字符串转换为数组
     *
     * @param mapList
     * @param field
     * @param seperator
     */
    public static void mapFieldToStringArray(List<Map<String, Object>> mapList, String field, String seperator) {
        if(mapList != null && !mapList.isEmpty()){
            mapList.stream().forEach(x -> mapFieldToStringArray(x,field,seperator));
        }
    }

    /**
     * 将选定键数组转换成嵌套子对象
     * @param mapList
     * @param fieldList 要转换的key数组
     * @param nestedObjKey 嵌套对象的key名
     * @param checkKey 嵌套聚合依据的key
     * @return
     *
     */
    public static List<Map<String, Object>> mapFieldArrayToNestedObj(List<Map<String, Object>> mapList, String[] fieldList, String nestedObjKey,String checkKey) {
        if (mapList == null) {return null;}
        List<Map<String, Object>> tempDataList = new ArrayList<>();
        mapList.forEach(data -> {
            if (!tempDataList.stream().anyMatch(x -> x.containsValue(data.get(checkKey)))) {
                List<Map<String, Object>> nestedDataList = new ArrayList<>();
                Map<String, Object> tempMap = new HashMap<>();
                tempMap.putAll(data);
                Arrays.stream(fieldList).forEach(key -> tempMap.remove(key));
                Map<String, Object> tempPropertyMap = new HashMap<>();
                Arrays.stream(fieldList).forEach(key ->
                    tempPropertyMap.put(key,data.get(key))
                );
                if(!Maps.filterValues(tempPropertyMap,x -> x != null).isEmpty()) {
                    nestedDataList.add(tempPropertyMap);
                }
                if(!nestedDataList.isEmpty()) {
                    tempMap.put(nestedObjKey, nestedDataList);
                }else{
                    tempMap.put(nestedObjKey, null);
                }
                tempDataList.add(tempMap);
            } else {
                List<Map<String, Object>> tempNestedDataList = (List<Map<String, Object>>) tempDataList.stream()
                        .filter(x -> x.containsValue(data.get(checkKey))).findFirst()
                        .orElse(Collections.emptyMap()).get(nestedObjKey);
                Map<String, Object> tempPropertyMap = new HashMap<>();
                if(!tempNestedDataList.isEmpty())
                Arrays.stream(fieldList).forEach(key ->
                    tempPropertyMap.put(key,data.get(key))
                );
                if(!Maps.filterValues(tempPropertyMap,x -> x != null).isEmpty()) {
                    tempNestedDataList.add(tempPropertyMap);
                }
            }
        });
        return tempDataList;
    }
}
