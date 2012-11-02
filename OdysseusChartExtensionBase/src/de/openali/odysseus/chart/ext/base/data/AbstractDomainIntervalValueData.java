package de.openali.odysseus.chart.ext.base.data;

import java.util.ArrayList;
import java.util.List;

import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.data.IDataContainer;

public abstract class AbstractDomainIntervalValueData<T_domain, T_target> implements IDataContainer<T_domain, T_target>
{
  private boolean m_isOpen = false;

  private List<T_domain> m_domainIntervalStartValues = new ArrayList<>();

  private List<T_domain> m_domainIntervalEndValues = new ArrayList<>();

  private List<T_domain> m_domainValues = new ArrayList<>();

  private List<T_target> m_targetValues = new ArrayList<>();

  @Override
  public boolean isOpen( )
  {
    return m_isOpen;
  }

  @Override
  public void open( )
  {
    // nur öffnen, wenn nicht schon offen
    if( !m_isOpen )
    {
      Logger.logInfo( Logger.TOPIC_LOG_DATA, this.getClass() + ": opening Data" ); //$NON-NLS-1$
      m_isOpen = openData();
    }
  }

  public abstract boolean openData( );

  public T_domain[] getDomainDataIntervalStart( )
  {
    return (T_domain[]) m_domainIntervalStartValues.toArray();
  }

  public T_domain[] getDomainDataIntervalEnd( )
  {
    return (T_domain[]) m_domainIntervalEndValues.toArray();
  }

  public T_domain[] getDomainValues( )
  {
    return (T_domain[]) m_domainValues.toArray();
  }

  public T_target[] getTargetValues( )
  {
    return (T_target[]) m_targetValues.toArray();
  }

  protected void setDomainValues( final List<T_domain> domainValues )
  {
    m_domainValues = domainValues;
  }

  protected void setTargetValues( final List<T_target> targetValues )
  {
    m_targetValues = targetValues;
  }

  protected void setDomainIntervalEndValues( final List<T_domain> domainIntervalEndValues )
  {
    m_domainIntervalEndValues = domainIntervalEndValues;
  }

  protected void setDomainIntervalStartValues( final List<T_domain> domainIntervalStartValues )
  {
    m_domainIntervalStartValues = domainIntervalStartValues;
  }

  @Override
  public void close( )
  {
    m_domainIntervalStartValues = null;
    m_domainIntervalEndValues = null;
    m_targetValues = null;
    m_isOpen = false;
  }

}
