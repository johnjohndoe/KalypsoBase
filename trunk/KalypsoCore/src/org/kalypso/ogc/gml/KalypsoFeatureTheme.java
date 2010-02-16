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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.awt.HighlightGraphics;
import org.kalypso.core.KalypsoCoreDebug;
import org.kalypso.core.KalypsoCoreExtensions;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.painter.FeatureThemePaintable;
import org.kalypso.ogc.gml.painter.IStylePaintable;
import org.kalypso.ogc.gml.painter.IStylePainter;
import org.kalypso.ogc.gml.painter.StylePainterFactory;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypsodeegree.graphics.displayelements.DisplayElement;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.event.FeatureStructureChangeModellEvent;
import org.kalypsodeegree.model.feature.event.FeaturesChangedModellEvent;
import org.kalypsodeegree.model.feature.event.IGMLWorkspaceModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEventListener;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;

/**
 * @author Andreas von Dömming
 */
public class KalypsoFeatureTheme extends AbstractKalypsoTheme implements IKalypsoFeatureTheme, ModellEventListener, IKalypsoStyleListener
{
  private final List<IKalypsoStyle> m_styles = new ArrayList<IKalypsoStyle>();

  private CommandableWorkspace m_workspace;

  private final IFeatureType m_featureType;

  private final FeatureList m_featureList;

  private final IFeatureSelectionManager m_selectionManager;

  private final String m_featurePath;

  /**
   * (Crude) hack: remember that we only have a (syntetic) list of only one feature.<br>
   * Fixes the problem, that single features do not correctly get updated.
   */
  private boolean m_isSingleFeature = false;

  /**
   * Holds the descriptor for the default icon of this theme. Is used in legends, such as the outline.
   */
  private Image m_featureThemeIcon;

  public KalypsoFeatureTheme( final CommandableWorkspace workspace, final String featurePath, final I10nString name, final IFeatureSelectionManager selectionManager, final IMapModell mapModel )
  {
    super( name, "FeatureTheme", mapModel ); //$NON-NLS-1$

    m_workspace = workspace;
    m_featurePath = featurePath;
    m_selectionManager = selectionManager;

    final Object featureFromPath = m_workspace.getFeatureFromPath( m_featurePath );

    if( featureFromPath instanceof FeatureList )
    {
      m_featureList = (FeatureList) featureFromPath;
      m_featureType = m_workspace.getFeatureTypeFromPath( m_featurePath );
    }
    else if( featureFromPath instanceof Feature )
    {
      final Feature singleFeature = (Feature) featureFromPath;
      final Feature parent = singleFeature.getOwner();
      m_featureList = FeatureFactory.createFeatureList( parent, singleFeature.getParentRelation() );
      m_featureList.add( singleFeature );
      m_featureType = singleFeature.getFeatureType();
      m_isSingleFeature = true;
    }
    else
    {
      // Should'nt we throw an exception here?
      m_featureList = null;
      m_featureType = null;
      setStatus( StatusUtilities.createStatus( IStatus.WARNING, Messages.getString( "org.kalypso.ogc.gml.KalypsoFeatureTheme.0" ) + featurePath, null ) ); //$NON-NLS-1$
    }

    m_workspace.addModellListener( this );
  }

  @Override
  public void dispose( )
  {
    final IKalypsoStyle[] styles = m_styles.toArray( new IKalypsoStyle[m_styles.size()] );
    for( final IKalypsoStyle element : styles )
      removeStyle( element );

    if( m_workspace != null )
    {
      m_workspace.removeModellListener( this );
      m_workspace = null;
    }

    if( m_featureThemeIcon != null )
      m_featureThemeIcon.dispose();

    super.dispose();
  }

  private void setDirty( )
  {
    fireRepaintRequested( getFullExtent() );
  }

  public CommandableWorkspace getWorkspace( )
  {
    return m_workspace;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoFeatureTheme#getFeatureType()
   */
  public IFeatureType getFeatureType( )
  {
    return m_featureType;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoFeatureTheme#getFeaturePath()
   */
  public String getFeaturePath( )
  {
    return m_featurePath;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#paint(java.awt.Graphics,
   *      org.kalypsodeegree.graphics.transformation.GeoTransform, java.lang.Boolean,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus paint( final Graphics g, final GeoTransform p, final Boolean selected, final IProgressMonitor monitor )
  {
    final Graphics graphics = wrapGraphicForSelection( g, selected );

    try
    {
      final IStylePaintable paintDelegate = new FeatureThemePaintable( p, graphics, m_selectionManager, selected );
      final IStylePainter painter = StylePainterFactory.create( this, selected );
      painter.paint( paintDelegate, monitor );

      if( m_featureList != null && KalypsoCoreDebug.SPATIAL_INDEX_PAINT.isEnabled() )
        m_featureList.paint( g, p );
    }
    catch( final CoreException e )
    {
      return e.getStatus();
    }

    return Status.OK_STATUS;
  }

  /**
   * Determines, if a {@link HighlightGraphics} will be used to draw the selection or not.
   */
  private Graphics wrapGraphicForSelection( final Graphics g, final Boolean selected )
  {
    /* If we draw normally, never use highlight graphics */
    if( selected == null || selected == false )
      return g;

    if( hasSelectionStyle() )
      return g;

    /* Use normal style with highlight graphics to paint */
    return new HighlightGraphics( (Graphics2D) g );
  }

  private boolean hasSelectionStyle( )
  {
    for( final IKalypsoStyle style : m_styles )
    {
      if( style.isUsedForSelection() )
        return true;
    }

    return false;
  }

  public void addStyle( final IKalypsoStyle style )
  {
    m_styles.add( style );
    style.addStyleListener( this );

    // HACKY: in order to refresh (not update) the outline, fire a visibility event
    fireVisibilityChanged( isVisible() );
  }

  public void removeStyle( final IKalypsoStyle style )
  {
    style.removeStyleListener( this );
    m_styles.remove( style );

    // HACKY: in order to refresh (not update) the outline, fire a visibility event
    fireVisibilityChanged( isVisible() );
  }

  public IKalypsoStyle[] getStyles( )
  {
    return m_styles.toArray( new IKalypsoStyle[m_styles.size()] );
  }

  /**
   * @see org.kalypsodeegree.model.feature.event.ModellEventListener#onModellChange(org.kalypsodeegree.model.feature.event.ModellEvent)
   */
  public void onModellChange( final ModellEvent modellEvent )
  {
    if( m_featureList == null )
      return;

    if( modellEvent instanceof IGMLWorkspaceModellEvent )
    {
      // my workspace ?
      final GMLWorkspace changedWorkspace = ((IGMLWorkspaceModellEvent) modellEvent).getGMLWorkspace();
      if( ((m_workspace != null) && (changedWorkspace != m_workspace) && (changedWorkspace != m_workspace.getWorkspace())) )
        return; // not my workspace

      if( modellEvent instanceof FeaturesChangedModellEvent )
      {
        final FeaturesChangedModellEvent featuresChangedModellEvent = ((FeaturesChangedModellEvent) modellEvent);
        final Feature[] features = featuresChangedModellEvent.getFeatures();

        // HACK: for single-feature lists (see flag), we must invalidate the list ourselves.
        if( m_isSingleFeature )
        {
          // TODO: we do not know which one of the changed features is the right one... (ses FIXME below)
          // So we just invalidate all features in this list
          for( final Feature feature : features )
            m_featureList.invalidate( feature );
        }

        if( features.length > 100 )
        {
          // OPTIMIZATION: as List#contains is quite slow, we generally repaint if the number of changed features
          // is too large.
          setDirty();
        }
        else
        {
          GM_Envelope invalidBox = null;
          for( final Feature feature : features )
          {
            if( m_featureList.contains( feature ) || m_featureList.contains( feature.getId() ) )
            {
              final GM_Envelope envelope = feature.getEnvelope();
              if( invalidBox == null )
                invalidBox = envelope;
              else
                invalidBox = invalidBox.getMerged( envelope );
            }
          }
          if( invalidBox != null )
          {
            // TODO: buffer: does not work well for points, or fat-lines
            fireRepaintRequested( invalidBox );
          }
        }
      }
      else if( modellEvent instanceof FeatureStructureChangeModellEvent )
      {
        final FeatureStructureChangeModellEvent fscme = (FeatureStructureChangeModellEvent) modellEvent;
        final Feature[] parents = fscme.getParentFeatures();
        for( final Feature parent : parents )
        {
          if( m_featureList.getParentFeature() == parent )
          {
            switch( fscme.getChangeType() )
            {
              case FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_ADD:
                // fall through
              case FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_DELETE:
                // fall through
              case FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_MOVE:
                setDirty();
                break;
              default:
                setDirty();
            }
          }
        }
      }
    }
    else
    {
      // unknown event, set dirty
      setDirty();
    }
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#getBoundingBox()
   */
  public GM_Envelope getFullExtent( )
  {
    final FeatureList visibleFeatures = getFeatureListVisible( null );
    if( visibleFeatures == null )
      return null;

    return visibleFeatures.getBoundingBox();
  }

  public FeatureList getFeatureList( )
  {
    return m_featureList;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoFeatureTheme#getFeatureListVisible(org.kalypsodeegree.model.geometry.GM_Envelope)
   */
  public FeatureList getFeatureListVisible( final GM_Envelope searchEnvelope )
  {
    if( m_featureList == null )
      return null;

    // TODO: we should also get the scale here (else we might return currently invisible features)

    /* Use complete bounding box if search envelope is not set. */
    final GM_Envelope env = searchEnvelope == null ? m_featureList.getBoundingBox() : searchEnvelope;

    // Put features in set in order to avoid duplicates
    final Set<Feature> features = new LinkedHashSet<Feature>();
    final IStylePaintable paintDelegate = new IStylePaintable()
    {
      public void paint( final DisplayElement displayElement, final IProgressMonitor paintMonitor )
      {
        final Feature feature = displayElement.getFeature();
        final GM_Envelope envelope = feature.getEnvelope();
        if( envelope != null && env.intersects( envelope ) )
        {
          features.add( feature );
        }
      }

      @Override
      public Double getScale( )
      {
        return null;
      }

      @Override
      public GM_Envelope getBoundingBox( )
      {
        return env;
      }

      @Override
      public boolean shouldPaintFeature( final Feature feature )
      {
        return true;
      }
    };

    final IProgressMonitor monitor = new NullProgressMonitor();

    try
    {
      final IStylePainter painter = StylePainterFactory.create( this, null );
      painter.paint( paintDelegate, monitor );
    }
    catch( final CoreException e )
    {
      KalypsoCorePlugin.getDefault().getLog().log( e.getStatus() );
    }

    final Feature parentFeature = m_featureList.getParentFeature();
    final IRelationType parentFTP = m_featureList.getParentFeatureTypeProperty();
    final FeatureList resultList = FeatureFactory.createFeatureList( parentFeature, parentFTP );
    resultList.addAll( features );
    return resultList;
  }

  /**
   * @see org.kalypso.commons.command.ICommandTarget#postCommand(org.kalypso.commons.command.ICommand,
   *      java.lang.Runnable)
   */
  public void postCommand( final ICommand command, final Runnable runnable )
  {
    try
    {
      m_workspace.postCommand( command );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
    if( runnable != null )
    {
      runnable.run();
    }
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoFeatureTheme#getSchedulingRule()
   */
  public ISchedulingRule getSchedulingRule( )
  {
    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoFeatureTheme#getSelectionManager()
   */
  public IFeatureSelectionManager getSelectionManager( )
  {
    return m_selectionManager;
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#getDefaultIcon()
   */
  @Override
  public ImageDescriptor getDefaultIcon( )
  {
    if( m_featureThemeIcon == null )
      m_featureThemeIcon = new Image( Display.getCurrent(), getClass().getResourceAsStream( "resources/featureTheme.gif" ) ); //$NON-NLS-1$

    return ImageDescriptor.createFromImage( m_featureThemeIcon );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoStyleListener#styleChanged()
   */
  public void styleChanged( )
  {
    setDirty();
    fireStatusChanged( this );
  }

  /**
   * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object getAdapter( final Class adapter )
  {
    if( adapter == IKalypsoThemeInfo.class )
      return createThemeInfo();

    return super.getAdapter( adapter );
  }

  private IKalypsoThemeInfo createThemeInfo( )
  {
    final IFeatureType featureType = getFeatureType();
    if( featureType == null )
      return null; // no data available; maybe show some status-message?

    /* If an explicit info is configured for this map, use it */
    // REMARK: is necessary to copy this from AbstractFeatureTheme, as this adapter must be called first
    final String themeInfoId = getProperty( IKalypsoTheme.PROPERTY_THEME_INFO_ID, null );
    if( themeInfoId != null )
      return KalypsoCoreExtensions.createThemeInfo( themeInfoId, this );

    // HACK: use featureThemeInfo from KalypsoUI as a default. This is needed, because this feature info the
    // featureType-properties mechanisms from KalypsoUI in order find a registered featureThemeInfo for the current
    // qname
    final IKalypsoThemeInfo defaultFeatureThemeInfo = KalypsoCoreExtensions.createThemeInfo( "org.kalypso.ui.featureThemeInfo.default", this ); //$NON-NLS-1$
    if( defaultFeatureThemeInfo != null )
      return defaultFeatureThemeInfo;

    return new FeatureThemeInfo( this, new Properties() );
  }

}