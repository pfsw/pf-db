// ===========================================================================
// CONTENT  : CLASS ObjectIdentifierDB
// AUTHOR   : Manfred Duchrow
// VERSION  : 2.0 - 18/04/2020
// HISTORY  :
//  05/01/2001  duma  CREATED
//  02/12/2001  duma  moved from com.mdcs.db.util
//	29/01/2002	duma	bugfix	-> Closing connection
//	28/06/2002	duma	changed	-> Support blocks of ids
//	22/12/2003	duma	changed	-> Use logger instead of stdout and stderr
//	22/02/2008	mdu		changed	-> Support setting blockSize from outside
//  18/04/2020  mdu   changed -> synchronized, connection.commit(), changeable column names
//
// Copyright (c) 2001-2020, by Manfred Duchrow. All rights reserved.
// ===========================================================================
package org.pfsw.db.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.pfsw.db.DatabaseAccessException;
import org.pfsw.db.LoggerProvider;
import org.pfsw.logging.Logger2;

/**
 * Instances of this class provide generation of unique identifiers
 * backed by a specific database table. That means the next available
 * id will be always updated in the database.
 * <p>
 * It allows the definition of a blockSize which determines how many IDs
 * are "loaded" into memory at once. A higher blockSize improves performance
 * of generating new IDs extremely, but also implies the risk to lose some
 * IDs if the application gets shut down and the in-memory IDs have not yet been consumed.
 * <p>
 * For a convenient way to setup a new instance see {@link ObjectIdentifierDBBuilder}.
 * <p>
 * In any critical (fatal) situation this class throws a {@link DatabaseAccessException}.
 * 
 * @author M.Duchrow
 * @version 2.0
 */
public class ObjectIdentifierDB extends ObjectIdentifierGenerator
{
  // =========================================================================
  // CONSTANTS
  // =========================================================================
  private static final boolean DEBUG = "true".equals(System.getProperty("org.pfsw.db.debug", "false"));

  public static final int INITIAL_BLOCKSIZE = 1;

  // =========================================================================
  // INSTANCE VARIABLES
  // =========================================================================
  private final DataSource dataSource;
  private String category = "$DEFAULT";
  protected boolean tableCreated = false;
  protected boolean categoryInitialized = false;
  private long lastPrefetchedId = 0;
  private Integer blockSize = INITIAL_BLOCKSIZE;

  private IdGeneratorTableSpec tableSpec = IdGeneratorTableSpec.create();

  private String sqlSelectForUpdateStatement = null;
  private String sqlSelectCategoryStatement = null;
  private String sqlSelectAnyStatement = null;
  private String sqlUpdateStatement = null;

  // =========================================================================
  // CLASS METHODS
  // =========================================================================
  /**
   * Creates a new instance with the data source and category.
   *
   * @param ds A valid data source that allows connection to a database (must not be null).
   * @param categoryName The name of the OID's category (must not be null).
   */
  public static ObjectIdentifierDB create(DataSource ds, String categoryName)
  {
    return new ObjectIdentifierDB(ds, categoryName);
  }

  /**
   * Creates a new instance with the data source and category.
   *
   * @param ds A valid data source that allows connection to a database (must not be null).
   * @param categoryName The name of the OID's category (must not be null).
   */
  public static ObjectIdentifierDB create(DataSource ds, IdGeneratorTableSpec tableSpec, String categoryName)
  {
    return create(ds, categoryName).setTableSpec(tableSpec);
  }
  
  // =========================================================================
  // CONSTRUCTORS
  // =========================================================================
  /**
   * Initialize the new instance with the given data source.
   *
   * @param ds A valid data source that allows connection to a database.
   */
  public ObjectIdentifierDB(DataSource ds)
  {
    this(null, ds);
  }

  /**
   * Initialize the new instance with the given data source.
   *
   * @param tableQualifier A qualifier that is put in front of the table name.
   * @param ds A valid data source that allows connection to a database.
   */
  public ObjectIdentifierDB(String tableQualifier, DataSource ds)
  {
    super();
    this.dataSource = ds;
    setTableQualifier(tableQualifier);
  }

  /**
   * Initialize the new instance with the data source.
   * Assign a category where the OIDs belong to.
   *
   * @param ds A valid data source that allows connection to a database.
   * @param categoryName The name of the OID's category.
   */
  public ObjectIdentifierDB(DataSource ds, String categoryName)
  {
    this(null, ds, categoryName);
  }

  /**
   * Initialize the new instance with the data source.
   * Assign a category where the OIDs belong to.
   *
   * @param tableQualifier A qualifier that is put in front of the table name.
   * @param ds A valid data source that allows connection to a database.
   * @param categoryName The name of the OID's category
   */
  public ObjectIdentifierDB(String tableQualifier, DataSource ds, String categoryName)
  {
    this(tableQualifier, ds);
    if (str().notNullOrBlank(categoryName))
    {
      setCategory(categoryName.trim());
    }
  }

  /**
   * Initialize the new instance with the data source.
   * Assign a category where the OIDs belong to.
   *
   * @param tableQualifier A qualifier that is put in front of the table name
   * @param ds A valid data source that allows connection to a database
   * @param categoryName The name of the OID's category
   * @param startId The first id to be generated.
   * @param idLength The length to which Ids are filled up with leading zeros.
   */
  public ObjectIdentifierDB(String tableQualifier, DataSource ds, String categoryName, long startId, int idLength)
  {
    super(startId, idLength);
    this.dataSource = ds;
    setTableQualifier(tableQualifier);
    if (str().notNullOrBlank(categoryName))
    {
      setCategory(categoryName.trim());
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
  public ObjectIdentifierDB setBlockSize(int newValue)
  {
    if (newValue > 0)
    {
      blockSize = new Integer(newValue);
    }
    return this;
  }

  @Override
  public synchronized long nextIdentifier()
  {
    if (getNextId() > getLastPrefetchedId())
    {
      loadNextIdFromDB();
    }
    return super.nextIdentifier();
  }

  // =========================================================================
  // PROTECTED INSTANCE METHODS
  // =========================================================================
  /**
   * This method should be invoked to prevent automatic table creation which makes
   * sense if the table has been create already externally.
   */
  protected void tableAlreadyCreated()
  {
    tableCreated = true;
  }

  protected synchronized void loadNextIdFromDB()
  {
    long id;

    if (isAllInitialized())
    {
      id = idFromDB();
      setNextId(id);
    }
  }

  protected boolean isAllInitialized() {
    return isTableCreated() & isCategoryInitialized();
  }
  
  protected synchronized boolean isTableCreated()
  {
    if (tableCreated)
    {
      return true;
    }
    tableCreated = initializeTableIfNecessary();
    return tableCreated;
  }

  protected synchronized boolean isCategoryInitialized()
  {
    if (categoryInitialized)
    {
      return true;
    }
    categoryInitialized = initializeCategory();
    return categoryInitialized;
  }

  protected boolean initializeTableIfNecessary()
  {
    Connection conn = null;
    PreparedStatement statement = null;
    String action = null;
    boolean ok = false;

    synchronized (getDataSource())
    {
      try
      {
        conn = getDbConnection();
      }
      catch (SQLException ex)
      {
        throw new DatabaseAccessException(ex, "Opening database connection for initializing table '%s' failed!", getTableName());
      }
      try
      {
        action = "Reading";
        ok = checkTableExists(conn);

        if (!ok)
        {
          action = "Creating";
          createOidTable(conn);
          ok = true;
        }

        if (ok)
        {
          ok = checkCategoryRowExists(conn);
          if (!ok)
          {
            action = "Init";
            createRowForCategory(conn);
            ok = true;
          }
        }
      }
      catch (SQLException ex)
      {
        throw new DatabaseAccessException(ex, "%s OID table '%s' failed.", action, getTableName());
      }
      finally
      {
        closeStatement(statement);
        closeConnection(conn);
      }
      return ok;
    }
  }

  protected boolean initializeCategory()
  {
    Connection conn;
    boolean ok = false;

    synchronized (getDataSource())
    {
      try
      {
        conn = getDbConnection();
      }
      catch (SQLException ex)
      {
        throw new DatabaseAccessException(ex, "Opening database connection for initializing ID generator '%s' failed!", getCategory());
      }
      try
      {
        ok = checkCategoryRowExists(conn);
        if (!ok)
        {
          ok = createRowForCategory(conn);
        }
      }
      catch (SQLException ex)
      {
        throw new DatabaseAccessException(ex, "Failed to create row for category '%s' in table '%s'.", getCategory(), getTableName());
      }
      finally
      {
        closeConnection(conn);
      }
      return ok;
    }
  }

  protected long idFromDB()
  {
    Connection conn = null;
    PreparedStatement statement = null;
    ResultSet result = null;
    long id = 0;
    int currentBlockSize = 0;
    long nextBlockStart = 0;

    try
    {
      conn = getDbConnection();
      conn.setAutoCommit(false);
    }
    catch (SQLException ex)
    {
      throw new DatabaseAccessException(ex, "Opening database for updating '%s' in table '%s' failed!", getCategory(), getTableName());
    }
    try
    {
      statement = conn.prepareStatement(sqlSelectNextId());
      result = statement.executeQuery();
      if (result.next())
      {
        id = result.getLong(getNextIdColumnName());
        logger().debugf("[%s] next-id from DB for category '%s': %d%n", Thread.currentThread().getName(), getCategory(), id);
        if (blockSize == null)
        {
          currentBlockSize = result.getInt(getBlockSizeColumnName());
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
        setNextIdInDB(conn, nextBlockStart);
        conn.commit();
        setLastPrefetchedId(nextBlockStart - 1);
      }
    }
    catch (SQLException ex)
    {
      rollback(conn);
      throw new DatabaseAccessException(ex, "Reading and updating '%s' table '%s' failed.", getCategory(), getTableName());
    }
    finally
    {
      closeStatement(statement);
      closeConnection(conn);
    }
    return id;
  }

  protected void setNextIdInDB(Connection conn, long id) throws SQLException
  {
    PreparedStatement statement = null;

    statement = conn.prepareStatement(sqlUpdateNextId());
    statement.setLong(1, id);
    statement.execute();
  }

  protected String sqlCreateOidTable()
  {
    StringBuffer buffer = new StringBuffer(200);

    buffer.append("CREATE TABLE ");
    buffer.append(getTableName());
    buffer.append("(\n  ");
    buffer.append(getCategoryColumnName());
    buffer.append("\t VARCHAR(50),\n  ");
    buffer.append(getNextIdColumnName());
    buffer.append("\t BIGINT,\n");
    buffer.append(getBlockSizeColumnName());
    buffer.append("\t INTEGER)\n");

    return buffer.toString();
  }

  protected String sqlInsertCategoryRow(String cat)
  {
    StringBuffer buffer = new StringBuffer(200);

    buffer.append("INSERT INTO ");
    buffer.append(getTableName());
    buffer.append(" VALUES ( '");
    buffer.append(cat);
    buffer.append("', '");
    buffer.append(Long.toString(getNextId()));
    buffer.append("', ");
    buffer.append(getBlockSize());
    buffer.append(" )");

    return buffer.toString();
  }

  protected String sqlUpdateNextId()
  {
    if (getSqlUpdateStatement() == null)
    {
      StringBuffer buffer = new StringBuffer(200);

      buffer.append("UPDATE ");
      buffer.append(getTableName());
      buffer.append(" SET ");
      buffer.append(getNextIdColumnName());
      buffer.append("=? WHERE ");
      buffer.append(getCategoryColumnName());
      buffer.append(" = '");
      buffer.append(getCategory());
      buffer.append("'");

      setSqlUpdateStatement(buffer.toString());
    }
    return getSqlUpdateStatement();
  }

  protected String sqlSelectNextId()
  {
    String statement;

    if (getSqlSelectForUpdateStatement() == null)
    {
      //@formatter:off
      statement = String.format("SELECT %s, %s FROM %s WHERE %s = '%s' FOR UPDATE%n", 
          getNextIdColumnName(), getBlockSizeColumnName(), getTableName(), getCategoryColumnName(), getCategory());
      //@formatter:on
      setSqlSelectForUpdateStatement(statement);
    }
    return getSqlSelectForUpdateStatement();
  }

  protected String sqlSelectCategory()
  {
    if (getSqlSelectCategoryStatement() == null)
    {
      StringBuffer buffer = new StringBuffer(200);

      buffer.append("SELECT ");
      buffer.append(getCategoryColumnName());
      buffer.append(" FROM ");
      buffer.append(getTableName());
      buffer.append(" WHERE ");
      buffer.append(getCategoryColumnName());
      buffer.append(" = '");
      buffer.append(getCategory());
      buffer.append("'");

      setSqlSelectCategoryStatement(buffer.toString());
    }
    return getSqlSelectCategoryStatement();
  }

  protected String sqlSelectAny()
  {
    if (getSqlSelectAnyStatement() == null)
    {
      StringBuffer buffer = new StringBuffer(200);

      buffer.append("SELECT ");
      buffer.append(getCategoryColumnName());
      buffer.append(" FROM ");
      buffer.append(getTableName());

      setSqlSelectAnyStatement(buffer.toString());
    }
    return getSqlSelectAnyStatement();
  }

  protected void createOidTable(Connection conn) throws SQLException
  {
    Statement statement;

    statement = conn.createStatement();
    statement.execute(sqlCreateOidTable());
    conn.commit();
    createRowForCategory(conn);
  }

  protected boolean createRowForCategory(Connection conn) throws SQLException
  {
    Statement statement;

    statement = conn.createStatement();
    statement.execute(sqlInsertCategoryRow(getCategory()));
    conn.commit();
    return true;
  }

  protected boolean checkTableExists(Connection conn)
  {
    try
    {
      return anyRowExists(conn, sqlSelectAny());
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
      return anyRowExists(conn, sqlSelectCategory());
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
    try
    {
      found = result.next();
    }
    finally
    {
      result.close();
    }
    return found;
  }

  protected void reportSQLException(SQLException ex, String msg, Object... args)
  {
    logger().errorf(ex, msg, args);
  }

  protected String getTableName()
  {
    if (getTableQualifier() == null)
    {
      return getUnqualifiedTableName();
    }
    return getTableQualifier() + "." + getUnqualifiedTableName();
  }

  protected Connection getDbConnection() throws SQLException
  {
    return getDataSource().getConnection();
  }

  protected void rollback(Connection conn)
  {
    try
    {
      conn.rollback();
    }
    catch (SQLException ex)
    {
      reportSQLException(ex, "Rollback of changes in table '%s' failed.", getTableName());
    }
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
        logger().warnf(ex, "Closing DB connection failed");
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
        logger().warnf(ex, "Closing SQL statement failed");
      }
    }
  }

  protected boolean hasTableQualifier()
  {
    return getTableQualifier() != null;
  }

  protected Logger2 logger()
  {
    return LoggerProvider.getLogger();
  }

  protected DataSource getDataSource()
  {
    return this.dataSource;
  }

  protected String getCategory()
  {
    return this.category;
  }

  protected void setCategory(String newValue)
  {
    this.category = newValue;
  }

  protected String getSqlSelectForUpdateStatement()
  {
    return this.sqlSelectForUpdateStatement;
  }

  protected void setSqlSelectForUpdateStatement(String newValue)
  {
    this.sqlSelectForUpdateStatement = newValue;
  }

  protected String getSqlSelectCategoryStatement()
  {
    return this.sqlSelectCategoryStatement;
  }

  protected void setSqlSelectCategoryStatement(String newValue)
  {
    this.sqlSelectCategoryStatement = newValue;
  }

  protected String getSqlSelectAnyStatement()
  {
    return this.sqlSelectAnyStatement;
  }

  protected void setSqlSelectAnyStatement(String newValue)
  {
    sqlSelectAnyStatement = newValue;
  }

  protected String getSqlUpdateStatement()
  {
    return this.sqlUpdateStatement;
  }

  protected void setSqlUpdateStatement(String newValue)
  {
    this.sqlUpdateStatement = newValue;
  }

  protected long getLastPrefetchedId()
  {
    return this.lastPrefetchedId;
  }

  protected void setLastPrefetchedId(long newValue)
  {
    this.lastPrefetchedId = newValue;
  }

  protected String getTableQualifier()
  {
    return getTableSpec().getTableQualifier();
  }
  
  protected void setTableQualifier(String qualifier)
  {
    getTableSpec().setTableQualifier(qualifier);
  }
  
  public String getUnqualifiedTableName()
  {
    return getTableSpec().getUnqualifiedTableName();
  }

  public void setUnqualifiedTableName(String unqualifiedTableName)
  {
    getTableSpec().setUnqualifiedTableName(unqualifiedTableName);
  }

  public String getCategoryColumnName()
  {
    return getTableSpec().getCategoryColumnName();
  }

  public void setCategoryColumnName(String categoryColumnName)
  {
    getTableSpec().setCategoryColumnName(categoryColumnName);
  }

  public String getNextIdColumnName()
  {
    return getTableSpec().getNextIdColumnName();
  }

  public void setNextIdColumnName(String nextIdColumnName)
  {
    getTableSpec().setNextIdColumnName(nextIdColumnName);
  }

  public String getBlockSizeColumnName()
  {
    return getTableSpec().getBlockSizeColumnName();
  }

  public void setBlockSizeColumnName(String blockSizeColumnName)
  {
    getTableSpec().setBlockSizeColumnName(blockSizeColumnName);
  }

  protected IdGeneratorTableSpec getTableSpec()
  {
    return this.tableSpec;
  }

  protected ObjectIdentifierDB setTableSpec(IdGeneratorTableSpec tableSpec)
  {
    this.tableSpec = tableSpec;
    return this;
  }
  
  @Override
  public String toString()
  {
    return String.format("%s('%s', '%s')", getClass().getSimpleName(), getTableName(), getCategory());
  }
}
