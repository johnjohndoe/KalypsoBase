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
import org.kalypso.contribs.eclipse.swt.widgets.ITextCompositeEventListener.MODIFY_EVENT;

public class TextComposite extends Composite
{
  private final FormToolkit m_toolkit;

  private final int m_style;

  private ImageHyperlink m_image;

  protected String m_string;

  protected Text m_text;

  protected boolean m_runSelectionChangeListener = true;

  protected ITextCompositeValidator m_validator;

  Set<ITextCompositeEventListener> m_listeners = new LinkedHashSet<ITextCompositeEventListener>();

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
    layout.horizontalSpacing = layout.verticalSpacing = 0;
    setLayout( layout );

    paint( widthHint );

    if( toolkit != null )
      toolkit.adapt( this );
  }

  public TextComposite( final Composite parent, final int style, final int widthHint )
  {
    this( null, parent, style, widthHint );
  }

  public void setValidator( final ITextCompositeValidator validator )
  {
    m_validator = validator;

    m_validator.check( m_string, MODIFY_EVENT.eModify );
  }

  private void paint( final int widthHint )
  {
    if( m_toolkit == null )
      m_text = new Text( this, m_style );
    else
      m_text = m_toolkit.createText( this, "", m_style );

    final GridData data = new GridData( GridData.FILL, GridData.FILL, true, false );
    if( widthHint != -1 )
      data.widthHint = widthHint;

    m_text.setLayoutData( data );

    if( m_toolkit == null )
      m_image = new ImageHyperlink( this, SWT.NONE );
    else
      m_image = m_toolkit.createImageHyperlink( this, SWT.NONE );

// GridData imgData = new GridData( GridData.FILL, GridData.FILL, false, false );
// imgData.heightHint = imgData.widthHint = 16;
// m_image.setLayoutData( imgData );

    m_text.addModifyListener( new ModifyListener()
    {
      public void modifyText( final ModifyEvent e )
      {
        m_string = m_text.getText();

        update( MODIFY_EVENT.eModify );
      }
    } );

    m_text.addFocusListener( new FocusAdapter()
    {
      @Override
      public void focusLost( final FocusEvent e )
      {
        update( MODIFY_EVENT.eFocusLost );
      }
    } );

  }

  public void update( final MODIFY_EVENT type )
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
      for( final ITextCompositeEventListener runnable : m_listeners )
      {
        runnable.run( type );
      }
    }
  }

  public void setText( final String text )
  {
    m_text.setText( text );

    update( MODIFY_EVENT.eFocusLost );
  }

  public String getText( )
  {
    return m_string;
  }

  public void addModifyListener( final ITextCompositeEventListener runnable )
  {
    m_listeners.add( runnable );
  }
}
