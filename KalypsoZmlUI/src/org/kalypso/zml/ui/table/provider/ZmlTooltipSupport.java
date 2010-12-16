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
package org.kalypso.zml.ui.table.provider;

import org.eclipse.swt.graphics.Image;
import org.kalypso.commons.java.util.StringUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.zml.core.table.model.ZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.core.table.schema.DataColumnType;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.provider.strategy.ExtendedZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlTooltipSupport
{
  private static final Image IMG = new Image( null, ZmlLabelProvider.class.getResourceAsStream( "icons/help_about_48.png" ) );

  private final ExtendedZmlTableColumn m_column;

  public ZmlTooltipSupport( final ExtendedZmlTableColumn column )
  {
    m_column = column;
  }

  public String getToolTipText( final ZmlModelRow row )
  {
    final AbstractColumnType type = m_column.getColumnType().getType();
    if( type instanceof DataColumnType )
    {
      final IZmlValueReference reference = row.get( type );

      return getDataTooltip( (DataColumnType) type, reference );
    }

    return null;
  }

  private String getDataTooltip( final DataColumnType column, final IZmlValueReference reference )
  {
    final StringBuffer buffer = new StringBuffer();

    try
    {
      final Object value = reference.getValue();
      final Integer status = reference.getStatus();
      final String source = reference.getDataSource();

      final String href = reference.getHref();

      if( value instanceof Number )
        buffer.append( buildInfoText( "Wert", value.toString() ) );

      if( status != null )
        buffer.append( buildInfoText( "Status", getStatus( status ) ) );

      if( source != null )
        buffer.append( buildInfoText( "Modellquelle", source ) );

      if( href != null )
        buffer.append( buildInfoText( "Lokale Quelle", href ) );
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    final String indexAxis = column.getIndexAxis();
    final String valueAxis = column.getValueAxis();

    buffer.append( "\n" );

    if( indexAxis != null )
      buffer.append( buildInfoText( "Indexachse", indexAxis ) );

    if( valueAxis != null )
      buffer.append( buildInfoText( "Datenachse", valueAxis ) );

    return StringUtilities.chop( buffer.toString() );
  }

  private String getStatus( final Integer status )
  {
    if( KalypsoStati.BIT_OK == status )
      return "Stützstelle";
    else if( KalypsoStati.BIT_CHECK == status )
      return "Gewarnt";
    else if( KalypsoStati.BIT_USER_MODIFIED == status )
      return "Benutzereingabe";

    return "Unbekannt";
  }

  private Object buildInfoText( final String label, final String value )
  {
    String tabs;
    if( label.length() > 8 )
      tabs = "\t";
    else
      tabs = "\t\t";

    String v;
    if( value.length() > 60 )
    {
      v = value.subSequence( 0, 60 ) + "\n\t\t" + value.substring( 60 );
    }
    else
      v = value;

    return String.format( "%s:%s%s\n", label, tabs, v );
  }

  public Image getToolTipImage( )
  {
    if( m_column.getColumnType().getType() instanceof DataColumnType )
      return IMG;

    return null;
  }

}
