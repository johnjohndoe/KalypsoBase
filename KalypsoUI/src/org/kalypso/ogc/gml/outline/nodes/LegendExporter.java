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
package org.kalypso.ogc.gml.outline.nodes;

import java.awt.Insets;
import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * Helper for creating legend images for themes.
 * 
 * @author Gernot Belger
 */
public class LegendExporter
{
  /**
   * This function exports the legend of the given themes to a file. It seems that it has to be run in an UI-Thread.
   * 
   * @param themes
   *          The themes to export.
   * @param file
   *          The file, where it should save to.
   * @param format
   *          The image format (for example: SWT.IMAGE_PNG).
   * @param device
   *          The device.
   * @param insets
   *          Defines the size of an empty border around the image. Can be <code>null</code>.
   * @param sizeWidth
   *          The width of the image, the legend is drawn onto.<br>
   *          If one of sizeWidth or sizeHeight is <=0 the width and height of the image is determined automatically.
   * @param sizeHeight
   *          The height of the image, the legend is drawn onto.<br>
   *          If one of sizeWidth or sizeHeight is <=0 the width and height of the image is determined automatically.
   * @param onlyVisible
   *          True, if only visible theme nodes should be asked.
   * @param monitor
   *          A progress monitor.
   * @return A status, containing information about the process.
   */
  public IStatus exportLegends( final IThemeNode[] nodes, final File file, final int format, final Device device, Insets insets, final Point size, final boolean onlyVisible, final IProgressMonitor monitor )
  {
    /* Monitor. */
    final SubMonitor progress = SubMonitor.convert( monitor, Messages.getString( "org.kalypso.ogc.gml.map.utilities.MapUtilities.0" ), 150 ); //$NON-NLS-1$

    /* The legend image. */
    Image image = null;

    try
    {
      /* Set default insets, if none are given. */
      if( insets == null )
        insets = new Insets( 5, 5, 5, 5 );

      /* white */
      final RGB background = new RGB( 255, 255, 255 );

      /* Create the legend image. */
      final NodeLegendBuilder legendBuilder = new NodeLegendBuilder( null, onlyVisible );
      legendBuilder.setBackground( background );
      legendBuilder.setInsets( insets );
      legendBuilder.setFixedSize( size );

      image = legendBuilder.createLegend( nodes, device, progress.newChild( 50 ) );
      if( image == null )
        return new Status( IStatus.WARNING, KalypsoGisPlugin.PLUGIN_ID, Messages.getString( "LegendExporter.0" ) ); //$NON-NLS-1$

      /* Monitor. */
      ProgressUtilities.worked( progress, 50 );

      /* Save the image. */
      final ImageLoader imageLoader = new ImageLoader();
      imageLoader.data = new ImageData[] { image.getImageData() };
      imageLoader.save( file.toString(), format );

      /* Monitor. */
      ProgressUtilities.worked( monitor, 50 );

      return Status.OK_STATUS;
    }
    catch( final Exception ex )
    {
      return new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), ex.getLocalizedMessage(), ex );
    }
    finally
    {
      if( image != null )
        image.dispose();

      /* Monitor. */
      progress.done();
    }
  }
}