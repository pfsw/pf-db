package org.pfsw.db.util;

import static org.junit.Assert.*;
import static org.pfsw.db.util.testhelper.CategorySpec.*;
import static org.pfsw.db.util.testhelper.UnitTestHelper.*;
import static org.pfsw.text.StringUtil.*;

import javax.sql.DataSource;

import org.junit.Test;
import org.pfsw.bif.identifier.IdentifierGenerationException;
import org.pfsw.db.DatabaseAccessException;

public class DefaultMultiCategoryIdentifierGeneratorTest
{
  private static final long START_ID_1 = 1000L;
  private static final long START_ID_2 = 2000L;
  private static final long START_ID_3 = 9998;

  @Test
  public void test_nextIdentifier__default_table()
  {
    assertNextIdentifier(createGenerator1());
  }

  @Test
  public void test_newIdentifier__default_table()
  {
    assertNewIdentifier(createGenerator1());
  }

  @Test
  public void test_nextIdentifier__other_table()
  {
    assertNextIdentifier(createGenerator2());
  }
  
  @Test
  public void test_newIdentifier__other_table()
  {
    assertNewIdentifier(createGenerator2());
  }
  
  @Test
  public void test_nextIdentifier__pre_created_table()
  {
    assertNextIdentifier(createGenerator3());
  }
  
  @Test
  public void test_newIdentifier__pre_created_table()
  {
    assertNewIdentifier(createGenerator3());
  }
  
  @Test
  public void test_nextIdentifier__not_existing_table()
  {
    try
    {
      createGenerator4().nextIdentifier("Beta");
      fail("Expected IdentifierGenerationException");
    }
    catch (RuntimeException e)
    {
      assertTrue(e instanceof IdentifierGenerationException);
      assertTrue(e.getCause() instanceof DatabaseAccessException);
    }
  }
  
  @Test
  public void test_newIdentifier__not_existing_table()
  {
    try
    {
      createGenerator4().newIdentifier("Alpha");
      fail("Expected IdentifierGenerationException");
    }
    catch (RuntimeException e)
    {
      assertTrue(e instanceof IdentifierGenerationException);
      assertTrue(e.getCause() instanceof DatabaseAccessException);
    }
  }
  
  // ===================== helper methods ==================================

  private void assertNextIdentifier(final MultiCategoryIdentifierGenerator generator)
  {
    for (int i = 0; i < 10; i++)
    {
      assertEquals(START_ID_1 + i, generator.nextIdentifier("Alpha"));
      assertEquals(START_ID_2 + i, generator.nextIdentifier("Beta"));
      assertEquals(START_ID_3 + i, generator.nextIdentifier("Gamma"));
      assertEquals(1L + i, generator.nextIdentifier("Delta"));
      assertEquals(1L + i, generator.nextIdentifier("Kappa"));
      assertEquals(OMEGA.getStartId() + i, generator.nextIdentifier(OMEGA.getCategoryName()));
      assertEquals(EPSILON.getStartId() + i, generator.nextIdentifier(EPSILON.getCategoryName()));
    }
  }

  private void assertNewIdentifier(final MultiCategoryIdentifierGenerator generator)
  {
    for (int i = 0; i < 10; i++)
    {
      assertEquals(Long.toString(START_ID_1 + i), generator.newIdentifier("Alpha"));
      assertEquals(Long.toString(START_ID_2 + i), generator.newIdentifier("Beta"));
      assertEquals(SU.leftPadCh(Long.toString(START_ID_3 + i), 6, '0'), generator.newIdentifier("Gamma"));
      assertEquals(Long.toString(1L + i), generator.newIdentifier("Delta"));
      assertEquals(Long.toString(1L + i), generator.newIdentifier("Kappa"));
      assertEquals(SU.leftPadCh(OMEGA.getStartId() + i, OMEGA.getLength(), OMEGA.getPaddingChar()), generator.newIdentifier(OMEGA.getCategoryName()));
      assertEquals(SU.leftPadCh(EPSILON.getStartId() + i, EPSILON.getLength(), EPSILON.getPaddingChar()), generator.newIdentifier(EPSILON.getCategoryName()));
    }
  }

  private MultiCategoryIdentifierGenerator createGenerator1()
  {
    return createGeneratorBuilder().build();
  }

  private MultiCategoryIdentifierGenerator createGenerator2()
  {
    //@formatter:off
    return createGeneratorBuilder()
        .tableSpec(createTableSpec())
        .build()
    ;
    //@formatter:on
  }
  
  private MultiCategoryIdentifierGenerator createGenerator3()
  {
    DataSource dataSource = createNewDatabase1();
    IdGeneratorTableSpec tableSpec = createTableSpec();
    SQLExecutor sqlExecutor = new SQLExecutor(dataSource);
    String sql;
    
    //@formatter:off
    sql = String.format("CREATE TABLE %s (%s VARCHAR(80) NOT NULL, %s BIGINT NOT NULL, %s INT NOT NULL)", 
        tableSpec.getUnqualifiedTableName(), tableSpec.getCategoryColumnName(), tableSpec.getNextIdColumnName(), tableSpec.getBlockSizeColumnName());
    try
    {
      sqlExecutor.executeSQL(sql);      
    }
    finally
    {
      sqlExecutor.commit();
      sqlExecutor.close();
    }
    
    return createGeneratorBuilder(dataSource)
        .tableSpec(tableSpec)
        .tableAlreadyCreated()
        .build()
    ;
    //@formatter:on
  }
  
  private MultiCategoryIdentifierGenerator createGenerator4()
  {
    //@formatter:off
    return createGeneratorBuilder()
        .tableSpec(createTableSpec())
        .tableAlreadyCreated() // this should cause an exception later when using the generator
        .build()
    ;
    //@formatter:on
  }
  
  private DBMultiCategoryIdGeneratorBuilder createGeneratorBuilder()
  {
    return createGeneratorBuilder(createNewDatabase1());
  }
  
  private DBMultiCategoryIdGeneratorBuilder createGeneratorBuilder(DataSource  dataSource)
  {
    //@formatter:off
    return DBMultiCategoryIdGeneratorBuilder.create(dataSource)
        .add("Alpha", START_ID_1, 1)
        .add("Beta", START_ID_2)
        .add("Gamma", START_ID_3, 100, 6)
        .addCategories("Delta", null, "Kappa", " ", "alpha") // null, " " and "alpha" must be skipped
        .addCategories(OMEGA, EPSILON)
    ;
    //@formatter:on
  }
  
  private IdGeneratorTableSpec createTableSpec() 
  {
    //@formatter:off
    return IdGeneratorTableSpec.create("ID_SEQUENCER")
        .setCategoryColumnName("SEGMENT")
        .setNextIdColumnName("NEXT_BLOCK_START")
        .setBlockSizeColumnName("CHUNK")
    ;
    //@formatter:on
  }
}