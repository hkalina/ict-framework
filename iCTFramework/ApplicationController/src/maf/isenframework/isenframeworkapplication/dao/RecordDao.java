package maf.isenframework.isenframeworkapplication.dao;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;

import java.util.List;

import maf.isenframework.isenframeworkapplication.data.AbstractRecord;
import maf.isenframework.isenframeworkapplication.data.User;

/**
 * Data access object of any *Record entity.
 */
public class RecordDao<T extends AbstractRecord> extends AbstractDao<T> {
    
    public RecordDao(Class<T> type) {
        super(type);
    }
    
    public List<T> getByUser(User user) throws SQLException {
        QueryBuilder<T, String> builder = dao.queryBuilder();
        builder.where().eq("user_id", user.getId());
        builder.orderBy("date", false);
        PreparedQuery<T> query = builder.prepare();
        return dao.query(query);
    }
    
    public List<T> getByUserLimit(User user, int limit) throws SQLException {
        QueryBuilder<T, String> builder = dao.queryBuilder();
        builder.where().eq("user_id", user.getId());
        builder.orderBy("date", false);
        builder.limit(new Long(limit));
        PreparedQuery<T> query = builder.prepare();
        return dao.query(query);
    }
    
    public List<T> getByUserIds(List<Integer> ids) throws SQLException {
        QueryBuilder<T, String> builder = dao.queryBuilder();
        builder.where().in("user_id", ids);
        builder.orderBy("date", false);
        PreparedQuery<T> query = builder.prepare();
        return dao.query(query);
    }
    
    public void copyToUser(T record, User target) throws SQLException {
        record.setId(0);
        record.setUser(target);
        add(record);
    }
    
}
