/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.productprice.infrastructure.util;


import com.ymatou.productprice.infrastructure.config.props.BizProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 异常信息包装器
 * @author chenpengxuan
 */
@Component
public class LogWrapper {

    private static final Logger logger = LoggerFactory.getLogger(LogWrapper.class);

    @Autowired
    private BizProps bizProps;

    /**
     * 异常记录
     * @param msg
     */
    public void recordErrorLog(String msg){
        if(bizProps.getExceptionWarningSwitch()){
            logger.error(msg);
        }else{
            logger.debug(msg);
        }
    }

    /**
     * 异常记录
     * @param msg
     * @param objects
     */
    public void recordErrorLog(String msg,Object... objects){
        if(bizProps.getExceptionWarningSwitch()){
            logger.error(msg,objects);
        }else{
            logger.debug(msg,objects);
        }
    }

    /**
     * 普通消息日志记录
     * @param msg
     * @param objects
     */
    public void recordInfoLog(String msg,Object...objects){
        logger.info(msg,objects);
    }

    /**
     * DEBUG消息日志记录
     * @param msg
     * @param objects
     */
    public void recordDebugLog(String msg,Object...objects){
        logger.info(msg,objects);
    }
}
