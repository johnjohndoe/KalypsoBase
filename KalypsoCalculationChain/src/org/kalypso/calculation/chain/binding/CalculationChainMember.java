package org.kalypso.calculation.chain.binding;

import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.binding.FeatureWrapperCollection;
import org.kalypsodeegree_impl.gml.binding.commons.AbstractFeatureBinder;

public class CalculationChainMember extends AbstractFeatureBinder implements ICalculationChainMember {
    private final FeatureWrapperCollection<ICalculationChainMemberEntry> m_inputs;
    private final FeatureWrapperCollection<ICalculationChainMemberEntry> m_outputs;

    public CalculationChainMember(final Feature featureToBind) {
	super(featureToBind, ICalculationChainMember.QNAME);
	m_inputs = new FeatureWrapperCollection<ICalculationChainMemberEntry>(featureToBind, ICalculationChainMemberEntry.class, QNAME_PROP_INPUTS);
	m_outputs = new FeatureWrapperCollection<ICalculationChainMemberEntry>(featureToBind, ICalculationChainMemberEntry.class, QNAME_PROP_OUTPUTS);
    }

    public FeatureWrapperCollection<ICalculationChainMemberEntry> getInputs() {
	return m_inputs;
    }

    public FeatureWrapperCollection<ICalculationChainMemberEntry> getOutputs() {
	return m_outputs;
    }

    public void addInput(final ICalculationChainMemberEntry entry) {
	m_inputs.add(entry);
    }

    public void addOutput(final ICalculationChainMemberEntry entry) {
	m_outputs.add(entry);
    }

    public String getTypeID() {
	final Object property = getFeature().getProperty(QNAME_PROP_TYPE_ID);
	return property == null ? "" : property.toString();
    }

    public void setTypeID(final String value) {
	getFeature().setProperty(QNAME_PROP_TYPE_ID, value);
    }

    public int getOrdinalNumber() {
	final Integer property = (Integer) getFeature().getProperty(QNAME_PROP_ORDINAL_NUMBER);
	return property == null ? 0 : property.intValue();
    }

    public void setOrdinalNumber(final int value) {
	getFeature().setProperty(QNAME_PROP_ORDINAL_NUMBER, value);
    }

    public boolean getUseAntLauncher() {
	final Boolean property = (Boolean) getFeature().getProperty(QNAME_PROP_USE_ANT_LAUNCHER);
	return property == null ? false : property.booleanValue();
    }

    public void setUseAntLauncher(final boolean value) {
	getFeature().setProperty(QNAME_PROP_USE_ANT_LAUNCHER, value);
    }

    @Override
    public int compareTo(final ICalculationChainMember another) {
	return new Integer(getOrdinalNumber()).compareTo(another.getOrdinalNumber());
    }

}
