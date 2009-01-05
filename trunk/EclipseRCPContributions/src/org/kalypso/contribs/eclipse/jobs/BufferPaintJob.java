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

import java.awt.Color;
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

  private Color m_bgColor = null;

  public BufferPaintJob( final IPaintable paintable )
  {
    super( "" );

    m_paintable = paintable;

    if( m_paintable != null )
      setName( m_paintable.toString() );
  }

  /**
   * Cancels the job and releases the buffered image.
   */
  public void dispose( )
  {
    cancel();

    if( m_image != null )
    {
      m_image.flush();
      m_image = null;
    }
  }

  public IPaintable getPaintable( )
  {
    return m_paintable;
  }

  public Color getBackgroundColor( )
  {
    return m_bgColor;
  }

  /**
   * Sets the background colour for this image. If non <code>null</code>, the image will be filled with the given colour
   * before paint.
   */
  public void setBackgroundColor( final Color bgColor )
  {
    m_bgColor = bgColor;
  }

  /**
   * Returns the current state of the buffered image.
   *
   * @return The buffered image; <code>null</code>, if the job has not yet started.
   */
  public BufferedImage getImage( )
  {
    return m_image;
  }

  /**
   * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus run( final IProgressMonitor monitor )
  {
    if( m_paintable == null )
      return Status.OK_STATUS;

    final SubMonitor progress = SubMonitor.convert( monitor, "Painting buffer", 100 ); //$NON-NLS-1$

    Graphics2D gr = null;
    try
    {
      progress.subTask( "Initializing buffer-image" );

      final Point size = m_paintable.getSize();
      final int width = size.x;
      final int height = size.y;
      if( width > 0 && height > 0 )
      {
        gr = createGraphics( width, height );
        // if image is null, workbench is probably shutting down,
        // just return without comment
        if( gr == null )
          return Status.OK_STATUS;

        if( m_bgColor != null )
        {
          gr.setBackground( m_bgColor );
          gr.fillRect( 0, 0, width, height );
        }

        ProgressUtilities.worked( progress, 10 );

        m_paintable.paint( gr, progress.newChild( 90 ) );
      }
    }
    catch( final CoreException ce )
    {
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

  private Graphics2D createGraphics( final int width, final int height )
  {
    /* Only recreate image, if width/height does not fit any more */
    if( m_image != null && (m_image.getWidth() != width || m_image.getHeight() != height) )
    {
      m_image.flush();
      m_image = null;
    }

    if( m_image == null )
      m_image = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );

    if( m_image == null )
      return null;

    final Graphics2D gr = m_image.createGraphics();
    if( gr == null )
      return null;

    configureGraphics( gr );

    return gr;
  }

  /**
   * Configures the graphics-context before actual painting is started (i.e.
   * {@link IPaintable#paint(Graphics2D, IProgressMonitor)} is called).<br>
   * Default behaviour is to set activate anti-aliasing (normal and text).<br>
   * Overwrite to change.
   */
  protected void configureGraphics( final Graphics2D gr )
  {
    gr.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
    gr.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
  }
}
