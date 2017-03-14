package com.ymatou.productprice.infrastructure.config.props;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import org.springframework.stereotype.Component;

/**
 * 业务相关配置
 * Created by zhangyifan on 2016/12/21.
 */
@Component
@DisconfFile(fileName = "biz.properties")
public class BizProps {
    public BizProps() {

    }

    /**
     * 异常日志开关
     */
    private Boolean exceptionWarningSwitch;

    /**
     * restclient连接超时上限(单位毫秒)
     */
    private long restconnectiontimeout;

    /**
     * restclient读超时上限(单位毫秒)
     */
    private long restreadtimeout;

    /**
     * restclient写超时上限(单位毫秒)
     */
    private long restwritetimeout;

    /**
     * restclient连接池size
     */
    private int restconnectionpoolsize;

    /**
     * restclient连接存活时间(单位分钟)
     */
    private long restconnectionaliveduration;

    /**
     * 是否使用并行
     */
    private boolean useParallel;

    /**
     * 并行数
     */
    private int parallelCount;

    @DisconfFileItem(name = "exceptionWarningSwitch")
    public Boolean getExceptionWarningSwitch() {
        return exceptionWarningSwitch;
    }

    public void setExceptionWarningSwitch(Boolean exceptionWarningSwitch) {
        this.exceptionWarningSwitch = exceptionWarningSwitch;
    }

    @DisconfFileItem(name = "restconnectiontimeout")
    public long getRestconnectiontimeout() {
        return restconnectiontimeout;
    }

    public void setRestconnectiontimeout(long restconnectiontimeout) {
        this.restconnectiontimeout = restconnectiontimeout;
    }

    @DisconfFileItem(name = "restreadtimeout")
    public long getRestreadtimeout() {
        return restreadtimeout;
    }

    public void setRestreadtimeout(long restreadtimeout) {
        this.restreadtimeout = restreadtimeout;
    }

    @DisconfFileItem(name = "restwritetimeout")
    public long getRestwritetimeout() {
        return restwritetimeout;
    }

    public void setRestwritetimeout(long restwritetimeout) {
        this.restwritetimeout = restwritetimeout;
    }

    @DisconfFileItem(name = "restconnectionpoolsize")
    public int getRestconnectionpoolsize() {
        return restconnectionpoolsize;
    }

    public void setRestconnectionpoolsize(int restconnectionpoolsize) {
        this.restconnectionpoolsize = restconnectionpoolsize;
    }

    @DisconfFileItem(name = "restconnectionaliveduration")
    public long getRestconnectionaliveduration() {
        return restconnectionaliveduration;
    }

    public void setRestconnectionaliveduration(long restconnectionaliveduration) {
        this.restconnectionaliveduration = restconnectionaliveduration;
    }

    @DisconfFileItem(name = "useParallel")
    public boolean isUseParallel() {
        return useParallel;
    }

    public void setUseParallel(boolean useParallel) {
        this.useParallel = useParallel;
    }

    @DisconfFileItem(name = "parallelCount")
    public int getParallelCount() {
        return parallelCount;
    }

    public void setParallelCount(int parallelCount) {
        this.parallelCount = parallelCount;
    }
}
