package org.kalypso.chart.framework.util.math;

import org.kalypso.contribs.java.util.DoubleComparator;


/**
 * 
 * @author burtscher1
 *
 *	Number class which also implements Compatable 
 *
 */
public class ComparableNumber extends Number implements Comparable<Number> {

	
	private final Number m_number;
	private final double m_delta;
	private final DoubleComparator m_comparator;

	public ComparableNumber(Number number)
	{
		this(number, 0.01);
	}
	
	public ComparableNumber(Number number, double delta)
	{
		m_number=number;
		m_delta=delta;
		m_comparator=new DoubleComparator(m_delta);
	}

	public int compareTo(Number number) {
		return m_comparator.compare(m_number, number);
	}

	@Override
	public double doubleValue() {
		return m_number.doubleValue();
	}

	@Override
	public float floatValue() {
		return m_number.floatValue();
	}

	@Override
	public int intValue() {
		return m_number.intValue();
	}

	@Override
	public long longValue() {
		return m_number.longValue();
	}

}
