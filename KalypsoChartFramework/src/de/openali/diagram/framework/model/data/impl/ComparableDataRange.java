package de.openali.diagram.framework.model.data.impl;

import java.util.List;

import org.apache.commons.collections.comparators.ComparableComparator;

import de.openali.diagram.framework.model.data.IDataRange;

public class ComparableDataRange<T extends Comparable> implements IDataRange<T>
{

	private List<T> m_items;
	private T m_max;
	private T m_min;
	private ComparableComparator m_comp=new ComparableComparator();

	public ComparableDataRange(List<T> items)
	{
		m_items=items;
		findMinMax();
	}
	
	public T getMax()
	{
		return m_max;
	}

	public T getMin()
	{
		return m_min;
	}
	
	private void findMinMax()
	{
		for (int i=0; i<m_items.size();i++)
		{
			if (i==0)
			{
				m_min=m_items.get(0);
				m_max=m_items.get(0);
			}
			else
			{
				T compItem=m_items.get(i);
				if (m_comp.compare(compItem, m_min) < 0)
				{
					m_min=compItem;
				}
				if (m_comp.compare(compItem, m_max) > 0)
				{
					m_max=compItem;
				}
			}
		}
	}

}
