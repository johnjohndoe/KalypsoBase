package com.bce.apps.wspfli;

import jargs.gnu.CmdLineParser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.kalypso.gml.processes.i18n.Messages;

import com.bce.ext.jargs.FileOption;
import com.bce.gis.operation.hmo2fli.Hmo2Fli;
import com.bce.util.StaticFrame;

/**
 * Hauptklasse der Anwendung WspFli
 * 
 * @author belger
 */
public class WspFli extends JPanel
{
  protected final static Icon SAVE_PREFS_ICON = new ImageIcon( WspFli.class.getResource( "resource/save.gif" ) ); //$NON-NLS-1$

  protected final static Icon DELETE_ICON = new ImageIcon( WspFli.class.getResource( "resource/delete.gif" ) ); //$NON-NLS-1$

  private final static Logger LOG = Logger.getLogger( WspFli.class.getName() );

  private final static ImageIcon ICON = new ImageIcon( WspFli.class.getResource( "resource/wspmap.gif" ) ); //$NON-NLS-1$

  private final static int CODE_NO_ERROR = 0;

  private final static int CODE_CANCEL = -1;

  private final static int CODE_ARGUMENT_ERROR = -2;

  private final static int CODE_ERROR = -3;

  private final static int CODE_MEMORY = -4;

  private static final Dimension FILE_FIELD_SIZE = new Dimension( 150, 25 );

  private static final Dimension NUMBER_FIELD_SIZE = new Dimension( 40, 25 );

  protected final JFileChooser HMO_FC = new JFileChooser();

  protected final JFileChooser SHAPE_FC = new JFileChooser();

  private final static Preferences USER_PREFS = Preferences.userNodeForPackage( WspFli.class );

  private final static String PREFS_RASTER = Messages.getString("com.bce.apps.wspfli.WspFli.3"); //$NON-NLS-1$

  private final static String PREFS_ISO = Messages.getString("com.bce.apps.wspfli.WspFli.4"); //$NON-NLS-1$

  private final static String PREFS_OFFSET = Messages.getString("com.bce.apps.wspfli.WspFli.5"); //$NON-NLS-1$

  private final static String PREFS_COUNT = Messages.getString("com.bce.apps.wspfli.WspFli.6"); //$NON-NLS-1$

  private final static String PREFS_GRENZEN = Messages.getString("com.bce.apps.wspfli.WspFli.7"); //$NON-NLS-1$

  private final static String PREFS_VOLUMEN = Messages.getString("com.bce.apps.wspfli.WspFli.8"); //$NON-NLS-1$

  private final boolean m_bFliTi;

  private final JTextField m_dgmTextField = new JTextField();

  private final JTextField m_wspTextField = new JTextField();

  protected final JTextField m_shapeTextField = new JTextField();

  private final JTextField m_rasterField = new JTextField();

  protected final JRadioButton m_isoRadio = new JRadioButton( Messages.getString("com.bce.apps.wspfli.WspFli.9") ); //$NON-NLS-1$

  private final JRadioButton m_polyRadio = new JRadioButton( Messages.getString("com.bce.apps.wspfli.WspFli.10") ); //$NON-NLS-1$

  private final ButtonGroup m_radioGroup = new ButtonGroup();

  private final GrenzenModel m_grenzen = new GrenzenModel( null );

  private final JTable m_grenzenTable = new JTable();

  final JTextField m_grenzenInputField = new JTextField();

  final JComboBox m_grenzenCombo = new JComboBox();

  protected JCheckBox m_volumenCheckBox;

  private JTextField m_saveGrenzenTextField;

  private JCheckBox m_saveGrenzenCheckBox;

  /**
   * @param args
   * @param hideFiles
   * @throws java.awt.HeadlessException
   */
  public WspFli( final WspFliArgs args, final boolean hideFiles )
  {
    super( new BorderLayout() );

    m_bFliTi = args.bFliTi;

    HMO_FC.setDialogTitle( Messages.getString("com.bce.apps.wspfli.WspFli.11") ); //$NON-NLS-1$
    HMO_FC.setAcceptAllFileFilterUsed( false );
    HMO_FC.setFileFilter( new EndFileFilter( ".hmo", Messages.getString("com.bce.apps.wspfli.WspFli.13") ) ); //$NON-NLS-1$ //$NON-NLS-2$

    SHAPE_FC.setDialogTitle( Messages.getString("com.bce.apps.wspfli.WspFli.14") ); //$NON-NLS-1$
    SHAPE_FC.setAcceptAllFileFilterUsed( false );
    SHAPE_FC.setFileFilter( new EndFileFilter( ".shp", Messages.getString("com.bce.apps.wspfli.WspFli.16") ) ); //$NON-NLS-1$ //$NON-NLS-2$

    createPanel( this, hideFiles );

    final String dgmFile = args.dgmFile == null ? "" : args.dgmFile.getAbsolutePath(); //$NON-NLS-1$
    final String wspFile = args.wspFile == null ? "" : args.wspFile.getAbsolutePath(); //$NON-NLS-1$
    final String shapeBase = args.shapeBase == null ? "" : args.shapeBase; //$NON-NLS-1$
    final String rasterSize = args.rasterSize == null ? USER_PREFS.get( PREFS_RASTER, "2.0" ) : args.rasterSize.toString(); //$NON-NLS-1$
    final boolean bDoVolumen = args.bDoVolumeCalculation == null ? USER_PREFS.getBoolean( PREFS_VOLUMEN, false ) : args.bDoVolumeCalculation;
    final boolean bIso = args.bIso == null ? USER_PREFS.getBoolean( PREFS_ISO, false ) : args.bIso.booleanValue();
    final String offset = USER_PREFS.get( PREFS_OFFSET, "0.5" ); //$NON-NLS-1$

    setData( dgmFile, wspFile, shapeBase, rasterSize, bIso, bDoVolumen, args.grenzen, offset );

    if( args.grenzen == null )
    {
      final String gName = USER_PREFS.get( PREFS_GRENZEN, null );
      if( gName != null )
        fillGrenzenCombo( gName );
    }
  }

  protected void fillGrenzenCombo( final String selectName )
  {
    Object selectItem = null;

    m_grenzenCombo.removeAllItems();

    try
    {
      final String[] names = USER_PREFS.childrenNames();
      for( int i = 0; i < names.length; i++ )
      {
        final String name = names[i];

        final Preferences node = USER_PREFS.node( name );
        final int count = node.getInt( PREFS_COUNT, 0 );
        final double[] grenzen = new double[count];
        for( int c = 0; c < count; c++ )
          grenzen[c] = node.getDouble( "" + c, 0.0 ); //$NON-NLS-1$

        final GrenzenItem item = new GrenzenItem( name, grenzen );
        m_grenzenCombo.addItem( item );

        if( name.equals( selectName ) )
          selectItem = item;
      }
    }
    catch( final BackingStoreException e )
    {
      e.printStackTrace();
    }

    // select item
    if( selectItem != null )
      m_grenzenCombo.setSelectedItem( selectItem );
    else
    {
      m_grenzenCombo.setSelectedItem( null );
      m_grenzen.setData( new double[] { 0.0 } );
    }
  }

  protected void addNewGrenzen( final String name )
  {
    // neue Grenzen zu den Prefs hinzufügen
    final double[] grenzen = m_grenzen.getData();

    if( name == null || name.length() == 0 )
      return;

    final Preferences node = USER_PREFS.node( name );

    node.putInt( Messages.getString("com.bce.apps.wspfli.WspFli.23"), grenzen.length ); //$NON-NLS-1$
    for( int i = 0; i < grenzen.length; i++ )
      node.putDouble( "" + i, grenzen[i] ); //$NON-NLS-1$
  }

  protected void removeGrenzen( )
  {
    final GrenzenItem item = (GrenzenItem) m_grenzenCombo.getSelectedItem();
    final int selectedIndex = m_grenzenCombo.getSelectedIndex();
    if( item == null )
      return;

    final String name = item.name;

    try
    {
      final String[] names = USER_PREFS.childrenNames();
      for( int i = 0; i < names.length; i++ )
      {
        if( name.equals( names[i] ) )
        {
          final Preferences node = USER_PREFS.node( name );
          node.removeNode();
          fillGrenzenCombo( null );
          if( m_grenzenCombo.getItemCount() > 0 )
            m_grenzenCombo.setSelectedIndex( Math.max( selectedIndex - 1, 0 ) );
          break;
        }
      }
    }
    catch( final BackingStoreException e )
    {
      e.printStackTrace();
    }
  }

  protected void fillGrenzen( final GrenzenItem item )
  {
    m_grenzen.setData( item.grenzen );
  }

  /**
   * Die Methode wird jetzt immer aufgerufen, wenn eine Erfolgreiche Eingabe getätigt wurde.
   */
  protected void savePrefs( )
  {
    USER_PREFS.put( PREFS_RASTER, m_rasterField.getText() );
    USER_PREFS.putBoolean( PREFS_ISO, m_isoRadio.isSelected() );
    USER_PREFS.put( PREFS_OFFSET, m_grenzenInputField.getText() );
    final Object selectedItem = m_grenzenCombo.getSelectedItem();
    USER_PREFS.put( PREFS_GRENZEN, selectedItem == null ? "" : selectedItem.toString() ); //$NON-NLS-1$
    USER_PREFS.putBoolean( PREFS_VOLUMEN, m_volumenCheckBox.isSelected() );

    if( m_saveGrenzenCheckBox != null && m_saveGrenzenCheckBox.isSelected() )
    {
      final String text = m_saveGrenzenTextField.getText();
      addNewGrenzen( text );
    }
  }

  private void createPanel( final JPanel panel, final boolean hideFiles )
  {
    final Insets insets = new Insets( 5, 5, 5, 5 );
    panel.setLayout( new GridBagLayout() );

    if( !hideFiles )
    {
      final JPanel filePanel = createFilePanel( insets );
      panel.add( filePanel, new GridBagConstraints( 0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, insets, 0, 0 ) );
    }

    final JPanel parameterPanel = createParameterPanel( insets );
    panel.add( parameterPanel, new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, insets, 0, 0 ) );

    if( m_bFliTi )
    {
      final JPanel grenzenPanel = createGrenzenPanel( insets );
      panel.add( grenzenPanel, new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, insets, 0, 0 ) );
    }

    // panel.add( new JButton( new SavePrefsAction() ), new GridBagConstraints( 0, 3, 0, 0, 0.0, 1.0,
    // GridBagConstraints.LINE_START, 0, insets, 0, 0 ) );
  }

  private JPanel createFilePanel( final Insets insets )
  {
    final JPanel panel = new JPanel();
    final GridBagLayout gridBagLayout = new GridBagLayout();
    panel.setLayout( gridBagLayout );
    panel.setBorder( BorderFactory.createTitledBorder( Messages.getString("com.bce.apps.wspfli.WspFli.26") ) ); //$NON-NLS-1$

    m_dgmTextField.setEditable( false );
    // m_dgmTextField.setHorizontalAlignment( SwingConstants.TRAILING );
    m_dgmTextField.setPreferredSize( FILE_FIELD_SIZE );
    m_dgmTextField.setMinimumSize( FILE_FIELD_SIZE );
    m_dgmTextField.setToolTipText( Messages.getString("com.bce.apps.wspfli.WspFli.27") ); //$NON-NLS-1$

    m_wspTextField.setEditable( false );
    m_wspTextField.setPreferredSize( FILE_FIELD_SIZE );
    m_wspTextField.setMinimumSize( FILE_FIELD_SIZE );
    m_wspTextField.setToolTipText( Messages.getString("com.bce.apps.wspfli.WspFli.28") ); //$NON-NLS-1$

    m_shapeTextField.setEditable( false );
    m_shapeTextField.setPreferredSize( FILE_FIELD_SIZE );
    m_shapeTextField.setMinimumSize( FILE_FIELD_SIZE );
    m_shapeTextField.setToolTipText( Messages.getString("com.bce.apps.wspfli.WspFli.29") ); //$NON-NLS-1$

    panel.add( new JLabel( Messages.getString("com.bce.apps.wspfli.WspFli.30") ), new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, 0, insets, 0, 0 ) ); //$NON-NLS-1$
    panel.add( m_dgmTextField, new GridBagConstraints( 1, 0, 2, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0 ) );
    panel.add( new JButton( new HmoAction( Messages.getString("com.bce.apps.wspfli.WspFli.31"), m_dgmTextField ) ), new GridBagConstraints( 3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0 ) ); //$NON-NLS-1$

    panel.add( new JLabel( Messages.getString("com.bce.apps.wspfli.WspFli.32") ), new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, 0, insets, 0, 0 ) ); //$NON-NLS-1$
    panel.add( m_wspTextField, new GridBagConstraints( 1, 1, 2, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0 ) );
    panel.add( new JButton( new HmoAction( Messages.getString("com.bce.apps.wspfli.WspFli.33"), m_wspTextField ) ), new GridBagConstraints( 3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0 ) ); //$NON-NLS-1$

    panel.add( new JLabel( Messages.getString("com.bce.apps.wspfli.WspFli.34") ), new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, 0, insets, 0, 0 ) ); //$NON-NLS-1$
    panel.add( m_shapeTextField, new GridBagConstraints( 1, 2, 2, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0 ) );
    panel.add( new JButton( new ShapeAction() ), new GridBagConstraints( 3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, 0, insets, 0, 0 ) );

    return panel;
  }

  private JPanel createParameterPanel( final Insets insets )
  {
    final JPanel panel = new JPanel();
    panel.setLayout( new GridBagLayout() );
    panel.setBorder( BorderFactory.createTitledBorder( Messages.getString("com.bce.apps.wspfli.WspFli.35") ) ); //$NON-NLS-1$

    final String rasterTooltip = Messages.getString("com.bce.apps.wspfli.WspFli.36"); //$NON-NLS-1$
    final JLabel rasterLabel = new JLabel( Messages.getString("com.bce.apps.wspfli.WspFli.37") ); //$NON-NLS-1$
    rasterLabel.setToolTipText( rasterTooltip );
    m_rasterField.setPreferredSize( NUMBER_FIELD_SIZE );
    m_rasterField.setMinimumSize( NUMBER_FIELD_SIZE );
    m_rasterField.setToolTipText( rasterTooltip );
    m_rasterField.setHorizontalAlignment( SwingConstants.TRAILING );

    m_radioGroup.add( m_isoRadio );
    m_radioGroup.add( m_polyRadio );
    m_isoRadio.setToolTipText( Messages.getString("com.bce.apps.wspfli.WspFli.38") ); //$NON-NLS-1$
    m_polyRadio.setToolTipText( Messages.getString("com.bce.apps.wspfli.WspFli.39") ); //$NON-NLS-1$
    m_isoRadio.addChangeListener( new ChangeListener()
    {
      @Override
      public void stateChanged( final ChangeEvent e )
      {
        m_volumenCheckBox.setEnabled( !m_isoRadio.isSelected() );
      }
    } );

    m_volumenCheckBox = new JCheckBox( Messages.getString("com.bce.apps.wspfli.WspFli.40") ); //$NON-NLS-1$
    m_volumenCheckBox.setToolTipText( Messages.getString("com.bce.apps.wspfli.WspFli.41") ); //$NON-NLS-1$

    panel.add( rasterLabel, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, 0, insets, 0, 0 ) );
    panel.add( m_rasterField, new GridBagConstraints( 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, 0, insets, 0, 0 ) );

    panel.add( new JLabel( Messages.getString("com.bce.apps.wspfli.WspFli.42") ), new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, 0, insets, 0, 0 ) ); //$NON-NLS-1$

    panel.add( m_isoRadio, new GridBagConstraints( 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, 0, insets, 0, 0 ) );
    panel.add( m_polyRadio, new GridBagConstraints( 1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, 0, insets, 0, 0 ) );

    panel.add( m_volumenCheckBox, new GridBagConstraints( 2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, 0, insets, 0, 0 ) );

    panel.add( new JLabel(), new GridBagConstraints( 3, 3, 1, 1, 1.0, 1.0, GridBagConstraints.LINE_START, 0, insets, 0, 0 ) );

    return panel;
  }

  private JPanel createGrenzenPanel( final Insets insets )
  {
    final JPanel panel = new JPanel();
    panel.setLayout( new GridBagLayout() );
    panel.setBorder( BorderFactory.createTitledBorder( Messages.getString("com.bce.apps.wspfli.WspFli.43") ) ); //$NON-NLS-1$

    m_grenzenInputField.setPreferredSize( NUMBER_FIELD_SIZE );
    m_grenzenInputField.setMinimumSize( NUMBER_FIELD_SIZE );
    m_grenzenInputField.setToolTipText( Messages.getString("com.bce.apps.wspfli.WspFli.44") ); //$NON-NLS-1$
    m_grenzenInputField.setHorizontalAlignment( SwingConstants.TRAILING );

    m_grenzenTable.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
    m_grenzenTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    m_grenzenTable.setModel( m_grenzen );
    m_grenzenTable.setTableHeader( null );

    m_grenzenCombo.setEditable( false );
    m_grenzenCombo.setToolTipText( Messages.getString("com.bce.apps.wspfli.WspFli.45") ); //$NON-NLS-1$

    m_grenzenCombo.addItemListener( new ItemListener()
    {
      @Override
      public void itemStateChanged( final ItemEvent e )
      {
        if( e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof GrenzenItem )
          fillGrenzen( (GrenzenItem) e.getItem() );
      }
    } );

    final JScrollPane tableScroller = new JScrollPane( m_grenzenTable );
    // tableScroller.setPreferredSize( new Dimension( FILE_FIELD_SIZE.width, 150
    // ) );
    tableScroller.setPreferredSize( new Dimension( 0, 150 ) );

    final JTextField saveGrenzenTextField = new JTextField( Messages.getString("com.bce.apps.wspfli.WspFli.46") ); //$NON-NLS-1$
    m_saveGrenzenTextField = saveGrenzenTextField;
    saveGrenzenTextField.setEnabled( false );
    saveGrenzenTextField.setToolTipText( Messages.getString("com.bce.apps.wspfli.WspFli.47") ); //$NON-NLS-1$
    final JCheckBox saveGrenzenCheckBox = new JCheckBox( Messages.getString("com.bce.apps.wspfli.WspFli.48") ); //$NON-NLS-1$
    saveGrenzenCheckBox.setToolTipText( Messages.getString("com.bce.apps.wspfli.WspFli.49") ); //$NON-NLS-1$
    m_saveGrenzenCheckBox = saveGrenzenCheckBox;
    saveGrenzenCheckBox.addChangeListener( new ChangeListener()
    {
      @Override
      public void stateChanged( final ChangeEvent e )
      {
        final boolean selected = saveGrenzenCheckBox.isSelected();
        saveGrenzenTextField.setEnabled( selected );
        if( selected )
        {
          saveGrenzenTextField.requestFocus();
          saveGrenzenTextField.selectAll();
        }
      }
    } );

    panel.add( new JLabel( Messages.getString("com.bce.apps.wspfli.WspFli.50") ), new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, 0, insets, 0, 0 ) ); //$NON-NLS-1$
    panel.add( m_grenzenCombo, new GridBagConstraints( 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0 ) );
    panel.add( new JButton( new RemoveGrenzenAction() ), new GridBagConstraints( 2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, 0, insets, 0, 0 ) );

    panel.add( new JSeparator( SwingConstants.HORIZONTAL ), new GridBagConstraints( 0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0 ) );

    panel.add( new JLabel( Messages.getString("com.bce.apps.wspfli.WspFli.51") ), new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, insets, 0, 0 ) ); //$NON-NLS-1$
    panel.add( tableScroller, new GridBagConstraints( 1, 2, 1, 5, 1.0, 1.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, insets, 0, 0 ) );

    panel.add( new JButton( new AddBackwardAction( m_grenzenTable ) ), new GridBagConstraints( 2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0 ) );
    panel.add( m_grenzenInputField, new GridBagConstraints( 2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0 ) );
    panel.add( new JButton( new AddForwardAction( m_grenzenTable ) ), new GridBagConstraints( 2, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0 ) );
    panel.add( new JButton( new RemoveAction( m_grenzenTable ) ), new GridBagConstraints( 2, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0 ) );

    panel.add( new JSeparator( SwingConstants.HORIZONTAL ), new GridBagConstraints( 0, 7, 3, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0 ) );

    panel.add( saveGrenzenCheckBox, new GridBagConstraints( 0, 8, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, 0, insets, 0, 0 ) );
    panel.add( saveGrenzenTextField, new GridBagConstraints( 1, 8, 2, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, insets, 0, 0 ) );

    return panel;
  }

  private void setData( final String dgmFile, final String wspFile, final String shapeBase, final String rasterSize, final boolean bIso, final boolean bDoVolumen, final double[] grenzen, final String offset )
  {
    m_dgmTextField.setText( dgmFile );
    m_wspTextField.setText( wspFile );
    m_shapeTextField.setText( shapeBase );
    m_rasterField.setText( rasterSize );
    m_isoRadio.setSelected( bIso );
    m_polyRadio.setSelected( !bIso );
    m_volumenCheckBox.setSelected( bDoVolumen );
    m_grenzenInputField.setText( offset );
    m_grenzen.setData( grenzen );
    fillGrenzenCombo( null );
  }

  private class RemoveGrenzenAction extends AbstractAction
  {
    public RemoveGrenzenAction( )
    {
      super( null, DELETE_ICON );

      putValue( SHORT_DESCRIPTION, Messages.getString("com.bce.apps.wspfli.WspFli.52") ); //$NON-NLS-1$
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed( final ActionEvent e )
    {
      removeGrenzen();
    }
  }

  private class ShapeAction extends AbstractAction
  {
    public ShapeAction( )
    {
      putValue( NAME, "..." ); //$NON-NLS-1$
      putValue( SHORT_DESCRIPTION, Messages.getString("com.bce.apps.wspfli.WspFli.54") ); //$NON-NLS-1$
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed( ActionEvent e )
    {
      SHAPE_FC.setSelectedFile( new File( m_shapeTextField.getText() ) );
      if( SHAPE_FC.showSaveDialog( WspFli.this ) == JFileChooser.APPROVE_OPTION )
      {
        String name = SHAPE_FC.getSelectedFile().getAbsolutePath();
        final int index = name.lastIndexOf( '.' );
        if( index != -1 )
          name = name.substring( 0, index );

        m_shapeTextField.setText( name );
      }
    }
  }

  private class HmoAction extends AbstractAction
  {
    private final JTextField m_field;

    public HmoAction( final String tooltip, final JTextField field )
    {
      putValue( NAME, "..." ); //$NON-NLS-1$
      putValue( SHORT_DESCRIPTION, tooltip );

      m_field = field;
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      HMO_FC.setSelectedFile( new File( m_field.getText() ) );
      if( HMO_FC.showOpenDialog( WspFli.this ) == JFileChooser.APPROVE_OPTION )
        m_field.setText( HMO_FC.getSelectedFile().getAbsolutePath() );
    }
  }

  private static class EndFileFilter extends FileFilter
  {
    private final String m_desc;

    private final String m_end;

    public EndFileFilter( final String end, final String desc )
    {
      m_end = end;
      m_desc = desc;
    }

    @Override
    public boolean accept( final File f )
    {
      if( f.isDirectory() )
        return true;

      final String name = f == null ? null : f.getName();

      return name != null && name.length() > m_end.length() && name.substring( name.length() - m_end.length() ).compareToIgnoreCase( m_end ) == 0;
    }

    @Override
    public String getDescription( )
    {
      return m_desc;
    }
  }

  // private class SavePrefsAction extends AbstractAction
  // {
  // public SavePrefsAction()
  // {
  // super( "Vorgaben speichern", SAVE_PREFS_ICON );
  //
  // putValue( SHORT_DESCRIPTION, "Speichert die aktuellen Einstellungen als Vorgabe." );
  // }
  //
  // public void actionPerformed( ActionEvent e )
  // {
  // savePrefs();
  // }
  // }

  private class AddForwardAction extends AbstractAction
  {
    private final JTable m_table;

    public AddForwardAction( final JTable table )
    {
      putValue( NAME, "+" ); //$NON-NLS-1$
      putValue( SHORT_DESCRIPTION, Messages.getString("com.bce.apps.wspfli.WspFli.57") ); //$NON-NLS-1$

      m_table = table;
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      try
      {
        final String value = m_grenzenInputField.getText();
        final double val = Double.parseDouble( value );

        GrenzenModel model = (GrenzenModel) m_table.getModel();

        model.addRow( val );
        m_table.setRowSelectionInterval( model.getRowCount() - 1, model.getRowCount() - 1 );
      }
      catch( final NumberFormatException nfe )
      {
        JOptionPane.showMessageDialog( null, Messages.getString("com.bce.apps.wspfli.WspFli.58"), Messages.getString("com.bce.apps.wspfli.WspFli.59"), JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
  }

  private class AddBackwardAction extends AbstractAction
  {
    private final JTable m_table;

    public AddBackwardAction( final JTable table )
    {
      putValue( NAME, "-" ); //$NON-NLS-1$
      putValue( SHORT_DESCRIPTION, Messages.getString("com.bce.apps.wspfli.WspFli.61") ); //$NON-NLS-1$

      m_table = table;
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      try
      {
        final String value = m_grenzenInputField.getText();
        final double val = Double.parseDouble( value );

        GrenzenModel model = (GrenzenModel) m_table.getModel();

        model.addFront( val );
        m_table.setRowSelectionInterval( 0, 0 );
      }
      catch( final NumberFormatException nfe )
      {
        JOptionPane.showMessageDialog( null, Messages.getString("com.bce.apps.wspfli.WspFli.62"), Messages.getString("com.bce.apps.wspfli.WspFli.63"), JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
  }

  private static class RemoveAction extends AbstractAction
  {
    private final JTable m_table;

    public RemoveAction( final JTable dtm )
    {
      super( null, DELETE_ICON );

      putValue( SHORT_DESCRIPTION, Messages.getString("com.bce.apps.wspfli.WspFli.64") ); //$NON-NLS-1$

      m_table = dtm;
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      final int i = m_table.getSelectedRow();
      if( i != -1 )
      {
        GrenzenModel model = (GrenzenModel) m_table.getModel();
        model.removeRow( i );

        final int si = Math.max( 0, Math.min( i, model.getRowCount() - 1 ) );
        if( model.getRowCount() > 0 )
          m_table.setRowSelectionInterval( si, si );
      }
    }
  }

  public WspFliArgs getValues( ) throws NullPointerException, NumberFormatException
  {
    final File dgmFile = new File( m_dgmTextField.getText() );
    final File wspFile = new File( m_wspTextField.getText() );
    final String shapeBase = m_shapeTextField.getText();
    final BigDecimal rasterSize = new BigDecimal( m_rasterField.getText() );
    final boolean bIso = m_isoRadio.isSelected();
    final boolean doVolumen = m_volumenCheckBox.isSelected();

    if( m_grenzenTable.isEditing() )
      m_grenzenTable.getCellEditor().stopCellEditing();

    final double[] grenzen = m_bFliTi ? m_grenzen.getData() : new double[] { 0.0 };

    return new WspFliArgs( dgmFile, wspFile, shapeBase, new Double( rasterSize.doubleValue() ), new Boolean( bIso ), grenzen, m_bFliTi, doVolumen );
  }

  public static void main( final String[] args )
  {
    for( int i = 0; i < args.length; i++ )
      System.out.println( args[i] );

    // GUI Komponenten erzeugen
    try
    {
      UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
    }
    catch( ClassNotFoundException e1 )
    {
      e1.printStackTrace();
    }
    catch( InstantiationException e1 )
    {
      e1.printStackTrace();
    }
    catch( IllegalAccessException e1 )
    {
      e1.printStackTrace();
    }
    catch( UnsupportedLookAndFeelException e1 )
    {
      e1.printStackTrace();
    }

    final JFrame rootFrame = StaticFrame.getStaticFrame();
    // The next line causes NullPointerException in Constructor
    // of FileChooserDialogs (only at XP Systems)
    rootFrame.setIconImage( ICON.getImage() );

    try
    {
      final CmdLineParser parser = new CmdLineParser();
      final CmdLineParser.Option helpOption = parser.addBooleanOption( 'h', "help" ); //$NON-NLS-1$
      final CmdLineParser.Option batchOption = parser.addBooleanOption( 'b', "batch" ); //$NON-NLS-1$

      final CmdLineParser.Option dgmFileOption = parser.addOption( new FileOption( 'd', "dgm", false ) ); //$NON-NLS-1$
      final CmdLineParser.Option wspFileOption = parser.addOption( new FileOption( 'w', "wsp", false ) ); //$NON-NLS-1$
      final CmdLineParser.Option shapeBaseOption = parser.addStringOption( 's', "shape" ); //$NON-NLS-1$
      final CmdLineParser.Option rasterSizeOption = parser.addDoubleOption( 'p', "precision" ); //$NON-NLS-1$
      final CmdLineParser.Option logFileOption = parser.addOption( new FileOption( 'l', "log", false ) ); //$NON-NLS-1$
      final CmdLineParser.Option isoOption = parser.addBooleanOption( 'i', "isolines" ); //$NON-NLS-1$
      final CmdLineParser.Option volumeOption = parser.addBooleanOption( 'v', "volume" ); //$NON-NLS-1$
      // final CmdLineParser.Option fliTiOption = parser.addBooleanOption( '\ufedc', "fliTi034" );
      final CmdLineParser.Option hideFilesOption = parser.addBooleanOption( 'f', "hideFilePanel" ); //$NON-NLS-1$

      parser.parse( args, Locale.ENGLISH );

      if( parser.getOptionValue( helpOption ) != null )
      {
        LOG.info( "wspfli.exe: Copyright 2003 Björnsen Beratende Ingenieure GmbH, Maria Trost 3, Koblenz, Germany" ); //$NON-NLS-1$
        LOG.info( "wspfli.exe -d <dmgFile> -w <wspFile> -s <shapeFile> -p <rasterSize> [-l <logFile>] [-i] [-b] [-v] [-f]" ); //$NON-NLS-1$
        LOG.info( "--help,-h 						Print this information" ); //$NON-NLS-1$
        LOG.info( "--batch,-b 					Run programm in batch modus (immediately start calculation)" ); //$NON-NLS-1$
        LOG.info( "--dgm,-d 						DGM file (hmo format)" ); //$NON-NLS-1$
        LOG.info( "--wsp,-w 						WSP file (hmo format)" ); //$NON-NLS-1$
        LOG.info( "--shape,-s 					SHAPE output filename" ); //$NON-NLS-1$
        LOG.info( "--precision,-p			  Precision" ); //$NON-NLS-1$
        LOG.info( "--isolines,-i 				Create isolines (if not set, polygones are created)" ); //$NON-NLS-1$
        LOG.info( "--volume,-v 					Calculate volume (only if polygones are created)" ); //$NON-NLS-1$
        LOG.info( "--hideFilePanel,-f 	Hides the input controls for the files" ); //$NON-NLS-1$
        LOG.info( "--log,-l 						log file" ); //$NON-NLS-1$
        System.exit( CODE_CANCEL );
      }

      // final Boolean bFliTiObj = (Boolean)parser.getOptionValue( fliTiOption );
      // final boolean bFliTi = bFliTiObj != null && bFliTiObj.booleanValue();
      // Option wird nicht mehr benutzt, kann demnächst ganz raus
      final boolean bFliTi = true;

      final File logFile = (File) parser.getOptionValue( logFileOption );
      if( logFile != null )
        Logger.getLogger( "" ).addHandler( new FileHandler( logFile.getAbsolutePath(), 10 * 1024 * 1024, 1, false ) ); //$NON-NLS-1$

      final File dgmFile = (File) parser.getOptionValue( dgmFileOption );
      final File wspFile = (File) parser.getOptionValue( wspFileOption );
      final String shapeBase = (String) parser.getOptionValue( shapeBaseOption );
      final Double rasterSize = (Double) parser.getOptionValue( rasterSizeOption );
      final Boolean bIso = (Boolean) parser.getOptionValue( isoOption );
      final Boolean bVolume = (Boolean) parser.getOptionValue( volumeOption );
      final Boolean bHideFilesObj = (Boolean) parser.getOptionValue( hideFilesOption );
      final boolean bHideFiles = bHideFilesObj == null ? false : bHideFilesObj;
      // todo: später auch die grenzen über Kommandozeile parsen
      final double[] grenzen = null;

      WspFliArgs fliArgs = new WspFliArgs( dgmFile, wspFile, shapeBase, rasterSize, bIso, grenzen, bFliTi, bVolume );

      if( parser.getOptionValue( batchOption ) == null )
      {
        final WspFli panel = new WspFli( fliArgs, bHideFiles );

        while( true )
        {
          if( JOptionPane.showConfirmDialog( rootFrame, panel, Messages.getString("com.bce.apps.wspfli.WspFli.88"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE ) == JOptionPane.OK_OPTION ) //$NON-NLS-1$
          {
            try
            {
              fliArgs = panel.getValues();
              panel.savePrefs();
              break;
            }
            catch( final Exception ex )
            {
              final String msg = Messages.getString("com.bce.apps.wspfli.WspFli.89") + ex.getLocalizedMessage(); //$NON-NLS-1$
              LOG.log( Level.INFO, msg, ex );
              JOptionPane.showMessageDialog( rootFrame, msg, Messages.getString("com.bce.apps.wspfli.WspFli.90"), JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$
            }
          }
          else
            System.exit( CODE_CANCEL );
        }
      }
      else
      {
        if( dgmFile == null )
          reportError( rootFrame, Messages.getString("com.bce.apps.wspfli.WspFli.91"), null, CODE_ERROR ); //$NON-NLS-1$

        if( wspFile == null )
          reportError( rootFrame, Messages.getString("com.bce.apps.wspfli.WspFli.92"), null, CODE_ERROR ); //$NON-NLS-1$

        if( rasterSize == null )
          reportError( rootFrame, Messages.getString("com.bce.apps.wspfli.WspFli.93"), null, CODE_ERROR ); //$NON-NLS-1$

        if( bIso == null )
          reportError( rootFrame, Messages.getString("com.bce.apps.wspfli.WspFli.94"), null, CODE_ERROR ); //$NON-NLS-1$
      }

      Hmo2Fli.transform( rootFrame, fliArgs.dgmFile, fliArgs.wspFile, fliArgs.rasterSize.doubleValue(), fliArgs.bIso.booleanValue(), fliArgs.shapeBase, fliArgs.grenzen, fliArgs.bDoVolumeCalculation );
    }
    catch( final CmdLineParser.IllegalOptionValueException iove )
    {
      reportError( rootFrame, Messages.getString("com.bce.apps.wspfli.WspFli.95") + iove.getLocalizedMessage(), iove, CODE_ERROR ); //$NON-NLS-1$
    }
    catch( final CmdLineParser.UnknownOptionException uoe )
    {
      reportError( rootFrame, Messages.getString("com.bce.apps.wspfli.WspFli.96") + uoe.getLocalizedMessage(), uoe, CODE_ARGUMENT_ERROR ); //$NON-NLS-1$
    }
    catch( final IOException ioe )
    {
      reportError( rootFrame, Messages.getString("com.bce.apps.wspfli.WspFli.97") + ioe.getLocalizedMessage(), ioe, CODE_ARGUMENT_ERROR ); //$NON-NLS-1$
    }
    catch( final OutOfMemoryError oome )
    {
      JOptionPane.showMessageDialog( rootFrame, Messages.getString("com.bce.apps.wspfli.WspFli.98"), Messages.getString("com.bce.apps.wspfli.WspFli.99"), JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$
      System.exit( CODE_MEMORY );
    }
    catch( final Throwable e )
    {
      final String msg = e.getLocalizedMessage();
      reportError( rootFrame, Messages.getString("com.bce.apps.wspfli.WspFli.100") + msg != null ? msg : Messages.getString("com.bce.apps.wspfli.WspFli.101"), e, CODE_ARGUMENT_ERROR ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    System.exit( CODE_NO_ERROR );
  }

  private static void reportError( final Component parent, final String msg, final Throwable t, final int errorCode )
  {
    if( t != null )
      t.printStackTrace();

    JOptionPane.showMessageDialog( parent, msg, Messages.getString("com.bce.apps.wspfli.WspFli.102"), JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$
    LOG.log( Level.SEVERE, msg, t );
    final StringWriter sw = new StringWriter();
    t.printStackTrace( new PrintWriter( sw ) );
    LOG.severe( sw.toString() );

    System.exit( errorCode );
  }
}
