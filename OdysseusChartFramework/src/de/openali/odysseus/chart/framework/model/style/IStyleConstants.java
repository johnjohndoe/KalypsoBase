package de.openali.odysseus.chart.framework.model.style;

import org.eclipse.swt.SWT;

public interface IStyleConstants
{

  public enum LINECAP implements ISWTable
  {
    FLAT(SWT.CAP_FLAT),
    ROUND(SWT.CAP_ROUND),
    SQUARE(SWT.CAP_SQUARE);
    private final int m_swtValue;

    private LINECAP( final int swtValue )
    {
      m_swtValue = swtValue;
    }

    @Override
    public int toSWT( )
    {
      return m_swtValue;
    }
  }

  public enum LINEJOIN implements ISWTable
  {
    MITER(SWT.JOIN_MITER),
    ROUND(SWT.JOIN_ROUND),
    BEVEL(SWT.JOIN_BEVEL);

    private final int m_swtValue;

    private LINEJOIN( final int swtValue )
    {
      m_swtValue = swtValue;
    }

    @Override
    public int toSWT( )
    {
      return m_swtValue;
    }
  }

  public enum FONTSTYLE implements ISWTable
  {
    NORMAL(SWT.NORMAL),
    ITALIC(SWT.ITALIC);

    private final int m_swtValue;

    private FONTSTYLE( final int swtValue )
    {
      m_swtValue = swtValue;
    }

    @Override
    public int toSWT( )
    {
      return m_swtValue;
    }
  }

  public enum FONTWEIGHT implements ISWTable
  {
    BOLD(SWT.BOLD),
    NORMAL(SWT.NORMAL);

    private final int m_swtValue;

    private FONTWEIGHT( final int swtValue )
    {
      m_swtValue = swtValue;
    }

    @Override
    public int toSWT( )
    {
      return m_swtValue;
    }
  }

}
