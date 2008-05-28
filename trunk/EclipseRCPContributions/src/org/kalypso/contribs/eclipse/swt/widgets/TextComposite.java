package org.kalypso.contribs.eclipse.swt.widgets;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.kalypso.contribs.eclipse.swt.widgets.ITextBoxValidator.EVENT_TYPE;

public class TextComposite extends Composite
{
  private final FormToolkit m_toolkit;

  private final int m_style;

  private ImageHyperlink m_image;

  protected String m_string;

  private Text m_text;

  protected boolean m_runSelectionChangeListener = true;

  protected ITextBoxValidator m_validator;

  Set<Runnable> m_listeners = new LinkedHashSet<Runnable>();

  private static final Image m_icon = new Image( null, TextComposite.class.getResourceAsStream( "icons/error.gif" ) );;

  static public final Color COLOR_WHITE = new Color( null, 255, 255, 255 );

  static public final Color COLOR_RED = new Color( null, 255, 24, 24 );

  public TextComposite( final FormToolkit toolkit, final Composite parent, final int style, final int widthHint )
  {
    super( parent, SWT.NULL );
    m_toolkit = toolkit;
    m_style = style;

    final GridLayout layout = new GridLayout( 2, false );
    layout.marginWidth = 0;
    setLayout( layout );

    paint( widthHint );

    toolkit.adapt( this );
  }

  public void setValidator( final ITextBoxValidator validator )
  {
    m_validator = validator;

    m_validator.check( m_string, EVENT_TYPE.eModify );
  }

  private void paint( final int widthHint )
  {
    m_text = m_toolkit.createText( this, "", m_style );
    final GridData data = new GridData( GridData.FILL, GridData.FILL, true, false );
    data.widthHint = widthHint;
    m_text.setLayoutData( data );

    m_image = m_toolkit.createImageHyperlink( this, SWT.NONE );

    m_text.addModifyListener( new ModifyListener()
    {
      public void modifyText( final ModifyEvent e )
      {
        m_string = m_text.getText();

        update( EVENT_TYPE.eModify );
      }
    } );

    m_text.addFocusListener( new FocusAdapter()
    {
      @Override
      public void focusLost( final FocusEvent e )
      {
        update( EVENT_TYPE.eFocusLost );
      }
    } );

  }

  protected void update( final EVENT_TYPE type )
  {

    if( m_validator != null )
    {
      if( m_validator.check( m_string, type ) )
      {

        m_runSelectionChangeListener = true;

        m_image.setImage( null );
        m_image.setToolTipText( "" );

        m_text.setBackground( COLOR_WHITE );
      }
      else
      {
        m_runSelectionChangeListener = false;

        m_image.setImage( m_icon );
        m_image.setToolTipText( m_validator.getToolTip() );

        m_text.setBackground( COLOR_RED );
      }

      this.layout();
    }

    if( m_runSelectionChangeListener )
    {
      for( final Runnable runnable : m_listeners )
      {
        runnable.run();
      }
    }
  }

  public void setText( final String text )
  {
    m_text.setText( text );

    update( EVENT_TYPE.eFocusLost );
  }

  public String getText( )
  {
    return m_string;
  }

  public void addModifyListener( final Runnable runnable )
  {
    m_listeners.add( runnable );
  }
}
