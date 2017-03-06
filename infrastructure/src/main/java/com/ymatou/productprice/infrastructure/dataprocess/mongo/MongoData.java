package com.ymatou.productprice.infrastructure.dataprocess.mongo;

import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * mongo 操作抽象
 * Created by chenpengxuan on 2017/2/6.
 */
public class MongoData{
    /**
     * mongo表名
     */
    private String tableName;

    /**
     * mongo 匹配条件
     */
    private Map<String,Object> matchCondition;

    public List<Map<String, Object>> getUpdateData() {
        return updateData;
    }

    public void setUpdateData(List<Map<String, Object>> updateData) {
        this.updateData = updateData;
    }

    /**
     * 需要更新的数据 Map<String, Object>表示一行数据
     */
    private List<Map<String, Object>> updateData;

    /**
     * mongo操作类型
     */
    private MongoOperationTypeEnum operationType;

    public MongoOperationTypeEnum getOperationType() {
        return operationType;
    }

    public void setOperationType(MongoOperationTypeEnum operationType) {
        this.operationType = operationType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<String, Object> getMatchCondition() {
        return matchCondition;
    }

    public void setMatchCondition(Map<String, Object> matchCondition) {
        this.matchCondition = matchCondition;
    }


    @Override
    public String toString()
    {
        String output= operationType+"   "+tableName+"  ";
        if(!CollectionUtils.isEmpty(matchCondition))
        {
            output += matchCondition.toString();
        }else
        {
            output += "conditions is empty";
        }
        return output;
    }
}
