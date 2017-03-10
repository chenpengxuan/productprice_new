package com.ymatou.productprice.infrastructure.dataprocess.mongo;

import com.ymatou.productprice.model.PrintFriendliness;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * mongo 查询操作抽象
 * Created by chenpengxuan on 2017/2/17.
 */
public class MongoQueryData extends PrintFriendliness{
    /**
     * mongo表名
     */
    private String tableName;

    /**
     * mongo 匹配条件
     */
    private Map<String,Object> matchCondition;

    /**
     * mongo操作类型
     */
    private MongoOperationTypeEnum operationType;

    /**
     * mongo 查询字段选择
     */
    private Map<String,Boolean> projection;

    /**
     * mongo 排序 true表示ASC false表示DESC
     */
    private Map<String,Boolean> sort;


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

    public MongoOperationTypeEnum getOperationType() {
        return operationType;
    }

    public void setOperationType(MongoOperationTypeEnum operationType) {
        this.operationType = operationType;
    }

    public Map<String, Boolean> getProjection() {
        return projection;
    }

    public void setProjection(Map<String, Boolean> projection) {
        this.projection = projection;
    }

    public Map<String, Boolean> getSort() {
        return sort;
    }

    public void setSort(Map<String, Boolean> sort) {
        this.sort = sort;
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
