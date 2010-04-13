package org.kalypso.model.wspm.core.gml;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.javax.xml.namespace.QNameUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilPointMarker;
import org.kalypso.model.wspm.core.profil.ProfilFactory;
import org.kalypso.model.wspm.core.profil.util.ProfilUtil;
import org.kalypso.model.wspm.core.util.WspmGeometryUtilities;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.observation.result.TupleResultUtilities;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;
import org.kalypso.ogc.sensor.timeseries.TimeserieUtils;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.gml.binding.commons.Image;
import org.kalypsodeegree_impl.model.feature.AbstractCachedFeature2;
import org.kalypsodeegree_impl.model.feature.FeatureCacheDefinition;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

public class ProfileFeatureBinding extends AbstractCachedFeature2 implements IProfileFeature
{
  // HACK: we define a pseudo qname that simulates a property. We use this property to cache the generated IProfil.
  private static final QName QNAME_PSEUDO_PROFILE = new QName( "--", "--" );

  private static final FeatureCacheDefinition CACHE_DEFINITION = new FeatureCacheDefinition();
  static
  {
    CACHE_DEFINITION.addCachedProperty( QNAME_LINE, QNAME_SRS );
    CACHE_DEFINITION.addCachedProperty( QNAME_LINE, QNAME_PROFILE );
    CACHE_DEFINITION.addCachedProperty( QNAME_LINE, ObservationFeatureFactory.OM_RESULT );
    CACHE_DEFINITION.addCachedProperty( QNAME_LINE, ObservationFeatureFactory.OM_RESULTDEFINITION );

    CACHE_DEFINITION.addCachedProperty( QNAME_PSEUDO_PROFILE, QNAME_SRS );
    CACHE_DEFINITION.addCachedProperty( QNAME_PSEUDO_PROFILE, QNAME_PROFILE );
    CACHE_DEFINITION.addCachedProperty( QNAME_PSEUDO_PROFILE, ObservationFeatureFactory.OM_RESULT );
    CACHE_DEFINITION.addCachedProperty( QNAME_PSEUDO_PROFILE, ObservationFeatureFactory.OM_RESULTDEFINITION );
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

    if( property.equals( QNAME_LINE ) )
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
    return getProperty( QNAME_LINE, GM_Curve.class );
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
    return (String) getProperty( QNAME_SRS );
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
    final Feature parent = getFeature().getOwner();
    if( parent != null && QNameUtilities.equals( parent.getFeatureType().getQName(), IWspmConstants.NS_WSPM, "WaterBody" ) ) //$NON-NLS-1$
      return new WspmWaterBody( parent );

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
    getFeature().setProperty( QNAME_SRS, srsName );
    getFeature().setEnvelopesUpdated();
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
   */
  @Override
  public Feature getFeature( )
  {
    return this;
  }

  /**
   * @see org.kalypsodeegree.model.feature.binding.IFeatureWrapper2#getGmlID()
   */
  @Override
  public String getGmlID( )
  {
    return this.getId();
  }

  public String getProfileType( )
  {
    return getProperty( ProfileFeatureFactory.QNAME_TYPE, String.class );
  }

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

    /* building of profile */
    // REMARK: handle buildings before table, because the setBuilding method resets the
    // corresponding table properties.
    final IObservation<TupleResult>[] profileObjects = getProfileObjects();
    if( profileObjects.length > 0 )
    {
      profil.createProfileObjects( profileObjects );
    }

    return profil;
  }

  @SuppressWarnings("unchecked")
  private IObservation<TupleResult>[] getProfileObjects( )
  {
    final List< ? > objects = (List< ? >) getProperty( QNAME_OBS_MEMBERS );
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
  public void setImage( final URL imageURL )
  {
    Image imageFeature = (Image) getProperty( QNAME_IMAGE_MEMBER );
    if( imageFeature == null )
    {
      final IFeatureType featureType = getFeatureType();
      final IFeatureType ft = featureType.getGMLSchema().getFeatureType( Image.QNAME );
      final IRelationType rt = (IRelationType) featureType.getProperty( QNAME_IMAGE_MEMBER );
      imageFeature = (Image) getWorkspace().createFeature( this, rt, ft );
      setProperty( QNAME_IMAGE_MEMBER, imageFeature );
    }

    try
    {
      imageFeature.setUri( imageURL == null ? null : imageURL.toURI() );
    }
    catch( final URISyntaxException e )
    {
      KalypsoModelWspmCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
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
    return createProfileSegment( profil, srsName, pointMarkerName );
  }

  public static GM_Curve createProfileSegment( final IProfil profil, String srsName, final String pointMarkerName )
  {
    try
    {
      final IRecord[] points = profil.getPoints();
      final List<GM_Position> positions = new ArrayList<GM_Position>( points.length );

      final int compRechtswert = TupleResultUtilities.indexOfComponent( profil, IWspmConstants.POINT_PROPERTY_RECHTSWERT );
      final int compHochwert = TupleResultUtilities.indexOfComponent( profil, IWspmConstants.POINT_PROPERTY_HOCHWERT );
      final int compBreite = TupleResultUtilities.indexOfComponent( profil, IWspmConstants.POINT_PROPERTY_BREITE );
      final int compHoehe = TupleResultUtilities.indexOfComponent( profil, IWspmConstants.POINT_PROPERTY_HOEHE );

      int left = 0;
      int right = points.length;

      if( pointMarkerName != null )
      {

        final IProfilPointMarker[] durchstroemte = profil.getPointMarkerFor( pointMarkerName );

        if( durchstroemte.length < 2 )
          return null;

        left = profil.indexOfPoint( durchstroemte[0].getPoint() );
        right = profil.indexOfPoint( durchstroemte[1].getPoint() );
      }

      // for( final IRecord point : points )
      for( int i = left; i < right; i++ )
      {
        final IRecord point = points[i];
        /* If there are no rw/hw create pseudo geometries from breite and station */
        final Double rw;
        final Double hw;

        if( compRechtswert != -1 && compHochwert != -1 )
        {
          rw = (Double) point.getValue( compRechtswert );
          hw = (Double) point.getValue( compHochwert );

          /* We assume here that we have a GAUSS-KRUEGER crs in a profile. */
          if( srsName == null )
            srsName = TimeserieUtils.getCoordinateSystemNameForGkr( Double.toString( rw ) );
        }
        else
        {
          if( compBreite == -1 )
            throw new IllegalStateException( "Cross sections without width or easting/northing attributes detected - geometric processing not possible." ); //$NON-NLS-1$

          rw = (Double) point.getValue( compBreite );
          hw = profil.getStation() * 1000;

          /* We assume here that we have a GAUSS-KRUEGER crs in a profile. */
          if( srsName == null )
            srsName = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
        }

        if( rw == null || hw == null || rw.isNaN() || hw.isNaN() )
          continue;

        final Double h = compHoehe == -1 ? null : (Double) point.getValue( compHoehe );

        final GM_Position position;
        if( h == null )
          position = GeometryFactory.createGM_Position( rw, hw );
        else
          position = GeometryFactory.createGM_Position( rw, hw, h );

        positions.add( position );
      }

      if( positions.size() < 2 )
        return null;

      final GM_Position[] poses = positions.toArray( new GM_Position[positions.size()] );
      final GM_Curve curve = GeometryFactory.createGM_Curve( poses, srsName );

      return (GM_Curve) WspmGeometryUtilities.GEO_TRANSFORMER.transform( curve );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return null;
  }
}
