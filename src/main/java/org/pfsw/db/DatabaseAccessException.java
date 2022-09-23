// ===========================================================================
// CONTENT  : CLASS DatabaseAccessException
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.0 - 13/04/2020
// HISTORY  :
//  13/04/2020  mdu  CREATED
//
// Copyright (c) 2020, by MDCS. All rights reserved.
// ===========================================================================
package org.pfsw.db;

/**
 * A runtime exception for various database problems.
 *
 * @author Manfred Duchrow
 * @version 1.0
 */
public class DatabaseAccessException extends RuntimeException
{
  private static final long serialVersionUID = -4893792774632796945L;

  public DatabaseAccessException(String message, Object... args)
  {
    super(String.format(message, args));
  }

  public DatabaseAccessException(Throwable cause, String message, Object... args)
  {
    super(String.format(message, args), cause);
  }
}
