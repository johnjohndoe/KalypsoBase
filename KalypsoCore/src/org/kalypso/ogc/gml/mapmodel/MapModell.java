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
package org.kalypso.ogc.gml.mapmodel;

import java.awt.Graphics;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.contribs.eclipse.core.runtime.SafeRunnable;
import org.kalypso.core.KalypsoCoreDebug;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoCascadingTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.IKalypsoThemeListener;
import org.kalypso.ogc.gml.KalypsoThemeAdapter;
import org.kalypso.ogc.gml.mapmodel.visitor.ThemeUsedForMaxExtentPredicate;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * @author Andreas von Dömming
 */
public class MapModell implements IMapModell
{
  private final Vector<IKalypsoTheme> m_themes = new Vector<>();

  private final Collection<IMapModellListener> m_listeners = Collections.synchronizedCollection( new HashSet<IMapModellListener>() );

  private final String m_coordinatesSystem;

  // TODO: this is problematic now, as we are using cascaded themes
  // Probably it would be much better to put the active theme int the MapPanel! this would probably solve all problems
  // at once...
  private IKalypsoTheme m_activeTheme = null;

  private I10nString m_name;

  private final IKalypsoThemeListener m_themeListener = new KalypsoThemeAdapter()
  {
    @Override
    public void contextChanged( final IKalypsoTheme source )
    {
      fireContextChanged( source );
    }

    @Override
    public void visibilityChanged( final IKalypsoTheme source, final boolean newVisibility )
    {
      fireThemeVisibilityChanged( source, newVisibility );
    }

    @Override
    public void statusChanged( final IKalypsoTheme source )
    {
      fireThemeStatusChanged( source );
    }
  };

  private final URL m_context;

  public MapModell( final String crs, final URL context )
  {
    m_coordinatesSystem = crs;
    m_context = context;
  }

  @Override
  public void dispose( )
  {
    activateTheme( null );

    final IKalypsoTheme[] themeArray = m_themes.toArray( new IKalypsoTheme[m_themes.size()] );
    m_themes.clear();

    for( final IKalypsoTheme theme : themeArray )
    {
      theme.dispose();
    }
  }

  @Override
  public URL getContext( )
  {
    return m_context;
  }

  /**
   * Activates the given theme and deactiveates the currently activated one.
   * <p>
   * This also applies to any sub-modells, only one theme can be activated in the whole theme tree.
   */
  @Override
  public void activateTheme( final IKalypsoTheme theme )
  {
    final IKalypsoTheme oldActiveTheme = getActiveTheme();

    // we just call internal activate for me and all submodell
    internalActivate( theme );

    final IKalypsoThemeVisitor visitor = new IKalypsoThemeVisitor()
    {
      @Override
      public boolean visit( final IKalypsoTheme visitedTheme )
      {
        if( visitedTheme instanceof IMapModell )
        {
          final IMapModell innerModell = (IMapModell) visitedTheme;
          innerModell.internalActivate( theme );
        }
        return true;
      }
    };

    accept( visitor, IKalypsoThemeVisitor.DEPTH_INFINITE );

    // HACK: we also fire theme activate, so listeners on the topmost modell
    // gets informed...
    // TODO: we should refaktor so, that all modell of one modell-tree will use
    // the same list of listeners (some kind of common event-manager)
    fireThemeActivated( oldActiveTheme, theme );
  }

  /**
   * Tries to activate the given theme within this modell.
   *
   * @return <code>true</code>, if the given theme is contained within this modell and was activated. <code>false</code>
   *         otherwise.
   */
  @Override
  public void internalActivate( final IKalypsoTheme theme )
  {
    /* Do nothing if this theme is already the activated theme */
    if( m_activeTheme == theme )
      return;

    final IKalypsoTheme themeToActivate = m_themes.contains( theme ) ? theme : null;

    if( m_activeTheme == themeToActivate )
      return;

    final IKalypsoTheme oldTheme = m_activeTheme;
    m_activeTheme = theme;
    fireThemeActivated( oldTheme, theme );
  }

  @Override
  public IKalypsoTheme getActiveTheme( )
  {
    // find active theme
    final IKalypsoTheme[] oldActiveTheme = new IKalypsoTheme[1]; // return holder for inner class, initially null
    final IKalypsoThemeVisitor findActiveVisitor = new IKalypsoThemeVisitor()
    {
      @Override
      public boolean visit( final IKalypsoTheme theme )
      {
        if( theme == null )
          return false;
        if( theme.getMapModell().isThemeActivated( theme ) )
        {
          oldActiveTheme[0] = theme;
          return false;
        }

        return true;
      }
    };
    accept( findActiveVisitor, IKalypsoThemeVisitor.DEPTH_INFINITE );

    return oldActiveTheme[0];
  }

  @Override
  public void addTheme( final IKalypsoTheme theme )
  {
    m_themes.add( theme );

    theme.addKalypsoThemeListener( m_themeListener );

    fireThemeAdded( theme );
  }

  @Override
  public void insertTheme( final IKalypsoTheme theme, final int position )
  {
    m_themes.insertElementAt( theme, position );

    theme.addKalypsoThemeListener( m_themeListener );

    fireThemeAdded( theme );

    if( m_activeTheme == null )
      activateTheme( theme );
  }

  @Override
  public IKalypsoTheme[] getAllThemes( )
  {
    return m_themes.toArray( new IKalypsoTheme[m_themes.size()] );
  }

  @Override
  public String getCoordinatesSystem( )
  {
    return m_coordinatesSystem;
  }

  @Override
  public IStatus paint( final Graphics g, final GeoTransform p, final IProgressMonitor monitor )
  {
    final SubMonitor progress = SubMonitor.convert( monitor, Messages.getString( "org.kalypso.ogc.gml.map.ThemePainter.0" ), m_themes.size() ); //$NON-NLS-1$
    final IKalypsoTheme[] themes = m_themes.toArray( new IKalypsoTheme[m_themes.size()] );
    final IStatus[] children = new IStatus[themes.length];
    for( int i = themes.length; i > 0; i-- )
    {
      final IKalypsoTheme theme = themes[i - 1];
      progress.subTask( theme.getLabel() );
      if( theme.isVisible() )
      {
        final IStatus status = theme.paint( g, p, null, progress.newChild( 1 ) );
        children[i - 1] = status;
      }
      else
        children[i - 1] = Status.OK_STATUS;
    }

    return new MultiStatus( KalypsoCorePlugin.getID(), -1, children, "", null ); //$NON-NLS-1$
  }

  @Override
  public IKalypsoTheme getTheme( final int pos )
  {
    return m_themes.elementAt( pos );
  }

  @Override
  public int getThemeSize( )
  {
    return m_themes.size();
  }

  @Override
  public boolean isThemeActivated( final IKalypsoTheme theme )
  {
    return m_activeTheme == theme;
  }

  @Override
  public void moveDown( final IKalypsoTheme theme )
  {
    final int pos = m_themes.indexOf( theme );
    if( pos > 0 )
    {
      swapThemes( theme, getTheme( pos - 1 ) );
    }
  }

  @Override
  public void moveUp( final IKalypsoTheme theme )
  {
    final int pos = m_themes.indexOf( theme );
    if( pos + 1 < m_themes.size() )
    {
      swapThemes( theme, getTheme( pos + 1 ) );
    }
  }

  public void removeTheme( final int pos )
  {
    removeTheme( m_themes.elementAt( pos ) );
  }

  @Override
  public void removeTheme( final IKalypsoTheme theme )
  {
    theme.removeKalypsoThemeListener( m_themeListener );

    if( m_themes.contains( theme ) )
    {
      m_themes.remove( theme );
    }
    else
    {
      this.accept( new IKalypsoThemeVisitor()
      {

        @Override
        public boolean visit( final IKalypsoTheme t )
        {
          if( t instanceof IKalypsoCascadingTheme )
          {
            final IKalypsoCascadingTheme cascading = (IKalypsoCascadingTheme) t;
            return !removeFromCascadingTheme( cascading, theme );
          }

          return true;
        }
      }, 1 );
    }

    fireThemeRemoved( theme, theme.isVisible() );
    if( m_activeTheme == theme )
    {
      // TODO: is this right? The theme has gone... probably activateTheme( null ) was meant?
      activateTheme( theme );
    }
  }

  /**
   * @hack normally it is better a IKalypsoTheme nows it's parent and iterate over these parent / child structure by
   *       IKalypsoThemeVisitor
   */
  protected boolean removeFromCascadingTheme( final IKalypsoCascadingTheme cascading, final IKalypsoTheme remove )
  {
    final IKalypsoTheme[] themes = cascading.getAllThemes();
    for( final IKalypsoTheme theme : themes )
    {
      if( theme.equals( remove ) )
      {
        cascading.removeTheme( remove );
        return true;
      }
      else if( theme instanceof IKalypsoCascadingTheme )
      {
        final IKalypsoCascadingTheme subCascading = (IKalypsoCascadingTheme) theme;
        final boolean removed = removeFromCascadingTheme( subCascading, remove );
        if( removed )
          return true;
      }
    }

    return false;
  }

  @Override
  public void swapThemes( final IKalypsoTheme theme1, final IKalypsoTheme theme2 )
  {
    final int pos1 = m_themes.indexOf( theme1 );
    final int pos2 = m_themes.indexOf( theme2 );
    m_themes.set( pos1, theme2 );
    m_themes.set( pos2, theme1 );

    fireThemeOrderChanged();
  }

  @Override
  public GM_Envelope getFullExtentBoundingBox( )
  {
    final IKalypsoTheme[] themes = getAllThemes();
    return MapModellHelper.calculateExtent( themes, new ThemeUsedForMaxExtentPredicate() );
  }

  @Override
  public void accept( final IKalypsoThemeVisitor ktv, final int depth )
  {
    final IKalypsoTheme[] allThemes = getAllThemes();
    for( final IKalypsoTheme element : allThemes )
    {
      accept( ktv, depth, element );
    }
  }

  @Override
  public void accept( final IKalypsoThemeVisitor ktv, final int depth, final IKalypsoTheme theme )
  {
    final boolean recurse = ktv.visit( theme );

    if( recurse && depth != FeatureVisitor.DEPTH_ZERO )
      if( theme instanceof IMapModell && depth == IKalypsoThemeVisitor.DEPTH_INFINITE )
      {
        final IMapModell innerModel = (IMapModell) theme;
        innerModel.accept( ktv, depth );
      }
  }

  @Override
  public I10nString getName( )
  {
    return m_name;
  }

  @Override
  public void setName( final I10nString name )
  {
    m_name = name;
  }

  @Override
  public String getLabel( )
  {
    return getName().getValue();
  }

  @Override
  public void addMapModelListener( final IMapModellListener l )
  {
    m_listeners.add( l );
  }

  @Override
  public void removeMapModelListener( final IMapModellListener l )
  {
    m_listeners.remove( l );
  }

  private interface IListenerRunnable
  {
    void visit( final IMapModellListener l );
  }

  /**
   * Runs the given runnable on every listener in a safe way.
   */
  private void acceptListenersRunnable( final IListenerRunnable r )
  {
    // REMARK: fetch via untyped method, else we get sync. problems
    final Object[] listeners = m_listeners.toArray();
    for( final Object l : listeners )
    {
      final ISafeRunnable code = new SafeRunnable()
      {
        @Override
        public void run( ) throws Exception
        {
          r.visit( (IMapModellListener) l );
        }
      };

      SafeRunner.run( code );
    }
  }

  protected void fireThemeAdded( final IKalypsoTheme theme )
  {
    acceptListenersRunnable( new IListenerRunnable()
    {
      @Override
      public void visit( final IMapModellListener l )
      {
        l.themeAdded( MapModell.this, theme );
      }
    } );
  }

  protected void fireThemeRemoved( final IKalypsoTheme theme, final boolean lastVisibility )
  {
    acceptListenersRunnable( new IListenerRunnable()
    {
      @Override
      public void visit( final IMapModellListener l )
      {
        l.themeRemoved( MapModell.this, theme, lastVisibility );
      }
    } );
  }

  protected void fireThemeActivated( final IKalypsoTheme previouslyActive, final IKalypsoTheme activeTheme )
  {
    KalypsoCoreDebug.MAP_MODELL.printf( Messages.getString( "org.kalypso.ogc.gml.mapmodel.MapModell.0", previouslyActive, activeTheme ) ); //$NON-NLS-1$

    acceptListenersRunnable( new IListenerRunnable()
    {
      @Override
      public void visit( final IMapModellListener l )
      {
        l.themeActivated( MapModell.this, previouslyActive, activeTheme );
      }
    } );
  }

  protected void fireThemeVisibilityChanged( final IKalypsoTheme theme, final boolean visibility )
  {
    acceptListenersRunnable( new IListenerRunnable()
    {
      @Override
      public void visit( final IMapModellListener l )
      {
        l.themeVisibilityChanged( MapModell.this, theme, visibility );
      }
    } );
  }

  protected void fireThemeStatusChanged( final IKalypsoTheme theme )
  {
    acceptListenersRunnable( new IListenerRunnable()
    {
      @Override
      public void visit( final IMapModellListener l )
      {
        l.themeStatusChanged( MapModell.this, theme );
      }
    } );
  }

  protected void fireThemeOrderChanged( )
  {
    acceptListenersRunnable( new IListenerRunnable()
    {
      @Override
      public void visit( final IMapModellListener l )
      {
        l.themeOrderChanged( MapModell.this );
      }
    } );
  }

  protected void fireContextChanged( final IKalypsoTheme theme )
  {
    acceptListenersRunnable( new IListenerRunnable()
    {
      @Override
      public void visit( final IMapModellListener l )
      {
        l.themeContextChanged( MapModell.this, theme );
      }
    } );
  }

  @Override
  public Object getThemeParent( final IKalypsoTheme abstractKalypsoTheme )
  {
    // normally, its just me
    return this;
  }

  /**
   * Returns always <code>true</code>.
   */
  @Override
  public boolean isLoaded( )
  {
    return true;
  }
}