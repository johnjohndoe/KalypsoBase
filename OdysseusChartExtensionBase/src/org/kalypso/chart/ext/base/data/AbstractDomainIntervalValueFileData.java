package org.kalypso.chart.ext.base.data;

import java.io.File;

public abstract class AbstractDomainIntervalValueFileData<T_domain, T_target> extends AbstractDomainIntervalValueData<T_domain, T_target>
{

  private File m_file;

  public void setInputFile( File f )
  {
    m_file = f;
  }

  public File getInputFile( )
  {
    return m_file;
  }

}
