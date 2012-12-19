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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.commons.KalypsoCommonsExtensions;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.commons.i18n.ITranslator;
import org.kalypso.commons.i18n.ITranslatorContext;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.gml.map.themes.KalypsoLegendTheme;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.mapmodel.IKalypsoThemeVisitor;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.IMapModellListener;
import org.kalypso.ogc.gml.mapmodel.MapModell;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.template.gismapview.Gismapview;
import org.kalypso.template.gismapview.Gismapview.Layers;
import org.kalypso.template.types.ExtentType;
import org.kalypso.template.types.I18NTranslatorType;
import org.kalypso.template.types.StyledLayerType;
import org.kalypso.template.types.StyledLayerType.Property;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.w3c.dom.Element;

/**
 * @author Gernot Belger
 */
public class GisTemplateMapModell implements IKalypsoLayerModell, ITranslatorContext
{
  private final IFeatureSelectionManager m_selectionManager;

  private final IMapModell m_modell;

  private boolean m_isLoaded = true;

  public GisTemplateMapModell( final URL context, final String crs, final IFeatureSelectionManager selectionManager )
  {
    m_selectionManager = selectionManager;
    m_modell = new MapModell( crs, context );

    setName( new I10nString( Messages.getString( "org.kalypso.ogc.gml.GisTemplateMapModell.0" ), null ) ); //$NON-NLS-1$
  }

  /**
   * Replaces layers based on Gismapview template. Resolves cascading themes if necessary.
   * 
   * @throws CoreException
   *           if a theme in the {@link Gismapview} cannot be loaded.
   */
  public void createFromTemplate( final Gismapview gisview ) throws CoreException
  {
    m_isLoaded = false;

    try
    {
      final ITranslator translator = createTranslator( gisview );
      final I10nString name = new I10nString( gisview.getName(), translator );
      setName( name );

      for( final IKalypsoTheme theme : getAllThemes() )
        if( !(theme instanceof KalypsoLegendTheme) )
          removeTheme( theme );
      final Layers layerListType = gisview.getLayers();

      if( layerListType != null )
      {
        final Object activeLayer = layerListType.getActive();
        final List<JAXBElement< ? extends StyledLayerType>> layerList = layerListType.getLayer();

        createFromTemplate( layerList, activeLayer );
      }
    }
    finally
    {
      m_isLoaded = true;
    }
  }

  private ITranslator createTranslator( final Gismapview gisview )
  {
    final I18NTranslatorType translatorElement = gisview.getTranslator();
    if( translatorElement == null )
      return null;

    final ITranslator translator = KalypsoCommonsExtensions.createTranslator( translatorElement.getId() );
    if( translator != null )
      translator.configure( this, translatorElement.getAny() );
    return translator;
  }

  public void createFromTemplate( final List<JAXBElement< ? extends StyledLayerType>> layerList, final Object activeLayer ) throws CoreException
  {
    for( final JAXBElement< ? extends StyledLayerType> layerType : layerList )
    {
      final IKalypsoTheme theme = addLayer( layerType.getValue() );
      if( layerType.getValue() == activeLayer )
        activateTheme( theme );
    }
  }

  @Override
  public void setName( final I10nString name )
  {
    m_modell.setName( name );
  }

  @Override
  public IKalypsoTheme addLayer( final StyledLayerType layer ) throws CoreException
  {
    final URL context = m_modell.getContext();

    final IKalypsoTheme theme = loadTheme( layer, context );
    if( theme != null )
    {
      theme.setVisible( layer.isVisible() );
      addTheme( theme );
    }
    return theme;
  }

  @Override
  public IKalypsoTheme insertLayer( final StyledLayerType layer, final int position ) throws Exception
  {
    final URL context = m_modell.getContext();

    final IKalypsoTheme theme = loadTheme( layer, context );
    if( theme != null )
    {
      insertTheme( theme, position );
      theme.setVisible( layer.isVisible() );
    }
    return theme;
  }

  @Override
  public void dispose( )
  {
    m_modell.dispose();
  }

  private IKalypsoTheme loadTheme( final StyledLayerType layerType, final URL context ) throws CoreException
  {
    final JAXBElement<String> lg = layerType.getLegendicon();
    final String legendIcon = lg == null ? null : lg.getValue();

    final JAXBElement<Boolean> sC = layerType.getShowChildren();
    final boolean showChildren = sC == null ? true : sC.getValue().booleanValue();

    final String id = layerType.getId();

    final String linktype = layerType.getLinktype();
    final ITranslator translator = getName().getTranslator();
    final I10nString layerName = new I10nString( layerType.getName(), translator );

    final IKalypsoThemeFactory themeFactory = ThemeFactoryExtension.getThemeFactory( linktype );
    if( themeFactory == null )
      throw new UnsupportedOperationException( Messages.getString( "org.kalypso.ogc.gml.GisTemplateMapModell.1", layerName.getValue(), linktype ) ); //$NON-NLS-1$

    final IKalypsoTheme theme = themeFactory.createTheme( layerName, layerType, context, this, m_selectionManager );
    if( theme instanceof AbstractKalypsoTheme )
    {
      ((AbstractKalypsoTheme)theme).setLegendIcon( legendIcon, context );
      ((AbstractKalypsoTheme)theme).setShowLegendChildren( showChildren );
      ((AbstractKalypsoTheme)theme).setId( id );
    }

    /* Read the properties. */
    final List<Property> properties = layerType.getProperty();
    for( final Property property : properties )
      theme.setProperty( property.getName(), property.getValue() );

    return theme;
  }

  /**
   * Create the gismapview object from the current state of the model.
   */
  public synchronized Gismapview createGismapTemplate( final GM_Envelope bbox, final String srsName, IProgressMonitor monitor ) throws CoreException
  {
    /* If no progress monitor was given, take the null progress monitor. */
    if( monitor == null )
      monitor = new NullProgressMonitor();

    try
    {
      /* Get all themes. */
      final IKalypsoTheme[] themes = m_modell.getAllThemes();

      /* Monitor. */
      monitor.beginTask( Messages.getString( "org.kalypso.ogc.gml.GisTemplateMapModell.10" ), themes.length * 1000 + 1000 ); //$NON-NLS-1$

      /* Create the gismap view. */
      final Gismapview gismapview = GisTemplateHelper.OF_GISMAPVIEW.createGismapview();

      /* Create the name. */
      final I10nString name = getName();
      gismapview.setName( name.getKey() );

      /* Create the translator. */
      final ITranslator i10nTranslator = name.getTranslator();
      if( i10nTranslator != null )
      {
        final I18NTranslatorType translator = GisTemplateHelper.OF_TEMPLATE_TYPES.createI18NTranslatorType();
        translator.setId( i10nTranslator.getId() );
        final List<Element> configuration = i10nTranslator.getConfiguration();
        if( configuration != null )
          translator.getAny().addAll( configuration );
        gismapview.setTranslator( translator );
      }

      /* Set the bounding box. */
      if( bbox != null )
      {
        final ExtentType extentType = GisTemplateHelper.OF_TEMPLATE_TYPES.createExtentType();
        extentType.setTop( bbox.getMax().getY() );
        extentType.setBottom( bbox.getMin().getY() );
        extentType.setLeft( bbox.getMin().getX() );
        extentType.setRight( bbox.getMax().getX() );
        extentType.setSrs( srsName );
        gismapview.setExtent( extentType );
      }

      /* Create the layers. */
      final Layers layersType = GisTemplateHelper.OF_GISMAPVIEW.createGismapviewLayers();
      final List<JAXBElement< ? extends StyledLayerType>> layerList = layersType.getLayer();
      gismapview.setLayers( layersType );

      /* Monitor. */
      monitor.worked( 500 );

      /* Collect already used ids. */
      final List<String> usedIds = new ArrayList<>();
      for( final IKalypsoTheme theme : themes )
      {
        /* Get the id of the theme. */
        final String usedId = theme.getId();
        if( usedId != null && usedId.length() > 0 && !usedIds.contains( usedId ) )
        {
          /* If one is set and not already used, store it. */
          usedIds.add( usedId );
          continue;
        }

        /* Otherwise the theme had no id set or a duplicate id was detected. */
        /* Mark the theme of beeing able to receive a new id. */
        theme.setId( null );
      }

      /* Monitor. */
      monitor.worked( 500 );

      /* Loop all themes. */
      for( final IKalypsoTheme theme : themes )
      {
        String id = theme.getId();
        if( id == null || id.length() == 0 )
        {
          id = MapUtilities.getNewId( usedIds );
          usedIds.add( id );
        }

        final JAXBElement< ? extends StyledLayerType> layerElement = GisTemplateLayerHelper.configureLayer( theme, id, bbox, srsName, new SubProgressMonitor( monitor, 1000 ) );
        if( layerElement != null )
        {
          layerList.add( layerElement );

          final StyledLayerType layer = layerElement.getValue();

          if( m_modell.isThemeActivated( theme ) && !(theme instanceof KalypsoLegendTheme) )
            layersType.setActive( layer );

          if( theme instanceof AbstractKalypsoTheme )
          {
            final AbstractKalypsoTheme kalypsoTheme = (AbstractKalypsoTheme)theme;
            final String legendIcon = kalypsoTheme.getLegendIcon();
            if( legendIcon != null )
              layer.setLegendicon( GisTemplateHelper.OF_TEMPLATE_TYPES.createStyledLayerTypeLegendicon( legendIcon ) );
          }
        }
      }

      return gismapview;
    }
    finally
    {
      /* Monitor. */
      monitor.done();
    }
  }

  public synchronized void saveGismapTemplate( final GM_Envelope bbox, final String srsName, IProgressMonitor monitor, final IFile file ) throws CoreException
  {
    if( monitor == null )
      monitor = new NullProgressMonitor();

    ByteArrayInputStream bis = null;
    try
    {
      final IKalypsoTheme[] themes = m_modell.getAllThemes();
      monitor.beginTask( Messages.getString( "org.kalypso.ogc.gml.GisTemplateMapModell.10" ), themes.length * 1000 + 1000 ); //$NON-NLS-1$

      final Gismapview gismapview = createGismapTemplate( bbox, srsName, new SubProgressMonitor( monitor, 100 ) );

      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      GisTemplateHelper.saveGisMapView( gismapview, bos, file.getCharset() );
      bos.close();
      bis = new ByteArrayInputStream( bos.toByteArray() );
      if( file.exists() )
        file.setContents( bis, false, true, new SubProgressMonitor( monitor, 900 ) );
      else
        file.create( bis, false, new SubProgressMonitor( monitor, 900 ) );

      bis.close();
    }
    catch( final Throwable e )
    {
      e.printStackTrace();
      throw new CoreException( StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.ogc.gml.GisTemplateMapModell.11" ) ) ); //$NON-NLS-1$
    }
    finally
    {
      monitor.done();

      IOUtils.closeQuietly( bis );
    }
  }

  @Override
  public void activateTheme( final IKalypsoTheme theme )
  {
    m_modell.activateTheme( theme );
  }

  @Override
  public void internalActivate( final IKalypsoTheme theme )
  {
    m_modell.internalActivate( theme );
  }

  @Override
  public void addTheme( final IKalypsoTheme theme )
  {
    m_modell.addTheme( theme );
  }

  @Override
  public IKalypsoTheme getActiveTheme( )
  {
    return m_modell.getActiveTheme();
  }

  @Override
  public IKalypsoTheme[] getAllThemes( )
  {
    return m_modell.getAllThemes();
  }

  @Override
  public String getCoordinatesSystem( )
  {
    return m_modell.getCoordinatesSystem();
  }

  @Override
  public GM_Envelope getFullExtentBoundingBox( )
  {
    return m_modell.getFullExtentBoundingBox();
  }

  @Override
  public IKalypsoTheme getTheme( final int pos )
  {
    return m_modell.getTheme( pos );
  }

  @Override
  public int getThemeSize( )
  {
    return m_modell.getThemeSize();
  }

  @Override
  public boolean isThemeActivated( final IKalypsoTheme theme )
  {
    return m_modell.isThemeActivated( theme );
  }

  @Override
  public void moveDown( final IKalypsoTheme theme )
  {
    m_modell.moveDown( theme );
  }

  @Override
  public void moveUp( final IKalypsoTheme theme )
  {
    m_modell.moveUp( theme );
  }

  @Override
  public IStatus paint( final Graphics g, final GeoTransform p, final IProgressMonitor monitor )
  {
    return m_modell.paint( g, p, monitor );
  }

  @Override
  public void removeTheme( final IKalypsoTheme theme )
  {
    m_modell.removeTheme( theme );
    theme.dispose();
  }

  @Override
  public void swapThemes( final IKalypsoTheme theme1, final IKalypsoTheme theme2 )
  {
    m_modell.swapThemes( theme1, theme2 );
  }

  @Override
  public URL getContext( )
  {
    return m_modell.getContext();
  }

  public IMapModell getModell( )
  {
    return m_modell;
  }

  @Override
  public void accept( final IKalypsoThemeVisitor visitor, final int depth )
  {
    m_modell.accept( visitor, depth );
  }

  @Override
  public void insertTheme( final IKalypsoTheme theme, final int position )
  {
    m_modell.insertTheme( theme, position );
  }

  @Override
  public I10nString getName( )
  {
    return m_modell.getName();
  }

  @Override
  public void accept( final IKalypsoThemeVisitor visitor, final int depth_infinite, final IKalypsoTheme theme )
  {
    m_modell.accept( visitor, depth_infinite, theme );
  }

  @Override
  public String getLabel( )
  {
    return m_modell.getLabel();
  }

  @Override
  public void addMapModelListener( final IMapModellListener l )
  {
    m_modell.addMapModelListener( l );
  }

  @Override
  public void removeMapModelListener( final IMapModellListener l )
  {
    m_modell.removeMapModelListener( l );
  }

  @Override
  public Object getThemeParent( final IKalypsoTheme theme )
  {
    return this;
  }

  @Override
  public boolean isLoaded( )
  {
    return m_isLoaded;
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    if( adapter.equals( IMapModell.class ) )
      return this;

    if( adapter.equals( GisTemplateMapModell.class ) )
      return this;

    return null;
  }
}