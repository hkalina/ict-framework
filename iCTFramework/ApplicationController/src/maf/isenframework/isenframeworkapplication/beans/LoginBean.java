package maf.isenframework.isenframeworkapplication.beans;

import java.sql.SQLException;

import java.util.Locale;
import java.util.ResourceBundle;

import maf.isenframework.isenframeworkapplication.dao.RecordDao;
import maf.isenframework.isenframeworkapplication.dao.UserDao;
import maf.isenframework.isenframeworkapplication.data.EducationalRecord;
import maf.isenframework.isenframeworkapplication.data.User;

import oracle.adfmf.amx.event.ActionEvent;
import oracle.adfmf.framework.api.AdfmfContainerUtilities;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;

import oracle.adfmf.util.BundleFactory;
import oracle.adfmf.util.Utility;

/**
 * Bean providing login functionality.
 * 
 * @see User
 */
public class LoginBean {

    private String formLogin = "admin";
    private String formPassword = "";
    private String formGesture = "";
    private String formMessage = "";
    private String defaultLanguage;
    private User logged;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private UserDao dao;

    public LoginBean() {
        dao = new UserDao(new RecordDao<EducationalRecord>(EducationalRecord.class), null, null, null);
        
        //set welcome label in application
        ResourceBundle bundle = BundleFactory.getBundle("maf.isenframework.isenframeworkapplication.ApplicationControllerBundle");
        String message = Utility.getResourceString(bundle, "LOGIN_WELCOME", null);
        setFormMessage(message);
        
       
        String defaultLang = Locale.getDefault().toString();
        if(defaultLang.length() > 2){
            //get default language of application, for us is very important fisrt 2 character, then we can change language of application
            defaultLanguage = defaultLang.substring(0, 2);    
        }
        else{
            defaultLanguage = defaultLang;
        }
    }

    public boolean tryLogIn() throws SQLException {
        //Locale.setDefault(Locale.getDefault());
        //http://www.ateam-oracle.com/error-handling-in-adf-mobile/
        //https://docs.oracle.com/javase/tutorial/i18n/resbundle/list.html
        //https://docs.oracle.com/middleware/1213/adf/develop/adf-bc-validation-rules.htm#ADFFD446
        
        //get location file to bundle structure
        ResourceBundle bundle = BundleFactory.getBundle("maf.isenframework.isenframeworkapplication.ApplicationControllerBundle");
        
        if (formLogin == null || (formPassword == null && formGesture == null)) {
            setFormMessage(Utility.getResourceString(bundle, "LOGIN_WELCOME", null));
            return false;
        }
        
        User foundUser = dao.getByLogin(formLogin);
        if(foundUser == null){
            setFormMessage(Utility.getResourceString(bundle, "LOGIN_FAILED_USER_NAME", null));
            return false;
        }
        if (foundUser.hasPassword()) {
            if ( ! foundUser.verifyPassword(formPassword)) {
                setFormMessage(Utility.getResourceString(bundle, "LOGIN_FAILED_USER_PASSWORD", null));
                return false;
            }
        } else {
            if ( ! foundUser.verifyGesture(formGesture)) {
                setFormMessage(Utility.getResourceString(bundle, "LOGIN_FAILED_USER_GESTURE", null));
                return false;
            }
        }
        
        setLogged(foundUser);
        setFormPassword(null);
        setFormGesture(null);
        setFormMessage(Utility.getResourceString(bundle, "LOGIN_WELCOME", null));
        
        UserBean userBean = (UserBean) AdfmfJavaUtilities.getELValue("#{UserBean}");
        userBean.setCurrent(foundUser);
        userBean.reloadLists();
        
        //set aplication's language for language of user
        Locale.setDefault(new Locale(foundUser.getLanguageString()));
        if (foundUser.getIsClient()) {
            AdfmfContainerUtilities.invokeContainerJavaScriptFunction(AdfmfJavaUtilities.getFeatureName(),
                                    "adf.mf.api.amx.doNavigation", new Object[] { "clientLogin" });
        } else {
            AdfmfContainerUtilities.invokeContainerJavaScriptFunction(AdfmfJavaUtilities.getFeatureName(),
                                    "adf.mf.api.amx.doNavigation", new Object[] { "assistantLogin" });
        }
        return true;
    }

    public String getFormLogin() {
        return formLogin;
    }

    public void setFormLogin(String formLogin) {
        String oldFormLogin = this.formLogin;
        this.formLogin = formLogin;
        propertyChangeSupport.firePropertyChange("formLogin", oldFormLogin, formLogin);
    }

    public String getFormPassword() {
        return formPassword;
    }

    public void setFormPassword(String formPassword) {
        String oldFormPassword = this.formPassword;
        this.formPassword = formPassword;
        propertyChangeSupport.firePropertyChange("formPassword", oldFormPassword, formPassword);
    }
    
    public String getFormGesture() {
        return formGesture;
    }

    public void setFormGesture(String formGesture) {
        String oldFormGesture = this.formGesture;
        this.formGesture = formGesture;
        propertyChangeSupport.firePropertyChange("formGesture", oldFormGesture, formGesture);
    }

    public User getLogged() {
        Utility.ApplicationLogger.severe("logged="+logged.getLogin()+" admin="+logged.getIsAdmin());
        return logged;
    }

    public void setLogged(User logged) {
        User oldLogged = this.logged;
        this.logged = logged;
        propertyChangeSupport.firePropertyChange("logged", oldLogged, logged);
    }

    public String getFormMessage() {
        return formMessage;
    }

    public void setFormMessage(String formMessage) {
        String oldFormMessage = this.formMessage;
        this.formMessage = formMessage;
        propertyChangeSupport.firePropertyChange("formMessage", oldFormMessage, formMessage);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
    public void escapeFramework() {
        if (logged.getCanEscapeFramework()) {
            AdfmfContainerUtilities.invokeContainerJavaScriptFunction(AdfmfJavaUtilities.getFeatureName(), 
                    "KioskPlugin.exitKiosk", new Object[] {});
        }
    }
    
    public void logOut(ActionEvent actionEvent) {
        logged = null;
        AdfmfContainerUtilities.resetFeature("iCTFramework", true);
    }
    
    public void changeApplicationLanguageToLoggedUserLanguage(ActionEvent actionEvent){
        changeApplicationLanguage(logged.getLanguageString());  
    }
    
    public void changeApplicaitonLanguageToDefault(ActionEvent actionEvent){
        changeApplicationLanguage(this.defaultLanguage);
    }
    
    public void changeApplicationLanguage(String language){
        //set aplication's language for language of user  
        Locale.setDefault(new Locale(language));            
    }
}
