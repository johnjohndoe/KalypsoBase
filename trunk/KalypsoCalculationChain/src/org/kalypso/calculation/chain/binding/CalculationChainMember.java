package org.kalypso.calculation.chain.binding;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree_impl.model.feature.FeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

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
    return property == null ? "" : property.toString();
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
  public IContainer getCalculationCaseFolder( )
  {
    final Object property = getProperty( QNAME_PROP_CALCULATION_CASE_FOLDER );
    if( property instanceof String )
    {
      final String url = (String) property;
      final Path path = new Path( url );
      IFolder folder;
      try
      {
        folder = ResourcesPlugin.getWorkspace().getRoot().getFolder( path );
        if( !folder.exists() )
          throw new IllegalStateException();

        return folder;
      }
      catch( final IllegalArgumentException e )
      {
        return ResourcesPlugin.getWorkspace().getRoot().getProject( url );
      }

    }

    return null;
  }

  @Override
  public void setCalculationCaseFolder( final IContainer container )
  {
    final String portableString = container.getFullPath().toPortableString();
// final String url = String.format( "platform:/resource/%s", portableString );

    setProperty( QNAME_PROP_CALCULATION_CASE_FOLDER, portableString );
  }

}
