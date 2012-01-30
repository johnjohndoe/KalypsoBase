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
package org.kalypso.model.wspm.ui.profil.wizard.landuse.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Properties;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.shape.dbf.IDBFField;

/**
 * @author Dirk Kuch
 */
public class LanduseProperties extends Properties
{
  private static final String SELECTION = "SHAPE_PROPERTY"; //$NON-NLS-1$

  private final ILanduseModel m_model;

  private String m_shapeProperty;

  public LanduseProperties( final ILanduseModel model )
  {
    m_model = model;
  }

  @Override
  public synchronized void load( final InputStream inStream ) throws IOException
  {
    super.load( inStream );

    m_shapeProperty = getProperty( SELECTION );
    remove( SELECTION );
  }

  @Override
  public synchronized void load( final Reader reader )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public synchronized void loadFromXML( final InputStream in )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void store( final OutputStream out, final String comments ) throws IOException
  {
    final IDBFField column = m_model.getShapeColumn();
    if( Objects.isNotNull( column ) )
      put( SELECTION, column.getName() );

    super.store( out, comments );
  }

  @Override
  public synchronized void storeToXML( final OutputStream os, final String comment )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public synchronized void storeToXML( final OutputStream os, final String comment, final String encoding )
  {
    throw new UnsupportedOperationException();
  }

  public String getColumn( )
  {
    return m_shapeProperty;
  }
}
