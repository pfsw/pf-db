// ===========================================================================
// CONTENT  : CLASS SQLExecutor
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.1 - 17/07/2002
// HISTORY  :
//  03/07/2002  duma  CREATED
//	17/07/2002	duma	added		->	prepareWriteStatement(), executeWriteStatement()
//
// Copyright (c) 2002, by Manfred Duchrow. All rights reserved.
// ===========================================================================
package org.pfsw.db.util;

// ===========================================================================
// IMPORTS
// ===========================================================================
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

/**
 * Provides an easy to use interface to execute SQL statements against
 * a database.
 *
 * @author Manfred Duchrow
 * @version 1.1
 */
public class SQLExecutor
{
  // =========================================================================
  // CONSTANTS
  // =========================================================================
  private static final boolean DEBUG = "true".equals(System.getProperty("org.pfsw.db.debug", "false"));

  // =========================================================================
  // INSTANCE VARIABLES
  // =========================================================================
  private DataSource dataSource = null;
  private Connection connection = null;

  // =========================================================================
  // CONSTRUCTORS
  // =========================================================================
  /**
   * Initialize the new instance with default values.
   * 
   * @param aDataSource The datasource the executor should connect to (must not be null)
   */
  public SQLExecutor(DataSource aDataSource)
  {
    super();
    if (aDataSource == null)
    {
      throw new IllegalArgumentException("Datasource in SQLExecutor constructor is null");
    }

    this.setDataSource(aDataSource);
  }

  // =========================================================================
  // PUBLIC INSTANCE METHODS
  // =========================================================================
  /**
   * Returns a PreparedStatement for the given SQL command.
   * 
   * @param sql A valid SQL statement with placeholders (?) (no SELECT allowed here!)
   * @throws SQLException Any problem that occurs during execution
   */
  public PreparedStatement prepareWriteStatement(String sql) throws SQLException
  {
    return this.getConnection().prepareStatement(sql);
  }

  /**
   * Executes the given statement and returns the number of affected rows.
   * 
   * @param statement A valid statement, created before by this executor
   * @throws SQLException Any problem that occurs during execution
   */
  public int executeWriteStatement(PreparedStatement statement) throws SQLException
  {
    Connection conn;

    conn = this.getConnection();
    if (conn != null)
    {
      return statement.executeUpdate();
    }
    return 0;
  }

  /**
   * Executes the given SQL command and returns the number of affected rows.
   * 
   * @param sql A valid SQL statement (no SELECT allowed here!)
   * @throws SQLException Any problem that occurs during execution
   */
  public int execute(String sql) throws SQLException
  {
    Statement statement;
    Connection conn;

    conn = this.getConnection();
    if (conn != null)
    {
      statement = conn.createStatement();
      return statement.executeUpdate(sql);
    }
    return 0;
  }

  /**
   * Executes the given SQL command and returns true if the execution was 
   * successful.
   * 
   * @param sql A valid SQL statement (no SELECT allowed here!)
   */
  public boolean executeSQL(String sql)
  {
    try
    {
      this.execute(sql);
      return true;
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

  /**
   * Closes all open connections.
   * Connections will be reopened automatically if necessary.
   */
  public boolean close()
  {
    if (!isClosed())
    {
      try
      {
        this.connection().close();
      }
      catch (SQLException e)
      {
        if (DEBUG)
        {
          e.printStackTrace();
        }
        return false;
      }
      this.connection(null);
      return true;
    }
    return true;
  }

  /**
   * Closes the given statement.
   * @return true if it is closed.
   */
  public boolean closeStatement(Statement statement)
  {
    if (statement != null)
    {
      try
      {
        statement.close();
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
    return true;
  }

  // =========================================================================
  // PROTECTED INSTANCE METHODS
  // =========================================================================
  protected Connection getConnection() throws SQLException
  {
    if (this.isClosed())
    {
      this.connection(this.newConnection());
    }
    return this.connection();
  }

  protected Connection newConnection() throws SQLException
  {
    return this.getDataSource().getConnection();
  }

  protected boolean isClosed()
  {
    return (this.connection() == null);
  }

  protected DataSource getDataSource()
  {
    return this.dataSource;
  }

  protected void setDataSource(DataSource newValue)
  {
    this.dataSource = newValue;
  }

  protected Connection connection()
  {
    return this.connection;
  }

  protected void connection(Connection newValue)
  {
    this.connection = newValue;
  }

}