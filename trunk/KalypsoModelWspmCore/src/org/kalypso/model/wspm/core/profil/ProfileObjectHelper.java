/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.model.wspm.core.profil;

import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.gml.ProfileObjectBinding;
import org.kalypso.model.wspm.core.profil.util.ProfileUtil;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;

/**
 * @author Holger Albert
 */
public class ProfileObjectHelper
{
  private ProfileObjectHelper( )
  {
  }

  public static void fillRecords( final ProfileObjectBinding profileObjectBinding, final IProfileObject profileObject )
  {
    final IObservation<TupleResult> profileObjectObservation = ObservationFeatureFactory.toObservation( profileObjectBinding );
    final TupleResult result = profileObjectObservation.getResult();

    final IComponent idComponent = ProfileUtil.getFeatureComponent( IWspmConstants.POINT_PROPERTY_ID );
    final IComponent commentComponent = ProfileUtil.getFeatureComponent( IWspmConstants.POINT_PROPERTY_COMMENT );
    final IComponent breiteComponent = ProfileUtil.getFeatureComponent( IWspmConstants.POINT_PROPERTY_BREITE );
    final IComponent hoeheComponent = ProfileUtil.getFeatureComponent( IWspmConstants.POINT_PROPERTY_HOEHE );
    final IComponent rechtswertComponent = ProfileUtil.getFeatureComponent( IWspmConstants.POINT_PROPERTY_RECHTSWERT );
    final IComponent hochwertComponent = ProfileUtil.getFeatureComponent( IWspmConstants.POINT_PROPERTY_HOCHWERT );
    final IComponent codeComponent = ProfileUtil.getFeatureComponent( IWspmConstants.POINT_PROPERTY_CODE );

    final int idIndex = result.indexOfComponent( idComponent );
    final int commentIndex = result.indexOfComponent( commentComponent );
    final int breiteIndex = result.indexOfComponent( breiteComponent );
    final int hoeheIndex = result.indexOfComponent( hoeheComponent );
    final int rechtswertIndex = result.indexOfComponent( rechtswertComponent );
    final int hochwertIndex = result.indexOfComponent( hochwertComponent );
    final int codeIndex = result.indexOfComponent( codeComponent );

    final IProfileObjectRecords records = profileObject.getRecords();

    for( int i = 0; i < result.size(); i++ )
    {
      final IRecord record = result.get( i );

      final String id = (String)record.getValue( idIndex );
      final String comment = (String)record.getValue( commentIndex );
      final Double breite = (Double)record.getValue( breiteIndex );
      final Double hoehe = (Double)record.getValue( hoeheIndex );
      final Double rechtswert = (Double)record.getValue( rechtswertIndex );
      final Double hochwert = (Double)record.getValue( hochwertIndex );
      final String code = (String)record.getValue( codeIndex );

      final IProfileObjectRecord profileObjectRecord = records.addNewRecord();
      profileObjectRecord.setId( id );
      profileObjectRecord.setComment( comment );
      profileObjectRecord.setBreite( breite );
      profileObjectRecord.setHoehe( hoehe );
      profileObjectRecord.setRechtswert( rechtswert );
      profileObjectRecord.setHochwert( hochwert );
      profileObjectRecord.setCode( code );
    }
  }

  public static void fillMetadata( final ProfileObjectBinding profileObjectBinding, final IProfileObject profileObject )
  {
    final IProfileMetadata profileObjectMetadata = profileObject.getMetadata();

    final String[] metadataKeys = profileObjectBinding.getMetadataKeys();
    for( final String metadataKey : metadataKeys )
    {
      final String metadataValue = profileObjectBinding.getMetadata( metadataKey );
      if( metadataValue != null && metadataValue.length() > 0 )
        profileObjectMetadata.setMetadata( metadataKey, metadataValue );
    }
  }
}