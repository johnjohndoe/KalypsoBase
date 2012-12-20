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
package org.kalypso.model.wspm.ui.view.chart;

import java.awt.geom.Point2D;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.changes.ProfileChangeHint;
import org.kalypso.model.wspm.core.profil.util.ProfileUtil;
import org.kalypso.model.wspm.core.profil.visitors.FindMinMaxVisitor;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.ui.view.ILayerStyleProvider;
import org.kalypso.observation.result.ComponentUtilities;
import org.kalypso.observation.result.IComponent;

import de.openali.odysseus.chart.ext.base.layer.TooltipFormatter;
import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;

/**
 * @author kimwerner
 */
public abstract class AbstractProfilePointsLayer extends AbstractProfilLayer
{
  private final String m_domainComponent;

  private ILineStyle m_lineStyle = null;

  private ILineStyle m_lineStyleActive = null;

  private ILineStyle m_lineStyleHover = null;

  private IPointStyle m_pointStyle = null;

  private IPointStyle m_pointStyleActive = null;

  private IPointStyle m_pointStyleHover = null;

  private IAreaStyle m_areaStyle = null;

  private int m_targetPropIndex = -1;

  // FIXME: does not belong here! -> move into layers that 'think' in profile properties
  private final String m_targetRangeProperty;

  public AbstractProfilePointsLayer( final String id, final IProfile profile, final String targetRangeProperty, final ILayerStyleProvider styleProvider )
  {
    super( id, profile );

    m_targetRangeProperty = targetRangeProperty;
    m_domainComponent = IWspmPointProperties.POINT_PROPERTY_BREITE;

    createStyles( styleProvider, id );
  }

  protected String getTargetProperty( )
  {
    return m_targetRangeProperty;
  }

  @Override
  public EditInfo commitDrag( final Point point, final EditInfo dragStartData )
  {
    final IComponent targetComponent = getTargetComponent();
    if( targetComponent != null )
    {
      getProfil().getSelection().setActivePointProperty( targetComponent );
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

  private void createStyles( final ILayerStyleProvider styleProvider, final String id )
  {
    if( styleProvider == null )
      return;

    // TODO: stlyes should be fetched on demand!
    // It is not guaranteed, that we need only one line style!

    // FIXME: remove theses magic names
    m_lineStyle = styleProvider.getStyleFor( id + ILayerStyleProvider.LINE, null ); //$NON-NLS-1$
    m_pointStyle = styleProvider.getStyleFor( id + ILayerStyleProvider.POINT, null ); //$NON-NLS-1$
    m_areaStyle = styleProvider.getStyleFor( id + ILayerStyleProvider.AREA, null ); //$NON-NLS-1$

    m_lineStyleActive = styleProvider.getStyleFor( id + "_LINE_ACTIVE", null ); //$NON-NLS-1$
    m_pointStyleActive = styleProvider.getStyleFor( id + "_POINT_ACTIVE", null ); //$NON-NLS-1$

    m_lineStyleHover = styleProvider.getStyleFor( id + "_LINE_HOVER", null ); //$NON-NLS-1$
    m_pointStyleHover = styleProvider.getStyleFor( id + "_POINT_HOVER", null ); //$NON-NLS-1$
  }

  @Override
  public EditInfo drag( final Point newPos, final EditInfo dragStartData )
  {
    return dragStartData;
  }

  @Override
  public void executeClick( final EditInfo clickInfo )
  {
    final Object data = clickInfo.getData();
    final Integer pos = data instanceof Integer ? (Integer)data : null;
    final IComponent cmp = getTargetComponent();
    if( !Objects.isNull( cmp, pos ) )
    {
      final IProfile profil = getProfil();
      profil.getSelection().setActivePoints( profil.getPoint( pos ) );
      profil.getSelection().setActivePointProperty( cmp );
    }
  }

  /**
   * To be implemented by subclasses - if needed
   */
  @Override
  public void executeDrop( final Point point, final EditInfo dragStartData )
  {
  }

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  @Override
  public IDataRange<Double> getDomainRange( )
  {
    if( getCoordinateMapper() == null )
      return null;

    final IComponent domain = getDomainComponent();
    if( Objects.isNull( domain ) )
      return null;

    final int domainPropertyIndex = getProfil().getResult().indexOfComponent( m_domainComponent );
    final FindMinMaxVisitor visitor = new FindMinMaxVisitor( domain.getId() );
    getProfil().accept( visitor, 1 );

    final IProfileRecord min = visitor.getMinimum();
    final IProfileRecord max = visitor.getMaximum();
    if( Objects.isNull( min, max ) )
      return null;

    return new DataRange( min.getValue( domainPropertyIndex ), max.getValue( domainPropertyIndex ) );
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

  // FIXME: this abstract default style stuff is contraproductive. Each laxer should once and for all define its own
  // styles.
  protected ILineStyle getLineStyleHover( )
  {
    if( m_lineStyleHover == null )
      m_lineStyleHover = ProfileStyleUtils.deriveHoverStyle( getLineStyle() );

    return m_lineStyleHover;
  }

  public Point2D getPoint2D( final IProfileRecord point )
  {
    final Double x = ProfileUtil.getDoubleValueFor( m_domainComponent, point );
    final Double y = ProfileUtil.getDoubleValueFor( getTargetPropertyIndex(), point );
    return new Point2D.Double( x, y );
  }

  protected IPointStyle getPointStyle( )
  {
    if( m_pointStyle == null )
    {
      // FIXME: ugly, why is point derived from line style?
      m_pointStyle = StyleUtils.getDefaultPointStyle();
      m_pointStyle.setStroke( getLineStyle().clone() );
      m_pointStyle.setInlineColor( getLineStyle().getColor() );
      // ??? ugly!
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

  // FIXME: this abstract default style stuff is contraproductive. Each laxer should once and for all define its own
  // styles.
  // FIXME: maybe move everything into the style provider?
  protected IPointStyle getPointStyleHover( )
  {
    if( m_pointStyleHover == null )
      m_pointStyleHover = ProfileStyleUtils.deriveHoverStyle( getPointStyle() );

    return m_pointStyleHover;
  }

  public IAreaStyle getAreaStyle( )
  {
    if( m_areaStyle == null )
      m_areaStyle = StyleUtils.getDefaultAreaStyle();

    return m_areaStyle;
  }

  @Override
  public IComponent getDomainComponent( )
  {
    return getComponent( m_domainComponent );
  }

  @Override
  public IComponent getTargetComponent( )
  {
    return getComponent( m_targetRangeProperty );
  }

  protected final int getTargetPropertyIndex( )
  {
    final IProfile profil = getProfil();

    if( m_targetPropIndex < 0 )
    {
      if( profil == null )
        return -1;
      m_targetPropIndex = profil.getResult().indexOfComponent( m_targetRangeProperty );
    }

    return m_targetPropIndex;
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IDataRange<Double> getTargetRange( final IDataRange<Double> domainIntervall )
  {
    final int targetPropertyIndex = getTargetPropertyIndex();
    if( getCoordinateMapper() == null || targetPropertyIndex == -1 )
      return null;

    final IComponent target = getTargetComponent();
    if( target == null )
      return null;

    final FindMinMaxVisitor visitor = new FindMinMaxVisitor( target.getId() );
    getProfil().accept( visitor, 1 );

    final IProfileRecord min = visitor.getMinimum();
    final IProfileRecord max = visitor.getMaximum();
    if( Objects.isNull( min, max ) )
      return null;

    final Number minValue = (Number)min.getValue( targetPropertyIndex );
    final Number maxValue = (Number)max.getValue( targetPropertyIndex );

    return new DataRange( minValue, maxValue );
  }

  @Override
  public String getTitle( )
  {
    final IComponent targetComponent = getTargetComponent();
    if( targetComponent == null )
      return super.getTitle();

    return targetComponent.getName();
  }

  protected String getTooltipInfo( final IProfileRecord point )
  {
    return formatTooltip( point, m_domainComponent, m_targetRangeProperty, IWspmConstants.POINT_PROPERTY_CODE );
  }

  public static String formatTooltip( final IProfileRecord point, final String domainComponentID, final String targetComponentID, final String... additionsComponentIDs )
  {
    final IProfile profile = point.getProfile();

    final IComponent domainComponent = profile.hasPointProperty( domainComponentID );
    final IComponent targetComponent = profile.hasPointProperty( targetComponentID );
    if( Objects.isNull( point, targetComponent, domainComponent ) )
      return StringUtils.EMPTY;

    try
    {
      final TooltipFormatter formatter = new TooltipFormatter( null, new String[] { "%s", "%s", "%s" }, new int[] { SWT.LEFT, SWT.RIGHT, SWT.LEFT } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

      final String domainUnit = ComponentUtilities.getComponentUnitLabel( domainComponent );
      final Object domainValue = point.getValue( domainComponent );
      formatter.addLine( domainComponent.getName(), String.format( "%10.2f", domainValue ), domainUnit ); //$NON-NLS-1$

      final String targetUnit = ComponentUtilities.getComponentUnitLabel( targetComponent );
      final Object targetValue = point.getValue( targetComponent );
      formatter.addLine( targetComponent.getName(), String.format( "%10.2f", targetValue ), targetUnit ); //$NON-NLS-1$

      /* code if set */
      for( final String additionalComponentID : additionsComponentIDs )
      {
        final IComponent additionalComponent = profile.hasPointProperty( additionalComponentID );
        if( additionalComponent != null )
        {
          final Object additionalValue = point.getValue( additionalComponent );
          if( additionalValue != null )
          {
            final String additionalString = additionalValue.toString();
            final String additionalUnit = ComponentUtilities.getComponentUnitLabel( additionalComponent );

            if( !StringUtils.isBlank( additionalString ) )
              formatter.addLine( additionalComponent.getName(), additionalString, additionalUnit );
          }
        }
      }

      /* comment if set */
      final IComponent commentComponent = profile.hasPointProperty( IWspmConstants.POINT_PROPERTY_COMMENT );
      if( commentComponent != null )
      {
        final String comment = point.getComment();
        if( !StringUtils.isBlank( comment ) )
          formatter.addFooter( comment );
      }

      return formatter.format();
    }
    catch( final RuntimeException e )
    {
      return e.getLocalizedMessage();
    }
  }

  @Override
  public void onProfilChanged( final ProfileChangeHint hint )
  {
    if( hint.isPointPropertiesChanged() )
      m_targetPropIndex = -1;

    super.onProfilChanged( hint );
  }

  protected Point toScreen( final IProfileRecord point )
  {
    final ICoordinateMapper cm = getCoordinateMapper();
    if( Objects.isNull( cm ) )
      return null;

    final Double x = ProfileUtil.getDoubleValueFor( m_domainComponent, point );
    final Double y = ProfileUtil.getDoubleValueFor( getTargetPropertyIndex(), point );
    if( Objects.isNull( x, y ) )
      return null;
    else if( x.isNaN() || y.isNaN() )
      return null;

    return cm.numericToScreen( x, y );
  }
}
