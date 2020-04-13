// ===========================================================================
// CONTENT  : CLASS ObjectIdentifierGenerator
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.6 - 13/04/2020
// HISTORY  :
//  12/12/2000  duma  CREATED
//  02/12/2001  duma  moved from com.mdcs.db.util
//	28/06/2002	duma	renamed	->	from ObjectIdentifierFactory
//	28/06/2002	duma	added		->	nextIdentifier()
//	02/07/2002	duma	added		->	Constructor with startId
//	26/07/2003	duma	bugfix	->	idLength of 1 was not allowed
//	22/02/2008	mdu		changed	->	to extend ObjectIdGenerator
//  13/02/2020  mdu   added   ->  setStartId()
//
// Copyright (c) 2000-2020, by Manfred Duchrow. All rights reserved.
// ===========================================================================
package org.pfsw.db.util;

import org.pfsw.text.ObjectIdGenerator;

/**
 * This class provides identifiers by incrementing an internal counter,
 * starting at 1.
 *
 * @author Manfred Duchrow
 * @version 1.6
 */
public class ObjectIdentifierGenerator extends ObjectIdGenerator implements ObjectIdentifierProducer
{
  /**
   * Initialize the new instance with default values.
   * That is an ID length of 10 and a start ID of 1.
   */
  public ObjectIdentifierGenerator()
  {
    super();
  }

  /**
   * Initialize the new instance with the length for the generated identifiers.
   * 
   * @param idLength The length to which Ids are filled up with leading zeros (must be > 0)
   */
  public ObjectIdentifierGenerator(int idLength)
  {
    this();
    setLength(idLength);
  }

  /**
   * Initialize the new instance with the length for the generated identifiers
   * and the id to start with.
   * 
   * @param startId The first id to be generated.
   * @param idLength The length to which Ids are filled up with leading zeros.
   */
  public ObjectIdentifierGenerator(long startId, int idLength)
  {
    this(idLength);
    setStartId(startId);
  }
  
  protected void setStartId(long startId)
  {
    if (startId >= 0)
    {
      setNextId(startId);
    }
  }
}