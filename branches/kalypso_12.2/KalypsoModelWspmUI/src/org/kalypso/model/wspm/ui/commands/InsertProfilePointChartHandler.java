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
package org.kalypso.model.wspm.ui.commands;

import org.apache.commons.lang3.Range;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.visitors.FindClosestPointVisitor;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.core.profil.wrappers.Profiles;
import org.kalypso.model.wspm.ui.i18n.CommonMessages;
import org.kalypso.model.wspm.ui.view.chart.AbstractProfilTheme;
import org.kalypso.observation.result.IInterpolationHandler;
import org.kalypso.observation.result.TupleResult;

import de.openali.odysseus.chart.framework.model.figure.IPaintable;
import de.openali.odysseus.chart.framework.model.figure.impl.PointFigure;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINECAP;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINEJOIN;
import de.openali.odysseus.chart.framework.model.style.impl.LineStyle;
import de.openali.odysseus.chart.framework.model.style.impl.PointStyle;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Dirk Kuch
 */
public class InsertProfilePointChartHandler extends AbstractProfilePointHandler
{
  private boolean m_doMouseDown;

  public InsertProfilePointChartHandler( final IChartComposite chart )
  {
    super( chart );

    super.setCursor( SWT.CURSOR_CROSS );
  }

  @Override
  protected void doMouseMove( final AbstractProfilTheme theme, final Point position )
  {
    final ICoordinateMapper mapper = theme.getCoordinateMapper();

    if( isSnapPoint( theme, position.x ) )
    {
      m_doMouseDown = false;
      setToolInfo( null );

      setCursor( SWT.CURSOR_ARROW );
    }
    else
    {
      m_doMouseDown = true;

      final double hoehe = Profiles.getHoehe( getProfile(), getBreite() );
      position.y = mapper.getTargetAxis().numericToScreen( hoehe );

      final String msg = String.format( CommonMessages.INSERT_POINT_TOOLTIP, getBreite(), hoehe ); //$NON-NLS-1$

      // TODO: hide tooltip if mouse is not in chart

      final EditInfo info = new EditInfo( theme, null, null, getBreite(), msg, new Point( position.x + 5, position.y + 45 ) );
      setToolInfo( info );

      setCursor( SWT.CURSOR_CROSS );
    }

    final IRangeSelection selection = getProfile().getSelection();
    selection.setCursor( getBreite() );
  }

  private boolean isSnapPoint( final AbstractProfilTheme theme, final int screenX )
  {
    final ICoordinateMapper mapper = theme.getCoordinateMapper();
    final IAxis domainAxis = mapper.getDomainAxis();

    final Number xPosition = domainAxis.screenToNumeric( screenX );
    final Number xMin = domainAxis.screenToNumeric( screenX - 5 );
    final Number xMax = domainAxis.screenToNumeric( screenX + 5 );

    final FindClosestPointVisitor visitor = new FindClosestPointVisitor( xPosition.doubleValue() );
    getProfile().accept( visitor, 1 );

    final IProfileRecord point = visitor.getPoint();
    final Range<Double> range = Range.between( xMin.doubleValue(), xMax.doubleValue() );

    if( range.contains( point.getBreite() ) )
      return true;

    return false;
  }

  @Override
  public void mouseDown( final MouseEvent e )
  {
    super.mouseDown( e );

    if( !m_doMouseDown )
      return;

    final Double cursor = getBreite();
    if( Objects.isNull( cursor, getProfile() ) )
      return;

    final IProfileRecord before = getProfile().findPreviousPoint( cursor );
    final IProfileRecord next = getProfile().findNextPoint( cursor );
    if( Objects.isNull( before, next ) )
      return;

    /* Ask user */
    final Shell shell = ((Composite) getChart()).getShell();
    final String message = String.format( CommonMessages.INSERT_POINT_CONFIRM, cursor );
    if( !MessageDialog.openConfirm( shell, CommonMessages.INSERT_POINT_TITLE, message ) )
      return;

    final double distance = (cursor - before.getBreite()) / (next.getBreite() - before.getBreite());

    final TupleResult result = getProfile().getResult();
    final IProfileRecord record = getProfile().createProfilPoint();
    final IInterpolationHandler interpolation = result.getInterpolationHandler();

    final int index = result.indexOf( before.getRecord() );
    if( interpolation.doInterpolation( result, record, index, distance ) )
      result.add( index + 1, record.getRecord() );

    final Job job = new Job( "Active point changed" ) //$NON-NLS-1$
    {
      @Override
      protected IStatus run( final IProgressMonitor monitor )
      {
        getProfile().getSelection().setRange( record );
        return Status.OK_STATUS;
      }
    };
    job.setSystem( true );
    job.setUser( false );

    job.schedule();

    setBreite( null );
  }

  @Override
  public void paintControl( final PaintEvent e )
  {
    super.paintControl( e );

    final IProfil profile = getProfile();
    if( Objects.isNull( profile ) )
      return;

    doPaintCursor( e, profile );
  }

  private void doPaintCursor( final PaintEvent e, final IProfil profile )
  {
    final IRangeSelection selection = profile.getSelection();
    final Double cursor = selection.getCursor();
    if( Objects.isNull( cursor ) || Double.isNaN( cursor ) )
      return;

    final IChartComposite chart = getChart();
    final AbstractProfilTheme theme = findProfileTheme( chart );
    if( Objects.isNull( theme ) )
      return;

    final IPaintable figure = getHoverFigure( theme, cursor );
    if( Objects.isNotNull( figure ) )
      figure.paint( e.gc );
  }

  private IPaintable getHoverFigure( final AbstractProfilTheme theme, final Double cursor )
  {
    final double hoehe = Profiles.getHoehe( getProfile(), cursor );

    final ICoordinateMapper mapper = theme.getCoordinateMapper();
    final IAxis domainAxis = mapper.getDomainAxis();

    final int x = domainAxis.numericToScreen( cursor );
    final int y = mapper.getTargetAxis().numericToScreen( hoehe );

    if( isOutOfRange( x ) )
      return null;

    final PointFigure pointFigure = new PointFigure();

    final ILineStyle lineStyle = new LineStyle( 3, new RGB( 0x2F, 0x9b, 0x21 ), 255, 0F, null, LINEJOIN.MITER, LINECAP.ROUND, 1, true );
    final PointStyle pointStyle = new PointStyle( lineStyle, 9, 9, 255, new RGB( 255, 255, 255 ), true, null, true );

    pointFigure.setStyle( pointStyle );
    pointFigure.setPoints( new Point[] { new Point( x, y ) } );

    return pointFigure;
  }

}
