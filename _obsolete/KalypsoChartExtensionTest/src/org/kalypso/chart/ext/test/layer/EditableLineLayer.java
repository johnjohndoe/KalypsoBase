package org.kalypso.chart.ext.test.layer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.ext.base.layer.AbstractEditableChartLayer;
import org.kalypso.chart.ext.test.data.EditableTestDataContainer;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.layer.EditInfo;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.kalypso.chart.framework.model.styles.IStyleConstants.SE_TYPE;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

/**
 * @author alibu
 */
public class EditableLineLayer<T_domain, T_target> extends AbstractEditableChartLayer<T_domain, T_target>
{

  private List<T_domain> m_domainData;

  private List<T_target> m_targetData;

  public EditableLineLayer( EditableTestDataContainer data, IAxis domainAxis, IAxis targetAxis )
  {
    super( domainAxis, targetAxis );
    setDataContainer( data );

    if( data != null )
    {
      data.open();
      m_domainData = (List<T_domain>) data.getDomainValues();
      m_targetData = (List<T_target>) data.getTargetValues();
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

    final ArrayList<Point> path = new ArrayList<Point>();
    final IStyledElement line = getStyle().getElement( SE_TYPE.LINE, 1 );
    final IStyledElement point = getStyle().getElement( SE_TYPE.POINT, 1 );
    for( int i = 0; i < m_domainData.size(); i++ )
    {
      IDataRange<T_domain> domRange = getDomainAxis().getLogicalRange();

      T_domain min = domRange.getMin();
      T_domain max = domRange.getMax();

      Comparator comp = getDomainAxis().getDataOperator().getComparator();

      final T_domain domVal = m_domainData.get( i );
      boolean setPoint = false;
      if( comp.compare( domVal, min ) >= 0 && comp.compare( domVal, max ) <= 0 )
      {
        setPoint = true;
      }
      else
      {
        // kleiner als min: Nachfolger muss >= min sein
        if( comp.compare( domVal, min ) <= 0 && i < m_domainData.size() - 1 )
        {
          T_domain next = m_domainData.get( i + 1 );
          if( comp.compare( next, min ) >= 0 )
          {
            setPoint = true;
          }
        }
        // größer als max: Vorgänger muss <= max sein
        else if( comp.compare( domVal, max ) >= 0 && i > 0 )
        {
          T_domain prev = m_domainData.get( i - 1 );
          if( comp.compare( prev, max ) <= 0 )
          {
            setPoint = true;
          }
          else
          {
            // jetzt kann man aufhören
            break;
          }
        }
      }

      if( setPoint )
      {
        final T_target targetVal = m_targetData.get( i );
        final int domScreen = getDomainAxis().logicalToScreen( domVal );
        final int valScreen = getTargetAxis().logicalToScreen( targetVal );
        Point p;
        if( getDomainAxis().getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) )
        {
          p = new Point( domScreen, valScreen );

        }
        else
        {
          p = new Point( valScreen, domScreen );
        }
        path.add( p );
      }
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
    IAxis<T_domain> domainAxis = getDomainAxis();
    IAxis<T_target> targetAxis = getTargetAxis();

    int domPos;
    int tarPos;
    if( domainAxis.getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) )
    {
      domPos = p.x;
      tarPos = p.y;
    }
    else
    {
      domPos = p.y;
      tarPos = p.x;
    }

    T_domain domainVal1 = domainAxis.screenToLogical( domPos - tolerance );
    T_domain domainVal2 = domainAxis.screenToLogical( domPos + tolerance );
    T_target targetVal1 = targetAxis.screenToLogical( tarPos + tolerance );
    T_target targetVal2 = targetAxis.screenToLogical( tarPos - tolerance );

    Comparator<T_target> ct = getTargetAxis().getDataOperator().getComparator();
    Comparator<T_domain> cd = getDomainAxis().getDataOperator().getComparator();

    // Jetzt rausfinden, welches der größere und welcher der kleinere Wert ist und entsprechend zuweisen
    T_domain domainValMin;
    T_domain domainValMax;
    T_target targetValMin;
    T_target targetValMax;
    if( cd.compare( domainVal1, domainVal2 ) <= 0 )
    {
      domainValMin = domainVal1;
      domainValMax = domainVal2;
    }
    else
    {
      domainValMin = domainVal2;
      domainValMax = domainVal1;
    }

    if( ct.compare( targetVal1, targetVal2 ) <= 0 )
    {
      targetValMin = targetVal1;
      targetValMax = targetVal2;
    }
    else
    {
      targetValMin = targetVal2;
      targetValMax = targetVal1;
    }

    // herausfinden, ob der Punkt IN DER NAEHE eines Datenpunktes liegt
    for( int i = 0; i < m_domainData.size(); i++ )
    {
      T_domain domainVal = m_domainData.get( i );
      // Abbrechen, wenn wir über die Domain-Range raus sind
      if( cd.compare( domainVal, domainValMax ) > 0 )
        break;

      if( cd.compare( domainVal, domainValMin ) >= 0 )
      {
        T_target targetVal = m_targetData.get( i );
        if( ct.compare( targetVal, targetValMin ) >= 0 && ct.compare( targetVal, targetValMax ) <= 0 )
        {

          return createEditInfo( domainVal, targetVal, p, i );
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
  public EditInfo edit( Point point, EditInfo editInfo )
  {
    if( editInfo != null )
    {
      boolean isHorizontal = getDomainAxis().getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) ? true : false;
      int pos = (Integer) editInfo.data;
      T_domain domVal = m_domainData.get( pos );
      T_target tarVal = m_targetData.get( pos );
      // Der Domain-Wert soll bleiben, der Target-Wert kann verändert werden
      T_target newTarVal;
      if( isHorizontal )
        newTarVal = getTargetAxis().screenToLogical( point.y );
      else
        newTarVal = getTargetAxis().screenToLogical( point.x );

      m_targetData.set( pos, newTarVal );
      getEventHandler().fireLayerContentChanged( this );
      Point editPoint;
      if( isHorizontal )
        editPoint = new Point( editInfo.pos.x, point.y );
      else
        editPoint = new Point( point.x, editInfo.pos.y );

      EditInfo newEditInfo = createEditInfo( domVal, newTarVal, editPoint, pos );
      return newEditInfo;
    }
    return null;
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

  private EditInfo createEditInfo( T_domain domainVal, T_target targetVal, Point p, int pos )
  {
    if( domainVal != null && targetVal != null )
    {
      IAxis<T_domain> domainAxis = getDomainAxis();
      IAxis<T_target> targetAxis = getTargetAxis();

      /*
       * Punkt des "Handles" ausrechnen - evtl. kann man hier auch den eingangspunkt verwenden; (wahrscheinlich aber
       * nicht, weil sich sonst der handle verschiebt)
       */
      Point dataPos;
      if( getDomainAxis().getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) )
        dataPos = new Point( domainAxis.logicalToScreen( domainVal ), targetAxis.logicalToScreen( targetVal ) );
      else
        dataPos = new Point( targetAxis.logicalToScreen( targetVal ), domainAxis.logicalToScreen( domainVal ) );
      int pointWidth = 10;
      Rectangle editShape = new Rectangle( dataPos.x - pointWidth, dataPos.y - pointWidth, pointWidth * 2 + 1, pointWidth * 2 + 1 );
      // editText
      String domainValStr = domainAxis.getDataOperator().getFormat( domainAxis.getLogicalRange() ).format( domainVal );
      String targetValStr = targetAxis.getDataOperator().getFormat( targetAxis.getLogicalRange() ).format( targetVal );
      String editText = domainAxis.getLabel() + ":\t" + domainValStr + "\n" + targetAxis.getLabel() + ":\t" + targetValStr;

      // übergibt nur die position des Datensatzes, der geändert wird
      Object editData = pos;

      EditInfo info = new EditInfo( this, editShape, editData, editText, p );

      // falls der Punkt in den Daten vorhanden ist, dann eine Info zurückgeben
      return info;
    }
    return null;
  }

}
