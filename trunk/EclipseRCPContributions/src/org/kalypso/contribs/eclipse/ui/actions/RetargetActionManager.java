package org.kalypso.contribs.eclipse.ui.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.AbstractGroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.RetargetAction;
import org.kalypso.contribs.eclipse.ui.IEditorPartAction;

/**
 * Helper class to register global actions and more.
 * 
 * @author Belger
 */
public class RetargetActionManager
{
  public static final String MENU_GROUP_PUSH = "push";

  public static final String MENU_GROUP_RADIO = "radio";

  public static final String MENU_GROUP_CHECK = "check";
  
  public static final String MENU_GROUP_MENU = "menu";

  public static final class RetargetInfo
  {
    private final RetargetAction m_retargetAction;

    private IEditorPartAction m_actionHandler = null;

    private String m_toolbarGroup = null;

    private String m_menuPath = null;

    /**
     * @param id global action id
     * @see org.eclipse.ui.actions.ActionFactory
     */
    public RetargetInfo( final String id, final String label, final int style )
    {
      m_retargetAction = new RetargetAction( id, label, style );
    }
    
    public RetargetInfo( final RetargetAction retargetAction )
    {
      m_retargetAction = retargetAction;
    }

    public IEditorPartAction getActionHandler( )
    {
      return m_actionHandler;
    }

    public void setActionHandler( final IEditorPartAction actionHandler )
    {
      m_actionHandler = actionHandler;
    }

    public String getMenuPath( )
    {
      return m_menuPath;
    }

    public void setMenuPath( final String menuPath )
    {
      m_menuPath = menuPath;
    }

    public RetargetAction getRetargetAction( )
    {
      return m_retargetAction;
    }

    public String getID( )
    {
      return m_retargetAction.getId();
    }

    /**
     * @return Returns the toolbarGroup.
     */
    public String getToolbarGroup( )
    {
      return m_toolbarGroup;
    }

    /**
     * @param toolbarGroup
     *          The toolbarGroup to set.
     */
    public void setToolbarGroup( final String toolbarGroup )
    {
      m_toolbarGroup = toolbarGroup;
    }
  }

  private Map<String, RetargetInfo> m_retargetMap = new HashMap<String, RetargetInfo>();

  public RetargetInfo addRetargetInfo( final RetargetInfo info )
  {
    m_retargetMap.put( info.getID(), info );

    return info;
  }

  public void registerGlobalActionHandlers( final IActionBars bars )
  {
    for( final Map.Entry<String, RetargetInfo> entry : m_retargetMap.entrySet() )
    {
      final String id = entry.getKey();
      final IAction action = entry.getValue().getActionHandler();
      if( action != null )
        bars.setGlobalActionHandler( id, action );
    }
  }

  public void addPartListeners( final IWorkbenchPage page )
  {
    for( final Map.Entry<String, RetargetInfo> entry : m_retargetMap.entrySet() )
      page.addPartListener( entry.getValue().getRetargetAction() );
  }

  public void partActivated( final IWorkbenchPart activePart )
  {
    for( final Map.Entry<String, RetargetInfo> entry : m_retargetMap.entrySet() )
      entry.getValue().getRetargetAction().partActivated( activePart );
  }

  public void setActiveEditor( final IEditorPart targetEditor )
  {
    for( final Map.Entry<String, RetargetInfo> entry : m_retargetMap.entrySet() )
    {
      final IEditorPartAction actionHandler = entry.getValue().getActionHandler();
      if( actionHandler != null )
        actionHandler.setEditorPart( targetEditor );
    }
  }

  public void contributeToToolBar( final IToolBarManager toolBarManager )
  {
    toolBarManager.add( new Separator( MENU_GROUP_CHECK ) );
    toolBarManager.add( new Separator( MENU_GROUP_PUSH ) );
    toolBarManager.add( new Separator( MENU_GROUP_RADIO ) );
    toolBarManager.add( new Separator( MENU_GROUP_MENU ) );
    toolBarManager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );

    for( final Map.Entry<String, RetargetInfo> entry : m_retargetMap.entrySet() )
    {
      final RetargetInfo info = entry.getValue();
      final String toolbarGroup = info.getToolbarGroup();
      if( toolbarGroup != null )
        toolBarManager.appendToGroup( toolbarGroup, info.getRetargetAction() );
    }
  }

  public void contributeToMenu( final IMenuManager menuManager )
  {
    for( final Map.Entry<String, RetargetInfo> entry : m_retargetMap.entrySet() )
    {
      final RetargetInfo info = entry.getValue();
      final String menuPath = info.getMenuPath();
      if( menuPath != null )
      {
        final IContributionItem item = menuManager.findUsingPath( menuPath );
        final RetargetAction retargetAction = info.getRetargetAction();
        if( item instanceof IMenuManager )
          ((IMenuManager) item).add( retargetAction );
        else if( item instanceof AbstractGroupMarker )
        {
          final AbstractGroupMarker gm = (AbstractGroupMarker) item;
          final IContributionManager parent = gm.getParent();
          parent.appendToGroup( gm.getGroupName(), retargetAction );
        }
      }
    }
  }

  public void disposeActions( final IActionBars bars, final IWorkbenchPage page )
  {
    for( final Map.Entry<String, RetargetInfo> entry : m_retargetMap.entrySet() )
    {
      final String id = entry.getKey();
      final RetargetInfo info = entry.getValue();

      page.removePartListener( info.getRetargetAction() );

      bars.getToolBarManager().remove( id );

      final String menuPath = info.getMenuPath();
      if( menuPath != null )
      {
        final IContributionItem item = bars.getMenuManager().findUsingPath( menuPath );
        if( item instanceof IMenuManager )
          ((IMenuManager) item).remove( id );
        else if( item instanceof AbstractGroupMarker )
        {
          final AbstractGroupMarker gm = (AbstractGroupMarker) item;
          final IContributionManager parent = gm.getParent();
          parent.remove( id );
        }
      }
    }
  }

  public void fillContextMenu( final IMenuManager manager )
  {
    manager.add( new Separator( MENU_GROUP_CHECK ) );
    manager.add( new Separator( MENU_GROUP_RADIO ) );
    manager.add( new Separator( MENU_GROUP_PUSH ) );
    manager.add( new Separator( MENU_GROUP_MENU ) );
    manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );

    for( final Map.Entry<String, RetargetInfo> entry : m_retargetMap.entrySet() )
    {
      final RetargetInfo info = entry.getValue();

      final String group = menuGroupForStyle( info.getRetargetAction().getStyle() );
      manager.appendToGroup( group, info.getRetargetAction() );
    }
  }

  public static String menuGroupForStyle( final int style )
  {
    switch( style )
    {
      case IAction.AS_PUSH_BUTTON:
        return MENU_GROUP_PUSH;

      case IAction.AS_CHECK_BOX:
        return MENU_GROUP_CHECK;

      case IAction.AS_RADIO_BUTTON:
        return MENU_GROUP_RADIO;
      case IAction.AS_DROP_DOWN_MENU:
        return MENU_GROUP_MENU;
    }

    return null;
  }
}
