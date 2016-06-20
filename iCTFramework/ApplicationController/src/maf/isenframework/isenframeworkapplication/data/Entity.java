package maf.isenframework.isenframeworkapplication.data;

/**
 * Abstract superclass of all database classes.
 * This type is required to support DAO.
 * Any database object has to have id.
 */
public abstract class Entity {

    public abstract int getId();

    public abstract void setId(int id);

}
