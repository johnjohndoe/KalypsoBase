package org.bce.eclipse.jface.action;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author gernot
 * 
 */
public class DropdownContributionItem<T> extends ControlContribution
{
  private final Collection<SelectionListener> m_listeners = new LinkedList<SelectionListener>();

  private T[] m_items;

  private Combo m_combo;

  private T m_item;

  public DropdownContributionItem( final T[] items )
  {
    this( items, null );
  }

  public DropdownContributionItem( final T[] items, final String id )
  {
    super( id );

    setItems ( items );
  }

  public void dispose( )
  {
    m_listeners.clear();
  }

  /**
   * @see org.eclipse.jface.action.ControlContribution#createControl(org.eclipse.swt.widgets.Composite)
   */
  protected Control createControl( final Composite parent )
  {
    m_combo = new Combo( parent, SWT.DROP_DOWN | SWT.READ_ONLY );

    if( m_items != null )
      setItems( m_items );
    
    if( m_item != null )
      setSelectedItem( m_item );
    
    for( final SelectionListener l : m_listeners )
      m_combo.addSelectionListener( l );
    m_listeners.clear();

    return m_combo;
  }

  public T[] getItems( )
  {
    return m_items;
  }

  public void setItems( final T[] items )
  {
    m_items = items;

    if( m_combo != null && !m_combo.isDisposed() )
    {
      m_combo.removeAll();
      if( m_items != null )
      {
        for( int i = 0; i < m_items.length; i++ )
          m_combo.add( m_items[i].toString() );
      }
      
      m_combo.setEnabled( m_items != null );
    }
  }

  public T getSelectedItem( )
  {
    final int index = m_combo.getSelectionIndex();
    if( index == -1 )
      return null;

    return m_items[index];
  }

  public void setSelectedItem( final T item )
  {
    m_item = item;
    
    if( m_combo == null || m_combo.isDisposed() )
      return;
    
    for( int i = 0; i < m_items.length; i++ )
    {
      if( item == m_items[i] )
      {
        m_combo.select( i );
        return;
      }
    }
  }

  public void addSelectionListener( final SelectionListener l )
  {
    if( m_combo != null )
      m_combo.addSelectionListener( l );
    else
      m_listeners.add( l );
  }

  public int getSelectionIndex( )
  {
    if( m_combo == null )
      return -1;

    return m_combo.getSelectionIndex();
  }
}
