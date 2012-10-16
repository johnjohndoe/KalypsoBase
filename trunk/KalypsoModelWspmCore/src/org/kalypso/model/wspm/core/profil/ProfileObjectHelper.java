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

    final IProfileObjectRecords records = profileObject.getRecords();

    for( int i = 0; i < result.size(); i++ )
    {
      final IRecord record = result.get( i );
      final IProfileObjectRecord profileObjectRecord = records.addNewRecord();

      updateStandardProperties( record, profileObjectRecord );
    }

    profileObject.setDescription( profileObjectObservation.getDescription() );
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

  public static void updateStandardProperties( final IRecord source, final IProfileObjectRecord target )
  {
    final TupleResult owner = source.getOwner();

    final IComponent idComponent = ProfileUtil.getFeatureComponent( IWspmConstants.POINT_PROPERTY_ID );
    final IComponent commentComponent = ProfileUtil.getFeatureComponent( IWspmConstants.POINT_PROPERTY_COMMENT );
    final IComponent breiteComponent = ProfileUtil.getFeatureComponent( IWspmConstants.POINT_PROPERTY_BREITE );
    final IComponent hoeheComponent = ProfileUtil.getFeatureComponent( IWspmConstants.POINT_PROPERTY_HOEHE );
    final IComponent rechtswertComponent = ProfileUtil.getFeatureComponent( IWspmConstants.POINT_PROPERTY_RECHTSWERT );
    final IComponent hochwertComponent = ProfileUtil.getFeatureComponent( IWspmConstants.POINT_PROPERTY_HOCHWERT );
    final IComponent codeComponent = ProfileUtil.getFeatureComponent( IWspmConstants.POINT_PROPERTY_CODE );

    final int idIndex = owner.indexOfComponent( idComponent );
    final int commentIndex = owner.indexOfComponent( commentComponent );
    final int breiteIndex = owner.indexOfComponent( breiteComponent );
    final int hoeheIndex = owner.indexOfComponent( hoeheComponent );
    final int rechtswertIndex = owner.indexOfComponent( rechtswertComponent );
    final int hochwertIndex = owner.indexOfComponent( hochwertComponent );
    final int codeIndex = owner.indexOfComponent( codeComponent );

    final Double breite = (Double)source.getValue( breiteIndex );
    /* missing components can only happen when updating components from the whole profile and creating new objects. Else, all components should be there */
    final String id = idIndex == -1 ? null : (String)source.getValue( idIndex );
    final String comment = commentIndex == -1 ? null : (String)source.getValue( commentIndex );
    final Double hoehe = hoeheIndex == -1 ? null : (Double)source.getValue( hoeheIndex );
    final Double rechtswert = rechtswertIndex == -1 ? null : (Double)source.getValue( rechtswertIndex );
    final Double hochwert = hochwertIndex == -1 ? null : (Double)source.getValue( hochwertIndex );
    final String code = codeIndex == -1 ? null : (String)source.getValue( codeIndex );

    target.setId( id );
    target.setComment( comment );
    target.setBreite( breite );
    target.setHoehe( hoehe );
    target.setRechtswert( rechtswert );
    target.setHochwert( hochwert );
    target.setCode( code );
  }
}