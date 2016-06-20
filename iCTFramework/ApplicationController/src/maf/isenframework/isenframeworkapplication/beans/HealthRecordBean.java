package maf.isenframework.isenframeworkapplication.beans;

import maf.isenframework.isenframeworkapplication.dao.RecordDao;
import maf.isenframework.isenframeworkapplication.data.HealthRecord;

/**
 * Bean allowing manipulation with health profile records.
 * 
 * @see HealthRecord
 */
public class HealthRecordBean extends AbstractRecordBean<HealthRecord> {
    
    public HealthRecordBean() {
        dao = new RecordDao<HealthRecord>(HealthRecord.class);
    }
    
    public HealthRecord getBlank() { // template of new record
        HealthRecord record = new HealthRecord();
        record.setUser(currentUser);
        return record;
    }

}