// ===========================================================================
// CONTENT  : INTERFACE MultiCategoryIdentifierGenerator
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.0 - 18/04/2020
// HISTORY  :
//  18/04/2020  mdu  CREATED
//
// Copyright (c) 2020, by MDCS. All rights reserved.
// ===========================================================================
package org.pfsw.db.util;

import org.pfsw.bif.identifier.IdentifierGenerationException;

/**
 * An ID generator that supports different ID sequences for different categories. 
 *
 * @author Manfred Duchrow
 * @version 1.0
 */
public interface MultiCategoryIdentifierGenerator
{
  /**
   * Returns a new identifier for the given category.
   * 
   * @param category An arbitrary but unique name for the ID sequence (must not be null).
   * @throws IdentifierGenerationException if the category cannot be found of ID generation fails for any reason.
   */
  String newIdentifier(String category);

  /**
   * Returns a new identifier which is different to the last one
   * for the given category.
   * 
   * @param category An arbitrary but unique name for the ID sequence (must not be null).
   * @return The next ID value. 
   * @throws IdentifierGenerationException if the category cannot be found of ID generation fails for any reason.
   */
  long nextIdentifier(String category);
}