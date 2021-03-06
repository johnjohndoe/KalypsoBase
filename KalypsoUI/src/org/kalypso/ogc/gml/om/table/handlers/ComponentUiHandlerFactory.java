/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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

import java.util.Map;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.XmlTypes;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.property.restriction.IRestriction;
import org.kalypso.gmlschema.property.restriction.RestrictionUtilities;
import org.kalypso.observation.result.ComponentUtilities;
import org.kalypso.observation.result.IComponent;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * Factory which creates the right handler for the right value type.
 * 
 * @author Dirk Kuch
 * @author Gernot Belger
 */
public class ComponentUiHandlerFactory
{
  public static IComponentUiHandler getHandler( final int index, final IComponent component, final boolean editable, final boolean resizeable, final boolean moveable, final String columnLabel, final int columnStyle, final int columnWidth, final int columnWidthPercent, final String displayFormat, final String nullFormat, final String parseFormat )
  {
    final QName valueTypeName = component.getValueTypeName();

    final String columnTooltip = columnLabel;

    final IRestriction[] restrictions = component.getRestrictions();
    if( ComponentUtilities.restrictionContainsEnumeration( restrictions ) )
    {
      final Map<Object, IAnnotation> items = RestrictionUtilities.getEnumerationItems( restrictions );
      return new ComponentUiEnumerationHandler( index, editable, resizeable, moveable, columnLabel, columnTooltip, columnStyle, columnWidth, columnWidthPercent, displayFormat, nullFormat, items );
    }

    if( XmlTypes.XS_DATETIME.equals( valueTypeName ) )
      return new ComponentUiDateHandler( index, editable, resizeable, moveable, columnLabel, columnTooltip, columnStyle, columnWidth, columnWidthPercent, displayFormat, nullFormat, parseFormat );
    else if( XmlTypes.XS_DOUBLE.equals( valueTypeName ) )
      return new ComponentUiDoubleHandler( index, editable, resizeable, moveable, columnLabel, columnTooltip, columnStyle, columnWidth, columnWidthPercent, displayFormat, nullFormat, parseFormat );
    else if( XmlTypes.XS_DECIMAL.equals( valueTypeName ) )
      return new ComponentUiDecimalHandler( index, editable, resizeable, moveable, columnLabel, columnTooltip, columnStyle, columnWidth, columnWidthPercent, displayFormat, nullFormat, parseFormat );
    else if( XmlTypes.XS_INTEGER.equals( valueTypeName ) )
      return new ComponentUiIntegerHandler( index, editable, resizeable, moveable, columnLabel, columnTooltip, columnStyle, columnWidth, columnWidthPercent, displayFormat, nullFormat, parseFormat );
    else if( XmlTypes.XS_STRING.equals( valueTypeName ) )
      return new ComponentUiStringHandler( index, editable, resizeable, moveable, columnLabel, columnTooltip, columnStyle, columnWidth, columnWidthPercent, displayFormat, nullFormat, parseFormat );
    else if( XmlTypes.XS_BOOLEAN.equals( valueTypeName ) )
      return new ComponentUiBooleanHandler( index, editable, resizeable, moveable, columnLabel, columnTooltip, columnStyle, columnWidth, columnWidthPercent, displayFormat, nullFormat, parseFormat );

    throw new UnsupportedOperationException( Messages.getString( "org.kalypso.ogc.gml.om.table.handlers.ComponentUiHandlerFactory.6" ) + valueTypeName ); //$NON-NLS-1$
  }
}