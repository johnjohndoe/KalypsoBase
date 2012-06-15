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
package org.kalypso.core.layoutwizard;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.sourceforge.projects.kalypsobase.layout.ObjectFactory;

import org.kalypso.commons.bind.JaxbUtilities;

/**
 * Utilities for bound layout schema.
 * 
 * @author Gernot Belger
 */
public final class LayoutBinding
{
  // private static final SchemaCache SCHEMA_CACHE = new SchemaCache( KalypsoCorePlugin.getID(), "etc/schemas/" ); //$NON-NLS-1$

  private static JAXBContext JC = null;

  private LayoutBinding( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" ); //$NON-NLS-1$
  }

  public static Unmarshaller createLayoutUnmarshaller( ) throws JAXBException
  {
    return createUnmarshaller( "layout/layout.xsd" ); //$NON-NLS-1$
  }

  // TODO: move these + schema-cache to common place
  private static Unmarshaller createUnmarshaller( final String schemaFile ) throws JAXBException
  {
    final Unmarshaller unmarshaller = getJaxbContext().createUnmarshaller();
    // final Schema schema = SCHEMA_CACHE.getSchema( schemaFile );
    // FIXME: make a tracing option
    // unmarshaller.setSchema( schema );
    return unmarshaller;
  }

  private static synchronized JAXBContext getJaxbContext( )
  {
    if( JC == null )
      JC = JaxbUtilities.createQuiet( ObjectFactory.class, net.sourceforge.projects.kalypsobase.swt.ObjectFactory.class );
    return JC;
  }
}
