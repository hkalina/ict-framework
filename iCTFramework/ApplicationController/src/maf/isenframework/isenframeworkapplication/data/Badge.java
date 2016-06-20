package maf.isenframework.isenframeworkapplication.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Object representing Badge - reward for success in educational application.
 * 
 * @see UserApplication
 */
@DatabaseTable
public class Badge extends Entity {

    @DatabaseField(allowGeneratedIdInsert = true, generatedId = true)
    private int id;

    @DatabaseField(foreign = true, uniqueIndexName = "unique_user_application_badge")
    private User user;

    @DatabaseField(foreign = true, uniqueIndexName = "unique_user_application_badge")
    private Application application;

    @DatabaseField(uniqueIndexName = "unique_user_application_badge")
    private String name;

    @DatabaseField(unknownEnumName = "NONE")
    private Level level;

    public enum Level {
        NONE, BRONZE, SILVER, GOLD, PLATINUM
    }

    @DatabaseField
    private int points;

    @Override
    public int getId() {
        return id;
    }

    public Badge() {} // ORM object need no-args constructor

    public Badge(User user, Application application, String name, Level level, int points) {
        super();
        this.user = user;
        this.application = application;
        this.name = name;
        this.level = level;
        this.points = points;
    }

    @Override
    public void setId(int id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

}
