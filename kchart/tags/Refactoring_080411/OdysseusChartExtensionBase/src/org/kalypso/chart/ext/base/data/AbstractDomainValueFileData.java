package org.kalypso.chart.ext.base.data;

import java.net.URL;

public abstract class AbstractDomainValueFileData<T_domain, T_target> extends AbstractDomainValueData<T_domain, T_target>
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
