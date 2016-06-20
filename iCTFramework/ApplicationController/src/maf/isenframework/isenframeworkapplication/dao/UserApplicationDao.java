package maf.isenframework.isenframeworkapplication.dao;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;

import java.util.List;

import maf.isenframework.isenframeworkapplication.data.Application;
import maf.isenframework.isenframeworkapplication.data.UserApplication;

/**
 * Data access object of UserApplication entity.
 */
public class UserApplicationDao extends AbstractDao<UserApplication> {

    public UserApplicationDao() {
        super(UserApplication.class);
    }
    
    public List<UserApplication> getByApplication(Application app) throws SQLException {
        QueryBuilder<UserApplication, String> builder = dao.queryBuilder();
        builder.where().eq("application_id", app.getId());
        PreparedQuery<UserApplication> query = builder.prepare();
        return dao.query(query);
    }
    
}
