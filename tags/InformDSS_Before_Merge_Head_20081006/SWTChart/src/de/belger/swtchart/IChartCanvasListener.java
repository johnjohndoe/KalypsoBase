package de.belger.swtchart;

/**
 * Changes in {@link de.belger.swtchart.ChartCanvas} are propagated to this listeners.
 * 
 * @author gernot
 * @see de.belger.swtchart.ChartCanvas
 */
public interface IChartCanvasListener
{
  /** A layer was added/removed */
  void onLayersChanged( );
}
