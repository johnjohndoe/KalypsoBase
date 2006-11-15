/**
 * 
 */
package org.kalypso.gml.ui.wizard.imports;

import java.io.File;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;
import javax.xml.namespace.QName;

import ogc31.www.opengis.net.gml.FileType;
import ogc31.www.opengis.net.gml.FileValueModelType;
import ogc31.www.opengis.net.gml.ObjectFactory;
import ogc31.www.opengis.net.gml.RangeSetType;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.kalypso.commons.resources.SetContentHelper;
import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.eclipse.jface.wizard.FileSelectWizardPage;
import org.kalypso.contribs.ogc31.KalypsoOGC31JAXBcontext;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.ogc.gml.WorldFile;
import org.kalypso.ogc.gml.WorldFileReader;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.model.coverage.GridRange;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.cs.ConvenienceCSFactory;
import org.kalypsodeegree_impl.model.cv.GridRange_Impl;
import org.kalypsodeegree_impl.model.cv.RectifiedGridDomain;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.opengis.cs.CS_CoordinateSystem;

/**
 * @author Gernot Belger
 */
public class RectifiedGridCoverageImportWizard extends Wizard implements IImportWizard
{
  private IStructuredSelection m_selection;

  private WizardNewFileCreationPage m_fileCreationPage;

  private FileSelectWizardPage m_fileSelectWizardPage;

  public RectifiedGridCoverageImportWizard( )
  {
    final IDialogSettings dialogSettings = KalypsoGmlUIPlugin.getDefault().getDialogSettings();
    final IDialogSettings settings = createDialogSettings( dialogSettings, "ImportRectifiedGridCoverageWizardSettings" );
    setDialogSettings( settings );
    setNeedsProgressMonitor( true );
  }

  private IDialogSettings createDialogSettings( final IDialogSettings dialogSettings, final String name )
  {
    final IDialogSettings section = dialogSettings.getSection( name );
    if( section != null )
      return section;

    return dialogSettings.addNewSection( name );
  }

  /**
   * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
   *      org.eclipse.jface.viewers.IStructuredSelection)
   */
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    m_selection = selection;
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#addPages()
   */
  @Override
  public void addPages( )
  {
    m_fileSelectWizardPage = new FileSelectWizardPage( "fileSectPage", "", new String[] { "*.tif", "*.gif", "*.jpg", "*.*" } );

    m_fileCreationPage = new WizardNewFileCreationPage( "newFile", m_selection );
    m_fileCreationPage.setTitle( "Auswahl der Datei" );
    m_fileCreationPage.setDescription( "Wählen Sie Speichertort und Name der neuen Datei aus." );
    // TODO: set file name corresponding to input file name
    m_fileCreationPage.setFileName( "RectifiedGridCoverage.gml" );

    addPage( m_fileSelectWizardPage );
    addPage( m_fileCreationPage );

    super.addPages();
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    // read/copy input
    final String inputFilePath = m_fileSelectWizardPage.getFilePath();

    final IPath containerName = m_fileCreationPage.getContainerFullPath();
    final String gmlFileName = m_fileCreationPage.getFileName();

    final ICoreRunnableWithProgress op = new ICoreRunnableWithProgress()
    {
      public IStatus execute( final IProgressMonitor monitor ) throws InvocationTargetException
      {
        monitor.beginTask( "Creating " + gmlFileName, 4 );

        try
        {
          //
          // Init KALYPSO
          //
          monitor.subTask( " - initializing KALYPSO..." );
          KalypsoGisPlugin.getDefault();

          // some commonly needed variables
          final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
          final IResource resource = root.findMember( containerName );
          final IContainer container = (IContainer) resource;
          final IFile gmlFileResource = container.getFile( new Path( gmlFileName ) );
          final File importFile = new File( inputFilePath );
          final IFile importFileResource = container.getFile( new Path( importFile.getName() ) );

          monitor.worked( 1 );

          //
          // Read world file
          //
          // TODO: get crs from user via wizard page
          final CS_CoordinateSystem crs = ConvenienceCSFactory.getInstance().getOGCCSByName( "EPSG:31467" );

          final WorldFile worldFile = new WorldFileReader().readWorldFileForImageFile( importFile );

          final RenderedOp image = JAI.create( "fileload", inputFilePath );
          final TiledImage tiledImage = new TiledImage( image, true );
          int height = tiledImage.getHeight();
          int width = tiledImage.getWidth();

          final GM_Envelope envelope = GeometryFactory.createGM_Envelope( worldFile.getUlcx(), worldFile.getUlcy() + (height * worldFile.getDx()), worldFile.getUlcx() + (width * worldFile.getDx()), worldFile.getUlcy() );

          final double[] lows = new double[] { 0, 0 };
          final double[] highs = new double[] { width, height };

          final GridRange gridRange = new GridRange_Impl( lows, highs );

          final GM_Point origin = GeometryFactory.createGM_Point( envelope.getMin().getX(), envelope.getMin().getY(), crs );

          final double[] offset = new double[] { worldFile.getDx(), worldFile.getDy() };

          //
          // Workspace && create workspace
          //
          monitor.subTask( " - creating GMLWorkspace..." );
          final GMLWorkspace workspace = FeatureFactory.createGMLWorkspace( new QName( NS.GML3, "RectifiedGridCoverage" ), "3.1", null, null );

          final FileType rangeSetFile = KalypsoOGC31JAXBcontext.GML3_FAC.createFileType();

          // file name relative to the gml
          rangeSetFile.setFileName( importFileResource.getName() );
          rangeSetFile.setMimeType( "image/" + importFileResource.getFileExtension() );
          rangeSetFile.setFileStructure( FileValueModelType.RECORD_INTERLEAVED );

          final RectifiedGridDomain domain = new RectifiedGridDomain( origin, offset, gridRange );

          final RangeSetType rangeSet = KalypsoOGC31JAXBcontext.GML3_FAC.createRangeSetType();
          rangeSet.setFile( rangeSetFile );

          
          // TODO: use the RectifiedGridCoverage-class instead
          final Feature rgcFeature = workspace.getRootFeature();
          // TODO: uncomment, as soon as we are able to serialize GM_Envelopes 
//          rgcFeature.setProperty( new QName( NS.GML3, "boundedBy" ), domain.getGM_Envelope( crs ) );
          rgcFeature.setProperty( new QName( NS.GML3, "description" ), "Imported via Kalypso from " + inputFilePath );
          rgcFeature.setProperty( new QName( NS.GML3, "rectifiedGridDomain" ), domain );
          rgcFeature.setProperty( new QName( NS.GML3, "rangeSet" ), rangeSet );

          monitor.worked( 1 );

          //
          // Import external file
          //
          monitor.subTask( " - creating GMLWorkspace..." );
          final List<File> fileList = new ArrayList<File>();
          fileList.add( importFile );

          final IOverwriteQuery overwriteQuery = new IOverwriteQuery()
          {
            public String queryOverwrite( final String pathString )
            {
              if( MessageDialog.openQuestion( getShell(), "Datei import", "Die Datei " + pathString + " exisitert bereits.\nSoll Sie überschrieben werden?" ) )
                return YES;
              return NO;
            }
          };
          final ImportOperation operation = new ImportOperation( containerName, FileSystemStructureProvider.INSTANCE, overwriteQuery, fileList );
          operation.setOverwriteResources( false );
          operation.setCreateContainerStructure( false );
          operation.setContext( getShell() );
          operation.run( new SubProgressMonitor( monitor, 1 ) );
          final IStatus importStatus = operation.getStatus();
          if( !importStatus.isOK() )
            return importStatus;

          //
          // create file
          //
          monitor.subTask( "- writing " + gmlFileName );
          if( !resource.exists() || !(resource instanceof IContainer) )
            StatusUtilities.createErrorStatus( "Container \"" + containerName + "\" does not exist." );

          final SetContentHelper contentHelper = new SetContentHelper()
          {
            @Override
            protected void write( final OutputStreamWriter writer ) throws Throwable
            {
              GmlSerializer.serializeWorkspace( writer, workspace );
            }
          };
          contentHelper.setFileContents( gmlFileResource, false, true, new SubProgressMonitor( monitor, 1 ) );
          monitor.worked( 1 );

          // //
          // // Open in Editor
          // //
          // monitor.subTask( " - opening file for editing..." );
          // getShell().getDisplay().asyncExec( new Runnable()
          // {
          // public void run( )
          // {
          // try
          // {
          // IDE.openEditor( page, file, true );
          // }
          // catch( final PartInitException e )
          // {
          // ErrorDialog.openError(getShell(), "Neue GML Datei", "Editor konnte nicht geöffnet werden.", e.getStatus());
          // }
          // }
          // } );
          // monitor.worked( 1 );

          return Status.OK_STATUS;
        }
        catch( final Throwable e )
        {
          throw new InvocationTargetException( e );
        }
        finally
        {
          monitor.done();
        }

      }
    };

    final IStatus status = RunnableContextHelper.execute( getContainer(), false, false, op );
    ErrorDialog.openError( getShell(), "Neue GML Datei", "Fehler beim Erzeugen der Datei", status );
    return status.isOK();
  }
}
