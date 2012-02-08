/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.zml.core.base;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.zml.obslink.TimeseriesLinkType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.gml.binding.commons.NamedFeatureHelper;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathException;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;

/**
 * Holds properties for a timeseries-feature. This class is used as a struct.
 * 
 * @author schlienger
 */
public final class TimeserieFeatureProperty
{
  public static final String TS_TYPE_TABLE = "table"; //$NON-NLS-1$

  public static final String TS_TYPE_DIAGRAM = "diag"; //$NON-NLS-1$

  public static final String TS_TYPE_RADIO = "radio"; //$NON-NLS-1$

  /** If set, use this property of the feature to create name */
  public static final String PROP_NAMECOLUMN = "nameColumn"; //$NON-NLS-1$

  /** If {@link #PROP_NAMECOLUMN}is not set, use this name instead */
  public static final String PROP_NAMESTRING = "nameString"; //$NON-NLS-1$

  public static final String PROP_LINKCOLUM = "linkColumn"; //$NON-NLS-1$

  public static final String PROP_COLOR = "color"; //$NON-NLS-1$

  /** Line width as float */
  public static final String PROP_LINE_WIDTH = "lineWidth"; //$NON-NLS-1$

  /** length of simple dash as float */
  public static final String PROP_LINE_DASH = "lineDash"; //$NON-NLS-1$

  public static final String PROP_EDITABLE = "editable"; //$NON-NLS-1$

  public static final String PROP_SHOW_LEGEND = "showLegend"; //$NON-NLS-1$

  public static final String PROP_TS_TYPE = "visible"; //$NON-NLS-1$

  public static final String DEFAULT_TS_TYPE = TS_TYPE_TABLE + "," + TS_TYPE_DIAGRAM + "," + TS_TYPE_RADIO; //$NON-NLS-1$

  public static final String DEFAULT_NAMESTRING = "%obsname% (%axisname%)"; //$NON-NLS-1$

  public static final String PROP_FILTER = "filter"; //$NON-NLS-1$

  public static final String PROP_ID = "id"; //$NON-NLS-1$

  private final Set<String> m_visibleTypes = new HashSet<String>();

  private final String m_nameColumn;

  private final String m_linkColumn;

  private final String m_filter;

  private final String m_nameString;

  private final String m_color;

  private final String m_lineWidth;

  private final String m_lineDash;

  private final boolean m_editable;

  private final boolean m_showLegend;

  private final String m_id;

  public TimeserieFeatureProperty( final String id, final String nameColumn, final String nameString, final String linkColumn, final String filter, final String color, final String lineWidth, final String lineDash, final boolean editable, final String[] visibleTypes, final boolean showLegend )
  {
    m_id = id;
    m_nameColumn = nameColumn;
    m_color = color;
    m_lineWidth = lineWidth;
    m_lineDash = lineDash;
    m_editable = editable;
    m_nameString = nameString;
    m_linkColumn = linkColumn;
    m_filter = filter;
    m_showLegend = showLegend;

    m_visibleTypes.addAll( Arrays.asList( visibleTypes ) );
  }

  public String getId( )
  {
    return m_id;
  }

  public String getFilter( )
  {
    return m_filter;
  }

  public String getLinkColumn( )
  {
    return m_linkColumn;
  }

  public String getNameColumn( )
  {
    return m_nameColumn;
  }

  public String getNameString( )
  {
    return m_nameString;
  }

  public String getColor( )
  {
    return m_color;
  }

  public String getLineWidth( )
  {
    return m_lineWidth;
  }

  public String getLineDash( )
  {
    return m_lineDash;
  }

  public boolean isEditable( )
  {
    return m_editable;
  }

  public String getName( final Feature feature )
  {
    final String fname = getNameProperty( feature );

    final String name = getNameString();
    if( fname == null )
      return name;

    return name.replaceAll( "%featureprop%", fname ); //$NON-NLS-1$
  }

  private String getNameProperty( final Feature feature )
  {
    final String nameColumn = getNameColumn();
    if( nameColumn == null )
      return null;

    final IPropertyType nameProp = feature.getFeatureType().getProperty( nameColumn );
    if( nameProp == null )
      return null;

    if( Feature.QN_NAME.equals( nameProp.getQName() ) )
      return NamedFeatureHelper.getName( feature );

    final Object value = feature.getProperty( nameColumn );
    if( value != null )
      return value.toString();

    return null;
  }

  public TimeseriesLinkType getLink( final Feature feature )
  {
    try
    {
      final String linkProperty = getLinkColumn();
      final GMLXPath xPath = new GMLXPath( linkProperty, null );
      final Object value = GMLXPathUtilities.query( xPath, feature );
      if( value instanceof TimeseriesLinkType )
        return (TimeseriesLinkType) value;
    }
    catch( final GMLXPathException e )
    {
      e.printStackTrace();
    }

    return null;
  }

  public boolean isVisible( final String type )
  {
    return m_visibleTypes.contains( type );
  }

  @Override
  public String toString( )
  {
    return ToStringBuilder.reflectionToString( this );
  }

  public boolean getShowLegend( )
  {
    return m_showLegend;
  }
}