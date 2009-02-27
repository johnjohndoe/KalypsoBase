package org.kalypso.chart.ext.base.data;

import java.util.ArrayList;
import java.util.List;

import org.kalypso.chart.framework.model.data.ITabularDataContainer;

public abstract class AbstractDomainValueData<T_domain, T_target> implements ITabularDataContainer<T_domain, T_target>
{

  private boolean m_isOpen = false;

  private boolean m_isLoading = false;

  private List<T_domain> m_domainValues = new ArrayList<T_domain>();

  private List<T_target> m_targetValues = new ArrayList<T_target>();

  public boolean isOpen( )
  {
    return m_isOpen;
  }

  public boolean isLoading( )
  {
    return m_isLoading;
  }

  public void setLoading( boolean isLoading )
  {
    m_isLoading = isLoading;
  }

  public synchronized void open( )
  {
    // nur öffnen, wenn nicht schon offen
    if( !m_isOpen )
    {
      if( m_isLoading )
      {
        // nix machen -> wird gerade geöffnet
      }
      else
      {
        System.out.println( "opening Data" );
        m_isOpen = openData();
      }
    }
  }

  public abstract boolean openData( );

  public void close( )
  {
    m_domainValues = null;
    m_targetValues = null;
    m_isOpen = false;
  }

  @SuppressWarnings("unchecked")
  public T_domain[] getDomainValues( )
  {
    open();
    final T_domain[] toArray = (T_domain[]) m_domainValues.toArray();
    return toArray;
  }

  protected void setDomainValues( List<T_domain> values )
  {
    m_domainValues = values;
  }

  protected void setTargetValues( List<T_target> values )
  {
    m_targetValues = values;
  }

  @SuppressWarnings("unchecked")
  public T_target[] getTargetValues( )
  {
    open();
    final T_target[] toArray = (T_target[]) m_targetValues.toArray();
    return toArray;
  }
}
