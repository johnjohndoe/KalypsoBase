package org.kalypso.model.wspm.ui.action.selection;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;

import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.utilities.tooltip.ToolTipRenderer;
import org.kalypso.ogc.gml.widgets.AbstractWidget;

/**
 * @author Dirk Kuch
 */
public class ProfileSelctionWidget extends AbstractWidget
{
  private final ToolTipRenderer m_tooltip = ToolTipRenderer.createStandardTooltip();

  private final SelectedProfilesMapPanelListener m_mapPanelListener = new SelectedProfilesMapPanelListener( this );

  /**
   * The current point on the map screen.
   */
  private Point m_currentPoint;

  private IProfil[] m_profiles;

  public ProfileSelctionWidget( )
  {
    super( "", "" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * This function resets the widget.
   */
  private void reset( )
  {
// if( m_strategy != null )
// {
// m_strategy.dispose();
// m_strategy = null;
// }
    m_currentPoint = null;

    final Cursor cursor = Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR );
    getMapPanel().setCursor( cursor );
  }

  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {
    super.activate( commandPoster, mapPanel );

    mapPanel.addSelectionChangedListener( m_mapPanelListener );
    m_profiles = m_mapPanelListener.doSelection( mapPanel.getSelection() );

    reset();

    /* Init the cursor. */
    final Cursor cursor = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR );
    mapPanel.setCursor( cursor );
  }

  @Override
  public void finish( )
  {
    getMapPanel().removeSelectionChangedListener( m_mapPanelListener );
    reset();

    repaintMap();

    super.finish();
  }

  @Override
  public void leftPressed( final Point p )
  {
// if( m_strategy == null )
// return;
//
// try
// {
// final GM_Point pos = MapUtilities.transform( getMapPanel(), p );
// m_strategy.addPoint( pos );
// repaintMap();
// }
// catch( final Exception e )
// {
// e.printStackTrace();
//
// /* Reset the widget. */
// activate( getCommandTarget(), getMapPanel() );
// }
  }

  @Override
  public void moved( final Point p )
  {
    m_currentPoint = p;

    repaintMap();
  }

  @Override
  public void paint( final Graphics g )
  {
// final IMapPanel mapPanel = getMapPanel();
// if( mapPanel == null )
// return;
//
// if( m_strategy != null )
// {
// m_strategy.paint( g, mapPanel, m_currentPoint );
// }
//
// if( m_tooltip != null )
// {
// final Rectangle bounds = mapPanel.getScreenBounds();
//
// if( m_strategy != null )
// {
// m_standardTooltip.setTooltip( STR_DEFAULT_TOOLTIP + m_strategy.getLabel() );
// }
//
// m_tooltip.paintToolTip( new Point( 5, bounds.height - 5 ), g, bounds );
// }
  }

  @Override
  public void keyReleased( final KeyEvent e )
  {
    final int keyCode = e.getKeyCode();
    switch( keyCode )
    {
      case KeyEvent.VK_ESCAPE:
// activate( getCommandTarget(), getMapPanel() );
// repaintMap();
        break;

      case KeyEvent.VK_BACK_SPACE:
// if( m_strategy != null )
// {
// m_strategy.removeLastPoint();
// repaintMap();
// }
        break;

      case KeyEvent.VK_SPACE:
// m_strategyExtendProfile = !m_strategyExtendProfile;
// final IDialogSettings settings = getSettings();
// if( settings != null )
// {
// settings.put( SETTINGS_MODE, m_strategyExtendProfile );
// }
// activate( getCommandTarget(), getMapPanel() );
// repaintMap();
        break;
    }
  }

  public void onSelectionChange( final IProfil[] profiles )
  {
    m_profiles = profiles;
  }

}