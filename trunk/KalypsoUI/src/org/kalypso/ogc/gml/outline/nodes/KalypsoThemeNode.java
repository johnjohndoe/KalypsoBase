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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.kalypso.commons.command.ICommand;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.catalog.CatalogManager;
import org.kalypso.core.catalog.ICatalog;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.IKalypsoThemeListener;
import org.kalypso.ogc.gml.IKalypsoThemeProvider;
import org.kalypso.ogc.gml.KalypsoThemeAdapter;
import org.kalypso.ogc.gml.command.EnableThemeCommand;
import org.kalypso.ogc.gml.mapmodel.IMapModell;

/**
 * @author Gernot Belger
 */
public class KalypsoThemeNode<T extends IKalypsoTheme> extends AbstractThemeNode<T> implements IKalypsoThemeProvider
{
  private final IKalypsoThemeListener m_themeListener = new KalypsoThemeAdapter()
  {
    /**
     * @see org.kalypso.ogc.gml.KalypsoThemeAdapter#statusChanged(org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void statusChanged( final IKalypsoTheme source )
    {
      handleStatusChanged();
    }

    @Override
    public void visibilityChanged( final IKalypsoTheme source, final boolean newVisibility )
    {
      refreshViewer( KalypsoThemeNode.this );
    }
  };

  private String m_externIconUrn;

  private Image m_externIcon;

  KalypsoThemeNode( final IThemeNode parent, final T theme )
  {
    super( parent, theme );

    theme.addKalypsoThemeListener( m_themeListener );
  }

  protected void handleStatusChanged( )
  {
    refreshViewer( KalypsoThemeNode.this );
  }

  @Override
  public void dispose( )
  {
    getElement().removeKalypsoThemeListener( m_themeListener );

    // REMARK: dispose image in swt thread. Else we might get a racing condition
    // with the re-creation of the image.
    final Image externIcon = m_externIcon;
    final Display display = PlatformUI.getWorkbench().getDisplay();
    if( display != null && !display.isDisposed() )
    {
      display.syncExec( new Runnable()
      {
        @Override
        public void run( )
        {
          if( externIcon != null )
            externIcon.dispose();
        }
      } );
    }

    super.dispose();
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoThemeProvider#getTheme()
   */
  @Override
  public T getTheme( )
  {
    return getElement();
  }

  /**
   * @see org.kalypso.ogc.gml.outline.AbstractThemeNode#getElementChildren()
   */
  @Override
  protected Object[] getElementChildren( )
  {
    return EMPTY_CHILDREN;
  }

  @Override
  public final ImageDescriptor getImageDescriptor( )
  {
    final IKalypsoTheme theme = getElement();

    final IStatus status = theme.getStatus();
    if( !status.isOK() )
      return getIconFromStatus( status );

    return getIcon();
  }

  private final ImageDescriptor getIcon( )
  {
    final IKalypsoTheme theme = getElement();
    final String legendIcon = theme.getLegendIcon();
    setExternIconUrn( legendIcon );

    final ImageDescriptor externalIcon = getExternalIcon();
    if( externalIcon != null )
      return externalIcon;

    return theme.getDefaultIcon();
  }

  private void setExternIconUrn( final String externIconUrn )
  {
    if( ObjectUtils.equals( externIconUrn, m_externIconUrn ) )
      return;

    m_externIconUrn = externIconUrn;
    if( m_externIcon != null )
    {
      m_externIcon.dispose();
      m_externIcon = null;
    }
  }

  // TODO: combine with StatusComposite#getImage
  private ImageDescriptor getIconFromStatus( final IStatus status )
  {
    final ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();

    switch( status.getSeverity() )
    {
      case IStatus.ERROR:
        return sharedImages.getImageDescriptor( "IMG_OBJS_ERROR_PATH" ); //$NON-NLS-1$
      case IStatus.WARNING:
        return sharedImages.getImageDescriptor( "IMG_OBJS_WARNING_PATH" ); //$NON-NLS-1$
      case IStatus.INFO:
        return sharedImages.getImageDescriptor( "IMG_OBJS_INFO_PATH" ); //$NON-NLS-1$
    }

    return null;
  }

  private ImageDescriptor getExternalIcon( )
  {
    /* If the image is disposed; this theme was disposed */
    if( m_externIcon != null && m_externIcon.isDisposed() )
      return null;

    /* If the m_legendIcon string was already evaluated and an image does exist, return this image. */
    if( m_externIcon != null && !m_externIcon.isDisposed() )
      return ImageDescriptor.createFromImage( m_externIcon );

    m_externIcon = createExternalIcon( m_externIconUrn );
    if( m_externIcon == null )
      return getElement().getDefaultIcon();

    return ImageDescriptor.createFromImage( m_externIcon );
  }

  protected Image createExternalIcon( final String externIconUrn )
  {
    if( externIconUrn == null )
      return null;

    /* Resolve the URL. */
    final URL absoluteUrl = getLegendIconURL( externIconUrn );

    /* On error, return the default icon. */
    if( absoluteUrl == null )
      return null;

    /* Create the descriptor. */
    final ImageDescriptor descriptor = ImageDescriptor.createFromURL( absoluteUrl );

    /* Create the Image. */
    return descriptor.createImage();
  }

  /**
   * This function returns the resolved URL for the legend icon or null, if none could be created.
   * 
   * @return The resolved URL for the legend icon or null, if none could be created.
   */
  private URL getLegendIconURL( final String externIconUrn )
  {
    try
    {
      /* A URL or URN was given. */
      if( externIconUrn.startsWith( "urn" ) ) //$NON-NLS-1$
      {
        // search for url
        final CatalogManager catalogManager = KalypsoCorePlugin.getDefault().getCatalogManager();
        final ICatalog baseCatalog = catalogManager.getBaseCatalog();
        if( baseCatalog == null )
          return null;

        final String uri = baseCatalog.resolve( externIconUrn, externIconUrn );
        if( uri == null || uri.equals( externIconUrn ) )
          return null;

        return new URL( uri );
      }

      final URL context = getElement().getContext();
      return UrlResolverSingleton.resolveUrl( context, externIconUrn );
    }
    catch( final MalformedURLException e )
    {
      KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );

      return null;
    }
  }

  @Override
  public String getLabel( )
  {
    final IKalypsoTheme theme = getElement();

    final StringBuffer sb = new StringBuffer();

    sb.append( theme.getLabel() );

    final IStatus status = theme.getStatus();
    if( !status.isOK() )
    {
      sb.append( " - " ); //$NON-NLS-1$
      sb.append( status.getMessage() );
    }

    return sb.toString();
  }

  /**
   * @see org.kalypso.ogc.gml.outline.nodes.AbstractThemeNode#getDescription()
   */
  @Override
  public String getDescription( )
  {
    final IKalypsoTheme theme = getElement();

    final IStatus status = theme.getStatus();
    if( status.isOK() )
      return null;

    return status.getMessage();
  }

  /**
   * @see org.eclipse.jface.viewers.ICheckStateProvider#isChecked(java.lang.Object)
   */
  @Override
  public boolean isChecked( final Object element )
  {
    return getElement().isVisible();
  }

  /**
   * @see org.eclipse.jface.viewers.ICheckStateProvider#isGrayed(java.lang.Object)
   */
  @Override
  public boolean isGrayed( final Object element )
  {
    return false;
  }

  @Override
  public Font getFont( final Object element )
  {
    final IKalypsoTheme theme = getElement();

    final FontRegistry fontRegistry = JFaceResources.getFontRegistry();

    if( !theme.isLoaded() )
      return fontRegistry.getItalic( JFaceResources.DIALOG_FONT );

    // falls aktiviert
    final IMapModell mapModell = theme.getMapModell();
    if( mapModell != null && mapModell.getActiveTheme() == theme )
      return fontRegistry.getBold( JFaceResources.DIALOG_FONT );

    return fontRegistry.get( JFaceResources.DIALOG_FONT );
  }

  /**
   * @see org.kalypso.ogc.gml.outline.AbstractThemeNode#setVisible(boolean)
   */
  @Override
  public ICommand setVisible( final boolean visible )
  {
    final IKalypsoTheme theme = getElement();

    if( theme.isVisible() == visible )
      return null;

    return new EnableThemeCommand( theme, visible );
  }
}
