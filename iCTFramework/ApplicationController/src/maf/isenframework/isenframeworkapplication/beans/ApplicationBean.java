package maf.isenframework.isenframeworkapplication.beans;

import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import java.sql.SQLException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import maf.isenframework.isenframeworkapplication.dao.ApplicationDao;
import maf.isenframework.isenframeworkapplication.data.Application;

import maf.isenframework.isenframeworkapplication.data.User;
import maf.isenframework.isenframeworkapplication.data.UserApplication;

import oracle.adf.model.datacontrols.device.DeviceManager;

import oracle.adf.model.datacontrols.device.DeviceManagerFactory;

import oracle.adfmf.amx.event.ActionEvent;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;
import oracle.adfmf.javax.faces.model.SelectItem;
import oracle.adfmf.util.Utility;

 /**
  * Bean allowing manipulation with applications.
  * 
  * @see Application
  */
public class ApplicationBean {
    
    private Application current;
    private boolean isAllowedTimeSet = false;
    private int allowedTime = 60;
    private boolean isInKiosk = false;
    private List<Application> list;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private ApplicationDao dao;

    public ApplicationBean() throws SQLException {
        dao = new ApplicationDao();
        list = dao.getAll();
    }

    public Application getCurrent() {
        return current;
    }

    public void setCurrent(Application current) throws UnsupportedEncodingException {
        this.current = current;
        propertyChangeSupport.firePropertyChange("current", null, current);
        propertyChangeSupport.firePropertyChange("linkUrl", null, getLinkUrl());
        propertyChangeSupport.firePropertyChange("manageLinkUrl", null, getManageLinkUrl());
        propertyChangeSupport.firePropertyChange("currentImagePathApp", null, getCurrentImagePathApp());
        
    }
    
    public int getAllowedTime() {
        return allowedTime;
    }
    
    public void setAllowedTime(int allowedTime) throws UnsupportedEncodingException {
        this.allowedTime = allowedTime;
        propertyChangeSupport.firePropertyChange("allowedTime", null, allowedTime);
        propertyChangeSupport.firePropertyChange("linkUrl", null, getLinkUrl());
    }
    
    public boolean getIsAllowedTimeSet() {
        return isAllowedTimeSet;
    }
    
    public void setIsAllowedTimeSet(boolean isAllowedTimeSet) throws UnsupportedEncodingException {
        this.isAllowedTimeSet = isAllowedTimeSet;
        propertyChangeSupport.firePropertyChange("isAllowedTimeSet", null, isAllowedTimeSet);
        propertyChangeSupport.firePropertyChange("linkUrl", null, getLinkUrl());
    }
    
    public boolean getIsInKiosk() {
        return isInKiosk;
    }
    
    public void setIsInKiosk(boolean isInKiosk) {
        this.isInKiosk = isInKiosk;
    }
    
    public List<Application> getList() throws SQLException {
        list = dao.getAll();
        return list;
    }
    
    public void setList(List<Application> list) {
        List<Application> oldList = this.list;
        this.list = list;
        propertyChangeSupport.firePropertyChange("list", oldList, list);
    }
    
    public SelectItem[] getSelectItems() throws SQLException {
        List<SelectItem> selectItems = new LinkedList<SelectItem>();
        for (Application application : getList()) {
            selectItems.add(new SelectItem(application.getId(), application.getName()));
        }
        return selectItems.toArray(new SelectItem[selectItems.size()]);
    }

    public int getCount() throws SQLException {
        return dao.getAll().size();
    }
    
    public void saveCurrent() throws SQLException {
        if (current.getId() == 0) {
            dao.add(current);
        } else {
            dao.update(current);
        }
        propertyChangeSupport.firePropertyChange("current", null, current);
        propertyChangeSupport.firePropertyChange("list", null, getList());
        propertyChangeSupport.firePropertyChange("selectItems", null, getSelectItems());
    }
    
    public void deleteCurrent() throws SQLException {
        if (current.getId() != 0) {
            dao.delete(current);
        }
        current = getBlank();
        propertyChangeSupport.firePropertyChange("current", null, current);
        propertyChangeSupport.firePropertyChange("list", null, getList());
        propertyChangeSupport.firePropertyChange("selectItems", null, getSelectItems());
    }
    
    public Application getBlank() throws SQLException {
        return new Application();
    }
    
    private String getBaseLinkUrl() throws UnsupportedEncodingException {
        User currentUser = (User) AdfmfJavaUtilities.getELValue("#{UserBean.current}");
        User loggedUser = (User) AdfmfJavaUtilities.getELValue("#{LoginBean.logged}");
        
        String url = current.getUrlScheme() + (isInKiosk ? "kiosk" : "") + "://";
        url += "?return=" + (isInKiosk ? "ictframeworkkiosk" : "ictframework");
        
        url += "&userId=" + currentUser.getId();
        url += "&userLogin=" + encode(currentUser.getLogin());
        url += "&userName=" + encode(currentUser.getName());
        url += "&userSurname=" + encode(currentUser.getSurname());
        url += "&userLevel=" + currentUser.getLevelString();
        url += "&userManagerId=" + currentUser.getManagerId();
        url += "&userLanguage=" + currentUser.getLanguageString();
        
        url += "&loggedId=" + loggedUser.getId();
        url += "&loggedLogin=" + encode(loggedUser.getLogin());
        url += "&loggedGesture=" + encode(loggedUser.getGestureHash());
        url += "&loggedName=" + encode(loggedUser.getName());
        url += "&loggedSurname=" + encode(loggedUser.getSurname());
        url += "&loggedLevel=" + loggedUser.getLevelString();
        url += "&loggedLanguage=" + loggedUser.getLanguageString();
        
        url += "&canEscapeApp=" + Boolean.toString(currentUser.getCanEscapeApp());
        return url;
    }
    
    public String getLinkUrl() throws UnsupportedEncodingException {
        String url = getBaseLinkUrl();
        url += "&allowedTime=" + (this.getIsAllowedTimeSet() ? this.getAllowedTime() : -1);
        return url;
    }
    
    public String getManageLinkUrl() throws UnsupportedEncodingException {
        String url = getBaseLinkUrl();
        url += "&manage=true";
        return url;
    }
    
    private String encode(String param) throws UnsupportedEncodingException {
        if (param == null) return "";
        return URLEncoder.encode(param, "UTF-8");
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
    public String getCurrentImagePathApp() {
        return current.getImagePath();
    }
    
    public void getPictureFromDeviceCamera(ActionEvent actionEvent) {
        this.pictureFromDevice(DeviceManager.CAMERA_SOURCETYPE_CAMERA);
    }

    public void getPictureFromDeviceAlbum(ActionEvent actionEvent) {
        this.pictureFromDevice(DeviceManager.CAMERA_SOURCETYPE_PHOTOLIBRARY);
    }
    
    /*modeOfDevicePicture - if it is 1, that means device manager opens camera and user can make photo
                          - if it is 2, that means devide manager opens album of device*/ 
    private void pictureFromDevice(int modeOfDevicePicture){
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
            propertyChangeSupport.firePropertyChange("currentImagePathApp", null, getCurrentImagePathApp());
        }
    }
}
