// ===========================================================================
// CONTENT  : CLASS DBUtil
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.0 - 15/10/2011
// HISTORY  :
//  15/10/2011  mdu  CREATED
//
// Copyright (c) 2011, by MDCS. All rights reserved.
// ===========================================================================
package org.pfsw.db.util;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

/**
 * Helper and convenience methods for Java database handling.
 *
 * @author Manfred Duchrow
 * @version 1.0
 */
public class DBUtil
{
  // =========================================================================
  // CONSTANTS
  // =========================================================================
  private static final boolean DEBUG = "true".equals(System.getProperty("org.pfsw.db.debug", "false"));
  
  // =========================================================================
  // CLASS VARIABLES
  // =========================================================================
  private static DBUtil soleInstance = new DBUtil();

  // =========================================================================
  // CLASS METHODS
  // =========================================================================

  /**
   * Returns the only instance this class supports (design pattern "Singleton")
   */
  public static DBUtil current()
  {
    return soleInstance;
  }

  // =========================================================================
  // CONSTRUCTORS
  // =========================================================================
  protected DBUtil()
  {
    super();
  }

  // =========================================================================
  // PUBLIC INSTANCE METHODS
  // =========================================================================
  /**
   * Loads the database driver with the given class name and registers it
   * at the java.sql.DriverManager.
   * 
   * @param driverClassName The class name of the driver to load
   * @return true if loading was successful, otherwise false
   */
  public boolean loadAndRegisterDriver(final String driverClassName)
  {
    if (driverClassName == null)
    {
      return false;
    }
    try
    {
      getClass().forName(driverClassName);
      return isDriverRegistered(driverClassName);
    }
    catch (ClassNotFoundException ex)
    {
      if (DEBUG)
      {
        ex.printStackTrace();
      }
      return false;
    }
  }

  /**
   * Returns true if the database driver with the specified class name is
   * registered at the java.sql.DriverManager.
   * 
   * @param driverClassName The class name of the driver to load
   */
  public boolean isDriverRegistered(final String driverClassName)
  {
    Enumeration<Driver> drivers;
    Driver driver;

    if (driverClassName == null)
    {
      return false;
    }
    drivers = DriverManager.getDrivers();
    while (drivers.hasMoreElements())
    {
      driver = drivers.nextElement();
      if (driverClassName.equals(driver.getClass().getName()))
      {
        return true;
      }
    }
    return false;
  }
}
