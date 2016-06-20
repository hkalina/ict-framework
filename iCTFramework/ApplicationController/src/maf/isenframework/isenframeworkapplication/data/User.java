package maf.isenframework.isenframeworkapplication.data;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ResourceBundle;

import maf.isenframework.isenframeworkapplication.beans.UserBean;
import maf.isenframework.isenframeworkapplication.dao.UserDao;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.util.BundleFactory;
import oracle.adfmf.util.Utility;

/**
 * Object representing user of system in database.
 */
@DatabaseTable
public class User extends Entity {

    @DatabaseField(allowGeneratedIdInsert = true, generatedId = true)
    private int id;

    @DatabaseField(unique = true)
    private String login;

    @DatabaseField
    private String password;

    @DatabaseField
    private String gesture;

    @DatabaseField(unknownEnumName = "CLIENT")
    private Level level = Level.CLIENT;

    public enum Level {
        CLIENT, ASSISTANT, ADMIN
    }
    
    //Language is used format ISO-639-lowercase-language-code
    public enum Language {
        en, //english
        cs  //czech
    }
    
    public String DEFAULT_PATH_OF_PICTURE = "/images/account-circle48x48.png";
    
    @DatabaseField
    private Language language = Language.cs;

    @DatabaseField
    private String name;

    @DatabaseField
    private String surname;
    
    @DatabaseField
    private Bool canEscapeApp;
    
    @DatabaseField
    private Bool canEscapeFramework;
    
    @DatabaseField
    private String imagePath;
    
    
    public enum Bool {
        TRUE, FALSE
    }
    
    @DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
    private User manager; // auto-refresh cause cyclic reference exception (even if maxForeignAutoRefreshLevel=1)

    @ForeignCollectionField
    private ForeignCollection<HealthRecord> healthRecords;

    @ForeignCollectionField
    private ForeignCollection<SocialRecord> socialRecords;

    @ForeignCollectionField
    private ForeignCollection<EducationalRecord> educationalRecords;

    @ForeignCollectionField
    private ForeignCollection<Badge> badges;
    
    @ForeignCollectionField
    private ForeignCollection<UserApplication> userApplications;

    public User() {
        this.setImagePath(null);} // ORM object need no-args constructor

    public User(String login, String password, String gesture, Level level, String name, String surname, boolean canEscapeApp, boolean canEscapeFramework, Language language, String picturePath) {
        this.login = login;
        this.setPassword(password);
        this.setGesture(gesture);
        this.level = level;
        this.name = name;
        this.surname = surname;
        this.canEscapeApp = canEscapeApp ? Bool.TRUE : Bool.FALSE;
        this.canEscapeFramework = canEscapeFramework ? Bool.TRUE : Bool.FALSE;
        this.language = language;
        this.setImagePath(picturePath);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login.toLowerCase();
    }

    public String getPassword() {
        return "";
    }
    
    public boolean hasPassword() {
        return password != null && password.length() > 0;
    }
    
    public String getPasswordHash() {
        if (password == null || password.length() == 0) return null;
        return password;
    }

    public boolean verifyPassword(String password) {
        if (this.password == null) return false;
        return this.password.equals(hash(password));
    }

    public void setPassword(String password) {
        if (password != null && password.length() > 0) {
            this.password = hash(password);
        } else {
            this.password = null;
        }
    }
    
    public String getGesture() {
        return "";
    }
    
    public String getGestureHash() {
        if (gesture == null || gesture.length() == 0) return null;
        return gesture;
    }

    public boolean verifyGesture(String gesture) {
        if (this.gesture == null) return false;
        return this.gesture.equals(hash(gesture));
    }

    public void setGesture(String gesture) {
        if (gesture != null && gesture.length() > 0) {
            this.gesture = hash(gesture);
        } else {
            this.gesture = null;
        }
    }

    public void setLanguage(User.Language language) {
        this.language = language;
    }

    public User.Language getLanguage() {
        return language;
    }
    
    public String getImagePath() {
        return this.imagePath;
    }

    public void setImagePath(String imagePath) {
        if(imagePath == null) {
            this.imagePath = DEFAULT_PATH_OF_PICTURE;
        }
        else{
            this.imagePath = imagePath;    
        }
    }

    private String hash(String password) {
        if(password == null){
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) throws SQLException {
        this.level = level;
        ((UserBean) AdfmfJavaUtilities.getELValue("#{UserBean}")).reloadDetails();
    }

    public String getLevelString() {
        return level == null ? Level.CLIENT.toString() : level.toString();
    }
    
    public String getLevelLocalizedString() {
        ResourceBundle bundle = BundleFactory.getBundle("maf.isenframework.isenframeworkapplication.ApplicationControllerBundle");
        return Utility.getResourceString(bundle, "EDIT_USER_CHOICE_" + getLevelString(), null);
    }

    public void setLevelString(String level) throws SQLException {
        setLevel(Level.valueOf(level));
    }
    
    public String getLanguageString() {
        return language == null ? "cs" : language.toString();
    }

    public void setLanguageString(String language) {
        this.language = Language.valueOf(language);
    }
    
    public boolean getIsAdmin() {
        return this.level == Level.ADMIN;
    }
    
    public boolean getIsClient() {
        return this.level == Level.CLIENT;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
    
    public String getFullname() {
        if (name != null && surname != null) return name + " " + surname;
        if (name != null) return name;
        if (surname != null) return surname;
        ResourceBundle bundle = BundleFactory.getBundle("maf.isenframework.isenframeworkapplication.ApplicationControllerBundle");
        return Utility.getResourceString(bundle, "USER_NO_NAME", null);
    }
    
    public boolean getCanEscapeApp() {
        return canEscapeApp == Bool.TRUE;
    }
    
    public void setCanEscapeApp(boolean canEscapeApp) {
        this.canEscapeApp = canEscapeApp ? Bool.TRUE : Bool.FALSE;
    }
    
    public boolean getCanEscapeFramework() {
        return canEscapeFramework == Bool.TRUE;
    }
    
    public void setCanEscapeFramework(boolean canEscapeFramework) {
        this.canEscapeFramework = canEscapeFramework ? Bool.TRUE : Bool.FALSE;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public Integer getManagerId() {
        return manager == null ? null : manager.getId();
    }

    public void setManagerId(Integer managerId) {
        if (managerId == null || managerId == 0 || managerId == id) {
            this.manager = null;
        } else {
            this.manager = new User();
            this.manager.setId(managerId);
        }
    }

    public ForeignCollection<HealthRecord> getHealthRecords() {
        return healthRecords;
    }

    public void setHealthRecords(ForeignCollection<HealthRecord> healthRecords) {
        this.healthRecords = healthRecords;
    }

    public ForeignCollection<SocialRecord> getSocialRecords() {
        return socialRecords;
    }

    public void setSocialRecords(ForeignCollection<SocialRecord> socialRecords) {
        this.socialRecords = socialRecords;
    }

    public ForeignCollection<EducationalRecord> getEducationalRecords() {
        return educationalRecords;
    }

    public void setEducationalRecords(ForeignCollection<EducationalRecord> educationalRecords) {
        this.educationalRecords = educationalRecords;
    }

    public ForeignCollection<Badge> getBadges() {
        return badges;
    }

    public void setBadges(ForeignCollection<Badge> badges) {
        this.badges = badges;
    }

    public ForeignCollection<UserApplication> getUserApplications() {
        return userApplications;
    }

    public void setUserApplications(ForeignCollection<UserApplication> userApplications) {
        this.userApplications = userApplications;
    }
    
    public List<Application> getUserApplicationsList() throws SQLException {
        userApplications.refreshCollection();
        List<Application> list = new LinkedList<Application>();
        for(Iterator<UserApplication> i = userApplications.iterator(); i.hasNext(); ) {
            Application application = i.next().getApplication();
            Utility.ApplicationLogger.severe("Adding: "+application.getName());
            list.add(application);
        }
        return list;
    }
    
    public Object getUserApplicationsIds() {
        List<Integer> list = new LinkedList<Integer>();
        if(userApplications != null) {
            for(Iterator<UserApplication> i = userApplications.iterator(); i.hasNext(); ) {
                Application application = i.next().getApplication();
                list.add(application.getId());
            }
        }
        return list.toArray();
    }
    
    public void setUserApplicationsIds(Object ids) {
        if(userApplications == null) {
            throw new RuntimeException("userApplications attribute is null!");
        }
        try {
            for(Iterator<UserApplication> i = userApplications.iterator(); i.hasNext(); ) {
                UserApplication current = i.next();
                if( ! userApplications.remove(current)){
                    Utility.ApplicationLogger.severe("Removing userapplication "+current.getId()+" FAILED");
                }
            }
            for(Object object : (Object[]) ids) {
                Integer id = (Integer) object;
                Utility.ApplicationLogger.severe("Adding application "+id+" to user");
                userApplications.add(new UserApplication(this, new Application(id)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    public boolean getEditableByLogged() throws SQLException {
        UserDao userDao = (UserDao) AdfmfJavaUtilities.getELValue("#{UserBean.dao}");
        User logged = (User) AdfmfJavaUtilities.getELValue("#{LoginBean.logged}");
        if (logged.getIsAdmin()) return true;
        if (logged.equals(this)) return false;
        return userDao.getSubordinatesOf(logged).contains(this);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if ( ! (obj instanceof User)) return false;
        return this.id == ((User) obj).id;
    }
    
    @Override
    public int hashCode() {
        return id;
    }
}
