// ===========================================================================
// CONTENT  : TEST CLASS ObjectIdentifierGeneratorTest
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.0 - 26/07/2003
// HISTORY  :
//  26/07/2003  duma  CREATED
//
// Copyright (c) 2003, by Manfred Duchrow. All rights reserved.
// ===========================================================================
package org.pfsw.db.util;

// ===========================================================================
// IMPORTS
// ===========================================================================
import static org.junit.Assert.*;

/**
 * Test class for corresponding business class.
 *
 * @author Manfred Duchrow
 * @version 1.0
 */
public class ObjectIdentifierGeneratorTest
{
  // =========================================================================
  // TEST METHODS
  // =========================================================================

  public void test_newIdentifier_1()
  {
    ObjectIdentifierGenerator idGen;

    idGen = new ObjectIdentifierGenerator(3);

    assertEquals("001", idGen.newIdentifier());
    assertEquals("002", idGen.newIdentifier());
    assertEquals("003", idGen.newIdentifier());
    assertEquals("004", idGen.newIdentifier());
  }

  public void test_newIdentifier_2()
  {
    ObjectIdentifierGenerator idGen;

    idGen = new ObjectIdentifierGenerator(1);

    assertEquals("1", idGen.newIdentifier());
    assertEquals("2", idGen.newIdentifier());
    assertEquals("3", idGen.newIdentifier());
    assertEquals("4", idGen.newIdentifier());
  }

  public void test_newIdentifier_3()
  {
    ObjectIdentifierGenerator idGen;

    idGen = new ObjectIdentifierGenerator(500, 4);

    assertEquals("0500", idGen.newIdentifier());
    assertEquals("0501", idGen.newIdentifier());
    assertEquals("0502", idGen.newIdentifier());
    assertEquals("0503", idGen.newIdentifier());
  }

  public void test_newIdentifier_4()
  {
    ObjectIdentifierGenerator idGen;

    idGen = new ObjectIdentifierGenerator(200, 2);

    assertEquals("200", idGen.newIdentifier());
    assertEquals("201", idGen.newIdentifier());
    assertEquals("202", idGen.newIdentifier());
    assertEquals("203", idGen.newIdentifier());
  }
}