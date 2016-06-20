package maf.isenframework.isenframeworkapplication.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Object representing record of health profile of the client.
 */
@DatabaseTable
public class HealthRecord extends AbstractRecord {

    @DatabaseField
    private String status;

    @DatabaseField
    private String anamnesis;

    @DatabaseField
    private String diagnosticSummary;

    @DatabaseField
    private String recommendation;

    @DatabaseField
    private String regimeMeasures;

    @DatabaseField
    private String conclusion;

    public HealthRecord() {} // ORM object need no-args constructor

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAnamnesis() {
        return anamnesis;
    }

    public void setAnamnesis(String anamnesis) {
        this.anamnesis = anamnesis;
    }

    public String getDiagnosticSummary() {
        return diagnosticSummary;
    }

    public void setDiagnosticSummary(String diagnosticSummary) {
        this.diagnosticSummary = diagnosticSummary;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getRegimeMeasures() {
        return regimeMeasures;
    }

    public void setRegimeMeasures(String regimeMeasures) {
        this.regimeMeasures = regimeMeasures;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public String getLabel() {
        return composeLabel(status, anamnesis);
    }

}
