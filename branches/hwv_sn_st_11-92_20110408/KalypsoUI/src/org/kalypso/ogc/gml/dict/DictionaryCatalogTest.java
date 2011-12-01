/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.dict;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.util.pool.KeyInfo;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.model.feature.Feature;

/**
 * A plugin test; can only be run in an eclipse runtime workspace.
 * <p>
 * Use the globally available 'KalypsoCoreTest.launch' (located in etc/test) to run this test.
 * </p>
 * 
 * @author Gernot Belger
 */
public class DictionaryCatalogTest extends Assert
{
  protected DictionaryCatalog getCatalog( ) throws Exception
  {
    return KalypsoGisPlugin.getDictionaryCatalog();
  }

  @Test
  public void testDictionaryIsReady( ) throws Exception
  {
    assertNotNull( "The KalypsoCore Plugin must provide a catalog.", getCatalog() ); //$NON-NLS-1$
  }

  /**
   * Retrieves one entry from a dictionary and test if it is non null.
   */
  @Test
  @Ignore
  public void testGetDictionaryEntry( ) throws Exception
  {
    final String urn = "urn:ogc:gml:kalypso:dict:phenomenon:dont:know#niederschlag"; //$NON-NLS-1$

    final Feature entry = getCatalog().getEntry( urn );
    assertNotNull( entry );

    getCatalog().releaseEntry( entry );
  }

  /**
   * Tests the behaviour of the pool in concert with getting a dictionary entry.
   * <p>
   * After release of the last entry, also the pool object (here the GMLWorkspace) should be set free.
   * </p>
   */
  @Test
  @Ignore
  public void testDictionaryIsPooled( ) throws Exception
  {
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();

    final KeyInfo[] infosBeforeStart = pool.getInfos();
    assertEquals( "Pool must be empty before start", 0, infosBeforeStart.length ); //$NON-NLS-1$

    final Feature entry = getCatalog().getEntry( "urn:ogc:gml:kalypso:dict:phenomenon:dont:know#niederschlag" ); //$NON-NLS-1$

    final KeyInfo[] infos = pool.getInfos();
    assertEquals( "Pool should have one object pooled at this point", 1, infos.length ); //$NON-NLS-1$

    // pool is working on the workspace of the feature
    final CommandableWorkspace cw = (CommandableWorkspace) infos[0].getObject();
    assertEquals( cw.getWorkspace(), entry.getWorkspace() );

    getCatalog().releaseEntry( entry );

    final KeyInfo[] infosAtEnd = pool.getInfos();
    assertEquals( "Pool must be empty after we have finished", 0, infosAtEnd.length ); //$NON-NLS-1$
  }

  /**
   * Tests the behaviour of the pool in concert with getting a dictionary entry.
   * <p>
   * This time we get several entries
   * </p>
   * <p>
   * After release of the last entry, also the pool object (here the GMLWorkspace) should be set free.
   * </p>
   */
  @Test
  @Ignore
  public void testDictionaryIsPooledComplex( ) throws Exception
  {
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();

    final KeyInfo[] infosBeforeStart = pool.getInfos();
    assertEquals( "Pool must be empty before start", 0, infosBeforeStart.length ); //$NON-NLS-1$

    // one and release it
    final Feature entry1 = getCatalog().getEntry( "urn:ogc:gml:kalypso:dict:phenomenon:dont:know#niederschlag" ); //$NON-NLS-1$
    getCatalog().releaseEntry( entry1 );

    assertEquals( "Pool should be empty now", 0, pool.getInfos().length ); //$NON-NLS-1$

    // two different features
    final Feature entry2 = getCatalog().getEntry( "urn:ogc:gml:kalypso:dict:phenomenon:dont:know#niederschlag" ); //$NON-NLS-1$
    final Feature entry3 = getCatalog().getEntry( "urn:ogc:gml:kalypso:dict:phenomenon:dont:know#wasserstand" ); //$NON-NLS-1$
    getCatalog().releaseEntry( entry2 );
    getCatalog().releaseEntry( entry3 );

    // one two times and release them, objet must be the same
    final Feature entry4 = getCatalog().getEntry( "urn:ogc:gml:kalypso:dict:phenomenon:dont:know#niederschlag" ); //$NON-NLS-1$
    final Feature entry5 = getCatalog().getEntry( "urn:ogc:gml:kalypso:dict:phenomenon:dont:know#niederschlag" ); //$NON-NLS-1$
    getCatalog().releaseEntry( entry4 );
    getCatalog().releaseEntry( entry5 );

    assertEquals( "Pool must be empty after we have finished", 0, pool.getInfos().length ); //$NON-NLS-1$
  }

  /**
   * Tests the behaviour of the pool in concert with getting a dictionary entry.
   * <p>
   * After release of the last entry, also the pool object (here the GMLWorkspace) should be set free.
   * </p>
   */
  @Test
  @Ignore
  public void testNotDirty( ) throws Exception
  {
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();

    final Feature entry = getCatalog().getEntry( "urn:ogc:gml:kalypso:dict:phenomenon:dont:know#niederschlag" ); //$NON-NLS-1$

    final KeyInfo[] infos = pool.getInfos();
    assertEquals( "Pool should have one object pooled at this point", 1, infos.length ); //$NON-NLS-1$
    assertFalse( infos[0].isDirty() );

    getCatalog().releaseEntry( entry );
  }
}
