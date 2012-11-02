package de.openali.odysseus.chart.ext.base.data;

import java.util.ArrayList;
import java.util.List;

import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.data.ITabularDataContainer;

public abstract class AbstractDomainValueData<T_domain, T_target> implements ITabularDataContainer<T_domain, T_target>
{
  private boolean m_isOpen = false;

  private boolean m_isLoading = false;

  private List<T_domain> m_domainValues = new ArrayList<>();

  private List<T_target> m_targetValues = new ArrayList<>();

  @Override
  public boolean isOpen( )
  {
    return m_isOpen;
  }

  public boolean isLoading( )
  {
    return m_isLoading;
  }

  public void setLoading( final boolean isLoading )
  {
    m_isLoading = isLoading;
  }

  @Override
  public synchronized void open( )
  {
    // nur �ffnen, wenn nicht schon offen
    if( !m_isOpen )
    {
      if( m_isLoading )
      {
        // nix machen -> wird gerade ge�ffnet
      }
      else
      {
        Logger.logInfo( Logger.TOPIC_LOG_DATA, this.getClass() + ": opening Data" ); //$NON-NLS-1$
        m_isOpen = openData();
      }
    }
  }

  public abstract boolean openData( );

  @Override
  public void close( )
  {
    m_domainValues = null;
    m_targetValues = null;
    m_isOpen = false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T_domain[] getDomainValues( )
  {
    open();
    if( m_domainValues == null )
    {
      return (T_domain[]) new Object[] {};
    }
    final T_domain[] toArray = (T_domain[]) m_domainValues.toArray();
    return toArray;
  }

  protected void setDomainValues( final List<T_domain> values )
  {
    m_domainValues = values;
  }

  protected void setTargetValues( final List<T_target> values )
  {
    m_targetValues = values;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T_target[] getTargetValues( )
  {

    open();
    if( m_targetValues == null )
    {
      return (T_target[]) new Object[] {};
    }
    // (T_target[]) m_targetValues.toArray()
    final Object[] toArray = new Object[m_targetValues.size()];

    for( int i = 0; i < m_targetValues.size(); i++ )
    {
      toArray[i] = m_targetValues.get( i );
    }

    return (T_target[]) toArray;
  }
}
