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

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.gml.classifications.IClassificationClass;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.model.ILanduseModel;

/**
 * @author Dirk Kuch
 */
public class LanduseMappingLabelProvider extends ColumnLabelProvider
{
  private static final String EMPTY_STRING = Messages.getString( "LanduseMappingLabelProvider.0" ); //$NON-NLS-1$

  private final int m_column;

  private final ILanduseModel m_model;

  public LanduseMappingLabelProvider( final ILanduseModel model, final int column )
  {
    m_model = model;
    m_column = column;
  }

  @Override
  public String getText( final Object element )
  {
    @SuppressWarnings("rawtypes")
    final Entry entry = toEntry( element );
    if( Objects.isNull( entry ) )
      return super.getText( element );

    if( m_column == 0 )
      return entry.getKey().toString();
    else if( m_column == 1 )
    {
      final Object value = entry.getValue();
      if( !(value instanceof String) )
        return EMPTY_STRING;

      final String strValue = (String) value;
      if( StringUtils.isEmpty( strValue ) )
        return EMPTY_STRING;

      final IClassificationClass clazz = findClass( strValue );
      if( Objects.isNull( clazz ) )
        return Messages.getString( "LanduseMappingLabelProvider.1" ); //$NON-NLS-1$

      return clazz.getLabelWithValues();
    }
    else
      throw new IllegalStateException();
  }

  private IClassificationClass findClass( final String name )
  {
    final IClassificationClass[] clazzes = m_model.getClasses();
    for( final IClassificationClass clazz : clazzes )
    {
      if( clazz.getName().equals( name ) )
        return clazz;
    }

    return null;
  }

  public static Map.Entry toEntry( final Object element )
  {
    if( element instanceof Map.Entry )
      return (Map.Entry) element;

    return null;
  }
}