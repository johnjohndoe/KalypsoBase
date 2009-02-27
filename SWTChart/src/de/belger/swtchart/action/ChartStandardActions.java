package de.belger.swtchart.action;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IMemento;

import de.belger.swtchart.ChartCanvas;
import de.belger.swtchart.axis.AxisRange;
import de.belger.swtchart.mouse.DragHandler;
import de.belger.swtchart.mouse.EditHandler;

/**
 * Handler of the common actions on a chart Should be disposed.
 */
public class ChartStandardActions
{
  public static enum Action
  {
    EDIT, ZOOM_IN, ZOOM_OUT, PAN
  }
  
  private final Map<Action, IAction> m_actionMap = new HashMap<Action, IAction>( 10 );

  private final DragHandler m_dragHandler;

  private final EditHandler m_editHandler;

  public ChartStandardActions( final ChartCanvas chart, final AxisRange domainRange,
      final AxisRange valueRange )
  {
    // create the actions
    m_dragHandler = new DragHandler( chart );
    m_editHandler = new EditHandler( chart );

    final IChartDragAction zoomAction = new ZoomInDragAction( chart, new AxisRange[]
    { domainRange, valueRange } );
    final IChartDragAction zoomoutAction = new ZoomOutDragAction( chart, new AxisRange[]
    { domainRange } );
    final IChartDragAction panAction = new PanDragAction( chart, new AxisRange[]
    { domainRange, valueRange } );

    m_actionMap.put( Action.ZOOM_IN, new SetDragAction( m_dragHandler, zoomAction, "Zoom" ) );
    m_actionMap
        .put( Action.ZOOM_OUT, new SetDragAction( m_dragHandler, zoomoutAction, "Zoom out" ) );
    m_actionMap.put( Action.PAN, new SetDragAction( m_dragHandler, panAction, "Pan" ) );

    final EditHandlerAction editAction = new EditHandlerAction( m_editHandler );
    m_actionMap.put( Action.EDIT, editAction );
  }

  public void dispose( )
  {
    m_dragHandler.dispose();
    m_editHandler.dispose();
  }

  public IAction getAction( final Action key )
  {
    return m_actionMap.get( key );
  }

  public void saveState( final IMemento memento, final String childID )
  {
    for( final ChartStandardActions.Action action : ChartStandardActions.Action.values() )
    {
      final IMemento actionMem = memento.createChild( childID, action.name() );
      final boolean checked = getAction( action ).isChecked();
      actionMem.putString( "checkState", Boolean.toString( checked ) );
    }
  }

  public void restoreState( final IMemento memento, final String childID )
  {
    final IMemento[] checkChildren = memento.getChildren( childID );
    for( int i = 0; i < checkChildren.length; i++ )
    {
      final IMemento checkMemento = checkChildren[i];
      final String id = checkMemento.getID();
      final Action action = ChartStandardActions.Action.valueOf( id );
      final String checkState = checkMemento.getString( "checkState" );
      final boolean checked = Boolean.parseBoolean( checkState );

      final IAction realAction = getAction( action );
      
      // dont deactivate the dragactions 
      if( realAction instanceof SetDragAction && !checked )
        continue;
      
      realAction.setChecked( checked );
    }
  }
}
