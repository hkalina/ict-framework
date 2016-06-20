package maf.isenframework.isenframeworkapplication;

import java.sql.SQLException;
import java.util.Date;

import maf.isenframework.isenframeworkapplication.dao.ApplicationDao;
import maf.isenframework.isenframeworkapplication.dao.BadgeDao;
import maf.isenframework.isenframeworkapplication.dao.RecordDao;
import maf.isenframework.isenframeworkapplication.dao.UserApplicationDao;
import maf.isenframework.isenframeworkapplication.dao.UserDao;
import maf.isenframework.isenframeworkapplication.data.Application;
import maf.isenframework.isenframeworkapplication.data.Badge;
import maf.isenframework.isenframeworkapplication.data.EducationalRecord;
import maf.isenframework.isenframeworkapplication.data.HealthRecord;
import maf.isenframework.isenframeworkapplication.data.SocialRecord;
import maf.isenframework.isenframeworkapplication.data.User;
import maf.isenframework.isenframeworkapplication.data.UserApplication;
import oracle.adfmf.application.LifeCycleListener;
import oracle.adfmf.framework.event.EventSource;
import oracle.adfmf.framework.event.EventSourceFactory;

/**
 * Listener of application lifecycle.
 * @see oracle.adfmf.application.LifeCycleListener
 */
public class LifeCycleListenerImpl implements LifeCycleListener {
    public LifeCycleListenerImpl() {}

    /**
     * The start method will be called at the start of the application.
     */
    @Override
    public void start() {
        ApplicationDao applicationDao = new ApplicationDao();
        try {
            if (! applicationDao.tableExists()) {
                initDatabase();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
        }
        EventSource openURLEventSource = EventSourceFactory.getEventSource(EventSourceFactory.OPEN_URL_EVENT_SOURCE_NAME);
        openURLEventSource.addListener(new UrlSchemeListener());
    }

    private void initDatabase() throws SQLException {
        ApplicationDao applicationDao = new ApplicationDao();
        RecordDao<EducationalRecord> educationalRecordDao = new RecordDao<EducationalRecord>(EducationalRecord.class);
        RecordDao<SocialRecord> socialRecordDao = new RecordDao<SocialRecord>(SocialRecord.class);
        RecordDao<HealthRecord> healthRecordDao = new RecordDao<HealthRecord>(HealthRecord.class);
        BadgeDao badgeDao = new BadgeDao();
        UserApplicationDao userApplicationDao = new UserApplicationDao();
        UserDao userDao = new UserDao(educationalRecordDao, socialRecordDao, healthRecordDao, badgeDao);

        applicationDao.createTable();
        badgeDao.createTable();
        educationalRecordDao.createTable();
        socialRecordDao.createTable();
        healthRecordDao.createTable();
        userApplicationDao.createTable();
        userDao.createTable();

        // testing data
        /*
        Application app1 = new Application("Aplikace ANO/NE", "yesno",null);
        Application app2 = new Application("Překlad znakové řeči", "signtrans",null);
        Application app3 = new Application("Testovací aplikace", "test",null);
        applicationDao.add(app1);
        applicationDao.add(app2);
        applicationDao.add(app3);//*/
        

        User user1 = new User("admin", "", "123", User.Level.ADMIN, "Ivana", "Správcová", true, true, User.Language.cs,null);
        user1.setCanEscapeFramework(true);
        userDao.add(user1);

        User user2 = new User("asistent", "", "123", User.Level.ASSISTANT, "Jana", "Asistentová", true, false, User.Language.cs,null);
        userDao.add(user2);
        
        User user3 = new User("klient", "", "123", User.Level.CLIENT, "Ondřej", "Klient", false, false, User.Language.cs,null);
        userDao.add(user3); // needs id before using as foreign key

        EducationalRecord rec1 = new EducationalRecord();
        rec1.setCharacteristic("charakteristika...");
        rec1.setDate(new Date(1447447755000L));
        rec1.setNeeds("vzdělávací potřeby...");
        userDao.addEducationalRecord(user3, rec1);

        SocialRecord rec2 = new SocialRecord();
        rec2.setDate(new Date(1447437755000L));
        rec2.setRelationships("vztahy klienta");
        userDao.addSocialRecord(user3, rec2);

        HealthRecord rec3 = new HealthRecord();
        rec3.setDate(new Date(1447327755000L));
        rec3.setStatus("problém klienta");
        rec3.setAnamnesis("anamnéza");
        userDao.addHealthRecord(user3, rec3);
        /*
        userApplicationDao.add(new UserApplication(user1, app1));
        userApplicationDao.add(new UserApplication(user1, app2));
        userApplicationDao.add(new UserApplication(user3, app1));
        userApplicationDao.add(new UserApplication(user3, app2));
        */
        user3.setManager(user2);
        userDao.update(user3);
        
        //Badge badge1 = new Badge(user1, app3, "Testovací odznak", Badge.Level.GOLD, 123);
        //badgeDao.add(badge1);
    }

    /**
     * The stop method will be called at the termination of the application.
     */
    @Override
    public void stop() {
        // Add code here...
    }

    /**
     * The activate method will be called when the application is started (post
     * the start method) and when an application is resumed by the operating
     * system. If the application supports checkpointing, this is a place where
     * the application should read the checkpoint information and resume the
     * process.
     */
    @Override
    public void activate() {
        // Add code here...
    }

    /**
     * The deactivate method will be called as part of the application shutdown
     * process or when the application is being deactivated/hibernated by the
     * operating system. This is the place where application developers would
     * write application checkpoint information in either a database or a
     * "device only" file so if the application is terminated while in the
     * background the application can resume the process when the application is
     * reactivated.
     */
    @Override
    public void deactivate() {
        // Add code here...
    }

}