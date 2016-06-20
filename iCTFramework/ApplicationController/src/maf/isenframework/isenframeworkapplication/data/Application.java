package maf.isenframework.isenframeworkapplication.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Object representing educational application in database.
 * 
 * @see UserApplication
 */
@DatabaseTable
public class Application extends Entity {

    @DatabaseField(allowGeneratedIdInsert = true, generatedId = true)
    private int id;

    @DatabaseField
    private String name;

    @DatabaseField
    private String urlScheme;
    
    public String DEFAULT_PATH_OF_APP_PICTURE = "/images/apps.png";
    
    @DatabaseField
    private String imagePath;

    public Application() {
        this.setImagePath(null);} // ORM object need no-args constructor

    public Application(int id) {
        this.id = id;
    }

    public Application(String name, String urlScheme, String picturePath) {
        this.name = name;
        this.urlScheme = urlScheme;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlScheme() {
        return urlScheme;
    }

    public void setUrlScheme(String urlScheme) {
        this.urlScheme = urlScheme;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if ( ! (obj instanceof Application)) return false;
        return this.id == ((Application) obj).id;
    }
    
    @Override
    public int hashCode() {
        return id;
    }
    

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        if (imagePath == null){
            this.imagePath = DEFAULT_PATH_OF_APP_PICTURE;
        }
        else{
            this.imagePath = imagePath;    
        }
    }
}
