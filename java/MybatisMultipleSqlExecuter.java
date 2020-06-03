package com.hh.commons.jdbc.mybatis;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by Owen Pan on 2018-03-16.
 */
public class MybatisMultipleSqlExecuter {
    private Logger logger = LoggerFactory.getLogger(MybatisMultipleSqlExecuter.class);
    private SqlSessionTemplate sqlSessionTemplate;
    private SqlSessionFactory sqlSessionFactory;
    private SqlSession sqlSession;

    public MybatisMultipleSqlExecuter(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.sqlSessionFactory = sqlSessionTemplate.getSqlSessionFactory();
        try {
            sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 改方法用于一个方法执行多条语句（非batch），用“;”分割
     *  注：
     * （1）Connection不用close，mybatis自己会回收；
     * （2）使用此方法可以使用spring的事务管理
     * @param statement
     * @param params
     */
    public void exec(String statement,Map<String, Object> params){
        PreparedStatement ps = null;
        try {
            Connection connection = sqlSession.getConnection();
            BoundSql boundSql = sqlSession.getConfiguration().getMappedStatement(statement).getBoundSql(params);

            String[] sqls = boundSql.getSql().split(";");
            List<ParameterMapping> list = boundSql.getParameterMappings();

            int j = 0;
            for( int i = 0 ; i < sqls.length; i++ ) {
                String sql = sqls[i].trim();
                if(logger.isDebugEnabled()) {
                    logger.debug("Preparing sql: " + sql);
                }
                ps = connection.prepareStatement(sql);
                int questionMarkCount = ps.getParameterMetaData().getParameterCount();
                StringBuilder sb = null;
                if(logger.isDebugEnabled()) {
                    sb = new StringBuilder();
                }
                for( int k = 1, length = j + questionMarkCount; j < length; j++, k++ ) {
                    Object param = null;
                    String propertyName = list.get(j).getProperty();
                    if(boundSql.hasAdditionalParameter(propertyName)) {
                        // 用于获取xml通过foreach循环的list参数
                        param = boundSql.getAdditionalParameter(propertyName);
                    } else {
                        // 用于获取正常的参数，包括对象参数
                        MetaObject metaObject = sqlSession.getConfiguration().newMetaObject(params);
                        param = metaObject.getValue(propertyName);
                    }
                    ps.setObject(k, param);
                    if(logger.isDebugEnabled()) {
                        sb.append(param==null?"null":param.toString()).append("(").append(param==null?list.get(j).getJavaType().getSimpleName():param.getClass().getSimpleName()).append("),");
                    }
                }
                if(logger.isDebugEnabled() && sb.length() > 0) {
                    logger.debug("Parameters: " + sb.toString().substring(0, sb.length() - 1));
                }
                int total = ps.executeUpdate();
                if(logger.isDebugEnabled()) {
                    logger.debug("Total: " + total);
                }
                ps.close();
            }
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if(ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            sqlSession.close();
        }
    }

    public void exec(String statement){
        exec(statement,null);
    }
}
