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
package org.kalypso.model.wspm.ui.view.chart.layer.wsp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.kalypso.commons.java.lang.Arrays;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.ui.plugin.AbstractUIPluginExt;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.util.ProfilUtil;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.ILayerStyleProvider;
import org.kalypso.model.wspm.ui.view.IProfilView;
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
  private Color m_color;

  /**
   * The wsp layer data.
   */
  private final IWspLayerData m_data;

  /**
   * True, if the area below the wsp lines should be filled. If there are more than one wsp line, this option should be
   * false, because you could see only the most above line and its area.
   */
  private final boolean m_fill;

  /**
   * The profile.
   */
  private final IProfil m_profile;

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

    m_profile = profile;
    m_data = data;
    m_fill = fill;
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.AbstractProfilTheme#createLegendEntries()
   */
  @Override
  public ILegendEntry[] createLegendEntries( )
  {
    final ILineStyle lineStyle = getLineStyle();

    // TODO: get Symbol from file
    final LegendEntry le = new LegendEntry( this, getTitle() )
    {
      @Override
      public void paintSymbol( final GC gc, final Point size )
      {
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
   * @see org.kalypso.model.wspm.ui.view.chart.AbstractProfilLayer#createLayerPanel()
   */
  @Override
  public IProfilView createLayerPanel( )
  {
    return new WspPanel( this );
  }

  public IProfil getProfile( )
  {
    return m_profile;
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.AbstractProfilLayer#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_color != null )
    {
      m_color.dispose();
    }

    super.dispose();
  }

  private String findActiveLabel( final Object activeElement )
  {
    Assert.isNotNull( activeElement );

    if( activeElement instanceof IWspLayerDataElement )
      return ((IWspLayerDataElement) activeElement).getLabel();

    return activeElement.toString();
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

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.AbstractProfilLayer#getHover(org.eclipse.swt.graphics.Point)
   */
  @Override
  public EditInfo getHover( final Point pos )
  {
    try
    {
      /* Get the profile. */
      final IProfil profile = getProfil();
      if( Objects.isNull( m_data, profile ) )
        return null;

      /* Get all active names. */
      final Object[] activeElements = m_data.getActiveElements();
      if( Arrays.isEmpty( activeElements ) )
        return null;

      /* Get the station. */
      final BigDecimal station = ProfilUtil.stationToBigDecimal( profile.getStation() );

      final IAxis domainAxis = getDomainAxis();
      final IAxis targetAxis = getTargetAxis();
      final IDataRange<Number> domainRange = domainAxis.getNumericRange();

      /* The x positions. */
      final int xStart = domainAxis.numericToScreen( domainRange.getMin() );
      final int xEnd = domainAxis.numericToScreen( domainRange.getMax() );

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
          hoverFigure.setStyle( getLineStyleHover() );
          hoverFigure.setPoints( new Point[] { new Point( xStart, y ), new Point( xEnd, y ) } );

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
    }

    return null;
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
    final IRecord[] ppoints = m_profile.getPoints();

    for( final IRecord record : ppoints )
    {
      /* Get x and y from the record. */
      final double x = ProfilUtil.getDoubleValueFor( IWspmConstants.POINT_PROPERTY_BREITE, record );
      final double y = ProfilUtil.getDoubleValueFor( IWspmConstants.POINT_PROPERTY_HOEHE, record );
      if( !Double.isNaN( x ) && !Double.isNaN( y ) )
      {
        /* Convert to screen coordinates. */
        final Point point = getCoordinateMapper().numericToScreen( x, y );
        points.add( point );
      }
    }

    if( points.size() < 2 )
      return null;

    /* At last/first point far away */
    points.add( 0, new Point( points.get( 0 ).x, -10000 ) );
    points.add( new Point( points.get( points.size() - 1 ).x, -10000 ) );

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
   * @see org.kalypso.model.wspm.ui.view.chart.AbstractProfilTheme#getTargetRange()
   */
  @Override
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
  {
    if( m_data == null )
      return null;

    Double min = null;
    Double max = null;
    final BigDecimal station = ProfilUtil.stationToBigDecimal( getProfil().getStation() );

    try
    {
      for( final Object element : m_data.getActiveElements() )
      {
        /* Search the value. */
        final double value = getValue( element, station );
        if( Double.isNaN( value ) )
          continue;

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
    catch( final Exception e )
    {
      /* Log the error message. */
      KalypsoModelWspmUIPlugin.getDefault().getLog().log( new Status( IStatus.ERROR, AbstractUIPluginExt.ID, e.getLocalizedMessage(), e ) );
    }

    if( min == null || Double.isNaN( min ) || max == null || Double.isNaN( max ) )
      return null;

    return new DataRange<Number>( min, max );
  }

  private double getValue( final Object element, final BigDecimal station ) throws Exception
  {
    return m_data.searchValue( element, station );
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.AbstractProfilLayer#paint(org.eclipse.swt.graphics.GC)
   */
  @Override
  public void paint( final GC gc )
  {
    if( Objects.isNull( m_data ) )
      return;

    if( Objects.isNull( m_color ) )
    {
      final ILineStyle lineStyle = getLineStyle();
      if( Objects.isNotNull( lineStyle ) )
      {
        final RGB rgb = lineStyle.getColor();
        m_color = new Color( gc.getDevice(), rgb );
      }
    }

    // HACK: we need to set the background color as we fill a clipped-rectangle in order to create the line
    if( Objects.isNotNull( m_color ) )
      gc.setBackground( m_color );

    try
    {
      /* Get the profile. */
      final IProfil profile = getProfil();
      if( Objects.isNull( profile ) )
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
      KalypsoModelWspmUIPlugin.getDefault().getLog().log( new Status( IStatus.ERROR, AbstractUIPluginExt.ID, ex.getLocalizedMessage(), ex ) );
    }
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
    final ICoordinateMapper cm = getCoordinateMapper();
    if( cm == null )
      return;

    final Point location = cm.numericToScreen( 0.0, height );

    final Rectangle clipping = gc.getClipping();
    final Region region = new Region();
    final int[] points = getPoints();
    if( ArrayUtils.isEmpty( points ) )
      return;

    region.add( points );
    region.intersect( clipping );

    final Rectangle toprect = new Rectangle( clipping.x, location.y - 100000, clipping.width, 100000 );
    region.subtract( toprect );

    /* If not fill, ... */
    if( !m_fill )
    {
      final int linesize = 2;
      final Rectangle bottomrect = new Rectangle( clipping.x, location.y + linesize, clipping.width, 10000 );
      region.subtract( bottomrect );
    }

    gc.setClipping( region );
    gc.fillRectangle( clipping );
    gc.setClipping( clipping );
  }
}