// ===========================================================================
// CONTENT  : CLASS IdGeneratorTableSpec
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.0 - 18/04/2020
// HISTORY  :
//  18/04/2020  mdu  CREATED
//
// Copyright (c) 2020, by MDCS. All rights reserved.
// ===========================================================================
package org.pfsw.db.util;

/**
 * A specification object for the name and column name definition
 * of a database table used for ID generation.
 *
 * @author Manfred Duchrow
 * @version 1.0
 */
public class IdGeneratorTableSpec
{
  public static final String OID_TABLE_NAME = "OIDADMIN";
  public static final String OID_CN_CATEGORY = "CATEGORY";
  public static final String OID_CN_NEXTID = "NEXTID";
  public static final String OID_CN_BLOCKSIZE = "BLOCKSIZE";

  private String tableQualifier = null;
  private String unqualifiedTableName = OID_TABLE_NAME;
  private String categoryColumnName = OID_CN_CATEGORY;
  private String nextIdColumnName = OID_CN_NEXTID;
  private String blockSizeColumnName = OID_CN_BLOCKSIZE;

  public static IdGeneratorTableSpec create()
  {
    return new IdGeneratorTableSpec();
  }

  public static IdGeneratorTableSpec create(String unqualifiedTableName)
  {
    return create().setUnqualifiedTableName(unqualifiedTableName);
  }

  public IdGeneratorTableSpec()
  {
    super();
  }

  public String getTableQualifier()
  {
    return this.tableQualifier;
  }

  public IdGeneratorTableSpec setTableQualifier(String tableQualifier)
  {
    this.tableQualifier = tableQualifier;
    return this;
  }

  public String getUnqualifiedTableName()
  {
    return this.unqualifiedTableName;
  }

  public IdGeneratorTableSpec setUnqualifiedTableName(String unqualifiedTableName)
  {
    this.unqualifiedTableName = unqualifiedTableName;
    return this;
  }

  public String getCategoryColumnName()
  {
    return this.categoryColumnName;
  }

  public IdGeneratorTableSpec setCategoryColumnName(String categoryColumnName)
  {
    this.categoryColumnName = categoryColumnName;
    return this;
  }

  public String getNextIdColumnName()
  {
    return this.nextIdColumnName;
  }

  public IdGeneratorTableSpec setNextIdColumnName(String nextIdColumnName)
  {
    this.nextIdColumnName = nextIdColumnName;
    return this;
  }

  public String getBlockSizeColumnName()
  {
    return this.blockSizeColumnName;
  }

  public IdGeneratorTableSpec setBlockSizeColumnName(String blockSizeColumnName)
  {
    this.blockSizeColumnName = blockSizeColumnName;
    return this;
  }

  @Override
  public String toString()
  {
    return String.format("%s('%s')", getClass().getSimpleName(), getUnqualifiedTableName());
  }
}
