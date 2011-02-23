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
package org.kalypso.ogc.gml.mapmodel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
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
public final class MapModellHelper
{
  private MapModellHelper( )
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
    return new WaitForMapOperation( panelOrModell );
  }

  /**
   * This function creates an image of a map model and keeps aspect ratio of the displayed map and its extend.
   * 
   * @param panel
   *          The map panel.
   * @param width
   *          The width of the new image.
   * @param height
   *          The height of the new image.
   * @param insets
   *          The insets of the image define a print border, which is kept empty.
   * @return The image showing the map.
   */
  public static BufferedImage createWellFormedImageFromModel( IMapPanel panel, int width, int height, Insets insets )
  {
    /* The remaining dimensions for the map considering the insets. */
    int mapWidth = width;
    int mapHeight = height;
    if( insets != null )
    {
      /* Calculate the remaining dimensions. */
      mapWidth = mapWidth - insets.left - insets.right;
      mapHeight = mapHeight - insets.top - insets.bottom;
    }

    /* Get the map model. */
    IMapModell mapModel = panel.getMapModell();

    /* Get the bounding box. */
    GM_Envelope boundingBox = panel.getBoundingBox();

    /* Calculate the ratio of the width and height of the available to the map. */
    double ratio = (double) mapHeight / (double) mapWidth;

    /* Adjust the bounding box. */
    GM_Envelope adjustedBoundingBox = MapModellHelper.adjustBoundingBox( mapModel, boundingBox, ratio );

    return MapModellHelper.createImageFromModell( mapModel, width, height, insets, adjustedBoundingBox );
  }

  /**
   * This function is used to create an image of a map model. It does not wait until all themes are loaded. It is used
   * from the map panel as well, where the drawing is done every refresh of the map. So it does not matter, when some
   * themes finish, if they finish at all.
   * 
   * @param panel
   *          The map panel.
   * @param width
   *          The width of the new image.
   * @param height
   *          The height of the new image.
   * @param insets
   *          The insets of the image define a print border, which is kept empty.
   * @param boundingBox
   *          The envelope of the map, which should be exported.
   * @return The image showing the map.
   */
  public static BufferedImage createImageFromModell( IMapModell model, int width, int height, Insets insets, GM_Envelope boundingBox )
  {
    /* If there is no bounding box, we cannot draw the map. */
    if( boundingBox == null )
      return new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );

    /* If the insets are missing, create them with the width of all borders = 0. */
    if( insets == null )
      insets = new Insets( 0, 0, 0, 0 );

    /* Create the image for the map WITH the border. */
    BufferedImage image = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
    Graphics2D gr = (Graphics2D) image.getGraphics();

    /* Calculate the remaining dimensions for the map considering the insets. */
    int mapWidth = width - insets.left - insets.right;
    int mapHeight = height - insets.top - insets.bottom;

    /* Create the image for the map WITHOUT the border. */
    BufferedImage mapImage = new BufferedImage( mapWidth, mapHeight, BufferedImage.TYPE_INT_ARGB );
    Graphics2D mapgr = (Graphics2D) mapImage.getGraphics();

    try
    {
      /* Make the backgrounds completely white. */
      gr.setColor( Color.white );
      gr.fillRect( 0, 0, width, height );
      mapgr.setColor( Color.white );
      mapgr.fillRect( 0, 0, mapWidth, mapHeight );

      /* Set the clips. */
      gr.setColor( Color.black );
      gr.setClip( 0, 0, width, height );
      mapgr.setColor( Color.black );
      mapgr.setClip( 0, 0, mapWidth, mapHeight );

      /* Configure the graphics contexts. */
      gr.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
      gr.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
      mapgr.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
      mapgr.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );

      /* Create the world to screen transform. */
      GeoTransform world2screen = new WorldToScreenTransform();
      world2screen.setSourceRect( boundingBox );
      world2screen.setDestRect( 0, 0, mapWidth, mapHeight, null );

      /* Paint the map. */
      model.paint( mapgr, world2screen, new NullProgressMonitor() );

      /* Draw it onto the image. */
      gr.drawImage( mapImage, insets.left, insets.top, null );

    }
    catch( Exception ex )
    {
      /* Print the stack trace. */
      ex.printStackTrace();
    }
    finally
    {
      /* Dispose the graphics context. */
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
   * @param depth
   *          One of the {@link IKalypsoThemeVisitor#DEPTH_} constants.
   * @param mapModel
   *          This model is searched
   * @param themeProperty
   *          This where this property is set are found
   */
  public static IKalypsoTheme[] findThemeByProperty( final IMapModell mapModel, final String themeProperty, final int depth )
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
    mapModel.accept( visitor, depth );
    return visitor.getFoundThemes();
  }

}
