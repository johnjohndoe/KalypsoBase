package org.kalypso.model.wspm.core.gml;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfileObject;
import org.kalypso.model.wspm.core.profil.ProfilFactory;
import org.kalypso.model.wspm.core.profil.ProfileObjectFactory;
import org.kalypso.model.wspm.core.profil.util.ProfilUtil;
import org.kalypso.model.wspm.core.util.WspmGeometryUtilities;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.gml.binding.commons.Image;
import org.kalypsodeegree_impl.model.feature.AbstractCachedFeature2;
import org.kalypsodeegree_impl.model.feature.FeatureCacheDefinition;

// FIXME: we have in parallel still the feature type handler for this kind of feature.
// These two concepts should not be used both at the same time. Remove the feature type handler!
public class ProfileFeatureBinding extends AbstractCachedFeature2 implements IProfileFeature
{
  // HACK: we define a pseudo qname that simulates a property. We use this property to cache the generated IProfil.
  private static final QName QNAME_PSEUDO_PROFILE = new QName( "--", "--" ); //$NON-NLS-1$ //$NON-NLS-2$

  private static final FeatureCacheDefinition CACHE_DEFINITION = new FeatureCacheDefinition();
  static
  {
    CACHE_DEFINITION.addCachedProperty( QN_PROPERTY_LINE, QN_PROPERTY_SRS );
    CACHE_DEFINITION.addCachedProperty( QN_PROPERTY_LINE, QN_PROFILE );
    CACHE_DEFINITION.addCachedProperty( QN_PROPERTY_LINE, ObservationFeatureFactory.OM_RESULT );
    CACHE_DEFINITION.addCachedProperty( QN_PROPERTY_LINE, ObservationFeatureFactory.OM_RESULTDEFINITION );

    CACHE_DEFINITION.addCachedProperty( QNAME_PSEUDO_PROFILE, QN_NAME );
    CACHE_DEFINITION.addCachedProperty( QNAME_PSEUDO_PROFILE, QN_DESCRIPTION );
    CACHE_DEFINITION.addCachedProperty( QNAME_PSEUDO_PROFILE, QN_PROPERTY_SRS );
    CACHE_DEFINITION.addCachedProperty( QNAME_PSEUDO_PROFILE, QN_PROFILE );
    CACHE_DEFINITION.addCachedProperty( QNAME_PSEUDO_PROFILE, ObservationFeatureFactory.OM_RESULT );
    CACHE_DEFINITION.addCachedProperty( QNAME_PSEUDO_PROFILE, ObservationFeatureFactory.OM_RESULTDEFINITION );
    CACHE_DEFINITION.addCachedProperty( QNAME_PSEUDO_PROFILE, QN_PROPERTY_OBS_MEMBERS );
  }

  public ProfileFeatureBinding( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues, CACHE_DEFINITION );
  }

  @Override
  protected Object recalculateProperty( final QName property )
  {
    if( property == QNAME_PSEUDO_PROFILE )
      return createProfile();

    if( property.equals( QN_PROPERTY_LINE ) )
      return createProfileSegment( this, null );

    return super.recalculateProperty( property );
  }

  private Object createProfile( )
  {
    try
    {
      return toProfile();
    }
    catch( final Exception e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      KalypsoModelWspmCorePlugin.getDefault().getLog().log( status );

      return null;
    }
  }

  /**
   * @see org.kalypso.model.wspm.core.gml.IProfileFeature#getBigStation()
   */
  @Override
  public BigDecimal getBigStation( )
  {
    return (BigDecimal) getProperty( ProfileFeatureFactory.QNAME_STATION );
  }

  /**
   * @see org.kalypso.model.wspm.core.gml.IProfileFeature#getLine()
   */
  @Override
  public GM_Curve getLine( )
  {
    return getProperty( QN_PROPERTY_LINE, GM_Curve.class );
  }

  /**
   * @see org.kalypso.model.wspm.core.gml.IProfileFeature#getProfil()
   */
  @Override
  public IProfil getProfil( )
  {
    return getProperty( QNAME_PSEUDO_PROFILE, IProfil.class );
  }

  /**
   * @see org.kalypso.model.wspm.core.gml.IProfileFeature#getSrsName()
   */
  @Override
  public String getSrsName( )
  {
    return (String) getProperty( QN_PROPERTY_SRS );
  }

  /**
   * @see org.kalypso.model.wspm.core.gml.IProfileFeature#getStation()
   */
  @Override
  public double getStation( )
  {
    final BigDecimal profileStation = getBigStation();

    return profileStation == null ? Double.NaN : profileStation.doubleValue();
  }

  /**
   * @see org.kalypso.model.wspm.core.gml.IProfileFeature#getWater()
   */
  @Override
  public WspmWaterBody getWater( )
  {
    final Feature parent = getOwner();
    if( parent instanceof WspmWaterBody )
      return (WspmWaterBody) parent;

    return null;
  }

  /**
   * @see org.kalypso.model.wspm.core.gml.IProfileFeature#setBigStation(java.math.BigDecimal)
   */
  @Override
  public void setBigStation( final BigDecimal bigStation )
  {
    setProperty( ProfileFeatureFactory.QNAME_STATION, bigStation );
  }

  /**
   * @see org.kalypso.model.wspm.core.gml.IProfileFeature#setSrsName(java.lang.String)
   */
  @Override
  public void setSrsName( final String srsName )
  {
    setProperty( QN_PROPERTY_SRS, srsName );
    setEnvelopesUpdated();
  }

  /**
   * @see org.kalypso.model.wspm.core.gml.IProfileFeature#setStation(double)
   */
  @Deprecated
  @Override
  public void setStation( final double station )
  {
    final BigDecimal bigStation = ProfilUtil.stationToBigDecimal( station );
    setBigStation( bigStation );
  }

  /**
   * @see org.kalypsodeegree.model.feature.binding.IFeatureWrapper2#getFeature()
   * @Deprecated: Implementation of {@link org.kalypsodeegree.model.feature.binding.IFeatureWrapper2}, do not use any
   *              more. This object already is a feature.
   */
  @Override
  @Deprecated
  public Feature getFeature( )
  {
    return this;
  }

  /**
   * @see org.kalypsodeegree.model.feature.binding.IFeatureWrapper2#getGmlID()
   * @Deprecated: Implementation of {@link org.kalypsodeegree.model.feature.binding.IFeatureWrapper2}, do not use any
   *              more. This object already is a feature.
   */
  @Override
  @Deprecated
  public String getGmlID( )
  {
    return this.getId();
  }

  @Override
  public String getProfileType( )
  {
    return getProperty( ProfileFeatureFactory.QNAME_TYPE, String.class );
  }

  @Override
  public void setProfileType( final String type )
  {
    setProperty( ProfileFeatureFactory.QNAME_TYPE, type );
  }

  private IProfil toProfile( )
  {
    /* profile type */
    final String type = getProfileType();
    if( type == null )
      return null;

    /* observation of profile */
    final IObservation<TupleResult> observation = ObservationFeatureFactory.toObservation( this );
    final IProfil profil = ProfilFactory.createProfil( type, observation );

    /* station of profile */
    final BigDecimal bigStation = (BigDecimal) getProperty( ProfileFeatureFactory.QNAME_STATION );
    if( bigStation != null )
    {
      final double station = bigStation.doubleValue();
      profil.setStation( station );
    }

    /* Some metadata */
    final String crs = getSrsName();
    profil.setProperty( IWspmConstants.PROFIL_PROPERTY_CRS, crs );

    /* TODO - @hack - add flow direction to profile meta data */
    final Feature parent = getParent();
    if( parent instanceof WspmWaterBody )
    {
      final WspmWaterBody waterBody = (WspmWaterBody) parent;
      profil.setProperty( IWspmConstants.PROFIL_PROPERT_WATERBODY_SRC, waterBody.getId() );
    }

    /* profile objects of profile */
    // REMARK: handle buildings before table, because the setBuilding method resets the
    // corresponding table properties.
    final IObservation<TupleResult>[] profileObjects = getProfileObjects();
    for( final IObservation<TupleResult> obs : profileObjects )
    {
      final IProfileObject profileObject = ProfileObjectFactory.createProfileObject( profil, obs );
      if( profileObject != null )
        profil.addProfileObjects( profileObject );
    }

    return profil;
  }

  @SuppressWarnings("unchecked")
  private IObservation<TupleResult>[] getProfileObjects( )
  {
    final List< ? > objects = (List< ? >) getProperty( QN_PROPERTY_OBS_MEMBERS );
    if( objects.size() == 0 )
      return new IObservation[] {};

    final List<IObservation<TupleResult>> myResults = new ArrayList<IObservation<TupleResult>>();

    // iterate over all profile objects and create its IProfileObject representation
    for( final Object obj : objects )
    {
      final IObservation<TupleResult> obs = ObservationFeatureFactory.toObservation( (Feature) obj );
      myResults.add( obs );
    }

    return myResults.toArray( new IObservation[] {} );
  }

  /**
   * @see org.kalypso.model.wspm.core.gml.IProfileFeature#setImage(java.net.URL)
   */
  @Override
  public Image addImage( final URL imageURL )
  {
    final IFeatureType featureType = getFeatureType();
    final IFeatureType ft = featureType.getGMLSchema().getFeatureType( Image.QNAME );
    final IRelationType rt = (IRelationType) featureType.getProperty( QN_PROPERTY_IMAGE_MEMBER );
    final Image imageFeature = (Image) getWorkspace().createFeature( this, rt, ft );

    try
    {
      getWorkspace().addFeatureAsComposition( this, rt, -1, imageFeature );
      imageFeature.setUri( imageURL == null ? null : imageURL.toURI() );
    }
    catch( final Exception e )
    {
      KalypsoModelWspmCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    return imageFeature;
  }

  // TODO: don't! Please discuss with me (Gernot)
  @Override
  public GM_Object getDefaultGeometryPropertyValue( )
  {
    return getLine();
  }

  public static GM_Curve createProfileSegment( final IProfileFeature profile, final String pointMarkerName )
  {
    final IProfil profil = profile.getProfil();
    if( profil == null )
      return null;

    final String srsName = profile.getSrsName();
    return WspmGeometryUtilities.createProfileSegment( profil, srsName, pointMarkerName );
  }

}
