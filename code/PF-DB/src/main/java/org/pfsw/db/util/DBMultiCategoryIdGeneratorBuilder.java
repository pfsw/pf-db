// ===========================================================================
// CONTENT  : CLASS DBMultiCategoryIdGeneratorBuilder
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.0 - 18/04/2020
// HISTORY  :
//  18/04/2020  mdu  CREATED
//
// Copyright (c) 2020, by MDCS. All rights reserved.
// ===========================================================================
package org.pfsw.db.util;

import static org.pfsw.text.StringUtil.*;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

/**
 * A builder supporting fluent API to create a database backed multi-category
 * identifier generator where the generators for the different categories all
 * share the same database table.
 *
 * @author Manfred Duchrow
 * @version 1.0
 */
public class DBMultiCategoryIdGeneratorBuilder
{
  private final DataSource dataSource;
  private final List<IdGeneratorCategorySpec> categorySpecs = new ArrayList<IdGeneratorCategorySpec>();
  private final DefaultMultiCategoryIdentifierGenerator multiCategoryIdGenerator = new DefaultMultiCategoryIdentifierGenerator();
  private IdGeneratorTableSpec tableSpec = IdGeneratorTableSpec.create();
  private boolean isTableAlreadyCreated = false;

  public static DBMultiCategoryIdGeneratorBuilder create(DataSource dataSource)
  {
    return new DBMultiCategoryIdGeneratorBuilder(dataSource);
  }

  public static DBMultiCategoryIdGeneratorBuilder create(DataSource dataSource, IdGeneratorTableSpec tableSpec)
  {
    return create(dataSource).tableSpec(tableSpec);
  }


  protected DBMultiCategoryIdGeneratorBuilder(DataSource dataSource)
  {
    super();
    this.dataSource = dataSource;
  }
  
  /**
   * Add a category with an ID producer using the given startId.
   */
  public DBMultiCategoryIdGeneratorBuilder add(String categoryName, long startId)
  {
    IdGeneratorCategorySpec categorySpec;
    
    //@formatter:off
    categorySpec = DefaultIdGeneratorCategorySpec.create(categoryName)
        .setStartId(startId)
    ;
    //@formatter:on
    return addCategories(categorySpec);
  }

  /**
   * Add a category with an ID producer using the given startId and blockSize.
   */
  public DBMultiCategoryIdGeneratorBuilder add(String categoryName, long startId, int blockSize)
  {
    IdGeneratorCategorySpec categorySpec;
    
    //@formatter:off
    categorySpec = DefaultIdGeneratorCategorySpec.create(categoryName)
        .setStartId(startId)
        .setBlockSize(blockSize)
        ;
    //@formatter:on
    return addCategories(categorySpec);
  }
  
  /**
   * Add a category with an ID producer using the given startId and blockSize and length.
   */
  public DBMultiCategoryIdGeneratorBuilder add(String categoryName, long startId, int blockSize, int length)
  {
    IdGeneratorCategorySpec categorySpec;
    
    //@formatter:off
    categorySpec = DefaultIdGeneratorCategorySpec.create(categoryName)
        .setStartId(startId)
        .setBlockSize(blockSize)
        .setLength(length)
    ;
    //@formatter:on
    return addCategories(categorySpec);
  }
  
  /**
   * Adds the given category names for which to register an ID producer.
   */
  public DBMultiCategoryIdGeneratorBuilder addCategories(String... categoryNames)
  {
    IdGeneratorCategorySpec categorySpec;

    for (String categoryName : categoryNames)
    {
      if (SU.notNullOrBlank(categoryName))
      {
        categorySpec = DefaultIdGeneratorCategorySpec.create(categoryName);
        addCategories(categorySpec);
      }
    }
    return this;
  }

  /**
   * Adds the categories with ID producers according to the given specs.
   */
  public DBMultiCategoryIdGeneratorBuilder addCategories(IdGeneratorCategorySpec... specifications)
  {
    String categoryName;

    for (IdGeneratorCategorySpec categorySpec : specifications)
    {
      if (SU.notNullOrBlank(categorySpec.getCategoryName()))
      {
        categoryName = categorySpec.getCategoryName().trim();
        if (isNewCategory(categoryName))
        {
          getCategorySpecs().add(categorySpec);
        }
      }
    }
    return this;
  }

  /**
   * If the default table or column names are not acceptable, specify here
   * the names to be used. 
   */
  public DBMultiCategoryIdGeneratorBuilder tableSpec(IdGeneratorTableSpec tableSpecification)
  {
    setTableSpec((tableSpecification == null) ? IdGeneratorTableSpec.create() : tableSpecification);
    return this;
  }

  /**
   * Invoke this method to signal that the table has been already created in the database.
   */
  public DBMultiCategoryIdGeneratorBuilder tableAlreadyCreated()
  {
    setTableAlreadyCreated(true);
    return this;
  }

  public MultiCategoryIdentifierGenerator build()
  {
    for (IdGeneratorCategorySpec categorySpec : getCategorySpecs())
    {
      regsiterIdGenerator(categorySpec);
    }
    return getMultiCategoryIdGenerator();
  }

  protected void regsiterIdGenerator(IdGeneratorCategorySpec categorySpec)
  {
    ObjectIdentifierDBBuilder builder;

    //@formatter:off
    builder = ObjectIdentifierDBBuilder.create(getDataSource())
        .setTableSpec(getTableSpec())
        .tableAlreadyCreated(isTableAlreadyCreated())
        .configureCategory(categorySpec)
    ;
    //@formatter:on
    getMultiCategoryIdGenerator().register(categorySpec.getCategoryName(), builder.build());
  }

  /**
   * Returns true if no category with the given name has yet been registered.
   */
  protected boolean isNewCategory(String categoryName)
  {
    for (IdGeneratorCategorySpec categorySpec : getCategorySpecs())
    {
      if (categorySpec.getCategoryName().equalsIgnoreCase(categoryName))
      {
        return false;
      }
    }
    return true;
  }

  protected DataSource getDataSource()
  {
    return this.dataSource;
  }

  protected IdGeneratorTableSpec getTableSpec()
  {
    return this.tableSpec;
  }

  protected void setTableSpec(IdGeneratorTableSpec tableSpec)
  {
    this.tableSpec = tableSpec;
  }

  protected List<IdGeneratorCategorySpec> getCategorySpecs()
  {
    return this.categorySpecs;
  }

  protected DefaultMultiCategoryIdentifierGenerator getMultiCategoryIdGenerator()
  {
    return this.multiCategoryIdGenerator;
  }

  protected boolean isTableAlreadyCreated()
  {
    return this.isTableAlreadyCreated;
  }

  protected void setTableAlreadyCreated(boolean isTableCreated)
  {
    this.isTableAlreadyCreated = isTableCreated;
  }
}
