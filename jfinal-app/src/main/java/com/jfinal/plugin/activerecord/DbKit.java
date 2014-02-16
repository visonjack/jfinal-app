/**
 * Copyright (c) 2011-2013, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.plugin.activerecord;

import com.jfinal.plugin.activerecord.cache.EhCache;
import com.jfinal.plugin.activerecord.cache.ICache;
import com.jfinal.plugin.activerecord.dialect.Dialect;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.sog.kit.cst.StringPool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * DbKit
 */
public final class DbKit {

    private static DataSource dataSource;
    private static final ThreadLocal<Connection> threadLocal = new ThreadLocal<Connection>();
    static final Object[] NULL_PARA_ARRAY = new Object[0];
    private static int transactionLevel = Connection.TRANSACTION_READ_COMMITTED;

    private static ICache cache = new EhCache();
    private static boolean showSql = false;

    static boolean devMode = false;
    static Dialect dialect = new MysqlDialect();

    static IContainerFactory containerFactory = new IContainerFactory() {
        public Map<String, Object> getAttrsMap() {
            return new HashMap<String, Object>();
        }

        public Map<String, Object> getColumnsMap() {
            return new HashMap<String, Object>();
        }

        public Set<String> getModifyFlagSet() {
            return new HashSet<String>();
        }
    };

    static void setContainerFactory(IContainerFactory containerFactory) {
        if (containerFactory != null)
            DbKit.containerFactory = containerFactory;
    }

    static void setDevMode(boolean devMode) {
        DbKit.devMode = devMode;
    }

    static void setShowSql(boolean showSql) {
        DbKit.showSql = showSql;
    }

    static void setDialect(Dialect dialect) {
        if (dialect != null)
            DbKit.dialect = dialect;
    }

    static void setCache(ICache cache) {
        DbKit.cache = cache;
    }

    public static Dialect getDialect() {
        return dialect;
    }

    public static ICache getCache() {
        return cache;
    }

    // Prevent new DbKit()
    private DbKit() {
    }

    /**
     * Inject DataSource
     */
    public static void setDataSource(DataSource dataSource) {
        DbKit.dataSource = dataSource;
    }

    static void setTransactionLevel(int transactionLevel) {
        DbKit.transactionLevel = transactionLevel;
    }

    public static int getTransactionLevel() {
        return transactionLevel;
    }

    /**
     * Get DataSrouce
     */
    public static DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Support transaction with Transaction interceptor
     */
    public static void setThreadLocalConnection(Connection connection) {
        threadLocal.set(connection);
    }

    public static void removeThreadLocalConnection() {
        threadLocal.remove();        // threadLocal.set(null);
    }

    /**
     * Get Connection. Support transaction if Connection in ThreadLocal
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = threadLocal.get();
        if (conn != null)
            return conn;
        return showSql ? new SqlReporter(dataSource.getConnection()).getConnection() : dataSource.getConnection();
    }

	/* backup before refactory
	public static final Connection getConnection() throws SQLException {
		Connection conn = threadLocal.get();
		if (showSql)
			return conn != null ? conn : new SqlReporter(dataSource.getConnection()).getConnection();
		else
			return conn != null ? conn : dataSource.getConnection();
	}*/

    /**
     * Helps to implement nested transaction.
     * Tx.intercept(...) and Db.tx(...) need this method to detected if it in nested transaction.
     */
    public static Connection getThreadLocalConnection() {
        return threadLocal.get();
    }

    /**
     * Close ResultSet、Statement、Connection
     * ThreadLocal support declare transaction.
     */
    public static void close(ResultSet rs, Statement st, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignored) {
            }
        }
        if (st != null) {
            try {
                st.close();
            } catch (SQLException ignored) {
            }
        }

        if (threadLocal.get() == null) {    // in transaction if conn in threadlocal
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new ActiveRecordException(e);
                }
            }
        }
    }

    public static void close(Statement st, Connection conn) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException ignored) {
            }
        }

        if (threadLocal.get() == null) {    // in transaction if conn in threadlocal
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new ActiveRecordException(e);
                }
            }
        }
    }

    public static void close(Connection conn) {
        if (threadLocal.get() == null)        // in transaction if conn in threadlocal
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new ActiveRecordException(e);
                }
    }

    static void closeIgnoreThreadLocal(Connection conn) {
        if (conn != null)
            try {
                conn.close();
            } catch (SQLException e) {
                throw new ActiveRecordException(e);
            }
    }

    static void closeQuietly(ResultSet rs, Statement st) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignored) {
            }
        }
        if (st != null) {
            try {
                st.close();
            } catch (SQLException ignored) {
            }
        }
    }

    static void closeQuietly(Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public static String replaceFormatSqlOrderBy(String sql) {
        sql = sql.replaceAll("(\\s)+", StringPool.SPACE);
        int index = sql.toLowerCase().lastIndexOf("ORDER BY");
        if (index > sql.toLowerCase().lastIndexOf(StringPool.RIGHT_BRACKET)) {
            String sql1 = sql.substring(0, index);
            String sql2 = sql.substring(index);
            sql2 = sql2.replaceAll("[oO][rR][dD][eE][rR] [bB][yY] [\u4e00-\u9fa5a-zA-Z0-9_.]+((\\s)+(([dD][eE][sS][cC])|([aA][sS][cC])))?(( )*,( )*[\u4e00-\u9fa5a-zA-Z0-9_.]+(( )+(([dD][eE][sS][cC])|([aA][sS][cC])))?)*", "");
            return sql1 + sql2;
        }
        return sql;
    }
}

