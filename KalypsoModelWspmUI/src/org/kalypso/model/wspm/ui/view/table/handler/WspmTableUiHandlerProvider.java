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
package org.kalypso.model.wspm.ui.view.table.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.kalypso.commons.xml.XmlTypes;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.property.restriction.IRestriction;
import org.kalypso.gmlschema.property.restriction.RestrictionUtilities;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.ui.view.table.ComponentUiProblemHandler;
import org.kalypso.observation.result.ComponentUtilities;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.om.table.handlers.ComponentUiBooleanHandler;
import org.kalypso.ogc.gml.om.table.handlers.ComponentUiDateHandler;
import org.kalypso.ogc.gml.om.table.handlers.ComponentUiDecimalHandler;
import org.kalypso.ogc.gml.om.table.handlers.ComponentUiDoubleHandler;
import org.kalypso.ogc.gml.om.table.handlers.ComponentUiEnumerationHandler;
import org.kalypso.ogc.gml.om.table.handlers.ComponentUiIntegerHandler;
import org.kalypso.ogc.gml.om.table.handlers.ComponentUiStringHandler;
import org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler;
import org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandlerProvider;

/**
 * @author Dirk Kuch
 */
public class WspmTableUiHandlerProvider implements IComponentUiHandlerProvider
{
  private static final int DEFAULT_SPACING = 100;

  private final IProfil m_profile;

  public WspmTableUiHandlerProvider( final IProfil profile )
  {
    m_profile = profile;
  }

  @Override
  public Map<Integer, IComponentUiHandler> createComponentHandler( final TupleResult tupleResult )
  {
    Assert.isTrue( tupleResult == m_profile.getResult() );

    final List<ComponentHandlerSortContainer> handlers = new ArrayList<ComponentHandlerSortContainer>();
    handlers.add( new ComponentHandlerSortContainer( "unknown", -1, new ComponentUiProblemHandler( m_profile ) ) ); //$NON-NLS-1$

    final IComponent[] pointMarkerTypes = m_profile.getPointMarkerTypes();
    final IComponent[] components = m_profile.getPointProperties();
    final int spacing = DEFAULT_SPACING / components.length;

    for( int index = 0; index < components.length; index++ )
    {
      final IComponent component = components[index];
      if( isPointMarker( pointMarkerTypes, component ) )
        continue;

      final IComponentUiHandler handler = createHandler( index, component, spacing );
      handlers.add( new ComponentHandlerSortContainer( component.getId(), index, handler ) );
    }

    Collections.sort( handlers, new ComponentHandlerContainerSorter() );

    final Map<Integer, IComponentUiHandler> map = new LinkedHashMap<Integer, IComponentUiHandler>();

    final ComponentHandlerSortContainer[] sorted = handlers.toArray( new ComponentHandlerSortContainer[] {} );
    for( final ComponentHandlerSortContainer handler : sorted )
    {
      map.put( handler.getIndex(), handler.getHandler() );
    }

    return map;
  }

  private boolean isPointMarker( final IComponent[] pointMarkerTypes, final IComponent component )
  {
    return ArrayUtils.contains( pointMarkerTypes, component );
  }

  private IComponentUiHandler createHandler( final int index, final IComponent component, final int spacing )
  {
    final String label = component.getName();
    final QName valueTypeName = component.getValueTypeName();

    final IRestriction[] restrictions = component.getRestrictions();
    if( ComponentUtilities.restrictionContainsEnumeration( restrictions ) )
    {
      final Map<Object, IAnnotation> items = RestrictionUtilities.getEnumerationItems( restrictions );
      return new ComponentUiEnumerationHandler( index, true, true, true, label, SWT.LEFT, DEFAULT_SPACING, spacing, "%s", "<not set>", items ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    if( component.getId().equals( IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS ) )
      return new RoughnessClassUiHandler( index, true, true, true, label, DEFAULT_SPACING, spacing, m_profile );
    else if( component.getId().equals( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS ) )
      return new VegetationClassUiHandler( index, true, true, true, label, DEFAULT_SPACING, spacing, m_profile );
    else if( component.getId().equals( IWspmPointProperties.POINT_PROPERTY_CODE ) )
      return new CodeClassificationClassUiHandler( index, true, true, true, label, DEFAULT_SPACING, spacing, m_profile );
    else if( XmlTypes.XS_DATETIME.equals( valueTypeName ) )
      return new ComponentUiDateHandler( index, true, true, true, label, SWT.NONE, DEFAULT_SPACING, spacing, "%s", "%s", "" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    else if( XmlTypes.XS_STRING.equals( valueTypeName ) )
      return new ComponentUiStringHandler( index, true, true, true, label, SWT.NONE, DEFAULT_SPACING, spacing, "%s", "%s", "" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    else if( XmlTypes.XS_INTEGER.equals( valueTypeName ) )
      return new ComponentUiIntegerHandler( index, true, true, true, label, SWT.NONE, DEFAULT_SPACING, spacing, "%s", "%s", "" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    else if( XmlTypes.XS_DECIMAL.equals( valueTypeName ) )
      return new ComponentUiDecimalHandler( index, true, true, true, label, SWT.RIGHT, DEFAULT_SPACING, spacing, "%.04f", "", "%.04f" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    else if( XmlTypes.XS_DOUBLE.equals( valueTypeName ) )
      return new ComponentUiDoubleHandler( index, true, true, true, label, SWT.RIGHT, DEFAULT_SPACING, spacing, "%.04f", "", "%.04f" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    else if( XmlTypes.XS_BOOLEAN.equals( valueTypeName ) )
      return new ComponentUiBooleanHandler( index, true, true, true, label, SWT.CENTER, DEFAULT_SPACING, spacing, "%b", "", "" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    else if( XmlTypes.XS_STRING.equals( valueTypeName ) )
      return new ComponentUiStringHandler( index, true, true, true, label, SWT.CENTER, DEFAULT_SPACING, spacing, "%b", "", "" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    throw new UnsupportedOperationException();
  }

}
