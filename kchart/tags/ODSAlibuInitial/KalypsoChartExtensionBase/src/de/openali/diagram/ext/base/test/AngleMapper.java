package de.openali.diagram.ext.base.test;

import de.openali.diagram.factory.configuration.exception.MapperProviderException;
import de.openali.diagram.factory.configuration.xsd.MapperType;
import de.openali.diagram.factory.provider.IMapperProvider;
import de.openali.diagram.framework.model.data.IDataRange;
import de.openali.diagram.framework.model.mapper.IMapper;
import de.openali.diagram.framework.model.mapper.registry.IMapperRegistry;

public class AngleMapper implements IMapper<Double, Integer>, IMapperProvider {

	private Double m_from=0.0;
	private Double m_to=100.0;
	private String m_id;
	private IMapperRegistry m_registry;

	public void autorange(IDataRange[] ranges) {
		// TODO Auto-generated method stub

	}
	
	public Class<?> getDataClass() {
		return Double.class;
	}

	public IMapper getMapper() throws MapperProviderException {
		return this;
	}

	public void init(MapperType at) {
		m_id=at.getId();
	}
	
	public Double getFrom() {
		return m_from;
	}

	public String getIdentifier() {
		return m_id;
	}

	public IMapperRegistry getRegistry() {
		// TODO Auto-generated method stub
		return m_registry;
	}

	public Double getTo() {
		// TODO Auto-generated method stub
		return m_to;
	}

	public int logicalToScreen(Double value) {
		// TODO Auto-generated method stub
		return (int)  ( ( (m_from.doubleValue()-m_to.doubleValue()) / 360.00) * (value.doubleValue() - m_from.doubleValue()) );
	}

	public void setFrom(Double min) {
		m_from=min;

	}

	public void setRegistry(IMapperRegistry mapperRegistry) {
		m_registry=mapperRegistry;
	}

	public void setTo(Double max) {
		m_to=max;
	}

}
