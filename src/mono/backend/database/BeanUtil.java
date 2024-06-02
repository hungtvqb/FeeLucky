/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mono.backend.database;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

public class BeanUtil {

    public static final String BOOLEAN_TRUE = "Y";
    public static final String BOOLEAN_FALSE = "N";

    private static final String METHOD_GET = "get";
    private static final String METHOD_BOOLEAN = "is";

    private static final Map<Class<?>, TableInfo> info = new Hashtable<Class<?>, TableInfo>();

    public static final String getIdColumnName(Class<?> entityClass) {
        init(entityClass);
        TableInfo fi = info.get(entityClass);
        return (fi != null) ? fi.getIdColumnName() : null;
    }

    public static final String getIdPropertyName(Class<?> entityClass) {
        init(entityClass);
        TableInfo fi = info.get(entityClass);
        return (fi != null) ? fi.getIdPropertyName() : null;
    }

    /**
     * phuc vu viec lay cac columnName tuong ung trong database de sinh cau lenh
     * insert tu dong
     *
     * @param entityClass
     * @return 
     * @see {@link #getPropertyNames(Class)}
     * @author hungtv
	 *
     */
    public static final String[] getColumnNames(Class<?> entityClass) {
        init(entityClass);
        String[] columnNames = null;
        TableInfo fi = info.get(entityClass);
        if (fi != null) {
            columnNames = fi.getColumnNames();
        }
        return columnNames;
    }

    /**
     * ham nay phuc vu viec lay du lieu cua 1 entity de sinh cau lenh insert tu
     * dong
     *
     * @see {@link #getColumnNames(Class)}
     * @author hungtv
	 *
     */
    public static final String[] getPropertyNames(Class<?> entityClass) {
        init(entityClass);
        String[] propertyNames = null;
        TableInfo fi = info.get(entityClass);
        if (fi != null) {
            propertyNames = fi.getPropertydNames();
        }
        return propertyNames;
    }

    /**
     * tra ve kieu gia tri cua method, tuong ung voi column, vi qui dinh
     * \@column cua dang dat tren method, vi the ham ham nay co ten la:
     * getColumnTypes
     *
     * @param entityClass
     * @return 
     * @see {@link #getPropertyNames(Class)} & {@link #getColumnNames(Class)}
     * @author hungtv
	 *
     */
    public static final Class<?>[] getColumnTypes(Class<?> entityClass) {
        init(entityClass);
        Class<?>[] clazz = null;
        TableInfo fi = info.get(entityClass);
        if (fi != null) {
            clazz = fi.getTypes();
        }
        return clazz;

    }

    /**
     * tableNameByClazz luu ca gia tri null, muc dich khong muon xu ly lan 2
     *
     * @author hungtv
	 *
     */
    public static String getTableName(Class<?> entityClass) {
        init(entityClass);
        TableInfo fi = info.get(entityClass);
        return (fi != null) ? fi.getTableName() : null;
    }

    public static final TableInfo init(Class<?> entityClass) {

        TableInfo fi = info.get(entityClass);
        if (fi != null) {
            return fi;
        }

        Method[] methods = entityClass.getMethods();
        fi = new TableInfo();

        String tableName = null;

        List<String> columnNames = new ArrayList<>();
        List<String> propertyNames = new ArrayList<>();
        List<Class<?>> clazz = new ArrayList<>();

        Table table = entityClass.getAnnotation(Table.class);
        tableName = (table == null) ? null : table.name();
        fi.setTableName(tableName);

        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith(METHOD_GET)) {
                name = name.substring(METHOD_GET.length());
            } else if (name.startsWith(METHOD_BOOLEAN)) {
                name = name.substring(METHOD_BOOLEAN.length());
            } else {
                continue;
            }
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);

            Column column = method.getAnnotation(Column.class);
            Id id = method.getAnnotation(Id.class);

            if (column != null) {
                columnNames.add(column.name());
                propertyNames.add(name);
                clazz.add(method.getReturnType());

                if (id != null) {
                    fi.setIdColumn(column.name());
                    fi.setIdColumnType(method.getReturnType());
                    fi.setIdPropertyName(name);
                }
            }
        }

        Field[] fields = entityClass.getFields();
        if (fields != null) {
            for (Field field : fields) {
                Annotation[] annotations = field.getAnnotations();
                if (annotations != null) {
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof Column) {
                            Column column = (Column) annotation;
                            String name = column.name();
                            propertyNames.add(field.getName());
                            columnNames.add(name);
                            clazz.add(field.getType());
                        } else if (annotation instanceof Id) {
                            Column column = field.getAnnotation(Column.class);
                            if (column == null) {
                                throw new IllegalArgumentException("Cannot found Column annotation!");
                            }
                            String name = column.name();
                            fi.setIdColumn(name);
                            fi.setIdColumnType(field.getType());
                            fi.setIdPropertyName(field.getName());
                        }
                    }
                }
            }
        }

        String[] tm_columnNames = new String[columnNames.size()];
        columnNames.toArray(tm_columnNames);

        String[] tm_propertyNames = new String[propertyNames.size()];
        propertyNames.toArray(tm_propertyNames);

        Class<?>[] tm_clazz = new Class<?>[clazz.size()];
        clazz.toArray(tm_clazz);

        fi.setPropertyNames(tm_propertyNames);
        fi.setColumnNames(tm_columnNames);
        fi.setTypes(tm_clazz);

        info.put(entityClass, fi);
        return fi;
    }

}

class TableInfo {

    private String tableName;
    private String idColumn;
    private String idField;
    private Class<?> idColumnType;
    private String[] columnNames;
    private String[] propertyNames;
    private Class<?>[] fieldTypes;

    public TableInfo() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIdColumnName() {
        return idColumn;
    }

    public void setIdColumn(String idColumn) {
        this.idColumn = idColumn;
    }

    public Class<?> getIdColumnType() {
        return idColumnType;
    }

    public void setIdColumnType(Class<?> idColumnType) {
        this.idColumnType = idColumnType;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public String[] getPropertydNames() {
        return propertyNames;
    }

    public void setPropertyNames(String[] propertyNames) {
        this.propertyNames = propertyNames;
    }

    public Class<?>[] getTypes() {
        return fieldTypes;
    }

    public void setTypes(Class<?>[] types) {
        this.fieldTypes = types;
    }

    public String getIdPropertyName() {
        return idField;
    }

    public void setIdPropertyName(String idField) {
        this.idField = idField;
    }
}
