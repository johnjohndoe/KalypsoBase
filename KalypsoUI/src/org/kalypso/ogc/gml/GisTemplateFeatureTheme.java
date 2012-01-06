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
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.core.variables.VariableUtils;
import org.kalypso.contribs.java.net.IUrlResolver2;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.catalog.CatalogSLD;
import org.kalypso.core.catalog.CatalogSLDUtils;
import org.kalypso.core.util.pool.IPoolListener;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.core.util.pool.KeyComparator;
import org.kalypso.core.util.pool.KeyInfo;
import org.kalypso.core.util.pool.PoolableObjectType;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.template.types.LayerType;
import org.kalypso.template.types.StyledLayerType;
import org.kalypso.template.types.StyledLayerType.Style;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.util.command.JobExclusiveCommandTarget;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.geometry.GM_Envelope;

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
public class GisTemplateFeatureTheme extends AbstractKalypsoTheme implements IPoolListener, ICommandTarget, IKalypsoFeatureTheme, IKalypsoSaveableTheme, IKalypsoStyleListener
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

  private final List<IKalypsoStyle> m_styles = new ArrayList<IKalypsoStyle>();

  private boolean m_hasStyles;

  private final List<IKalypsoStyle> m_defaultStyles = new ArrayList<IKalypsoStyle>();

  /**
   * The unmodified href. This href will be saved again to the map.
   */
  private final String m_href;

  public GisTemplateFeatureTheme( final I10nString layerName, final LayerType layerType, final URL context, final IFeatureSelectionManager selectionManager, final IMapModell mapModel )
  {
    super( layerName, layerType.getLinktype(), mapModel );

    m_selectionManager = selectionManager;
    m_href = layerType.getHref();

    final String source = VariableUtils.resolveVariablesQuietly( m_href );
    final String type = layerType.getLinktype();
    final String featurePath = layerType.getFeaturePath();

    m_layerKey = new PoolableObjectType( type, source, context );
    m_featurePath = featurePath;

    setType( type.toUpperCase() );

    if( layerType instanceof StyledLayerType )
    {
      m_hasStyles = true;
      final StyledLayerType mapLayerType = (StyledLayerType) layerType;
      initStyles( context, mapLayerType );
      GisTemplateLayerHelper.updateProperties( mapLayerType, this );
    }
    else
      m_hasStyles = false;

    setStatus( StatusUtilities.createInfoStatus( Messages.getString( "org.kalypso.ogc.gml.GisTemplateFeatureTheme.3" ) ) ); //$NON-NLS-1$

    final boolean isLazyLoading = false;
    // TODO: get from preferences or properties or ....
    // Only load, if we are not lazLoading. Visible layers will immediately start loading, as
    // they will soon will call setVisible( true )
    if( !isLazyLoading )
      startLoading();
  }

  /**
   * Called from constructor, initialised (and starts loading) of all configured styles of this theme.
   */
  private void initStyles( final URL context, final StyledLayerType styleType )
  {
    final List<Style> stylesList = styleType.getStyle();
    for( final Style style : stylesList )
    {
      final String linktype = style.getLinktype();
      final String href = style.getHref();
      final String userStyleName = style.getStyle();

      final PoolableObjectType sldPoolableObjectType = new PoolableObjectType( linktype, href, context );
      final boolean usedForSelection = style.isSelection();

      if( userStyleName == null || href.startsWith( "urn" ) )
        m_styles.add( new GisTemplateFeatureTypeStyle( sldPoolableObjectType, usedForSelection ) );
      else
        m_styles.add( new GisTemplateUserStyle( sldPoolableObjectType, userStyleName, usedForSelection ) );
    }
  }

  public List<IKalypsoStyle> getStyleList( )
  {
    return m_styles;
  }

  private void startLoading( )
  {
    try
    {
      setStatus( StatusUtilities.createInfoStatus( Messages.getString( "org.kalypso.ogc.gml.GisTemplateFeatureTheme.0" ) ) ); //$NON-NLS-1$
      final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
      pool.addPoolListener( this, m_layerKey );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, Messages.getString( "org.kalypso.ogc.gml.GisTemplateFeatureTheme.4" ), e ); //$NON-NLS-1$
      setStatus( status );
    }
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
    final IKalypsoStyle[] templateStyles = m_styles.toArray( new IKalypsoStyle[m_styles.size()] );
    for( final IKalypsoStyle style : templateStyles )
      removeStyle( style );
    final IKalypsoStyle[] defaultStyles = m_defaultStyles.toArray( new IKalypsoStyle[m_defaultStyles.size()] );
    for( final IKalypsoStyle style : defaultStyles )
      removeStyle( style );

    super.dispose();
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#paint(java.awt.Graphics,
   *      org.kalypsodeegree.graphics.transformation.GeoTransform, java.lang.Boolean,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
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
  @Override
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
  @Override
  public void postCommand( final ICommand command, final Runnable runnable )
  {
    m_commandTarget.postCommand( command, runnable );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#getBoundingBox()
   */
  @Override
  public GM_Envelope getFullExtent( )
  {
    if( m_theme != null )
      return m_theme.getFullExtent();
    return null;
  }

  /**
   * @see org.kalypso.util.pool.IPoolListener#objectLoaded(org.kalypso.util.pool.IPoolableObjectType, java.lang.Object,
   *      org.eclipse.core.runtime.IStatus)
   */
  @Override
  public void objectLoaded( final IPoolableObjectType key, final Object newValue, final IStatus status )
  {
    if( KeyComparator.getInstance().compare( key, m_layerKey ) == 0 )
    {
      m_loaded = true;

      try
      {
        setTheme( null );

        if( newValue != null )
        {
          final CommandableWorkspace commandableWorkspace = (CommandableWorkspace) newValue;

          /* Get current property set */
          final KalypsoFeatureTheme kalypsoFeatureTheme = new KalypsoFeatureTheme( commandableWorkspace, m_featurePath, getName(), m_selectionManager, getMapModell() );
          setTheme( kalypsoFeatureTheme );
        }
      }
      catch( final Throwable e )
      {
        final IStatus errorStatus = StatusUtilities.createStatus( IStatus.ERROR, Messages.getString( "org.kalypso.ogc.gml.GisTemplateFeatureTheme.1" ) + e.toString(), e ); //$NON-NLS-1$
        KalypsoGisPlugin.getDefault().getLog().log( errorStatus );
        return;
      }
      finally
      {
        // REMARK: accessing the full extent here may cause dead lock due to access to pool via x-linked features at
        // this
        // point.
        // Also: Causes the map to hang during loading, so we don't do it, even if now the map always gets repaintet,
        // even
        // if the theme extent does not cover the map. invalidate( getFullExtent() );
        fireRepaintRequested( null );
        fireContextChanged();

        if( m_theme != null && !m_theme.getStatus().isOK() )
          setStatus( m_theme.getStatus() );
        else
          setStatus( status );
      }
    }
  }

  private void setTheme( final KalypsoFeatureTheme theme )
  {
    if( theme == null )
    {
      // clear the old theme
      if( m_theme != null )
      {
        m_theme.dispose();
        m_theme = null;
      }

      return;
    }

    // Get some properties from myself (as long as m_theme == null )
    final String legendIcon = getLegendIcon();
    final boolean shouldShowLegendChildren = shouldShowLegendChildren();
    final String[] propertyNames = getPropertyNames();
    final Map<String, String> properties = new HashMap<String, String>();
    for( final String propName : propertyNames )
    {
      final String value = getProperty( propName, null );
      properties.put( propName, value );
    }

    m_theme = theme;

    /* Put current property set into m_theme */
    m_theme.setLegendIcon( legendIcon, getContext() );
    m_theme.setShowLegendChildren( shouldShowLegendChildren );
    for( final String propName : propertyNames )
      m_theme.setProperty( propName, properties.get( propName ) );

    m_theme.addKalypsoThemeListener( m_themeListener );

    m_commandTarget = new JobExclusiveCommandTarget( m_theme.getWorkspace(), null );

    // UGLY HACK: if this theme is used as table-container, do not be concerned about styles (else we get ugly
    // exceptions)
    if( !m_hasStyles )
      return;

    boolean hasSelectionStyle = false;
    for( final IKalypsoStyle style : m_styles )
    {
      addStyleInternal( style );
      if( style.isUsedForSelection() )
        hasSelectionStyle = true;
    }

    final IFeatureType featureType = getFeatureType();
    final URL context = m_layerKey.getContext();
    if( m_styles.isEmpty() )
      addDefaultStyle( featureType, context, false );

    if( !hasSelectionStyle )
      addDefaultStyle( featureType, context, true );
  }

  private void addDefaultStyle( final IFeatureType featureType, final URL context, final boolean usedForSelection )
  {
    final IKalypsoStyle style = createDefaultStyle( featureType, context, usedForSelection );
    if( style != null )
    {
      m_defaultStyles.add( style );
      addStyleInternal( style );
    }
  }

  private static IKalypsoStyle createDefaultStyle( final IFeatureType featureType, final URL context, final boolean usedForSelection )
  {
    if( featureType == null )
      return null;

    /* Try to find a style from the catalogue */
    final CatalogSLD styleCatalog = KalypsoCorePlugin.getDefault().getSLDCatalog();
    final IUrlResolver2 resolver = new IUrlResolver2()
    {
      @Override
      public URL resolveURL( final String href ) throws MalformedURLException
      {
        return UrlResolverSingleton.resolveUrl( context, href );
      }
    };

    String ftsURN;
    if( usedForSelection )
      ftsURN = CatalogSLD.getSelectedURN( featureType );
    else
      ftsURN = CatalogSLD.getDefaultURN( featureType );

    // check, if this urn resolves in the catalog
    URL ftsURL = null;
    try
    {
      ftsURL = styleCatalog.getURL( resolver, ftsURN, ftsURN );
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();
    }

    if( ftsURL == null )
    {
      // REMARK: in theory, we could also create a default selection style, but this
      // breaks too much old projects. So in this case the HighlightGraphics will still
      // be used
      if( usedForSelection )
        return null;

      // we could not find a definition for this feature type. So we use a global default style
      final IValuePropertyType defaultGeometryProperty = featureType.getDefaultGeometryProperty();
      if( defaultGeometryProperty == null )
      {
        // No registered style and the features have no geometry; we just do nothing
        return null;
      }

      // if we have a default geometry (which will be used by the display element)
      // we try to use a geometry-specific default-style
      if( usedForSelection )
        ftsURN = CatalogSLDUtils.getDefaultSelectionStyleURN( defaultGeometryProperty.getValueQName() );
      else
        ftsURN = CatalogSLDUtils.getDefaultStyleURN( defaultGeometryProperty.getValueQName() );
    }

    final PoolableObjectType sldPoolableObjectType = new PoolableObjectType( "sld", ftsURN, context ); //$NON-NLS-1$
    return new GisTemplateFeatureTypeStyle( sldPoolableObjectType, usedForSelection ); //$NON-NLS-1$
  }

  @Override
  public void objectInvalid( final IPoolableObjectType key, final Object oldValue )
  {
    if( KeyComparator.getInstance().compare( key, m_layerKey ) == 0 )
    {
      // clear the theme
      setStatus( new Status( IStatus.WARNING, KalypsoGisPlugin.getId(), Messages.getString( "org.kalypso.ogc.gml.GisTemplateFeatureTheme.2" ) ) ); //$NON-NLS-1$
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
  @Override
  public CommandableWorkspace getWorkspace( )
  {
    if( m_theme != null )
      return m_theme.getWorkspace();

    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoFeatureTheme#getFeatureType()
   */
  @Override
  public IFeatureType getFeatureType( )
  {
    if( m_theme != null )
      return m_theme.getFeatureType();

    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoFeatureTheme#getFeaturePath()
   */
  @Override
  public String getFeaturePath( )
  {
    if( m_theme != null )
      return m_theme.getFeaturePath();

    return null;
  }

  @Override
  public void addStyle( final IKalypsoStyle style )
  {
    m_styles.add( style );

    addStyleInternal( style );
  }

  private void addStyleInternal( final IKalypsoStyle style )
  {
    style.addStyleListener( this );

    if( m_theme != null )
      m_theme.addStyle( style );
  }

  @Override
  public void removeStyle( final IKalypsoStyle style )
  {
    if( m_theme != null )
      m_theme.removeStyle( style );

    style.dispose();
  }

  @Override
  public IKalypsoStyle[] getStyles( )
  {
    if( m_theme != null )
      return m_theme.getStyles();

    return new IKalypsoStyle[0];
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoFeatureTheme#getFeatureList()
   */
  @Override
  public FeatureList getFeatureList( )
  {
    if( m_theme != null )
      return m_theme.getFeatureList();
    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoFeatureTheme#getSchedulingRule()
   */
  @Override
  public ISchedulingRule getSchedulingRule( )
  {
    return m_commandTarget.getSchedulingRule();
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoFeatureTheme#getFeatureListVisible(org.kalypsodeegree.model.geometry.GM_Envelope)
   */
  @Override
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
    for( final IKalypsoStyle style : m_styles )
      if( !style.isLoaded() )
        return false;

    return m_loaded;
  }

  /**
   * This function returns the unmodified href. This href will be saved again to the map.
   * 
   * @return The unmodified href. This href will be saved again to the map.
   */
  public String getHref( )
  {
    return m_href;
  }

  public IPoolableObjectType getLayerKey( )
  {
    return m_layerKey;
  }

  /**
   * @see org.kalypso.util.pool.IPoolListener#isDisposed()
   */
  @Override
  public boolean isDisposed( )
  {
    return m_disposed;
  }

  @Override
  public IFeatureSelectionManager getSelectionManager( )
  {
    return m_selectionManager;
  }

  /**
   * @see org.kalypso.util.pool.IPoolListener#dirtyChanged(org.kalypso.util.pool.IPoolableObjectType, boolean)
   */
  @Override
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

  @Override
  public String getLabel( )
  {
    if( m_theme != null )
      return m_theme.getLabel();

    return super.getLabel();
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#getDefaultIcon()
   */
  @Override
  public ImageDescriptor getDefaultIcon( )
  {
    if( m_theme != null )
      return m_theme.getDefaultIcon();

    return KalypsoGisPlugin.getImageProvider().getImageDescriptor( ImageProvider.DESCRIPTORS.IMAGE_THEME_FEATURE );
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

  /**
   * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
   */
  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
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
      return m_theme.getLegendIcon();

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
   * @see org.kalypso.ogc.gml.IKalypsoStyleListener#styleChanged(org.kalypso.ogc.gml.KalypsoUserStyle)
   */
  @Override
  public void styleChanged( )
  {
    fireStatusChanged( this );
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
    else // HM: this will probably cause problems, as the theme is not really loaded
      // But else, the stuff waiting for the map to load will wait forever...
      if( !checkPoolListener )
        m_loaded = true;

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
      if( poolListener == this )
        return true;

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

  @Override
  public boolean isDirty( )
  {
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    if( m_layerKey == null )
      return false;

    final KeyInfo key = pool.getInfoForKey( m_layerKey );
    if( key == null )
      return false;

    return key.isDirty();
  }
}