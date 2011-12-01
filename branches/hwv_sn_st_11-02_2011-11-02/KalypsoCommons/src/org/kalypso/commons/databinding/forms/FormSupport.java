/**
 * This code was mainly copied from: {@link org.eclipse.jface.databinding.dialog.TitleAreaDialogSupport}.
 */
package org.kalypso.commons.databinding.forms;

import java.util.Iterator;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.dialog.IValidationMessageProvider;
import org.eclipse.jface.databinding.dialog.ValidationMessageProvider;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Connects the validation result from the given data binding context to the given
 * {@link org.eclipse.ui.forms.widgets.Form}, updating the form's error message accordingly.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class FormSupport
{
  /**
   * Connect the validation result from the given data binding context to the given {@link Form}.
   * 
   * @return an instance of {@link FormSupport}
   */
  public static FormSupport create( final ScrolledForm scrolledForm, final DataBindingContext dbc )
  {
    return new FormSupport( scrolledForm, scrolledForm.getForm(), dbc );
  }

  /**
   * Connect the validation result from the given data binding context to the given {@link Form}.
   * 
   * @return an instance of {@link FormSupport}
   */
  public static FormSupport create( final Form form, final DataBindingContext dbc )
  {
    return new FormSupport( null, form, dbc );
  }

  private DataBindingContext m_dbc;

  private IValidationMessageProvider m_messageProvider = new ValidationMessageProvider();

  private IObservableValue m_aggregateStatusProvider;

  private boolean m_uiChanged = false;

  private IChangeListener m_uiChangeListener = new IChangeListener()
  {
    @Override
    public void handleChange( final ChangeEvent event )
    {
      handleUIChanged();
    }
  };

  private IListChangeListener m_validationStatusProvidersListener = new IListChangeListener()
  {
    @Override
    public void handleListChange( final ListChangeEvent event )
    {
      doHandleValidationStatusChanged( event.diff );
    }
  };

  private IListChangeListener m_validationStatusProviderTargetsListener = new IListChangeListener()
  {
    @Override
    public void handleListChange( final ListChangeEvent event )
    {
      doHandleValidationTargetsChange( event.diff );
    }
  };

  private ValidationStatusProvider m_currentStatusProvider;

  private IStatus m_currentStatus;

  private final ScrolledForm m_scrolledForm;

  private Form m_form;

  private FormSupport( final ScrolledForm scrolledForm, final Form form, final DataBindingContext dbc )
  {
    m_scrolledForm = scrolledForm;
    m_form = form;
    m_dbc = dbc;

    init();
  }

  /**
   * Sets the {@link IValidationMessageProvider} to use for providing the message text and message type to display on
   * the title area dialog.
   * 
   * @param messageProvider
   *          The {@link IValidationMessageProvider} to use for providing the message text and message type to display
   *          on the title area dialog.
   * @since 1.4
   */
  public void setValidationMessageProvider( final IValidationMessageProvider messageProvider )
  {
    m_messageProvider = messageProvider;
    handleStatusChanged();
  }

  private void init( )
  {
    ObservableTracker.setIgnore( true );
    try
    {
      m_aggregateStatusProvider = new MaxSeverityValidationStatusProvider( m_dbc );
    }
    finally
    {
      ObservableTracker.setIgnore( false );
    }

    m_aggregateStatusProvider.addValueChangeListener( new IValueChangeListener()
    {
      @Override
      public void handleValueChange( final ValueChangeEvent event )
      {
        statusProviderChanged();
      }
    } );

    m_form.addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        dispose();
      }
    } );

    statusProviderChanged();
    m_dbc.getValidationStatusProviders().addListChangeListener( m_validationStatusProvidersListener );
    for( final Iterator< ? > it = m_dbc.getValidationStatusProviders().iterator(); it.hasNext(); )
    {
      final ValidationStatusProvider validationStatusProvider = (ValidationStatusProvider) it.next();
      final IObservableList targets = validationStatusProvider.getTargets();
      targets.addListChangeListener( m_validationStatusProviderTargetsListener );
      for( final Iterator< ? > iter = targets.iterator(); iter.hasNext(); )
      {
        ((IObservable) iter.next()).addChangeListener( m_uiChangeListener );
      }
    }
  }

  protected void statusProviderChanged( )
  {
    m_currentStatusProvider = (ValidationStatusProvider) m_aggregateStatusProvider.getValue();
    if( m_currentStatusProvider != null )
    {
      m_currentStatus = (IStatus) m_currentStatusProvider.getValidationStatus().getValue();
    }
    else
    {
      m_currentStatus = null;
    }
    handleStatusChanged();
  }

  void handleUIChanged( )
  {
    m_uiChanged = true;
    if( m_currentStatus != null )
    {
      handleStatusChanged();
    }
    m_dbc.getValidationStatusProviders().removeListChangeListener( m_validationStatusProvidersListener );
    for( final Iterator< ? > it = m_dbc.getValidationStatusProviders().iterator(); it.hasNext(); )
    {
      final ValidationStatusProvider validationStatusProvider = (ValidationStatusProvider) it.next();
      final IObservableList targets = validationStatusProvider.getTargets();
      targets.removeListChangeListener( m_validationStatusProviderTargetsListener );
      for( final Iterator< ? > iter = targets.iterator(); iter.hasNext(); )
      {
        ((IObservable) iter.next()).removeChangeListener( m_uiChangeListener );
      }
    }
  }

  private void handleStatusChanged( )
  {
    // FIXME: instead use message manager of form

    if( m_form == null || m_form.isDisposed() )
      return;

    m_form.getMessageManager().addMessage( m_aggregateStatusProvider, null, m_validationStatusProviderTargetsListener, 0 );

    final String message = m_messageProvider.getMessage( m_currentStatusProvider );
    final int type = m_messageProvider.getMessageType( m_currentStatusProvider );
    if( type == IMessageProvider.ERROR )
    {
      if( m_uiChanged )
        m_form.setMessage( message, IMessageProvider.ERROR );
      else
        m_form.setMessage( null );

      if( m_currentStatus != null && currentStatusHasException() )
      {
        handleStatusException();
      }
    }
    else
    {
      m_form.setMessage( message, type );
    }

    if( m_scrolledForm != null )
      m_scrolledForm.reflow( false );
  }

  private boolean currentStatusHasException( )
  {
    boolean hasException = false;
    if( m_currentStatus.getException() != null )
    {
      hasException = true;
    }
    if( m_currentStatus instanceof MultiStatus )
    {
      final MultiStatus multiStatus = (MultiStatus) m_currentStatus;

      for( int i = 0; i < multiStatus.getChildren().length; i++ )
      {
        final IStatus status = multiStatus.getChildren()[i];
        if( status.getException() != null )
        {
          hasException = true;
          break;
        }
      }
    }
    return hasException;
  }

  /**
   * This is called when a Override to provide custom exception handling and reporting.
   */
  private void handleStatusException( )
  {
    if( m_currentStatus.getException() != null )
    {
      logThrowable( m_currentStatus.getException() );
    }
    else if( m_currentStatus instanceof MultiStatus )
    {
      final MultiStatus multiStatus = (MultiStatus) m_currentStatus;
      for( int i = 0; i < multiStatus.getChildren().length; i++ )
      {
        final IStatus status = multiStatus.getChildren()[i];
        if( status.getException() != null )
        {
          logThrowable( status.getException() );
        }
      }
    }
  }

  private void logThrowable( final Throwable throwable )
  {
    Policy.getLog().log( new Status( IStatus.ERROR, Policy.JFACE_DATABINDING, IStatus.OK, "Unhandled exception: " + throwable.getMessage(), throwable ) ); //$NON-NLS-1$
  }

  /**
   * Disposes of this title area dialog support object, removing any listeners it may have attached.
   */
  public void dispose( )
  {
    if( m_aggregateStatusProvider != null )
      m_aggregateStatusProvider.dispose();
    if( m_dbc != null && !m_uiChanged )
    {
      for( final Iterator< ? > it = m_dbc.getValidationStatusProviders().iterator(); it.hasNext(); )
      {
        final ValidationStatusProvider validationStatusProvider = (ValidationStatusProvider) it.next();
        final IObservableList targets = validationStatusProvider.getTargets();
        targets.removeListChangeListener( m_validationStatusProviderTargetsListener );
        for( final Iterator<?> iter = targets.iterator(); iter.hasNext(); )
        {
          ((IObservable) iter.next()).removeChangeListener( m_uiChangeListener );
        }
      }
      m_dbc.getValidationStatusProviders().removeListChangeListener( m_validationStatusProvidersListener );
    }
    m_aggregateStatusProvider = null;
    m_dbc = null;
    m_uiChangeListener = null;
    m_validationStatusProvidersListener = null;
    m_validationStatusProviderTargetsListener = null;
    m_form = null;
  }

  protected void doHandleValidationTargetsChange( final ListDiff diff )
  {
    final ListDiffEntry[] differences = diff.getDifferences();
    for( final ListDiffEntry listDiffEntry : differences )
    {
      final IObservable target = (IObservable) listDiffEntry.getElement();
      if( listDiffEntry.isAddition() )
      {
        target.addChangeListener( m_uiChangeListener );
      }
      else
      {
        target.removeChangeListener( m_uiChangeListener );
      }
    }
  }

  protected void doHandleValidationStatusChanged( final ListDiff diff )
  {
    final ListDiffEntry[] differences = diff.getDifferences();
    for( final ListDiffEntry listDiffEntry : differences )
    {
      final ValidationStatusProvider validationStatusProvider = (ValidationStatusProvider) listDiffEntry.getElement();
      final IObservableList targets = validationStatusProvider.getTargets();
      if( listDiffEntry.isAddition() )
      {
        targets.addListChangeListener( m_validationStatusProviderTargetsListener );
        for( final Iterator< ? > it = targets.iterator(); it.hasNext(); )
        {
          ((IObservable) it.next()).addChangeListener( m_uiChangeListener );
        }
      }
      else
      {
        targets.removeListChangeListener( m_validationStatusProviderTargetsListener );
        for( final Iterator< ? > it = targets.iterator(); it.hasNext(); )
        {
          ((IObservable) it.next()).removeChangeListener( m_uiChangeListener );
        }
      }
    }
  }
}