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

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.kalypso.commons.java.util.PropertiesHelper;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.FeatureSelectionHelper;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypso.simulation.ui.KalypsoSimulationUIPlugin;
import org.kalypso.ui.editor.featureeditor.FeatureTemplateviewer;
import org.kalypso.util.command.JobExclusiveCommandTarget;
import org.kalypso.util.swt.SWTUtilities;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;

/**
 * @author Belger
 */
public class MapAndFeatureWizardPage extends AbstractCalcWizardPage
{
  /** Argument: Position des Haupt-Sash: Integer von 0 bis 100 */
  private final static String PROP_MAINSASH = "mainSash";

  /** Argument: Position des rechten Sash: Integer von 0 bis 100 */
  private final static String PROP_RIGHTSASH = "rightSash";

  /** Argument: Pfad auf Vorlage für die Feature-View (.gft Datei) */
  private final static String PROP_FEATURETEMPLATE = "featureTemplate";

  /** Argument: SWT-Style für die Composite des Features. Default ist {@link SWT#BORDER} */
  private static final String PROP_FEATURE_VIEW_STYLE = "featureControlStyle";

  /**
   * Argument: falls true, wird der festeingebaute Berechnungsknopf nicht gezeigt. Nur aus Gründen der
   * Rückwärtskompabilität. Eigentlich sollten alle Modell (z.B. WeisseElster) lieber Ant-Knöpfe benutzen.
   */
  private static final String PROP_HIDE_CALC_BUTTON = "hideCalcButton";

  /**
   * Basisname der Zeitreihen-Properties. Es kann mehrere Zeitreihen geben-Property geben: eine für jede Kurventyp.
   */
  public final static String PROP_TIMEPROPNAME = "timeserie";

  protected final FeatureTemplateviewer m_templateviewer = new FeatureTemplateviewer( new JobExclusiveCommandTarget(
      null, null ), 0, 0 );

  public MapAndFeatureWizardPage()
  {
    super( "<MapAndFeatureWizardPage>", SELECT_FROM_FEATUREVIEW );
  }

  public void dispose()
  {
    if( m_templateviewer != null )
      m_templateviewer.dispose();
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
      createRightPanel( sashForm );

      setControl( sashForm );

      showFeatureInFeatureView();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
  }

  private void createRightPanel( final SashForm sashForm ) throws NumberFormatException
  {
    final Composite rightPanel = new Composite( sashForm, SWT.NONE );
    final GridLayout gridLayout = new GridLayout();
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;

    rightPanel.setLayout( gridLayout );
    rightPanel.setLayoutData( new GridData( GridData.FILL_BOTH ) );

    final SashForm rightSash = new SashForm( rightPanel, SWT.VERTICAL );
    rightSash.setLayoutData( new GridData( GridData.FILL_BOTH ) );
    createFeaturePanel( rightSash );
    initDiagram( rightSash );

    final boolean hideCalcButton = Boolean.valueOf( getArguments().getProperty( PROP_HIDE_CALC_BUTTON, "false" ) )
        .booleanValue();
    if( hideCalcButton )
      initButtons( rightPanel, null, null, null );
    else
      initButtons( rightPanel, "Berechnung durchführen", null, new SelectionAdapter()
      {
        public void widgetSelected( final SelectionEvent e )
        {
          runCalculation();
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

    // die Karte soll immer maximiert sein
    rightSash.addControlListener( new ControlAdapter()
    {
      public void controlResized( ControlEvent e )
      {
        maximizeMap();
      }
    } );
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.modelpages.AbstractCalcWizardPage#saveState(org.eclipse.core.runtime.IProgressMonitor)
   */
  public void saveState( final IProgressMonitor monitor ) throws CoreException
  {
    monitor.beginTask( "Daten speichern", 2 );

    super.saveState( new SubProgressMonitor( monitor, 1 ) );

    final IStatus status = m_templateviewer.saveGML( new SubProgressMonitor( monitor, 1 ) );
    if( !status.isOK() )
      throw new CoreException( status );
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.modelpages.AbstractCalcWizardPage#restoreState()
   */
  public void restoreState() throws CoreException
  {
    super.restoreState();

    // zusätzlich das Diagramm aktualisieren, sonst passiert es evtl. nicht
    refreshDiagram();
  }

  private void createFeaturePanel( final Composite parent )
  {
    final String featureTemplateArgument = getArguments().getProperty( PROP_FEATURETEMPLATE, null );
    final String featureViewStyle = getArguments().getProperty( PROP_FEATURE_VIEW_STYLE, "SWT.BORDER" );
    final int viewStyle = SWTUtilities.createStyleFromString( featureViewStyle );

    final Properties featureTemplateProps = PropertiesHelper.parseFromString( featureTemplateArgument, '#' );
    final String templateFileName = featureTemplateArgument.replaceAll( "#.*", "" );
    final ICoreRunnableWithProgress op = new ICoreRunnableWithProgress()
    {
      public IStatus execute( final IProgressMonitor monitor ) throws CoreException
      {
        try
        {
          final IFile templateFile = (IFile)getProject().findMember( templateFileName );
          if( templateFile != null && templateFile.exists() )
          {
            final Reader reader = new InputStreamReader( templateFile.getContents(), templateFile.getCharset() );
            m_templateviewer.loadInput( reader, getContext(), monitor, featureTemplateProps );
            parent.getDisplay().asyncExec( new Runnable()
            {
              public void run()
              {
                m_templateviewer.createControls( parent, viewStyle );
              }
            } );
          }

          return Status.OK_STATUS;
        }
        catch( UnsupportedEncodingException e )
        {
          throw new CoreException( new Status( IStatus.ERROR, KalypsoSimulationUIPlugin.getID(), 0, "", e ) );
        }
      }
    };

    final IStatus status = RunnableContextHelper.execute( getContainer(), true, false, op );
    if( !status.isOK() )
    {
      ErrorDialog.openError( getShell(), "Feature Template laden", "Fehler beim Laden der Vorlage" + templateFileName,
          status );

      m_templateviewer.dispose();

      final Label label = new Label( parent, SWT.NONE );
      label.setText( "Vorlage konnte nicht geladen werden: " + templateFileName );
    }
  }

  private void createMapPanel( final Composite parent ) throws Exception, CoreException
  {
    final Composite mapPanel = new Composite( parent, SWT.NONE );
    mapPanel.setLayout( new GridLayout() );

    final Control mapControl = initMap( mapPanel );
    mapControl.setLayoutData( new GridData( GridData.FILL_BOTH ) );
  }

  protected void showFeatureInFeatureView()
  {
    // if the featureToSelect property is set, we use the map!
    final String[] fid = getFeaturesToSelect();
    {
      final boolean forceSelectionInMap = !m_templateviewer.hasAdditionalDataObject();
      selectFeaturesInMap( fid, true, forceSelectionInMap );
    }

    // else we show the feature defined in the feature-template
    final Thread waitForFeature = new Thread()
    {
      public void run()
      {
        super.run();
        int max = 10;
        while( true )
        {
          if( m_templateviewer.getFeature() != null )
          {
            refreshDiagram();
            return;
          }
          try
          {
            sleep( 500 );
          }
          catch( final InterruptedException e )
          {
            e.printStackTrace();
          }
          if( max-- < 0 ) // but do not wait for ever
          {
            refreshDiagram();
            return;
          }
        }
      }
    };
    waitForFeature.start();
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.modelpages.AbstractCalcWizardPage#getSelectedFeatures()
   */
  protected FeatureList getSelectedFeatures()
  {
    final FeatureList list = FeatureFactory.createFeatureList( null, null );
    final Feature feature = m_templateviewer.getFeature();
    if( feature != null )
      list.add( feature );
    return list;
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.modelpages.AbstractCalcWizardPage#getFeatures()
   */
  protected FeatureList getFeatures()
  {
    final FeatureList result = FeatureFactory.createFeatureList( null, null );
    final Feature feature = m_templateviewer.getFeature();
    if( feature != null )
      result.add( feature );
    return result;
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.modelpages.AbstractCalcWizardPage#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
   */
  public void selectionChanged( final SelectionChangedEvent event )
  {
    super.selectionChanged( event );

    // refresh featureview
    final ISelection selection = event.getSelection();
    if( selection instanceof IFeatureSelection )
    {
      final IFeatureSelection featureSelection = (IFeatureSelection)selection;
      final Feature feature = FeatureSelectionHelper.getFirstFeature( featureSelection );
      if( feature != null )
      {
        final CommandableWorkspace workspace = featureSelection.getWorkspace( feature );

        final Control control = m_templateviewer.getControl();
        if( control != null && !control.isDisposed() )
          control.getDisplay().asyncExec( new Runnable()
          {
            public void run()
            {
              m_templateviewer.setFeature( workspace, feature );
            }
          } );
      }
    }
  }
}