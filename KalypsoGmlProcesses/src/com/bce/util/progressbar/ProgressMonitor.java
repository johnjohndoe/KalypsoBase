package com.bce.util.progressbar;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.kalypso.gml.processes.i18n.Messages;

/**
 * Eine alternative zum javax.swing.ProgressMonitor
 * 
 * @author belger
 */
public class ProgressMonitor implements Progressable
{
  private boolean m_canceled = false;

  private JDialog m_dialog;

  private JProgressBar m_progressBar = new JProgressBar();

  private JLabel m_noteField = new JLabel();

  private JOptionPane m_optionPane;

  private static final String CANCEL = Messages.getString("com.bce.util.progressbar.ProgressMonitor.0"); //$NON-NLS-1$

  /**
   * -
   * 
   * @param parent -
   * @param title -
   */
  public ProgressMonitor( final Component parent, final String title )
  {
    m_optionPane = new JOptionPane( new Object[] { m_noteField, m_progressBar }, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new String[] { CANCEL }, null );

    m_dialog = createDialog( parent, title, m_optionPane );
    m_dialog.setVisible( true );
  }

  /**
   * -
   * 
   * @param parentComponent -
   * @param title -
   * @param op -
   * @return -
   */
  public JDialog createDialog( final Component parentComponent, final String title, final JOptionPane op )
  {
    Frame frame = JOptionPane.getFrameForComponent( parentComponent );
    final JDialog dialog = new JDialog( frame, title, false );
    Container contentPane = dialog.getContentPane();

    contentPane.setLayout( new BorderLayout() );
    contentPane.add( m_optionPane, BorderLayout.CENTER );
    dialog.pack();
    dialog.setLocationRelativeTo( parentComponent );
    dialog.addWindowListener( new WindowAdapter()
    {
      boolean gotFocus = false;

      @Override
      public void windowClosing( WindowEvent we )
      {
        op.setValue( CANCEL );
      }

      @Override
      public void windowActivated( WindowEvent we )
      {
        // Once window gets focus, set initial focus
        if( !gotFocus )
        {
          op.selectInitialValue();
          gotFocus = true;
        }
      }
    } );

    m_optionPane.addPropertyChangeListener( new PropertyChangeListener()
    {
      public void propertyChange( PropertyChangeEvent event )
      {
        if( dialog.isVisible() && event.getSource() == op && (event.getPropertyName().equals( JOptionPane.VALUE_PROPERTY ) || event.getPropertyName().equals( JOptionPane.INPUT_VALUE_PROPERTY )) )
          cancel();
      }
    } );

    return dialog;
  }

  /**
   * @see com.bce.util.progressbar.Progressable#setNote(java.lang.String)
   */
  public void setNote( final String note )
  {
    m_noteField.setText( note );
    // m_dialog.pack();
  }

  /**
   * @see com.bce.util.progressbar.Progressable#reset(int, int)
   */
  public void reset( final int min, final int max )
  {
    m_progressBar.setMinimum( min );
    m_progressBar.setMaximum( max );
    m_progressBar.setValue( 0 );
  }

  /**
   * @see com.bce.util.progressbar.Progressable#setCurrent(int)
   */
  public void setCurrent( int current )
  {
    m_progressBar.setValue( current );
  }

  /**
   * @see com.bce.util.progressbar.Progressable#isCanceled()
   */
  public boolean isCanceled( )
  {
    return m_canceled;
  }

  /**
   * @see com.bce.util.progressbar.Progressable#cancel()
   */
  public void cancel( )
  {
    m_canceled = true;
    m_dialog.setVisible( false );
    m_dialog.dispose();
    m_dialog = null;
  }
}
