package mono.backend.database;

import mono.backend.bean.PO;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.jdbc.core.RowMapper;

/**
 * su dung de lay mot domain ke thua tu PO
 *
 * @author hungtv
 * @param <T>
 *
 */
public class GenericMapper<T extends PO> implements RowMapper<T> {

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        String[] columnNames = BeanUtil.getColumnNames(entityClass);
        String[] propertyNames = BeanUtil.getPropertyNames(entityClass);
        Class<?>[] clazzTypes = BeanUtil.getColumnTypes(entityClass);

        T t;
        try {
            t = entityClass.newInstance();
            if (t instanceof PO) {
                ((PO) t).setGenericMapper(true);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            return null;
        }

        int columnLength = propertyNames.length;

        ResultSetMetaData meta = rs.getMetaData();
        int metaLength = meta.getColumnCount();

        for (int j = 0; j < columnLength; j++) {
            String columnName = columnNames[j];
            String propertyName = propertyNames[j];
            Class<?> clazz = clazzTypes[j];

            for (int i = 1; i <= metaLength; i++) {
                String metaColumn = meta.getColumnName(i);
                if (metaColumn.equalsIgnoreCase(columnName)) {
                    try {
                        Object value;
                        if (clazz.isAssignableFrom(Long.class)) {
                            value = rs.getLong(columnName);
                        } else if (clazz.isAssignableFrom(boolean.class)
                                || clazz.isAssignableFrom(Boolean.class)) {
                            value = BeanUtil.BOOLEAN_TRUE.equals(rs.getString(columnName));
                        } else if (clazz.isAssignableFrom(Integer.class)
                                || clazz.isAssignableFrom(int.class)) {
                            value = rs.getInt(columnName);
                        } else if (clazz.isAssignableFrom(BigDecimal.class)) {
                            value = rs.getBigDecimal(columnName);
                        } else if (t.equals(Double.class) || t.equals(double.class)) {
                            value = rs.getDouble(columnName);
                        } else if (t.equals(Float.class) || t.equals(float.class)) {
                            value = rs.getFloat(columnName);
                        } else if (clazz.isAssignableFrom(Timestamp.class)) {
                            value = rs.getTimestamp(columnName);
                        } else if (clazz.isAssignableFrom(Date.class)
                                || clazz.isAssignableFrom(java.util.Date.class)) {
                            value = rs.getDate(columnName);
                        } else {
                            value = rs.getObject(columnName);
                        }
                        PropertyUtils.setSimpleProperty(t, propertyName, value);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new IllegalArgumentException(" No column: "
                                + columnName + " in table ");
                    } catch (Exception e) {
//						e.printStackTrace();
                    }

                    break;
                }

            }
        }

        return t;
    }

    protected Class<T> entityClass;

    public GenericMapper(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

}
