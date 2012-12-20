/**
 *
 */
package org.kalypso.kml.export.wizard;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.kml.export.KMLExportDelegate;
import org.kalypso.kml.export.KalypsoKMLPlugin;
import org.kalypso.kml.export.constants.IKMLExportSettings;
import org.kalypso.kml.export.convert.StyleConverter;
import org.kalypso.kml.export.interfaces.IKMLAdapter;
import org.kalypso.kml.export.utils.ThemeGoogleEarthExportable;
import org.kalypso.ogc.gml.IKalypsoCascadingTheme;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.visitor.KalypsoThemeVisitor;
import org.kalypso.ogc.gml.painter.IStylePainter;
import org.kalypso.ogc.gml.painter.StylePainterFactory;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
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

  private final IMapPanel m_mapPanel;

  private final IKMLAdapter[] m_provider;

  public KMLExporter( final IMapPanel mapPanel, final IKMLExportSettings settings )
  {
    m_mapPanel = mapPanel;
    m_settings = settings;

    /* get extension points for rendering geometries and adding additional geometries */
    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IConfigurationElement[] elements = registry.getConfigurationElementsFor( IKMLAdapter.ID );

    // TODO handling of several providers, which provider wins / rules, returns a special geometry, aso
    final List<IKMLAdapter> provider = new ArrayList<>( elements.length );
    for( final IConfigurationElement element : elements )
    {
      try
      {
        provider.add( (IKMLAdapter)element.createExecutableExtension( "class" ) ); //$NON-NLS-1$
      }
      catch( final CoreException e )
      {
        e.printStackTrace();
      }
    }

    m_provider = provider.toArray( new IKMLAdapter[] {} );
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    try
    {
      /* basic data stuff for processing */
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
      folder.setDescription( "http://sourceforge.net/projects/kalypso/" ); //$NON-NLS-1$

      /* process map */
      final KalypsoThemeVisitor visitor = new KalypsoThemeVisitor( new ThemeGoogleEarthExportable() );
      for( final IKalypsoTheme theme : mapModell.getAllThemes() )
        visitor.visit( theme );

      final IKalypsoTheme[] themes = visitor.getFoundThemes();
      for( final IKalypsoTheme theme : themes )
      {
        processTheme( folder, theme );
      }

      // final StyleTypeFactory styleFactory = StyleTypeFactory.getStyleFactory();
      // styleFactory.addStylesToDocument( document );

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
      try( final BufferedOutputStream outputStream = new BufferedOutputStream( new FileOutputStream( m_settings.getExportFile() ) ) )
      {
        // REMARK: we are directly using JAXB instead of calling: kml.marshal( outputStream );
        // in order to use jaxb from KalypsoCommons, so we avoid having the whole jaxb in this plugin

        final JAXBContext jaxbContext = JAXBContext.newInstance( Kml.class );
        final Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
        // marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new Kml.NameSpaceBeautyfier());
        marshaller.marshal( kml, outputStream );
      }
    }
    catch( final Exception e )
    {
      return new Status( IStatus.ERROR, "GoogleEarthExporter", e.getMessage(), e ); //$NON-NLS-1$
    }

    return Status.OK_STATUS;
  }

  private void processTheme( final Folder parentFolder, final IKalypsoTheme theme )
  {
    if( !theme.isVisible() )
      return;

    /* get inner themes */
    if( theme instanceof IKalypsoCascadingTheme )
    {
      final Folder folder = parentFolder.createAndAddFolder();
      folder.setName( theme.getName().getValue() );

      final IKalypsoCascadingTheme cascading = (IKalypsoCascadingTheme)theme;
      final IMapModell inner = cascading.getInnerMapModel();

      final IKalypsoTheme[] themes = inner.getAllThemes();
      for( final IKalypsoTheme t : themes )
      {
        processTheme( folder, t );
      }
    }
    /* "paint" inner themes */
    else if( theme instanceof IKalypsoFeatureTheme )
    {
      try
      {
        final File imageDir = m_settings.getExportFile().getParentFile();
        final String label = theme.getLabel();

        final Folder folder = parentFolder.createAndAddFolder();
        folder.setName( label );

        final String validImageName = FileUtilities.validateName( label, "_" ); //$NON-NLS-1$

        final IKalypsoFeatureTheme ft = (IKalypsoFeatureTheme)theme;

        final double scale = m_mapPanel.getCurrentScale();
        final GM_Envelope bbox = m_mapPanel.getBoundingBox();

        final GeoTransform world2screen = m_mapPanel.getProjection();

        final StyleConverter converter = new StyleConverter( imageDir, validImageName, world2screen );

        final KMLExportDelegate delegate = new KMLExportDelegate( m_provider, folder, scale, bbox, converter );

        final IStylePainter painter = StylePainterFactory.create( ft, null );
        painter.paint( delegate, new NullProgressMonitor() );
      }
      catch( final CoreException e )
      {
        KalypsoKMLPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
      finally
      {
      }
    }
  }
}