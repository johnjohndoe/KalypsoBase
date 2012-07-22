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
package org.kalypso.ogc.gml;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

/**
 * Helper class that loads the image for a {@link KalypsoPictureTheme}.
 *
 * @author Gernot Belger
 */
class ImageHolder
{
  private SeekableStream m_stream = null;

  private TiledImage m_image = null;

  private URL m_imageURL;

  public void dispose( )
  {
    setImage( null, null, null );
  }

  public synchronized TiledImage getImage( )
  {
    return m_image;
  }

  private synchronized void setImage( final TiledImage image, final SeekableStream stream, final URL imageUrl )
  {
    if( m_image != null )
      m_image.dispose();

    if( m_stream != null )
      IOUtils.closeQuietly( m_stream );

    m_stream = stream;
    m_imageURL = imageUrl;
    m_image = image;
  }

  public synchronized URL getImageURL( )
  {
    return m_imageURL;
  }

  public IStatus loadImage( final String filePath, final URL context )
  {
    try
    {
      /* prepare for exception */
      setImage( null, null, null );

      // UGLY HACK: replace backslashes with slashes. The add-picture-theme action seems to put backslashes (on windows)
      // in the relative URLs (which is even wrong in windows). Should be fixed there, but is fixed also here to support
      // older projects.
      final String filePathChecked = filePath.replaceAll( "\\\\", "/" ); //$NON-NLS-1$ //$NON-NLS-2$

      final URL imageUrl = UrlResolverSingleton.resolveUrl( context, filePathChecked );

      final Pair<RenderedOp, SeekableStream> pair = openImage( imageUrl );

      final RenderedOp image = pair.getKey();
      final SeekableStream stream = pair.getValue();

      final TiledImage tiledImage = new TiledImage( image, false );

      setImage( tiledImage, stream, imageUrl );

      return Status.OK_STATUS;
    }
    catch( final MalformedURLException e )
    {
      return createStatus( e, Messages.getString( "KalypsoPictureTheme.2" ), filePath ); //$NON-NLS-1$
    }
    catch( final OutOfMemoryError error )
    {
      // REMARK: this will happen if we load big images
      // It is safe to catch it here, as the heap will be freed immediately, if the image could not be loaded
      return createStatus( error, Messages.getString( "KalypsoPictureTheme.3" ), filePath ); //$NON-NLS-1$
    }
    catch( final Throwable error )
    {
      // REMARK: this will happen if we load big images
      // It is safe to catch it here, as the heap will be freed immediately, if the image could not be loaded
      return createStatus( error, Messages.getString( "KalypsoPictureTheme.4" ), filePath ); //$NON-NLS-1$
    }
  }

  private Pair<RenderedOp, SeekableStream> openImage( final URL imageUrl ) throws IOException
  {
    final File imageFile = findLocalFile( imageUrl );

    if( imageFile == null )
    {
      // REMARK: this should actually happen seldom (neither java nor eclipse file); actually this method is
      // buggy and never closes the internally opened stream.
      final RenderedOp image = JAI.create( "url", imageUrl ); //$NON-NLS-1$
      return Pair.of( image, null );
    }
    else
    {
      // REMARK: in case of local file's we can handle the stream ourself's and avoid the JAI bug mentioned above.
      final FileSeekableStream stream = new FileSeekableStream( imageFile );
      final RenderedOp image = JAI.create( "stream", stream ); //$NON-NLS-1$
      return Pair.of( image, null );
    }
  }

  private File findLocalFile( final URL imageUrl )
  {
    final File javaFile = FileUtils.toFile( imageUrl );
    if( javaFile != null )
      return javaFile;

    return ResourceUtilities.findJavaFileFromURL( imageUrl );
  }

  private IStatus createStatus( final Throwable e, final String formatString, final Object... formatArguments )
  {
    final String msg = String.format( formatString, formatArguments );
    return new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), msg, e );
  }
}