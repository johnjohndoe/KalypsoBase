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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.chart.ui.editor.mousehandler.AbstractChartHandler;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.core.profil.wrappers.Profiles;
import org.kalypso.model.wspm.ui.i18n.CommonMessages;
import org.kalypso.model.wspm.ui.view.chart.IProfilChartLayer;
import org.kalypso.observation.result.IInterpolationHandler;
import org.kalypso.observation.result.TupleResult;

import de.openali.odysseus.chart.framework.model.figure.IPaintable;
import de.openali.odysseus.chart.framework.model.figure.impl.PointFigure;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINECAP;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINEJOIN;
import de.openali.odysseus.chart.framework.model.style.impl.LineStyle;
import de.openali.odysseus.chart.framework.model.style.impl.PointStyle;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Kim Werner
 */
public class InsertProfilePointChartHandler extends AbstractChartHandler
{
  private boolean m_canInsert = false;

  private Integer m_p1 = null;

  public InsertProfilePointChartHandler( final IChartComposite chart )
  {
    super( chart );
    super.setCursor( SWT.CURSOR_CROSS );
  }

  private void doPaintMouse( final PaintEvent e )
  {
    final IPaintable figure = getHoverFigure();
    if( Objects.isNotNull( figure ) )
      figure.paint( e.gc );
  }

  @Override
  public void mouseMove( final MouseEvent e )
  {
    super.mouseMove( e );
    m_p1 = null;
    SelectionChartHandlerHelper.updateCursor( getChart(), e.x );
    final Point screen = new Point( e.x, e.y );
    m_canInsert = !isOutOfRange( screen );
    final Point snapped = m_canInsert ? SelectionChartHandlerHelper.snapToScreenPoint( getChart(), screen ) : null;
    if( m_canInsert )
    {
      m_canInsert = snapped == null;
      m_p1 = snapped == null ? e.x : snapped.x;
    }
  }

  @Override
  public void paintControl( final PaintEvent e )
  {
    super.paintControl( e );
    doPaintMouse( e );

  }

  private final IProfileRecord insertPoint( final Double xPosition )
  {
    final IProfilChartLayer theme = SelectionChartHandlerHelper.findProfileTheme( getChart() );
    if( theme == null )
    {
      return null;
    }
    final IProfile profile = theme.getProfil();
    if( profile == null )
      return null;
    final IProfileRecord before = profile.findPreviousPoint( xPosition );
    final IProfileRecord next = profile.findNextPoint( xPosition );
    final TupleResult result = profile.getResult();
    final double distance = (xPosition - before.getBreite()) / (next.getBreite() - before.getBreite());
    final int index = result.indexOf( before );
    final IProfileRecord record = profile.createProfilPoint();
    final IInterpolationHandler interpolation = result.getInterpolationHandler();
    if( interpolation.doInterpolation( result, record, index, distance ) )
    {
      profile.getResult().add( index + 1, record );
      return record;
    }
    return null;
  }

  @Override
  public void mouseUp( final MouseEvent e )
  {
    super.mouseUp( e );

    if( !m_canInsert )
      return;
    final Double xPosition = SelectionChartHandlerHelper.getNumericFromScreen( getChart(), m_p1 );

    /* Ask user */
    final Shell shell = ((Composite)getChart()).getShell();
    final String message = String.format( CommonMessages.INSERT_POINT_CONFIRM, xPosition );
    if( !MessageDialog.openConfirm( shell, CommonMessages.INSERT_POINT_TITLE, message ) )
      return;
    final IProfileRecord record = insertPoint( xPosition );
    if( record != null )
    {
      IRangeSelection selection = SelectionChartHandlerHelper.getSelectionFromChart( getChart() );
      selection.setActivePoints( record );
    }
    m_canInsert = false;
  }

  @SuppressWarnings( "rawtypes" )
  private IPaintable getHoverFigure( )
  {
    if( m_p1 == null )
    {
      return null;
    }
    final IProfilChartLayer theme = SelectionChartHandlerHelper.findProfileTheme( getChart() );
    if( theme == null )
    {
      return null;
    }
    final ICoordinateMapper mapper = theme.getCoordinateMapper();
    final IAxis domainAxis = mapper.getDomainAxis();
    final Double x = domainAxis.screenToNumeric( m_p1 );
    final IProfile profile = theme.getProfil();

    final double hoehe = Profiles.getHoehe( profile, x );
    final RGB rgb = m_canInsert ? new RGB( 0x2F, 0x9b, 0x21 ) : new RGB( 0xFF, 0x0, 0x0 );

    final int y = mapper.getTargetAxis().numericToScreen( hoehe );
    final ILineStyle lineStyle = new LineStyle( 3, rgb, 255, 0F, null, LINEJOIN.MITER, LINECAP.ROUND, 1, true );
    final PointStyle pointStyle = new PointStyle( lineStyle, 9, 9, 255, new RGB( 255, 255, 255 ), true, null, true );
    final PointFigure pointFigure = new PointFigure( pointStyle );
    pointFigure.setStyle( pointStyle );
    pointFigure.setCenterPoint( m_p1, y );

    return pointFigure;
  }
}
