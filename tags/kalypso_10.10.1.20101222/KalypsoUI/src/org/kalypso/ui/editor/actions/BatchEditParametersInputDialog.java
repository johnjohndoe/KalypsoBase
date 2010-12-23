package org.kalypso.ui.editor.actions;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.i18n.Messages;

/**
 * Input dialog for selecting the action to take
 */
final class BatchEditParametersInputDialog extends TitleAreaDialog
{
  private String m_op = "+"; //$NON-NLS-1$

  private double m_amount;

  private final String m_windowTitle;

  /**
   * Creates a new input dialog
   */
  public BatchEditParametersInputDialog( final Shell shell, final String windowTitle, double focusedValue )
  {
    super( shell );
    m_windowTitle = windowTitle;
    m_amount = focusedValue;
  }

  /**
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell( Shell newShell )
  {
    super.configureShell( newShell );

    newShell.setText( m_windowTitle );
  }

  @Override
  protected Control createDialogArea( final Composite parent )
  {
    final Composite composite = (Composite) super.createDialogArea( parent );

    final Composite panel = new Composite( composite, SWT.NONE );
    panel.setLayout( new GridLayout() );
    panel.setLayoutData( new GridData( GridData.FILL_BOTH ) );

    final Text text = new Text( panel, SWT.BORDER );
    text.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    if( !Double.isNaN( m_amount ) )
      text.setText( "" + m_amount ); //$NON-NLS-1$
    text.addModifyListener( new ModifyListener()
    {
      @Override
      public void modifyText( ModifyEvent e )
      {
        handleTextChanged( text.getText() );
      }
    } );

    final Group radioButtonGroup = new Group( panel, SWT.SHADOW_ETCHED_IN );
    radioButtonGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    radioButtonGroup.setText( Messages.getString( "org.kalypso.ui.editor.actions.FeatureBatchEditActionDelegate.3" ) ); //$NON-NLS-1$
    radioButtonGroup.setLayout( new GridLayout() );

    addBtton( radioButtonGroup, "=", Messages.getString( "org.kalypso.ui.editor.actions.FeatureBatchEditActionDelegate.4" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    addBtton( radioButtonGroup, "+", Messages.getString( "org.kalypso.ui.editor.actions.FeatureBatchEditActionDelegate.7" ) ).setSelection( true ); //$NON-NLS-1$ //$NON-NLS-2$
    addBtton( radioButtonGroup, "-", Messages.getString( "org.kalypso.ui.editor.actions.FeatureBatchEditActionDelegate.9" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    addBtton( radioButtonGroup, "*", Messages.getString( "org.kalypso.ui.editor.actions.FeatureBatchEditActionDelegate.11" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    addBtton( radioButtonGroup, "/", Messages.getString( "org.kalypso.ui.editor.actions.FeatureBatchEditActionDelegate.12" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    setTitle( Messages.getString("org.kalypso.ui.editor.actions.BatchEditParametersInputDialog.0") ); //$NON-NLS-1$
    setMessage( Messages.getString( "org.kalypso.ui.editor.actions.FeatureBatchEditActionDelegate.1" ) );//$NON-NLS-1$ 

    return composite;
  }

  protected void handleTextChanged( String text )
  {
    m_amount = NumberUtils.parseQuietDouble( text );

    Button button = getButton( IDialogConstants.OK_ID );
    button.setEnabled( !Double.isNaN( m_amount ) );
  }

  public String getOperator( )
  {
    return m_op;
  }

  protected void setOperator( String operator )
  {
    m_op = operator;
  }

  public double getAmount( )
  {
    return m_amount;
  }

  public Button addBtton( final Composite parent, final String operation, final String tooltip )
  {
    final Button button = new Button( parent, SWT.RADIO );
    button.setText( operation );
    button.setToolTipText( tooltip );
    button.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( SelectionEvent e )
      {
        setOperator( operation );
      }
    } );

    return button;
  }
}