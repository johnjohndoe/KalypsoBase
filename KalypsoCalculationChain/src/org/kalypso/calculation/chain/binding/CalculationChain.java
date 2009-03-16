package org.kalypso.calculation.chain.binding;

import java.util.Collections;

import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.binding.FeatureWrapperCollection;
import org.kalypsodeegree_impl.gml.binding.commons.AbstractFeatureBinder;

public class CalculationChain extends AbstractFeatureBinder implements ICalculationChain {
    private final FeatureWrapperCollection<ICalculationChainMember> m_calculations;
    private boolean m_isSorted = true;

    public CalculationChain(final Feature featureToBind) {
	super(featureToBind, ICalculationChain.QNAME);
	m_calculations = new FeatureWrapperCollection<ICalculationChainMember>(featureToBind, ICalculationChainMember.class, QNAME_PROP_CALCULATIONS);
    }

    /**
     * @return List of calculations, ordered by ordinalNumber property
     */
    public FeatureWrapperCollection<ICalculationChainMember> getCalculations() {
	sort();
	return m_calculations;
    }

    public void addCalculation(final ICalculationChainMember member) {
	m_calculations.add(member);
	m_isSorted = false;
    }

    private void sort() {
	if (m_isSorted)
	    return;
	Collections.sort(m_calculations);
	m_isSorted = true;
    }
}
