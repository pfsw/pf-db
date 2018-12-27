// ===========================================================================
// CONTENT  : TEST CLASS {ClassName}
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.0 - 15/10/2011
// HISTORY  :
//  15/10/2011  mdu  CREATED
//
// Copyright (c) 2011, by MDCS. All rights reserved.
// ===========================================================================
package org.pfsw.db.util;

// ===========================================================================
// IMPORTS
// ===========================================================================
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test class for corresponding business class.
 *
 * @author Manfred Duchrow
 * @version 1.0
 */
public class DBUtilTest
{
  // =========================================================================
  // CONSTANTS
  // =========================================================================
  private static final DBUtil DBU = DBUtil.current();

  // =========================================================================
  // TEST METHODS
  // =========================================================================
  @Test
  public void test_isDriverRegistered_UNKNOWN()
  {
    assertFalse(DBU.isDriverRegistered(null));
    assertFalse(DBU.isDriverRegistered("dummy.jdbc.driver.UnknownDriver"));
  }

  @Test
  public void test_loadAndRegisterDriver_UNKNOWN()
  {
    assertFalse(DBU.loadAndRegisterDriver(null));
    assertFalse(DBU.loadAndRegisterDriver("dummy.jdbc.driver.UnknownDriver"));
  }

  @Test
  public void test_loadAndRegisterDriver_H2()
  {
    assertTrue(DBU.loadAndRegisterDriver("org.h2.Driver"));
  }
}
