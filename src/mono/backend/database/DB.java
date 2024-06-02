package mono.backend.database;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

@Component
public class DB {

    public static final boolean ID_MANNUAL_GEN = false;

    public static final int SQL_INSERT = 0;
    public static final int SQL_INSERTONLY = 1;
    public static final int SQL_UPDATE = 2;

    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static DB _this;

    // tulv2. Chi class DB moi duoc phep tao doi tuong bang toan tu new.
    private DB() {
    }

    @PostConstruct
    void afterInit() {
        if (_this == null) {
            _this = this;
        }
    }

    /**
     * Su dung synchronized trong truong hop tai mot thoi diem co nhieu noi goi
     * getDB(). 22.08.2014 tulv2
     *
     * @return
     */
    public static synchronized DB getDB() {
        if (_this == null) {
            _this = new DB();
        }
        return _this;
    }

    public DataSource getDataSource() {
        return ConnectionPoolManager.getDefaltDataSource();
    }

    /**
     * cho nay, khong ai duoc lay ra de dung, muc dich de invisible spring jdbc
     * voi cac lap trinh vien
     *
     * @author hungtv
     * @since 21.08.2014
     *
     */
    private synchronized JdbcTemplate getJdbcTemplate() {
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(getDataSource());
        }
        return jdbcTemplate;
    }

    private synchronized NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        if (namedParameterJdbcTemplate == null) {
            namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(getDataSource());
        }

        return namedParameterJdbcTemplate;
    }

    //=====================================================================
    public static Integer queryForInt(String sql) {
        return queryForObject(sql, Integer.class);
    }

    public static Long queryForLong(String sql) {
        return queryForObject(sql, Long.class);
    }

    public static Long queryForLong(String sql, Object... args) {
        return getDB().getJdbcTemplate().queryForObject(sql, args, Long.class);
    }

    // tulv2 24.08.2014
    public static Long queryForLong(String sql, List<Object> params) {
        Object[] args = params.toArray();
        return getDB().getJdbcTemplate().queryForObject(sql, Long.class, args);
    }

    public static <T> T queryForObject(String sql, Class<T> clazz) {
        return getDB().getJdbcTemplate().queryForObject(sql, clazz);
    }

    public static <T> T queryForObject(String sql, RowMapper<T> mapper, Object arg) {
        List<Object> params = new LinkedList<>();
        params.add(arg);
        return getDB().getJdbcTemplate().queryForObject(sql, mapper, params);
    }

    public static <T> T queryForObject(String sql, Object[] args, RowMapper<T> mapper) {
        return getDB().getJdbcTemplate().queryForObject(sql, args, mapper);
    }

    public static <T> T queryForObject(String sql, RowMapper<T> mapper, List<Object> params) {
        return getDB().getJdbcTemplate().queryForObject(sql, mapper, params);
    }

    public static <T> List<T> query(String sql, RowMapper<T> mapper) {
        return getDB().getJdbcTemplate().query(sql, mapper);
    }

    public static <T> List<T> query(String sql, Object[] args, RowMapper<T> mapper) {
        return getDB().getJdbcTemplate().query(sql, args, mapper);
    }

    public static <T> List<T> query(String sql, List<Object> params, RowMapper<T> mapper) {
        return getDB().getJdbcTemplate().query(sql, params.toArray(), mapper);
    }

    public static <T> List<T> query(String sql, RowMapper<T> mapper, Object... args) {
        return getDB().getJdbcTemplate().query(sql, mapper, args);
    }

    public static int update(String sql) {
        return getDB().getJdbcTemplate().update(sql);
    }

    public static int update(String sql, Long param) {
        return getDB().getJdbcTemplate().update(sql, param);
    }

    public static int update(String sql, Object args) {
        return getDB().getJdbcTemplate().update(sql, args);
    }

    public static int update(String sql, Object... args) {
        return getDB().getJdbcTemplate().update(sql, args);
    }


    public static int[] batchUpdate(String sql, List<Object[]> batchArgs) {
        int i = 0;
//        for (Object[] objects : batchArgs) {
//             logger.info(++i + "value: " + Arrays.toString(objects));
//        }
        return getDB().getJdbcTemplate().batchUpdate(sql, batchArgs);
    }

    public static int[] batchUpdateEx(String sql, List<Object> batchArgs) throws DataAccessException {
        List<Object[]> params = new ArrayList<>();
        for (Object object : batchArgs) {
            params.add(new Object[]{object});
        }
        return getDB().getJdbcTemplate().batchUpdate(sql, params);
    }

    public static int[] batchUpdate(String sql, List<Object[]> batchArgs, int[] argTypes) {
        return getDB().getJdbcTemplate().batchUpdate(sql, batchArgs, argTypes);
    }

    public static String addPagingSQL(String sql, int start, int end) {
        String value = sql;
        if (start >= 0 && end > 0) {
            value = "SELECT * FROM (SELECT a.*, RowNum rNum FROM (" + sql
                    + ") a WHERE RowNum <= " + end + ") WHERE rNum >= " + start;
        }
        return value;
    }

    public static <T> List<T> query(String sql, Map<String, ?> paramMap, RowMapper<T> mapper) {
        return getDB().getNamedParameterJdbcTemplate().query(sql, paramMap, mapper);
    }

    private static CustomizeDatasource customizeDatasource ;
    
    public synchronized static DataSource getCustomizeDataSource() {
        if (customizeDatasource == null) {
            customizeDatasource = new CustomizeDatasource();
            customizeDatasource.start();
        }
        
        return customizeDatasource;
    }
    
    public static Map<String, Object> query(String sql, Map<String, Object> paramMap) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(getCustomizeDataSource()).withProcedureName(sql);
        MapSqlParameterSource in = new MapSqlParameterSource();//.addValue("in_id", 1);
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            String string = entry.getKey();
            Object object = entry.getValue();
            in.addValue(string, object);
        }

        Map<String, Object> out = jdbcCall.execute(in);
        return out;
    }

    //==================================================================
    //============SQL for Entity========================================
    //==================================================================
    private static Map<Class<?>, String> s_SqlPoInsert = new HashMap<>();

    /**
     * tra lai lenh insert vao table cua mot entity, truong ID luon dung dau
     * tien
     *
     * @author hungtv
	 *
     */
    public static String getSqlInsert(Class<?> entityClass) {
        String sqlInsert = s_SqlPoInsert.get(entityClass);
        if (sqlInsert != null && !"".equals(sqlInsert.trim())) {
            return sqlInsert;
        }

        String[] columnNames = BeanUtil.getColumnNames(entityClass);
        int columnLength = columnNames == null ? 0 : columnNames.length;
        String keyColumnName = BeanUtil.getIdColumnName(entityClass);
        if (keyColumnName == null || columnLength == 0) {
            return null;
        }

        StringBuilder sqlCols = new StringBuilder();
        StringBuilder sqlValues = new StringBuilder();

        boolean isFirt = true;
        if (ID_MANNUAL_GEN) {
            sqlCols.append(keyColumnName);
            sqlValues.append("?");
            isFirt = false;
        }

        for (String columnName : columnNames) {
            if (keyColumnName.equals(columnName)) {
                continue;
            }
            if (isFirt) {
                sqlCols.append(columnName);
                sqlValues.append("?");
            } else {
                sqlCols.append(",").append(columnName);
                sqlValues.append(",?");
            }

            isFirt = false;
        }

        String tableName = BeanUtil.getTableName(entityClass);

        if (columnLength > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ");
            sb.append(tableName);
            sb.append("(").append(sqlCols);
            sb.append(") VALUES(");
            sb.append(sqlValues);
            sb.append(")");
            sqlInsert = sb.toString();
            s_SqlPoInsert.put(entityClass, sqlInsert);
        }

        return sqlInsert;
    }

    private static Map<Class<?>, String> s_SqlPoInsertOnly = new HashMap<>();

    /**
     * tra lai lenh insert vao table cua mot entity, truong ID: ko lay gia tri
     * trong DB, ma tra ve luon la
     *
     * @author hungtv
	 *
     */
    public static String getSqlInsertOnly(Class<?> entityClass) {
        String sqlInsertOnly = s_SqlPoInsertOnly.get(entityClass);
        if (sqlInsertOnly != null && !"".equals(sqlInsertOnly.trim())) {
            return sqlInsertOnly;
        }

        String[] columnNames = BeanUtil.getColumnNames(entityClass);
        int columnLength = columnNames == null ? 0 : columnNames.length;
        String keyColumnName = BeanUtil.getIdColumnName(entityClass);
        if (keyColumnName == null || columnLength <= 1) {
            return null;
        }

        String tableName = BeanUtil.getTableName(entityClass);

        StringBuilder sqlCols = new StringBuilder();
        StringBuilder sqlValues = new StringBuilder();

        boolean isFirt = true;
        if (ID_MANNUAL_GEN) {
            String id = Sequence.getSequenceName(tableName);
            sqlCols.append(keyColumnName);
            sqlValues.append(id);
            isFirt = false;
        }

        for (String columnName : columnNames) {
            if (isFirt) {
                sqlCols.append(columnName);
                sqlValues.append("?");
            } else {
                sqlCols.append(",").append(columnName);
                sqlValues.append(",?");
            }

            isFirt = false;
        }

        if (columnLength > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ");
            sb.append(tableName);
            sb.append("(").append(sqlCols);
            sb.append(") VALUES(");
            sb.append(sqlValues);
            sb.append(")");
            sqlInsertOnly = sb.toString();
            s_SqlPoInsertOnly.put(entityClass, sqlInsertOnly);
        }

        return sqlInsertOnly;
    }

    /**
     * @param entity
     * @param type phuc vu cho muc dich lay du lieu
     *
     * <ul>
     * <li>PO.SQL_INSERT: la insert lay getId</li>
     * <li>PO.SQL_INSERTONLY: la ko lay getId</li>
     * <li>PO.SQL_UPDATE: lay du lieu phuc vu update</li>
     * </ul>
     * @return Object[] trong do Object[0] la id trong truong hop type =
     * PO.SQL_INSERT
     * @author hungtv
     * @param <E>
     *
     */
    public static <E> Object[] getEntityValues(E entity, int type) {
        List<Object> arrList = new ArrayList<>();
        String[] propertyNames = BeanUtil.getPropertyNames(entity.getClass());

        String keyPropertyName = BeanUtil.getIdPropertyName(entity.getClass());
        Object obj = null;

        if (ID_MANNUAL_GEN && SQL_INSERT == type) {
            String tableName = BeanUtil.getTableName(entity.getClass());
            obj = Sequence.getNextID(tableName);
            arrList.add(obj);
        }

        for (String propertyName : propertyNames) {
            obj = null;
            if (!keyPropertyName.equalsIgnoreCase(propertyName)) {
                try {
                    obj = PropertyUtils.getSimpleProperty(entity, propertyName);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//                    e.printStackTrace();
                }

                if (obj instanceof Boolean) {
                    boolean bValue = false;
                    if (obj instanceof Boolean) {
                        bValue = ((Boolean) obj).booleanValue();
                    } else {
                        bValue = BeanUtil.BOOLEAN_TRUE.equals(obj);
                    }

                    obj = bValue ? BeanUtil.BOOLEAN_TRUE : BeanUtil.BOOLEAN_FALSE;
                } else if (obj instanceof String) {
                    obj = org.apache.commons.lang.StringEscapeUtils.escapeSql((String) obj);
                }
                arrList.add(obj);
            }

        }

        if (SQL_UPDATE == type) {
            obj = null;
            try {
                obj = PropertyUtils.getSimpleProperty(entity, keyPropertyName);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            }
            arrList.add(obj);
        }

        return arrList.toArray();
    }

    private static Map<Class<?>, String> s_SqlPoUpdate = new HashMap<>();

    public static String getSqlUpdate(Class<?> entityClass) {
        String sqlUpdate = s_SqlPoUpdate.get(entityClass);
        if (sqlUpdate != null && !"".equals(sqlUpdate.trim())) {
            return sqlUpdate;
        }

        String[] columnNames = BeanUtil.getColumnNames(entityClass);
        int columnLength = columnNames == null ? 0 : columnNames.length;
        String tableName = BeanUtil.getTableName(entityClass);

        StringBuilder sqlCols = new StringBuilder("UPDATE ");
        sqlCols.append(tableName).append(" SET ");

        String keyColumnName = BeanUtil.getIdColumnName(entityClass);

        int last = columnLength - 1;
        for (int i = 0; i < columnLength; i++) {
            String columnName = columnNames[i];
            if (!keyColumnName.equalsIgnoreCase(columnName)) {
                if (i < last) {
                    sqlCols.append(columnName).append(" =? ,");
                } else {
                    sqlCols.append(columnName).append(" =? ");
                }
            }
        }

        sqlCols.append(" WHERE ").append(keyColumnName).append(" =?");

        if (columnLength > 0) {
            sqlUpdate = sqlCols.toString();
            s_SqlPoUpdate.put(entityClass, sqlUpdate);
        }

        return sqlUpdate;
    }
}
