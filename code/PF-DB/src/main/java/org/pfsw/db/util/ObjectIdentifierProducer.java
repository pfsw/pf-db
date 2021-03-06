// ===========================================================================
// CONTENT  : INTERFACE ObjectIdentifierProducer
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.3 - 22/02/2008
// HISTORY  :
//  05/01/2001  duma  CREATED
//  02/12/2001  duma  moved from com.mdcs.db.util
//	28/06/2002	duma	added	nextIdentifier()
//	22/02/2008	mdu		changed -->	to extend IObjectIdGenerator
//
// Copyright (c) 2001-2008, by Manfred Duchrow. All rights reserved.
// ===========================================================================
package org.pfsw.db.util;

import org.pfsw.bif.identifier.IObjectIdGenerator;

/**
 * An object that generates new identifiers must implement this interface.
 *
 * @author Manfred Duchrow
 * @version 1.3
 */
public interface ObjectIdentifierProducer extends IObjectIdGenerator
{
  /**
   * Returns a new identifier which is different to the last one.
   */
  public long nextIdentifier();

}