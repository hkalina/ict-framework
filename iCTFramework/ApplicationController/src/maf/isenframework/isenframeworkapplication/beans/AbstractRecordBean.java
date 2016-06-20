package maf.isenframework.isenframeworkapplication.beans;

import java.sql.SQLException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.LinkedList;
import java.util.List;

import maf.isenframework.isenframeworkapplication.dao.RecordDao;
import maf.isenframework.isenframeworkapplication.data.AbstractRecord;
import maf.isenframework.isenframeworkapplication.data.User;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;
import oracle.adfmf.javax.faces.model.SelectItem;

/**
 * Abstract superclass of all beans for profiles records manipulation.
 * 
 * @see AbstractRecord
 */
public abstract class AbstractRecordBean<T extends AbstractRecord> {
    
    protected T current;
    protected User currentUser;
    protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    protected RecordDao<T> dao;
    
    public AbstractRecordBean() {
        super();
    }
    
    public abstract T getBlank();
    
    public void setCurrentUser(User currentUser) throws Exception {
        this.currentUser = currentUser;
        propertyChangeSupport.firePropertyChange("current", null, current);
        propertyChangeSupport.firePropertyChange("currentUserRecords", null, getCurrentUserRecords());
    }
    
    public void refreshRecords() throws SQLException {
        propertyChangeSupport.firePropertyChange("currentUserRecords", null, getCurrentUserRecords());
    }
    
    public RecordDao<T> getDao() {
        return dao;
    }
    
    public List<T> getCurrentUserRecords() throws SQLException {
        return dao.getByUser(currentUser);
    }
    
    public T getLast() throws SQLException {
        List<T> list = dao.getByUserLimit(currentUser, 1);
        return list.size() > 0 ? list.get(0) : getBlank();
    }
    
    public List<T> getCurrentUserLastRecords() throws SQLException {
        return dao.getByUserLimit(currentUser, 3);
    }
    
    public T getCurrent() {
        if (current == null) current = getBlank();
        return current;
    }
    
    public void setCurrent(T current) {
        T old = this.current;
        this.current = current;
        propertyChangeSupport.firePropertyChange("current", old, current);
    }
    
    public void onUserIdsForCopyChange() throws SQLException {
        propertyChangeSupport.firePropertyChange("selectItemsForCopy", null, getSelectItemsForCopy());
    }
    
    public SelectItem[] getSelectItemsForCopy() throws SQLException {
        List<SelectItem> selectItems = new LinkedList<SelectItem>();
        List<Integer> ids = (List<Integer>) AdfmfJavaUtilities.getELValue("#{CopyBean.userIdsForCopy}");
        List<T> records = dao.getByUserIds(ids);
        DateFormat format = new SimpleDateFormat("d.M.yyyy");
        for (T record : records) {
            selectItems.add(new SelectItem(record.getId(), (record.getDate() == null ? "(neuvedeno)" : format.format(record.getDate())) + " " + record.getLabel()));
        }
        return selectItems.toArray(new SelectItem[selectItems.size()]);
    }
    
    public void saveCurrent() throws SQLException {
        if (current.getId() == 0) {
            dao.add(current);
        } else {
            dao.update(current);
        }
        propertyChangeSupport.firePropertyChange("current", null, current);
        propertyChangeSupport.firePropertyChange("currentUserRecords", null, getCurrentUserRecords());
    }
    
    public void deleteCurrent() throws SQLException {
        if (current.getId() != 0) {
            dao.delete(current);
        }
        current = getBlank();
        propertyChangeSupport.firePropertyChange("current", null, current);
        propertyChangeSupport.firePropertyChange("currentUserRecords", null, getCurrentUserRecords());
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
}
