// ===========================================================================
// CONTENT  : CLASS DataSourceProxy
// AUTHOR   : Manfred Duchrow
// VERSION  : 2.0 - 21/06/2014
// HISTORY  :
//  18/08/2001  duma  CREATED
//  02/12/2001  duma  moved from com.mdcs.db.util
//	22/02/2008	mdu		added	-->	setDriver()
//  21/06/2104  mdu   added --> methods of interface Wrapper
//
// Copyright (c) 2001-2014, by Manfred Duchrow. All rights reserved.
// ===========================================================================
package org.pfsw.db.util;

// ===========================================================================
// IMPORTS
// ===========================================================================
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * This class is a datasource wrapper for other datasources or simple
 * connections. It allows to pass simple connection information such
 * as url, userid, password as a datasource to other objects that require
 * a javax.sql.DataSource rather than java.sql.Connection as input.
 * In addition the wrapping of other datasource objects plus their associated
 * userid/password data is supported. That allows to pass around just one object
 * that can be asked to return a connection via getConnection().
 *
 * @author Manfred Duchrow
 * @version 2.0
 */
public class DataSourceProxy implements DataSource
{
  // =========================================================================
  // CONSTANTS
  // =========================================================================

  // =========================================================================
  // INSTANCE VARIABLES
  // =========================================================================
  private String dbUrl = null;
  private String dbUserid = null;
  private String dbPassword = null;
  private Connection dbConnection = null;
  private DataSource dataSource = null;

  // =========================================================================
  // CONSTRUCTORS
  // =========================================================================
  /**
   * Initialize the new instance with a ready-to-use connection.
   */
  public DataSourceProxy(Connection conn)
  {
    super();
    this.setDbConnection(conn);
  }

  /**
   * Initialize the new instance with all necessary connection information.
   */
  public DataSourceProxy(String url, String username, String password)
  {
    super();
    this.setDbUrl(url);
    this.setDbUserid(username);
    this.setDbPassword(password);
  }

  /**
   * Initialize the new instance with an URL for the database connection.
   */
  public DataSourceProxy(String url)
  {
    this(url, null, null);
  }

  public DataSourceProxy(DataSource dataSource, String username, String password)
  {
    super();
    this.setDataSource(dataSource);
    this.setDbUserid(username);
    this.setDbPassword(password);
  }

  public DataSourceProxy(DataSource dataSource)
  {
    this(dataSource, null, null);
  }

  // =========================================================================
  // PUBLIC INSTANCE METHODS
  // =========================================================================
  /**
   * Attempt to establish a database connection
   */
  @Override
  public Connection getConnection() throws SQLException
  {
    if (this.hasConnection())
    {
      return this.getDbConnection();
    }

    if (this.hasDataSource())
    {
      return this.getDataSourceConnection();
    }

    if ((this.getDbUserid() == null) || (this.getDbPassword() == null))
    {
      return DriverManager.getConnection(this.getDbUrl());
    }
    return DriverManager.getConnection(this.getDbUrl(), this.getDbUserid(), this.getDbPassword());
  }

  /**
   * Attempt to establish a database connection
   */
  @Override
  public Connection getConnection(String username, String password) throws SQLException
  {
    if (this.hasDataSource())
    {
      return this.getDataSource().getConnection(username, password);
    }
    return DriverManager.getConnection(this.getDbUrl(), username, password);
  }

  /**
   * Always returns false.
   */
  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException
  {
    return false;
  }

  /**
   * Always throws SQLException, becaus eno interfaces are supported.
   */
  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException
  {
    throw new SQLException("Interface " + iface + " not supported!");
  }

  public String getDbUrl()
  {
    return this.dbUrl;
  }

  public void setDbUrl(String url)
  {
    this.dbUrl = url;
  }

  public String getDbUserid()
  {
    return this.dbUserid;
  }

  public void setDbUserid(String userId)
  {
    this.dbUserid = userId;
  }

  public String getDbPassword()
  {
    return this.dbPassword;
  }

  public void setDbPassword(String password)
  {
    this.dbPassword = password;
  }

  /**
   * Returns the maximum time in seconds that this
   * data source can wait while attempting to connect
   * to a database.
   */
  @Override
  public int getLoginTimeout() throws SQLException
  {
    if (this.hasDataSource())
    {
      return this.getDataSource().getLoginTimeout();
    }
    return DriverManager.getLoginTimeout();
  }

  /**
   * Returns the log writer for this data source
   */
  @Override
  public PrintWriter getLogWriter() throws SQLException
  {
    if (this.hasDataSource())
    {
      return this.getDataSource().getLogWriter();
    }
    return DriverManager.getLogWriter();
  }

  /**
   * Sets the maximum time in seconds that this
   * data source can wait while attempting to connect
   * to a database.
   */
  @Override
  public void setLoginTimeout(int timeout) throws SQLException
  {
    if (this.hasDataSource())
    {
      this.getDataSource().setLoginTimeout(timeout);
    }
    else
    {
      DriverManager.setLoginTimeout(timeout);
    }
  }

  /**
   * Sets the log writer for this data source
   */
  @Override
  public void setLogWriter(PrintWriter writer) throws SQLException
  {
    if (this.hasDataSource())
    {
      this.getDataSource().setLogWriter(writer);
    }
    else
    {
      DriverManager.setLogWriter(writer);
    }
  }

  /**
   * Creates an instance of the given class name in order to register the 
   * database driver.
   */
  public void setDriverClassName(String driverClassName)
  {
    try
    {
      Class.forName(driverClassName).newInstance();
    }
    catch (Exception e)
    {
      DriverManager.println(e.toString());
    }
  }

  // =========================================================================
  // PROTECTED INSTANCE METHODS
  // =========================================================================
  protected boolean hasDataSource()
  {
    return this.getDataSource() != null;
  }

  protected boolean hasConnection()
  {
    return this.getDbConnection() != null;
  }

  /**
   * Attempt to establish a database connection using the wrapped datasource
   */
  protected Connection getDataSourceConnection() throws SQLException
  {
    if ((this.getDbUserid() == null) || (this.getDbPassword() == null))
    {
      return this.getDataSource().getConnection();
    }
    return this.getDataSource().getConnection(this.getDbUserid(), this.getDbPassword());
  }

  protected Connection getDbConnection()
  {
    return this.dbConnection;
  }

  protected void setDbConnection(Connection newValue)
  {
    this.dbConnection = newValue;
  }

  protected DataSource getDataSource()
  {
    return this.dataSource;
  }

  protected void setDataSource(DataSource newValue)
  {
    this.dataSource = newValue;
  }

}
