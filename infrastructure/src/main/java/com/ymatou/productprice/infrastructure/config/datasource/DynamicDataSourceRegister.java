package com.ymatou.productprice.infrastructure.config.datasource;

/**
 * Created by chenpengxuan on 2016/9/1.
 */

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.bind.PropertySourceUtils;
import org.springframework.boot.bind.RelaxedDataBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 动态数据源注册<br/>
 * 启动动态数据源请在启动类中
 * 添加 @Import(DynamicDataSourceRegister.class)
 */
@Configuration
@DependsOn({"disconfMgrBean2", "propertyPlaceholderConfigurer"})
public class DynamicDataSourceRegister {

    private static final String datasourcePerfix = "spring.datasource.";
    private ConversionService conversionService = new DefaultConversionService();
    private PropertyValues dataSourcePropertyValues;
    @Autowired
    private Properties propertiesDisconf;
    @Autowired
    private Environment env;

    // 如配置文件中未指定数据源类型，使用该默认值
    private static final Object DATASOURCE_TYPE_DEFAULT = "com.alibaba.druid.pool.DruidDataSource";
    // 数据源
    private Map<String, DataSource> customDataSources = new HashMap<>();

    @Bean
    public DynamicDataSource dataSource() throws Exception {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        Map<Object, Object> targetDataSources = new HashMap<Object, Object>();
        initCustomDataSources();
        targetDataSources.putAll(customDataSources);
        dynamicDataSource.setTargetDataSources(targetDataSources);
        return dynamicDataSource;
    }

    /**
     * 创建DataSource
     *
     * @param dsMap
     * @return
     */
    @SuppressWarnings("unchecked")
    public DataSource buildDataSource(Map<String, Object> dsMap) throws Exception{
            Object type = dsMap.get("type");
            if (type == null) type = DATASOURCE_TYPE_DEFAULT;// 默认DataSource
            Class<? extends DataSource> dataSourceType;
            dataSourceType = (Class<? extends DataSource>) Class.forName((String) type);
            String driverClassName = dsMap.get("driver-class-name").toString();
            String url = dsMap.get("url").toString();
            String username = dsMap.get("username").toString();
            String password = dsMap.get("password").toString();
            DataSourceBuilder factory = DataSourceBuilder.create().driverClassName(driverClassName).url(url)
                    .username(username).password(password).type(dataSourceType);
            return factory.build();
    }

    /**
     * 为DataSource绑定更多数据
     *
     * @param dataSource
     * @create 2016年4月19日
     */
    private void dataBinder(DataSource dataSource, Map<String, Object> dsMap) throws Exception{
        RelaxedDataBinder dataBinder = new RelaxedDataBinder(dataSource);
        dataBinder.setConversionService(conversionService);
        dataBinder.setIgnoreNestedProperties(false);
        dataBinder.setIgnoreInvalidFields(false);
        dataBinder.setIgnoreUnknownFields(true);
        if (dataSourcePropertyValues == null) {
            Map<String, Object> values = new HashMap<>(dsMap);
            // 排除已经设置的属性
            values.remove("type");
            values.remove("driver-class-name");
            values.remove("url");
            values.remove("username");
            values.remove("password");
            dataSourcePropertyValues = new MutablePropertyValues(values);
        }
        dataBinder.bind(dataSourcePropertyValues);
    }

    /**
     * 初始化更多数据源
     *
     * @create 2016年4月18日
     */
    private void initCustomDataSources() throws Exception {
        // 读取配置文件获取更多数据源
        PropertySource propertySource = new PropertiesPropertySource("datasource", propertiesDisconf);
        MutablePropertySources propertySources = new MutablePropertySources();
        propertySources.addFirst(propertySource);
        String dsPrefixs = (String) propertySource.getProperty(datasourcePerfix + "dataSourceName");
        for (String dsPrefix : dsPrefixs.split(",")) {// 多个数据源
            Map<String, Object> dsMap = PropertySourceUtils.getSubProperties(propertySources, "spring.datasource.", dsPrefix + ".");
            DataSource ds = buildDataSource(dsMap);
            customDataSources.put(dsPrefix, ds);
            dataBinder(ds, dsMap);
        }
    }
}
