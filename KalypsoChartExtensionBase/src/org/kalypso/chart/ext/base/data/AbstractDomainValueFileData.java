package org.kalypso.chart.ext.base.data;

import java.io.File;

public abstract class AbstractDomainValueFileData<T_domain, T_target> extends AbstractDomainValueData<T_domain, T_target>
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
