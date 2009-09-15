package org.kalypso.calculation.chain.binding;

import javax.xml.namespace.QName;

import org.kalypso.calculation.chain.ModelConnectorUrlCatalog;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.FeatureBindingCollection;

public interface ICalculationChain extends Feature
{
  final static QName QNAME = new QName( ModelConnectorUrlCatalog.NS_CCHAIN, "CalculationChain" ); //$NON-NLS-1$

  final static QName QNAME_PROP_CALCULATIONS = new QName( ModelConnectorUrlCatalog.NS_CCHAIN, "calculationMember" ); //$NON-NLS-1$

  FeatureBindingCollection<ICalculationChainMember> getCalculations( );

  void addCalculation( final ICalculationChainMember member );
}
