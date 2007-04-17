package de.openali.diagram.ext.base.test;

import java.net.URL;

import org.eclipse.swt.graphics.Image;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

import de.openali.diagram.ext.base.layer.AbstractChartLayer;
import de.openali.diagram.factory.configuration.exception.LayerProviderException;
import de.openali.diagram.factory.configuration.xsd.LayerType;
import de.openali.diagram.factory.configuration.xsd.MapperRefType;
import de.openali.diagram.factory.configuration.xsd.LayerType.Mapper;
import de.openali.diagram.factory.provider.ILayerProvider;
import de.openali.diagram.framework.model.IDiagramModel;
import de.openali.diagram.framework.model.data.IDataRange;
import de.openali.diagram.framework.model.layer.IChartLayer;
import de.openali.diagram.framework.model.mapper.IMapper;
import de.openali.diagram.framework.model.mapper.registry.IMapperRegistry;

public class CircleLayer extends AbstractChartLayer implements ILayerProvider {

	private IDiagramModel m_model;
	private LayerType m_lt;
	private IMapper m_angleMapper;
	@SuppressWarnings("unused")
	private IMapper m_lengthMapper;

	public IChartLayer getLayer(URL context) throws LayerProviderException {
		
		return this;
	}

	public void init(IDiagramModel model, LayerType lt) {
		m_model=model;
		m_lt=lt;
		IMapperRegistry mr=m_model.getAxisRegistry();
		Mapper mapper = m_lt.getMapper();
		MapperRefType[] mapperRefArray = mapper.getMapperRefArray();
		for (MapperRefType mapperRef : mapperRefArray)
		{
			String mapperRole=mapperRef.getRole();
			IMapper m=mr.getMapper(mapperRef.getRef());
			
			if (mapperRole.equals("angle"))
				m_angleMapper=m;
			else if (mapperRole.equals("length"))
				m_lengthMapper=m;
		}
	}

	public String getId()
	{
		return "circle";
	}
	
	public void drawIcon(Image img, int width, int height) {
		
	}

	public IDataRange getDomainRange() {
		return null;
	}

	public IDataRange getTargetRange() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public void paint(GCWrapper gc) {
		gc.setLineWidth(10);
		int center_x=m_domainAxis.logicalToScreen(0.5);
		int center_y=m_targetAxis.logicalToScreen(0.5);
		int allAngles=0;
		for (int i=0; i<=360; i+=10)
		{
				int angle=m_angleMapper.logicalToScreen(40);
				gc.drawArc(center_x, center_y, 200, 200, allAngles, angle);
				allAngles+=angle;
		}
	}

}
