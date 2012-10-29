package org.kalypso.contribs.eclipse.jface.viewers;

public interface ITooltipProvider
{
  /**
   * Provides a tooltip for the given element.
   * 
   * @param columnIndex
   *          the zero-based index of the column in which the tooltip appears
   * @return the tooltip for the element, or <code>null</code> to use no tooltip
   */
  public String getTooltip( final Object element );
}
