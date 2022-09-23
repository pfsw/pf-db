// ===========================================================================
// CONTENT  : CLASS DefaultIdGeneratorCategorySpec
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.0 - 18/04/2020
// HISTORY  :
//  18/04/2020  mdu  CREATED
//
// Copyright (c) 2020, by MDCS. All rights reserved.
// ===========================================================================
package org.pfsw.db.util;

/**
 * An ID generator specification with the following default values.
 * <ul>
 * <li>startId = 1</li>
 * <li>blockSize = 10</li>
 * <li>length = 0</li>
 * <li>paddingChar = '0'</li>
 * <li>idPrefix = null</li>
 * <ul>
 *
 * @author Manfred Duchrow
 * @version 1.0
 */
public class DefaultIdGeneratorCategorySpec implements IdGeneratorCategorySpec
{
  private final String categoryName;
  private long startId = 1;
  private int blockSize = 10;
  private int length = 0;
  private char paddingChar = '0';
  private String idPrefix = null;

  public static DefaultIdGeneratorCategorySpec create(String categoryName)
  {
    return new DefaultIdGeneratorCategorySpec(categoryName);
  }

  public DefaultIdGeneratorCategorySpec(String categoryName)
  {
    super();
    this.categoryName = categoryName;
  }

  @Override
  public String getCategoryName()
  {
    return this.categoryName;
  }

  @Override
  public long getStartId()
  {
    return this.startId;
  }

  public DefaultIdGeneratorCategorySpec setStartId(long startId)
  {
    this.startId = startId;
    return this;
  }

  @Override
  public int getBlockSize()
  {
    return this.blockSize;
  }

  public DefaultIdGeneratorCategorySpec setBlockSize(int blockSize)
  {
    this.blockSize = blockSize;
    return this;
  }

  @Override
  public int getLength()
  {
    return this.length;
  }

  public DefaultIdGeneratorCategorySpec setLength(int length)
  {
    this.length = length;
    return this;
  }

  @Override
  public char getPaddingChar()
  {
    return this.paddingChar;
  }

  public DefaultIdGeneratorCategorySpec setPaddingChar(char paddingChar)
  {
    this.paddingChar = paddingChar;
    return this;
  }

  @Override
  public String getIdPrefix()
  {
    return this.idPrefix;
  }
  
  public DefaultIdGeneratorCategorySpec setIdPrefix(String idPrefix)
  {
    this.idPrefix = idPrefix;
    return this;
  }
  
  @Override
  public String toString()
  {
    return String.format("%s('%s', start=%d, blockSize=%d, length=%d, padChar=%s, prefix='%s')", 
        getClass().getSimpleName(), getCategoryName(), getStartId(), getBlockSize(), getLength(), getPaddingChar(), getIdPrefix());
  }
}
