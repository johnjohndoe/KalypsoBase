package org.kalypso.model.wspm.core.gml;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.util.pool.KeyInfo;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilListener;
import org.kalypso.model.wspm.core.profil.IProfileObject;
import org.kalypso.model.wspm.core.profil.ProfilFactory;
import org.kalypso.model.wspm.core.profil.ProfileObjectFactory;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.core.profil.util.ProfilUtil;
import org.kalypso.model.wspm.core.util.WspmGeometryUtilities;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.command.ChangeFeaturesCommand;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.gml.binding.commons.Image;
import org.kalypsodeegree_impl.model.feature.AbstractCachedFeature2;
import org.kalypsodeegree_impl.model.feature.FeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.FeatureCacheDefinition;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.LineString;

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

  private final IProfilListener m_profilListener = new IProfilListener()
  {
    @Override
    public void onProfilChanged( final ProfilChangeHint hint )
    {
      handleCachedProfileChanged( hint );
    }

    @Override
    public void onProblemMarkerChanged( final IProfil source )
    {
    }
  };

  private IFeatureBindingCollection<Image> m_images = null;

  public ProfileFeatureBinding( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues, CACHE_DEFINITION );
  }

  @Override
  protected Object recalculateProperty( final QName property, final Object oldValue )
  {
    if( property == QNAME_PSEUDO_PROFILE )
      return createProfile( (IProfil) oldValue );

    if( property.equals( QN_PROPERTY_LINE ) )
      return createProfileSegment( this, null );

    return super.recalculateProperty( property, oldValue );
  }

  private Object createProfile( final IProfil oldProfile )
  {
    try
    {
      if( oldProfile != null )
        oldProfile.removeProfilListener( m_profilListener );

      final IProfil profile = toProfile();

      if( profile != null )
        // TODO: validation
        profile.addProfilListener( m_profilListener );

      return profile;
    }
    catch( final Exception e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      KalypsoModelWspmCorePlugin.getDefault().getLog().log( status );

      return null;
    }
  }

  @Override
  public BigDecimal getBigStation( )
  {
    return (BigDecimal) getProperty( ProfileFeatureFactory.QNAME_STATION );
  }

  @Override
  public GM_Curve getLine( )
  {
    return getProperty( QN_PROPERTY_LINE, GM_Curve.class );
  }

  @Override
  public IProfil getProfil( )
  {
    return getProperty( QNAME_PSEUDO_PROFILE, IProfil.class );
  }

  @Override
  public String getSrsName( )
  {
    return (String) getProperty( QN_PROPERTY_SRS );
  }

  @Override
  public double getStation( )
  {
    final BigDecimal profileStation = getBigStation();

    return profileStation == null ? Double.NaN : profileStation.doubleValue();
  }

  @Override
  public WspmWaterBody getWater( )
  {
    final Feature parent = getOwner();
    if( parent instanceof WspmWaterBody )
      return (WspmWaterBody) parent;

    return null;
  }

  @Override
  public void setBigStation( final BigDecimal bigStation )
  {
    setProperty( ProfileFeatureFactory.QNAME_STATION, bigStation );
  }

  @Override
  public void setSrsName( final String srsName )
  {
    setProperty( QN_PROPERTY_SRS, srsName );
    setEnvelopesUpdated();
  }

  @Deprecated
  @Override
  public void setStation( final double station )
  {
    final BigDecimal bigStation = ProfilUtil.stationToBigDecimal( station );
    setBigStation( bigStation );
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
    final IProfil profil = ProfilFactory.createProfil( type, observation, this );

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
    final Feature parent = getOwner();
    if( parent instanceof WspmWaterBody )
    {
      final WspmWaterBody waterBody = (WspmWaterBody) parent;
      profil.setProperty( IWspmConstants.PROFIL_PROPERTY_WATERBODY_SRC, waterBody.getId() );
    }

    /* profile objects of profile */
    // REMARK: handle buildings before table, because the setBuilding method resets the
    // corresponding table properties.
    final IObservation<TupleResult>[] profileObjects = getProfileObjects();
    for( final IObservation<TupleResult> obs : profileObjects )
    {
      final IProfileObject profileObject = ProfileObjectFactory.createProfileObject( profil, obs );
      if( profileObject == null )
        System.out.println( "failed to create Object: " + obs.getName() ); //$NON-NLS-1$
      else
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

  @Override
  public Image addImage( final URI imageURI )
  {
    final IFeatureType featureType = getFeatureType();
    final IFeatureType ft = featureType.getGMLSchema().getFeatureType( Image.FEATURE_IMAGE );
    final IRelationType rt = (IRelationType) featureType.getProperty( QN_PROPERTY_IMAGE_MEMBER );
    final Image imageFeature = (Image) getWorkspace().createFeature( this, rt, ft );

    try
    {
      getWorkspace().addFeatureAsComposition( this, rt, -1, imageFeature );
      imageFeature.setUri( imageURI == null ? null : imageURI );
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

  @Override
  public synchronized IFeatureBindingCollection<Image> getImages( )
  {
    if( m_images == null )
      m_images = new FeatureBindingCollection<Image>( this, Image.class, QN_PROPERTY_IMAGE_MEMBER, true );

    return m_images;
  }

  @Override
  public LineString getJtsLine( ) throws GM_Exception
  {
    final String crs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

    final GM_Curve profileCurve = WspmGeometryUtilities.createProfileSegment( getProfil(), crs );
    final LineString profileLineString = (LineString) JTSAdapter.export( profileCurve );

    return profileLineString;
  }

  /**
   * @see org.kalypso.model.wspm.core.gml.IProfileProvider#addProfilProviderListener(org.kalypso.model.wspm.core.gml.IProfileProviderListener)
   */
  @Override
  public void addProfilProviderListener( final IProfileProviderListener l )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.kalypso.model.wspm.core.gml.IProfileProvider#removeProfilProviderListener(org.kalypso.model.wspm.core.gml.IProfileProviderListener)
   */
  @Override
  public void removeProfilProviderListener( final IProfileProviderListener l )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.kalypso.model.wspm.core.gml.IProfileProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.kalypso.model.wspm.core.gml.IProfileProvider#getResult()
   */
  @Override
  public Object getResult( )
  {
    // FIXME
    // final ProfileAndResults profileAndResults = ProfileAndResults.search( fs );

    // TODO Auto-generated method stub
    return null;
  }

  // Validation
// final CommandableWorkspace workspace = profileAndResults.getWorkspace();
// final URL workspaceContext = workspace == null ? null : workspace.getContext();
// m_file = workspaceContext == null ? null : ResourceUtilities.findFileFromURL( workspaceContext );

  protected void handleCachedProfileChanged( final ProfilChangeHint hint )
  {
    final IProfil profile = getProfil();
    if( profile == null )
      return;

    final CommandableWorkspace workspace = findCommanableWorkspace();
    if( workspace == null )
      return;

    try
    {
      if( (hint.getEvent() & ProfilChangeHint.DATA_CHANGED) != 0 )
      {
        final FeatureChange[] featureChanges = ProfileFeatureFactory.toFeatureAsChanges( profile, this );

        final ChangeFeaturesCommand command = new ChangeFeaturesCommand( workspace, featureChanges );
        workspace.postCommand( command );
      }
    }
    catch( final Exception e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      KalypsoModelWspmCorePlugin.getDefault().getLog().log( status );
    }
  }

  // FIXME: MEGA-HACK - find right commandbleWorkspace via Pool, should be solved otherwise
  private CommandableWorkspace findCommanableWorkspace( )
  {
    final GMLWorkspace myWorkspace = getWorkspace();

    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    final KeyInfo[] infos = pool.getInfos();
    for( final KeyInfo info : infos )
    {
      final Object object = info.getObject();
      if( object instanceof CommandableWorkspace )
      {
        final CommandableWorkspace cmdWorkspace = (CommandableWorkspace) object;
        if( cmdWorkspace.getWorkspace() == myWorkspace )
          return cmdWorkspace;
      }
    }

    return null;
  }
}