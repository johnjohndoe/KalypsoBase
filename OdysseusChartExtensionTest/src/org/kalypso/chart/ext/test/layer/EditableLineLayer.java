package org.kalypso.chart.ext.test.layer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.ext.test.data.EditableTestDataContainer;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.figure.IFigure;
import de.openali.odysseus.chart.framework.model.figure.IPaintable;
import de.openali.odysseus.chart.framework.model.figure.impl.MultiFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PointFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolygonFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.impl.ColorFill;
import de.openali.odysseus.chart.framework.util.StyleUtils;

/**
 * @author alibu
 */
public class EditableLineLayer extends AbstractLineLayer implements IEditableChartLayer
{

  private final EditableTestDataContainer m_data;

  public EditableLineLayer( final ILayerProvider provider, final EditableTestDataContainer dataContainer, final ILineStyle lineStyle, final IPointStyle pointStyle )
  {
    super( provider, lineStyle, pointStyle );
    m_data = dataContainer;
  }

  private List<Object> m_domainData;

  private List<Object> m_targetData;

  private PointFigure m_editPointFigure;

  private PolylineFigure m_editLineFigure;

  /**
   * symbolizer which is shows while editing data
   */
  private MultiFigure m_editPaintable;

  /**
   * symbolizer which is shows while hovering data
   */
  private IPaintable m_hoverPaintable;

  private PointFigure m_hoverPointFigure;

  private PolygonFigure m_hoverRectFigure;

  private PolylineFigure m_hoverLineFigure;

  private IPointStyle m_hoverPointStyle;

  private IAreaStyle m_hoverRectStyle;

  private ILineStyle m_hoverLineStyle;

  private IPointStyle m_editPointStyle;

  private ILineStyle m_editLineStyle;

  private boolean m_isLocked = false;

  @Override
  public void init( )
  {
    final EditableTestDataContainer data = getDataContainer();
    if( data != null )
    {
      data.open();
      m_domainData = data.getDomainValues();
      m_targetData = data.getTargetValues();
    }
    else
      Logger.logWarning( Logger.TOPIC_LOG_GENERAL, "Layer " + getId() + " has not yet been opened" );
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC,
   *      org.eclipse.swt.graphics.Device)
   */
  @Override
  public void paint( final GC gc )
  {
    final ArrayList<Point> path = new ArrayList<Point>();
    final IAxis domAxis = getDomainAxis();
    final IAxis tarAxis = getTargetAxis();
    final IDataOperator dopDom = domAxis.getDataOperator( m_domainData.get( 0 ).getClass() );
    final IDataOperator dopTar = tarAxis.getDataOperator( m_targetData.get( 0 ).getClass() );

    final Comparator comp = dopDom.getComparator();

    for( int i = 0; i < m_domainData.size(); i++ )
    {
      final IDataRange<Number> domRange = getDomainAxis().getNumericRange();

      final Number min = domRange.getMin();
      final Number max = domRange.getMax();

      final Number domVal = dopDom.logicalToNumeric( m_domainData.get( i ) );
      boolean setPoint = false;
      if( (comp.compare( domVal, min ) >= 0) && (comp.compare( domVal, max ) <= 0) )
        setPoint = true;
      else // kleiner als min: Nachfolger muss >= min sein
      if( (comp.compare( domVal, min ) <= 0) && (i < m_domainData.size() - 1) )
      {
        final Object next = m_domainData.get( i + 1 );
        if( comp.compare( next, min ) >= 0 )
          setPoint = true;
      }
      // größer als max: Vorgänger muss <= max sein
      else if( (comp.compare( domVal, max ) >= 0) && (i > 0) )
      {
        final Object prev = m_domainData.get( i - 1 );
        if( comp.compare( prev, max ) <= 0 )
          setPoint = true;
        else
          // jetzt kann man aufhören
          break;
      }

      if( setPoint )
      {
        final Number targetVal = dopTar.logicalToNumeric( m_targetData.get( i ) );
        final int domScreen = domAxis.numericToScreen( domVal );
        final int valScreen = tarAxis.numericToScreen( targetVal );
        final ORIENTATION ori = getDomainAxis().getPosition().getOrientation();
        final Point unOri = new Point( domScreen, valScreen );
        final Point p = new Point( ori.getX( unOri ), ori.getY( unOri ) );
        path.add( p );
      }
    }
    drawLine( gc, path );
    drawPoints( gc, path );
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IEditableChartLayer#getEditInfo(org.eclipse.swt.graphics.Point)
   */
  @Override
  @SuppressWarnings("unchecked")
  public EditInfo getHover( final Point p )
  {
    // Umrechnen von screen nach logisch
    // TODO: im Moment wird von domain = horizontal und target = vertkal ausgegangen
    final int tolerance = 4;
    final IAxis domainAxis = getDomainAxis();
    final IAxis targetAxis = getTargetAxis();

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

    final Number domainVal1 = domainAxis.screenToNumeric( domPos - tolerance );
    final Number domainVal2 = domainAxis.screenToNumeric( domPos + tolerance );
    final Number targetVal1 = targetAxis.screenToNumeric( tarPos + tolerance );
    final Number targetVal2 = targetAxis.screenToNumeric( tarPos - tolerance );

    final Comparator ct = getTargetAxis().getDataOperator( domainVal1.getClass() ).getComparator();
    final Comparator cd = getDomainAxis().getDataOperator( targetVal1.getClass() ).getComparator();

    // Jetzt rausfinden, welches der größere und welcher der kleinere Wert ist und entsprechend zuweisen
    Object domainValMin;
    Object domainValMax;
    Object targetValMin;
    Object targetValMax;
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
      final Object domainVal = m_domainData.get( i );
      // Abbrechen, wenn wir über die Domain-Range raus sind
      if( cd.compare( domainVal, domainValMax ) > 0 )
        break;

      if( cd.compare( domainVal, domainValMin ) >= 0 )
      {
        final Object targetVal = m_targetData.get( i );
        if( (ct.compare( targetVal, targetValMin ) >= 0) && (ct.compare( targetVal, targetValMax ) <= 0) )
          return createEditInfo( p, i, false );
      }
    }

    return null;

  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IEditableChartLayer#edit(org.eclipse.swt.graphics.Point,
   *      java.lang.Object)
   */
  @Override
  public EditInfo drag( final Point point, final EditInfo editInfo )
  {
    if( editInfo != null )
    {
      final int pos = (Integer) editInfo.m_data;

      // m_targetData.set( pos, newTarVal );
      // getEventHandler().fireLayerContentChanged( this );
      final boolean isHorizontal = getDomainAxis().getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) ? true : false;
      Point editPoint;
      if( isHorizontal )
        editPoint = new Point( editInfo.m_pos.x, point.y );
      else
        editPoint = new Point( point.x, editInfo.m_pos.y );

      final EditInfo newEditInfo = createEditInfo( editPoint, pos, true );
      return newEditInfo;
    }
    return null;
  }

  public EditableTestDataContainer getDataContainer( )
  {
    return m_data;
  }

  /**
   * @param pos
   *          position of edited / hovered data in data set
   */
  @SuppressWarnings("unchecked")
  private <T_domain, T_target> EditInfo createEditInfo( final Point mousePos, final int pos, final boolean isDragging )
  {
    final EditableTestDataContainer dc = getDataContainer();
    final T_domain domainVal = (T_domain) dc.getDomainValues().get( pos );
    final T_target targetVal;
    if( isDragging )
    {
      // Der Domain-Wert soll bleiben, der Target-Wert kann verändert werden
      final boolean isHorizontal = getDomainAxis().getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) ? true : false;
      if( isHorizontal )
        targetVal = (T_target) getTargetAxis().screenToNumeric( mousePos.y );
      else
        targetVal = (T_target) getTargetAxis().screenToNumeric( mousePos.x );
    }
    else
      targetVal = (T_target) dc.getTargetValues().get( pos );

    if( (domainVal != null) && (targetVal != null) )
    {
      final IAxis domainAxis = getDomainAxis();
      final IAxis targetAxis = getTargetAxis();
      final IDataOperator<T_domain> dopDomain = domainAxis.getDataOperator( (Class<T_domain>) domainVal.getClass() );
      final IDataOperator<T_target> dopTarget = targetAxis.getDataOperator( (Class<T_target>) targetVal.getClass() );
      final Number domainValNum = dopDomain.logicalToNumeric( domainVal );
      final Number targetValNum = dopTarget.logicalToNumeric( targetVal );

      /*
       * Punkt des "Handles" ausrechnen - evtl. kann man hier auch den eingangspunkt verwenden; (wahrscheinlich aber
       * nicht, weil sich sonst der handle verschiebt)
       */
      final Point editPos = getCoordinateMapper().numericToScreen( domainValNum, targetValNum );

      // Aktiver Punkt
      final T_domain dataDomainVal = (T_domain) dc.getDomainValues().get( pos );
      final T_target dataTargetVal = (T_target) dc.getTargetValues().get( pos );

      // Punkt davor
      final T_domain preDomainVal = (T_domain) dc.getDomainValues().get( pos - 1 );
      final T_target preTargetVal = (T_target) dc.getTargetValues().get( pos - 1 );
      // Punkt danach
      final T_domain postDomainVal = (T_domain) dc.getDomainValues().get( pos + 1 );
      final T_target postTargetVal = (T_target) dc.getTargetValues().get( pos + 1 );

      final Number dataDomainValNum = dopDomain.logicalToNumeric( dataDomainVal );
      final Number dataTargetValNum = dopTarget.logicalToNumeric( dataTargetVal );
      final Number preDomainValNum = dopDomain.logicalToNumeric( preDomainVal );
      final Number preTargetValNum = dopTarget.logicalToNumeric( preTargetVal );
      final Number postDomainValNum = dopDomain.logicalToNumeric( postDomainVal );
      final Number postTargetValNum = dopTarget.logicalToNumeric( postTargetVal );

      Point dataPos = null;
      if( (dataDomainValNum != null) && (dataTargetValNum != null) )
        dataPos = getCoordinateMapper().numericToScreen( dataDomainValNum, dataTargetValNum );
      Point preDataPos = null;
      if( (preDomainValNum != null) && (preTargetValNum != null) )
        preDataPos = getCoordinateMapper().numericToScreen( preDomainValNum, preTargetValNum );
      Point postDataPos = null;
      if( (postDomainValNum != null) && (postTargetValNum != null) )
        postDataPos = getCoordinateMapper().numericToScreen( postDomainValNum, postTargetValNum );

      final Point[] editPoints = new Point[] { preDataPos, editPos, postDataPos };
      final Point[] hoverPoints = new Point[] { preDataPos, dataPos, postDataPos };

      IPaintable editPaintable = null;
      IPaintable hoverPaintable = null;

      hoverPaintable = getHoverPaintable( hoverPoints );
      if( isDragging )
        editPaintable = getEditPaintable( editPoints );

      // text für ToolTip
      final String domainValStr = domainAxis.getDataOperator( (Class<T_domain>) domainVal.getClass() ).getFormat( domainAxis.getNumericRange() ).format( domainVal );
      final String targetValStr = targetAxis.getDataOperator( (Class<T_target>) targetVal.getClass() ).getFormat( targetAxis.getNumericRange() ).format( targetVal );
      final String editText = getId() + "\n" + domainAxis.getLabel() + ":\t" + domainValStr + "\n" + targetAxis.getLabel() + ":\t" + targetValStr;

      // übergibt nur die position des Datensatzes, der geändert wird
      final Object editData = pos;

      final EditInfo info = new EditInfo( this, hoverPaintable, editPaintable, editData, editText, mousePos );

      // falls der Punkt in den Daten vorhanden ist, dann eine Info zurückgeben
      return info;
    }
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer#commitDrag(org.eclipse.swt.graphics.Point,
   *      de.openali.odysseus.chart.framework.model.layer.EditInfo)
   */
  @Override
  public EditInfo commitDrag( final Point point, final EditInfo dragStartData )
  {
    final boolean isHorizontal = getDomainAxis().getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) ? true : false;
    final int pos = (Integer) dragStartData.m_data;
    // Der Domain-Wert soll bleiben, der Target-Wert kann verändert werden
    Number newTarVal;
    if( isHorizontal )
      newTarVal = getTargetAxis().screenToNumeric( point.y );
    else
      newTarVal = getTargetAxis().screenToNumeric( point.x );

    m_targetData.set( pos, newTarVal );
    getEventHandler().fireLayerContentChanged( this );
    return null;
  }

  public IPaintable getEditPaintable( final Point[] points )
  {
    if( m_editPointFigure == null )
    {
      m_editPointFigure = new PointFigure();
      m_editPointStyle = getPointFigure().getStyle().copy();
      m_editPointStyle.setAlpha( (int) (getPointFigure().getStyle().getAlpha() * 0.5) );
      m_editPointFigure.setStyle( m_editPointStyle );
    }

    if( m_editLineFigure == null )
    {
      m_editLineFigure = new PolylineFigure();
      m_editLineStyle = getPolylineFigure().getStyle().copy();
      m_editLineStyle.setAlpha( (int) (getPolylineFigure().getStyle().getAlpha() * 0.5) );
      m_editLineFigure.setStyle( m_editLineStyle );
    }

    m_editPointFigure.setPoints( points );
    m_editLineFigure.setPoints( points );

    if( m_editPaintable == null )
      m_editPaintable = new MultiFigure( new IFigure[] { m_editLineFigure, m_editPointFigure } );
    return m_editPaintable;
  }

  private IPaintable getHoverPaintable( final Point[] points )
  {
    if( m_hoverPointFigure == null )
    {
      m_hoverPointFigure = new PointFigure();
      m_hoverPointStyle = getPointFigure().getStyle().copy();
      m_hoverPointFigure.setStyle( m_hoverPointStyle );
    }

    if( m_hoverRectFigure == null )
    {
      m_hoverRectFigure = new PolygonFigure();
      m_hoverRectStyle = StyleUtils.getDefaultAreaStyle();
      m_hoverRectStyle.setFill( new ColorFill( new RGB( 0, 255, 0 ) ) );
      m_hoverRectFigure.setStyle( m_hoverRectStyle );
    }

    if( m_hoverLineFigure == null )
    {
      m_hoverLineFigure = new PolylineFigure();
      m_hoverLineStyle = getPolylineFigure().getStyle().copy();
      m_hoverLineStyle.setColor( new RGB( 255, 0, 0 ) );
      m_hoverLineStyle.setWidth( getPolylineFigure().getStyle().getWidth() + 2 );
      m_hoverLineFigure.setStyle( m_hoverLineStyle );
    }

    m_hoverLineFigure.setPoints( points );

    // Rechteck um Punkt
    final int width = 10;
    final Point mid = points[1];
    final Point[] rectPoints = new Point[4];
    rectPoints[0] = new Point( mid.x - width, mid.y - width );
    rectPoints[1] = new Point( mid.x - width, mid.y + width );
    rectPoints[2] = new Point( mid.x + width, mid.y + width );
    rectPoints[3] = new Point( mid.x + width, mid.y - width );

    m_hoverRectFigure.setPoints( rectPoints );
    m_hoverPointFigure.setPoints( points );

    if( m_editPaintable == null )
      m_hoverPaintable = new MultiFigure( new IFigure[] { m_hoverRectFigure, m_hoverLineFigure, m_hoverPointFigure } );
    return m_hoverPaintable;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  @Override
  @SuppressWarnings("unchecked")
  public IDataRange<Number> getDomainRange( )
  {
    final IDataRange logRange = getDataContainer().getDomainRange();
    final Object min = logRange.getMin();
    if( min != null )
    {
      final IDataOperator dop = getDomainAxis().getDataOperator( min.getClass() );
      return new ComparableDataRange<Number>( new Number[] { dop.logicalToNumeric( min ), dop.logicalToNumeric( logRange.getMax() ) } );
    }
    return null;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#getTargetRange()
   */
  @Override
  @SuppressWarnings("unchecked")
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
  {
    final IDataRange logRange = getDataContainer().getTargetRange();
    final Object min = logRange.getMin();
    if( min != null )
    {
      final IDataOperator dop = getDomainAxis().getDataOperator( min.getClass() );
      return new ComparableDataRange<Number>( new Number[] { dop.logicalToNumeric( min ), dop.logicalToNumeric( logRange.getMax() ) } );
    }
    return null;
  }

  @Override
  public void dispose( )
  {
    super.dispose();

  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer#isLocked()
   */
  @Override
  public boolean isLocked( )
  {
    return m_isLocked;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer#lockLayer(boolean)
   */
  @Override
  public void lockLayer( final boolean isLocked )
  {
    m_isLocked = isLocked;

  }
}
