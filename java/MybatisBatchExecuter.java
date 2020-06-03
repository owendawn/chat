package com.rabbit.ganzhi3.util;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * Created by Owen Pan on 2017-09-07.
 */
public abstract class MybatisBatchExecuter<T> {
    private SqlSessionTemplate sqlSessionTemplate;
    private SqlSessionFactory sqlSessionFactory;
    private SqlSession sqlSession;

    public MybatisBatchExecuter(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.sqlSessionFactory = sqlSessionTemplate.getSqlSessionFactory();
        try {
            sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract T doBatchExecute(SqlSession sqlSession);
    protected abstract T doException(Exception e,SqlSession sqlSession);

    public T executeBatch() {
        try {
            T re= doBatchExecute(this.sqlSession);
            sqlSession.commit();
            sqlSession.clearCache();
            return re;
        } catch (Exception e) {
            return doException(e,sqlSession);
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
    }

    public static void main(String[] args) {
        /*
        new MybatisBatchExecuter<Boolean>(sqlSessionTemplate){
            private int idx=0;
            @Override
            protected Boolean doBatchExecute(SqlSession sqlSession) {
                list.forEach(it->{
                    sqlSession.insert(it.getAddSql(),it.getData());
                    if(idx!=0&&idx++%100==0){
                        sqlSession.flushStatements();
                    }
                });
                sqlSession.commit();
                System.out.println("add:"+list.size());
                return true;
            }

            @Override
            protected Boolean doException(Exception e, SqlSession sqlSession) {
                e.printStackTrace();
                list.forEach(it->{
                    try {
                        sqlSession.insert(it.getUpdateSql(), it.getData());
                        sqlSession.commit();
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                });
                return false;
            }
        }.executeBatch();
        */
    }
}
