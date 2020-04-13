// ===========================================================================
// CONTENT  : CLASS ObjectIdentifierDBBuilder
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.0 - 12/04/2020
// HISTORY  :
//  12/04/2020  mdu  CREATED
//
// Copyright (c) 2020, by MDCS. All rights reserved.
// ===========================================================================
package org.pfsw.db.util;

import javax.sql.DataSource;

/**
 * Builder for ObjectIdentifierDB that supports fluent API.
 *
 * @author Manfred Duchrow
 * @version 1.0
 */
public class ObjectIdentifierDBBuilder
{
  /**
   * Creates a new builder instance with the given data source.
   *
   * @param ds A valid data source that allows connection to a database
   */
  public static ObjectIdentifierDBBuilder create(DataSource ds)
  {
    return new ObjectIdentifierDBBuilder(ds);
  }

  private final ObjectIdentifierDB objectIdentifierDB;

  protected ObjectIdentifierDBBuilder(DataSource ds)
  {
    super();
    this.objectIdentifierDB = new ObjectIdentifierDB(ds);
  }
  
  /**
   * Sets the category name for the identifiers.
   */
  public ObjectIdentifierDBBuilder setCategory(String category) 
  {
    getObjectIdentifierDB().setCategory(category);
    return this;
  }
  
  /**
   * Sets a prefix string for all IDs to be generated.
   * 
   * By default no prefix is set.
   */
  public ObjectIdentifierDBBuilder setPrefix(String prefix) 
  {
    getObjectIdentifierDB().setPrefix(prefix);
    return this;
  }
  
  /**
   * Sets the first value of the IDs to be generated.
   * Be aware that this value will be ignored if there is already a table entry
   * in the database for the category and it contains a different value.
   */
  public ObjectIdentifierDBBuilder setStartId(long startValue)
  {
    getObjectIdentifierDB().setStartId(startValue);
    return this;
  }

  /**
   * Sets how many IDs are reserved in-memory per read of the category from the database.
   * The default block size is 1.
   */
  public ObjectIdentifierDBBuilder setBlockSize(int blockSize)
  {
    getObjectIdentifierDB().setBlockSize(blockSize);
    return this;
  }
  
  /**
   * Sets the length of the resulting identifiers.
   * This implies that padding is activated and the identifiers will
   * be filled up with leading padding characters.
   *  
   * @see #setPaddingChar(char)
   * @see #noPadding()
   */
  public ObjectIdentifierDBBuilder setLength(int length)
  {
    getObjectIdentifierDB().setLength(length);
    return this;
  }
  
  /**
   * Configures the ID generator to not do any (left) padding on the
   * generated identifiers.
   * Padding is activated by default!
   */
  public ObjectIdentifierDBBuilder noPadding()
  {
    return setLength(-1);
  }
  
  /**
   * Sets the padding character to the given values.
   * The default padding character is '0'.
   */
  public ObjectIdentifierDBBuilder setPaddingChar(char ch)
  {
    getObjectIdentifierDB().setPadChar(ch);
    return this;
  }
  
  /**
   * This method should be invoked to prevent automatic table creation which makes
   * sense if the table has been create already externally.
   */
  public ObjectIdentifierDBBuilder tableAlreadyInitialized() 
  {
    getObjectIdentifierDB().tableAlreadyInitialized();
    return this;
  }

  public ObjectIdentifierDBBuilder setTableQualifier(String tableQualifier)
  {
    getObjectIdentifierDB().setTableQualifier(tableQualifier);
    return this;
  }
  
  public ObjectIdentifierDBBuilder setTableName(String tableName)
  {
    getObjectIdentifierDB().setUnqualifiedTableName(tableName);
    return this;
  }
  
  public ObjectIdentifierDBBuilder setCategoryColumn(String columnName)
  {
    getObjectIdentifierDB().setCategoryColumnName(columnName);
    return this;
  }
  
  public ObjectIdentifierDBBuilder setNextIdColumn(String columnName)
  {
    getObjectIdentifierDB().setNextIdColumnName(columnName);
    return this;
  }
  
  public ObjectIdentifierDBBuilder setBlockSizeColumn(String columnName)
  {
    getObjectIdentifierDB().setBlockSizeColumnName(columnName);
    return this;
  }
  
  public ObjectIdentifierDB build()
  {
    return getObjectIdentifierDB();
  }

  protected ObjectIdentifierDB getObjectIdentifierDB()
  {
    return this.objectIdentifierDB;
  }
}
