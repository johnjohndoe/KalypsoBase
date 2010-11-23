/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.gml;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.contribs.eclipse.core.runtime.SafeRunnable;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCoreExtensions;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * Abstract implementation of IKalypsoTheme<br>
 * Implements common features to all KalypsoTheme's
 * 
 * @author Gernot Belger
 */
public abstract class AbstractKalypsoTheme extends PlatformObject implements IKalypsoTheme
{
  private static interface IListenerRunnable
  {
    void visit( final IKalypsoThemeListener l );
  }

  protected static final Object[] EMPTY_CHILDREN = new Object[] {};

  protected static final IStatus PAINT_STATUS = StatusUtilities.createStatus( IStatus.INFO, Messages.getString( "org.kalypso.ogc.gml.AbstractKalypsoTheme.0" ), null ); //$NON-NLS-1$

  private final Collection<IKalypsoThemeListener> m_listeners = new HashSet<IKalypsoThemeListener>();

  private final Map<String, String> m_properties = Collections.synchronizedMap( new HashMap<String, String>() );

  private final IMapModell m_mapModel;

  /**
   * Stores the relative URL or an URN for an icon, which can be used for the layer in a legend. May be null.
   */
  private String m_externIconUrn = null;

  /**
   * The context, if the theme is part of a template loaded from a file. May be null. Used to resolve dependend
   * resources like the legend-icon.
   */
  private URL m_context = null;

  private I10nString m_name;

  private String m_type;

  /**
   * The status of this theme. Should be set of implementing classes whenever something unexpected occurs (e.g. error
   * while loading the theme, ...).
   */
  private IStatus m_status = Status.OK_STATUS;

  private boolean m_isVisible = true;

  /**
   * Holds the descriptor for the default icon of all themes. Is used in legends, such as the outline.
   */
  private org.eclipse.swt.graphics.Image m_standardThemeIcon;

  /**
   * True, if the theme should show its children in an outline. Otherwise false.
   */
  private boolean m_showLegendChildren = true;

  /**
   * The constructor.
   * 
   * @param name
   *          The name of the theme.
   * @param type
   *          The type of the theme.
   * @param mapModel
   *          The map model to use.
   */
  public AbstractKalypsoTheme( final I10nString name, final String type, final IMapModell mapModel )
  {
    Assert.isNotNull( mapModel );

    m_name = name;
    m_type = type;
    m_mapModel = mapModel;

    /* Initialize properties */
    // deleteable defaults to 'true', because this was the old behavior
    m_properties.put( IKalypsoTheme.PROPERTY_DELETEABLE, Boolean.toString( true ) );
  }

  /**
   * Runns the given runnable on every listener in a safe way.
   */
  private void acceptListenersRunnable( final IListenerRunnable r )
  {
    final IKalypsoThemeListener[] listeners = m_listeners.toArray( new IKalypsoThemeListener[m_listeners.size()] );
    for( final IKalypsoThemeListener l : listeners )
    {
      final ISafeRunnable code = new SafeRunnable()
      {
        @Override
        public void run( ) throws Exception
        {
          r.visit( l );
        }
      };

      SafeRunner.run( code );
    }
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#addKalypsoThemeListener(org.kalypso.ogc.gml.IKalypsoThemeListener)
   */
  @Override
  public void addKalypsoThemeListener( final IKalypsoThemeListener listener )
  {
    Assert.isNotNull( listener );

    m_listeners.add( listener );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#dispose()
   */
  @Override
  public void dispose( )
  {
    m_listeners.clear();

    if( m_standardThemeIcon != null )
    {
      m_standardThemeIcon.dispose();
      m_standardThemeIcon = null;
    }
  }

  /**
   * Fire the given event to my registered listeners.
   */
  protected void fireContextChanged( )
  {
    acceptListenersRunnable( new IListenerRunnable()
    {
      @Override
      public void visit( final IKalypsoThemeListener l )
      {
        if( l == null )
          return;

        l.contextChanged( AbstractKalypsoTheme.this );
      }
    } );
  }

  /**
   * Fire the given event to my registered listeners.
   */
  protected void fireStatusChanged( final IKalypsoTheme theme )
  {
    // TODO: this is also used to fire events for child-themes
    // we should get the child-theme as parameter and give this instead of myself to the statusChanged event

    final IKalypsoTheme changedTheme = theme == null ? this : theme;

    acceptListenersRunnable( new IListenerRunnable()
    {
      @Override
      public void visit( final IKalypsoThemeListener l )
      {
        if( l == null )
          return;

        l.statusChanged( changedTheme );
      }
    } );
  }

  /**
   * Fire the given event to my registered listeners.
   */
  protected void fireVisibilityChanged( final boolean newVisibility )
  {
    acceptListenersRunnable( new IListenerRunnable()
    {
      @Override
      public void visit( final IKalypsoThemeListener l )
      {
        if( l == null )
          return;

        l.visibilityChanged( AbstractKalypsoTheme.this, newVisibility );
      }
    } );
  }

  /**
   * Fire the given event to my registered listeners.<br>
   * 
   * @param invalidExtent
   *          The extent that is no more valid; <code>null</code> indicating that the complete theme should be
   *          repainted.
   */
  protected void fireRepaintRequested( final GM_Envelope invalidExtent )
  {
    final IKalypsoTheme theme = AbstractKalypsoTheme.this;
    acceptListenersRunnable( new IListenerRunnable()
    {
      @Override
      public void visit( final IKalypsoThemeListener l )
      {
        if( l == null )
          return;

        l.repaintRequested( theme, invalidExtent );
      }
    } );
  }

  /**
   * Returns the type of the theme by default. Override if needed.
   * 
   * @see org.kalypso.ogc.gml.IKalypsoTheme#getContext()
   */
  @Override
  public String getTypeContext( )
  {
    return getType();
  }

  /**
   * This function should return the default image descriptor representing the theme. <br>
   * Subclasses should override, if they want an own standard icon for representing them.<br>
   * <strong>Note:</strong><br>
   * <br>
   * This has only an effect, if the user does not define an URL or URN and the theme has more then one style or rule.
   * 
   * @return The default image descriptor.
   */
  @Override
  public ImageDescriptor getDefaultIcon( )
  {
    // FIXME: might be called after theme was already disposed... the image is then never disposed....
    // Maybe we need to introduce a flag ot know if the theme is already disposed.
    if( m_standardThemeIcon == null )
      m_standardThemeIcon = new Image( Display.getCurrent(), AbstractKalypsoTheme.class.getResourceAsStream( "resources/standardTheme.gif" ) ); //$NON-NLS-1$

    return ImageDescriptor.createFromImage( m_standardThemeIcon );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#getLabel()
   */
  @Override
  public String getLabel( )
  {
    return getName().getValue();
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#getMapModell()
   */
  @Override
  public IMapModell getMapModell( )
  {
    return m_mapModel;
  }

  @Override
  public I10nString getName( )
  {
    return m_name;
  }

  /**
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
   */
  public Object getParent( final Object o )
  {
    Assert.isTrue( o == this );

    return m_mapModel.getThemeParent( this );
  }

  /**
   * @return <code>defaultValue</code>, if the requested property is not set.
   * @see org.kalypso.ogc.gml.IKalypsoTheme#getProperty(java.lang.String, java.lang.String)
   */
  @Override
  public String getProperty( final String name, final String defaultValue )
  {
    if( !m_properties.containsKey( name ) )
      return defaultValue;

    return m_properties.get( name );
  }

  /**
   * Return the names of all known properties.
   */
  public String[] getPropertyNames( )
  {
    return m_properties.keySet().toArray( new String[m_properties.keySet().size()] );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#getStatus()
   */
  @Override
  public IStatus getStatus( )
  {
    return m_status;
  }

  @Override
  public String getType( )
  {
    return m_type;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#isLoaded()
   */
  @Override
  public boolean isLoaded( )
  {
    return true;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#isVisible()
   */
  @Override
  public boolean isVisible( )
  {
    return m_isVisible;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#removeKalypsoThemeListener(org.kalypso.ogc.gml.IKalypsoThemeListener)
   */
  @Override
  public void removeKalypsoThemeListener( final IKalypsoThemeListener listener )
  {
    m_listeners.remove( listener );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#setName(org.kalypso.contribs.java.lang.I10nString)
   */
  @Override
  public void setName( final I10nString name )
  {
    m_name = name;

    fireStatusChanged( this );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#setProperty(java.lang.String, java.lang.String)
   */
  @Override
  public void setProperty( final String name, final String value )
  {
    m_properties.put( name, value );

    // REMARK: we use status changed at the moment, maybe we should fire a special event for properties?
    fireStatusChanged( this );
  }

  public void setStatus( final IStatus status )
  {
    // Do not fire change if status did not change really; else we may get endless loop
    if( ObjectUtils.equals( status.getSeverity(), m_status.getSeverity() ) && ObjectUtils.equals( status.getMessage(), m_status.getMessage() ) )
      return;

    m_status = status;

    fireStatusChanged( this );
  }

  public void setType( final String type )
  {
    m_type = type;

    fireStatusChanged( this );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#setVisible(boolean)
   */
  @Override
  public void setVisible( final boolean visible )
  {
    if( visible != m_isVisible )
    {
      m_isVisible = visible;
      fireVisibilityChanged( visible );
    }
  }

  @Override
  public String toString( )
  {
    return getLabel();
  }

  /**
   * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
   */
  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    if( adapter == IKalypsoThemeInfo.class )
    {
      /* If an explicit info is configured for this map, use it */
      final String themeInfoId = getProperty( IKalypsoTheme.PROPERTY_THEME_INFO_ID, null );
      if( themeInfoId != null )
        return KalypsoCoreExtensions.createThemeInfo( themeInfoId, this );
    }

    return super.getAdapter( adapter );
  }

  /**
   * This function returns the URL or URN defined by the user for an icon, which should be displayed in a legend or an
   * outline.
   * 
   * @return The URL or URN string. May be null.
   */
  @Override
  public String getLegendIcon( )
  {
    return m_externIconUrn;
  }

  protected void setContext( final URL context )
  {
    m_context = context;
  }

  @Override
  public void setLegendIcon( final String legendIcon, final URL context )
  {
    if( ObjectUtils.equals( m_externIconUrn, legendIcon ) && ObjectUtils.equals( m_context, context ) )
      return;

    m_externIconUrn = legendIcon;
    m_context = context;

    fireStatusChanged( this );
  }

  /**
   * This function returns the context.
   * 
   * @return The context, if the theme is part of a template loaded from a file. May be null.
   */
  @Override
  public URL getContext( )
  {
    return m_context;
  }

  /**
   * This function returns true, if the theme allows showing its children in an outline. Otherwise, it will return
   * false.
   * 
   * @return True,if the theme allows showing its children in an outline. Otherwise, false.
   */
  @Override
  public boolean shouldShowLegendChildren( )
  {
    return m_showLegendChildren;
  }

  public void setShowLegendChildren( final boolean showChildren )
  {
    if( ObjectUtils.equals( m_showLegendChildren, showChildren ) )
      return;

    m_showLegendChildren = showChildren;

    fireStatusChanged( this );
  }
}