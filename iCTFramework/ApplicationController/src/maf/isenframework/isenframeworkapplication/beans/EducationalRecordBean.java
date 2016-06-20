package maf.isenframework.isenframeworkapplication.beans;

import maf.isenframework.isenframeworkapplication.dao.RecordDao;
import maf.isenframework.isenframeworkapplication.data.EducationalRecord;

/**
 * Bean allowing manipulation with educational profile records.
 * 
 * @see EducationalRecord
 */
public class EducationalRecordBean extends AbstractRecordBean<EducationalRecord> {
    
    public EducationalRecordBean() {
        super();
        dao = new RecordDao<EducationalRecord>(EducationalRecord.class);
    }
    
    public EducationalRecord getBlank() { // template of new record
        EducationalRecord record = new EducationalRecord();
        record.setUser(currentUser);
        return record;
    }
    
}
