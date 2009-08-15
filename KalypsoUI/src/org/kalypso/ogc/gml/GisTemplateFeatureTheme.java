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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.net.IUrlResolver2;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.catalog.CatalogSLD;
import org.kalypso.core.util.pool.IPoolListener;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.core.util.pool.KeyComparator;
import org.kalypso.core.util.pool.KeyInfo;
import org.kalypso.core.util.pool.PoolableObjectType;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ogc.gml.wms.provider.legends.feature.FeatureThemeLegendProvider;
import org.kalypso.template.types.LayerType;
import org.kalypso.template.types.ObjectFactory;
import org.kalypso.template.types.StyledLayerType;
import org.kalypso.template.types.StyledLayerType.Property;
import org.kalypso.template.types.StyledLayerType.Style;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.util.command.JobExclusiveCommandTarget;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.UserStyle;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.graphics.sld.DefaultStyleFactory;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;
import org.kalypsodeegree_impl.graphics.sld.StyleNotDefinedException;
import org.kalypsodeegree_impl.graphics.sld.UserStyle_Impl;

/**
 * <p>
 * Ein Decorator f�r ein {@link org.kalypso.ogc.gml.KalypsoFeatureTheme}, welches dieses (asynchron) �ber den Pool aus
 * einer Source l�dt.
 * </p>
 * <p>
 * Die ganze dynamic, also die �berwachung, ob sich das Pool-Objekt ge�ndert hat etc. findet hier statt
 * </p>
 * <p>
 * Hier findet auch die Verwaltung statt, ob sich Daten des Themas ge�ndert haben
 * </p>
 * <p>
 * Implementiert unter anderem {@link org.kalypso.commons.command.ICommandTarget}, da sich die Daten des unterliegenden
 * Themas �ndern k�nnen
 * </p>
 *
 * @author Gernot Belger
 */
public class GisTemplateFeatureTheme extends AbstractKalypsoTheme implements IPoolListener, ICommandTarget, IKalypsoFeatureTheme, IKalypsoSaveableTheme, IKalypsoUserStyleListener
{
  protected static final Logger LOGGER = Logger.getLogger( GisTemplateFeatureTheme.class.getName() );

  private final IKalypsoThemeListener m_themeListener = new IKalypsoThemeListener()
  {

    @Override
    public void contextChanged( final IKalypsoTheme source )
    {
      handleContextChanged();
    }

    @Override
    public void repaintRequested( final IKalypsoTheme source, final GM_Envelope invalidExtent )
    {
      handleRepaintRequested( invalidExtent );
    }

    @Override
    public void statusChanged( final IKalypsoTheme source )
    {
      handleStatusChanged();
    }

    @Override
    public void visibilityChanged( final IKalypsoTheme source, final boolean newVisibility )
    {
      handleVisibilityChanged( newVisibility );
    }
  };

  private JobExclusiveCommandTarget m_commandTarget;

  private boolean m_loaded = false;

  private final PoolableObjectType m_layerKey;

  private final String m_featurePath;

  private KalypsoFeatureTheme m_theme = null;

  private boolean m_disposed = false;

  private final IFeatureSelectionManager m_selectionManager;

  private final List<GisTemplateUserStyle> m_gisTemplateUserStyles = new ArrayList<GisTemplateUserStyle>();

  public GisTemplateFeatureTheme( final I10nString layerName, final LayerType layerType, final URL context, final IFeatureSelectionManager selectionManager, final IMapModell mapModel )
  {
    super( layerName, layerType.getLinktype(), mapModel );

    m_selectionManager = selectionManager;
    final String source = layerType.getHref();
    final String type = layerType.getLinktype();
    final String featurePath = layerType.getFeaturePath();
    m_layerKey = new PoolableObjectType( type, source, context );
    m_featurePath = featurePath;
    if( layerType instanceof StyledLayerType )
    {
      final StyledLayerType mapLayerType = (StyledLayerType) layerType;
      setType( type.toUpperCase() );

      final List<Style> stylesList = mapLayerType.getStyle();
      for( final Style style : stylesList )
      {
        final PoolableObjectType sldPoolableObjectType = new PoolableObjectType( style.getLinktype(), style.getHref(), context );
        final boolean usedForSelection = style.isSelection();
        final GisTemplateUserStyle gisTemplateUserStyle = new GisTemplateUserStyle( sldPoolableObjectType, style.getStyle(), usedForSelection );
        m_gisTemplateUserStyles.add( gisTemplateUserStyle );
      }

      GisTemplateFeatureTheme.configureProperties( this, mapLayerType );
    }

    setStatus( StatusUtilities.createInfoStatus( Messages.get( "org.kalypso.ogc.gml.GisTemplateFeatureTheme.3" ) ) ); //$NON-NLS-1$

    final boolean isLazyLoading = false;
    // TODO: get from preferences or properties or ....
    // Only load, if we are not lazLoading. Visible layers will immediately start loading, as
    // they will soon will call setVisible( true )
    if( !isLazyLoading )
      startLoading();
  }

  private void startLoading( )
  {
    try
    {
      setStatus( StatusUtilities.createInfoStatus( Messages.get( "org.kalypso.ogc.gml.GisTemplateFeatureTheme.0" ) ) ); //$NON-NLS-1$
      final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
      pool.addPoolListener( this, m_layerKey );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, Messages.get( "org.kalypso.ogc.gml.GisTemplateFeatureTheme.4" ), e );
      setStatus( status );
    }
  }

  public static void configureProperties( final IKalypsoTheme theme, final StyledLayerType mapLayerType )
  {
    final List<Property> propertyList = mapLayerType.getProperty();
    for( final Property property : propertyList )
      theme.setProperty( property.getName(), property.getValue() );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#dispose()
   */
  @Override
  public void dispose( )
  {
    m_disposed = true;
    // remove from pool
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    pool.removePoolListener( this );
    if( m_commandTarget != null )
      m_commandTarget.dispose();
    if( m_theme != null )
    {
      m_theme.dispose();
      m_theme = null;
    }
    // remove styles
    final GisTemplateUserStyle[] templateStyles = m_gisTemplateUserStyles.toArray( new GisTemplateUserStyle[m_gisTemplateUserStyles.size()] );
    for( final GisTemplateUserStyle style : templateStyles )
      removeStyle( style );

    super.dispose();
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#paint(java.awt.Graphics,
   *      org.kalypsodeegree.graphics.transformation.GeoTransform, java.lang.Boolean,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  public IStatus paint( final Graphics g, final GeoTransform p, final Boolean selected, final IProgressMonitor monitor )
  {
    if( m_theme != null )
    {
      if( selected == null || !selected )
        setStatus( PAINT_STATUS );
      final IStatus status = m_theme.paint( g, p, selected, monitor );
      if( selected == null || !selected )
        setStatus( status );
      return status;
    }

    return Status.OK_STATUS;
  }

  /**
   * @see org.kalypso.ogc.gml.ITemplateTheme#saveFeatures(org.eclipse.core.runtime.IProgressMonitor)
   */
  public void saveFeatures( final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      if( m_theme != null )
        KalypsoCorePlugin.getDefault().getPool().saveObject( m_theme.getWorkspace(), monitor );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw new CoreException( StatusUtilities.statusFromThrowable( e, "Fehler beim Speichern" ) ); //$NON-NLS-1$
    }
  }

  /**
   * @see org.kalypso.commons.command.ICommandTarget#postCommand(org.kalypso.commons.command.ICommand,
   *      java.lang.Runnable)
   */
  public void postCommand( final ICommand command, final Runnable runnable )
  {
    m_commandTarget.postCommand( command, runnable );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#getBoundingBox()
   */
  public GM_Envelope getFullExtent( )
  {
    if( m_theme != null )
      return m_theme.getFullExtent();
    return null;
  }

  public void fillLayerType( final LayerType layer, final String id, final boolean isVisible )
  {
    final ObjectFactory extentFac = new ObjectFactory();
    final PoolableObjectType key = m_layerKey;
    layer.setId( id );
    layer.setHref( key.getLocation() );
    layer.setLinktype( key.getType() );
    layer.setActuate( "onRequest" ); //$NON-NLS-1$
    layer.setType( "simple" ); //$NON-NLS-1$
    layer.setFeaturePath( m_featurePath );
    if( layer instanceof StyledLayerType )
    {
      final StyledLayerType styledLayerType = (StyledLayerType) layer;
      styledLayerType.setName( getName().getKey() );
      styledLayerType.setVisible( isVisible );
      styledLayerType.getDepends();

      final String legendIcon = getLegendIcon();
      if( legendIcon != null )
        styledLayerType.setLegendicon( extentFac.createStyledLayerTypeLegendicon( legendIcon ) );

      styledLayerType.setShowChildren( extentFac.createStyledLayerTypeShowChildren( shouldShowLegendChildren() ) );

      final List<Style> stylesList = styledLayerType.getStyle();
      for( final GisTemplateUserStyle style : m_gisTemplateUserStyles )
      {
        final Style styleType = extentFac.createStyledLayerTypeStyle();
        style.fillStyleType( stylesList, styleType );
      }

      GisTemplateFeatureTheme.fillProperties( this, extentFac, styledLayerType );
    }
  }

  public static void fillProperties( final AbstractKalypsoTheme theme, final ObjectFactory extentFac, final StyledLayerType styledLayerType )
  {
    final List<Property> propertyList = styledLayerType.getProperty();
    final String[] propertyNames = theme.getPropertyNames();
    for( final String name : propertyNames )
    {
      final Property property = extentFac.createStyledLayerTypeProperty();
      property.setName( name );
      final String value = theme.getProperty( name, null );
      if( value != null )
      {
        property.setValue( value );
        propertyList.add( property );
      }
    }
  }

  /**
   * @see org.kalypso.util.pool.IPoolListener#objectLoaded(org.kalypso.util.pool.IPoolableObjectType, java.lang.Object,
   *      org.eclipse.core.runtime.IStatus)
   */
  public void objectLoaded( final IPoolableObjectType key, final Object newValue, IStatus status )
  {
    m_loaded = true;

    try
    {
      if( KeyComparator.getInstance().compare( key, m_layerKey ) == 0 )
      {
        // clear the theme
        if( m_theme != null )
        {
          m_theme.dispose();
          m_theme = null;
        }
        if( newValue != null )
        {
          final CommandableWorkspace commandableWorkspace = (CommandableWorkspace) newValue;

          /* Get current property set */
          final String legendIcon = getLegendIcon();
          final boolean shouldShowLegendChildren = shouldShowLegendChildren();
          final String[] propertyNames = getPropertyNames();
          final Map<String, String> properties = new HashMap<String, String>();
          for( final String propName : propertyNames )
          {
            final String value = getProperty( propName, null );
            properties.put( propName, value );
          }

          m_theme = new KalypsoFeatureTheme( commandableWorkspace, m_featurePath, getName(), m_selectionManager, getMapModell() );
          /* Put current property set into m_theme */
          m_theme.setLegendIcon( legendIcon, getContext() );
          m_theme.setShowLegendChildren( shouldShowLegendChildren );
          for( final String propName : propertyNames )
            m_theme.setProperty( propName, properties.get( propName ) );

          m_theme.addKalypsoThemeListener( m_themeListener );
          if( !m_theme.getStatus().isOK() )
            status = m_theme.getStatus();

          m_commandTarget = new JobExclusiveCommandTarget( m_theme.getWorkspace(), null );

          boolean hasSelectionStyle = false;
          for( final GisTemplateUserStyle style : m_gisTemplateUserStyles )
          {
            addStyle( style );
            if( style.isUsedForSelection() )
              hasSelectionStyle = true;
          }

          final IFeatureType featureType = getFeatureType();
          final URL context = m_layerKey.getContext();
          if( m_gisTemplateUserStyles.isEmpty() )
          {
            final UserStyle userStyle = createDefaultStyle( featureType, context, false );
            if( userStyle != null )
              addStyle( new GisTemplateUserStyle( userStyle, userStyle.getTitle(), false ) );
          }

          if( !hasSelectionStyle )
          {
            final UserStyle userStyle = createDefaultStyle( featureType, context, true );
            if( userStyle != null )
              addStyle( new GisTemplateUserStyle( userStyle, userStyle.getTitle(), true ) );
          }

        }
      }
    }
    catch( final Throwable e )
    {
      final IStatus errorStatus = StatusUtilities.createStatus( IStatus.ERROR, Messages.get( "org.kalypso.ogc.gml.GisTemplateFeatureTheme.1" ) + e.toString(), e ); //$NON-NLS-1$
      KalypsoGisPlugin.getDefault().getLog().log( errorStatus );
      setStatus( status );
      return;
    }

    // REMARK: accessing the full extent here may cause dead lock due to access to pool via x-linked features at this
    // point.
    // Also: Causes the map to hang during loading, so we don't do it, even if now the map always gets repaintet, even
    // if the theme extent does not cover the map. invalidate( getFullExtent() );
    fireRepaintRequested( null );
    fireContextChanged();

    setStatus( status );
  }

  private static UserStyle createDefaultStyle( final IFeatureType featureType, final URL context, final boolean usedForSelection )
  {
    if( featureType == null )
      return null;

    /* Try to find a style from the catalogue */
    final CatalogSLD styleCatalog = KalypsoCorePlugin.getDefault().getSLDCatalog();
    final IUrlResolver2 resolver = new IUrlResolver2()
    {
      public URL resolveURL( final String href ) throws MalformedURLException
      {
        return UrlResolverSingleton.resolveUrl( context, href );
      }
    };

    final FeatureTypeStyle fts;
    if( usedForSelection )
      fts = null; // TODO: get from catalog
    else
      fts = styleCatalog.getDefault( resolver, featureType );

    if( fts == null )
    {
      /* No generic default style for selection */
      if( usedForSelection )
        return null;

      try
      {
        System.out.println( "no default style found for " + featureType.getQName() ); //$NON-NLS-1$
        return DefaultStyleFactory.createUserStyle( featureType, " - " //$NON-NLS-1$
            + Messages.get( "org.kalypso.ogc.gml.GisTemplateFeatureTheme.generatedstyle" ) //$NON-NLS-1$
            + " -" ); //$NON-NLS-1$
      }
      catch( final StyleNotDefinedException e )
      {
        e.printStackTrace();

        return null;
      }
    }

    /*
     * Create a user style that wraps the catalog-based Feature Type Style. Inherit name, title and abstract.
     */
    final String name = fts.getName();
    final String title = fts.getTitle();
    final String description = fts.getAbstract();
    return (UserStyle_Impl) StyleFactory.createStyle( name, title, description, fts );
  }

  /**
   * @see org.kalypso.util.pool.IPoolListener#objectInvalid(org.kalypso.util.pool.IPoolableObjectType, java.lang.Object)
   */
  public void objectInvalid( final IPoolableObjectType key, final Object oldValue )
  {
    if( KeyComparator.getInstance().compare( key, m_layerKey ) == 0 )
    {
      // clear the theme
      setStatus( StatusUtilities.createWarningStatus( Messages.get( "org.kalypso.ogc.gml.GisTemplateFeatureTheme.2" ) ) ); //$NON-NLS-1$
      m_theme.dispose();
      m_theme = null;
    }

    // schon mal mitteilen, dass sich das Thema ge�ndert hat
    fireContextChanged();
    fireRepaintRequested( getFullExtent() );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoFeatureTheme#getWorkspace()
   */
  public CommandableWorkspace getWorkspace( )
  {
    if( m_theme != null )
      return m_theme.getWorkspace();
    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoFeatureTheme#getFeatureType()
   */
  public IFeatureType getFeatureType( )
  {
    if( m_theme != null )
      return m_theme.getFeatureType();
    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoFeatureTheme#getFeaturePath()
   */
  public String getFeaturePath( )
  {
    if( m_theme != null )
      return m_theme.getFeaturePath();
    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoFeatureTheme#addStyle(org.kalypso.ogc.gml.KalypsoUserStyle)
   */
  public void addStyle( final KalypsoUserStyle style )
  {
    style.addStyleListener( this );

    if( m_theme != null )
      m_theme.addStyle( style );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoFeatureTheme#removeStyle(org.kalypso.ogc.gml.KalypsoUserStyle)
   */
  public void removeStyle( final KalypsoUserStyle style )
  {
    if( m_theme != null )
      m_theme.removeStyle( style );
    style.dispose();
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoFeatureTheme#getStyles()
   */
  public UserStyle[] getStyles( )
  {
    if( m_theme != null )
      return m_theme.getStyles();
    return new UserStyle[0];
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoFeatureTheme#getFeatureList()
   */
  public FeatureList getFeatureList( )
  {
    if( m_theme != null )
      return m_theme.getFeatureList();
    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoFeatureTheme#getSchedulingRule()
   */
  public ISchedulingRule getSchedulingRule( )
  {
    return m_commandTarget.getSchedulingRule();
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoFeatureTheme#getFeatureListVisible(org.kalypsodeegree.model.geometry.GM_Envelope)
   */
  public FeatureList getFeatureListVisible( final GM_Envelope env )
  {
    if( m_theme != null )
      return m_theme.getFeatureListVisible( env );
    return null;
  }

  /**
   * @see org.kalypso.loader.IPooledObject#isLoaded()
   */
  @Override
  public boolean isLoaded( )
  {
    for( final GisTemplateUserStyle style : m_gisTemplateUserStyles )
    {
      if( !style.isLoaded() )
        return false;
    }

    return m_loaded;
  }

  public IPoolableObjectType getLayerKey( )
  {
    return m_layerKey;
  }

  /**
   * @see org.kalypso.util.pool.IPoolListener#isDisposed()
   */
  public boolean isDisposed( )
  {
    return m_disposed;
  }

  public IFeatureSelectionManager getSelectionManager( )
  {
    return m_selectionManager;
  }

  /**
   * @see org.kalypso.util.pool.IPoolListener#dirtyChanged(org.kalypso.util.pool.IPoolableObjectType, boolean)
   */
  public void dirtyChanged( final IPoolableObjectType key, final boolean isDirty )
  {
    // TODO Change label, showing if dirty or not
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#getContext()
   */
  @Override
  public String getTypeContext( )
  {
    final IFeatureType featureType = getFeatureType();
    if( featureType != null )
      return featureType.getQName().toString();
    else
      return super.getTypeContext();
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#getLabel(java.lang.Object)
   */
  @Override
  public String getLabel( final Object o )
  {
    if( m_theme != null )
      return m_theme.getLabel( m_theme );

    return super.getLabel( o );
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#getImageDescriptor(java.lang.Object)
   */
  @Override
  public ImageDescriptor getImageDescriptor( final Object object )
  {
    if( m_theme != null )
      return m_theme.getImageDescriptor( object );

    return super.getImageDescriptor( object );
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#getDefaultIcon()
   */
  @Override
  protected ImageDescriptor getDefaultIcon( )
  {
    if( m_theme != null )
      return m_theme.getDefaultIcon();

    return KalypsoGisPlugin.getImageProvider().getImageDescriptor( ImageProvider.DESCRIPTORS.IMAGE_THEME_FEATURE );
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#getChildren(java.lang.Object)
   */
  @Override
  public Object[] getChildren( final Object o )
  {
    if( m_theme == null )
      return super.getChildren( o );

    return m_theme.getChildren( m_theme );
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#getName()
   */
  @Override
  public I10nString getName( )
  {
    if( m_theme != null )
      return m_theme.getName();

    return super.getName();
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#setName(org.kalypso.contribs.java.lang.I10nString)
   */
  @Override
  public void setName( final I10nString name )
  {
    if( m_theme != null )
      m_theme.setName( name );

    super.setName( name );
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#getType()
   */
  @Override
  public String getType( )
  {
    if( m_theme != null )
      return super.getType();

    return super.getType();
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#setType(java.lang.String)
   */
  @Override
  public void setType( final String type )
  {
    if( m_theme != null )
      m_theme.setType( type );

    super.setType( type );
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#getStatus()
   */
  @Override
  public IStatus getStatus( )
  {
    if( m_theme != null )
      return m_theme.getStatus();

    return super.getStatus();
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#setStatus(org.eclipse.core.runtime.IStatus)
   */
  @Override
  public void setStatus( final IStatus status )
  {
    if( m_theme != null )
      ((AbstractKalypsoTheme) m_theme).setStatus( status );
    else
      super.setStatus( status );
  }

  public void paint( final double scale, final GM_Envelope bbox, final Boolean selected, final IProgressMonitor monitor, final IPaintDelegate delegate ) throws CoreException
  {
    if( m_theme != null )
      m_theme.paint( scale, bbox, selected, monitor, delegate );
  }

  /**
   * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object getAdapter( final Class adapter )
  {
    if( m_theme != null )
    {
      final Object result = m_theme.getAdapter( adapter );
      if( result != null )
        return result;
    }

    return super.getAdapter( adapter );
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#getProperty(java.lang.String, java.lang.String)
   */
  @Override
  public String getProperty( final String name, final String defaultValue )
  {
    if( m_theme == null )
      return super.getProperty( name, defaultValue );

    return m_theme.getProperty( name, defaultValue );
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#setProperty(java.lang.String, java.lang.String)
   */
  @Override
  public void setProperty( final String name, final String value )
  {
    super.setProperty( name, value );

    if( m_theme != null )
      m_theme.setProperty( name, value );
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#getLegendIcon()
   */
  @Override
  public String getLegendIcon( )
  {
    if( m_theme != null )
      m_theme.getLegendIcon();

    return super.getLegendIcon();
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#shouldShowLegendChildren()
   */
  @Override
  public boolean shouldShowLegendChildren( )
  {
    if( m_theme != null )
      return m_theme.shouldShowLegendChildren();

    return super.shouldShowLegendChildren();
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#setShowLegendChildren(boolean)
   */
  @Override
  public void setShowLegendChildren( final boolean showChildren )
  {
    if( m_theme != null )
      m_theme.setShowLegendChildren( showChildren );

    super.setShowLegendChildren( showChildren );
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#setLegendIcon(java.lang.String, java.net.URL)
   */
  @Override
  public void setLegendIcon( final String legendIcon, final URL context )
  {
    super.setLegendIcon( legendIcon, context );

    if( m_theme != null )
      m_theme.setLegendIcon( legendIcon, context );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoUserStyleListener#styleChanged(org.kalypso.ogc.gml.KalypsoUserStyle)
   */
  public void styleChanged( final KalypsoUserStyle source )
  {
    fireStatusChanged( this );
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#getLegendGraphic(org.eclipse.swt.graphics.Font)
   */
  @Override
  public Image getLegendGraphic( final Font font ) throws CoreException
  {
    if( m_theme != null )
    {
      final FeatureThemeLegendProvider provider = new FeatureThemeLegendProvider( m_theme );
      return provider.getLegendGraphic( font );
    }

    return super.getLegendGraphic( font );
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#setVisible(boolean)
   */
  @Override
  public void setVisible( final boolean visible )
  {
    // Lazy loading: first time we are set visible, start loading

    // Always check, as visible is set to true in constructor
    final boolean checkPoolListener = checkPoolListener();
    if( visible )
    {
      if( !checkPoolListener )
        startLoading();
    }
    else
    {
      // HM: this will probably cause problems, as the theme is not really loaded
      // But else, the stuff waiting for the map to load will wait forever...
      if( !checkPoolListener )
        m_loaded = true;
    }

    super.setVisible( visible );
  }

  /**
   * Check, if we are already started loading. This is done by checking, if we are already listening to the pool.
   */
  private boolean checkPoolListener( )
  {
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    final KeyInfo info = pool.getInfoForKey( m_layerKey );
    if( info == null )
      return false;

    // Check, if we are really in the list of listeners, maybe someone else has already fetched this object
    final IPoolListener[] listeners = info.getPoolListeners();
    for( final IPoolListener poolListener : listeners )
    {
      if( poolListener == this )
        return true;
    }

    return false;
  }

  protected void handleContextChanged( )
  {
    fireContextChanged();
  }

  protected void handleRepaintRequested( final GM_Envelope invalidExtent )
  {
    fireRepaintRequested( invalidExtent );
  }

  protected void handleStatusChanged( )
  {
    fireStatusChanged( this );
  }

  protected void handleVisibilityChanged( final boolean newVisibility )
  {
    fireVisibilityChanged( newVisibility );
  }

}