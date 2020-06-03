package com.rabbit.ganzhi3.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 2016/8/26
 *
 * @author owen pan
 * jdbc工具类
 * <p>
 * PanJdbcPreparedManager pjpm=new PanJdbcPreparedManager();
 * pjpm.setDataSource(dataSource);
 * <p>
 * PanJdbcPreparedManager.QueryCallBack queryCallBack=pjpm.query("select sysdate() from dual", new Object[]{});
 * Long str=queryCallBack.queryObjectCallBack(Long.class,pjpm);
 * <p>
 * queryCallBack=pjpm.query("select sysdate() as time from dual", new Object[]{});
 * Map map=queryCallBack.queryMapCallBack(pjpm);
 * <p>
 * queryCallBack=pjpm.query("select sysdate() as time from dual", new Object[]{});
 * List list=queryCallBack.queryListCallBack(pjpm);
 */
public class PanJdbcPreparedManager {
    private Logger logger = LoggerFactory.getLogger(PanJdbcPreparedManager.class);
    private static DataSource dataSourceInstance;

    protected Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private ExceptionCatcher exceptionCatcher=new ExceptionCatcher() {
        @Override
        protected void catchException(String msg, Exception e) {
            logger.error(msg, e);
        }
    };

    public PanJdbcPreparedManager() {
    }

    public PanJdbcPreparedManager(DataSource dataSourceInstance) {
        setDataSource(dataSourceInstance);
    }

    private DataSource getDataSource() {
        return dataSourceInstance;
    }

    public void setDataSource(DataSource dataSource) {
        dataSourceInstance = dataSource;
    }

    public void setExceptionCatcher(ExceptionCatcher exceptionCatcher) {
        this.exceptionCatcher = exceptionCatcher;
    }

    protected Connection getConnection() throws SQLException {
        connection = getDataSource().getConnection();
        logger.debug("connection hash is : " + connection.hashCode());
        return connection;
    }

    private ResultSet getQueryResultSet(String sql, Object[] params) throws SQLException {
        preparedStatement = getConnection().prepareStatement(sql);
        setParams(preparedStatement, params);
        return preparedStatement.executeQuery();
    }

    private int getUpdateResultSet(String sql, Object[] params) throws SQLException {
        preparedStatement = getConnection().prepareStatement(sql);
        setParams(preparedStatement, params);
        return preparedStatement.executeUpdate();
    }

    private int[] getBatchUpdateResultSet(String sql, List<Object[]> params) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        preparedStatement = connection.prepareStatement(sql);
        int[] re = new int[]{};
        for (int i = 0; i < params.size(); i++) {
            setParams(preparedStatement, params.get(i));
            preparedStatement.addBatch();
            if (i != 0 && i % 100 == 0) {
                int[] rs = preparedStatement.executeBatch();
                int[] dd = new int[re.length + rs.length];
                System.arraycopy(re, 0, dd, 0, re.length);
                System.arraycopy(rs, 0, dd, re.length, rs.length);
                re=dd;
            }
        }
        int[] rs2 = preparedStatement.executeBatch();
        int[] dd2 = new int[re.length + rs2.length];
        connection.commit();
        connection.setAutoCommit(true);
        System.arraycopy(re, 0, dd2, 0, re.length);
        System.arraycopy(rs2, 0, dd2, re.length, rs2.length);
        return dd2;
    }

    private void setParams(PreparedStatement preparedStatement, Object[] params) throws SQLException {
        if (params == null || params.length == 0) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }

    public int[] executeBatch(String sql, List<Object[]> params) {
        try {
            return getBatchUpdateResultSet(sql, params);
        } catch (SQLException e) {
            exceptionCatcher. catchException(e.getMessage(), e);
            return null;
        } finally {
            free();
        }
    }

    public QueryCallBack query(String sql) {
        return query(sql, null);
    }

    public QueryCallBack query(String sql, Object[] params) {
        try {
            resultSet = getQueryResultSet(sql, params);
            return new QueryCallBack(resultSet, this);
        } catch (SQLException e) {
            exceptionCatcher.catchException(e.getMessage(), e);
            return null;
        }
    }

    public int execute(String sql) {
        return execute(sql, null);
    }

    public int execute(String sql, Object[] params) {
        try {
            return getUpdateResultSet(sql, params);
        } catch (SQLException e) {
            exceptionCatcher.catchException(e.getMessage(), e);
            return -1;
        } finally {
            free();
        }
    }



    private void free() {
        try {
            if (resultSet != null) {
                resultSet.close(); // 关闭结果集
            }

        } catch (SQLException e) {
            exceptionCatcher.catchException(e.getMessage(),e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close(); // 关闭Statement
                }
            } catch (SQLException e) {
                exceptionCatcher.catchException(e.getMessage(),e);
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                        logger.debug("connection  is closed: " + connection.hashCode());
                    }
                } catch (SQLException e) {
                    exceptionCatcher.catchException(e.getMessage(),e);
                }
            }
        }
    }


    public static class QueryCallBack {
        private Logger logger = LoggerFactory.getLogger(QueryCallBack.class);
        private ResultSet resultSet;
        private ThreadLocal<PanJdbcPreparedManager> local = new ThreadLocal<>();
        private AbstractDateParser abstractDateParser = new AbstractDateParser() {
            @Override
            protected Object parseDate(Date date) {
                return date.getTime();
            }
        };
        private ExceptionCatcher exceptionCatcher=new ExceptionCatcher() {
            @Override
            protected void catchException(String msg, Exception e) {
                logger.error(msg, e);
            }
        };

        QueryCallBack(ResultSet resultSet, PanJdbcPreparedManager panJDBCPreparedManager) {
            this.resultSet = resultSet;
            local.set(panJDBCPreparedManager);
        }

        public void setExceptionCatcher(ExceptionCatcher exceptionCatcher) {
            this.exceptionCatcher = exceptionCatcher;
        }

        public void setAbstractDateParser(AbstractDateParser abstractDateParser) {
            this.abstractDateParser = abstractDateParser;
        }

        private Object parseData(Object it) {
            if (it == null) {
                return null;
            }
            if (it instanceof Timestamp) {
                Timestamp timestamp = (Timestamp) it;
                it = abstractDateParser.parseDate(new Date(timestamp.getTime()));
            }
            if (it instanceof Time) {
                Time timestamp = (Time) it;
                it = abstractDateParser.parseDate(new Date(timestamp.getTime()));
            }
            if (it instanceof Date) {
                Date timestamp = (Date) it;
                it = abstractDateParser.parseDate(new Date(timestamp.getTime()));
            }
            return it;
        }

        public <T> T queryObjectCallBack(Class<T> returnClass) {
            try {
                if (resultSet != null) {
                    if (resultSet.next()) {
                        if (returnClass.equals(String.class)
                                || returnClass.equals(Integer.class)
                                || returnClass.equals(Long.class)
                                || returnClass.equals(Short.class)
                                || returnClass.equals(Double.class)
                                || returnClass.equals(Float.class)
                                || returnClass.equals(BigDecimal.class)
                                ) {
                            Object it = parseData(resultSet.getObject(1));
                            if (it != null) {
                                if (returnClass.equals(String.class)) {
                                    return returnClass.cast(String.valueOf(it));
                                } else if (returnClass.equals(Integer.class)) {
                                    return returnClass.cast(Integer.valueOf(it.toString()));
                                } else if (returnClass.equals(Long.class)) {
                                    return returnClass.cast(Long.valueOf(it.toString()));
                                } else if (returnClass.equals(Short.class)) {
                                    return returnClass.cast(Short.valueOf(it.toString()));
                                } else if (returnClass.equals(Double.class)) {
                                    return returnClass.cast(Double.valueOf(it.toString()));
                                } else if (returnClass.equals(Float.class)) {
                                    return returnClass.cast(Float.valueOf(it.toString()));
                                } else {
                                    return returnClass.cast(it);
                                }
                            }
                        } else {
                            return parseToRealObject(resultSet,returnClass);
                        }
                    }
                }
                return null;
            } catch (SQLException e) {
                exceptionCatcher.catchException(e.getMessage(), e);
                return null;
            } finally {
                local.get().free();
                local.remove();
            }
        }

        public Map queryMapCallBack() {
            try {
                if (resultSet != null) {
                    ResultSetMetaData rsmd = resultSet.getMetaData();
                    int count = rsmd.getColumnCount();
                    String[] names = new String[count];
                    for (int i = 0; i < count; i++) {
                        names[i] = rsmd.getColumnName(i + 1);
                    }

                    if (resultSet.next()) {
                        Map<String, Object> map = new HashMap<>(names.length);
                        for (int i = 0; i < names.length; i++) {
                            Object it = resultSet.getObject(i + 1);
                            map.put(names[i], parseData(it));
                        }
                        return map;
                    } else {
                        return null;
                    }
                }
                return null;
            } catch (SQLException e) {
                exceptionCatcher.catchException(e.getMessage(), e);
                return null;
            } finally {
                local.get().free();
                local.remove();
            }
        }

        public List queryListCallBack() {
            try {
                if (resultSet != null) {
                    ResultSetMetaData rsmd = resultSet.getMetaData();
                    int count = rsmd.getColumnCount();
                    String[] names = new String[count];
                    for (int i = 0; i < count; i++) {
                        names[i] = rsmd.getColumnName(i + 1);
                    }
                    List<Map<String, Object>> list = null;
                    while (resultSet.next()) {
                        if (list == null) {
                            list = new ArrayList<>();
                        }
                        Map<String, Object> map = new HashMap<>();
                        for (String name : names) {
                            Object it = resultSet.getObject(name);
                            map.put(name, parseData(it));
                        }
                        list.add(map);
                    }
                    return list;
                }
                return null;
            } catch (SQLException e) {
                exceptionCatcher.catchException(e.getMessage(), e);
                return null;
            } finally {
                local.get().free();
                local.remove();
            }
        }

        public <T> List queryListCallBack(Class<T> returnClass) {
            try {
                if (resultSet != null) {
                    List<T> list = new ArrayList<>();
                    while (resultSet.next()) {
                        if (returnClass.equals(String.class)
                                || returnClass.equals(Integer.class)
                                || returnClass.equals(Long.class)
                                || returnClass.equals(Short.class)
                                || returnClass.equals(Double.class)
                                || returnClass.equals(Float.class)
                                || returnClass.equals(BigDecimal.class)
                                ) {
                            Object it = parseData(resultSet.getObject(1));
                            if (it != null) {
                                if (returnClass.equals(String.class)) {
                                    list.add(returnClass.cast(String.valueOf(it)));
                                } else if (returnClass.equals(Integer.class)) {
                                    list.add(returnClass.cast(Integer.valueOf(it.toString())));
                                } else if (returnClass.equals(Long.class)) {
                                    list.add(returnClass.cast(Long.valueOf(it.toString())));
                                } else if (returnClass.equals(Short.class)) {
                                    list.add(returnClass.cast(Short.valueOf(it.toString())));
                                } else if (returnClass.equals(Double.class)) {
                                    list.add(returnClass.cast(Double.valueOf(it.toString())));
                                } else if (returnClass.equals(Float.class)) {
                                    list.add(returnClass.cast(Float.valueOf(it.toString())));
                                } else {
                                    list.add(returnClass.cast(it));
                                }
                            }
                        } else {
                            T bean = parseToRealObject(resultSet, returnClass);
                            if(bean!=null){
                                list.add(bean);
                            }
                        }
                    }
                    return list;
                }
                return null;
            } catch (SQLException e) {
                exceptionCatcher.catchException(e.getMessage(), e);
                return null;
            } finally {
                local.get().free();
                local.remove();
            }
        }


        private <T> T parseToRealObject(ResultSet resultSet, Class<T> returnClass) {
            try {
                ResultSetMetaData rsmd = resultSet.getMetaData();
                int count = rsmd.getColumnCount();
                String[] names = new String[count];
                for (int i = 0; i < count; i++) {
                    names[i] = rsmd.getColumnName(i + 1);
                }

                T bean = returnClass.newInstance();
                for (int i = 0; i < names.length; i++) {
                    String name = names[i];

                    Method gm;
                    try {
                        gm = returnClass.getDeclaredMethod("get" + name.substring(0, 1).toUpperCase() + name.substring(1));
                    } catch (NoSuchMethodException e) {
                        continue;
                    }
                    Method m = returnClass.getDeclaredMethod("set" + name.substring(0, 1).toUpperCase() + name.substring(1), gm.getReturnType());
                    m.setAccessible(true);
                    Object o = resultSet.getObject(i + 1);
                    if (o == null) {
                        continue;
                    }
                    if (gm.getReturnType().equals(String.class)) {
                        m.invoke(bean, String.valueOf(o));
                    } else if (gm.getReturnType().equals(Integer.class)) {
                        m.invoke(bean, Integer.valueOf(o.toString()));
                    } else if (gm.getReturnType().equals(Long.class)) {
                        m.invoke(bean, Long.valueOf(o.toString()));
                    } else if (gm.getReturnType().equals(Short.class)) {
                        m.invoke(bean, Short.valueOf(o.toString()));
                    } else if (gm.getReturnType().equals(Double.class)) {
                        m.invoke(bean, Double.valueOf(o.toString()));
                    } else if (gm.getReturnType().equals(Float.class)) {
                        m.invoke(bean, Float.valueOf(o.toString()));
                    } else {
                        m.invoke(bean, gm.getReturnType().cast(o));
                    }
                }
                return bean;
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                exceptionCatcher.catchException(e.getMessage(), e);
            } catch (SQLException | NoSuchMethodException e) {
                exceptionCatcher.catchException(e.getMessage(),e);
            }
            return null;
        }
    }

    public static abstract class AbstractDateParser {
        protected abstract Object parseDate(Date date);
    }

    public static abstract class ExceptionCatcher{
        protected abstract void catchException(String msg,Exception e);
    }
}
