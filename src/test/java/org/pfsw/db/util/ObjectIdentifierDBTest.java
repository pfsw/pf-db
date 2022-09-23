package org.pfsw.db.util;

import static org.junit.Assert.*;
import static org.pfsw.db.util.testhelper.UnitTestHelper.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.sql.DataSource;

import org.junit.Test;
import org.pfsw.db.DatabaseAccessException;
import org.pfsw.text.ObjectIdGenerator;
import org.pfsw.text.StringUtil;

public class ObjectIdentifierDBTest
{
  @Test
  public void test_toString()
  {
    ObjectIdGenerator idGen = ObjectIdentifierDBBuilder.create(createNewDatabase1()).setCategory("Epsilon").build();

    assertEquals("ObjectIdentifierDB('OIDADMIN', 'Epsilon')", idGen.toString());
  }

  @Test
  public void test_nextIdentifier__two_different_categories()
  {
    ObjectIdGenerator idGen1 = createIdGenerator1();
    ObjectIdGenerator idGen2 = createIdGenerator2();

    assertEquals("A:0000000500", idGen1.newIdentifier());
    assertEquals("0001000", idGen2.newIdentifier());
  }

  @Test
  public void test_nextIdentifier__thousands()
  {
    ObjectIdGenerator idGen1 = createIdGenerator1(77);
    String expected;
    long start = System.currentTimeMillis();

    for (int i = 500; i < 4500; i++)
    {
      expected = "A:" + StringUtil.SU.leftPad(i, 10);
      assertEquals(expected, idGen1.newIdentifier());
    }
    debug("Generation of 4000 IDs took %d ms", System.currentTimeMillis() - start);
  }

  @Test
  public void test_nextIdentifier__parallel_with_same_generator()
  {
    Collection<String> ids = Collections.synchronizedCollection(new ArrayList<String>());
    ObjectIdGenerator idGen = createIdGenerator2();
    Thread thread1, thread2, thread3;

    thread1 = new Thread(createRunnable(idGen, ids), "thread-a1");
    thread2 = new Thread(createRunnable(idGen, ids), "thread-a2");
    thread3 = new Thread(createRunnable(idGen, ids), "thread-a3");
    thread1.start();
    thread2.start();
    thread3.start();
    sleep(500);
    assertEquals(9, ids.size());
    assertTrue(ids.contains("0001000"));
    assertTrue(ids.contains("0001001"));
    assertTrue(ids.contains("0001002"));

    assertTrue(ids.contains("0001003"));
    assertTrue(ids.contains("0001004"));
    assertTrue(ids.contains("0001005"));

    assertTrue(ids.contains("0001006"));
    assertTrue(ids.contains("0001007"));
    assertTrue(ids.contains("0001008"));
  }

  @Test
  public void test_nextIdentifier__parallel_with_separate_generator()
  {
    Collection<String> ids = Collections.synchronizedCollection(new ArrayList<String>());
    Thread thread1, thread2, thread3;

    assertTrue(createIdGenerator2().isTableCreated());

    thread1 = new Thread(createRunnable(null, ids), "thread-b1");
    thread2 = new Thread(createRunnable(null, ids), "thread-b2");
    thread3 = new Thread(createRunnable(null, ids), "thread-b3");
    thread1.start();
    thread2.start();
    thread3.start();
    sleep(500);
    assertEquals(9, ids.size());
    assertTrue(ids.contains("0001000"));
    assertTrue(ids.contains("0001001"));
    assertTrue(ids.contains("0001002"));

    assertTrue(ids.contains("0001005"));
    assertTrue(ids.contains("0001006"));
    assertTrue(ids.contains("0001007"));

    assertTrue(ids.contains("0001010"));
    assertTrue(ids.contains("0001011"));
    assertTrue(ids.contains("0001012"));
  }

  @Test
  public void test_nextIdentifier__custom_table()
  {
    ObjectIdGenerator idGen = createIdGenerator3();

    assertEquals("98", idGen.newIdentifier());
    assertEquals("99", idGen.newIdentifier());
    assertEquals("100", idGen.newIdentifier());
    assertEquals("101", idGen.newIdentifier());
  }

  @Test(expected = DatabaseAccessException.class)
  public void test_nextIdentifier__missing_table()
  {
    ObjectIdGenerator idGen = createIdGenerator2(true);

    idGen.newIdentifier();
  }

  // ======================== helper methods =================================

  private Runnable createRunnable(final ObjectIdGenerator idGenerator, final Collection<String> ids)
  {
    final ObjectIdGenerator idGen;

    idGen = (idGenerator == null) ? createIdGenerator2(true) : idGenerator;

    return new Runnable()
    {
      @Override
      public void run()
      {
        for (int i = 1; i <= 3; i++)
        {
          String id = idGen.newIdentifier();
          ids.add(id);
          debug("[%s] added '%s'", Thread.currentThread().getName(), id);
          sleep(10L);
        }
      }
    };
  }

  private ObjectIdentifierDB createIdGenerator1()
  {
    return createIdGenerator1(3);
  }

  private ObjectIdentifierDB createIdGenerator1(int blockSize)
  {
    //@formatter:off
    return ObjectIdentifierDBBuilder.create(createNewDatabase1())
        .setCategory("Alpha")
        .setPrefix("A:")
        .setStartId(500)
        .setLength(10)
        .setBlockSize(blockSize)
        .build();
    //@formatter:on
  }

  private ObjectIdentifierDB createIdGenerator2()
  {
    return createIdGenerator2(false);
  }

  private ObjectIdentifierDB createIdGenerator2(boolean tableExists)
  {
    ObjectIdentifierDBBuilder builder;
    DataSource dataSource;

    dataSource = tableExists ? createDataSource1() : createNewDatabase1();

    //@formatter:off
    builder = ObjectIdentifierDBBuilder.create(dataSource)
              .setCategory("Beta")
              .setStartId(1000)
              .setLength(7)
              .setBlockSize(5);
    //@formatter:on
    if (tableExists)
    {
      builder.tableAlreadyCreated();
    }
    return builder.build();
  }

  // Different table
  private ObjectIdentifierDB createIdGenerator3()
  {
    //@formatter:off
    return ObjectIdentifierDBBuilder.create(createNewDatabase1())
        .setCategory("Omega")
        .setStartId(98)
        .setBlockSize(10)
        .noPadding()
        .setTableName("pf_db_id_sequence")
        .setCategoryColumn("object_type")
        .setNextIdColumn("next_id")
        .setBlockSizeColumn("block_size")
        .build();
    //@formatter:on
  }
}