package org.kalypso.calculation.chain.binding.testing;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.kalypso.calculation.chain.CalculationChainRunnable;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.simulation.core.SimulationJobSpecification;

public class JUnitTest_Connector_lzNA_kzNA_Test extends TestCase
{

  public final void testExecute( ) throws UnsupportedEncodingException
  {

    final List<SimulationJobSpecification> jobSpecificationList = new ArrayList<SimulationJobSpecification>();

    final IContainer calcCaseKZ = ResourcesPlugin.getWorkspace().getRoot().getProject( "DemoModell" ).getFolder( "Rechenvarianten/kurzzeit1" );
    final SimulationJobSpecification jobSpec = new SimulationJobSpecification( "KalypsoModelConnector_LZNA_KZNA", calcCaseKZ.getFullPath(),null );
    jobSpec.addInput( "LZNA_ERGEBNISSE_AKTUEL_ANFANGWERTE", "platform:/resource//DemoModell/Rechenvarianten/langzeitTest1/Ergebnisse/Aktuell/Anfangswerte", false );
//    jobSpec.addInput( "KZNA_CALCULATION", "platform:/resource//DemoModell/Rechenvarianten/kurzzeit1/.calculation", false );
//    jobSpec.addOutput( "KZNA_ANFANGWERTE_LZSIM", "platform:/resource//DemoModell/Rechenvarianten/kurzzeit1/Anfangswerte/lzsim.gml", false );
    jobSpec.addInput( "KZNA_CALCULATION", ".calculation", true );
    jobSpec.addOutput( "KZNA_ANFANGWERTE_LZSIM", "Anfangswerte/lzsim.gml", true );
    jobSpecificationList.add( jobSpec );

    try
    {
      final URI workspaceUri = ResourcesPlugin.getWorkspace().getRoot().getLocationURI();

      final IWorkbench workbench = PlatformUI.getWorkbench();
      final Shell shell = workbench.getDisplay().getActiveShell();
      final CalculationChainRunnable chainRunnable = new CalculationChainRunnable( jobSpecificationList, workspaceUri.toURL() );
      RunnableContextHelper.execute( new ProgressMonitorDialog( shell ), true, false, chainRunnable );
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();
    }
  }
}
