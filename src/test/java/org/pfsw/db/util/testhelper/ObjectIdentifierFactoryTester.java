// ===========================================================================
// CONTENT  : CLASS ObjectIdentifierFactoryTester
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.2 - 29/01/2002
// HISTORY  :
//  05/01/2001  duma  CREATED
//  02/12/2001  duma  moved from com.mdcs.db.util
//	29/01/2002	duma	changed	-> Don't keep connection open anymore
//
// Copyright (c) 2001-2002, by Manfred Duchrow. All rights reserved.
// ===========================================================================
package org.pfsw.db.util.testhelper;

// ===========================================================================
// IMPORTS
// ===========================================================================
import java.io.PrintStream;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.pfsw.db.util.DataSourceProxy;
import org.pfsw.db.util.ObjectIdentifierDB;
import org.pfsw.db.util.ObjectIdentifierGenerator;
import org.pfsw.db.util.ObjectIdentifierProducer;

/**
 * Testing the object identifier producer
 *
 * @author Manfred Duchrow
 * @version 1.1
 */
public class ObjectIdentifierFactoryTester
{
  // =========================================================================
  // CONSTANTS
  // =========================================================================
  protected static final PrintStream OUT = System.out;
  private static final String DATABASE_NAME = "jdbc:idb:X:\\InstantDB\\3.21\\Databases\\Test\\test.prp";
  private static final String JDBC_DRIVER = "org.enhydra.instantdb.jdbc.idbDriver";

  // =========================================================================
  // CLASS METHODS
  // =========================================================================

  public static void main(String[] args)
  {
    ObjectIdentifierFactoryTester inst = new ObjectIdentifierFactoryTester();

    inst.run();
    System.exit(0);
  }

  // =========================================================================
  // CONSTRUCTORS
  // =========================================================================
  /**
   * Initialize the new instance with default values.
   */
  public ObjectIdentifierFactoryTester()
  {
    super();
  }

  // =========================================================================
  // PROTECTED INSTANCE METHODS
  // =========================================================================

  protected void run()
  {
    System.out.println("Max-long: " + Long.MAX_VALUE);
    System.out.println("Min-long: " + Long.MIN_VALUE);
    test1();
    test2();
  }

  protected void test1()
  {
    ObjectIdentifierProducer idFactory = new ObjectIdentifierGenerator();
    OUT.println("TEST 1\n------\n");
    OUT.println();
    this.generateIds(idFactory);
  }

  protected void test2()
  {
    ObjectIdentifierDB idFactory = null;

    idFactory = new ObjectIdentifierDB(this.createDataSource(), "TEST1");
    idFactory.setBlockSize(15);
    OUT.println("TEST 2\n------\n");
    this.generateIds(idFactory);
    OUT.println();
  }

  protected void generateIds(ObjectIdentifierProducer idFactory)
  {
    for (int i = 1; i <= 20; i++)
    {
      OUT.println("ID = " + idFactory.newIdentifier());
    }
  }

  protected void reportSQLException(String msg, SQLException ex)
  {
    System.err.println("==============================");
    System.err.println(msg);
    System.err.println(ex.toString());
    ex.printStackTrace(System.err);
    System.err.println("==============================");
  }

  protected DataSource createDataSource()
  {
    DataSourceProxy ds;

    ds = new DataSourceProxy(DATABASE_NAME);
    ds.setDriverClassName(JDBC_DRIVER);
    return ds;
  }

}