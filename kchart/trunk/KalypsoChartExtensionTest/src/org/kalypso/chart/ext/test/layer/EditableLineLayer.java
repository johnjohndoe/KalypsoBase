package org.kalypso.chart.ext.test.layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.ext.test.data.EditableTestDataContainer;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.layer.AbstractEditableChartLayer;
import org.kalypso.chart.framework.model.layer.EditInfo;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.kalypso.chart.framework.model.styles.IStyleConstants.SE_TYPE;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

/**
 * @author alibu
 */
public class EditableLineLayer extends AbstractEditableChartLayer<Number, Number>
{

  private List<Number> m_domainData;

  private List<Number> m_targetData;

  public EditableLineLayer( EditableTestDataContainer data, IAxis<Number> domainAxis, IAxis<Number> targetAxis )
  {
    super( domainAxis, targetAxis );
    setDataContainer( data );

    if( data != null )
    {
      data.open();
      m_domainData = data.getDomainValues();
      m_targetData = data.getTargetValues();
    }
    else
    {
      Logger.logWarning( Logger.TOPIC_LOG_GENERAL, "Layer " + getId() + " has not yet been opened" );
    }
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#drawIcon(org.eclipse.swt.graphics.Image, int, int)
   */
  public void drawIcon( Image img )
  {
    final Rectangle bounds = img.getBounds();
    final int height = bounds.height;
    final int width = bounds.width;
    final GC gc = new GC( img );
    final GCWrapper gcw = new GCWrapper( gc );

    final ArrayList<Point> path = new ArrayList<Point>();

    path.add( new Point( 0, height / 2 ) );
    path.add( new Point( width / 5, height / 2 ) );
    path.add( new Point( width / 5 * 2, height / 4 ) );
    path.add( new Point( width / 5 * 3, height / 4 * 3 ) );
    path.add( new Point( width / 5 * 4, height / 2 ) );
    path.add( new Point( width, height / 2 ) );

    final IStyledElement element = getStyle().getElement( SE_TYPE.LINE, 1 );

    element.setPath( path );
    element.paint( gcw );

    gcw.dispose();
    gc.dispose();
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( GCWrapper gc )
  {
    final EditableTestDataContainer dataContainer = getDataContainer();

    final ArrayList<Point> path = new ArrayList<Point>();
    final IStyledElement line = getStyle().getElement( SE_TYPE.LINE, 1 );
    final IStyledElement point = getStyle().getElement( SE_TYPE.POINT, 1 );
    for( int i = 0; i < m_domainData.size(); i++ )
    {
      final Number domVal = m_domainData.get( i );
      final Number targetVal = m_targetData.get( i );
      final int domScreen = getDomainAxis().logicalToScreen( domVal );
      final int valScreen = getTargetAxis().logicalToScreen( targetVal );
      path.add( new Point( domScreen, valScreen ) );
    }
    line.setPath( path );
    line.paint( gc );

    point.setPath( path );
    point.paint( gc );
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IEditableChartLayer#getEditInfo(org.eclipse.swt.graphics.Point)
   */
  public EditInfo getEditInfo( Point p )
  {
    // Umrechnen von screen nach logisch
    // TODO: im Moment wird von domain = horizontal und target = vertkal ausgegangen
    int tolerance = 2;
    IAxis<Number> domainAxis = getDomainAxis();
    IAxis<Number> targetAxis = getTargetAxis();
    Double domainValMin = domainAxis.screenToLogical( p.x - tolerance ).doubleValue();
    Double domainValMax = domainAxis.screenToLogical( p.x + tolerance ).doubleValue();
    Double targetValMin = targetAxis.screenToLogical( p.y + tolerance ).doubleValue();
    Double targetValMax = targetAxis.screenToLogical( p.y - tolerance ).doubleValue();

    // herausfinden, ob der Punkt IN DER NAEHE eines Datenpunktes liegt
    for( int i = 0; i < m_domainData.size(); i++ )
    {
      Double domainVal = m_domainData.get( i ).doubleValue();
      if( domainVal >= domainValMin && domainVal <= domainValMax )
      {
        Double targetVal = m_targetData.get( i ).doubleValue();
        if( targetVal >= targetValMin && targetVal <= targetValMax )
        {
          Object editData = null;
          /*
           * Punkt des "Handles" ausrechnen - evtl. kann man hier auch den eingangspunkt verwenden; (wahrscheinlich aber
           * nicht, weil sich sonst der handle verschiebt)
           */
          Point dataPos = new Point( domainAxis.logicalToScreen( domainVal ), targetAxis.logicalToScreen( targetVal ) );
          int pointWidth = 10;
          Rectangle editShape = new Rectangle( dataPos.x - pointWidth, dataPos.y - pointWidth, pointWidth * 2 + 1, pointWidth * 2 + 1 );
          String editText = domainAxis.getLabel() + ":\t" + domainVal + "\n" + targetAxis.getLabel() + ":\t" + targetVal;

          EditInfo info = new EditInfo( this, editShape, editData, editText );

          // falls der Punkt in den Daten vorhanden ist, dann eine Info zurückgeben
          return info;
        }
      }
    }

    return null;

  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IEditableChartLayer#alwaysAllowsEditing()
   */
  public boolean alwaysAllowsEditing( )
  {
    // TODO Auto-generated method stub
    return true;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IEditableChartLayer#edit(org.eclipse.swt.graphics.Point,
   *      java.lang.Object)
   */
  public void edit( Point point, Object data )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IEditableChartLayer#setActivePoint(java.lang.Object)
   */
  public void setActivePoint( Object data )
  {

  }

  @Override
  public EditableTestDataContainer getDataContainer( )
  {
    return (EditableTestDataContainer) super.getDataContainer();
  }

}
