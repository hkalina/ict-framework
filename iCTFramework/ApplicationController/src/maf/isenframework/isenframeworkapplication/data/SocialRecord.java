package maf.isenframework.isenframeworkapplication.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Object representing record of social profile of the client.
 */
@DatabaseTable
public class SocialRecord extends AbstractRecord {

    @DatabaseField
    private String relationships;

    @DatabaseField
    private String socialSituation;
    
    @DatabaseField
    private String eating;
    
    @DatabaseField
    private String hygiene;
    
    @DatabaseField
    private String riskSituations;

    public SocialRecord() {} // ORM object need no-args constructor

    public String getRelationships() {
        return relationships;
    }

    public void setRelationships(String relationships) {
        this.relationships = relationships;
    }

    public String getSocialSituation() {
        return socialSituation;
    }

    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    public String getEating() {
        return eating;
    }

    public void setEating(String eating) {
        this.eating = eating;
    }

    public String getHygiene() {
        return hygiene;
    }

    public void setHygiene(String hygiene) {
        this.hygiene = hygiene;
    }

    public String getRiskSituations() {
        return riskSituations;
    }

    public void setRiskSituations(String riskSituations) {
        this.riskSituations = riskSituations;
    }

    public String getLabel() {
        return composeLabel(relationships, socialSituation);
    }

}
