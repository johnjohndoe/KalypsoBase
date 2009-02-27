package de.openali.diagram.framework.model.legend;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.ImageData;

public class LegendItem implements ILegendItem {

	List<ILegendItem> m_children=new ArrayList<ILegendItem>();
	private ILegendItem m_parent=null;
	private String m_label;
	private ImageData m_imageData;

	public LegendItem(ILegendItem parent, String label, ImageData imageData)
	{
		m_label=label;
		m_imageData=imageData;
		m_parent=parent;
	}
	
	
	/* (non-Javadoc)
	 * @see de.openali.diagram.framework.model.legend.ILegendItem2#getChildren()
	 */
	public ILegendItem[] getChildren()
	{
		return (ILegendItem[]) m_children.toArray();
	}
	
	/* (non-Javadoc)
	 * @see de.openali.diagram.framework.model.legend.ILegendItem2#addChild(de.openali.diagram.framework.model.legend.LegendItem)
	 */
	public void addChild(ILegendItem l)
	{
		m_children.add(l);
	}
	
	/* (non-Javadoc)
	 * @see de.openali.diagram.framework.model.legend.ILegendItem2#getParent()
	 */
	public ILegendItem getParent()
	{
		return m_parent;
	}
	
	/* (non-Javadoc)
	 * @see de.openali.diagram.framework.model.legend.ILegendItem2#getLabel()
	 */
	public String getLabel()
	{
		return m_label;
	}
	
	/* (non-Javadoc)
	 * @see de.openali.diagram.framework.model.legend.ILegendItem2#getImage()
	 */
	public ImageData getImage()
	{
		return m_imageData;
	}


	public void setParent(ILegendItem parent) {
		m_parent=parent;
	}
	
	
	
}
