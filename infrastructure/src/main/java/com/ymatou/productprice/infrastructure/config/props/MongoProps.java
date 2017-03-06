package com.ymatou.productprice.infrastructure.config.props;

/**
 * Created by zhangyifan on 2016/12/09.
 */

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import org.springframework.stereotype.Component;

@Component
@DisconfFile(fileName = "mongo.properties")
public class MongoProps {
    private String mongoProductUrl;

    @DisconfFileItem(name = "mongoProductUri")
    public String getMongoProductUrl() {
        return mongoProductUrl;
    }

    public void setMongoProductUrl(String mongoProductUrl) {
        this.mongoProductUrl = mongoProductUrl;
    }
}
