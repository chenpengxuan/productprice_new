package com.ymatou.productprice.infrastructure.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Optional;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

/**
 * Created by zhangyifan on 2016/12/14.
 */
public class Utils {
    /**
     * 默认格式
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private static volatile String localIp;

    public static String uuid() {
        return new ObjectId().toHexString();
    }

    /**
     * 处理null问题
     *
     * @return
     */
    public static BigDecimal zeroIfNull(BigDecimal number) {
        BigDecimal zero = BigDecimal.ZERO;
        return optional(number, zero);
    }

    /**
     * 处理null问题
     *
     * @return
     */
    public static Double zeroIfNull(Double number) {
        Double zero = 0D;
        return optional(number, zero);
    }

    /**
     * 处理null问题
     *
     * @return
     */
    public static Integer zeroIfNull(Integer number) {
        Integer zero = 0;
        return optional(number, zero);
    }

    /**
     * 处理null问题
     *
     * @return
     */
    public static Float zeroIfNull(Float number) {
        Float zero = 0F;
        return optional(number, zero);
    }

    /**
     * 处理null问题
     *
     * @return
     */
    public static Long zeroIfNull(Long number) {
        Long zero = 0L;
        return optional(number, zero);
    }

    /**
     * 处理null问题
     *
     * @param b
     * @return
     */
    public static boolean falseIfNull(Boolean b) {
        return optional(b, false);
    }

    private static <T> T optional(T t, T defaultValue) {
        return Optional.fromNullable(t).or(defaultValue);
    }

    /**
     * 空转换器
     * @param t
     * @param defaultValue
     * @param <T>
     * @return
     */
    public static <T> T makeNullDefaultValue(T t, T defaultValue) {
        //针对基本类型，不为nullable，则进行特殊处理
        return t.getClass().isPrimitive() && t == null ? defaultValue : optional(t,defaultValue);
    }

    /**
     * 本机IP用于抢占定时任务，必须获取成功
     *
     * @return
     */
    public static String localIp() {
        if (localIp != null) {
            return localIp;
        }
        synchronized (Utils.class) {
            if (localIp == null) {
                try {
                    Enumeration<NetworkInterface> netInterfaces = NetworkInterface
                            .getNetworkInterfaces();

                    while (netInterfaces.hasMoreElements() && localIp == null) {
                        NetworkInterface ni = netInterfaces.nextElement();
                        if (!ni.isLoopback() && ni.isUp() && !ni.isVirtual()) {
                            Enumeration<InetAddress> address = ni.getInetAddresses();

                            while (address.hasMoreElements() && localIp == null) {
                                InetAddress addr = address.nextElement();

                                if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()
                                        && !(addr.getHostAddress().indexOf(":") > -1)) {
                                    localIp = addr.getHostAddress();

                                }
                            }
                        }
                    }

                } catch (Throwable t) {
                    throw new RuntimeException("Failed to fetch local ip", t);
                }
            }

            if (localIp == null || "127.0.0.1".equals(localIp)) {
                throw new RuntimeException("Failed to fetch local ip:" + localIp);
            }

            return localIp;
        }
    }

    /**
     * 对象转json字符串
     *
     * @param object
     * @return
     */
    public static String toJSONString(Object object) {
        JSON.DEFFAULT_DATE_FORMAT = DEFAULT_DATE_FORMAT;
        return JSON.toJSONString(object, SerializerFeature.WriteDateUseDateFormat, SerializerFeature.SortField);
    }

    public static Date addDate(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, day);
        return calendar.getTime();
    }

    /**
     * 获取当前时间时间戳
     * @return
     */
    public static Timestamp getNow(){
        return new Timestamp(new Date().getTime());
    }

    /**
     * 根据指定时间获取对应时间戳
     * @param dateTime
     * @return
     */
    public static Timestamp getTimestamp(String dateTime){
        return Timestamp.valueOf(dateTime);
    }

    /**
     * double格式化，保留几位小数
     *
     * @param value
     * @param digit
     * @return
     */
    public static double doubleFormat(double value, int digit) {
        BigDecimal bg = new BigDecimal(value);
        double doubleValue = bg.setScale(digit, BigDecimal.ROUND_HALF_UP).doubleValue();
        return doubleValue;
    }

    /**
     * decimal格式化，保留几位小数
     * @param value
     * @param digit
     * @return
     */
    public static double decimalFormat(BigDecimal value, int digit) {
        double doubleValue = value.setScale(digit, BigDecimal.ROUND_HALF_UP).doubleValue();
        return doubleValue;
    }

    /**
     * 日期处理
     * @param dateTime
     * @return
     */
    public static DateTime parseDateTime(String dateTime){
        DateTimeFormatter format = DateTimeFormat.forPattern(DEFAULT_DATE_FORMAT);
        return DateTime.parse(dateTime,format);
    }
}
