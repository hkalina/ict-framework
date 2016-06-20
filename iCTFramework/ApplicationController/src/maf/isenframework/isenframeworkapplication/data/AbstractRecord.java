package maf.isenframework.isenframeworkapplication.data;

import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

/**
 * Abstract superclass of all clients profile records classes
 */
public abstract class AbstractRecord extends Entity {
    
    @DatabaseField(allowGeneratedIdInsert = true, generatedId = true)
    private int id;
    
    @DatabaseField(foreign = true, foreignAutoRefresh = true, index = true)
    private User user;
    
    @DatabaseField
    private Date date;
    
    public AbstractRecord() {}
    
    @Override
    public int getId() {
        return id;
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
    
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    
    public String getLabel() {
        return "?";
    }
    
    protected String composeLabel(String first, String second) {
        StringBuilder builder = new StringBuilder();
        if (first != null) builder.append(first);
        if (first != null && second != null) builder.append("; ");
        if (second != null) builder.append(second);
        return builder.toString();
    }
    
}
