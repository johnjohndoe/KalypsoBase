/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.chart.ext.observation.layer;

import java.text.Format;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.figure.IPaintable;
import de.openali.odysseus.chart.framework.model.figure.impl.PointFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.IRetinalMapper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

/**
 * @author burtscher1
 */
public class BranchLayer extends AbstractLineLayer implements ITooltipChartLayer
{

  private final TupleResult m_data;

  private final String m_domainComponentId;

  private final String m_targetComponentId;

  private final String m_iconComponentId;

  private IComponent m_domainComponent = null;

  private IComponent m_targetComponent = null;

  private IComponent m_iconComponent = null;

  private boolean m_isInited = false;

  private IDataRange<Number> m_domainRange;

  private IDataRange<Number> m_targetRange;

  private HashMap<Object, IPointStyle> m_mapping;

  private final Map<Object, ArrayList<Point>> m_pointMarks = new HashMap<Object, ArrayList<Point>>();

  private IPointStyle m_hoverStyle = null;

  /**
   * @param iconComponent
   *          the component which shall be mapped to an icon
   */
  public BranchLayer( final ILayerProvider provider, final TupleResult data, final String domainComponentId, final String targetComponentId, final String iconComponentenId, final ILineStyle lineStyle, final IPointStyle pointStyle )
  {
    super( provider, lineStyle, pointStyle );

    m_data = data;
    m_domainComponentId = domainComponentId;
    m_targetComponentId = targetComponentId;
    m_iconComponentId = iconComponentenId;
  }

  @Override
  @SuppressWarnings({ "unchecked", "deprecation" })
  public void init( )
  {
    // find components
    final IComponent[] components = m_data.getComponents();
    for( final IComponent component : components )
      if( component.getId().equals( m_domainComponentId ) )
        m_domainComponent = component;
      else if( component.getId().equals( m_targetComponentId ) )
        m_targetComponent = component;
      else if( component.getId().equals( m_iconComponentId ) )
        m_iconComponent = component;

    if( (m_domainComponent != null) && (m_targetComponent != null) && (m_iconComponent != null) )
      m_isInited = true;

    // Icon-Mapping vorbereiten

    m_mapping = new HashMap<Object, IPointStyle>();
    final IRetinalMapper mapper = getMapper( "icon" );
    final IDataOperator dop = mapper.getDataOperator( String.class );

    final String[] smallStyles = new String[] { "ConnectionNode", "LinkageNode", "CrossSectionNode" };

    for( int i = 0; i < m_data.size(); i++ )
    {
      final IRecord record = m_data.get( i );
      final Object value = record.getValue( m_iconComponent );
      if( !m_mapping.containsKey( value ) )
      {

        final IPointStyle iconStyle = mapper.numericToScreen( dop.logicalToNumeric( value ), getPointStyle() );
        if( !ArrayUtils.contains( smallStyles, value ) )
        {
          iconStyle.setHeight( 16 );
          iconStyle.setWidth( 16 );
        }

        m_mapping.put( value, iconStyle );

      }
    }

  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  @Override
  public IDataRange<Number> getDomainRange( )
  {
    if( m_isInited )
    {
      if( m_domainRange == null )
        m_domainRange = getRange( m_data, m_domainComponent, getDomainAxis() );
      return m_domainRange;
    }
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getTargetRange()
   */
  @Override
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
  {
    if( m_isInited )
    {
      if( m_targetRange == null )
        m_targetRange = getRange( m_data, m_targetComponent, getTargetAxis() );
      return m_targetRange;
    }
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC)
   */
  @Override
  @SuppressWarnings("deprecation")
  public void paint( final GC gc )
  {

    final List<Point> path = new ArrayList<Point>();

    for( final Object o : m_mapping.keySet() )
      m_pointMarks.put( o, new ArrayList<Point>() );

    if( m_isInited )
      for( int i = 0; i < m_data.size(); i++ )
      {
        final IRecord record = m_data.get( i );
        final Point point = getCoordinateMapper().logicalToScreen( record.getValue( m_domainComponent ), record.getValue( m_targetComponent ) );
        path.add( point );
        // Punkte merken
        m_pointMarks.get( record.getValue( m_iconComponent ) ).add( point );
      }

    final PolylineFigure polylineFigure = getPolylineFigure();
    polylineFigure.setPoints( path.toArray( new Point[] {} ) );
    polylineFigure.paint( gc );

    // Punkte zeichnen
    for( final Entry<Object, ArrayList<Point>> e : m_pointMarks.entrySet() )
    {
      final ArrayList<Point> points = e.getValue();
      final PointFigure pointFigure = getPointFigure();
      final Object nodeType = e.getKey();
      final IPointStyle iconStyle = m_mapping.get( nodeType );

      pointFigure.setStyle( iconStyle );
      pointFigure.setPoints( points.toArray( new Point[points.size()] ) );
      pointFigure.paint( gc );

    }

  }

  @SuppressWarnings({ "unchecked", "deprecation" })
  private static IDataRange<Number> getRange( final TupleResult data, final IComponent comp, final IAxis axis )
  {
    final int size = data.size();
    Object value = null;
    IDataOperator op = null;
    double min = Double.MAX_VALUE;
    double max = -Double.MAX_VALUE;
    for( int i = 0; i < size; i++ )
    {
      final IRecord record = data.get( i );
      value = record.getValue( comp );

      // Beim ersten Mal: abfragen
      if( op == null )
        op = axis.getDataOperator( value.getClass() );
      // Überprüfen, ob vorhanden
      if( op != null )
      {
        if( op.logicalToNumeric( value ).doubleValue() < min )
          min = op.logicalToNumeric( value ).doubleValue();
        if( op.logicalToNumeric( value ).doubleValue() > max )
          max = op.logicalToNumeric( value ).doubleValue();
      }
      else
      {
        Logger.logFatal( Logger.TOPIC_LOG_LAYER, "There's no data operator for class '" + value.getClass() + "'" );
        return null;
      }
    }
    return new DataRange<Number>( min, max );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getLegendEntries(org.eclipse.swt.graphics.Point)
   */
  @Override
  public ILegendEntry[] createLegendEntries( )
  {

    final ArrayList<ILegendEntry> entries = new ArrayList<ILegendEntry>();
    final ILineStyle ls = getPolylineFigure().getStyle();
    if( ls.isVisible() )
    {

      final LegendEntry le = new LegendEntry( this, "Length section" )
      {

        @Override
        public void paintSymbol( final GC gc, final Point size )
        {
          final int sizeX = size.x;
          final int sizeY = size.y;

          final ArrayList<Point> path = new ArrayList<Point>();
          path.add( new Point( 0, sizeX / 2 ) );
          path.add( new Point( sizeX / 5, sizeY / 2 ) );
          path.add( new Point( sizeX / 5 * 2, sizeY / 4 ) );
          path.add( new Point( sizeX / 5 * 3, sizeY / 4 * 3 ) );
          path.add( new Point( sizeX / 5 * 4, sizeY / 2 ) );
          path.add( new Point( sizeX, sizeY / 2 ) );
          drawLine( gc, path );
        }

      };
      entries.add( le );

    }

    final PointFigure pf = new PointFigure();
    for( final Entry<Object, IPointStyle> e : m_mapping.entrySet() )
    {
      final IPointStyle ps = e.getValue();
      if( ps.isVisible() )
      {

        final LegendEntry le = new LegendEntry( this, e.getKey().toString() )
        {
          @Override
          public void paintSymbol( final GC gc, final Point size )
          {
            pf.setPoints( new Point[] { new Point( size.x / 2, size.y / 2 ) } );
            pf.setStyle( ps );
            pf.paint( gc );
          }

        };
        entries.add( le );
      }
    }

    return entries.toArray( new ILegendEntry[] {} );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer#getHover(org.eclipse.swt.graphics.Point)
   */
  @Override
  @SuppressWarnings({ "unchecked", "deprecation" })
  public EditInfo getHover( final Point pos )
  {
    // Umrechnen von screen nach logisch
    final int tolerance = 4;
    final IAxis domainAxis = getDomainAxis();
    final IAxis targetAxis = getTargetAxis();

    int domPos;
    int tarPos;
    if( domainAxis.getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) )
    {
      domPos = pos.x;
      tarPos = pos.y;
    }
    else
    {
      domPos = pos.y;
      tarPos = pos.x;
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
    for( int i = 0; i < m_data.size(); i++ )
    {
      final IRecord record = m_data.get( i );
      final Object domainVal = record.getValue( m_domainComponent );
      // Abbrechen, wenn wir über die Domain-Range raus sind
      if( cd.compare( domainVal, domainValMax ) > 0 )
        break;

      if( cd.compare( domainVal, domainValMin ) >= 0 )
      {
        final Object targetVal = record.getValue( m_targetComponent );
        if( (ct.compare( targetVal, targetValMin ) >= 0) && (ct.compare( targetVal, targetValMax ) <= 0) )
          return createTooltipInfo( pos, i );
      }
    }

    return null;

  }

  /**
   * @param pos
   *          position of edited / hovered data in data set
   */
  @SuppressWarnings({ "unchecked", "deprecation" })
  private <T_domain, T_target> EditInfo createTooltipInfo( final Point mousePos, final int pos )
  {
    final IRecord record = m_data.get( pos );
    final T_domain domainVal = (T_domain) record.getValue( m_domainComponent );
    final T_target targetVal = (T_target) record.getValue( m_targetComponent );

    if( (domainVal != null) && (targetVal != null) )
    {
      final IAxis domainAxis = getDomainAxis();
      final IAxis targetAxis = getTargetAxis();
      final IDataOperator<T_domain> dopDomain = domainAxis.getDataOperator( (Class<T_domain>) domainVal.getClass() );
      final IDataOperator<T_target> dopTarget = targetAxis.getDataOperator( (Class<T_target>) targetVal.getClass() );

      final Number dataDomainValNum = dopDomain.logicalToNumeric( domainVal );
      final Number dataTargetValNum = dopTarget.logicalToNumeric( targetVal );

      Point dataPos = null;
      if( (dataDomainValNum != null) && (dataTargetValNum != null) )
        dataPos = getCoordinateMapper().numericToScreen( dataDomainValNum, dataTargetValNum );

      final IRetinalMapper mapper = getMapper( "icon" );
      final Object iconVal = record.getValue( m_iconComponent );
      final IDataOperator iop = mapper.getDataOperator( iconVal.getClass() );

      m_hoverStyle = mapper.numericToScreen( iop.logicalToNumeric( iconVal ), getPointStyle() );
      m_hoverStyle.getStroke().setVisible( true );
      m_hoverStyle.setWidth( 20 );
      m_hoverStyle.setHeight( 20 );

      final PointFigure hoverFigure = new PointFigure();
      hoverFigure.setStyle( m_hoverStyle );
      hoverFigure.setPoints( new Point[] { dataPos } );
      final IPaintable hoverPaintable = hoverFigure;

      // text für ToolTip
      final String domainValStr = domainAxis.getDataOperator( (Class<T_domain>) domainVal.getClass() ).getFormat( domainAxis.getNumericRange() ).format( domainVal );
      final String targetValStr = targetAxis.getDataOperator( (Class<T_target>) targetVal.getClass() ).getFormat( targetAxis.getNumericRange() ).format( targetVal );
      final Format format = mapper.getDataOperator( iconVal.getClass() ).getFormat( null );
      final String iconValStr = format.format( iconVal );

      final String editText = getTitle() + "\n" + domainAxis.getLabel() + ": " + domainValStr + "\n" + targetAxis.getLabel() + ": " + targetValStr + "\n" + iconValStr;

      final EditInfo info = new EditInfo( this, hoverPaintable, null, null, editText, mousePos );

      // falls der Punkt in den Daten vorhanden ist, dann eine Info zurückgeben
      return info;
    }
    return null;
  }

  @Override
  public void dispose( )
  {
    super.dispose();

    m_mapping.clear();
  }

}
