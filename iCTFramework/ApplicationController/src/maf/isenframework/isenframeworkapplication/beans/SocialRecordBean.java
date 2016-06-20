package maf.isenframework.isenframeworkapplication.beans;

import maf.isenframework.isenframeworkapplication.dao.RecordDao;
import maf.isenframework.isenframeworkapplication.data.SocialRecord;

/**
 * Bean allowing manipulation with social profile records.
 * 
 * @see SocialRecord
 */
public class SocialRecordBean extends AbstractRecordBean<SocialRecord> {
    
    public SocialRecordBean() {
        dao = new RecordDao<SocialRecord>(SocialRecord.class);
    }
    
    public SocialRecord getBlank() { // template of new record
        SocialRecord record = new SocialRecord();
        record.setUser(currentUser);
        return record;
    }
    
}
