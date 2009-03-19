package org.kalypso.calculation.chain.binding;

import javax.xml.namespace.QName;

import org.kalypso.calculation.chain.ModelConnectorUrlCatalog;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.FeatureBindingCollection;

public interface ICalculationChain extends Feature
{
  final static QName QNAME = new QName( ModelConnectorUrlCatalog.NS_CCHAIN, "CalculationChain" );

  final static QName QNAME_PROP_CALCULATIONS = new QName( ModelConnectorUrlCatalog.NS_CCHAIN, "calculationMember" );

  FeatureBindingCollection<ICalculationChainMember> getCalculations( );

  void addCalculation( final ICalculationChainMember member );
}
