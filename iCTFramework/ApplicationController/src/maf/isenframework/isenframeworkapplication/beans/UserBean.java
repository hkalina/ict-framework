package maf.isenframework.isenframeworkapplication.beans;

import java.sql.SQLException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import java.util.Locale;
import java.util.ResourceBundle;

import maf.isenframework.isenframeworkapplication.dao.BadgeDao;
import maf.isenframework.isenframeworkapplication.dao.RecordDao;
import maf.isenframework.isenframeworkapplication.dao.UserDao;
import maf.isenframework.isenframeworkapplication.data.EducationalRecord;
import maf.isenframework.isenframeworkapplication.data.HealthRecord;
import maf.isenframework.isenframeworkapplication.data.SocialRecord;
import maf.isenframework.isenframeworkapplication.data.User;
import maf.isenframework.isenframeworkapplication.data.User.Level;

import oracle.adf.model.datacontrols.device.DeviceManager;
import oracle.adf.model.datacontrols.device.DeviceManagerFactory;

import oracle.adfmf.amx.event.ActionEvent;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;
import oracle.adfmf.javax.faces.model.SelectItem;
import oracle.adfmf.util.BundleFactory;
import oracle.adfmf.util.Utility;

/**
 * Bean allowing manipulation with users.
 * 
 * @see User
 */
public class UserBean {

    private User current;
    private User copyFrom;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private UserDao dao;
    private RecordDao<HealthRecord> healthRecordDao;
    private RecordDao<SocialRecord> socialRecordDao;
    private RecordDao<EducationalRecord> educationalRecordDao;
    private String DeleteMessage;
    private boolean currentRequirePassword;

    public UserBean() {
        healthRecordDao = new RecordDao<HealthRecord>(HealthRecord.class);
        socialRecordDao = new RecordDao<SocialRecord>(SocialRecord.class);
        educationalRecordDao = new RecordDao<EducationalRecord>(EducationalRecord.class);
        dao = new UserDao(educationalRecordDao, socialRecordDao, healthRecordDao, new BadgeDao());
        //get location file to bundle structure
        ResourceBundle bundle = BundleFactory.getBundle("maf.isenframework.isenframeworkapplication.ApplicationControllerBundle");
        setDeleteMessage(Utility.getResourceString(bundle, "DETAIL_USER_ALERT_DELETE_USER",null));
    }
    
    public UserDao getDao() {
        return dao;
    }

    public List<User> getAdminsList() throws SQLException {
        User logged = (User) AdfmfJavaUtilities.getELValue("#{LoginBean.logged}");
        if (logged.getLevel() == User.Level.ADMIN) {
            return dao.getByLevel(User.Level.ADMIN);
        } else {
            return Collections.emptyList();
        }
    }

    public List<User> getAssistantsList() throws SQLException {
        User logged = (User) AdfmfJavaUtilities.getELValue("#{LoginBean.logged}");
        if (logged.getLevel() == User.Level.ADMIN) {
            return dao.getByLevel(User.Level.ASSISTANT);
        } else {
            List<User> assistants = dao.getByLevelAndManager(User.Level.ASSISTANT, logged);
            assistants.add(logged);
            return assistants;
        }
    }

    public List<User> getClientsList() throws SQLException {
        User logged = (User) AdfmfJavaUtilities.getELValue("#{LoginBean.logged}");
        if (logged.getLevel() == User.Level.ADMIN) {
            return dao.getByLevel(User.Level.CLIENT);
        } else {
            return dao.getByLevelAndManager(User.Level.CLIENT, logged);
        }
    }

    public boolean getAdminsNotEmpty() throws SQLException {
        return ! getAdminsList().isEmpty();
    }

    public boolean getAssistantsNotEmpty() throws SQLException {
        return ! getAssistantsList().isEmpty();
    }

    public boolean getClientsNotEmpty() throws SQLException {
        return ! getClientsList().isEmpty();
    }

    public SelectItem[] getManagerSelectItems() throws SQLException {
        User logged = (User) AdfmfJavaUtilities.getELValue("#{LoginBean.logged}");
        List<SelectItem> selectItems = new LinkedList<SelectItem>();
        if (logged.getLevel() == Level.ADMIN) {
            selectItems.add(new SelectItem(0, "Nestanoven"));
        }
        for (User user : getAssistantsList()) { // trimmed by permissions, contain logged assitant
            selectItems.add(new SelectItem(user.getId(), user.getFullname()));
        }
        for (User user : getAdminsList()) { // already trimmed by permissions
            selectItems.add(new SelectItem(user.getId(), user.getFullname()));
        }
        return selectItems.toArray(new SelectItem[selectItems.size()]);
    }
    
    public SelectItem[] getClientSelectItems() throws SQLException {
        List<SelectItem> selectItems = new LinkedList<SelectItem>();
        for (User user : getClientsList()) {
            selectItems.add(new SelectItem(user.getId(), user.getFullname()));
        }
        return selectItems.toArray(new SelectItem[selectItems.size()]);
    }

    public User getCurrent() {
        return current;
    }

    public void setCurrent(User current) throws SQLException {
        this.current = current;
        reloadDetails();
        reloadLists();
    }

    public int getCopyFromId() {
        return copyFrom == null ? 0 : copyFrom.getId();
    }

    public void setCopyFromId(int copyFromId) throws SQLException {
        if (copyFromId == 0) {
            this.copyFrom = null;
        } else {
            this.copyFrom = dao.getById(copyFromId).get(0);
        }
    }

    public User getBlank() throws SQLException {
        User user = new User(); // template of new user
        user.setLevel(User.Level.CLIENT);
        
        User logged = (User) AdfmfJavaUtilities.getELValue("#{LoginBean.logged}");
        if (!logged.getIsAdmin()) user.setManager(logged);
        
        dao.initForeignCollections(user);
        return user;
    }

    public void saveCurrent(ActionEvent actionEvent) throws SQLException {
        if (current.getId() == 0) {
            Object userapps = current.getUserApplicationsIds();
            dao.add(current);
            current.setUserApplicationsIds(userapps);
            if (copyFrom != null) {
                for (HealthRecord record : healthRecordDao.getByUser(copyFrom)) {
                    healthRecordDao.copyToUser(record, current);
                }
                for (SocialRecord record : socialRecordDao.getByUser(copyFrom)) {
                    socialRecordDao.copyToUser(record, current);
                }
                for (EducationalRecord record : educationalRecordDao.getByUser(copyFrom)) {
                    educationalRecordDao.copyToUser(record, current);
                }
            }
        } else {
            dao.update(current);
        }
        reloadLists();
        
        //porovname jestli se jedna o stejneho uzivatele a pokud ano, tak zmenime jazyk na ten, ktery byl zvolen
        User logged = (User) AdfmfJavaUtilities.getELValue("#{LoginBean.logged}");
        if (current.equals(logged)) {
            Locale.setDefault(new Locale(current.getLanguageString()));
        }
    }
    
    public void reloadLists() throws SQLException {
        propertyChangeSupport.firePropertyChange("current", null, current);
        propertyChangeSupport.firePropertyChange("adminsList", null, getAdminsList());
        propertyChangeSupport.firePropertyChange("assistantsList", null, getAssistantsList());
        propertyChangeSupport.firePropertyChange("clientsList", null, getClientsList());
        propertyChangeSupport.firePropertyChange("adminsNotEmpty", null, getAdminsNotEmpty());
        propertyChangeSupport.firePropertyChange("assistantsNotEmpty", null, getAssistantsNotEmpty());
        propertyChangeSupport.firePropertyChange("clientsNotEmpty", null, getClientsNotEmpty());
        propertyChangeSupport.firePropertyChange("managerSelectItems", null, getManagerSelectItems());
        propertyChangeSupport.firePropertyChange("clientSelectItems", null, getClientSelectItems());
    }
    
    public void reloadDetails() throws SQLException {
        propertyChangeSupport.firePropertyChange("current", null, current);
        propertyChangeSupport.firePropertyChange("currentIsClient", null, getCurrentIsClient());
        propertyChangeSupport.firePropertyChange("currentIsAdmin", null, getCurrentIsAdmin());
        propertyChangeSupport.firePropertyChange("currentImagePath", null, getCurrentImagePath());
    }

    public boolean getCurrentRequirePassword() {
        return currentRequirePassword || current.hasPassword();
    }
    
    public void setCurrentRequirePassword(boolean require) {
        propertyChangeSupport.firePropertyChange("currentRequirePassword", this.currentRequirePassword, require);
        this.currentRequirePassword = require;
        if (!require) this.current.setPassword(null);
    }
    
    public boolean getCurrentIsClient() {
        return current.getIsClient();
    }
    
    public boolean getCurrentIsAdmin() {
        return current.getIsAdmin();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
    public void onDeleteButtonClicked(ActionEvent actionEvent) throws SQLException {
        Utility.ApplicationLogger.severe("Removing user "+current.getId());
        dao.delete(current);
        reloadLists();
    }
    
    public void setDeleteMessage(String DeleteMessage) {
        String oldDeleteMessage = this.DeleteMessage;
        this.DeleteMessage = DeleteMessage;
        propertyChangeSupport.firePropertyChange("DeleteMessage", oldDeleteMessage, DeleteMessage);
    }

    public String getDeleteMessage() {
        return DeleteMessage;
    }

    public String getCurrentImagePath() {
        return current.getImagePath();
    }

    
    public void getPictureFromDeviceCamera(ActionEvent actionEvent) throws SQLException {
        this.pictureFromDevice(DeviceManager.CAMERA_SOURCETYPE_CAMERA);
        
    }

    public void getPictureFromDeviceAlbum(ActionEvent actionEvent) throws SQLException {
        this.pictureFromDevice(DeviceManager.CAMERA_SOURCETYPE_PHOTOLIBRARY);
    }
    
    /*modeOfDevicePicture - if it is 1, that means device manager opens camera and user can make photo
                          - if it is 2, that means devide manager opens album of device*/ 
    private void pictureFromDevice(int modeOfDevicePicture) throws SQLException {
        DeviceManager deviceManager = DeviceManagerFactory.getDeviceManager();
        int QUALITY_OF_PICTURE = 50;
        /* There are two posibilities:
         * first - 0 means type Data, device manager returns image as base64 encoded string
         * second - 1 means type File, device manager returns URI of image in phone.*/
        //@SuppressWarnings("oracle.jdeveloper.java.semantic-warning")
        int DESTINATION_TYPE = 1;
        /* There are two posibilities:
         * first - true means, that user can edit image after selection
         * second - false means, that user cannot edit image after selection*/        
        boolean ALLOW_EDIT = false;
        /* There are two posibilities:
         * first - 0 means type JPEG
         * second - 1 means type PNG */
        int ENCODING_TYPE = 1;
        //vaule in pixels of picture.
        int TARGET_WIGHT = 300;
        int TARGET_HEIGHT = 300;
        String picture = deviceManager.getPicture(QUALITY_OF_PICTURE, DESTINATION_TYPE, modeOfDevicePicture, ALLOW_EDIT, ENCODING_TYPE, TARGET_WIGHT, TARGET_HEIGHT);
        if (picture != null && picture.trim().length()>0){
            this.current.setImagePath(picture);
            propertyChangeSupport.firePropertyChange("currentImagePath", null, getCurrentImagePath());
        } 
    }
}
