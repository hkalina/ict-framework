package maf.isenframework.isenframeworkapplication.beans;

import java.sql.SQLException;

import java.util.LinkedList;
import java.util.List;

import maf.isenframework.isenframeworkapplication.dao.RecordDao;
import maf.isenframework.isenframeworkapplication.dao.UserDao;
import maf.isenframework.isenframeworkapplication.data.AbstractRecord;
import maf.isenframework.isenframeworkapplication.data.EducationalRecord;
import maf.isenframework.isenframeworkapplication.data.HealthRecord;
import maf.isenframework.isenframeworkapplication.data.SocialRecord;
import maf.isenframework.isenframeworkapplication.data.User;

import oracle.adfmf.amx.event.ActionEvent;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;
import oracle.adfmf.util.Utility;

/**
 * Bean allowing coping profiles records between users.
 * 
 * @see AbstractRecord
 */
public class CopyBean {
    
    protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    
    private String recordsTypeForCopy = "";
    private List<Integer> userIdsForCopy = new LinkedList<Integer>();
    private List<Integer> recordIdsForCopy = new LinkedList<Integer>();
    private List<Integer> targetUserIdsForCopy = new LinkedList<Integer>();
    
    public CopyBean() {}
    
    public String getRecordsTypeForCopy() {
        return recordsTypeForCopy;
    }
    
    public void setRecordsTypeForCopy(String recordsTypeForCopy) {
        this.recordsTypeForCopy = recordsTypeForCopy;
        userIdsForCopy.clear();
        recordIdsForCopy.clear();
        targetUserIdsForCopy.clear();
        
        User current = (User) AdfmfJavaUtilities.getELValue("#{UserBean.current}");
        userIdsForCopy.add(current.getId());
        targetUserIdsForCopy.add(current.getId());
        
        propertyChangeSupport.firePropertyChange("recordsTypeForCopy", null, getRecordsTypeForCopy());
        propertyChangeSupport.firePropertyChange("userIdsForCopy", null, getUserIdsForCopy());
        propertyChangeSupport.firePropertyChange("recordIdsForCopy", null, getRecordIdsForCopy());
        propertyChangeSupport.firePropertyChange("targetUserIdsForCopy", null, getTargetUserIdsForCopy());
    }
    
    public Object getUserIdsForCopy() {
        Utility.ApplicationLogger.severe("TEST "+userIdsForCopy.size());
        return userIdsForCopy;
    }
    
    public void setUserIdsForCopy(Object ids) throws SQLException {
        Utility.ApplicationLogger.severe("TEST "+((Object[]) ids).length);
        userIdsForCopy.clear();
        for(Object id : (Object[]) ids) {
            userIdsForCopy.add((Integer) id);
        }
        ((HealthRecordBean) AdfmfJavaUtilities.getELValue("#{HealthRecordBean}")).onUserIdsForCopyChange();
        ((SocialRecordBean) AdfmfJavaUtilities.getELValue("#{SocialRecordBean}")).onUserIdsForCopyChange();
        ((EducationalRecordBean) AdfmfJavaUtilities.getELValue("#{EducationalRecordBean}")).onUserIdsForCopyChange();
        propertyChangeSupport.firePropertyChange("userIdsForCopy", null, getUserIdsForCopy());
    }
    
    public Object getRecordIdsForCopy() {
        Utility.ApplicationLogger.severe("TEST "+recordIdsForCopy.size());
        return recordIdsForCopy;
    }
    
    public void setRecordIdsForCopy(Object ids) {
        Utility.ApplicationLogger.severe("TEST "+((Object[]) ids).length);
        recordIdsForCopy.clear();
        for(Object id : (Object[]) ids) {
            recordIdsForCopy.add((Integer) id);
        }
        propertyChangeSupport.firePropertyChange("recordIdsForCopy", null, getRecordIdsForCopy());
    }
    
    public Object getTargetUserIdsForCopy() {
        Utility.ApplicationLogger.severe("TEST "+targetUserIdsForCopy.size());
        return targetUserIdsForCopy;
    }
    
    public void setTargetUserIdsForCopy(Object ids) {
        Utility.ApplicationLogger.severe("TEST "+((Object[]) ids).length);
        targetUserIdsForCopy.clear();
        for(Object id : (Object[]) ids) {
            targetUserIdsForCopy.add((Integer) id);
        }
        propertyChangeSupport.firePropertyChange("targetUserIdsForCopy", null, getTargetUserIdsForCopy());
    }
    
    public void copy(ActionEvent event) {
        System.out.println("COPING...");
        try {
            AbstractRecordBean bean = null;
            if ("HEALTH".equals(recordsTypeForCopy)) bean = (AbstractRecordBean) AdfmfJavaUtilities.getELValue("#{HealthRecordBean}");
            else if ("SOCIAL".equals(recordsTypeForCopy)) bean = (AbstractRecordBean) AdfmfJavaUtilities.getELValue("#{SocialRecordBean}");
            else if ("EDUCATIONAL".equals(recordsTypeForCopy)) bean = (AbstractRecordBean) AdfmfJavaUtilities.getELValue("#{EducationalRecordBean}");
            else throw new RuntimeException("Unknown records type!");
            
            RecordDao recordDao = bean.getDao();
            UserDao userDao = (UserDao) AdfmfJavaUtilities.getELValue("#{UserBean.dao}");
            
            List<AbstractRecord> records = recordDao.getByIds(recordIdsForCopy);
            for (User target : userDao.getByIds(targetUserIdsForCopy)) {
                for (AbstractRecord record : records) {
                    System.out.println("COPY "+record.getId()+"-"+target.getId());
                    recordDao.copyToUser(record, target);
                }
            }
            bean.refreshRecords();
            
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
}
