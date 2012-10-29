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
package org.kalypso.model.wspm.ui.view.chart;

import java.awt.geom.Point2D;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.changes.ProfileChangeHint;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.ui.view.ILayerStyleProvider;
import org.kalypso.observation.result.ComponentUtilities;

import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener.ContentChangeType;
import de.openali.odysseus.chart.framework.model.figure.IPaintable;
import de.openali.odysseus.chart.framework.model.figure.impl.PointFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.util.img.ChartImageInfo;

/**
 * @author kimwerner
 */
public class PointsLineLayer extends AbstractProfilePointsLayer
{
  public PointsLineLayer( final String id, final IProfile profil, final String targetRangeProperty, final ILayerStyleProvider styleProvider )
  {
    super( id, profil, targetRangeProperty, styleProvider );

    setData( IProfilChartLayer.VIEW_DATA_KEY, IProfilChartLayer.ALLOW_VERTICAL_EDITING );
  }

  @Override
  public EditInfo drag( final Point newPos, final EditInfo dragStartData )
  {
    if( dragStartData.getPosition() == null )
      return null;

    final Point newPoint = verifyPos( dragStartData.getPosition(), newPos );
    final Integer index = (Integer)dragStartData.getData();

    final Point next = toScreen( getNextNonNull( index ) );
    final Point previous = toScreen( getPreviousNonNull( index ) );

    final PolylineFigure lineFigure = new PolylineFigure();
    lineFigure.setPoints( new Point[] { previous, newPoint, next } );
    lineFigure.setStyle( getLineStyleHover() );

    final PointFigure pointFigure = new PointFigure();

    pointFigure.setStyle( getPointStyleHover() );
    pointFigure.setPoints( new Point[] { newPoint } );

    final IPaintable dragFigure = new IPaintable()
    {

      @Override
      public void paint( final GC gc )
      {
        lineFigure.paint( gc );
        pointFigure.paint( gc );

      }
    };

    final Point2D point = ProfilLayerUtils.toNumeric( getCoordinateMapper(), newPoint );

    return new EditInfo( this, null, dragFigure, dragStartData.getData(), String.format( TOOLTIP_FORMAT, new Object[] { getDomainComponent().getName(), point.getX(), getTargetComponent().getName(),
        point.getY(), ComponentUtilities.getComponentUnitLabel( getTargetComponent() ) } ), dragStartData.getPosition() );
  }

  private IProfileRecord getNextNonNull( final int index )
  {
    final IProfileRecord[] points = getProfil().getPoints();
    final int prop = getProfil().indexOfProperty( getTargetProperty() );
    for( int i = index + 1; i < points.length; i++ )
    {

      if( points[i] != null && points[i].getValue( prop ) != null )
        return points[i];
    }
    return points[index];
  }

  private IProfileRecord getPreviousNonNull( final int index )
  {
    final IProfileRecord[] points = getProfil().getPoints();
    final int prop = getProfil().indexOfProperty( getTargetProperty() );
    for( int i = index - 1; i > -1; i-- )
    {
      if( points[i] != null && points[i].getValue( prop ) != null )
        return points[i];
    }

    return points[index];
  }

  @Override
  public void executeDrop( final Point point, final EditInfo dragStartData )
  {
    if( dragStartData.getPosition() == null )
      return;

    final Point newPoint = verifyPos( dragStartData.getPosition(), point );
    final Integer pos = dragStartData.getData() instanceof Integer ? (Integer)dragStartData.getData() : -1;
    if( pos > -1 )
    {
      final IProfile profil = getProfil();
      final IProfileRecord profilPoint = profil.getPoint( pos );
      final Integer hoehe = profil.indexOfProperty( getTargetComponent() );
      // final Integer breite = profil.indexOfProperty( getDomainComponent() );
      final ICoordinateMapper cm = getCoordinateMapper();

      // Object editMode = getData( IProfilChartLayer.VIEW_DATA_KEY );
      // if( editMode == IProfilChartLayer.ALLOW_VERTICAL_EDITING )

      // final Double x = cm.getDomainAxis().screenToNumeric( newPoint.x ).doubleValue();
      // profilPoint.setValue( breite, x );

      final Double y = cm.getTargetAxis().screenToNumeric( newPoint.y ).doubleValue();
      profilPoint.setValue( hoehe, y );

      profil.getSelection().setActivePoints( profilPoint );

      getEventHandler().fireLayerContentChanged( this, ContentChangeType.value );
    }
  }

  @Override
  public EditInfo getHover( final Point pos )
  {
    if( !isVisible() )
      return null;

    final ProfilePointHover helper = new ProfilePointHover( this, getPointStyleHover() );
    return helper.getHover( pos );
  }

  @Override
  public synchronized ILegendEntry[] getLegendEntries( )
  {
    final LegendEntry le = new LegendEntry( this, toString() )
    {
      @Override
      public void paintSymbol( final GC gc, final Point size )
      {
        final Rectangle clipping = gc.getClipping();

        final PolylineFigure figure = new PolylineFigure();
        figure.setStyle( getLineStyle() );
        final Point[] path = new Point[6];
        path[0] = new Point( 0, clipping.width / 2 );
        path[1] = new Point( clipping.width / 5, clipping.height / 2 );
        path[2] = new Point( clipping.width / 5 * 2, clipping.height / 4 );
        path[3] = new Point( clipping.width / 5 * 3, clipping.height / 4 * 3 );
        path[4] = new Point( clipping.width / 5 * 4, clipping.height / 2 );
        path[5] = new Point( clipping.width, clipping.height / 2 );
        figure.setPoints( path );
        figure.paint( gc );
      }
    };

    return new ILegendEntry[] { le };
  }

  @Override
  public void onProfilChanged( final ProfileChangeHint hint )
  {
    if( hint.isPointsChanged() || hint.isPointValuesChanged() || hint.isSelectionChanged() )
    {
      getEventHandler().fireLayerContentChanged( this, ContentChangeType.value );
    }
  }

  @Override
  public void paint( final GC gc, final ChartImageInfo chartImageInfo, final IProgressMonitor monitor )
  {
    final IProfile profil = getProfil();
    if( profil == null )
      return;

    /** differ between selected and plain (not selected) points */
    final Set<Point> plain = new LinkedHashSet<>();
    final Set<Point> selectedPoints = new LinkedHashSet<>();
    final Set<Point> selectedLinePoints = new LinkedHashSet<>();

    final IProfileRecord[] points = profil.getPoints();
    for( final IProfileRecord point : points )
    {
      final Point screen = toScreen( point );
      if( Objects.isNull( screen ) )
        continue;

      if( point.isSelected() )
      {
        selectedPoints.add( screen );
        selectedLinePoints.add( screen );

        /** draw profile segment as activated, too */
        final IProfileRecord next = point.getNextPoint();
        if( Objects.isNotNull( next ) && !next.isSelected() )
        {
          final Point nextScreen = toScreen( next );
          selectedLinePoints.add( nextScreen );
        }

      }

      plain.add( screen );
    }

    final PolylineFigure lineFigure = new PolylineFigure();
    lineFigure.setStyle( getLineStyle() );
    lineFigure.setPoints( plain.toArray( new Point[] {} ) );
    lineFigure.paint( gc );

    if( !selectedLinePoints.isEmpty() )
    {
      lineFigure.setStyle( getLineStyleActive() );
      lineFigure.setPoints( selectedLinePoints.toArray( new Point[] {} ) );
      lineFigure.paint( gc );
    }

    final PointFigure pointFigure = new PointFigure();
    pointFigure.setStyle( getPointStyle() );
    pointFigure.setPoints( plain.toArray( new Point[] {} ) );
    pointFigure.paint( gc );

    if( !selectedPoints.isEmpty() )
    {
      pointFigure.setStyle( getPointStyleActive() );
      pointFigure.setPoints( selectedPoints.toArray( new Point[] {} ) );
      pointFigure.paint( gc );
    }
  }

  private Point verifyPos( final Point oldPos, final Point newPos )
  {
    final Object o = getData( IProfilChartLayer.VIEW_DATA_KEY );
    if( o != null )
    {
      try
      {
        final int i = Integer.valueOf( o.toString() );
        if( (i & 2) == 0 )
        {
          newPos.y = oldPos.y;
        }
        if( (i & 1) == 0 )
        {
          newPos.x = oldPos.x;
        }
      }
      catch( final NumberFormatException e )
      {
        return oldPos;
      }
    }
    return newPos;
  }
}
