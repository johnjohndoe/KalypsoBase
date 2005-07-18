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
package org.kalypso.simulation.ui.wizards.calculation.modelpages;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;
import javax.xml.rpc.ServiceException;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.commons.java.net.UrlResolver;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.java.lang.reflect.ClassUtilities;
import org.kalypso.contribs.java.lang.reflect.ClassUtilityException;
import org.kalypso.metadoc.Document;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.featureview.modfier.StringModifier;
import org.kalypso.ogc.gml.map.MapPanel;
import org.kalypso.ogc.gml.util.FeatureLabelProvider;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.diagview.DiagViewUtils;
import org.kalypso.ogc.sensor.diagview.grafik.GrafikLauncher;
import org.kalypso.ogc.sensor.zml.ZmlURL;
import org.kalypso.services.proxy.IObservationService;
import org.kalypso.simulation.ui.KalypsoSimulationUIPlugin;
import org.kalypso.simulation.ui.calccase.ModelNature;
import org.kalypso.simulation.ui.calccase.jface.CalcCaseTableTreeViewer;
import org.kalypso.simulation.ui.wizards.calculation.TSLinkWithName;
import org.kalypso.template.obsdiagview.ObsdiagviewType;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.calcwizard.bericht.ExportWizardBerichtWizard;
import org.kalypso.ui.calcwizard.bericht.IBerichtExporter;
import org.kalypso.ui.metadoc.util.MultiDocumentServiceWrapper;
import org.kalypso.zml.obslink.TimeseriesLink;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.FeatureType;
import org.kalypsodeegree.model.feature.FeatureTypeProperty;
import org.kalypsodeegree.model.feature.event.ModellEventListener;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;

/**
 * @author Belger
 */
public class ExportResultsWizardPage extends AbstractCalcWizardPage implements ModellEventListener
{
  // Beispiel:
  //   <page className="org.kalypso.ui.calcwizard.ViewResultsWizardPage"
  // pageTitle="Kontrolle der Ergebnisse"
  // imageLocation="icons/calcwizard/boden.gif" >
  //        <arg name="mapTemplate" value=".modellTyp/vorlagen/rechenfall/karte2.gmt"/>
  //        <arg name="tableTemplate"
  // value=".modellTyp/vorlagen/rechenfall/table2.gtt"/>
  //        <arg name="timeseriesPropertyNames"
  // value="Wasserstand#Wasserstand_gerechnet"/>
  //        <arg name="mainSash" value="50"/>
  //        <arg name="rightSash" value="40"/>
  //        <arg name="grafikToolTemplate" value=".modellTyp/grafik.exe_"/>
  //    </page>
  //  

  /**
   * Argument: Ein oder mehrere Exporter für den den Berichtsexport. Mehrere Exporter werden durch mehrere 'exporter'
   * Elemente, unterschieden durch eine beliebig angehängte Zeichenkette unterschieden.
   * <p>
   * Dieses Argumetn kann Unterargumente enthalten, welche an den eigentlichen Berichtsexporter weitergereicht werden.
   * </p>
   * <p>
   * Minimal muss das Unterargument 'class' definiert sein, welches eine konkrete Klasse beschreibt, welche
   * {@link org.kalypso.ui.calcwizard.bericht.IBerichtExporter}implementiert.
   */
  private static final String PROP_EXPORTER = "exporter";

  /** Argument: Position des Haupt-Sash: Integer von 0 bis 100 */
  private final static String PROP_MAINSASH = "mainSash";

  /** Argument: Position des rechten Sash: Integer von 0 bis 100 */
  private final static String PROP_RIGHTSASH = "rightSash";

  /** Argument: Pfad auf die Vorlage für das Diagramm (.odt Datei) */
  private final static String PROP_DIAGTEMPLATE = "diagTemplate";

  /** Argument: */
  private static final String PROP_RESULT_TS_NAME = "resultProperty";

  /** Argument: */
  private static final String PROP_PROGNOSE_TS_NAME = "prognoseProperty";

  /** Argument: */
  private static final String PROP_PEGEL_NAME = "pegelNameProperty";

  protected Object[] m_checkedElements;

  protected ObsdiagviewType m_obsdiagviewType;

  private IBerichtExporter[] m_berichtExporters;

  public ExportResultsWizardPage()
  {
    super( "<ViewResultsWizardPage>", SELECT_FROM_MAPVIEW );
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
   */
  public void dispose()
  {
    super.dispose();
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl( final Composite parent )
  {
    try
    {
      final SashForm sashForm = new SashForm( parent, SWT.HORIZONTAL );
      createMapPanel( sashForm );

      final SashForm rightSash = new SashForm( sashForm, SWT.VERTICAL );

      final Composite rightTopPanel = new Composite( rightSash, SWT.NONE );
      rightTopPanel.setLayout( new GridLayout( 2, false ) );
      createDataButtonsPanel( rightTopPanel ).setLayoutData( new GridData( GridData.FILL_VERTICAL ) );
      createCalccasesGroup( rightTopPanel ).setLayoutData( new GridData( GridData.FILL_BOTH ) );

      final Composite rightBottomPanel = new Composite( rightSash, SWT.NONE );
      rightBottomPanel.setLayout( new GridLayout() );

      initDiagram( rightBottomPanel ).setLayoutData( new GridData( GridData.FILL_BOTH ) );
      initButtons( rightBottomPanel, "Zeitreihe(n) bearbeiten",
          "Öffnet die im Diagram dargestellten Zeitreihen zur Bearbeitung", new SelectionAdapter()
          {
            public void widgetSelected( final SelectionEvent e )
            {
              startGrafik();
            }
          } );

      final int mainWeight = Integer.parseInt( getArguments().getProperty( PROP_MAINSASH, "50" ) );
      final int rightWeight = Integer.parseInt( getArguments().getProperty( PROP_RIGHTSASH, "50" ) );

      sashForm.setWeights( new int[]
      {
          mainWeight,
          100 - mainWeight } );

      rightSash.setWeights( new int[]
      {
          rightWeight,
          100 - rightWeight } );

      rightSash.addControlListener( getControlAdapter() );
      sashForm.addControlListener( getControlAdapter() );

      setControl( sashForm );

      postCreateControl();

      // Load Template for Grafix.exe
      final String diagFileName = getArguments().getProperty( PROP_DIAGTEMPLATE );
      if( diagFileName != null )
      {
        final IFile diagFile = (IFile)getProject().findMember( diagFileName );
        try
        {
          m_obsdiagviewType = DiagViewUtils.loadDiagramTemplateXML( diagFile.getContents() );
        }
        catch( final Exception e )
        {
          e.printStackTrace();
        }
      }
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
  }

  private Control createCalccasesGroup( final Composite parent )
  {
    final Group treeGroup = new Group( parent, SWT.NONE );
    treeGroup.setText( "Rechenvarianten im Diagram" );
    treeGroup.setLayout( new GridLayout() );

    final CalcCaseTableTreeViewer calcCaseViewer = new CalcCaseTableTreeViewer( getCalcFolder(), treeGroup, SWT.BORDER
        | SWT.SINGLE | SWT.CHECK | SWT.HIDE_SELECTION | SWT.FULL_SELECTION );
    calcCaseViewer.getControl().setLayoutData( new GridData( GridData.FILL_BOTH ) );
    calcCaseViewer.setInput( getProject().getFolder( ModelNature.PROGNOSE_FOLDER ) );
    calcCaseViewer.setSelection( new StructuredSelection( getCalcFolder() ), true );
    calcCaseViewer.addCheckStateListener( new ICheckStateListener()
    {
      public void checkStateChanged( final CheckStateChangedEvent event )
      {
        m_checkedElements = calcCaseViewer.getCheckedElements();

        refreshDiagram();
      }
    } );
    calcCaseViewer.setChecked( getCalcFolder(), true );
    // the last line will not notify the listeners, so do it myself
    m_checkedElements = new Object[]
    { getCalcFolder() };

    return treeGroup;
  }

  private Control createDataButtonsPanel( final Composite parent )
  {
    m_berichtExporters = createExporters();

    final Group buttonGroup = new Group( parent, SWT.NONE );
    buttonGroup.setText( "Datenablagen" );
    buttonGroup.setLayout( new GridLayout() );

    final Button exportPrognosesButton = new Button( buttonGroup, SWT.PUSH );
    exportPrognosesButton.setText( "Export Prognosen" );
    exportPrognosesButton.setToolTipText( "Exportiert die Prognosen der aktuellen Rechenvariante" );
    exportPrognosesButton.setLayoutData( new GridData() );
    exportPrognosesButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      public void widgetSelected( SelectionEvent e )
      {
        exportPrognoseTimeseries();
      }
    } );

    final Button berichtsablageButton = new Button( buttonGroup, SWT.PUSH );
    berichtsablageButton.setLayoutData( new GridData() );
    berichtsablageButton.setText( "Bericht(e) ablegen" );
    berichtsablageButton.setToolTipText( "Legt für die aktuelle Rechenvariante Dokumente im Berichtswesen ab." );
    berichtsablageButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      public void widgetSelected( SelectionEvent e )
      {
        exportSelectedDocuments();
      }
    } );

    berichtsablageButton.setEnabled( !( m_berichtExporters == null || m_berichtExporters.length == 0 ) );

    return buttonGroup;
  }

  /**
   * Exports for all selected document types
   */
  protected void exportSelectedDocuments()
  {
    final Shell shell = getContainer().getShell();

    FeatureList features = getFeatures();
    FeatureList selectedFeatures = getSelectedFeatures();

    final String resultProperty = getArguments().getProperty( PROP_RESULT_TS_NAME );
    final URL context;
    try
    {
      context = ResourceUtilities.createURL( getCalcFolder() );
      features = FeatureFactory.createFeatureList( null, null, filterForValidTimeseriesLinks( features, resultProperty,
          context ) );
      selectedFeatures = filterForValidTimeseriesLinks( selectedFeatures, resultProperty, context );
    }
    catch( MalformedURLException e )
    {
      e.printStackTrace();
    }

    final String nameProperty = getArguments().getProperty( PROP_PEGEL_NAME );

    MultiDocumentServiceWrapper metadocService = null;
    try
    {
      metadocService = new MultiDocumentServiceWrapper();

      final ExportWizardBerichtWizard wizard = new ExportWizardBerichtWizard( features, selectedFeatures, nameProperty,
          metadocService.getDummyExportableDoc(), metadocService.getDummyDocument(), m_berichtExporters );
      final WizardDialog dialog = new WizardDialog( getContainer().getShell(), wizard );
      if( dialog.open() == Window.OK )
      {
        final Feature[] choosenFeatures = wizard.getChoosenFeatures();
        final IBerichtExporter[] choosenExporter = wizard.getChoosenExporter();

        final MultiDocumentServiceWrapper msw = metadocService;
        final ICoreRunnableWithProgress op = new ICoreRunnableWithProgress()
        {
          public IStatus execute( final IProgressMonitor monitor ) throws CoreException, InterruptedException
          {
            exportDocuments( monitor, msw, choosenFeatures, choosenExporter );
            return Status.OK_STATUS;
          }
        };
        final IStatus status = RunnableContextHelper.execute( getContainer(), false, true, op );
        if( !status.isOK() )
          ErrorDialog.openError( shell, "Berichtsablage", "Fehler bei der Berichtsablage.", status );
      }
    }
    catch( final CoreException e )
    {
      e.printStackTrace();

      ErrorDialog.openError( shell, "Berichtsablage", "Berichtsablagedienst konnte nicht initialisiert werden.", e
          .getStatus() );
      return;
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
    finally
    {
      metadocService.dispose();
    }
  }

  protected void exportDocuments( final IProgressMonitor monitor, final MultiDocumentServiceWrapper metadocService,
      final Feature[] features, final IBerichtExporter[] exporter ) throws CoreException, InterruptedException
  {
    monitor.beginTask( "Berichtsablage", exporter.length * features.length );

    final Collection stati = new LinkedList();
    for( int i = 0; i < exporter.length; i++ )
    {
      final IBerichtExporter berichtExporter = exporter[i];
      final String extension = berichtExporter.getExtension();
      for( int j = 0; j < features.length; j++ )
      {
        monitor.setTaskName( "Berichtsablage - " + berichtExporter.toString() );

        final Document doc = metadocService.getCopyDocument( extension );
        OutputStream os = null;
        try
        {
          os = new FileOutputStream( doc.getFile() );
          final IStatus status = berichtExporter.export( features[j], os );
          if( !status.isOK() )
            stati.add( status );

          metadocService.commitDocument( doc );
        }
        catch( final CoreException ce )
        {
          stati.add( ce.getStatus() );
        }
        catch( final Exception e )
        {
          e.printStackTrace();

          doc.dispose();

          final String msg = e.getLocalizedMessage();
          final String message = msg == null ? "Unbekannter Fehler" : msg;

          stati.add( KalypsoGisPlugin.createErrorStatus( message, e ) );
        }
        finally
        {
          IOUtils.closeQuietly( os );
        }

        monitor.worked( 1 );
        if( monitor.isCanceled() )
          throw new InterruptedException();
      }
    }

    if( stati.isEmpty() )
      return;

    throw new CoreException( new org.eclipse.core.runtime.MultiStatus( KalypsoGisPlugin.getId(), 0, (IStatus[])stati
        .toArray( new IStatus[stati.size()] ), "Beim Export ein oder mehrerer Dokumente traten Fehler auf.", null ) );
  }

  private IBerichtExporter[] createExporters()
  {
    final Collection exporters = new ArrayList();

    final Arguments arguments = getArguments();
    for( final Iterator aIt = arguments.entrySet().iterator(); aIt.hasNext(); )
    {
      final Map.Entry entry = (Entry)aIt.next();
      final String key = (String)entry.getKey();
      if( key.startsWith( PROP_EXPORTER ) )
      {
        try
        {
          // todo: maybe check if argument is ok?
          final Arguments args = (Arguments)entry.getValue();
          final String classname = args.getProperty( "class" );
          if( classname != null )
          {
            final IBerichtExporter exporter = (IBerichtExporter)ClassUtilities.newInstance( classname,
                IBerichtExporter.class, this.getClass().getClassLoader() );
            exporter.init( getContext(), args );
            exporters.add( exporter );
          }
        }
        catch( final ClassUtilityException e )
        {
          e.printStackTrace();
        }
      }
    }

    return (IBerichtExporter[])exporters.toArray( new IBerichtExporter[exporters.size()] );
  }

  private FeatureList chooseSelectedFeatures( final IFolder calcCase )
  {
    // Timeserie-Links holen
    try
    {
      final String resultProperty = getArguments().getProperty( PROP_RESULT_TS_NAME );
      final URL context = ResourceUtilities.createURL( calcCase );
      final FeatureList features = filterForValidTimeseriesLinks( getFeatures(), resultProperty, context );
      final FeatureList selectedFeatures = filterForValidTimeseriesLinks( getSelectedFeatures(), resultProperty,
          context );
      // view it!
      final String nameProperty = getArguments().getProperty( PROP_PEGEL_NAME );
      final FeatureType featureType = ( (IKalypsoFeatureTheme)getMapModell().getActiveTheme() ).getFeatureType();
      final FeatureTypeProperty ftp = featureType.getProperty( nameProperty );
      if( ftp == null )
      {
        System.out.println( "No FeatureType for Property: " + nameProperty );
        return null;
      }

      final ILabelProvider labelProvider = new FeatureLabelProvider( new StringModifier( ftp ) );
      final ListSelectionDialog dialog = new ListSelectionDialog( getContainer().getShell(), features,
          new ArrayContentProvider(), labelProvider, "Die Daten folgender Pegel werden exportiert:" );
      dialog.setInitialElementSelections( selectedFeatures );
      dialog.setTitle( "Export Pegel: Rechenvariante " + calcCase.getName() );
      if( dialog.open() != Window.OK )
        return null;

      final Object[] result = dialog.getResult();
      final Feature[] resultFeatures = (Feature[])org.kalypso.contribs.java.util.Arrays.castArray( result, new Feature[result.length] );
      return FeatureFactory.createFeatureList( features.getParentFeature(), features.getParentFeatureTypeProperty(), resultFeatures );
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * test for each feature of the given list, if the timeserieslink in the given property is valid
   * 
   * @param featureList
   *          list of features
   * @param propertyNameTimeserieslink
   * @param context
   * @return List of features that have valid timeserieslinks
   */
  private FeatureList filterForValidTimeseriesLinks( final FeatureList featureList,
      final String propertyNameTimeserieslink, final URL context )
  {
    final FeatureList result = FeatureFactory.createFeatureList( featureList.getParentFeature(), featureList
        .getParentFeatureTypeProperty() );

    // TODO filter auch im berichtsexport benutzen

    final UrlResolver resolver = new UrlResolver();
    URL resultURL;
    for( Iterator iter = featureList.iterator(); iter.hasNext(); )
    {
      Feature fe = (Feature)iter.next();
      TimeseriesLink resultLink = (TimeseriesLink)fe.getProperty( propertyNameTimeserieslink );
      if( resultLink == null )
        continue;

      try
      {
        resultURL = resolver.resolveURL( context, ZmlURL.getIdentifierPart( resultLink.getHref() ) );
        // let's see if it throws an exception
        resultURL.openStream();
        // no exception means, result is existing
        result.add( fe );
      }
      catch( Exception e )
      {
        //   nothing, as exception is expected if result is not there
      }
    }
    return result;
  }

  /**
   * Allows user to export selected timeseries into repository. Handles UI selection and delegates call to
   * performPrognoseExport.
   */
  protected void exportPrognoseTimeseries()
  {
    final IFolder calcFolder = getCalcFolder();
    final FeatureList featureList = chooseSelectedFeatures( calcFolder );
    if( featureList == null )
      return;

    final String resultTsName = getArguments().getProperty( PROP_RESULT_TS_NAME );
    final String prognoseTsName = getArguments().getProperty( PROP_PROGNOSE_TS_NAME );

    final ICoreRunnableWithProgress op = new ICoreRunnableWithProgress()
    {
      public IStatus execute( final IProgressMonitor monitor ) throws CoreException, InvocationTargetException,
          InterruptedException
      {
        try
        {
          // TODO: put it into an ant task
          final IObservationService srv = KalypsoGisPlugin.getDefault().getObservationServiceProxy();
          final URL context = ResourceUtilities.createURL( calcFolder );
          final CommitPrognoseFeatureVisitor visitor = new CommitPrognoseFeatureVisitor( srv, new UrlResolver(),
              context, resultTsName, prognoseTsName, monitor );

          monitor.beginTask( "Prognosen werden veröffentlicht", featureList.size() );
          featureList.accept( visitor );

          return new org.eclipse.core.runtime.MultiStatus( KalypsoSimulationUIPlugin.getID(), 0, visitor.getStati(),
              "Fehler sind während des Exports aufgetaucht.", null );
        }
        catch( final ServiceException e )
        {
          throw new InvocationTargetException( e, "Zeitreihendienst" );
        }
        catch( final MalformedURLException e )
        {
          e.printStackTrace();
          throw new InvocationTargetException( e );
        }
      }
    };

    final IStatus status = RunnableContextHelper.execute( getContainer(), false, true, op );
    final Shell shell = getContainer().getShell();
    ErrorDialog.openError( shell, "Export Prognose Zeitreihen", "Fehler beim Export der Zeitreihen", status );
  }

//  /**
//   * Performs the timeseries export
//   */
//  private IStatus performPrognoseExport( final TSLinkWithName[] resultTss, final TSLinkWithName[] prognoseTss,
//      final IFolder calcCase, final IProgressMonitor monitor ) throws ServiceException
//  {
//    if( resultTss.length != prognoseTss.length )
//      throw new IllegalArgumentException( "Timeseries links not same length" );
//
//    final Logger LOG = Logger.getLogger( getClass().getName() );
//
//    monitor.beginTask( "Prognosen zurück speichern", prognoseTss.length );
//
//    final MultiStatus multiStatus = new MultiStatus( IStatus.WARNING, KalypsoGisPlugin.getId(), 0,
//        "Fehler sind während des Exports aufgetaucht." );
//
//    try
//    {
//      final URL context = ResourceUtilities.createURL( calcCase );
//
//      final UrlResolver resolver = new UrlResolver();
//
//      final IObservationService srv = KalypsoGisPlugin.getDefault().getObservationServiceProxy();
//
//      for( int i = 0; i < prognoseTss.length; i++ )
//      {
//        if( monitor.isCanceled() )
//          return Status.CANCEL_STATUS;
//
//        final TSLinkWithName lnkRS = resultTss[i];
//        final TSLinkWithName lnkPG = prognoseTss[i];
//
//        try
//        {
//          final URL urlRS = resolver.resolveURL( context, lnkRS.href );
//          final IObservation source = ZmlFactory.parseXML( urlRS, lnkRS.href );
//
//          final String destRef = ZmlURL.insertRequest( lnkPG.href, new ObservationRequest( new Date(), new Date() ) );
//          final URL urlPG = resolver.resolveURL( context, destRef );
//          final IObservation dest = ZmlFactory.parseXML( urlPG, destRef );
//
//          ITuppleModel values = null;
//          try
//          {
//            // copy values from source into dest, expecting full compatibility
//            values = ObservationUtilities.optimisticValuesCopy( source, dest, null, true );
//          }
//          catch( IllegalArgumentException e )
//          {
//            // not all axes could be associated between source and dest
//            LOG.warning( "Observations are not compatible: " + e + "\n" + "Check if all axes of " + dest
//                + " are defined in " + source );
//
//            multiStatus.addMessage( "Zeitreihe konnte nicht auf dem Server hochgeladen werden "
//                + "weil sie nicht mit der Zielzeitreihe kompatibel ist. " + "Prüfen Sie u.a. die Achsenliste in: "
//                + source, e );
//          }
//
//          // todo: maybe inform when nothing happened during copy
//          if( values == null )
//            LOG.info( "Nothing to copy for " + source.getName() );
//          else
//          {
//            // save observation if it is a server side one
//            if( ZmlURL.isServerSide( lnkPG.href ) )
//            {
//              ServiceRepositoryObservation.setValuesFor( values, lnkPG.href, srv );
//              LOG.info( "Observation saved on server: " + lnkPG.href );
//            }
//            else
//              LOG.warning( "! Observation not server side: " + lnkPG.href );
//          }
//        }
//        catch( final SensorException e )
//        {
//          LOG.warning( e.getLocalizedMessage() );
//
//          multiStatus.addMessage( "Fehler mit folgenden Zeitreihen: " + lnkRS.href + " und " + lnkPG.href, e );
//        }
//
//        monitor.worked( 1 );
//      }
//
//      return multiStatus;
//    }
//    catch( MalformedURLException e )
//    {
//      LOG.warning( e.getLocalizedMessage() );
//
//      return KalypsoGisPlugin.createErrorStatus( "Fehler in der Konfiguration", e );
//    }
//    finally
//    {
//      monitor.done();
//    }
//  }

  private void createMapPanel( final Composite parent ) throws Exception, CoreException
  {
    final Composite mapPanel = new Composite( parent, SWT.NONE );
    mapPanel.setLayout( new GridLayout() );

    final Control mapControl = initMap( mapPanel, MapPanel.WIDGET_SINGLE_SELECT );
    mapControl.setLayoutData( new GridData( GridData.FILL_BOTH ) );
  }

  protected void startGrafik()
  {
    final IFolder calcFolder = getCalcFolder();
    final IFolder grafikFolder = calcFolder.getFolder( "grafik" );

    final ObsdiagviewType xml;
    try
    {
      xml = DiagViewUtils.buildDiagramTemplateXML( m_diagView );
    }
    catch( JAXBException e2 )
    {
      e2.printStackTrace();
      return;
    }

    final ICoreRunnableWithProgress op = new ICoreRunnableWithProgress()
    {
      public IStatus execute( final IProgressMonitor monitor ) throws InvocationTargetException
      {
        try
        {
          monitor.beginTask( "Grafik öffnen", IProgressMonitor.UNKNOWN );

          if( !monitor.isCanceled() )
            GrafikLauncher.startGrafikODT( "grafik", xml, grafikFolder, monitor );

          return Status.OK_STATUS;
        }
        catch( final SensorException se )
        {
          se.printStackTrace();
          throw new InvocationTargetException( se );
        }
        finally
        {
          monitor.done();
        }
      }
    };

    final IStatus status = RunnableContextHelper.execute( getContainer(), true, false, op );
    if( !status.isOK() )
      ErrorDialog.openError( getShell(), "Hochwasser Vorhersage", "Grafik öffnen", status );
  }

  /**
   * Überschrieben, da wir das gleiche für mehrere contexte = mehrere Rechenfälle ausführen
   */
  public void refreshDiagram()
  {
    // TODO: sollte nicht diese methode überschreiben sondern besser die methode
    // getObservationsToShow!

    // erstmal leer, damit das Diagramm gelöscht wird
    refreshDiagramForContext( new TSLinkWithName[] {}, getContext() );

    if( m_checkedElements == null || m_checkedElements.length == 0 )
      return;

    // Ist ein Hack, der davon ausgeht, dass das modell sich nie
    // ändern wird
    // es werden einfach die links vom aktuellen Modell gegen alle
    // selektierten Rechenfälle aufgelöst
    final TSLinkWithName[] obs = getObservationsToShow( true );

    for( int i = 0; i < m_checkedElements.length; i++ )
    {
      try
      {
        final IFolder calcCase = (IFolder)m_checkedElements[i];
        final URL context = ResourceUtilities.createURL( calcCase );
        refreshDiagramForContext( obs, context );
      }
      catch( final MalformedURLException e )
      {
        e.printStackTrace();
      }
    }
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.modelpages.AbstractCalcWizardPage#getObservationsToShow(boolean)
   */
  protected TSLinkWithName[] getObservationsToShow( final boolean onlySelected )
  {
    return getObservations( onlySelected );
  }
}