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
package org.kalypso.model.wspm.ui.profil.wizard.landuse.utils;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.model.ILanduseModel;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.model.LanduseMappingUpdater;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.model.LanduseProperties;
import org.kalypso.shape.ShapeFile;
import org.kalypso.shape.dbf.IDBFField;

/**
 * @author Dirk Kuch
 */
public class LanduseMappingFilter extends ViewerFilter
{
  private final ILanduseModel m_model;

  private String[] m_properties;

  private ShapeFile m_shapeFile;

  private IDBFField m_column;

  public LanduseMappingFilter( final ILanduseModel model )
  {
    m_model = model;
  }

  @Override
  public boolean select( final Viewer viewer, final Object parentElement, final Object element )
  {
    /** only show entries which are part of the selected shape file */
    @SuppressWarnings("rawtypes")
    final Entry entry = LanduseMappingLabelProvider.toEntry( element );
    if( Objects.isNull( element ) )
      return false;

    final String property = getProperty( entry );
    if( Objects.isNull( property ) )
      return false;

    final String[] properties = getProperties();

    return ArrayUtils.contains( properties, property );
  }

  private String[] getProperties( )
  {
    final Set<String> entries = new HashSet<>();

    final ShapeFile shapeFile = m_model.getShapeFile();
    final IDBFField column = m_model.getShapeColumn();

    if( Objects.isNull( shapeFile, column ) )
      return new String[] {};

    if( Objects.equal( m_shapeFile, shapeFile ) && Objects.equal( m_column, column ) )
      return m_properties;

    final LanduseProperties properties = new LanduseProperties( m_model );
    final LanduseMappingUpdater updater = new LanduseMappingUpdater( shapeFile, column, properties );
    updater.run( new NullProgressMonitor() );

    final Set<Object> keys = properties.keySet();
    for( final Object key : keys )
    {
      if( key instanceof String )
        entries.add( (String) key );
    }

    m_properties = entries.toArray( new String[] {} );
    m_shapeFile = shapeFile;
    m_column = column;

    return m_properties;
  }

  private String getProperty( @SuppressWarnings("rawtypes") final Entry entry )
  {
    final Object object = entry.getKey();
    if( object instanceof String )
      return (String) object;

    return null;
  }

}
