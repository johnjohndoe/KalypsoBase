/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.ogc.gml.om.table.handlers;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.kalypso.commons.xml.XmlTypes;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.property.restriction.IRestriction;
import org.kalypso.gmlschema.property.restriction.RestrictionUtilities;
import org.kalypso.observation.result.ComponentUtilities;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.TupleResult;

/**
 * Default implementation of {@link IComponentUiHandlerProvider}.<br/>
 * Creates columns for all components of the given observation according to its types.
 * 
 * @author Gernot Belger
 * @deprecated To be used in combinasion with the deprecated {@link org.kalypso.ogc.gml.om.table.TupleResultContentProvider}. Use {@link DefaultComponentUiHandlerProvider2} instead, if the new
 *             {@link org.kalypso.ogc.gml.om.table.TupleResultContentProvider2} is used.
 */
@Deprecated
public class DefaultComponentUiHandlerProvider implements IComponentUiHandlerProvider
{
  @Override
  public Map<Integer, IComponentUiHandler> createComponentHandler( final TupleResult tupleResult )
  {
    final Map<Integer, IComponentUiHandler> result = new LinkedHashMap<>();

    final IComponent[] components = tupleResult.getComponents();
    final int widthPercent = components.length == 0 ? 100 : (int)(100.0 / components.length);

    for( int i = 0; i < components.length; i++ )
    {
      final IComponent component = components[i];
      final IComponentUiHandler handlerForComponent = handlerForComponent( i, component, widthPercent );
      if( handlerForComponent != null )
        result.put( i, handlerForComponent );
    }

    return result;
  }

  private IComponentUiHandler handlerForComponent( final int index, final IComponent component, final int columnWidthPercent )
  {
    // Some default value
    final boolean editable = true;
    final boolean resizeable = true;
    final boolean moveable = true;
    final String columnLabel = ComponentUtilities.getComponentLabel( component );
    final int columnWidth = 100;

    final IRestriction[] restrictions = component.getRestrictions();
    if( ComponentUtilities.restrictionContainsEnumeration( restrictions ) )
    {
      final Map<Object, IAnnotation> items = RestrictionUtilities.getEnumerationItems( restrictions );
      return new ComponentUiEnumerationHandler( index, editable, resizeable, moveable, columnLabel, SWT.NONE, columnWidth, columnWidthPercent, StringUtils.EMPTY, StringUtils.EMPTY, items );
    }

    final QName valueTypeName = component.getValueTypeName();

    if( valueTypeName.equals( XmlTypes.XS_DATETIME ) )
      return new ComponentUiDateHandler( index, editable, resizeable, moveable, columnLabel, SWT.NONE, columnWidth, columnWidthPercent, "%1$tm %1$te,%1$tY", StringUtils.EMPTY, null ); //$NON-NLS-1$

    if( valueTypeName.equals( XmlTypes.XS_DOUBLE ) )
    {
      final String format = ComponentUtilities.getDecimalFormat( component );
      return new ComponentUiDoubleHandler( index, editable, resizeable, moveable, columnLabel, SWT.RIGHT, columnWidth, columnWidthPercent, format, StringUtils.EMPTY, null );
    }

    if( valueTypeName.equals( XmlTypes.XS_DECIMAL ) )
    {
      final String format = ComponentUtilities.getDecimalFormat( component );
      return new ComponentUiDecimalHandler( index, editable, resizeable, moveable, columnLabel, SWT.RIGHT, columnWidth, columnWidthPercent, format, StringUtils.EMPTY, null );
    }

    if( valueTypeName.equals( XmlTypes.XS_INTEGER ) )
      return new ComponentUiIntegerHandler( index, editable, resizeable, moveable, columnLabel, SWT.RIGHT, columnWidth, columnWidthPercent, "%d", StringUtils.EMPTY, null ); //$NON-NLS-1$

    if( valueTypeName.equals( XmlTypes.XS_STRING ) )
      return new ComponentUiStringHandler( index, editable, resizeable, moveable, columnLabel, SWT.LEFT, columnWidth, columnWidthPercent, "%s", StringUtils.EMPTY, null ); //$NON-NLS-1$ 

    return null;
  }
}
