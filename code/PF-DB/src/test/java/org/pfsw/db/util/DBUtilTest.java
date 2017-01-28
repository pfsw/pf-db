// ===========================================================================
// CONTENT  : TEST CLASS {ClassName}
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.0 - 15/10/2011
// HISTORY  :
//  15/10/2011  mdu  CREATED
//
// Copyright (c) 2011, by MDCS. All rights reserved.
// ===========================================================================
package org.pfsw.db.util ;

// ===========================================================================
// IMPORTS
// ===========================================================================
import org.pfsw.db.util.DBUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test class for corresponding business class.
 *
 * @author Manfred Duchrow
 * @version 1.0
 */
public class DBUtilTest extends TestCase
{
  // =========================================================================
  // CONSTANTS
  // =========================================================================
	private static final DBUtil DBU = DBUtil.current();

  // =========================================================================
  // INSTANCE VARIABLES
  // =========================================================================

  // =========================================================================
  // CONSTRUCTORS
  // =========================================================================
  /**
   * Initialize the new instance with default values.
   */
  public DBUtilTest( String name )
  {
    super( name ) ;
  } // DBUtilTest() 

  // =========================================================================
  // PUBLIC CLASS METHODS
  // =========================================================================
  public static Test suite()
  {
		return new TestSuite( DBUtilTest.class ) ;
	} // suite() 

  // =========================================================================
  // TEST METHODS
  // =========================================================================

  public void test_isDriverRegistered_UNKNOWN()
  {
  	assertFalse(DBU.isDriverRegistered(null));
  	assertFalse(DBU.isDriverRegistered("dummy.jdbc.driver.UnknownDriver"));
  } // test_isDriverRegistered_UNKNOWN() 
  
  // -------------------------------------------------------------------------
  
  public void test_loadAndRegisterDriver_UNKNOWN()
  {
  	assertFalse(DBU.loadAndRegisterDriver(null));
  	assertFalse(DBU.loadAndRegisterDriver("dummy.jdbc.driver.UnknownDriver"));
  } // test_loadAndRegisterDriver_UNKNOWN() 
  
  // -------------------------------------------------------------------------
  
  public void test_loadAndRegisterDriver_ODBC()
  {
  	assertTrue(DBU.loadAndRegisterDriver("sun.jdbc.odbc.JdbcOdbcDriver"));
  } // test_loadAndRegisterDriver_ODBC() 

  // -------------------------------------------------------------------------

  // =========================================================================
  // PROTECTED INSTANCE METHODS
  // =========================================================================
  @Override
  protected void setUp() throws Exception
  {
    super.setUp() ;
	} // setUp() 

  // -------------------------------------------------------------------------

} // class DBUtilTest 
