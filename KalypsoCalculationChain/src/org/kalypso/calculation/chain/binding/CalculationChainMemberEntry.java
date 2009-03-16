package org.kalypso.calculation.chain.binding;

import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.gml.binding.commons.AbstractFeatureBinder;

public class CalculationChainMemberEntry extends AbstractFeatureBinder implements ICalculationChainMemberEntry {

    public CalculationChainMemberEntry(final Feature featureToBind) {
	super(featureToBind, ICalculationChainMemberEntry.QNAME);
    }

    @Override
    public String getKey() {
	final Object property = getFeature().getProperty(QNAME_PROP_KEY);
	return property == null ? null : property.toString();
    }

    @Override
    public String getValue() {
	final Object property = getFeature().getProperty(QNAME_PROP_VALUE);
	return property == null ? null : property.toString();
    }

    @Override
    public boolean isOptional() {
	final Boolean property = (Boolean) getFeature().getProperty(QNAME_PROP_IS_OPTIONAL);
	return property == null ? false : property.booleanValue();
    }

    @Override
    public boolean isRelativeToCalculationCase() {
	final Boolean property = (Boolean) getFeature().getProperty(QNAME_PROP_IS_RELATIVE_TO_CALC_CASE);
	return property == null ? true : property.booleanValue();
    }

    @Override
    public void setKey(final String value) {
	getFeature().setProperty(QNAME_PROP_KEY, value);
    }

    @Override
    public void setValue(final String value) {
	getFeature().setProperty(QNAME_PROP_VALUE, value);
    }

    @Override
    public void setOptional(final boolean value) {
	getFeature().setProperty(QNAME_PROP_IS_OPTIONAL, value);
    }

    @Override
    public void setRelativeToCalculationCase(final boolean value) {
	getFeature().setProperty(QNAME_PROP_IS_RELATIVE_TO_CALC_CASE, value);
    }

}
