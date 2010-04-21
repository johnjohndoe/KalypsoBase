package org.kalypso.calculation.chain.binding;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.eclipse.core.internal.resources.PlatformURLResourceConnection;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree_impl.model.feature.FeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

@SuppressWarnings("restriction")
public class CalculationChainMember extends Feature_Impl implements ICalculationChainMember
{
  private final FeatureBindingCollection<ICalculationChainMemberEntry> m_inputs = new FeatureBindingCollection<ICalculationChainMemberEntry>( this, ICalculationChainMemberEntry.class, QNAME_PROP_INPUTS );;

  private final FeatureBindingCollection<ICalculationChainMemberEntry> m_outputs = new FeatureBindingCollection<ICalculationChainMemberEntry>( this, ICalculationChainMemberEntry.class, QNAME_PROP_OUTPUTS );

  public CalculationChainMember( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  public FeatureBindingCollection<ICalculationChainMemberEntry> getInputs( )
  {
    return m_inputs;
  }

  public FeatureBindingCollection<ICalculationChainMemberEntry> getOutputs( )
  {
    return m_outputs;
  }

  public void addInput( final ICalculationChainMemberEntry entry )
  {
    m_inputs.add( entry );
  }

  public void addOutput( final ICalculationChainMemberEntry entry )
  {
    m_outputs.add( entry );
  }

  public String getTypeID( )
  {
    final Object property = getProperty( QNAME_PROP_TYPE_ID );
    return property == null ? "" : property.toString(); //$NON-NLS-1$
  }

  public void setTypeID( final String value )
  {
    setProperty( QNAME_PROP_TYPE_ID, value );
  }

  public int getOrdinalNumber( )
  {
    final Integer property = (Integer) getProperty( QNAME_PROP_ORDINAL_NUMBER );
    return property == null ? 0 : property.intValue();
  }

  public void setOrdinalNumber( final int value )
  {
    setProperty( QNAME_PROP_ORDINAL_NUMBER, value );
  }

  public boolean getUseAntLauncher( )
  {
    final Boolean property = (Boolean) getProperty( QNAME_PROP_USE_ANT_LAUNCHER );
    return property == null ? false : property.booleanValue();
  }

  public void setUseAntLauncher( final boolean value )
  {
    setProperty( QNAME_PROP_USE_ANT_LAUNCHER, value );
  }

  @Override
  public int compareTo( final ICalculationChainMember another )
  {
    return new Integer( getOrdinalNumber() ).compareTo( another.getOrdinalNumber() );
  }

  @Override
  public IPath getCalculationCaseFolder( ) throws URIException, MalformedURLException
  {
    final Object property = getProperty( QNAME_PROP_CALCULATION_CASE_FOLDER );
    if( property instanceof String )
    {
      final String encodedUrl = (String) property;
      final String decodedUrl = URIUtil.decode( encodedUrl );
      IPath path;
      
      if( encodedUrl.startsWith( PlatformURLResourceConnection.RESOURCE_URL_STRING ) )
      {
        path = ResourceUtilities.findPathFromURL( new URL( decodedUrl ) );
      }
      else
      {
        path = new Path( decodedUrl );
      }

      return path;
    }

    return null;
  }

  @Override
  public void setCalculationCaseFolder( final IPath path ) throws URIException
  {
    final String encodedPath = URIUtil.encodePath( path.toOSString() );
    setProperty( QNAME_PROP_CALCULATION_CASE_FOLDER, encodedPath );
  }

}
