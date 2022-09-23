// ===========================================================================
// CONTENT  : INTERFACE IdGeneratorCategorySpec
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.0 - 18/04/2020
// HISTORY  :
//  18/04/2020  mdu  CREATED
//
// Copyright (c) 2020, by MDCS. All rights reserved.
// ===========================================================================
package org.pfsw.db.util;

/**
 * A definition of an ID generator for a specific category.  
 *
 * @author Manfred Duchrow
 * @version 1.0
 */
public interface IdGeneratorCategorySpec
{
  /**
   * Returns the unique category name.
   */
  String getCategoryName();

  /**
   * Returns the initial identifier value to start generation with. 
   */
  long getStartId();

  /**
   * Returns the block size to be reserved for in-memory incrementing. 
   */
  int getBlockSize();

  /**
   * Returns the length of the IDs.
   * If the length is greater than zero it means that the string representation
   * of the ID will be left padded with the padding character to the required length.
   * @see #getPaddingChar() 
   */
  int getLength();

  /**
   * Returns the character to be used for filling up generated IDs to a common fixed length.
   * @see #getLength().
   */
  char getPaddingChar();
  
  /**
   * Returns a string that must be used as prefix of any generated ID's string representation
   * or null if no prefix is wanted.
   */
  String getIdPrefix();
}