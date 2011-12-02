/**
 *
 */
package org.kalypso.kml.export.wizard;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.kml.export.KMLExportDelegate;
import org.kalypso.kml.export.KMLThemeVisitor;
import org.kalypso.kml.export.KalypsoKMLPlugin;
import org.kalypso.kml.export.constants.IKMLExportSettings;
import org.kalypso.kml.export.interfaces.IKMLAdapter;
import org.kalypso.kml.export.utils.ThemeGoogleEarthExportable;
import org.kalypso.ogc.gml.AbstractCascadingLayerTheme;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.visitor.KalypsoThemeVisitor;
import org.kalypso.ogc.gml.painter.IStylePainter;
import org.kalypso.ogc.gml.painter.StylePainterFactory;
import org.kalypso.ui.views.map.MapView;
import org.kalypsodeegree.model.geometry.GM_Envelope;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;

/**
 * basic runnable to export a map view into a kml file.
 * 
 * @author Dirk Kuch
 */
public class KMLExporter implements ICoreRunnableWithProgress
{
  private final IKMLExportSettings m_settings;

  private final MapView m_view;

  private IMapPanel m_mapPanel;

  private final IKMLAdapter[] m_provider;

  /**
   */
  public KMLExporter( final MapView view, final IKMLExportSettings settings )
  {
    m_view = view;
    m_settings = settings;

    /* get extension points for rendering geometries and adding additional geometries */
    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IConfigurationElement[] elements = registry.getConfigurationElementsFor( IKMLAdapter.ID );

    // TODO handling of several providers, which provider wins / rules, returns a special geometry, aso
    final List<IKMLAdapter> provider = new ArrayList<IKMLAdapter>( elements.length );
    for( final IConfigurationElement element : elements )
    {
      try
      {
        provider.add( (IKMLAdapter) element.createExecutableExtension( "class" ) ); //$NON-NLS-1$
      }
      catch( final CoreException e )
      {
        e.printStackTrace();
      }
    }

    m_provider = provider.toArray( new IKMLAdapter[] {} );
  }

  /**
   * @return
   * @throws IOException
   */
  private File createTmpDir( ) throws IOException
  {
    final URL urlTmpDir = new File( System.getProperty( "java.io.tmpdir" ) ).toURI().toURL(); //$NON-NLS-1$

    Assert.isNotNull( urlTmpDir );

    /* delete old test dir */
    final URL urlBaseDir = new URL( urlTmpDir + "kalypsoGoogleEarthExport/" ); //$NON-NLS-1$

    final File fBaseDir = new File( urlBaseDir.getFile() );
    if( !fBaseDir.exists() )
      FileUtils.forceMkdir( fBaseDir );

    FileUtils.cleanDirectory( fBaseDir );

    return fBaseDir;
  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.
   * IProgressMonitor)
   */
  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    try
    {
      /* basic data stuff for processing */
      m_mapPanel = m_view.getMapPanel();
      final IMapModell mapModell = m_mapPanel.getMapModell();

      /* kml instance and document root */
      final Kml kml = new Kml();
      final Document document = kml.createAndSetDocument();

      /* basic kml settings */
      // TODO
// GoogleEarthUtils.setMapBoundary( m_mapPanel.getBoundingBox(), mapModell.getCoordinatesSystem(), kml, document );
// GoogleEarthUtils.setLookAt( m_mapPanel.getBoundingBox(), mapModell.getCoordinatesSystem(), kml );

      document.setName( m_settings.getExportName() );
      document.setDescription( m_settings.getExportDescription() );

      final Folder folder = kml.createAndSetFolder();
      folder.setName( "Kalypso Google Earth (TM) Export" ); //$NON-NLS-1$
      folder.setDescription( "http://sourceforge.net/projects/kalypso/" );

      /* process map */
      final KalypsoThemeVisitor visitor = new KMLThemeVisitor( new ThemeGoogleEarthExportable() );
      for( final IKalypsoTheme theme : mapModell.getAllThemes() )
        visitor.visit( theme );

      final IKalypsoTheme[] themes = visitor.getFoundThemes();
      for( final IKalypsoTheme theme : themes )
      {
        processTheme( folder, theme );
      }

      // FIXME
// final StyleTypeFactory styleFactory = StyleTypeFactory.getStyleFactory( document );
// styleFactory.addStylesToDocument( documentType );

      // TODO;
// GoogleEarthExportUtils.removeEmtpyFolders( folderType );
// documentType.getAbstractFeatureGroup().add( googleEarthFactory.createFolder( folderType ) );
//
// PlacemarkUtil.addAdditional( folderType, m_provider, googleEarthFactory );

      // remove empty folders / layers
// FolderUtil.removeEmptyFolders( folderType );

      // dispose styles (singelton!)
// styleFactory.dispose();

      /* marshalling */
      kml.marshal( m_settings.getExportFile() );
    }
    catch( final Exception e )
    {
      return new Status( IStatus.ERROR, "GoogleEarthExporter", e.getMessage() ); //$NON-NLS-1$
    }

    return Status.OK_STATUS;
  }

  private void processTheme( final Folder parentFolder, final IKalypsoTheme theme )
  {
    /* get inner themes */
    if( theme instanceof AbstractCascadingLayerTheme )
    {
      final Folder folder = parentFolder.createAndAddFolder();
      folder.setName( theme.getName().getValue() );

      final AbstractCascadingLayerTheme cascading = (AbstractCascadingLayerTheme) theme;
      final GisTemplateMapModell inner = cascading.getInnerMapModel();

      final IKalypsoTheme[] themes = inner.getAllThemes();
      for( final IKalypsoTheme t : themes )
      {
        processTheme( folder, t );
      }
    }
    /* "paint" inner themes */
    else if( theme instanceof IKalypsoFeatureTheme )
      try
      {
        final Folder folder = parentFolder.createAndAddFolder();
        folder.setName( theme.getName().getValue() );
        final IKalypsoFeatureTheme ft = (IKalypsoFeatureTheme) theme;

        final double scale = m_mapPanel.getCurrentScale();
        final GM_Envelope bbox = m_mapPanel.getBoundingBox();
        final KMLExportDelegate delegate = new KMLExportDelegate( m_provider, folder, scale, bbox );

        final IStylePainter painter = StylePainterFactory.create( ft, null );
        painter.paint( delegate, new NullProgressMonitor() );
      }
      catch( final CoreException e )
      {
        KalypsoKMLPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }

  }
}
