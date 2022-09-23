// ===========================================================================
// CONTENT  : CLASS DefaultMultiCategoryIdentifierGenerator
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.0 - 18/04/2020
// HISTORY  :
//  18/04/2020  mdu  CREATED
//
// Copyright (c) 2020, by MDCS. All rights reserved.
// ===========================================================================
package org.pfsw.db.util;

import static org.pfsw.text.StringUtil.*;

import java.util.HashMap;
import java.util.Map;

import org.pfsw.bif.identifier.IdentifierGenerationException;
import org.pfsw.db.LoggerProvider;
import org.pfsw.logging.Logger2;

/**
 * A default implementation of an identifier generator for multiple categories.
 * The assumption is that an instance of this class gets generated and populated
 * (see {@link #register(String, ObjectIdentifierProducer)}) with category related
 * ID producers once and then will be used with that setup for ID generation.
 * Registering further ID producers at a later time might cause errors because the
 * underlying ID producer registry is not thread-safe.
 *
 * @author Manfred Duchrow
 * @version 1.0
 */
public class DefaultMultiCategoryIdentifierGenerator implements MultiCategoryIdentifierGenerator
{
  private final Map<String, ObjectIdentifierProducer> generatorRegistry = new HashMap<String, ObjectIdentifierProducer>();

  public DefaultMultiCategoryIdentifierGenerator()
  {
    super();
  }

  @Override
  public String newIdentifier(String category)
  {
    try
    {
      return getIdProducer(category).newIdentifier();
    }
    catch (IdentifierGenerationException e)
    {
      throw e;
    }
    catch (RuntimeException e)
    {
      throw new IdentifierGenerationException(e, "Unable to generate new identifier for category '%s'", category);
    }
  }
  
  @Override
  public long nextIdentifier(String category)
  {
    try
    {
      return getIdProducer(category).nextIdentifier();      
    }
    catch (IdentifierGenerationException e)
    {
      throw e;
    }
    catch (RuntimeException e)
    {
      throw new IdentifierGenerationException(e, "Unable to generate next identifier for category '%s'", category);
    }
  }
  
  /**
   * Registers the given identifier producer for the specified category.
   * If the category is null or blank or the ID producer is null, a warning will be logged
   * and nothing registered.
   * 
   * @param categoryName The category to register the ID producer under (must not be null or blank).
   * @param idProducer The ID producer to be registered (must not be null).
   * @return this object.
   */
  public DefaultMultiCategoryIdentifierGenerator register(String categoryName, ObjectIdentifierProducer idProducer) 
  {
    String category;

    category = normalizeCategory(categoryName);
    if (SU.isNullOrEmpty(category) || (idProducer == null))
    {
      log().warnf("Cannot register %s for category '%s'", idProducer, category);
      return this;
    }
    getGeneratorRegistry().put(category, idProducer);
    return this;
  }
  
  protected ObjectIdentifierProducer getIdProducer(String categoryName) 
  {
    String category;
    ObjectIdentifierProducer identifierProducer;
    
    category = normalizeCategory(categoryName);
    if (category == null)
    {
      throw new IdentifierGenerationException("Cannot generate identifier for category=null");
    }
    identifierProducer = getGeneratorRegistry().get(category);
    if (identifierProducer == null)
    {
      throw new IdentifierGenerationException("No identifier generator registered for category '%s'", category);      
    }
    return identifierProducer;
  }
  
  protected String normalizeCategory(String category) 
  {
    return (category == null) ? null : category.trim();
  }
  
  protected Logger2 log()
  {
    return LoggerProvider.getLogger();
  }
  
  protected Map<String, ObjectIdentifierProducer> getGeneratorRegistry()
  {
    return this.generatorRegistry;
  }
}
