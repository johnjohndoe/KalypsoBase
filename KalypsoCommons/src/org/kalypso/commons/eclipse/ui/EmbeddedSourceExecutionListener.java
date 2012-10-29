package org.kalypso.commons.eclipse.ui;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * This listeners makes sure that if a command is executed, the right source is injected into the evaluation context.<br/>
 * This happens only if this listener is responsible for that source.
 * 
 * @author Gernot Belger
 */
public final class EmbeddedSourceExecutionListener implements IExecutionListener
{
  private final String[] m_commands;

  private final IHandlerService m_handlerService;

  private final ToolBar m_toolbar;

  private final String m_sourceName;

  private final Object m_sourceValue;

  public EmbeddedSourceExecutionListener( final String[] commands, final IHandlerService handlerService, final ToolBar toolbar, final String sourceName, final Object sourceValue )
  {
    m_commands = commands;
    m_handlerService = handlerService;
    m_toolbar = toolbar;
    m_sourceName = sourceName;
    m_sourceValue = sourceValue;
  }

  @Override
  public void notHandled( final String commandId, final NotHandledException exception )
  {
  }

  @Override
  public void preExecute( final String commandId, final ExecutionEvent event )
  {
    if( !ArrayUtils.contains( m_commands, commandId ) )
      return;

    final ToolBar parentToolbar = findToolbar( event );

    if( parentToolbar == m_toolbar )
    {
      final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
      context.addVariable( m_sourceName, m_sourceValue );
    }
  }

  private ToolBar findToolbar( final ExecutionEvent event )
  {
    final Event trigger = (Event) event.getTrigger();

    if( trigger.widget instanceof ToolItem )
    {
      final ToolItem toolItem = (ToolItem) trigger.widget;
      final ToolBar parentToolbar = toolItem.getParent();
      return parentToolbar;
    }

    if( trigger.widget instanceof ToolBar )
      return (ToolBar) trigger.widget;

    throw new IllegalArgumentException();
  }

  @Override
  public void postExecuteFailure( final String commandId, final ExecutionException exception )
  {
    if( !ArrayUtils.contains( m_commands, commandId ) )
      return;

    final IEvaluationContext currentState = m_handlerService.getCurrentState();
    currentState.removeVariable( m_sourceName );
  }

  @Override
  public void postExecuteSuccess( final String commandId, final Object returnValue )
  {
    if( !ArrayUtils.contains( m_commands, commandId ) )
      return;

    final IEvaluationContext currentState = m_handlerService.getCurrentState();
    currentState.removeVariable( m_sourceName );
  }
}