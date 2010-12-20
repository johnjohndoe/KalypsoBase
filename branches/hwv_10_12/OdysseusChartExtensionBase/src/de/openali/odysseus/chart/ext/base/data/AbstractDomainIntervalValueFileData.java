package de.openali.odysseus.chart.ext.base.data;

import java.net.URL;

public abstract class AbstractDomainIntervalValueFileData<T_domain, T_target> extends AbstractDomainIntervalValueData<T_domain, T_target>
{

  private URL m_url;

  public void setInputURL( URL url )
  {
    m_url = url;
  }

  public URL getInputURL( )
  {
    return m_url;
  }

}
