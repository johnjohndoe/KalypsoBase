package org.kalypso.calculation.chain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.kalypso.simulation.core.simspec.Modeldata;
import org.kalypso.simulation.core.simspec.Modeldata.Input;
import org.kalypso.simulation.core.simspec.Modeldata.Output;
import org.kalypso.simulation.core.util.SimulationUtilitites;

public class CalculationChainMemberJobSpecification
{
  private boolean m_useAntLauncher = false;

  private final String m_calculationTypeID;

  private final List<Input> m_inputList = new ArrayList<Input>();

  private final List<Output> m_outputList = new ArrayList<Output>();

  private final IPath m_container;

  public CalculationChainMemberJobSpecification( final String calculationTypeID, final IPath container )
  {
    m_calculationTypeID = calculationTypeID;
    m_container = container;
  }

  public void addInput( final String id, final String path )
  {
    m_inputList.add( SimulationUtilitites.createInput( id, path ) );
  }

  public void addInput( final String id, final String path, final boolean isRelativeToCalcCase )
  {
    final Input input = SimulationUtilitites.createInput( id, path );
    input.setRelativeToCalcCase( isRelativeToCalcCase );
    m_inputList.add( input );
  }

  public void addInput( final String id, final String path, final boolean isRelativeToCalcCase, final boolean isOptional )
  {
    m_inputList.add( SimulationUtilitites.createInput( id, path, isRelativeToCalcCase, isOptional ) );
  }

  public void addOutput( final String id, final String path )
  {
    m_outputList.add( SimulationUtilitites.createOutput( id, path ) );
  }

  public void addOutput( final String id, final String path, final boolean isRelativeToCalcCase )
  {
    m_outputList.add( SimulationUtilitites.createOutput( id, path, isRelativeToCalcCase ) );
  }

  public void resetEntries( )
  {
    m_inputList.clear();
    m_outputList.clear();
  }

  public boolean useDefaultModelspec( )
  {
    return m_useAntLauncher || (m_inputList.isEmpty() && m_outputList.isEmpty());
  }

  public String getCalculationTypeID( )
  {
    return m_calculationTypeID;
  }

  public IPath getContainer( )
  {
    return m_container;
  }

  public Modeldata getModeldata( )
  {
    return SimulationUtilitites.createModelData( m_calculationTypeID, m_inputList, m_outputList );
  }

  public Map<String, Object> getAntProperties( )
  {
    final Map<String, Object> properties = new HashMap<String, Object>();
    for( final Input input : m_inputList )
    {
      properties.put( input.getId(), input.getPath() );
    }
    return properties;
  }

  public void setAntLauncher( final boolean useAntLauncher )
  {
    m_useAntLauncher = useAntLauncher;
  }

  public boolean useAntLauncher( )
  {
    return m_useAntLauncher;
  }

}
