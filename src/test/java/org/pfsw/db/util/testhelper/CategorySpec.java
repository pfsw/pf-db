// ===========================================================================
// CONTENT  : ENUM CategorySpec
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.0 - 18/04/2020
// HISTORY  :
//  18/04/2020  mdu  CREATED
//
// Copyright (c) 2020, by MDCS. All rights reserved.
// ===========================================================================
package org.pfsw.db.util.testhelper;

import org.pfsw.db.util.IdGeneratorCategorySpec;

public enum CategorySpec implements IdGeneratorCategorySpec
{
  EPSILON("Epsilon", 123L, 20, 9, '_', "ET"),
  OMEGA("Omega", 77L, 8);
  
  private final String categoryName;
  private final long startId;
  private final int blockSize;
  private final int length;
  private final char paddingChar;
  private final String idPrefix;
  
  private CategorySpec(String categoryName, long startId, int blockSize, int length, char paddingChar, String idPrefix)
  {
    this.categoryName = categoryName;
    this.startId = startId;
    this.blockSize = blockSize;
    this.length = length;
    this.paddingChar = paddingChar;
    this.idPrefix = idPrefix;
  }

  private CategorySpec(String categoryName, long startId, int blockSize)
  {
    this(categoryName, startId, blockSize, 8, '0', null);
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

  @Override
  public int getBlockSize()
  {
    return this.blockSize;
  }

  @Override
  public int getLength()
  {
    return this.length;
  }

  @Override
  public char getPaddingChar()
  {
    return this.paddingChar;
  }

  @Override
  public String getIdPrefix()
  {
    return this.idPrefix;
  }

  @Override
  public String toString()
  {
    return String.format("%s(%s, '%s', start=%d, blockSize=%d, length=%d, padChar=%s, prefix='%s')", name(), 
        getClass().getSimpleName(), getCategoryName(), getStartId(), getBlockSize(), getLength(), getPaddingChar(), getIdPrefix());
  }
}