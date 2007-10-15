package org.kalypso.chart.ext.base.data;

import java.util.ArrayList;
import java.util.List;

import org.kalypso.chart.framework.model.data.IDataContainer;

public abstract class AbstractDomainIntervalValueData<T_domain, T_target> implements IDataContainer<T_domain, T_target>
{

  private boolean m_isOpen = false;

  private List<T_domain> m_domainIntervalStartValues = new ArrayList<T_domain>();

  private List<T_domain> m_domainIntervalEndValues = new ArrayList<T_domain>();

  private List<T_domain> m_domainValues = new ArrayList<T_domain>();

  private List<T_target> m_targetValues = new ArrayList<T_target>();

  public boolean isOpen( )
  {
    return m_isOpen;
  }

  public void open( )
  {
    // nur öffnen, wenn nicht schon offen
    if( !m_isOpen )
    {
      System.out.println( "opening Data " );
      m_isOpen = openData();
    }
  }

  public abstract boolean openData( );

  @SuppressWarnings("unchecked")
  public T_domain[] getDomainDataIntervalStart( )
  {
    return (T_domain[]) m_domainIntervalStartValues.toArray();
  }

  @SuppressWarnings("unchecked")
  public T_domain[] getDomainDataIntervalEnd( )
  {
    return (T_domain[]) m_domainIntervalEndValues.toArray();
  }

  @SuppressWarnings("unchecked")
  public T_domain[] getDomainValues( )
  {
    return (T_domain[]) m_domainValues.toArray();
  }

  @SuppressWarnings("unchecked")
  public T_target[] getTargetValues( )
  {
    return (T_target[]) m_targetValues.toArray();
  }

  protected void setDomainValues( List<T_domain> domainValues )
  {
    m_domainValues = domainValues;
  }

  protected void setTargetValues( List<T_target> targetValues )
  {
    m_targetValues = targetValues;
  }

  protected void setDomainIntervalEndValues( List<T_domain> domainIntervalEndValues )
  {
    m_domainIntervalStartValues = domainIntervalEndValues;
  }

  protected void setDomainIntervalStartValues( List<T_domain> domainIntervalStartValues )
  {
    m_domainIntervalEndValues = domainIntervalStartValues;
  }

  public void close( )
  {
    m_domainIntervalStartValues = null;
    m_domainIntervalEndValues = null;
    m_targetValues = null;
    m_isOpen = false;
  }

}
