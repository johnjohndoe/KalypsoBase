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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.kalypso.contribs.eclipse.core.runtime.SafeRunnable;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.util.pool.IPoolListener;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.core.util.pool.KeyComparator;
import org.kalypso.core.util.pool.KeyInfo;
import org.kalypso.core.util.pool.PoolableObjectType;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.i18n.Messages;
import org.kalypso.loader.ILoader;
import org.kalypso.loader.LoaderException;
import org.kalypso.ogc.gml.loader.SldLoader;
import org.kalypso.template.types.StyledLayerType.Style;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.StyledLayerDescriptor;
import org.kalypsodeegree.graphics.sld.UserStyle;
import org.kalypsodeegree.xml.Marshallable;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * Wrapped UserStyle to provide fireModellEvent Method
 *
 * @author doemming
 */
public class GisTemplateUserStyle implements IKalypsoUserStyle, Marshallable, IPoolListener
{
  private final Collection<IKalypsoUserStyleListener> m_listeners = new HashSet<IKalypsoUserStyleListener>();

  protected final String m_styleName;

  private boolean m_disposed = false;

  protected UserStyle m_userStyle;

  /** Flag, if this style is used for selected features or not. */
  private final boolean m_isUsedForSelection;

  private final PoolableObjectType m_styleKey;

  private boolean m_loaded = false;

  private boolean m_dirty;

  private ResourceBundle m_resourceBundle = null;

  public GisTemplateUserStyle( final PoolableObjectType poolableStyleKey, final String styleName, final boolean usedForSelection )
  {
    m_userStyle = createDummyStyle( Messages.getString( "org.kalypso.ogc.gml.GisTemplateUserStyle.0" ), String.format( "Loading style", poolableStyleKey.getLocation() ) ); //$NON-NLS-1$ //$NON-NLS-2$
    m_styleName = styleName;
    m_isUsedForSelection = usedForSelection;

    m_styleKey = poolableStyleKey;
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    pool.addPoolListener( this, m_styleKey );
  }

  /**
   * @return a empty style
   */
  private static UserStyle createDummyStyle( final String title, final String abstr )
  {
    return StyleFactory.createUserStyle( "dummyStyle", title, abstr, false, new FeatureTypeStyle[0] ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypsodeegree.xml.Marshallable#exportAsXML()
   */
  public String exportAsXML( )
  {
    return ((Marshallable) m_userStyle).exportAsXML();
  }

  public void addFeatureTypeStyle( final FeatureTypeStyle featureTypeStyle )
  {
    m_userStyle.addFeatureTypeStyle( featureTypeStyle );
  }

  public String getAbstract( )
  {
    return m_userStyle.getAbstract();
  }

  public FeatureTypeStyle[] getFeatureTypeStyles( )
  {
    return m_userStyle.getFeatureTypeStyles();
  }

  public String getName( )
  {
    return m_userStyle.getName();
  }

  public String getTitle( )
  {
    return m_userStyle.getTitle();
  }

  public boolean isDefault( )
  {
    return m_userStyle.isDefault();
  }

  public void removeFeatureTypeStyle( final FeatureTypeStyle featureTypeStyle )
  {
    m_userStyle.removeFeatureTypeStyle( featureTypeStyle );
  }

  public void setAbstract( final String abstract_ )
  {
    m_userStyle.setAbstract( abstract_ );
  }

  public void setDefault( final boolean default_ )
  {
    m_userStyle.setDefault( default_ );
  }

  public void setFeatureTypeStyles( final FeatureTypeStyle[] featureTypeStyles )
  {
    m_userStyle.setFeatureTypeStyles( featureTypeStyles );
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.UserStyle#getFeatureTypeStyle(java.lang.String)
   */
  public FeatureTypeStyle getFeatureTypeStyle( final String featureTypeStyleName )
  {
    return m_userStyle.getFeatureTypeStyle( featureTypeStyleName );
  }

  public void setName( final String name )
  {
    m_userStyle.setName( name );
  }

  public void setTitle( final String title )
  {
    m_userStyle.setTitle( title );
  }

  public boolean isDisposed( )
  {
    return m_disposed;
  }

  /**
   * Adds a listener to the list of listeners. Has no effect if the same listeners is already registered.
   */
  public void addStyleListener( final IKalypsoUserStyleListener l )
  {
    m_listeners.add( l );
  }

  /**
   * Removes a listener from the list of listeners. Has no effect if the listeners is not registered.
   */
  public void removeStyleListener( final IKalypsoUserStyleListener l )
  {
    m_listeners.remove( l );
  }

  /**
   * @return <code>true</code>, if this layer is used to draw selected features. Else, <code>false</code>.
   */
  public boolean isUsedForSelection( )
  {
    return m_isUsedForSelection;
  }

  /**
   * @see org.kalypso.util.pool.IPoolListener#objectLoaded(org.kalypso.util.pool.IPoolableObjectType, java.lang.Object,
   *      org.eclipse.core.runtime.IStatus)
   */
  public void objectLoaded( final IPoolableObjectType key, final Object newValue, final IStatus status )
  {
    m_loaded = true;

    if( KeyComparator.getInstance().compare( m_styleKey, key ) == 0 && newValue != null )
    {
      try
      {
        final StyledLayerDescriptor sld = (StyledLayerDescriptor) newValue;
        m_userStyle = findUserStyle( sld );

        final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
        final KeyInfo info = pool.getInfoForKey( key );
        final ILoader loader = info == null ? null : info.getLoader();
        if( loader instanceof SldLoader )
          m_resourceBundle = ((SldLoader) loader).getResourceBundle();
      }
      catch( final Exception e )
      {
        e.printStackTrace();
      }

      fireStyleChanged();
      setDirty( false );
    }
  }

  private UserStyle findUserStyle( final StyledLayerDescriptor sld )
  {
    final UserStyle userStyle = sld.findUserStyle( m_styleName );

    if( userStyle != null )
      return userStyle;

    final UserStyle defaultUserStyle = sld.getDefaultUserStyle();
    if( defaultUserStyle != null )
      return defaultUserStyle;

    final String message = Messages.getString( "org.kalypso.ogc.gml.GisTemplateUserStyle.1", m_styleName ) + m_styleName; //$NON-NLS-1$
    final IStatus status = StatusUtilities.createStatus( IStatus.WARNING, message, null );
    KalypsoGisPlugin.getDefault().getLog().log( status );

    final String title = Messages.getString("org.kalypso.ogc.gml.GisTemplateUserStyle.2"); //$NON-NLS-1$
    final String abstr = message;
    return createDummyStyle( title, abstr );
  }

  /**
   * @see org.kalypso.util.pool.IPoolListener#objectInvalid(org.kalypso.util.pool.IPoolableObjectType, java.lang.Object)
   */
  public void objectInvalid( final IPoolableObjectType key, final Object oldValue )
  {
    if( KeyComparator.getInstance().compare( m_styleKey, key ) == 0 )
    {
      m_userStyle = createDummyStyle( Messages.getString("org.kalypso.ogc.gml.GisTemplateUserStyle.3"),  Messages.getString("org.kalypso.ogc.gml.GisTemplateUserStyle.9")  ); //$NON-NLS-1$ //$NON-NLS-2$

      fireStyleChanged();
      setDirty( false );
    }
  }

  public void dispose( )
  {
    m_disposed = true;
    m_listeners.clear();

    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    pool.removePoolListener( this );
    m_userStyle = createDummyStyle( Messages.getString( "org.kalypso.ogc.gml.GisTemplateUserStyle.4" ), Messages.getString("org.kalypso.ogc.gml.GisTemplateUserStyle.5") ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * @see org.kalypso.loader.IPooledObject#isLoaded()
   */
  public boolean isLoaded( )
  {
    return m_loaded;
  }

  /**
   * @param styleType
   */
  public void fillStyleType( final List<Style> stylesList, final Style styleType )
  {
    if( m_styleKey == null )
      return;
    styleType.setActuate( "onRequest" ); //$NON-NLS-1$
    styleType.setHref( m_styleKey.getLocation() );
    styleType.setLinktype( m_styleKey.getType() );
    styleType.setStyle( m_styleName );
    styleType.setType( "simple" ); //$NON-NLS-1$
    if( isUsedForSelection() )
      styleType.setSelection( true );
    stylesList.add( styleType );
  }

  public PoolableObjectType getPoolKey( )
  {
    return m_styleKey;
  }

  /**
   * @see org.kalypso.util.pool.IPoolListener#dirtyChanged(org.kalypso.util.pool.IPoolableObjectType, boolean)
   */
  public void dirtyChanged( final IPoolableObjectType key, final boolean isDirty )
  {
    setDirty( isDirty );
  }

  private String findLabel( )
  {
    /* If present, the title is the user-friendly label. */
    if( getTitle() != null )
      return getTitle();

    /* Fallback: if no titel is present, take the name (id like). */
    if( getName() != null )
      return getName();

    return Messages.getString( "org.kalypso.ogc.gml.GisTemplateUserStyle.6" ); //$NON-NLS-1$
  }

  public String getLabel( )
  {
    final String label = findLabel();

    if( !isLoaded() )
      return label + Messages.getString( "org.kalypso.ogc.gml.GisTemplateUserStyle.8" ); //$NON-NLS-1$

    if( m_dirty )
      return label + "*"; //$NON-NLS-1$

    return label;
  }

  public void save( final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
      final Object object = pool.getObject( m_styleKey );
      pool.saveObject( object, monitor );
    }
    catch( final LoaderException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  /**
   * Runs the given runnable on every listener in a safe way.
   */
  public void fireStyleChanged( )
  {
    final IKalypsoUserStyleListener[] listeners = m_listeners.toArray( new IKalypsoUserStyleListener[m_listeners.size()] );
    for( final IKalypsoUserStyleListener l : listeners )
    {
      final ISafeRunnable code = new SafeRunnable()
      {
        public void run( ) throws Exception
        {
          l.styleChanged( GisTemplateUserStyle.this );
        }
      };

      SafeRunner.run( code );
      setDirty( true );
    }
  }

  private void setDirty( final boolean isDirty )
  {
    if( m_dirty == isDirty )
      return;

    m_dirty = isDirty;

    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    if( m_styleKey != null )
    {
      final KeyInfo info = pool.getInfoForKey( m_styleKey );
      if( info != null )
        info.setDirty( isDirty );
    }
  }

  /**
   * Resolves an international string against the (possibly exsiting) internal resource bundle of thisa style.<br>
   *
   * @return If <code>text</code> startswith '%', the text is assumed to be a key of the internal reource bundle and is
   *         resolved against it. Else, <code>text</code> is returned.
   */
  public String resolveI18nString( final String text )
  {
    if( text == null )
      return null;

    if( text.isEmpty() )
      return text;

    if( text.charAt( 0 ) == '%' )
    {
      final String key = text.substring( 1 );
      if( m_resourceBundle == null )
        return  Messages.getString("org.kalypso.ogc.gml.GisTemplateUserStyle.7", key ); //$NON-NLS-1$

      try
      {
        return m_resourceBundle.getString( key );
      }
      catch( final MissingResourceException e )
      {
        return String.format( "!%s!", key ); //$NON-NLS-1$
      }
    }

    return text;
  }
}
