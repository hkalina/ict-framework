package maf.isenframework.isenframeworkapplication.dao;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import maf.isenframework.isenframeworkapplication.data.Badge;
import maf.isenframework.isenframeworkapplication.data.EducationalRecord;
import maf.isenframework.isenframeworkapplication.data.HealthRecord;
import maf.isenframework.isenframeworkapplication.data.SocialRecord;
import maf.isenframework.isenframeworkapplication.data.User;
import maf.isenframework.isenframeworkapplication.data.UserApplication;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.util.Utility;

/**
 * Data access object of User entity.
 */
public class UserDao extends AbstractDao<User> {

    RecordDao<EducationalRecord> educationalRecordDao; // only to get max id when creating new record
    RecordDao<SocialRecord> socialRecordDao;
    RecordDao<HealthRecord> healthRecordDao;
    BadgeDao badgeDao;

    public UserDao(RecordDao<EducationalRecord> educationalRecordDao, RecordDao<SocialRecord> socialRecordDao, RecordDao<HealthRecord> healthRecordDao, BadgeDao badgeDao) {
        super(User.class);
        this.educationalRecordDao = educationalRecordDao;
        this.socialRecordDao = socialRecordDao;
        this.healthRecordDao = healthRecordDao;
        this.badgeDao = badgeDao;
    }

    public User getByLogin(String login) throws SQLException {
        QueryBuilder<User, String> builder = dao.queryBuilder();
        builder.where().eq("login", login.toLowerCase());
        PreparedQuery<User> query = builder.prepare();
        return dao.queryForFirst(query);
    }

    public List<User> getByLevel(User.Level level) throws SQLException {
        QueryBuilder<User, String> builder = dao.queryBuilder();
        builder.where().eq("level", level);
        PreparedQuery<User> query = builder.prepare();
        return dao.query(query);
    }

    public List<User> getByLevelAndManager(User.Level level, User manager) throws SQLException {
        Set<User> subordinates = new HashSet<User>();
        subordinates.add(manager);
        getSubordinatesOf(manager, subordinates);
        QueryBuilder<User, String> builder = dao.queryBuilder();
        builder.where().eq("level", level).and().in("manager_id", subordinates);
        PreparedQuery<User> query = builder.prepare();
        return dao.query(query);
    }
    
    public Set<User> getSubordinatesOf(User manager) throws SQLException {
        Set<User> subordinates = new HashSet<User>();
        getSubordinatesOf(manager, subordinates);
        return subordinates;
    }
    
    private Set<User> getSubordinatesOf(User manager, Set<User> subordinates) throws SQLException {
        for (User user : getDirectSubordinatesOf(manager)) {
            if (subordinates.add(user)) {
                Utility.ApplicationLogger.severe("subordinates add: "+user.getId()+" ADDED");
                getSubordinatesOf(user, subordinates);
            } else {
                Utility.ApplicationLogger.severe("subordinates add: "+user.getId()+" ALREADY PRESENT");
            }
        }
        return subordinates;
    }
    
    private List<User> getDirectSubordinatesOf(User manager) throws SQLException {
        QueryBuilder<User, String> builder = dao.queryBuilder();
        builder.where().eq("manager_id", manager);
        PreparedQuery<User> query = builder.prepare();
        return dao.query(query);
    }

    public void addEducationalRecord(User user, EducationalRecord record) throws SQLException {
        if (user.getEducationalRecords() == null) {
            dao.assignEmptyForeignCollection(user, "educationalRecords");
        }
        if(record.getId() == 0){ // generate id
            record.setId(educationalRecordDao.nextId());
        }
        user.getEducationalRecords().add(record);
    }

    public void addSocialRecord(User user, SocialRecord record) throws SQLException {
        if (user.getSocialRecords() == null) {
            dao.assignEmptyForeignCollection(user, "socialRecords");
        }
        if(record.getId() == 0){ // generate id
            record.setId(socialRecordDao.nextId());
        }
        user.getSocialRecords().add(record);
    }

    public void addHealthRecord(User user, HealthRecord record) throws SQLException {
        if (user.getHealthRecords() == null) {
            dao.assignEmptyForeignCollection(user, "healthRecords");
        }
        if(record.getId() == 0){ // generate id
            record.setId(healthRecordDao.nextId());
        }
        user.getHealthRecords().add(record);
    }
    
    public void addCopyOfHealthRecord(User user, HealthRecord record) throws SQLException {
        record.setId(0);
        record.setUser(user);
        user.getHealthRecords().add(record);
    }

    public void addBadge(User user, Badge badge) throws SQLException {
        if (user.getHealthRecords() == null) {
            dao.assignEmptyForeignCollection(user, "badge");
        }
        if(badge.getId() == 0){ // generate id
            badge.setId(badgeDao.nextId());
        }
        user.getBadges().add(badge);
    }

    public void initForeignCollections(User user) throws SQLException {
        if (user.getEducationalRecords() == null) {
            dao.assignEmptyForeignCollection(user, "educationalRecords");
        }
        if (user.getSocialRecords() == null) {
            dao.assignEmptyForeignCollection(user, "socialRecords");
        }
        if (user.getHealthRecords() == null) {
            dao.assignEmptyForeignCollection(user, "healthRecords");
        }
        if (user.getUserApplications() == null) {
            dao.assignEmptyForeignCollection(user, "userApplications");
        }
        if (user.getBadges() == null) {
            dao.assignEmptyForeignCollection(user, "badges");
        }
    }
    
    public void delete(User user) throws SQLException {
        RecordDao<SocialRecord> socialDao = (RecordDao<SocialRecord>) AdfmfJavaUtilities.getELValue("#{SocialRecordBean.dao}");
        RecordDao<HealthRecord> healthDao = (RecordDao<HealthRecord>) AdfmfJavaUtilities.getELValue("#{HealthRecordBean.dao}");
        RecordDao<EducationalRecord> educationalDao = (RecordDao<EducationalRecord>) AdfmfJavaUtilities.getELValue("#{EducationalRecordBean.dao}");
        
        Utility.ApplicationLogger.severe("Removing dependencies...");
        if (user.getSocialRecords() != null) {
            ForeignCollection<SocialRecord> collection = user.getSocialRecords();
            for(Iterator<SocialRecord> i = collection.iterator(); i.hasNext(); ) {
                socialDao.delete(i.next());
            }
        }
        if (user.getHealthRecords() != null) {
            ForeignCollection<HealthRecord> collection = user.getHealthRecords();
            for(Iterator<HealthRecord> i = collection.iterator(); i.hasNext(); ) {
                healthDao.delete(i.next());
            }
        }
        if (user.getEducationalRecords() != null) {
            ForeignCollection<EducationalRecord> collection = user.getEducationalRecords();
            for(Iterator<EducationalRecord> i = collection.iterator(); i.hasNext(); ) {
                educationalDao.delete(i.next());
            }
        }
        if (user.getUserApplications() != null) {
            UserApplicationDao uadao = new UserApplicationDao();
            ForeignCollection<UserApplication> collection = user.getUserApplications();
            for(Iterator<UserApplication> i = collection.iterator(); i.hasNext(); ) {
                uadao.delete(i.next());
            }
        }
        Utility.ApplicationLogger.severe("Dependencies removed.");
        
        // subordinates of given assistant will have new manager - manager of deleted assistant
        Utility.ApplicationLogger.severe("Fixing subordinate structure...");
        for (User subordinate : getDirectSubordinatesOf(user)) {
            subordinate.setManager(user.getManager());
            update(subordinate);
        }
        Utility.ApplicationLogger.severe("Subordinate structure fixed.");
        super.delete(user);
    }

}
