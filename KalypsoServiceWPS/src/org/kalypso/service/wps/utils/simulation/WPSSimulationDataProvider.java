/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 * 
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 * 
 *  ---------------------------------------------------------------------------*/
package org.kalypso.service.wps.utils.simulation;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import net.opengeospatial.ows.BoundingBoxType;
import net.opengeospatial.wps.ComplexValueType;
import net.opengeospatial.wps.DataInputsType;
import net.opengeospatial.wps.Execute;
import net.opengeospatial.wps.IOValueType;
import net.opengeospatial.wps.LiteralValueType;
import net.opengeospatial.wps.IOValueType.ComplexValueReference;

import org.apache.commons.io.FileUtils;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.simulation.core.ISimulationDataProvider;
import org.kalypso.simulation.core.SimulationException;

/**
 * A DataProvider for simulations running in the WPS service.
 * 
 * @author Holger Albert
 */
public class WPSSimulationDataProvider implements ISimulationDataProvider
{
  /**
   * The execute request contains the input data.
   */
  private Execute m_execute = null;

  /**
   * Contains the id of the inputs as key and the input itself as value.
   */
  private Map<String, Object> m_inputList = null;

  /**
   * The temporary directory.
   */
  private final File m_tmpDir;

  /**
   * The constructor.
   * 
   * @param execute
   *          The execute request.
   */
  public WPSSimulationDataProvider( final Execute execute, final File tmpDir ) throws SimulationException
  {
    m_execute = execute;
    m_tmpDir = tmpDir;
    m_inputList = index( m_execute );
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationDataProvider#dispose()
   */
  public void dispose( )
  {
    /*
     * The input data will not be deleted, this is a concern of the client, because only he could have write access to
     * this place.
     */
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationDataProvider#getInputForID(java.lang.String)
   */
  public Object getInputForID( final String id ) throws SimulationException
  {
    final Object input = m_inputList.get( id );
    if( input == null )
    {
      throw new SimulationException( "Input not available with the ID: " + id, null );
    }

    if( input instanceof ComplexValueType )
    {
      final ComplexValueType complexValue = (ComplexValueType) input;

      final List<Object> content = complexValue.getContent();
      if( content.size() == 0 )
      {
        return null;
      }

      final String hexString = (String) content.get( 0 );

      try
      {
        final byte[] bytes = DatatypeConverter.parseHexBinary( hexString );
        final File file = FileUtilities.createNewUniqueFile( "complexValue_", m_tmpDir );
        FileUtils.writeByteArrayToFile( file, bytes );
        return file.toURI();
      }
      catch( final Exception e )
      {
        throw new SimulationException( "Problem converting complex-valued input from hexadecimal binary to file.", e );
      }
    }
    else if( input instanceof LiteralValueType )
    {
      final LiteralValueType literalValue = (LiteralValueType) input;
      final String dataType = literalValue.getDataType();
      final String value = literalValue.getValue();

      // TODO Inspect further. Perhaps use QName(XS, "string") and so on. Don't forget the WPSSimulationResultEater
      // and the SimulationJob, if you change this.
      // maybe dataType is xs:...
      // TODO: consider unit of measure (uom)
      if( dataType.endsWith( "string" ) )
      {
        return DatatypeConverter.parseString( value );
      }
      else if( dataType.endsWith( "int" ) )
      {
        return DatatypeConverter.parseInt( value );
      }
      else if( dataType.endsWith( "double" ) )
      {
        return DatatypeConverter.parseDouble( value );
      }
      else if( dataType.endsWith( "boolean" ) )
      {
        return DatatypeConverter.parseBoolean( value );
      }

      throw new SimulationException( "Invalid type for literal value: " + dataType );
    }
    else if( input instanceof ComplexValueReference )
    {
      final ComplexValueReference complexValueReference = (ComplexValueReference) input;
      final String reference = complexValueReference.getReference();

      try
      {
        final URL url = new URL( reference );
        return url;
      }
      catch( final MalformedURLException e )
      {
        try
        {
          final URI uri = new URI( reference );
          return uri;
        }
        catch( final URISyntaxException e2 )
        {
          throw new SimulationException( "Value reference is not a valid URI.", e2 );
        }
      }
    }
    else
    {
      /* BoundingBoxType. */
      final BoundingBoxType boundingBox = (BoundingBoxType) input;
      return boundingBox;
    }
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationDataProvider#hasID(java.lang.String)
   */
  public boolean hasID( final String id )
  {
    return m_inputList.containsKey( id );
  }

  /**
   * Indexes the input values with their id.
   * 
   * @param execute
   *          The execute request, containing the input data.
   * @return The indexed map.
   */
  private Map<String, Object> index( final Execute execute ) throws SimulationException
  {
    final Map<String, Object> inputList = new LinkedHashMap<String, Object>();

    final DataInputsType dataInputs = execute.getDataInputs();
    final List<IOValueType> inputs = dataInputs.getInput();
    for( final IOValueType input : inputs )
    {
      Object value = null;
      if( input.getComplexValue() != null )
      {
        value = input.getComplexValue();
      }
      else if( input.getLiteralValue() != null )
      {
        value = input.getLiteralValue();
      }
      else if( input.getComplexValueReference() != null )
      {
        value = input.getComplexValueReference();
      }
      else if( input.getBoundingBoxValue() != null )
      {
        value = input.getBoundingBoxValue();
      }
      else
      {
        throw new SimulationException( "Input has no valid value!", null );
      }

      inputList.put( input.getIdentifier().getValue(), value );
    }

    return inputList;
  }
}