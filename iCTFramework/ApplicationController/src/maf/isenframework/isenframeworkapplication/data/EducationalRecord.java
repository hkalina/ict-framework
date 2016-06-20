package maf.isenframework.isenframeworkapplication.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Object representing record of educational profile of the client.
 */
@DatabaseTable
public class EducationalRecord extends AbstractRecord {

    @DatabaseField
    private String needs;

    @DatabaseField
    private String characteristic;

    @DatabaseField
    private String acquiredCompetences;

    @DatabaseField
    private String targetCompetences;
    
    @DatabaseField
    private String methods;
    
    @DatabaseField
    private String therapies;
    
    public EducationalRecord() {} // ORM object need no-args constructor

    public String getNeeds() {
        return needs;
    }

    public void setNeeds(String needs) {
        this.needs = needs;
    }

    public String getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(String characteristic) {
        this.characteristic = characteristic;
    }

    public String getAcquiredCompetences() {
        return acquiredCompetences;
    }

    public void setAcquiredCompetences(String acquiredCompetences) {
        this.acquiredCompetences = acquiredCompetences;
    }

    public String getTargetCompetences() {
        return targetCompetences;
    }

    public void setTargetCompetences(String targetCompetences) {
        this.targetCompetences = targetCompetences;
    }

    public String getMethods() {
        return methods;
    }

    public void setMethods(String methods) {
        this.methods = methods;
    }

    public String getTherapies() {
        return therapies;
    }

    public void setTherapies(String therapies) {
        this.therapies = therapies;
    }
    
    public String getLabel() {
        return composeLabel(needs, characteristic);
    }
    
}
