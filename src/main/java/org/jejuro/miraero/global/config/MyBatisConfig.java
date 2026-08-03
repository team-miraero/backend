package org.jejuro.miraero.global.config;

import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = {
    "org.jejuro.miraero.domain.user.mapper",
    "org.jejuro.miraero.domain.mydata.mapper",
    "org.jejuro.miraero.domain.product.mapper",
        "org.jejuro.miraero.domain.transaction.mapper",
        "org.jejuro.miraero.domain.goal.mapper",
    "org.jejuro.miraero.domain.pacemaker.mapper"
})
public class MyBatisConfig {

  @Bean
  public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
    SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
    factoryBean.setDataSource(dataSource);
    factoryBean.setMapperLocations(
        new PathMatchingResourcePatternResolver().getResources("classpath*:mappers/**/*.xml"));
    factoryBean.setTypeAliasesPackage("org.jejuro.miraero.domain");
    return factoryBean.getObject();
  }
}
