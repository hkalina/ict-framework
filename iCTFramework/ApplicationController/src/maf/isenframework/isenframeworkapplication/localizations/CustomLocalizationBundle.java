package maf.isenframework.isenframeworkapplication.localizations;
import java.util.*;

/**
 * English localization package.
 */
public class CustomLocalizationBundle extends ListResourceBundle {
    public CustomLocalizationBundle(){
        super();
    }
    public Object[][] getContents() {
        return contents;
    }

    @SuppressWarnings("oracle.jdeveloper.java.string-constructor")
    private Object[][] contents = {
        { "STATE", new String("ANGLICTINA") },
    };
}
