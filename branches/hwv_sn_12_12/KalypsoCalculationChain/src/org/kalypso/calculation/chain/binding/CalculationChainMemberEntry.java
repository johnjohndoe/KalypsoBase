package org.kalypso.calculation.chain.binding;

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

public class CalculationChainMemberEntry extends Feature_Impl implements ICalculationChainMemberEntry
{
  public CalculationChainMemberEntry( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  @Override
  public String getKey( )
  {
    final Object property = getProperty( QNAME_PROP_KEY );
    return property == null ? null : property.toString();
  }

  @Override
  public String getValue( )
  {
    final Object property = getProperty( QNAME_PROP_VALUE );
    return property == null ? null : property.toString();
  }

  @Override
  public boolean isOptional( )
  {
    final Boolean property = (Boolean) getProperty( QNAME_PROP_IS_OPTIONAL );
    return property == null ? false : property.booleanValue();
  }

  @Override
  public boolean isRelativeToCalculationCase( )
  {
    final Boolean property = (Boolean) getProperty( QNAME_PROP_IS_RELATIVE_TO_CALC_CASE );
    return property == null ? true : property.booleanValue();
  }

  @Override
  public void setKey( final String value )
  {
    setProperty( QNAME_PROP_KEY, value );
  }

  @Override
  public void setValue( final String value )
  {
    setProperty( QNAME_PROP_VALUE, value );
  }

  @Override
  public void setOptional( final boolean value )
  {
    setProperty( QNAME_PROP_IS_OPTIONAL, value );
  }

  @Override
  public void setRelativeToCalculationCase( final boolean value )
  {
    setProperty( QNAME_PROP_IS_RELATIVE_TO_CALC_CASE, value );
  }

}
