/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.model.wspm.core.gml;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.IWspmNamespaces;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileMetadata;
import org.kalypso.model.wspm.core.profil.IProfileObject;
import org.kalypso.model.wspm.core.profil.ProfileFactory;
import org.kalypso.model.wspm.core.profil.ProfileObjectFactory;
import org.kalypso.model.wspm.core.profil.util.ProfileUtil;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * Intermediates between the {@link IProfil} interface and Features of QName {org.kalypso.model.wspm.profile}profile.
 *
 * @author Gernot Belger
 */
public final class ProfileFeatureFactory implements IWspmConstants
{
  public static final QName QNAME_STATION = new QName( IWspmConstants.NS_WSPMPROF, "station" ); //$NON-NLS-1$

  public static final QName QNAME_TYPE = new QName( IWspmConstants.NS_WSPMPROF, "type" ); //$NON-NLS-1$

  public static final QName QN_PROF_PROFILE = new QName( IWspmConstants.NS_WSPMPROF, "Profile" ); //$NON-NLS-1$

  public static final String DICT_COMP_PROFILE_PREFIX = "urn:ogc:gml:dict:kalypso:model:wspm:profilePointComponents#"; //$NON-NLS-1$

  private ProfileFeatureFactory( )
  {
    // private: never instantiate
  }

  /**
   * Writes the contents of a profile into a feature. The feature must substitute
   * {org.kalypso.model.wspm.profile}profile.
   * <p>
   * Assumes, that the given feature is empty.
   * </p>
   */
  static void toFeature( final IProfile profile, final IProfileFeature targetFeature )
  {
    final FeatureChange[] changes = toFeatureAsChanges( profile, targetFeature );
    for( final FeatureChange change : changes )
      change.getFeature().setProperty( change.getProperty(), change.getNewValue() );

    targetFeature.setEnvelopesUpdated();
  }

  /**
   * Converts a profile to a feature. The feature is not yet changed but the needed changes are returned as feature
   * changes.
   */
  @SuppressWarnings( "unchecked" )//$NON-NLS-1$
  static FeatureChange[] toFeatureAsChanges( final IProfile profile, final IProfileFeature targetFeature )
  {
    final IFeatureType featureType = targetFeature.getFeatureType();

    final List<FeatureChange> changes = new ArrayList<>();

    try
    {
      /* Name, description and srs. */
      final String name = profile.getName();
      final String description = profile.getComment();
      final String srs = profile.getSrsName();
      changes.add( new FeatureChange( targetFeature, featureType.getProperty( IProfileFeature.PROPERTY_SRS ), srs ) );

      final List<String> namelist = new ArrayList<>();
      namelist.add( name );

      changes.add( getFeatureChangeName( targetFeature, name ) );
      changes.add( new FeatureChange( targetFeature, featureType.getProperty( Feature.QN_DESCRIPTION ), description ) );

      /* Station. */
      final double station = profile.getStation();
      changes.add( getFeatureChangeStation( targetFeature, station ) );

      /* Type. */
      final String profiletype = profile.getType();
      changes.add( new FeatureChange( targetFeature, featureType.getProperty( ProfileFeatureFactory.QNAME_TYPE ), profiletype ) );

      /* Ensure that record-definition is there. */
      final FeatureChange changeRecordDefinition = checkRecordDefinition( targetFeature );
      if( changeRecordDefinition != null )
        changes.add( changeRecordDefinition );

      final FeatureChange[] obsChanges = ObservationFeatureFactory.toFeatureAsChanges( profile, targetFeature );
      Collections.addAll( changes, obsChanges );

      /* Profile Objects. */
      final IRelationType profileObjectsRelationType = (IRelationType)featureType.getProperty( new QName( IWspmNamespaces.NS_WSPMPROF, "member" ) );
      final FeatureList profileObjectList = FeatureFactory.createFeatureList( targetFeature, profileObjectsRelationType, new Feature[] {} );
      final IFeatureType profileObjectType = featureType.getGMLSchema().getFeatureType( IObservation.QNAME_OBSERVATION );
      final IRelationType profileObjectParentRelation = profileObjectList.getPropertyType();

      final IProfileObject[] profileObjects = profile.getProfileObjects();
      for( final IProfileObject profileObject : profileObjects )
      {
        final Feature profileObjectFeature = targetFeature.getWorkspace().createFeature( targetFeature, profileObjectParentRelation, profileObjectType );
        profileObjectList.add( profileObjectFeature );

        // TODO How to handle the metadata of the profile objects...
        final FeatureChange[] featureAsChanges = ObservationFeatureFactory.toFeatureAsChanges( profileObject.getObservation(), profileObjectFeature );
        Collections.addAll( changes, featureAsChanges );
      }

      /* Always to set the building, even if null. */
      changes.add( new FeatureChange( targetFeature, profileObjectsRelationType, profileObjectList ) );

      /* Metadata. */
      final IRelationType metadataRelationType = (IRelationType)featureType.getProperty( IProfileFeature.MEMBER_METADATA );
      final FeatureList metadataList = FeatureFactory.createFeatureList( targetFeature, metadataRelationType, new Feature[] {} );
      final IFeatureType medatataType = featureType.getGMLSchema().getFeatureType( Metadata.FEATURE_METADATA );
      final IRelationType metadataParentRelation = metadataList.getPropertyType();

      final IProfileMetadata metadata = profile.getMetadata();
      final String[] keys = metadata.getKeys();
      for( final String key : keys )
      {
        final String value = metadata.getMetadata( key );

        final Feature metadataFeature = targetFeature.getWorkspace().createFeature( targetFeature, metadataParentRelation, medatataType );
        metadataList.add( metadataFeature );

        changes.add( new FeatureChange( metadataFeature, medatataType.getProperty( Metadata.PROPERTY_KEY ), key ) );
        changes.add( new FeatureChange( metadataFeature, medatataType.getProperty( Metadata.PROPERTY_VALUE ), value ) );
      }

      changes.add( new FeatureChange( targetFeature, metadataRelationType, metadataList ) );
    }
    catch( final Exception e )
    {
      // TODO: better error handling!
      e.printStackTrace();
    }

    return changes.toArray( new FeatureChange[changes.size()] );
  }

  private static FeatureChange getFeatureChangeName( final Feature feature, final String profileName )
  {
    final List<String> namelist = new ArrayList<>();
    namelist.add( profileName );

    return new FeatureChange( feature, Feature.QN_NAME, namelist );
  }

  private static FeatureChange getFeatureChangeStation( final Feature feature, final double profileStation )
  {
    if( Double.isNaN( profileStation ) || Double.isInfinite( profileStation ) )
      return new FeatureChange( feature, ProfileFeatureFactory.QNAME_STATION, null );
    else
    {
      final BigDecimal bigStation = ProfileUtil.stationToBigDecimal( profileStation );
      return new FeatureChange( feature, ProfileFeatureFactory.QNAME_STATION, bigStation );
    }
  }

  private static FeatureChange checkRecordDefinition( final Feature feature )
  {
    final Feature recordDefinition = FeatureHelper.resolveLink( feature, ObservationFeatureFactory.OM_RESULTDEFINITION );
    if( recordDefinition == null )
    {
      final Feature rd = ObservationFeatureFactory.createSafeFeature( feature, ObservationFeatureFactory.OM_RESULTDEFINITION, ObservationFeatureFactory.SWE_RECORDDEFINITIONTYPE );
      return new FeatureChange( feature, ObservationFeatureFactory.OM_RESULTDEFINITION, rd );
    }

    return null;
  }

  static IProfile toProfile( final ProfileFeatureBinding profileFeature )
  {
    /* Profile type. */
    final String type = profileFeature.getProfileType();
    if( type == null )
      return null;

    /* Observation of profile. */
    final IObservation<TupleResult> observation = ObservationFeatureFactory.toObservation( profileFeature );
    final IProfile profil = ProfileFactory.createProfil( type, observation, profileFeature );

    /* Station of profile. */
    final BigDecimal bigStation = (BigDecimal)profileFeature.getProperty( ProfileFeatureFactory.QNAME_STATION );
    if( bigStation != null )
    {
      final double station = bigStation.doubleValue();
      profil.setStation( station );
    }

    /* Srs of profile. */
    profil.setSrsName( profileFeature.getSrsName() );

    /* TODO - @hack - add flow direction to profile metadata. */
    final Feature parent = profileFeature.getOwner();
    if( parent instanceof WspmWaterBody )
    {
      final WspmWaterBody waterBody = (WspmWaterBody)parent;
      profil.getMetadata().setMetadata( IWspmConstants.PROFIL_PROPERTY_FLOW_DIRECTION, Boolean.toString( waterBody.isDirectionUpstreams() ) );
    }

    /* Profile objects of profile. */
    // REMARK: handle buildings before table, because the setBuilding method resets the corresponding table properties.
    final IObservation<TupleResult>[] profileObjects = profileFeature.getProfileObjects();
    for( final IObservation<TupleResult> obs : profileObjects )
    {
      // TODO How to handle the metadata of the profile objects...
      final IProfileObject profileObject = ProfileObjectFactory.createProfileObject( profil, obs );
      if( profileObject == null )
        System.out.println( "failed to create Object: " + obs.getName() ); //$NON-NLS-1$
      else
        profil.addProfileObjects( profileObject );
    }

    /* Metadata. */
    final IProfileMetadata profileMetadata = profil.getMetadata();

    final IFeatureBindingCollection<Metadata> metadataList = profileFeature.getMetadata();
    for( final Metadata metadata : metadataList )
    {
      final String key = metadata.getKey();
      final String value = metadata.getValue();

      profileMetadata.setMetadata( key, value );
    }

    return profil;
  }
}