/*--------------- Kalypso-Header -------------------------------------------

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

 --------------------------------------------------------------------------*/
package org.kalypso.ogc.sensor;

import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.IRequest;

/**
 * Eine sog. Observation im Sinne von OGC Sensor-ML. Beschreibt eine maschinelle oder menschliche Wert-Erfassung.
 * 
 * @author schlienger
 */
public interface IObservation extends IObservationEventProvider
{
  /**
   * Returns the name of this Observation
   * 
   * @return name
   */
  String getName( );

  /**
   * Returns the list of Metadata.
   * 
   * @return metadata
   */
  MetadataList getMetadataList( );

  /**
   * Returns the list of axis
   * 
   * @return axes array
   */
  IAxis[] getAxes( );

  /**
   * Returns the values resulting from the measurements this observation stands for.
   * 
   * @param args
   *          some client defined arguments that can be interpreted by the implementation. Implementors of this
   *          interface can use this parameter, but they are not forced to do so.
   * @return model
   * @throws SensorException
   */
  ITupleModel getValues( final IRequest args ) throws SensorException;

  /**
   * Sets the given values.
   * 
   * @param values
   * @throws SensorException
   */
  void setValues( final ITupleModel values ) throws SensorException;

  /**
   * Returns the localisation of the base file behind this observation.<br/>
   * Also may serve as identifier.
   * 
   * @return href
   */
  String getHref( );
}