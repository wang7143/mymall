package com.atguigu.gulimall.ware.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@MapperScan("com.atguigu.gulimall.ware.dao")
public class WareMybatisConfig {

    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor Interceptor = new PaginationInterceptor();
        //设置请求的页面大于最大页后操作，true调回到首页，false继续请求 默认false
        Interceptor.setOverflow(true);
        //设置最大单页限制数量，默认500条，-1不受限制
        Interceptor.setLimit(1000);
        return Interceptor;
    }

    @Autowired
    DataSourceProperties dataSourceProperties;

    @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties){
        HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        if(StringUtils.hasText(dataSourceProperties.getName())){
            dataSource.setPoolName(dataSourceProperties.getName());
        }
        return new DataSourceProxy(dataSource);
    }
}
