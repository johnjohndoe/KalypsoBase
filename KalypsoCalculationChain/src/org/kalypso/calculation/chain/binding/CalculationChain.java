package org.kalypso.calculation.chain.binding;

import java.util.Collections;

import org.kalypso.afgui.model.IModel;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.FeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

public class CalculationChain extends Feature_Impl implements ICalculationChain, IModel 
{
  private final FeatureBindingCollection<ICalculationChainMember> m_calculations = new FeatureBindingCollection<ICalculationChainMember>( this, ICalculationChainMember.class, QNAME_PROP_CALCULATIONS );

  private boolean m_isSorted = true;

  public CalculationChain( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  /**
   * @return List of calculations, ordered by ordinalNumber property
   */
  public FeatureBindingCollection<ICalculationChainMember> getCalculations( )
  {
    sort();
    return m_calculations;
  }

  public void addCalculation( final ICalculationChainMember member )
  {
    m_calculations.add( member );
    m_isSorted = false;
  }

  private void sort( )
  {
    if( m_isSorted )
      return;
    Collections.sort( m_calculations );
    m_isSorted = true;
  }
  
  @Override
  public Feature getFeature( )
  {
    return this;
  }
  
  @Override
  public String getGmlID( )
  {
    return this.getId();
  }

  @Override
  public String getVersion( )
  {
   return IModel.NO_VERSION;
  }
}
