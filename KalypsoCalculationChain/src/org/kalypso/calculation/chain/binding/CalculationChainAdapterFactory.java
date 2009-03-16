/**
 *
 */
package org.kalypso.calculation.chain.binding;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.IAdapterFactory;
import org.kalypsodeegree.model.feature.Feature;

public class CalculationChainAdapterFactory implements IAdapterFactory {
    interface AdapterConstructor {
	public Object constructAdapter(final Feature feature, final Class<?> cls) throws IllegalArgumentException;
    }

    private final Map<Class<?>, AdapterConstructor> m_constructors = createConstructorMap();

    /**
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
     *      java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(final Object adaptableObject, final Class adapterType) {
	final AdapterConstructor ctor = m_constructors.get(adapterType);
	if (ctor != null) {
	    return ctor.constructAdapter((Feature) adaptableObject, adapterType);
	}
	return null;
    }

    /**
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    public Class<?>[] getAdapterList() {
	return m_constructors.keySet().toArray(new Class[m_constructors.size()]);
    }

    private final Map<Class<?>, AdapterConstructor> createConstructorMap() {
	final Map<Class<?>, AdapterConstructor> cMap = new Hashtable<Class<?>, AdapterConstructor>();

	final AdapterConstructor cTor = new AdapterConstructor() {
	    public Object constructAdapter(final Feature feature, final Class<?> cls) throws IllegalArgumentException {
		final QName featureQName = feature.getFeatureType().getQName();
		if (featureQName.equals(ICalculationChain.QNAME))
		    return new CalculationChain(feature);
		else if (featureQName.equals(ICalculationChainMember.QNAME))
		    return new CalculationChainMember(feature);
		else if (featureQName.equals(ICalculationChainMemberEntry.QNAME))
		    return new CalculationChainMemberEntry(feature);
		else
		    return null;
	    }
	};
	cMap.put(ICalculationChain.class, cTor);
	cMap.put(ICalculationChainMember.class, cTor);
	cMap.put(ICalculationChainMemberEntry.class, cTor);

	return Collections.unmodifiableMap(cMap);
    }

}
