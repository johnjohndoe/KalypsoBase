package org.kalypso.calculation.chain.binding.testing;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.kalypso.calculation.chain.CalculationChainMemberJobSpecification;
import org.kalypso.calculation.chain.CalculationChainRunnable;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;

public class JUnitTest_CalculationChain extends TestCase
{

  public final void testExecute( ) throws UnsupportedEncodingException
  {

    final List<CalculationChainMemberJobSpecification> jobSpecificationList = new ArrayList<CalculationChainMemberJobSpecification>();

    /*
     * NA calculation; uses model-defined (default) model data specification
     */
    final IContainer calcCaseNA = ResourcesPlugin.getWorkspace().getRoot().getProject( "01-Kollau-NA-PlanerClient" ).getFolder( "Rechenvarianten/kz-2002_10_26" );
    final CalculationChainMemberJobSpecification jobSpecification_NA = new CalculationChainMemberJobSpecification( "KalypsoNA", calcCaseNA );
// jobSpecificationList.add(jobSpecification_NA);

    /*
     * NA - WSPM connector; defines custom model data specification
     */
    final IProject calcCaseWSPM = ResourcesPlugin.getWorkspace().getRoot().getProject( "02-Kollau-1D-PlanerClient" );
    final CalculationChainMemberJobSpecification jobSpecification_NA_WSPM = new CalculationChainMemberJobSpecification( "KalypsoModelConnector_NA_WSPM", calcCaseWSPM );
    jobSpecification_NA_WSPM.addInput( "NA_Model", "platform:/resource//01-Kollau-NA-PlanerClient/modell.gml", false );
    jobSpecification_NA_WSPM.addInput( "NA_StatisticalReport", "platform:/resource//01-Kollau-NA-PlanerClient/Rechenvarianten/kz-2002_10_26/Ergebnisse/Aktuell/Reports/nodesMax.zml", false );
    jobSpecification_NA_WSPM.addInput( "NA_RiverCode", "" );
    jobSpecification_NA_WSPM.addInput( "WSPM_RunoffEventID", "RunOffEvent1234888262265697" );
    jobSpecification_NA_WSPM.addInput( "WSPM_Model", "modell.gml", true );
    jobSpecification_NA_WSPM.addOutput( "WSPM_Model", "modell.gml", true );
    jobSpecificationList.add( jobSpecification_NA_WSPM );

    /*
     * WSPM calculation; uses Ant launch and model-defined (default) model data specification; in this case, inputs are
     * used to define Ant parameters
     */
    final CalculationChainMemberJobSpecification jobSpecification_WSPM = new CalculationChainMemberJobSpecification( "WspmTuhhV1.0", calcCaseWSPM );
    jobSpecification_WSPM.setAntLauncher( true );
    jobSpecification_WSPM.addInput( "calc.xpath", "id( 'CalculationWspmTuhhSteadyState123488821975039' )" );
    jobSpecification_WSPM.addInput( "result.path", "Ergebnisse/kollau-ist-HQ_5_neu" );
// jobSpecificationList.add(jobSpecification_WSPM);

    /*
     * WSPM - FM connector; defines custom model data specification
     */
    final IContainer calcCaseFM = ResourcesPlugin.getWorkspace().getRoot().getProject( "FM_FloodDemo" ).getFolder( "Basis" );
    final CalculationChainMemberJobSpecification jobSpecification_WSPM_FM = new CalculationChainMemberJobSpecification( "KalypsoModelConnector_WSPM_FM", calcCaseFM );
    jobSpecification_WSPM_FM.addInput( "WSPM_TinFile", "platform:/resource//02-Kollau-1D-PlanerClient/Ergebnisse/kollau-ist-HQ_5_neu/_aktuell/Daten/WspTin.gml", false );
    jobSpecification_WSPM_FM.addInput( "WSPM_TinReference", "platform:/resource//02-Kollau-1D-PlanerClient/Ergebnisse/kollau-ist-HQ_5_neu/_aktuell/Daten/WspTin.gml" );
    jobSpecification_WSPM_FM.addInput( "FM_Model", "models/flood.gml", true );
    jobSpecification_WSPM_FM.addOutput( "FM_Model", "models/flood.gml", true );
    // jobSpecificationList.add(jobSpecification_WSPM_FM);

    /*
     * FM calculation; uses model-defined (default) model data specification
     */
    // calcCaseFM we already have...
    final CalculationChainMemberJobSpecification jobSpecification_FM = new CalculationChainMemberJobSpecification( "KalypsoFloodSimulation", calcCaseFM );
    // jobSpecificationList.add(jobSpecification_FM);

    /*
     * FM - RM connector; defines custom model data specification
     */
    final IContainer calcCaseRM = ResourcesPlugin.getWorkspace().getRoot().getProject( "RM_C01" ).getFolder( "Basis" );
    final CalculationChainMemberJobSpecification jobSpecification_FM_RM = new CalculationChainMemberJobSpecification( "KalypsoModelConnector_FM_RM", calcCaseRM );
    jobSpecification_FM_RM.addInput( "FM_Model", "platform:/resource//FM_FloodDemo/Basis/models/flood.gml", false );
    jobSpecification_FM_RM.addInput( "FM_EventsFolder", "platform:/resource//FM_FloodDemo/Basis/events", false );
    jobSpecification_FM_RM.addInput( "RM_Model", "models/RasterDataModel.gml", true );
    jobSpecification_FM_RM.addOutput( "RM_Model", "models/RasterDataModel.gml", true );
    jobSpecification_FM_RM.addOutput( "RM_InputRasterFolder", "models/raster/input", true );
    // jobSpecificationList.add(jobSpecificationC_FM_RM);

    /*
     * RM calculation; uses model-defined (default) model data specification
     */
    // calcCaseRM we already have...
    final CalculationChainMemberJobSpecification jobSpecification_RM = new CalculationChainMemberJobSpecification( "KalypsoRisk_RiskZonesCalculation", calcCaseRM );
    // jobSpecificationList.add(jobSpecification_RM);

    final IWorkbench workbench = PlatformUI.getWorkbench();
    final Shell shell = workbench.getDisplay().getActiveShell();
    final CalculationChainRunnable chainRunnable = new CalculationChainRunnable( jobSpecificationList );
    RunnableContextHelper.execute( new ProgressMonitorDialog( shell ), true, false, chainRunnable );
  }
}
