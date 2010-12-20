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
package org.kalypso.model.wspm.ui.view.chart.layer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.util.ProfilUtil;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.ILayerStyleProvider;
import org.kalypso.model.wspm.ui.view.chart.AbstractProfilTheme;
import org.kalypso.observation.result.IRecord;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;

/**
 * Displays constant wsp lines in the cross section.
 * 
 * @author Gernot Belger
 * @author Holger Albert
 */
public class WspLayer extends AbstractProfilTheme
{
  /**
   * @see org.kalypso.model.wspm.ui.view.chart.AbstractProfilTheme#getTargetRange()
   */
  @Override
  public IDataRange<Number> getTargetRange( IDataRange<Number> domainIntervall )
  {
    Double min = null;
    Double max = null;
    BigDecimal station = ProfilUtil.stationToBigDecimal( getProfil().getStation() );

    try
    {
      if( m_data == null )
        return null;

      for( final Object element : m_data.getActiveElements() )
      {
        /* Search the value. */
        final Double value = getValue( element, station );
        if( min == null || max == null )
        {
          min = value;
          max = value;
        }
        else
        {
          min = Math.min( min, value );
          max = Math.max( max, value );
        }
      }
    }
    catch( Exception e )
    {
      /* Log the error message. */
      KalypsoModelWspmUIPlugin.getDefault().getLog().log( new Status( IStatus.ERROR, KalypsoModelWspmUIPlugin.ID, e.getLocalizedMessage(), e ) );
    }

    if( min == null || Double.isNaN( min ) || max == null || Double.isNaN( max ) )
      return null;

    return new DataRange<Number>( min, max );
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.AbstractProfilTheme#createLegendEntries()
   */
  @Override
  public ILegendEntry[] createLegendEntries( )
  {
    // TODO: get Symbol from file
    final LegendEntry le = new LegendEntry( this, getTitle() )
    {
      @Override
      public void paintSymbol( final GC gc, final Point size )
      {
        final ILineStyle lineStyle = getLineStyle();
        if( lineStyle == null )
          return;

        final PolylineFigure rf = new PolylineFigure();

        rf.setStyle( lineStyle );
        rf.getStyle().setWidth( 2 );
        rf.getStyle().setColor( lineStyle.getColor() );
        final int d = gc.getClipping().height / 3;
        rf.setPoints( new Point[] { new Point( 1, d + 1 ), new Point( gc.getClipping().width - 1, d + 1 ) } );
        rf.paint( gc );
        rf.setPoints( new Point[] { new Point( 1, 2 * d ), new Point( gc.getClipping().width - 1, 2 * d ) } );
        rf.paint( gc );
      }
    };

    return new ILegendEntry[] { le };
  }

  /**
   * The profile.
   */
  private final IProfil m_profil;

  /**
   * The wsp layer data.
   */
  private final IWspLayerData m_data;

  /**
   * True, if the area below the wsp lines should be filled. If there are more than one wsp line, this option should be
   * false, because you could see only the most above line and its area.
   */
  private final boolean m_fill;

  private Color m_color;

  /**
   * The constructor.
   * 
   * @param profile
   *          The profile.
   * @param layerId
   *          The id of the layer.
   * @param styleProvider
   *          The style provider.
   * @param data
   *          The wsp layer data.
   * @param fill
   *          True, if the area below the wsp lines should be filled. If there are more than one wsp line, this option
   *          should be false, because you could see only the most above line and its area.
   */
  public WspLayer( final IProfil profile, final String layerId, final ILayerStyleProvider styleProvider, final IWspLayerData data, final boolean fill, final ICoordinateMapper mapper )
  {
    super( profile, layerId, Messages.getString( "WspLayer.0" ), null, mapper, styleProvider ); //$NON-NLS-1$

    m_profil = profile;
    m_data = data;
    m_fill = fill;
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.AbstractProfilLayer#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_color != null )
      m_color.dispose();

    super.dispose();
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.AbstractProfilLayer#paint(org.eclipse.swt.graphics.GC)
   */
  @Override
  public void paint( final GC gc )
  {
    if( m_color == null )
    {
      final ILineStyle lineStyle = getLineStyle();
      if( lineStyle != null )
      {
        final RGB rgb = lineStyle.getColor();
        m_color = new Color( gc.getDevice(), rgb );
      }
    }

    // HACK: we need to set the background color as we fill a clipped-rectangle in order to create the line
    if( m_color != null )
      gc.setBackground( m_color );

    try
    {
      /* No data. */
      if( m_data == null )
        return;

      /* Get the profile. */
      final IProfil profile = getProfil();
      if( profile == null )
        return;

      /* Get all active names. */
      final Object[] activeElements = m_data.getActiveElements();

      /* Get the station. */
      final BigDecimal station = ProfilUtil.stationToBigDecimal( profile.getStation() );

      /* Paint the values for the active names. */
      for( final Object element : activeElements )
      {
        /* Search the value. */
        final double value = getValue( element, station );
        if( Double.isNaN( value ) )
          continue;

        /* Paint the value. */
        paint( gc, value );
      }
    }
    catch( final Exception ex )
    {
      /* Log the error message. */
      KalypsoModelWspmUIPlugin.getDefault().getLog().log( new Status( IStatus.ERROR, KalypsoModelWspmUIPlugin.ID, ex.getLocalizedMessage(), ex ) );
    }
  }

  private double getValue( final Object element, final BigDecimal station ) throws Exception
  {
    return m_data.searchValue( element, station );
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.AbstractProfilLayer#getHover(org.eclipse.swt.graphics.Point)
   */
  @Override
  public EditInfo getHover( final Point pos )
  {
    try
    {
      /* No data. */
      if( m_data == null )
        return null;

      /* Get the profile. */
      final IProfil profile = getProfil();
      if( profile == null )
        return null;

      /* Get all active names. */
      final Object[] activeElements = m_data.getActiveElements();

      /* Nothing was ever activated or nothing is activated. */
      if( activeElements == null || activeElements.length == 0 )
        return null;

      /* Get the station. */
      final BigDecimal station = ProfilUtil.stationToBigDecimal( profile.getStation() );

      /* Get the domain axis. */
      final IAxis domainAxis = getDomainAxis();

      /* Get the range. */
      final IDataRange<Number> domainRange = domainAxis.getNumericRange();

      /* The x positions. */
      final int x_start = domainAxis.numericToScreen( domainRange.getMin() );
      final int x_end = domainAxis.numericToScreen( domainRange.getMax() );

      /* Get the target axis. */
      final IAxis targetAxis = getTargetAxis();

      /* Search the values for the active names. */
      for( final Object activeElement : activeElements )
      {
        /* Search the value. */
        final double value = getValue( activeElement, station );
        if( Double.isNaN( value ) )
          continue;

        /* The y position. */
        final int y = targetAxis.numericToScreen( value );

        if( pos.y >= y - 5 && pos.y <= y + 5 )
        {
          /* Create a full rectangle figure. */
          final PolylineFigure hoverFigure = new PolylineFigure();
          hoverFigure.setStyle( getLineStyle_hover() );
          hoverFigure.setPoints( new Point[] { new Point( x_start, y ), new Point( x_end, y ) } );

          final String activeLabel = findActiveLabel( activeElement );
          final String format = String.format( "%-12s %-14s%n%-12s %-10.2f [m]", Messages.getString( "org.kalypso.model.wspm.ui.view.chart.layer.WspLayer.1" ), activeLabel, Messages.getString( "org.kalypso.model.wspm.ui.view.chart.layer.WspLayer.2" ), value ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          return new EditInfo( this, hoverFigure, null, null, format, pos );
        }
      }

      return null;
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();

      return null;
    }
  }

  private String findActiveLabel( final Object activeElement )
  {
    Assert.isNotNull( activeElement );

    if( activeElement instanceof IWspLayerDataElement )
      return ((IWspLayerDataElement) activeElement).getLabel();

    return activeElement.toString();
  }

  /**
   * This function paints one wsp line.
   * 
   * @param gc
   *          The graphical context.
   * @param height
   *          The height of the wsp line.
   */
  private void paint( final GC gc, final double height )
  {
    final Rectangle clipping = gc.getClipping();
    final ICoordinateMapper cm = getCoordinateMapper();
    if( cm == null )
      return;
    final Point location = cm.numericToScreen( 0.0, height );

    final Region clipreg = new Region();
    final int[] points = getPoints();

    clipreg.add( points );
    clipreg.intersect( clipping );

    final Rectangle toprect = new Rectangle( clipping.x, location.y - 100000, clipping.width, 100000 );
    clipreg.subtract( toprect );

    /* If not fill, ... */
    if( !m_fill )
    {
      final int linesize = 2;
      final Rectangle bottomrect = new Rectangle( clipping.x, location.y + linesize, clipping.width, 10000 );
      clipreg.subtract( bottomrect );
    }

    gc.setClipping( clipreg );
    gc.fillRectangle( clipping );
    gc.setClipping( clipping );
  }

  /**
   * This function creates the points of the polygon above the line of the cross section.
   * 
   * @return The points.
   */
  private int[] getPoints( )
  {
    /* Create the polygon above the line of the cross section. */
    final List<Point> points = new ArrayList<Point>();

    /* Get the record (points) of the profile. */
    final IRecord[] ppoints = m_profil.getPoints();
    for( int i = 0; i < ppoints.length; i++ )
    {
      /* Get the record. */
      final IRecord record = ppoints[i];

      /* Get x and y from the record. */
      final Double x = ProfilUtil.getDoubleValueFor( IWspmConstants.POINT_PROPERTY_BREITE, record );
      final Double y = ProfilUtil.getDoubleValueFor( IWspmConstants.POINT_PROPERTY_HOEHE, record );

      /* Convert to screen coordinates. */
      final Point point = getCoordinateMapper().numericToScreen( x, y );

      if( i == 0 )
        points.add( new Point( point.x, -1000 ) );

      points.add( point );

      if( i == ppoints.length - 1 )
        points.add( new Point( point.x, -1000 ) );
    }

    /* The array for the screen coordinates. */
    final int[] ps = new int[points.size() * 2];

    /* Add the screen coordinates to the array. */
    int count = 0;
    for( int i = 0; i < points.size(); i++ )
    {
      /* Get the screen coordinates. */
      final Point p = points.get( i );

      /* Add the screen coordinates to the array. */
      ps[count++] = p.x;
      ps[count++] = p.y;
    }

    return ps;
  }

  /**
   * This function returns the wsp layer data.
   * 
   * @return The wsp layer data.
   */
  public IWspLayerData getData( )
  {
    return m_data;
  }
}