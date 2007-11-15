package de.belger.swtchart.axis;

import de.belger.swtchart.util.LogicalRange;
import de.belger.swtchart.util.SwitchDelegate;

/**
 * Die AxisRange managed die Umrechnung von Screen in logische Koordinaten innerhalb einer Achse.
 * 
 * @author Gernot Belger
 */
public class AxisRange
{
  private transient double m_ratio;

  private int m_screenFrom;

  private int m_screenTo;

  private double m_logicalFrom;

  private double m_logicalTo;

  private final SwitchDelegate m_switchDelegate;

  private final boolean m_inverted;

  private final double m_screenRatio;

  private String m_label;

  private final int m_gapSpace;

  public AxisRange( final String label, final SwitchDelegate coordSwitcher )
  {
    this( label, coordSwitcher, false );
  }

  public AxisRange( final String label, final SwitchDelegate coordSwitcher, boolean inverted )
  {
    this( label, coordSwitcher, inverted, 0, 1.0 );
  }

  public AxisRange( final String label, final SwitchDelegate coordSwitcher, final boolean inverted, final int gapSpace, double screenRatio )
  {
    m_label = label;
    m_switchDelegate = coordSwitcher;
    m_inverted = inverted;
    m_screenRatio = screenRatio;
    m_gapSpace = gapSpace;
  }

  public boolean isInverted( )
  {
    return m_inverted;
  }

  public SwitchDelegate getSwitch( )
  {
    return m_switchDelegate;
  }

  public double logical2screen( final double logical )
  {
    return (logical - m_logicalFrom) * m_ratio + m_screenFrom;
  }

  public double screen2Logical( final int x )
  {
    return m_logicalFrom + (x - m_screenFrom) / m_ratio;
  }

  public void setLogicalRange( final LogicalRange value )
  {
    setLogicalRange( value, false );
  }

  public void setLogicalRange( final LogicalRange value, final Boolean keepRatio )
  {
    if( keepRatio )
    {
      m_ratio = (m_screenTo - m_screenFrom) / (m_logicalTo - m_logicalFrom);
      final double deltaX = (m_screenTo - m_screenFrom) * m_ratio / 2;
      m_logicalFrom = -deltaX;
      m_logicalTo = +deltaX;
    }

    m_logicalFrom = value.getFrom();
    m_logicalTo = value.getTo();

    recalcRatio();
  }

  public void setScreenRange( final int screenFrom, final int screenTo )
  {

    m_screenFrom = m_inverted ? screenTo - m_gapSpace : screenFrom + m_gapSpace;
    final int screenlength = (int) ((screenTo - screenFrom - m_gapSpace * 2) * m_screenRatio) * (m_inverted ? -1 : 1);
    m_screenTo = m_screenFrom + screenlength;
    recalcRatio();
  }

  private void recalcRatio( )
  {
    try
    {
      // Ratio is often used, so calc it once
      m_ratio = (m_screenTo - m_screenFrom) / (m_logicalTo - m_logicalFrom);

    }
    catch( ArithmeticException e )
    {
      m_ratio = 1;
    }
  }

  public int getScreenFrom( )
  {
    return m_screenFrom;
  }

  public int getScreenTo( )
  {
    return m_screenTo;
  }

  public double getLogicalFrom( )
  {
    return m_logicalFrom;
  }

  public double getLogicalTo( )
  {
    return m_logicalTo;
  }

  public double screenLength2Logical( final int screenInterval )
  {
    return screenInterval / m_ratio;
  }

  public String getLable( )
  {
    return m_label;
  }

  /**
   * @return Returns true, if given position is beetween {@link #m_screenFrom} and {@link #m_screenTo}
   */
  public boolean isInScreen( final int pos )
  {
    return (m_screenFrom <= pos && pos <= m_screenTo) || (m_screenTo <= pos && pos <= m_screenFrom);
  }

  public int getGapSpace( )
  {
    return m_gapSpace;
  }

  public double getRatio( )
  {
    return m_ratio;
  }

  public void setLabel( String label )
  {
    m_label = label;
  }
}