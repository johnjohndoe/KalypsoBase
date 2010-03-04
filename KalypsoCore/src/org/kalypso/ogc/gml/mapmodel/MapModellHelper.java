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
package org.kalypso.ogc.gml.mapmodel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.IKalypsoThemeFilter;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.mapmodel.visitor.KalypsoThemeLoadStatusVisitor;
import org.kalypso.ogc.gml.mapmodel.visitor.KalypsoThemeVisitor;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.graphics.transformation.WorldToScreenTransform;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * Utility class for {@link IMapModell} associated functions.
 * 
 * @author Gernot Belger
 */
public class MapModellHelper
{
  public MapModellHelper( )
  {
    throw new UnsupportedOperationException( Messages.getString( "org.kalypso.ogc.gml.mapmodel.MapModellHelper.0" ) ); //$NON-NLS-1$
  }

  /**
   * Waits for a {@link MapPanel} to be completely loaded. A progress dialog opens if this operation takes long.<br>
   * If an error occurs, an error dialog will be shown.
   * 
   * @param panelOrModell
   *          An {@link IMapPanel} or an {@link IMapModell}. Use a panel, if the modell is stil about to be loaded. Use
   *          a modell, if you do not have a panel (i.e. for image export or similar).
   * @return <code>false</code> if any error happened, the map is not guaranteed to be loaded in this case.
   * @see ProgressUtilities#busyCursorWhile(ICoreRunnableWithProgress)
   * @see #createWaitForMapOperation(MapPanel)
   */
  public static boolean waitForAndErrorDialog( final Shell shell, final Object panelOrModell, final String windowTitle, final String message )
  {
    final ICoreRunnableWithProgress operation = createWaitForMapOperation( panelOrModell );
    final IStatus waitErrorStatus = ProgressUtilities.busyCursorWhile( operation );
    ErrorDialog.openError( shell, windowTitle, message, waitErrorStatus );
    return waitErrorStatus.isOK();
  }

  /**
   * Creates an {@link ICoreRunnableWithProgress} which waits for a {@link MapPanel} to be loaded.<br>
   * Uses the {@link IMapModell#isLoaded()} and {@link IKalypsoTheme#isLoaded()} methods.
   * 
   * @param panelOrModell
   *          An {@link IMapPanel} or an {@link IMapModell}. Use a panel, if the modell is stil about to be loaded. Use
   *          a modell, if you do not have a panel (i.e. for image export or similar).
   */
  public static ICoreRunnableWithProgress createWaitForMapOperation( final Object panelOrModell )
  {
    final ICoreRunnableWithProgress waitForMapOperation = new ICoreRunnableWithProgress()
    {
      public IStatus execute( final IProgressMonitor monitor ) throws InterruptedException
      {
        monitor.beginTask( Messages.getString( "org.kalypso.ogc.gml.mapmodel.MapModellHelper.1" ), IProgressMonitor.UNKNOWN ); //$NON-NLS-1$

        Thread.sleep( 250 );

        while( true )
        {
          if( monitor.isCanceled() )
            return Status.CANCEL_STATUS;

          try
          {
            IMapModell modell;
            if( panelOrModell instanceof IMapPanel )
              modell = ((IMapPanel) panelOrModell).getMapModell();
            else if( panelOrModell instanceof IMapModell )
              modell = (IMapModell) panelOrModell;
            else
              throw new IllegalArgumentException();

            if( isMapLoaded( modell ) )
              return Status.OK_STATUS;

            Thread.sleep( 250 );

            monitor.worked( 10 );
          }
          catch( final InterruptedException e )
          {
            return StatusUtilities.statusFromThrowable( e );
          }
        }
      }
    };
    return waitForMapOperation;
  }

  /**
   * Create an image of a map model and keep aspection ration of displayed map and its extend
   */
  public static BufferedImage createWellFormedImageFromModel( final IMapPanel panel, final int width, final int height )
  {
    final IMapModell mapModell = panel.getMapModell();
    final GM_Envelope bbox = panel.getBoundingBox();

    final double ratio = (double) height / (double) width;
    final GM_Envelope boundingBox = MapModellHelper.adjustBoundingBox( mapModell, bbox, ratio );

    final Rectangle bounds = new Rectangle( width, height );

    return MapModellHelper.createImageFromModell( boundingBox, bounds, bounds.width, bounds.height, mapModell );
  }

  /**
   * Is used to create an image of a map model. Does not wait until all themes are loaded. Is used from the map panel as
   * well, where the drawing is done every refresh of the map. So it does not matter, when some themes finish, if they
   * finish at last.
   */
  public static BufferedImage createImageFromModell( final GM_Envelope bbox, final Rectangle bounds, final int width, final int height, final IMapModell model )
  {
    final BufferedImage image = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
    final Graphics2D gr = (Graphics2D) image.getGraphics();
    try
    {
      gr.setColor( Color.white );
      gr.fillRect( 0, 0, width, height );
      gr.setColor( Color.black );
      gr.setClip( 0, 0, width, height );

      gr.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
      gr.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );

      final int x = bounds.x;
      final int y = bounds.y;
      final int w = bounds.width;
      final int h = bounds.height;

      final GeoTransform world2screen = new WorldToScreenTransform();
      world2screen.setSourceRect( bbox );
      world2screen.setDestRect( x, y, w + x, h + y, null );

      try
      {
        model.paint( gr, world2screen, new NullProgressMonitor() );
      }
      catch( final Exception e )
      {
        e.printStackTrace();
      }
    }
    finally
    {
      gr.dispose();
    }

    return image;
  }

  public static IKalypsoTheme[] filterThemes( final IMapModell modell, final IKalypsoThemeFilter filter )
  {
    final IKalypsoTheme[] allThemes = modell.getAllThemes();
    final List<IKalypsoTheme> themes = new ArrayList<IKalypsoTheme>( allThemes.length );
    for( final IKalypsoTheme theme : allThemes )
      if( filter.accept( theme ) )
        themes.add( theme );

    return themes.toArray( new IKalypsoTheme[themes.size()] );
  }

  /**
   * Calculates the common extent of all given themes.
   * 
   * @param predicate
   *          If not <code>null</code>, only themes applying to the predicate are considered.
   * @return <code>null</code>, if the array of themes is empty or null.
   */
  public static GM_Envelope calculateExtent( final IKalypsoTheme[] themes, final IKalypsoThemePredicate predicate )
  {
    if( themes == null )
      return null;

    GM_Envelope result = null;
    for( final IKalypsoTheme kalypsoTheme : themes )
    {
      if( (predicate == null) || predicate.decide( kalypsoTheme ) )
      {
        final GM_Envelope boundingBox = kalypsoTheme.getFullExtent();
        if( result == null )
          result = boundingBox;
        else
          result = result.getMerged( boundingBox );
      }
    }

    return result;
  }

  /**
   * Adjust an given bounding box (env) to an new ratio
   */
  public static GM_Envelope adjustBoundingBox( final IMapModell model, GM_Envelope env, final double ratio )
  {
    if( env == null )
      env = model == null ? null : model.getFullExtentBoundingBox();

    if( env == null )
      return null;

    if( Double.isNaN( ratio ) )
      return env;

    final double minX = env.getMin().getX();
    final double minY = env.getMin().getY();

    final double maxX = env.getMax().getX();
    final double maxY = env.getMax().getY();

    double dx = (maxX - minX) / 2d;
    double dy = (maxY - minY) / 2d;

    if( dx * ratio > dy )
      dy = dx * ratio;
    else
      dx = dy / ratio;

    final double mx = (maxX + minX) / 2d;
    final double my = (maxY + minY) / 2d;

    return GeometryFactory.createGM_Envelope( mx - dx, my - dy, mx + dx, my + dy, env.getCoordinateSystem() );
  }

  /**
   * Tests if a given map-model is fully loaded.<br>
   * REMARK: this only checks, that all its themes (and sub-themes) return <code>true</code> for its isLoaded methods<br>
   * Themes may also report <code>true</code>, if loading its data has failed.
   */
  public static boolean isMapLoaded( final IMapModell model )
  {
    if( model == null || !model.isLoaded() )
      return false;

    final KalypsoThemeLoadStatusVisitor visitor = new KalypsoThemeLoadStatusVisitor();
    model.accept( visitor, FeatureVisitor.DEPTH_INFINITE );

    return visitor.isLoaded();
  }

  /**
   * Finds all themes with the given theme property from the map model.
   * 
   *@param depth
   *          One of the {@link IKalypsoThemeVisitor#DEPTH_} constants.
   * @param mapModell
   *          This model is searched
   * @param themeProperty
   *          This where this property is set are found
   */
  public static final IKalypsoTheme[] findThemeByProperty( final IMapModell mapModell, final String themeProperty, final int depth )
  {
    final IKalypsoThemePredicate predicate = new IKalypsoThemePredicate()
    {
      @Override
      public boolean decide( final IKalypsoTheme theme )
      {
        final String property = theme.getProperty( themeProperty, null );
        return property != null;
      }
    };

    final KalypsoThemeVisitor visitor = new KalypsoThemeVisitor( predicate );
    mapModell.accept( visitor, depth );
    return visitor.getFoundThemes();
  }

}
