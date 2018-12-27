// ===========================================================================
// CONTENT  : CLASS ObjectIdentifierDB
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.5 - 22/02/2008
// HISTORY  :
//  05/01/2001  duma  CREATED
//  02/12/2001  duma  moved from com.mdcs.db.util
//	29/01/2002	duma	bugfix	-> Closing connection
//	28/06/2002	duma	changed	-> Support blocks of ids
//	22/12/2003	duma	changed	-> Use logger instead of stdout and stderr
//	22/02/2008	mdu		changed	-> Support setting blockSize from outside
//
// Copyright (c) 2001-2008, by Manfred Duchrow. All rights reserved.
// ===========================================================================
package org.pfsw.db.util;

// ===========================================================================
// IMPORTS
// ===========================================================================
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.pfsw.db.LoggerProvider;
import org.pfsw.logging.Logger;

/**
 * Instances of this class provide generation of unique identifiers
 * backed by a specific database table. That means the next available
 * id will be always updated in the database.
 * 
 * @author M.Duchrow
 * @version 1.5
 */
public class ObjectIdentifierDB extends ObjectIdentifierGenerator
{
  // =========================================================================
  // CONSTANTS
  // =========================================================================
  private static final boolean DEBUG = "true".equals(System.getProperty("org.pfsw.db.debug", "false"));
  
  protected static final String OID_TABLE_NAME = "OIDADMIN";
  protected static final String OID_CN_CATEGORY = "CATEGORY";
  protected static final String OID_CN_NEXTID = "NEXTID";
  protected static final String OID_CN_BLOCKSIZE = "BLOCKSIZE";

  protected static final int INITIAL_BLOCKSIZE = 1;

  // =========================================================================
  // INSTANCE VARIABLES
  // =========================================================================
  private DataSource dataSource = null;
  private String category = "$DEFAULT";
  protected boolean tableInitialized = false;
  private String select = null;
  private String selectCategory = null;
  private String selectAny = null;
  private String update = null;
  private String qualifier = null;
  private long lastPrefetchedId = 0;
  protected Integer blockSize = null;

  // =========================================================================
  // CONSTRUCTORS
  // =========================================================================
  /**
   * Initialize the new instance with the given data source.
   *
   * @param ds A valid data source that allows connection to a database
   */
  public ObjectIdentifierDB(DataSource ds)
  {
    this(null, ds);
  }

  /**
   * Initialize the new instance with the given data source.
   *
   * @param tableQualifier A qualifier that is put in front of the table name
   * @param ds A valid data source that allows connection to a database
   */
  public ObjectIdentifierDB(String tableQualifier, DataSource ds)
  {
    this.setDataSource(ds);
    this.setQualifier(tableQualifier);
  }

  /**
   * Initialize the new instance with the data source.
   * Assign a category where the OIDs belong to.
   *
   * @param ds A valid data source that allows connection to a database
   * @param categoryName The name of the OID's category
   */
  public ObjectIdentifierDB(DataSource ds, String categoryName)
  {
    this(ds);
    if ((categoryName != null) && (categoryName.length() > 0))
    {
      this.setCategory(categoryName);
    }
  }

  /**
   * Initialize the new instance with the data source.
   * Assign a category where the OIDs belong to.
   *
   * @param tableQualifier A qualifier that is put in front of the table name
   * @param ds A valid data source that allows connection to a database
   * @param categoryName The name of the OID's category
   */
  public ObjectIdentifierDB(String tableQualifier, DataSource ds, String categoryName)
  {
    this(tableQualifier, ds);
    if ((categoryName != null) && (categoryName.length() > 0))
    {
      this.setCategory(categoryName);
    }
  }

  // =========================================================================
  // PUBLIC INSTANCE METHODS
  // =========================================================================
  /**
   * Returns the block size this generator is using.
   */
  public int getBlockSize()
  {
    if (blockSize == null)
    {
      return INITIAL_BLOCKSIZE;
    }
    return blockSize.intValue();
  }

  /**
   * Set the block size this generator is using.
   */
  public void setBlockSize(int newValue)
  {
    if (newValue > 0)
    {
      blockSize = new Integer(newValue);
    }
  }

  // =========================================================================
  // PROTECTED INSTANCE METHODS
  // =========================================================================

  @Override
  protected long getNextId()
  {
    if (this.getNextAvailableId() > this.getLastPrefetchedId())
    {
      this.loadNextIdFromDB();
    }
    return this.getNextAvailableId();
  }

  protected void loadNextIdFromDB()
  {
    long id = 0;

    if (this.tableInitialized())
    {
      id = this.idFromDB();
      this.setNextAvailableId(id);
    }
  }

  @Override
  protected void setNextId(long id)
  {
    if (id > this.getLastPrefetchedId())
    {
      this.loadNextIdFromDB();
    }
    else
    {
      this.setNextAvailableId(id);
    }
  }

  protected void setNextIdInDB(Connection conn, long id) throws SQLException
  {
    PreparedStatement statement = null;

    statement = conn.prepareStatement(this.sqlUpdateNextId());
    statement.setString(1, Long.toString(id));
    statement.execute();
  }

  protected boolean tableInitialized()
  {
    Connection conn = null;
    PreparedStatement statement = null;
    String action = null;
    boolean ok = false;

    if (tableInitialized)
    {
      return true;
    }
    try
    {
      action = "Reading";
      conn = this.getDbConnection();
      ok = this.checkTableExists(conn);

      if (!ok)
      {
        action = "Creating";
        this.createOidTable(conn);
        ok = true;
      }

      if (ok)
      {
        ok = this.checkCategoryRowExists(conn);
        if (!ok)
        {
          action = "Init";
          this.createRowForCategory(conn);
          ok = true;
        }
      }
      tableInitialized = ok;
    }
    catch (SQLException ex)
    {
      this.reportSQLException(action + " OID table failed.", ex);
    }
    finally
    {
      this.closeStatement(statement);
      this.closeConnection(conn);
    }

    return tableInitialized;
  }

  protected long idFromDB()
  {
    Connection conn = null;
    PreparedStatement statement = null;
    ResultSet result = null;
    String idStr = null;
    long id = 0;
    int currentBlockSize = 0;
    long nextBlockStart = 0;

    try
    {
      conn = this.getDbConnection();
      statement = conn.prepareStatement(this.sqlSelectNextId());
      result = statement.executeQuery();
      if (result.next())
      {
        idStr = result.getString(OID_CN_NEXTID);
        id = Long.parseLong(idStr);
        if (blockSize == null)
        {
          currentBlockSize = result.getInt(OID_CN_BLOCKSIZE);
        }
        else
        {
          currentBlockSize = blockSize.intValue();
        }

        if (currentBlockSize <= 0)
        {
          currentBlockSize = 1;
        }

        nextBlockStart = id + currentBlockSize;
        this.setNextIdInDB(conn, nextBlockStart);
        this.setLastPrefetchedId(nextBlockStart - 1);
      }
    }
    catch (SQLException ex)
    {
      this.reportSQLException("Reading OID table failed.", ex);
    }
    finally
    {
      this.closeStatement(statement);
      this.closeConnection(conn);
    }
    return id;
  }

  protected String sqlCreateOidTable()
  {
    StringBuffer buffer = new StringBuffer(200);

    buffer.append("CREATE TABLE ");
    buffer.append(this.getTableName());
    buffer.append("(\n  ");
    buffer.append(OID_CN_CATEGORY);
    buffer.append("\t VARCHAR(50),\n  ");
    buffer.append(OID_CN_NEXTID);
    buffer.append("\t VARCHAR(50),\n");
    buffer.append(OID_CN_BLOCKSIZE);
    buffer.append("\t INTEGER )\n");

    return buffer.toString();
  }

  protected String sqlInsertCategoryRow(String cat)
  {
    StringBuffer buffer = new StringBuffer(200);

    buffer.append("INSERT INTO ");
    buffer.append(this.getTableName());
    buffer.append(" VALUES ( '");
    buffer.append(cat);
    buffer.append("', '");
    buffer.append(Long.toString(this.getDefaultStartId()));
    buffer.append("', ");
    buffer.append(this.getBlockSize());
    buffer.append(" )");

    return buffer.toString();
  }

  protected String sqlUpdateNextId()
  {
    if (this.getUpdate() == null)
    {
      StringBuffer buffer = new StringBuffer(200);

      buffer.append("UPDATE ");
      buffer.append(this.getTableName());
      buffer.append(" SET ");
      buffer.append(OID_CN_NEXTID);
      buffer.append("=? WHERE ");
      buffer.append(OID_CN_CATEGORY);
      buffer.append(" = '");
      buffer.append(this.getCategory());
      buffer.append("'");

      this.setUpdate(buffer.toString());
    }
    return this.getUpdate();
  }

  protected String sqlSelectNextId()
  {
    if (this.getSelect() == null)
    {
      StringBuffer buffer = new StringBuffer(200);

      buffer.append("SELECT ");
      buffer.append(OID_CN_NEXTID);
      buffer.append(", ");
      buffer.append(OID_CN_BLOCKSIZE);
      buffer.append(" FROM ");
      buffer.append(this.getTableName());
      buffer.append(" WHERE ");
      buffer.append(OID_CN_CATEGORY);
      buffer.append(" = '");
      buffer.append(this.getCategory());
      buffer.append("'");

      this.setSelect(buffer.toString());
    }
    return this.getSelect();
  }

  protected String sqlSelectCategory()
  {
    if (this.getSelectCategory() == null)
    {
      StringBuffer buffer = new StringBuffer(200);

      buffer.append("SELECT ");
      buffer.append(OID_CN_CATEGORY);
      buffer.append(" FROM ");
      buffer.append(this.getTableName());
      buffer.append(" WHERE ");
      buffer.append(OID_CN_CATEGORY);
      buffer.append(" = '");
      buffer.append(this.getCategory());
      buffer.append("'");

      this.setSelectCategory(buffer.toString());
    }
    return this.getSelectCategory();
  }

  protected String sqlSelectAny()
  {
    if (this.getSelectAny() == null)
    {
      StringBuffer buffer = new StringBuffer(200);

      buffer.append("SELECT ");
      buffer.append(OID_CN_CATEGORY);
      buffer.append(" FROM ");
      buffer.append(this.getTableName());

      this.setSelectAny(buffer.toString());
    }
    return this.getSelectAny();
  }

  protected String categoryString()
  {
    StringBuffer buffer = new StringBuffer(200);

    buffer.append("'");
    buffer.append(this.getCategory());
    buffer.append("'");

    return buffer.toString();
  }

  protected void createOidTable(Connection conn) throws SQLException
  {
    Statement statement;

    statement = conn.createStatement();
    statement.execute(this.sqlCreateOidTable());
    conn.commit();
    this.createRowForCategory(conn);
  }

  protected void createRowForCategory(Connection conn) throws SQLException
  {
    Statement statement;

    statement = conn.createStatement();
    statement.execute(this.sqlInsertCategoryRow(this.getCategory()));
    conn.commit();
  }

  protected boolean checkTableExists(Connection conn)
  {
    try
    {
      this.anyRowExists(conn, this.sqlSelectAny());
      return true; // Don't care how many rows exist.
    }
    catch (SQLException e)
    {
      if (DEBUG)
      {
        e.printStackTrace();
      }
      return false;
    }
  }

  protected boolean checkCategoryRowExists(Connection conn)
  {
    try
    {
      return this.anyRowExists(conn, this.sqlSelectCategory());
    }
    catch (SQLException e)
    {
      if (DEBUG)
      {
        e.printStackTrace();
      }
      return false;
    }
  }

  protected boolean anyRowExists(Connection conn, String sql) throws SQLException
  {
    PreparedStatement statement = null;
    ResultSet result = null;
    boolean found = false;

    statement = conn.prepareStatement(sql);
    result = statement.executeQuery();
    found = result.next();
    result.close();
    return found;
  }

  protected void reportSQLException(String msg, SQLException ex)
  {
    this.logger().logError(msg, ex);
  }

  protected String getTableName()
  {
    if (this.getQualifier() == null)
    {
      return OID_TABLE_NAME;
    }
    return this.getQualifier() + "." + OID_TABLE_NAME;
  }

  protected Connection getDbConnection() throws SQLException
  {
    return this.getDataSource().getConnection();
  }

  protected void closeConnection(Connection conn)
  {
    if (conn != null)
    {
      try
      {
        conn.close();
      }
      catch (SQLException ex)
      {
        this.logger().logException(ex);
      }
    }
  }

  protected void closeStatement(Statement stmt)
  {
    if (stmt != null)
    {
      try
      {
        stmt.close();
      }
      catch (SQLException ex)
      {
        this.logger().logException(ex);
      }
    }
  }

  protected Logger logger()
  {
    return LoggerProvider.getLogger();
  }

  protected DataSource getDataSource()
  {
    return this.dataSource;
  }

  protected void setDataSource(DataSource newValue)
  {
    this.dataSource = newValue;
  }

  protected String getCategory()
  {
    return this.category;
  }

  protected void setCategory(String newValue)
  {
    this.category = newValue;
  }

  protected String getSelect()
  {
    return this.select;
  }

  protected void setSelect(String newValue)
  {
    this.select = newValue;
  }

  protected String getSelectCategory()
  {
    return this.selectCategory;
  }

  protected void setSelectCategory(String newValue)
  {
    this.selectCategory = newValue;
  }

  protected String getSelectAny()
  {
    return this.selectAny;
  }

  protected void setSelectAny(String newValue)
  {
    selectAny = newValue;
  }

  protected String getUpdate()
  {
    return this.update;
  }

  protected void setUpdate(String newValue)
  {
    this.update = newValue;
  }

  protected String getQualifier()
  {
    return this.qualifier;
  }

  protected void setQualifier(String newValue)
  {
    this.qualifier = newValue;
  }

  protected long getLastPrefetchedId()
  {
    return this.lastPrefetchedId;
  }

  protected void setLastPrefetchedId(long newValue)
  {
    this.lastPrefetchedId = newValue;
  }
}
