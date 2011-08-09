/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.model.wspm.ui.view.chart;

import java.awt.geom.Point2D;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilChange;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.core.profil.util.ProfilUtil;
import org.kalypso.model.wspm.ui.view.ILayerStyleProvider;
import org.kalypso.model.wspm.ui.view.IProfilView;
import org.kalypso.observation.result.ComponentUtilities;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;

import de.openali.odysseus.chart.factory.layer.AbstractChartLayer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINECAP;
import de.openali.odysseus.chart.framework.util.StyleUtils;

/**
 * @author kimwerner
 */
public abstract class AbstractProfilLayer extends AbstractChartLayer implements IProfilChartLayer
{
  private final String m_domainComponent;

  private boolean m_isLocked = false;

  private ILineStyle m_lineStyle = null;

  private ILineStyle m_lineStyleActive = null;

  private ILineStyle m_lineStyleHover = null;

  private IPointStyle m_pointStyle = null;

  private IPointStyle m_pointStyleActive = null;

  private IPointStyle m_pointStyleHover = null;

  private IProfil m_profil;

  private int m_targetPropIndex = -1;

  private final String m_targetRangeProperty;

  public AbstractProfilLayer( final String id, final IProfil profil, final String targetRangeProperty, final ILayerStyleProvider styleProvider )
  {
    super( null );

    m_profil = profil;
    m_targetRangeProperty = targetRangeProperty;
    m_domainComponent = IWspmConstants.POINT_PROPERTY_BREITE;
    setIdentifier( id );
    createStyles( styleProvider, id );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer#commitDrag(org.eclipse.swt.graphics.Point,
   *      de.openali.odysseus.chart.framework.model.layer.EditInfo)
   */
  @Override
  public EditInfo commitDrag( final Point point, final EditInfo dragStartData )
  {
    final IComponent targetComponent = getTargetComponent();
    if( targetComponent != null )
    {
      getProfil().setActivePointProperty( targetComponent );
    }

    if( point == null || dragStartData.getPosition() == point )
    {
      executeClick( dragStartData );
    }
    else
    {
      executeDrop( point, dragStartData );
    }

    return null;
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.IProfilChartLayer#createLayerPanel()
   */
  @Override
  public IProfilView createLayerPanel( )
  {
    // override this method
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractChartLayer#createLegendEntries()
   */
  @Override
  protected ILegendEntry[] createLegendEntries( )
  {
    // override this method
    return null;
  }

  private void createStyles( final ILayerStyleProvider styleProvider, final String id )
  {
    if( styleProvider == null )
      return;

    // TODO: stlyes should be fetched on demand!
    // It is not guaranteed, that we need only one line style!

    // FIXME: remove theses magic names
    m_lineStyle = styleProvider.getStyleFor( id + "_LINE", null ); //$NON-NLS-1$
    m_pointStyle = styleProvider.getStyleFor( id + "_POINT", null ); //$NON-NLS-1$

    m_lineStyleActive = styleProvider.getStyleFor( id + "_LINE_ACTIVE", null ); //$NON-NLS-1$
    m_pointStyleActive = styleProvider.getStyleFor( id + "_POINT_ACTIVE", null ); //$NON-NLS-1$

    m_lineStyleHover = styleProvider.getStyleFor( id + "_LINE_HOVER", null ); //$NON-NLS-1$
    m_pointStyleHover = styleProvider.getStyleFor( id + "_POINT_HOVER", null ); //$NON-NLS-1$
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#dispose()
   */
  @Override
  public void dispose( )
  {
    /**
     * don't dispose Styles, StyleProvider will do
     */

  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer#drag(org.eclipse.swt.graphics.Point,
   *      de.openali.odysseus.chart.framework.model.layer.EditInfo)
   */

  @Override
  public EditInfo drag( final Point newPos, final EditInfo dragStartData )
  {
    // override this method
    return dragStartData;// return new EditInfo( this, null, null, dragStartData.m_data, "", newPos );
  }

  /**
   * @see org.kalypso.model.wspm.tuhh.ui.chart.AbstractProfilLayer#executeClick(de.openali.odysseus.chart.framework.model.layer.EditInfo)
   */
  @Override
  public void executeClick( final EditInfo clickInfo )
  {
    final Object data = clickInfo.getData();
    final Integer pos = data instanceof Integer ? (Integer) data : null;
    final IComponent cmp = getTargetComponent();
    if( !Objects.isNull( cmp, pos ) )
    {
      final IProfil profil = getProfil();
      profil.setActivePoint( profil.getPoint( pos ) );
      profil.setActivePointProperty( cmp );
    }
  }

  /**
   * To be implemented by subclasses - if needed
   * 
   * @see org.kalypso.model.wspm.ui.view.chart.IProfilChartLayer#executeDrop(org.eclipse.swt.graphics.Point,
   *      de.openali.odysseus.chart.framework.model.layer.EditInfo)
   */
  @Override
  public void executeDrop( final Point point, final EditInfo dragStartData )
  {
  }

  @Override
  public IComponent getDomainComponent( )
  {
    return getProfil() == null ? null : getProfil().hasPointProperty( m_domainComponent );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  @Override
  public IDataRange<Number> getDomainRange( )
  {
    if( getCoordinateMapper() == null )
      return null;
    final Double max = ProfilUtil.getMaxValueFor( getProfil(), getDomainComponent() );
    final Double min = ProfilUtil.getMinValueFor( getProfil(), getDomainComponent() );
    if( Objects.isNull( min, max ) )
      return null;

    return new DataRange<Number>( min, max );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer#getHover(org.eclipse.swt.graphics.Point)
   */
  @Override
  public EditInfo getHover( final Point pos )
  {
    if( !isVisible() || getProfil() == null )
      return null;
    final IRecord[] profilPoints = getProfil().getPoints();
    final int len = profilPoints.length;
    for( int i = 0; i < len; i++ )
    {

      final Rectangle hover = getHoverRect( profilPoints[i] );
      if( hover == null )
      {
        continue;
      }

      if( hover.contains( pos ) )
      {
        final Point target = toScreen( profilPoints[i] );
        if( target == null )
          return new EditInfo( this, null, null, i, getTooltipInfo( profilPoints[i] ), RectangleUtils.getCenterPoint( hover ) );
        return new EditInfo( this, null, null, i, getTooltipInfo( profilPoints[i] ), target );
      }
    }
    return null;
  }

  @SuppressWarnings("unused")
  public Rectangle getHoverRect( final IRecord profilPoint )
  {
    return null;
  }

  protected ILineStyle getLineStyle( )
  {
    if( m_lineStyle == null )
      m_lineStyle = StyleUtils.getDefaultLineStyle();

    return m_lineStyle;
  }

  protected ILineStyle getLineStyleActive( )
  {
    if( m_lineStyleActive == null )
    {
      m_lineStyleActive = getLineStyle().clone();
      m_lineStyleActive.setColor( COLOR_ACTIVE );
    }
    return m_lineStyleActive;
  }

  protected ILineStyle getLineStyleHover( )
  {
    if( m_lineStyleHover == null )
    {
      m_lineStyleHover = getLineStyle().clone();
      m_lineStyleHover.setDash( 0f, HOVER_DASH );
      m_lineStyleHover.setLineCap( LINECAP.FLAT );
    }
    return m_lineStyleHover;
  }

  public IRecord getNextNonNull( final int index )
  {
    final IRecord[] points = getProfil().getPoints();
    final int prop = getProfil().indexOfProperty( m_targetRangeProperty );
    for( int i = index + 1; i < points.length; i++ )
    {

      if( points[i] != null && points[i].getValue( prop ) != null )
        return points[i];
    }
    return points[index];

  }

  public Point2D getPoint2D( final IRecord point )
  {
    final Double x = ProfilUtil.getDoubleValueFor( m_domainComponent, point );
    final Double y = ProfilUtil.getDoubleValueFor( getTargetPropertyIndex(), point );
    return new Point2D.Double( x, y );
  }

  protected IPointStyle getPointStyle( )
  {
    if( m_pointStyle == null )
    {
      m_pointStyle = StyleUtils.getDefaultPointStyle();
      m_pointStyle.setStroke( getLineStyle().clone() );
      m_pointStyle.setInlineColor( getLineStyle().getColor() );
      m_pointStyle.setWidth( POINT_STYLE_WIDTH );
      m_pointStyle.setHeight( POINT_STYLE_WIDTH );
    }
    return m_pointStyle;
  }

  protected IPointStyle getPointStyleActive( )
  {
    if( m_pointStyleActive == null )
    {
      m_pointStyleActive = getPointStyle().clone();
      m_pointStyleActive.setStroke( getLineStyleActive().clone() );
      m_pointStyleActive.setInlineColor( getLineStyleActive().getColor() );
    }
    return m_pointStyleActive;
  }

  protected IPointStyle getPointStyleHover( )
  {
    if( m_pointStyleHover == null )
    {
      m_pointStyleHover = getPointStyle().clone();
      m_pointStyleHover.setStroke( getLineStyleHover().clone() );
      m_pointStyleHover.setFillVisible( false );
    }
    return m_pointStyleHover;
  }

  public IRecord getPreviousNonNull( final int index )
  {
    final IRecord[] points = getProfil().getPoints();
    final int prop = getProfil().indexOfProperty( m_targetRangeProperty );
    for( int i = index - 1; i > -1; i-- )
    {
      if( points[i] != null && points[i].getValue( prop ) != null )
        return points[i];
    }
    return points[index];

  }

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.IProfilChartLayer#getProfil()
   */
  @Override
  public IProfil getProfil( )
  {
    return m_profil;
  }

  @Override
  public IComponent getTargetComponent( )
  {
    if( m_profil == null || getTargetPropertyIndex() == -1 )
      return null;

    final int indexOfProperty = m_profil.indexOfProperty( m_targetRangeProperty );
    if( indexOfProperty < 0 )
      return null;
    return m_profil.getResult().getComponent( indexOfProperty );
  }

  protected final int getTargetPropertyIndex( )
  {
    if( m_targetPropIndex < 0 )
    {
      if( m_profil == null )
        return -1;
      m_targetPropIndex = m_profil.getResult().indexOfComponent( m_targetRangeProperty );
    }
    return m_targetPropIndex;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getTargetRange()
   */
  @Override
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
  {
    final int targetPropertyIndex = getTargetPropertyIndex();
    if( getCoordinateMapper() == null || targetPropertyIndex == -1 )
      return null;

    final Double max = ProfilUtil.getMaxValueFor( getProfil(), targetPropertyIndex );
    final Double min = ProfilUtil.getMinValueFor( getProfil(), targetPropertyIndex );
    if( Objects.isNull( min, max ) )
      return null;

    if( Math.abs( min - max ) < 0.001 )
      return new DataRange<Number>( min - 1, min + 1 );

    return new DataRange<Number>( min, max );
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractChartLayer#getTitle()
   */
  @Override
  public String getTitle( )
  {
    final IComponent targetComponent = getTargetComponent();
    if( targetComponent == null )
      return ""; //$NON-NLS-1$

    return targetComponent.getName();
  }

  public String getTooltipInfo( final IRecord point )
  {
    if( Objects.isNull( point, getTargetComponent(), getDomainComponent() ) )
      return ""; //$NON-NLS-1$

    try
    {
      final Point2D p = getPoint2D( point );
      return String.format( TOOLTIP_FORMAT, new Object[] { getDomainComponent().getName(), p.getX(), getTargetComponent().getName(), p.getY(),
          ComponentUtilities.getComponentUnitLabel( getTargetComponent() ) } );
    }
    catch( final RuntimeException e )
    {
      return e.getLocalizedMessage();
    }

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

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.IProfilChartLayer#onProfilChanged(org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint,
   *      org.kalypso.model.wspm.core.profil.IProfilChange[])
   */
  @Override
  public void onProfilChanged( final ProfilChangeHint hint, final IProfilChange[] changes )
  {
    final IProfil profil = getProfil();
    if( profil == null )
      return;
    if( hint.isActivePointChanged() )
    {
      getEventHandler().fireLayerContentChanged( this );
    }
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC)
   */
  @Override
  public void paint( final GC gc )
  {
    // override this method
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.IProfilChartLayer#removeYourself()
   */
  @Override
  public void removeYourself( )
  {
    // override this method
    throw new UnsupportedOperationException();
  }

  public void setLineStyle( final ILineStyle lineStyle )
  {
    m_lineStyle = lineStyle;
  }

  public void setLineStyleActive( final ILineStyle lineStyleActive )
  {
    m_lineStyleActive = lineStyleActive;
  }

  public void setLineStyleHover( final ILineStyle lineStyleHover )
  {
    m_lineStyleHover = lineStyleHover;
  }

  public void setPointStyle( final IPointStyle pointStyle )
  {
    m_pointStyle = pointStyle;
  }

  public void setPointStyleActive( final IPointStyle pointStyleActive )
  {
    m_pointStyleActive = pointStyleActive;
  }

  public void setPointStyleHover( final IPointStyle pointStyleHover )
  {
    m_pointStyleHover = pointStyleHover;
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.IProfilChartLayer#setProfil(org.kalypso.model.wspm.core.profil.IProfil)
   */
  @Override
  public void setProfil( final IProfil profil )
  {
    m_profil = profil;
  }

  public Point2D toNumeric( final Point point )
  {
    if( point == null )
      return null;
    final ICoordinateMapper cm = getCoordinateMapper();
    final Double x = cm.getDomainAxis().screenToNumeric( point.x ).doubleValue();
    final Double y = cm.getTargetAxis().screenToNumeric( point.y ).doubleValue();
    return new Point2D.Double( x, y );
  }

  public Point toScreen( final IRecord point )
  {
    final ICoordinateMapper cm = getCoordinateMapper();
    if( Objects.isNull( cm ) )
      return null;

    final Double x = ProfilUtil.getDoubleValueFor( m_domainComponent, point );
    final Double y = ProfilUtil.getDoubleValueFor( getTargetPropertyIndex(), point );
    if( Objects.isNull( x, y ) )
      return null;

    if( x.isNaN() || y.isNaN() )
      return null;

    return cm.numericToScreen( x, y );
  }
}
