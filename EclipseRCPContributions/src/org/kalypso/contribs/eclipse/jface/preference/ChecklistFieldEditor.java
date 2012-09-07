/**
 *
 */
package org.kalypso.contribs.eclipse.jface.preference;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * A field editor in form of a checkable list.
 *
 * @author belger
 */
public class ChecklistFieldEditor extends FieldEditor implements ICheckStateListener
{
  private final Map<String, Object> m_idMap = new HashMap<>();

  private final Map<Object, String> m_contentMap = new HashMap<>();

  private CheckboxTableViewer m_checklist;

  private Object[] m_checkedElements;

  private final Object[] m_content;

  private Group m_group;

  public ChecklistFieldEditor( final Object[] content, final String idMethod, final ILabelProvider labelProvider, final String name, final String labelText, final Composite parent )
  {
    super( name, labelText, parent );

    m_content = content;
    if( m_checklist != null )
    {
      m_checklist.setLabelProvider( labelProvider );
      m_checklist.setInput( m_content );
    }

    // hash id -> element
    final Class< ? >[] noclasses = new Class[] {};
    final Object[] noobjects = new Object[] {};
    for( final Object element : content )
    {
      try
      {
        final Class< ? extends Object> klass = element.getClass();
        final Method method = klass.getMethod( idMethod, noclasses );
        final String id = (String) method.invoke( element, noobjects );
        m_idMap.put( id, element );
        m_contentMap.put( element, id );
      }
      catch( final Exception e )
      {
        e.printStackTrace();
      }
    }
  }

  @Override
  protected void adjustForNumColumns( final int numColumns )
  {
    ((GridData) m_group.getLayoutData()).horizontalSpan = numColumns;
  }

  @Override
  protected void doFillIntoGrid( final Composite parent, final int numColumns )
  {
    if( m_group == null )
    {
      m_group = new Group( parent, SWT.NONE );
      m_group.setLayout( new GridLayout() );
      final GridData gridData = new GridData( GridData.FILL_BOTH );
      gridData.horizontalSpan = numColumns;
      m_group.setLayoutData( gridData );
      m_group.setText( getLabelText() );

      m_checklist = CheckboxTableViewer.newCheckList( m_group, SWT.NONE );
      m_checklist.setContentProvider( new ArrayContentProvider() );
      m_checklist.addCheckStateListener( this );
      m_checklist.getControl().setLayoutData( new GridData( GridData.FILL_BOTH ) );
    }

    checkParent( m_group, parent );
  }

  @Override
  public void setFocus( )
  {
    if( m_checklist != null )
      m_checklist.getControl().setFocus();
  }

  @Override
  protected void doLoad( )
  {
    final String value = getPreferenceStore().getString( getPreferenceName() );
    uncheckElements( value );
  }

  @Override
  protected void doLoadDefault( )
  {
    final String value = getPreferenceStore().getDefaultString( getPreferenceName() );
    uncheckElements( value );
  }

  private void uncheckElements( final String value )
  {
    if( m_checklist != null )
    {
      final String[] strings = value.split( ";" );

      final List<Object> elementsToCheck = new ArrayList<>( strings.length );
      Collections.addAll( elementsToCheck, m_content );

      for( final String string : strings )
      {
        final Object object = m_idMap.get( string );
        elementsToCheck.remove( object );
      }

      m_checklist.setCheckedElements( elementsToCheck.toArray( new Object[elementsToCheck.size()] ) );
    }
  }

  @Override
  protected void doStore( )
  {
    if( m_checklist != null )
    {
      final StringBuffer selection = new StringBuffer();

      final List<Object> checked = m_checkedElements == null ? new ArrayList<>() : Arrays.asList( m_checkedElements );

      for( final Object element : m_content )
      {
        if( !checked.contains( element ) )
          selection.append( m_contentMap.get( element ) );
      }
      getPreferenceStore().setValue( getPreferenceName(), selection.toString() );
    }
  }

  @Override
  public int getNumberOfControls( )
  {
    return 1;
  }

  @Override
  public void checkStateChanged( final CheckStateChangedEvent event )
  {
    m_checkedElements = m_checklist.getCheckedElements();
  }
}
