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

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
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
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.kalypso.auth.KalypsoAuthPlugin;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.commons.java.net.UrlResolver;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.java.lang.ISupplier;
import org.kalypso.metadoc.IExportTarget;
import org.kalypso.metadoc.IExporter;
import org.kalypso.metadoc.impl.MetadocExtensions;
import org.kalypso.metadoc.impl.MultiExporter;
import org.kalypso.metadoc.impl.MultiExporter.ISupplierCreator;
import org.kalypso.metadoc.ui.ExportDocumentsWizard;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.featureview.modfier.StringModifier;
import org.kalypso.ogc.gml.util.FeatureLabelProvider;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.diagview.DiagViewUtils;
import org.kalypso.ogc.sensor.diagview.grafik.GrafikLauncher;
import org.kalypso.ogc.sensor.zml.ZmlURL;
import org.kalypso.simulation.ui.KalypsoSimulationUIPlugin;
import org.kalypso.simulation.ui.calccase.ModelNature;
import org.kalypso.simulation.ui.calccase.ModelSynchronizer;
import org.kalypso.simulation.ui.calccase.jface.CalcCaseTableTreeViewer;
import org.kalypso.simulation.ui.wizards.calculation.CalcWizard;
import org.kalypso.simulation.ui.wizards.calculation.TSLinkWithName;
import org.kalypso.template.obsdiagview.ObsdiagviewType;
import org.kalypso.zml.obslink.TimeseriesLink;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.FeatureType;
import org.kalypsodeegree.model.feature.FeatureTypeProperty;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.feature.FeaturePath;

/**
 * Mostly used as the last page of the calculation wizard. The user can export the features in different ways:
 * <ul>
 * <li>back to the repository
 * <li>as document within the metadoc infrastructure
 * </ul>
 * 
 * @author belger,schlienger
 */
public class ExportResultsWizardPage extends AbstractCalcWizardPage
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

  private static final String STR_EXPORT_PROGNOSEN = "Vorhersagen exportieren";

  /**
   * Argument: One or more exporters used for metadoc document exports. Many 'exporter' elements can be specified in the
   * arguments, they must be followed by some arbitrary string.
   * <p>
   * For the sustained arguments, @see MultiExporter
   * </p>
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
  private static final String PROP_PEGEL_NAME = "pegelNameProperty";

  protected Object[] m_checkedElements;

  protected ObsdiagviewType m_obsdiagviewType;

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

      selectFeaturesInMap( getFeaturesToSelect() );

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
    treeGroup.setText( "Rechenvarianten im Diagramm" );
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
    final Group buttonGroup = new Group( parent, SWT.NONE );
    buttonGroup.setText( "Datenablagen" );
    buttonGroup.setLayout( new GridLayout() );

    final Button exportPrognosesButton = new Button( buttonGroup, SWT.PUSH );
    exportPrognosesButton.setText( STR_EXPORT_PROGNOSEN );
    exportPrognosesButton.setToolTipText( "Exportiert die Vorhersagen der aktuellen Rechenvariante" );
    exportPrognosesButton.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
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
    berichtsablageButton.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
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

    // show button only if there is any configured exporter
    final IExporter[] exporters = createExporters();
    berichtsablageButton.setEnabled( !( exporters == null || exporters.length == 0 ) );

    // Rechenvariante archivieren
    final Button archiveButton = new Button( buttonGroup, SWT.PUSH );
    archiveButton.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
    archiveButton.setText( "Variante archivieren" );
    archiveButton.setToolTipText( "Archiviert die aktuelle Rechenvariante auf dem Server." );
    archiveButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      public void widgetSelected( SelectionEvent e )
      {
        archiveCalcCase();
      }
    } );

    return buttonGroup;
  }

  protected void archiveCalcCase()
  {
    final CalcWizard wizard = (CalcWizard)getWizard();
    final ModelSynchronizer synchronizer = wizard.getModelSynchronizer();
    final IFolder calcFolder = getCalcFolder();

    final WorkspaceModifyOperation op = new WorkspaceModifyOperation()
    {
      protected void execute( final IProgressMonitor monitor ) throws CoreException
      {
        monitor.beginTask( "Rechenvariante wird archiviert", 1000 );

        try
        {
          synchronizer.commitFolder( calcFolder, monitor );
        }
        finally
        {
          monitor.done();
        }
      }
    };

    final IStatus status = RunnableContextHelper.execute( getContainer(), false, true, op );
    ErrorDialog.openError( getContainer().getShell(), "Rechenvariante archivieren",
        "Fehler beim Archivieren der Rechenvariante", status );
  }

  /**
   * Exports for all selected document types
   */
  protected void exportSelectedDocuments()
  {
    final Shell shell = getContainer().getShell();

    try
    {
      // retrieve the potential targets
      final IExportTarget target = MetadocExtensions.retrieveTarget( "metadocServiceTarget" );

      // init target with common properties
      final IFolder currentCalcCase = getCalcFolder();
      final ModelNature nature = (ModelNature)currentCalcCase.getProject().getNature( ModelNature.ID );
      final String scenarioId = nature.getScenario( currentCalcCase ).getId();
      target.setProperty( "currentScenarioId", scenarioId );
      target.setProperty( "calcCaseName", currentCalcCase.getName() );
      target.setProperty( "calcCaseDescription", (String)nature.loadCalculationAndReadProperty( currentCalcCase, "description" ) );
      target.setProperty( "projectName", currentCalcCase.getProject().getName() );
      target.setProperty( "autor", KalypsoAuthPlugin.getDefault().getCurrentUser().getUserName() );
      
      // create and initialise the exporters
      final IExporter[] exporters = createExporters();

      final ExportDocumentsWizard wizard = new ExportDocumentsWizard( shell, exporters, target );
      final WizardDialog dialog = new WizardDialog( shell, wizard );

      // just open the dialog, business is done in the dialog
      dialog.open();
    }
    catch( final CoreException e )
    {
      e.printStackTrace();

      ErrorDialog.openError( shell, "Berichtsablage", "Berichtsablagedienst konnte nicht initialisiert werden.", e
          .getStatus() );
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      ErrorDialog.openError( shell, "Berichtsabalge", "Fehler sind aufgetreten", StatusUtilities.createStatus(
          IStatus.ERROR, "Siehe Details", e ) );
    }
  }

  /**
   * Creates the exporters using the arguments. Each argument which begins with "exporter" (see PROP_EXPORTER) should
   * denote an exporter. The exporter is defined by its extension id (the eclipse extension mechanism is used here).
   * <p>
   * Each exporter is also initialised (the method init() is called) with this class as adapter for delivering context
   * dependent information such as the list of selected features.
   * <p>
   * Additionally to the required "id" argument, you can provide an optional "name" argument which, if present, replaces
   * the value of the name property defined in the extension.
   * 
   * @return newly created list of exporters
   */
  private IExporter[] createExporters()
  {
    final Collection stati = new ArrayList();

    final Arguments arguments = getArguments();
    final ISupplierCreator creator = new ISupplierCreator()
    {
      public ISupplier createSupplier( final Arguments args )
      {
        return new ExporterSupplier( args );
      }
    };
    
    final Collection exporters = MultiExporter.createExporterFromArguments( stati, arguments, PROP_EXPORTER, creator );

    if( stati.size() > 0 )
    {
      final MultiStatus status = new MultiStatus( KalypsoSimulationUIPlugin.getID(), 0, (IStatus[])stati
          .toArray( new IStatus[stati.size()] ), "", null );
      ErrorDialog.openError( getContainer().getShell(), "", "", status );
    }

    return (IExporter[])exporters.toArray( new IExporter[exporters.size()] );
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
      final Feature[] resultFeatures = (Feature[])org.kalypso.contribs.java.util.Arrays.castArray( result,
          new Feature[result.length] );
      return FeatureFactory.createFeatureList( features.getParentFeature(), features.getParentFeatureTypeProperty(),
          resultFeatures );
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
   * @return List of features that have valid timeserieslinks
   */
  protected FeatureList filterForValidTimeseriesLinks( final FeatureList featureList,
      final String propertyNameTimeserieslink, final URL context )
  {
    final FeatureList result = FeatureFactory.createFeatureList( featureList.getParentFeature(), featureList
        .getParentFeatureTypeProperty() );

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
      catch( final Exception ignored )
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

    // put feature-ids in feature-path'es
    final StringBuffer sb = new StringBuffer();
    for( final Iterator iter = featureList.iterator(); iter.hasNext(); )
    {
      final Feature f = (Feature)iter.next();
      final String featurePath = new FeaturePath( f ).toString();
      sb.append( featurePath );
      if( iter.hasNext() )
        sb.append( ';' );
    }
    final Properties antProps = new Properties();
    antProps.setProperty( "exportPrognosenFeaturePath", sb.toString() );

    // call ant - with properties
    launchAnt( "exportVorhersagen", "Vorhersage-Zeitreihen werden auf den Server gespeichert", STR_EXPORT_PROGNOSEN,
        "Fehler bemi Speichern der Vorhersage-Zeotreihen", antProps );
  }

  private void createMapPanel( final Composite parent ) throws Exception, CoreException
  {
    final Composite mapPanel = new Composite( parent, SWT.NONE );
    mapPanel.setLayout( new GridLayout() );

    final Control mapControl = initMap( mapPanel );
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
    m_diagView.removeAllItems();

    if( m_checkedElements == null || m_checkedElements.length == 0 )
      return;

    // Ist ein Hack, der davon ausgeht, dass das modell sich nie
    // ändern wird es werden einfach die links vom aktuellen Modell gegen alle
    // selektierten Rechenfälle aufgelöst
    final TSLinkWithName[] obs = getObservations( true );

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
   * Handy supplier for being used with exporters
   * 
   * @author schlienger
   */
  private class ExporterSupplier implements ISupplier
  {
    private final Arguments m_args;

    public ExporterSupplier( final Arguments args )
    {
      m_args = args;
    }

    /**
     * @see org.kalypso.contribs.java.lang.ISupplier#supply(java.lang.Object)
     */
    public Object supply( final Object request )
    {
      if( "features".equals( request ) )
      {
        final FeatureList features = getFeatures();
        final String property = getArguments().getProperty( PROP_RESULT_TS_NAME );
        final URL context = getContext();

        return filterForValidTimeseriesLinks( features, property, context );
      }

      if( "selectedFeatures".equals( request ) )
        return getSelectedFeatures();

      if( "propertyName".equals( request ) )
        return getArguments().getProperty( PROP_PEGEL_NAME );

      if( "arguments".equals( request ) )
        return m_args;

      if( "context".equals( request ) )
        return getContext();

      // Tricky clone-hack to produce new supplier on a subset of the arguments.
      // This is used by the MultiExporter.
      if( request instanceof Arguments )
        return new ExporterSupplier( (Arguments)request );
      
      Logger.getLogger( getClass().getName() ).warning(
          "Supplier asked for " + request.toString() + " but request could not be satisfied" );

      return null;
    }
  }
}