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
import org.kalypsodeegree.xml.Marshallable;

/**
 * @author Gernot Belger
 */
public abstract class AbstractTemplateStyle implements IKalypsoStyle, Marshallable, IPoolListener
{
  private final Collection<IKalypsoStyleListener> m_listeners = new HashSet<IKalypsoStyleListener>();

  private boolean m_disposed = false;

  /** Flag, if this style is used for selected features or not. */
  private final boolean m_isUsedForSelection;

  private final PoolableObjectType m_styleKey;

  private boolean m_loaded = false;

  private ResourceBundle m_resourceBundle = null;

  public AbstractTemplateStyle( final PoolableObjectType poolableStyleKey, final boolean usedForSelection )
  {
    m_isUsedForSelection = usedForSelection;

    m_styleKey = poolableStyleKey;
  }

  protected void startLoad( )
  {
    if( m_styleKey != null )
    {
      final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
      pool.addPoolListener( this, m_styleKey );
    }
  }

  @Override
  public boolean isDisposed( )
  {
    return m_disposed;
  }

  /**
   * Adds a listener to the list of listeners. Has no effect if the same listeners is already registered.
   */
  @Override
  public void addStyleListener( final IKalypsoStyleListener l )
  {
    m_listeners.add( l );
  }

  /**
   * Removes a listener from the list of listeners. Has no effect if the listeners is not registered.
   */
  @Override
  public void removeStyleListener( final IKalypsoStyleListener l )
  {
    m_listeners.remove( l );
  }

  /**
   * @return <code>true</code>, if this layer is used to draw selected features. Else, <code>false</code>.
   */
  @Override
  public boolean isUsedForSelection( )
  {
    return m_isUsedForSelection;
  }

  /**
   * @see org.kalypso.util.pool.IPoolListener#objectLoaded(org.kalypso.util.pool.IPoolableObjectType, java.lang.Object,
   *      org.eclipse.core.runtime.IStatus)
   */
  @Override
  public void objectLoaded( final IPoolableObjectType key, final Object newValue, final IStatus status )
  {
    m_loaded = true;

    if( KeyComparator.getInstance().compare( m_styleKey, key ) == 0 && newValue != null )
    {
      try
      {
        handleObjectLoaded( newValue );

        final KeyInfo info = getPoolInfo();
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

  protected abstract void handleObjectLoaded( Object newValue );

  /**
   * @see org.kalypso.util.pool.IPoolListener#objectInvalid(org.kalypso.util.pool.IPoolableObjectType, java.lang.Object)
   */
  @Override
  public void objectInvalid( final IPoolableObjectType key, final Object oldValue )
  {
    if( KeyComparator.getInstance().compare( m_styleKey, key ) == 0 )
    {
      handleObjectLoaded( null );

      fireStyleChanged();
      setDirty( false );
    }
  }

  @Override
  public void dispose( )
  {
    m_disposed = true;
    m_listeners.clear();

    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    pool.removePoolListener( this );

    handleObjectLoaded( null );
  }

  /**
   * @see org.kalypso.loader.IPooledObject#isLoaded()
   */
  @Override
  public boolean isLoaded( )
  {
    return m_loaded;
  }

  public PoolableObjectType getPoolKey( )
  {
    return m_styleKey;
  }

  /**
   * @see org.kalypso.util.pool.IPoolListener#dirtyChanged(org.kalypso.util.pool.IPoolableObjectType, boolean)
   */
  @Override
  public void dirtyChanged( final IPoolableObjectType key, final boolean isDirty )
  {
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

  @Override
  public String getLabel( )
  {
    final String label = findLabel();

    if( !isLoaded() )
      return label + Messages.getString( "org.kalypso.ogc.gml.GisTemplateUserStyle.8" ); //$NON-NLS-1$

    if( isDirty() )
      return label + "*"; //$NON-NLS-1$

    return label;
  }

  @Override
  public boolean isDirty( )
  {
    final KeyInfo info = getPoolInfo();
    return info.isDirty();
  }

  @Override
  public void save( final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      final KeyInfo info = getPoolInfo();
      info.saveObject( monitor );
    }
    catch( final LoaderException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  /**
   * Runs the given runnable on every listener in a safe way.
   */
  @Override
  public void fireStyleChanged( )
  {
    final IKalypsoStyleListener[] listeners = m_listeners.toArray( new IKalypsoStyleListener[m_listeners.size()] );
    for( final IKalypsoStyleListener l : listeners )
    {
      final ISafeRunnable code = new SafeRunnable()
      {
        @Override
        public void run( ) throws Exception
        {
          l.styleChanged();
        }
      };

      SafeRunner.run( code );
      setDirty( true );
    }
  }

  private void setDirty( final boolean isDirty )
  {
    final KeyInfo info = getPoolInfo();
    if( info != null )
      info.setDirty( isDirty );
  }

  private KeyInfo getPoolInfo( )
  {
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    return pool.getInfoForKey( m_styleKey );
  }

  /**
   * Resolves an international string against the (possibly existing) internal resource bundle of this style.<br>
   * 
   * @return If <code>text</code> startswith '%', the text is assumed to be a key of the internal resource bundle and is
   *         resolved against it. Else, <code>text</code> is returned.
   */
  @Override
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
        return Messages.getString( "org.kalypso.ogc.gml.GisTemplateUserStyle.7", key ); //$NON-NLS-1$

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

  /**
   * @param styleType
   */
  @Override
  public void fillStyleType( final List<Style> stylesList, final Style styleType )
  {
    if( m_styleKey == null )
      return;
    styleType.setActuate( "onRequest" ); //$NON-NLS-1$
    styleType.setHref( m_styleKey.getLocation() );
    styleType.setLinktype( m_styleKey.getType() );

    final String styleName = getStyleName();
    styleType.setStyle( styleName );

    styleType.setType( "simple" ); //$NON-NLS-1$
    if( isUsedForSelection() )
      styleType.setSelection( true );
    stylesList.add( styleType );
  }

  abstract protected String getStyleName( );

}
