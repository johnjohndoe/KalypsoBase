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
package org.kalypso.model.wspm.ui.commands;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.chart.ui.editor.commandhandler.ChartHandlerUtilities;
import org.kalypso.chart.ui.editor.mousehandler.AbstractChartHandler;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.IWspmLayers;
import org.kalypso.model.wspm.core.profil.wrappers.ProfilePointWrapper;
import org.kalypso.model.wspm.core.profil.wrappers.ProfileWrapper;
import org.kalypso.model.wspm.ui.view.chart.AbstractProfilTheme;
import org.kalypso.observation.result.IInterpolationHandler;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.figure.IPaintable;
import de.openali.odysseus.chart.framework.model.figure.impl.PointFigure;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.manager.visitors.FindLayerVisitor;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINECAP;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINEJOIN;
import de.openali.odysseus.chart.framework.model.style.impl.LineStyle;
import de.openali.odysseus.chart.framework.model.style.impl.PointStyle;
import de.openali.odysseus.chart.framework.util.img.ChartTooltipPainter;
import de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Dirk Kuch
 */
public class InsertProfilePointChartHandler extends AbstractChartHandler
{
  private Double m_breite;

  private ProfilePointWrapper m_point;

  protected ProfileWrapper m_profile;

  public InsertProfilePointChartHandler( final IChartComposite chart )
  {
    super( chart );

    super.setCursor( SWT.CURSOR_CROSS );
  }

  @Override
  public void mouseMove( final MouseEvent e )
  {
    super.mouseMove( e );

    final IChartComposite chart = getChart();
    final Rectangle bounds = chart.getPlotRect();

    final Point position = ChartHandlerUtilities.screen2plotPoint( new Point( e.x, e.y ), bounds );
    if( !isValid( bounds, position ) )
    {
      doReset();

      return;
    }

    final AbstractProfilTheme theme = findProfileTheme( chart );
    final ICoordinateMapper mapper = theme.getCoordinateMapper();

    m_profile = new ProfileWrapper( theme.getProfil() );
    m_breite = mapper.getDomainAxis().screenToNumeric( position.x ).doubleValue();
    m_point = m_profile.hasPoint( m_breite.doubleValue(), 0.1 );

    if( isOutOfInterpolationRange() )
    {
      doReset();
      return;
    }

    if( Objects.isNotNull( m_point ) )
    {
      final StringBuilder builder = new StringBuilder();
      builder.append( "Bestehender Punkt: " );
      builder.append( String.format( "Breite %.2f m, ", m_point.getBreite() ) );
      builder.append( String.format( "Höhe %.2f m", m_point.getHoehe() ) );

      final String msg = builder.toString();
      final Point p = calculatePosition( chart, msg );

      final EditInfo info = new EditInfo( theme, null, null, m_point.getBreite(), msg, p );
      setToolInfo( info );

      setCursor( SWT.CURSOR_ARROW );
    }
    else
    {
      final double hoehe = m_profile.getHoehe( m_breite );
      position.y = mapper.getTargetAxis().numericToScreen( hoehe );

      final StringBuilder builder = new StringBuilder();
      builder.append( "Neuen Punkt einfügen: " );
      builder.append( String.format( "Breite %.2f m, ", m_breite ) );
      builder.append( String.format( "Höhe %.2f m", hoehe ) );

      final String msg = builder.toString();
      final Point p = calculatePosition( chart, msg );

      final EditInfo info = new EditInfo( theme, getHoverFigure( position ), null, m_breite, msg, p );
      setToolInfo( info );

      setCursor( SWT.CURSOR_CROSS );
    }

  }

  private boolean isOutOfInterpolationRange( )
  {
    final ProfilePointWrapper p0 = m_profile.getFirstPoint();
    final ProfilePointWrapper pn = m_profile.getLastPoint();

    if( m_breite < p0.getBreite() )
      return true;
    else if( m_breite > pn.getBreite() )
      return true;

    return false;
  }

  private void doReset( )
  {
    m_profile = null;
    m_breite = null;
    m_point = null;

    setCursor( SWT.CURSOR_ARROW );
  }

  private boolean isValid( final Rectangle bounds, final Point position )
  {
    if( position.x < 0 )
      return false;
    else if( position.x > bounds.width )
      return false;

    return true;
  }

  private IPaintable getHoverFigure( final Point position )
  {
    final PointFigure pointFigure = new PointFigure();

    final ILineStyle lineStyle = new LineStyle( 3, new RGB( 0x2F, 0x9b, 0x21 ), 255, 0F, null, LINEJOIN.MITER, LINECAP.ROUND, 1, true );
    final PointStyle pointStyle = new PointStyle( lineStyle, 9, 9, 255, new RGB( 255, 255, 255 ), true, null, true );

    pointFigure.setStyle( pointStyle );
    pointFigure.setPoints( new Point[] { new Point( position.x, position.y ) } );

    return pointFigure;
  }

  @Override
  public void mouseDown( final MouseEvent e )
  {
    super.mouseDown( e );

    if( Objects.isNull( m_breite, m_profile ) )
      return;

    final ProfilePointWrapper before = m_profile.findPreviousPoint( m_breite );
    final ProfilePointWrapper next = m_profile.findNextPoint( m_breite );
    if( Objects.isNull( before, next ) )
      return;

    final double distance = (m_breite - before.getBreite()) / (next.getBreite() - before.getBreite());

    final TupleResult result = m_profile.getProfile().getResult();
    final IRecord record = result.createRecord();
    final IInterpolationHandler interpolation = result.getInterpolationHandler();

    final int index = result.indexOf( before.getRecord() );
    if( interpolation.doInterpolation( result, record, index, distance ) )
      result.add( index + 1, record );

    final Job job = new Job( "Active point changed" )
    {
      @Override
      protected IStatus run( final IProgressMonitor monitor )
      {
        m_profile.getProfile().setActivePoint( record );
        return Status.OK_STATUS;
      }
    };
    job.setSystem( true );
    job.setUser( false );

    job.schedule();

    m_breite = null;
    m_point = null;
  }

  private Point calculatePosition( final IChartComposite composite, final String msg )
  {
    final Composite c = (Composite) composite;
    final Rectangle bounds = c.getBounds();

    final ChartTooltipPainter painter = getTooltipPainter();

    final IChartLabelRenderer renderer = painter.getLabelRenderer();
    renderer.getTitleTypeBean().setText( msg );

// final Rectangle size = renderer.getSize();

    // FIXME: ask kim

    final int x = -50; // bounds.width - size.width;
    final int y = bounds.y + bounds.height;

    return new Point( x, y );
  }

  private AbstractProfilTheme findProfileTheme( final IChartComposite chart )
  {
    final IChartModel model = chart.getChartModel();

    final FindLayerVisitor visitor = new FindLayerVisitor( IWspmLayers.LAYER_GELAENDE );
    model.getLayerManager().accept( visitor );

    final IChartLayer layer = visitor.getLayer();

    return (AbstractProfilTheme) layer;
  }

  @Override
  public void paintControl( final PaintEvent e )
  {

    super.paintControl( e );
  }
}
