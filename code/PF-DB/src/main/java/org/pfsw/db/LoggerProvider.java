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

import org.pfsw.logging.Logger;
import org.pfsw.logging.Logger2;
import org.pfsw.logging.Logger2Logger;
import org.pfsw.logging.nil.NilLogger;
import org.pfsw.logging.stdout.PrintStreamLogger;

/**
 * This is the central access point for the package's logger.
 * Replacing the logger here means that all classes in this package
 * will use the new logger for further output.
 *
 * @author Manfred Duchrow
 * @version 2.0
 */
public class LoggerProvider
{
  // =========================================================================
  // CLASS VARIABLES
  // =========================================================================
  private static Logger2 logger = new Logger2Logger(new PrintStreamLogger());

  // =========================================================================
  // PUBLIC CLASS METHODS
  // =========================================================================
  /**
   * Returns the current logger used by this component to report
   * errors and exceptions. 
   */
  public static Logger2 getLogger()
  {
    return logger;
  }

  /**
   * Replace the logger by another one. A value of null installs
   * the prg.pf.logging.NilLogger.
   */
  public static void setLogger(Logger newLogger)
  {
    setLogger2(new Logger2Logger((newLogger == null) ? new NilLogger() : newLogger));
  }

  /**
   * Replace the logger by another one. A value of null installs
   * the prg.pf.logging.NilLogger.
   */
  public static void setLogger2(Logger2 newLogger)
  {
    if (newLogger == null)
    {
      logger = new Logger2Logger(new NilLogger());
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