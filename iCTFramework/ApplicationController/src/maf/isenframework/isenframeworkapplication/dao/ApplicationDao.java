package maf.isenframework.isenframeworkapplication.dao;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;

import java.util.Iterator;
import java.util.List;

import maf.isenframework.isenframeworkapplication.data.Application;
import maf.isenframework.isenframeworkapplication.data.EducationalRecord;
import maf.isenframework.isenframeworkapplication.data.HealthRecord;
import maf.isenframework.isenframeworkapplication.data.SocialRecord;
import maf.isenframework.isenframeworkapplication.data.User;
import maf.isenframework.isenframeworkapplication.data.UserApplication;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.util.Utility;

/**
 * Data access object of Application entity.
 */
public class ApplicationDao extends AbstractDao<Application> {

    public ApplicationDao() {
        super(Application.class);
    }
    
    public List<Application> getByUrlScheme(String scheme) throws SQLException {
        QueryBuilder<Application, String> builder = dao.queryBuilder();
        builder.where().eq("urlScheme", scheme);
        PreparedQuery<Application> query = builder.prepare();
        return dao.query(query);
    }
    
    public void delete(Application app) throws SQLException {
        UserApplicationDao userAppDao = new UserApplicationDao();
        
        List<UserApplication> userApps = userAppDao.getByApplication(app);
        for(Iterator<UserApplication> i = userApps.iterator(); i.hasNext(); ) {
            userAppDao.delete(i.next());
        }
        
        super.delete(app);
    }

}
