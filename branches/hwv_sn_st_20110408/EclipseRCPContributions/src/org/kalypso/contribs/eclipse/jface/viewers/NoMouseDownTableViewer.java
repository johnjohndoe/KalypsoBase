/**
 * 
 */
package org.kalypso.contribs.eclipse.jface.viewers;

import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <p>
 * This is a {@link org.eclipse.jface.viewers.TableViewer} which has the mouseDownd-Event unhooked.
 * </p>
 * <p>
 * The effekt is, that no editing occurs if you click at it.
 * </p>
 * <p>
 * Usefull in combination with {@link org.eclipse.swt.custom.TableCursor}'s
 * </p>
 * 
 * @author Belger
 */
public final class NoMouseDownTableViewer extends TableViewer
{
  public NoMouseDownTableViewer( final Composite parent, int style )
  {
    super( parent, style );
  }

  @Override
  protected void hookControl( final Control control )
  {
    super.hookControl( control );
    
    // want we want to to is:
    // StructuredViewer.super.hookControl( control );
    // but this is not allowed
    
    // so we copy everything from my super classes except from TableViewer
    
    // from ContentViewer
    control.addDisposeListener( new DisposeListener()
    {
      @Override
      @SuppressWarnings( "synthetic-access" )
      public void widgetDisposed( DisposeEvent event )
      {
        handleDispose( event );
      }
    } );

    // from StructuredViewer
    final OpenStrategy handler = new OpenStrategy( control );
    handler.addSelectionListener( new SelectionListener()
    {
      @Override
      @SuppressWarnings( "synthetic-access" )
      public void widgetSelected( SelectionEvent e )
      {
        handleSelect( e );
      }

      @Override
      @SuppressWarnings( "synthetic-access" )
      public void widgetDefaultSelected( SelectionEvent e )
      {
        handleDoubleSelect( e );
      }
    } );
    handler.addPostSelectionListener( new SelectionAdapter()
    {
      @Override
      @SuppressWarnings( "synthetic-access" )
      public void widgetSelected( SelectionEvent e )
      {
        handlePostSelect( e );
      }
    } );
    
    // cannot call, because handleOpen is private
    // this is only important for single click support
    
    // handler.addOpenListener(new IOpenEventListener() {
    // public void handleOpen(SelectionEvent e) {
    // StructuredViewer.this.handleOpen(e);
    // }
    // });
  }
}