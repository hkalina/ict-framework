package maf.isenframework.isenframeworkapplication.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import oracle.adfmf.util.Utility;

/**
 * Relationship between User and Application.
 * If this relationship exists, user is allowed to use the application.
 */
@DatabaseTable
public class UserApplication extends Entity {

    @DatabaseField(id = true, useGetSet = true)
    private String composedId;

    @DatabaseField(foreign = true, uniqueIndexName = "unique_user_application")
    private User user;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, uniqueIndexName = "unique_user_application")
    private Application application;

    public UserApplication() {} // ORM object need no-args constructor
    
    public UserApplication(User user, Application application) {
        this.user = user;
        this.application = application;
    }

    @Override
    public int getId() {
        return -1;
    }

    @Override
    public void setId(int id) {}

    public String getComposedId() {
        return (user == null ? "" : user.getId()) + "-" + (application == null ? "" : application.getId());
    }

    public void setComposedId(String composedId) {
        Utility.ApplicationLogger.severe("Setting ComposedId of "+getComposedId()+": "+composedId);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if ( ! (obj instanceof UserApplication)) return false;
        return this.application.getId() == ((UserApplication) obj).application.getId()
                   && this.user.getId() == ((UserApplication) obj).user.getId();
    }
    
    @Override
    public int hashCode() {
        return user.getId();
    }
}
