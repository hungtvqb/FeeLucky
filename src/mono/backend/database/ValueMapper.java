package mono.backend.database;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * su dung trong truong hop chi de lay vai thong tin hoac mot dto
 * @author quangnv11
 * 
 * @param <T>
 */

public class ValueMapper<T> implements RowMapper<T> {

    Class<T> clazz;
    String column;

	public ValueMapper(String column, Class<T> clazz) {
		this.column = column;
		this.clazz = clazz;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		if (Long.class.isAssignableFrom(clazz))
			return (T) (Object) rs.getLong(column);
		if (String.class.isAssignableFrom(clazz))
			return (T) (Object) rs.getString(column);
		if (Date.class.isAssignableFrom(clazz))
			return (T) (Object) rs.getDate(column);
		if (Integer.class.isAssignableFrom(clazz))
			return (T) (Object) rs.getInt(column);
		return rs.getObject(column, clazz);
	}



}
