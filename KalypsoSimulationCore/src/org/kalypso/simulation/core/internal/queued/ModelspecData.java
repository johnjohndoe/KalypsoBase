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
package org.kalypso.simulation.core.internal.queued;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.kalypso.simulation.core.ISimulationDataProvider;
import org.kalypso.simulation.core.SimulationDescription;
import org.kalypso.simulation.core.SimulationException;
import org.kalypso.simulation.core.simspec.DataType;
import org.kalypso.simulation.core.simspec.Modelspec;

/**
 * @author belger
 */
public class ModelspecData
{
  private final Modelspec m_modelspec;

  private final Map<String, DataType> m_inputHash;

  private final Map<String, DataType> m_outputHash;

  public ModelspecData( final URL modelspecUrl, final Unmarshaller unmarshaller ) throws SimulationException
  {
    try
    {
      m_modelspec = (Modelspec) unmarshaller.unmarshal( modelspecUrl );

      m_inputHash = createHash( m_modelspec.getInput() );
      m_outputHash = createHash( m_modelspec.getOutput() );
    }
    catch( final JAXBException e )
    {
      throw new SimulationException( "Modelspezifikation konnte nicht geladen werden.", e );
    }
  }

  private Map<String, DataType> createHash( final List<DataType> list )
  {
    final HashMap<String, DataType> map = new HashMap<String, DataType>( list.size() );
    for( final DataType data : list )
      map.put( data.getId(), data );

    return map;
  }

  public DataType getInput( final String id )
  {
    return m_inputHash.get( id );
  }

  /**
   * Prüft, ob für alle benötigten ID eine eingabe da ist.
   *
   * @throws CalcJobServiceException
   */
  public void checkInput( final ISimulationDataProvider data ) throws SimulationException
  {
    for( final DataType input : m_inputHash.values() )
    {
      final String id = input.getId();
      final String description = input.getDescription();
      if( !input.isOptional() && !data.hasID( id ) )
        throw new SimulationException( "Keine Eingangsdaten für ID " + id + " (" + description + ") vorhanden.", null );
    }
  }

  public SimulationDescription[] getInput( )
  {
    return toDescription( m_inputHash.values() );
  }

  public SimulationDescription[] getOutput( )
  {
    return toDescription( m_outputHash.values() );
  }

  private SimulationDescription[] toDescription( final Collection<DataType> values )
  {
    final SimulationDescription[] beans = new SimulationDescription[values.size()];
    int count = 0;
    for( final DataType data : values )
      beans[count++] = new SimulationDescription( data.getId(), data.getDescription(), data.getType() );

    return beans;
  }

  public boolean hasOutput( final String id )
  {
    return m_outputHash.containsKey( id );
  }
}