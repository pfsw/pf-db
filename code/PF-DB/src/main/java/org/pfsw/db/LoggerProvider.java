// ===========================================================================
// CONTENT  : CLASS LoggerProvider
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.0 - 22/12/2003
// HISTORY  :
//  22/12/2003  duma  CREATED
//
// Copyright (c) 2003, by Manfred Duchrow. All rights reserved.
// ===========================================================================
package org.pfsw.db;

// ===========================================================================
// IMPORTS
// ===========================================================================
import org.pfsw.logging.Logger;
import org.pfsw.logging.nil.NilLogger;
import org.pfsw.logging.stdout.PrintStreamLogger;

/**
 * This is the central access point for the package's logger.
 * Replacing the logger here means that all classes in this package
 * will use the new logger for further output.
 *
 * @author Manfred Duchrow
 * @version 1.0
 */
public class LoggerProvider
{
  // =========================================================================
  // CLASS VARIABLES
  // =========================================================================
  private static Logger logger = new PrintStreamLogger();

  // =========================================================================
  // PUBLIC CLASS METHODS
  // =========================================================================
  /**
   * Returns the current logger used by this component to report
   * errors and exceptions. 
   */
  public static Logger getLogger()
  {
    return logger;
  }

  /**
   * Replace the logger by another one. A value of null installs
   * the prg.pf.logging.NilLogger.
   */
  public static void setLogger(Logger newLogger)
  {
    if (newLogger == null)
    {
      logger = new NilLogger();
    }
    else
    {
      logger = newLogger;
    }
  }

  // =========================================================================
  // CONSTRUCTORS
  // =========================================================================
  /**
   * Initialize the new instance with default values.
   */
  private LoggerProvider()
  {
    super();
  }

}