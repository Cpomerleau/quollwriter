package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

public class UserConfigurableObjectTypeDataHandler implements DataHandler<UserConfigurableObjectType, NamedObject>
{

    private static final String STD_SELECT_PREFIX = "SELECT dbkey, userobjtype, name, pluralname, description, markup, icon, layout, lastmodified, datecreated, properties, id, version FROM userobjecttype_v ";

    private ObjectManager objectManager = null;

    /**
     * A cache, since the same type can be used in multiple places
     * we need a single object so that we have one object -> multiple uses.
     */
    private Map<Long, UserConfigurableObjectType> cache = new HashMap ();    
    
    public UserConfigurableObjectTypeDataHandler (ObjectManager om)
    {

        this.objectManager = om;

    }

    private UserConfigurableObjectType getUserConfigurableObjectType (ResultSet   rs,
                                                                      NamedObject parent,
                                                                      boolean     loadChildObjects)
                                                               throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            UserConfigurableObjectType t = this.cache.get (key);
            
            if (t != null)
            {
                
                return t;
                
            }                        
            
            t = new UserConfigurableObjectType ();
            t.setKey (key);
            t.setUserObjectType (rs.getString (ind++));
            t.setName (rs.getString (ind++));
            t.setObjectTypePluralName (rs.getString (ind++));
            t.setDescription (new StringWithMarkup (rs.getString (ind++),
                                                    rs.getString (ind++)));
            t.setIconName (rs.getString (ind++));
            t.setLayout (rs.getString (ind++));
            t.setLastModified (rs.getTimestamp (ind++));
            t.setDateCreated (rs.getTimestamp (ind++));
            t.setPropertiesAsString (rs.getString (ind++));
            t.setId (rs.getString (ind++));
            t.setVersion (rs.getString (ind++));            
            
            Connection conn = rs.getStatement ().getConnection ();                        
            
            this.objectManager.getObjects (UserConfigurableObjectTypeField.class,
                                           t,
                                           conn,
                                           loadChildObjects);
            
            if ((parent instanceof Project)
                &&
                (t.getUserObjectType () != null)
               )
            {
                
                ((Project) parent).addUserConfigurableObjectType (t.getUserObjectType (),
                                                                  t);
                
            }
            
            this.cache.put (key,
                            t);
            
            return t;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load user configurable object type",
                                        e);

        }

    }

    public List<UserConfigurableObjectType> getObjects (NamedObject parent,
                                                        Connection  conn,
                                                        boolean     loadChildObjects)
                                                 throws GeneralException
    {

        List<UserConfigurableObjectType> ret = new ArrayList ();

        try
        {

            List params = new ArrayList ();

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE",
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getUserConfigurableObjectType (rs,
                                                             parent,
                                                             loadChildObjects));

            }

            try
            {

                rs.close ();

            } catch (Exception e)
            {
            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load user configurable object types for: " +
                                        parent,
                                        e);

        }

        return ret;

    }

    public UserConfigurableObjectType getObjectByKey (long        key,
                                                      NamedObject parent,
                                                      Connection  conn,
                                                      boolean     loadChildObjects)
                                               throws GeneralException
    {

        UserConfigurableObjectType t = null;

        try
        {

            List params = new ArrayList ();
            params.add (key);

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE AND dbkey = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                t = this.getUserConfigurableObjectType (rs,
                                                        parent,
                                                        loadChildObjects);

            }

            try
            {

                rs.close ();

            } catch (Exception e)
            {
            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load user configurable object type for key: " +
                                        key,
                                        e);

        }

        return t;

    }

    public void createObject (UserConfigurableObjectType t,
                              Connection                 conn)
                       throws GeneralException
    {

        List params = new ArrayList ();
        params.add (t.getKey ());
        params.add (t.getUserObjectType ());
        params.add (t.getObjectTypePluralName ());
        params.add (t.getIconName ());
        params.add (t.getLayout ());        
        
        this.objectManager.executeStatement ("INSERT INTO userobjecttype (dbkey, userobjtype, pluralname, icon, layout) VALUES (?, ?, ?, ?, ?)",
                                             params,
                                             conn);

        // Save the fields.
        this.objectManager.saveObjects (t.getConfigurableFields (),
                                        conn);
                                             
    }

    public void deleteObject (UserConfigurableObjectType t,
                              boolean                    deleteChildObjects,                              
                              Connection                 conn)
                       throws GeneralException
    {

        // Delete the fields first.
        this.objectManager.deleteObjects (t.getConfigurableFields (),
                                          conn);
    
        List params = new ArrayList ();
        params.add (t.getKey ());

        this.objectManager.executeStatement ("DELETE FROM userobjecttype WHERE dbkey = ?",
                                             params,
                                             conn);

    }

    public void updateObject (UserConfigurableObjectType t,
                              Connection                 conn)
                       throws GeneralException
    {

        List params = new ArrayList ();
        params.add (t.getObjectTypePluralName ());
        params.add (t.getIconName ());
        params.add (t.getLayout ());
        params.add (t.getKey ());

        this.objectManager.executeStatement ("UPDATE userobjecttype SET pluralname = ?, icon = ?, layout = ? WHERE dbkey = ?",
                                             params,
                                             conn);

        // Save the fields.
        this.objectManager.saveObjects (t.getConfigurableFields (),
                                        conn);

    }

}