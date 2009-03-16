package org.kalypso.calculation.chain.binding;

import javax.xml.namespace.QName;

import org.kalypso.calculation.chain.ModelConnectorUrlCatalog;
import org.kalypsodeegree.model.feature.binding.FeatureWrapperCollection;
import org.kalypsodeegree.model.feature.binding.IFeatureWrapper2;

public interface ICalculationChain extends IFeatureWrapper2
{
  public final static QName QNAME = new QName( ModelConnectorUrlCatalog.NS_CCHAIN, "CalculationChain" );

  public final static QName QNAME_PROP_CALCULATIONS = new QName( ModelConnectorUrlCatalog.NS_CCHAIN, "calculationMember" );

  public FeatureWrapperCollection<ICalculationChainMember> getCalculations( );

  public void addCalculation( final ICalculationChainMember member );
}
