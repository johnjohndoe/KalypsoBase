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
package org.kalypso.ogc.gml.featureview.control;

import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCoreExtensions;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.featureview.control.comparators.IViewerComparator;
import org.kalypso.ogc.gml.featureview.control.filters.IViewerFilter;
import org.kalypso.template.featureview.Combo;
import org.kalypso.template.featureview.Combo.Entry;
import org.kalypso.template.featureview.Combo.Filter;
import org.kalypso.template.featureview.Combo.Sorter;
import org.kalypso.template.featureview.ControlType;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Gernot Belger
 */
public class ComboFeatureControlFactory implements IFeatureControlFactory
{
  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureControlFactory#createFeatureControl(org.kalypso.ogc.gml.featureview.control.IFeatureComposite,
   *      org.kalypsodeegree.model.feature.Feature, org.kalypso.gmlschema.property.IPropertyType,
   *      org.kalypso.template.featureview.ControlType, org.kalypso.gmlschema.annotation.IAnnotation)
   */
  @Override
  public IFeatureControl createFeatureControl( final IFeatureComposite parentComposite, final Feature feature, final IPropertyType pt, final ControlType controlType, final IAnnotation annotation )
  {
    final Combo comboType = (Combo) controlType;

    final Sorter sorter = comboType.getSorter();
    final ViewerComparator comparator = createComparator( feature, sorter );

    final Filter filter = comboType.getFilter();
    final ViewerFilter viewerFilter = createFilter( filter, feature );

    /* Handle the entries. */
    final List<Entry> entryList = comboType.getEntry();
    final Map<Object, String> comboEntries = createComboEntries( pt, entryList );

    return new ComboFeatureControl( feature, pt, comboEntries, comparator, viewerFilter );
  }

  private Map<Object, String> createComboEntries( final IPropertyType pt, final List<Entry> entryList )
  {
    final Map<Object, String> comboEntries = new LinkedHashMap<Object, String>( entryList.size() );

    if( pt instanceof IValuePropertyType )
    {
      final ITypeRegistry<IMarshallingTypeHandler> typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
      final IMarshallingTypeHandler typeHandler = typeRegistry.getTypeHandlerFor( pt );

      for( final Entry entry : entryList )
      {
        final String label = entry.getLabel();
        final String any = entry.getValue();
        try
        {
          final Object object = typeHandler.parseType( any );
          comboEntries.put( object, label );
        }
        catch( final ParseException e )
        {
          final IStatus status = StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.ogc.gml.featureview.control.FeatureComposite.parse" ) + any ); //$NON-NLS-1$
          KalypsoGisPlugin.getDefault().getLog().log( status );
        }
      }
    }

    return comboEntries;
  }

  private ViewerComparator createComparator( final Feature feature, final Sorter sorter )
  {
    if( sorter == null )
      return null;

    /* The id of the sorter. */
    final String id = getSorterId( sorter );

    try
    {
      final ViewerComparator comparator = KalypsoCoreExtensions.createComparator( id );
      initComparator( feature, sorter, comparator );
      return comparator;
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      return null;
    }

  }

  private void initComparator( final Feature feature, final Sorter sorter, final ViewerComparator comparator )
  {
    if( comparator instanceof IViewerComparator )
    {
      /* The parameter map. */
      final Map<String, String> params = new HashMap<String, String>();

      /* Get all parameter. */
      final List<org.kalypso.template.featureview.Combo.Sorter.Param> parameter = sorter.getParam();
      if( parameter != null )
      {
        /* Collect all parameter. */
        for( final org.kalypso.template.featureview.Combo.Sorter.Param param : parameter )
          params.put( param.getName(), param.getValue() );
      }

      ((IViewerComparator) comparator).init( feature, params );
    }
  }

  private String getSorterId( final Sorter sorter )
  {
    final String id = sorter.getId();
    if( id == null || id.length() == 0 )
      return "org.kalypso.ui.featureview.comparators.defaultComparator"; //$NON-NLS-1$
    return id;
  }

  private ViewerFilter createFilter( final Filter filter, final Feature feature )
  {
    /* If there is a filter, look deeper. */
    if( filter == null )
      return null;

    final String id = getFilterId( filter );

    try
    {
      final ViewerFilter viewerFilter = KalypsoCoreExtensions.createViewerFilter( id );
      ((IViewerFilter) viewerFilter).init( feature, filter.getExpression() );
      return viewerFilter;
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      return null;
    }
  }

  private String getFilterId( final Filter filter )
  {
    final String id = filter.getId();
    if( id == null || id.length() == 0 )
      return "org.kalypso.ui.featureview.filters.defaultFilter"; //$NON-NLS-1$
    return id;
  }

}
