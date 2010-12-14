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
package org.kalypso.contribs.eclipse.jobs;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.kalypso.contribs.eclipse.EclipseRCPContributionsPlugin;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;

/**
 * A {@link Job} that paints a IPaintable onto a {@link BufferedImage}.<br>
 * Its own paint method, paints the current state of the {@link BufferedImage}.<br>
 *
 * @author Gernot Belger
 */
public class BufferPaintJob extends Job
{
  /**
   * Call-back interface for the thing that really gets painted by this buffer-painter.
   */
  public static interface IPaintable
  {
    Point getSize( );

    void paint( Graphics2D g, IProgressMonitor monitor ) throws CoreException;
  }

  private final IPaintable m_paintable;

  private BufferedImage m_image = null;

  private final ImageCache m_imageCache;

  public BufferPaintJob( final IPaintable paintable, final ImageCache imageCache )
  {
    super( "" );

    m_paintable = paintable;
    m_imageCache = imageCache;

    if( m_paintable != null )
      setName( m_paintable.toString() );
  }

  /**
   * Cancels the job and releases the buffered image.
   */
  public void dispose( )
  {
    cancel();

    synchronized( this )
    {
      if( m_image != null )
      {
        m_imageCache.release( m_image );
        m_image = null;
      }
    }
  }

  public IPaintable getPaintable( )
  {
    return m_paintable;
  }

  /**
   * Returns the current state of the buffered image.
   *
   * @return The buffered image; <code>null</code>, if the job has not yet started.
   */
  public synchronized BufferedImage getImage( )
  {
    return m_image;
  }

  /**
   * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus run( final IProgressMonitor monitor )
  {
    // System.out.println("Paint job running");

    if( m_paintable == null )
    {
      //System.out.println("BufferPaintJob: paintable was null");
      return Status.OK_STATUS;
    }

    final SubMonitor progress = SubMonitor.convert( monitor, "Painting buffer", 100 ); //$NON-NLS-1$

    Graphics2D gr = null;
    try
    {
      progress.subTask( "Initializing buffer-image" );

      final Point size = m_paintable.getSize();
      if( size.x > 0 && size.y > 0 )
      {
        gr = createGraphics( size );
        // if image is null, workbench is probably shutting down,
        // just return without comment
        if( gr == null )
        {
          //    System.out.println("BufferPaintJob: image was null");
          return Status.OK_STATUS;
        }

        ProgressUtilities.worked( progress, 10 );

        m_paintable.paint( gr, progress.newChild( 90, SubMonitor.SUPPRESS_NONE ) );
      }
    }
    catch( final CoreException ce )
    {
      final IStatus status = ce.getStatus();
      if( status.matches( IStatus.CANCEL ) )
        return status;

      EclipseRCPContributionsPlugin.getDefault().getLog().log( ce.getStatus() );

      // REMARK: We translate every error to an warning, to avoid the error-dlg popup.
      // Especially for buffered layers this is needed, as we can have multiple thread running at once, producing lots
      // of error output
      if( status.matches( IStatus.ERROR ) )
        return StatusUtilities.cloneStatus( status, IStatus.WARNING );

      return ce.getStatus();
    }
    catch( final Throwable t )
    {
      return StatusUtilities.createStatus( IStatus.ERROR, "Failed to paint buffer image", t );
    }
    finally
    {
      if( gr != null )
        gr.dispose();

      monitor.done();
    }

    return Status.OK_STATUS;
  }

  private synchronized Graphics2D createGraphics( final Point size )
  {
    /* Only recreate image, if width/height does not fit any more */
    if( m_image != null && m_image.getWidth() != size.x && m_image.getHeight() != size.y )
    {
      m_imageCache.release( m_image );
      m_image = null;
    }

    if( m_image == null )
      ceateImage( size );

    final Graphics2D gr = m_image.createGraphics();
    if( gr == null )
      return null;

    configureGraphics( gr );

    return gr;
  }

  private void ceateImage( final Point size )
  {
    m_image = m_imageCache.akquire( size );
  }

  /**
   * Configures the graphics-context before actual painting is started (i.e.
   * {@link IPaintable#paint(Graphics2D, IProgressMonitor)} is called).<br>
   * Default behaviour is to set activate anti-aliasing (normal and text).<br>
   * Overwrite to change.
   */
  private void configureGraphics( final Graphics2D gr )
  {
    gr.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
    gr.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
  }
}
