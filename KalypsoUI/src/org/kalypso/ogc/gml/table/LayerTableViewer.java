/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.table;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.commons.command.InvisibleCommand;
import org.kalypso.contribs.eclipse.swt.custom.ExcelTableCursor;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.jaxb.TemplateUtilities;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.core.util.pool.KeyInfo;
import org.kalypso.core.util.pool.PoolableObjectType;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ogc.gml.GisTemplateHelper;
import org.kalypso.ogc.gml.IFeaturesProvider;
import org.kalypso.ogc.gml.KalypsoFeatureThemeSelection;
import org.kalypso.ogc.gml.command.ChangeFeaturesCommand;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.featureview.IFeatureModifier;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.FeatureSelectionHelper;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypso.ogc.gml.selection.IFeatureSelectionListener;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ogc.gml.table.celleditors.IFeatureModifierFactory;
import org.kalypso.ogc.gml.table.command.ChangeSortingCommand;
import org.kalypso.template.gistableview.Gistableview;
import org.kalypso.template.gistableview.Gistableview.Layer;
import org.kalypso.template.gistableview.Gistableview.Layer.Column;
import org.kalypso.template.gistableview.Gistableview.Layer.Sort;
import org.kalypso.template.types.LayerType;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.KalypsoUIExtensions;
import org.kalypso.util.swt.SWTUtilities;
import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.filterencoding.FilterConstructionException;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * @todo TableCursor soll sich auch bewegen, wenn die Sortierung sich ändert
 * @author Gernot Belger
 */
public class LayerTableViewer extends TableViewer implements ICellModifier
{
  protected Logger LOGGER = Logger.getLogger( LayerTableViewer.class.getName() );

  public static final String COLUMN_PROP_NAME = "columnName"; //$NON-NLS-1$

  /**
   * Label Property. Feature-Annotation style format string. The context-feature in this case is the paretn feature of
   * the shown list.
   */
  public static final String COLUMN_PROP_LABEL = "columnLabel"; //$NON-NLS-1$

  /**
   * Tooltip Property. Feature-Annotation style format string. The context-feature in this case is the paretn feature of
   * the shown list.
   */
  public static final String COLUMN_PROP_TOOLTIP = "columnTooltip"; //$NON-NLS-1$

  public static final String COLUMN_PROP_EDITABLE = "columnEditable"; //$NON-NLS-1$

  public static final String COLUMN_PROP_WIDTH = "columnWidth"; //$NON-NLS-1$

  public static final String COLUMN_PROP_FORMAT = "columnFormat"; //$NON-NLS-1$

  public static final String COLUMN_PROP_MODIFIER = "columnModifier"; //$NON-NLS-1$

  private final IFeatureModifierFactory m_featureModiferFactory;

  private IFeatureModifier[] m_modifier;

  private final LayerTableSorter m_sorter = new LayerTableSorter();

  protected final ICommandTarget m_templateTarget;

  protected boolean m_isApplyTemplate = false;

  private final IFeatureSelectionListener m_globalSelectionListener = new IFeatureSelectionListener()
  {
    @Override
    public void selectionChanged( final Object source, final IFeatureSelection selection )
    {
      final Feature[] features = FeatureSelectionHelper.getFeatures( selection );
      final List<Feature> globalFeatureList = new ArrayList<Feature>( Arrays.asList( features ) );

      // filter ths which are in my list
      final IFeaturesProvider featureProvider = getInput();
      if( featureProvider == null )
        return;

      final FeatureList featureList = featureProvider.getFeatureList();
      final List< ? > themeFeatures = featureList == null ? new ArrayList<Object>() : (List< ? >) featureList;
      globalFeatureList.retainAll( themeFeatures );
      final Feature[] globalFeatures = globalFeatureList.toArray( new Feature[globalFeatureList.size()] );

      final Control control = getControl();
      if( control.isDisposed() )
        return;

      control.getDisplay().syncExec( new Runnable()
      {
        @Override
        public void run( )
        {
          if( control.isDisposed() )
            return;

          final ISelection tableSelection = getSelection();
          if( tableSelection instanceof IFeatureSelection )
          {
            final IFeatureSelection currentSelection = (IFeatureSelection) tableSelection;
            final Feature[] currentFeatures = FeatureSelectionHelper.getFeatures( currentSelection );
            if( !org.kalypso.contribs.java.util.Arrays.equalsUnordered( globalFeatures, currentFeatures ) )
              setSelection( selection );
          }
        }
      } );
    }
  };

  /**
   * This class handles selections of the column headers. Selection of the column header will cause resorting of the
   * shown tasks using that column's sorter. Repeated selection of the header will toggle sorting order (ascending
   * versus descending).
   */
  private final SelectionListener m_headerListener = new SelectionAdapter()
  {
    /**
     * Handles the case of user selecting the header area.
     * <p>
     * If the column has not been selected previously, it will set the sorter of that column to be the current tasklist
     * sorter. Repeated presses on the same column header will toggle sorting order (ascending/descending/original).
     */
    @Override
    public void widgetSelected( final SelectionEvent e )
    {
      // column selected - need to sort
      final TableColumn tableColumn = (TableColumn) e.widget;

      m_templateTarget.postCommand( new ChangeSortingCommand( LayerTableViewer.this, tableColumn ), null );
    }
  };

  private final ControlListener m_headerControlListener = new ControlAdapter()
  {
    /**
     * @see org.eclipse.swt.events.ControlAdapter#controlResized(org.eclipse.swt.events.ControlEvent)
     */
    @Override
    public void controlResized( final ControlEvent e )
    {
      if( m_isApplyTemplate == true )
        return;

      final TableColumn tc = (TableColumn) e.widget;

      // kann nicht rückgüngig gemacht werden, sorgt aber dafür, dass der Editor
      // dirty ist
      final int width = tc.getWidth();
      if( width != ((Integer) tc.getData( COLUMN_PROP_WIDTH )).intValue() )
      {
        m_templateTarget.postCommand( new InvisibleCommand(), null );
        // removeListener and again add listener may reduce some flickering
        // effects ?? (doemming)
        tc.removeControlListener( this );
        tc.setData( COLUMN_PROP_WIDTH, new Integer( width ) );
        tc.addControlListener( this );
      }
    }
  };

  private final IFeatureSelectionManager m_selectionManager;

  private final IFeatureChangeListener m_fcl;

  private ExcelTableCursor m_tableCursor = null;

  /**
   * @param parent
   * @param templateTarget
   * @param featureControlFactory
   */
  public LayerTableViewer( final Composite parent, final int style, final ICommandTarget templateTarget, final IFeatureModifierFactory featureControlFactory, final IFeatureSelectionManager selectionManager, final IFeatureChangeListener fcl )
  {
    super( parent, style | SWT.MULTI | SWT.FULL_SELECTION );

    setUseHashlookup( true );

    m_featureModiferFactory = featureControlFactory;
    m_templateTarget = templateTarget;
    m_selectionManager = selectionManager;
    m_fcl = fcl;
    if( m_selectionManager != null )
      m_selectionManager.addSelectionListener( m_globalSelectionListener );

    setContentProvider( new LayerTableContentProvider( selectionManager ) );
    setLabelProvider( new LayerTableLabelProvider( this ) );
    setCellModifier( this );
    setSorter( m_sorter );

    // init table
    final Table table = getTable();
    table.setHeaderVisible( true );
    table.setLinesVisible( true );
    // disable capture to let selection of table and tableviewer in sync
    table.setCapture( false );

    m_tableCursor = new ExcelTableCursor( this, SWT.NONE, ExcelTableCursor.ADVANCE_MODE.DOWN, true );
  }

  @Override
  protected void handleDispose( final DisposeEvent event )
  {
    final ILayerTableInput input = getInput();
    if( input != null )
      input.dispose();

    if( m_selectionManager != null )
      m_selectionManager.removeSelectionListener( m_globalSelectionListener );

    super.handleDispose( event );
  }

  /**
   * @see org.eclipse.jface.viewers.ContentViewer#getInput()
   */
  @Override
  public ILayerTableInput getInput( )
  {
    return (ILayerTableInput) super.getInput();
  }

  public void setInput( final Layer layer, final URL context )
  {
    final String href = layer.getHref();
    final String type = layer.getLinktype();
    final String featurePath = layer.getFeaturePath();

    final IPoolableObjectType poolKey = new PoolableObjectType( type, href, context );

    final ILayerTableInput input = new PoolLayerTableInput( poolKey, featurePath );

    setInput( input );
  }

  public void setInput( final CommandableWorkspace workspace, final String featurePath, final ICommandTarget commandTarget )
  {
    final ILayerTableInput layerTableInput = new WorkspaceLayerTableInput( workspace, featurePath, commandTarget );
    setInput( layerTableInput );
  }

  /** Configures the table accordingly to the template. Does NOT change the input element. */
  public void applyLayer( final Layer layer )
  {
    m_isApplyTemplate = true;

    try
    {
      clearColumns();

      setFilters( new ViewerFilter[0] );

      final Sort sort = layer.getSort();
      final List<Column> columnList = layer.getColumn();
      setSortAndColumns( sort, columnList );

      applyFilter( layer );
    }
    finally
    {
      refreshAll();
      m_isApplyTemplate = false;
    }
  }

  private void applyFilter( final Layer layer )
  {
    try
    {
      final Filter filter = GisTemplateHelper.getFilter( layer );
      if( filter == null )
        return;

      final ViewerFilter viewerFilter = new FeatureViewerFilter( filter );
      addFilter( viewerFilter );
    }
    catch( final FilterConstructionException e )
    {
      e.printStackTrace();
    }
  }

  private void setSortAndColumns( final Sort sort, final List<Column> columnList )
  {
    if( sort != null )
    {
      m_sorter.setPropertyName( sort.getPropertyName() );
      m_sorter.setInverse( sort.isInverse() );
    }

    for( final Column ct : columnList )
      addColumn( ct.getName(), ct.getLabel(), ct.getTooltip(), ct.isEditable(), ct.getWidth(), ct.getAlignment(), ct.getFormat(), ct.getModifier(), false );
  }

  void clearColumns( )
  {
    final Table table = getTable();
    if( table.isDisposed() )
      return;

    final TableColumn[] columns = table.getColumns();
    for( final TableColumn element : columns )
      element.dispose();
  }

  public void addColumn( final String propertyName, final String label, final String tooltip, final boolean isEditable, final int width, final String alignment, final String format, final String modifier, final boolean bRefreshColumns )
  {
    final Table table = getTable();

    final int alignmentInt = SWTUtilities.createStyleFromString( alignment );
    final TableColumn tc = new TableColumn( table, alignmentInt );
    tc.setAlignment( alignmentInt );

    tc.setData( COLUMN_PROP_NAME, propertyName );
    tc.setData( COLUMN_PROP_LABEL, label );
    tc.setData( COLUMN_PROP_TOOLTIP, tooltip );
    tc.setData( COLUMN_PROP_EDITABLE, Boolean.valueOf( isEditable ) );
    // die Breite noch mal extra speichern, damit das Redo beim Resizen geht
    tc.setData( COLUMN_PROP_WIDTH, new Integer( width ) );
    tc.setData( COLUMN_PROP_FORMAT, format );
    tc.setData( COLUMN_PROP_MODIFIER, modifier );
    tc.setToolTipText( tooltip );
    tc.setWidth( width );
    setColumnText( tc );

    tc.addSelectionListener( m_headerListener );
    tc.addControlListener( m_headerControlListener );

    if( bRefreshColumns )
      refreshAll();
  }

  protected void setColumnText( final TableColumn tc )
  {
    final String propertyName = (String) tc.getData( COLUMN_PROP_NAME );

    final String label = (String) tc.getData( COLUMN_PROP_LABEL );
    final String tooltip = (String) tc.getData( COLUMN_PROP_TOOLTIP );

    final String sortPropertyName = m_sorter.getPropertyName();

    final String[] textAndTooltip = getLabelAndTooltip( label, tooltip, propertyName );

    final String text;
    if( propertyName.equals( sortPropertyName ) )
      text = textAndTooltip[0] + " " + (m_sorter.isInverse() ? "\u00ab" : "\u00bb"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    else
      text = textAndTooltip[0];

    final String tooltipText = textAndTooltip[1];

    tc.setText( text );
    tc.setToolTipText( tooltipText );
  }

  private String[] getLabelAndTooltip( final String label, final String tooltip, final String propertyName )
  {
    final String[] result = new String[2];

    result[0] = propertyName; // prepare for exception

    try
    {
      final IFeaturesProvider input = getInput();

      final IFeatureType featureType = input.getFeatureType();

      if( featureType != null )
      {
        final IPropertyType property = featureType.getProperty( propertyName );
        if( property != null )
        {
          final IAnnotation annotation = property.getAnnotation();
          result[0] = annotation.getLabel();
          result[1] = annotation.getTooltip();
        }
      }

      if( label != null )
        result[0] = label;

      if( tooltip != null )
        result[1] = tooltip;
    }
    catch( final Exception e )
    {
      // if data is not loaded yet, we provide the propertyname
      e.printStackTrace();

      result[1] = e.toString();
    }

    return result;
  }

  private void checkColumns( )
  {
    final IFeaturesProvider input = getInput();
    if( input == null )
      return;

    final IFeatureType featureType = input.getFeatureType();
    final Table table = getTable();

    if( featureType == null || table == null || table.isDisposed() )
      return;

    final TableColumn[] columns = table.getColumns();
    boolean changed = false;

    for( final TableColumn column : columns )
    {
      if( column != null )
      {
        final String propName = column.getData( COLUMN_PROP_NAME ).toString();
        if( featureType.getProperty( propName ) == null )
        {
          column.dispose();
          changed = true;
        }
      }
    }

    if( changed )
      refreshAll();
  }

  public void refreshAll( )
  {
    refreshCellEditors();
    refreshColumnProperties();
    refresh();
  }

  public void removeColumn( final String name )
  {
    final TableColumn column = getColumn( name );
    if( column != null )
      column.dispose();

    refreshAll();
  }

  /**
   * @see org.eclipse.jface.viewers.StructuredViewer#refresh()
   */
  @Override
  public void refresh( )
  {
    if( isDisposed() )
      return;
    // FIXME: causes refresh to be called twice...
    checkColumns();
    // die Namen der Spalten auffrischen, wegen der Sortierungs-Markierung
    final TableColumn[] columns = getTable().getColumns();
    for( final TableColumn element : columns )
    {
      setColumnText( element );

      // Should work, but does not, but why???
// /* as long as width is 'auto', autoresize the column */
// final int width = ((Integer) element.getData( COLUMN_PROP_WIDTH )).intValue();
// if( width == -1 )
// element.pack();
    }

    super.refresh();
  }

  /**
   * @see org.eclipse.jface.viewers.TableViewer#getCellEditors()
   */
  private void refreshCellEditors( )
  {
    m_modifier = null;
    // dispose old modifiers
    final CellEditor[] oldEditors = getCellEditors();
    if( oldEditors != null )
    {
      for( final CellEditor element : oldEditors )
      {
        if( element != null )
          element.dispose();
      }
    }

    final Table table = getTable();
    if( table.isDisposed() )
      return;

    final TableColumn[] columns = table.getColumns();
    final CellEditor[] editors = new CellEditor[columns.length];
    setCellEditors( editors );

    final IFeaturesProvider featureProvider = getInput();
    if( featureProvider == null )
      return;

    final IFeatureType featureType = featureProvider.getFeatureType();
    if( featureType == null )
      return;
    // set new modifiers, new celleditors and new cellvalidators
    m_modifier = new IFeatureModifier[columns.length];
    for( int i = 0; i < editors.length; i++ )
    {
      final String propName = columns[i].getData( COLUMN_PROP_NAME ).toString();
      final String format = (String) columns[i].getData( COLUMN_PROP_FORMAT );
      final IPropertyType ftp = featureType.getProperty( propName );
      final String modifierId = (String) columns[i].getData( COLUMN_PROP_MODIFIER );

      m_modifier[i] = createModifier( format, ftp, modifierId );
      if( m_modifier[i] != null )
      {
        editors[i] = m_modifier[i].createCellEditor( table );
        editors[i].setValidator( m_modifier[i] );
      }
    }
    setCellEditors( editors );
  }

  private IFeatureModifier createModifier( final String format, final IPropertyType ftp, final String modifierId )
  {
    if( modifierId != null && !modifierId.isEmpty() )
    {
      try
      {
        final IFeatureModifier modifier = KalypsoUIExtensions.createFeatureModifier( ftp, modifierId );
        if( modifier == null )
          System.out.println( "No feature modifier with id: " + modifierId );
        else
          return modifier;
      }
      catch( final CoreException e )
      {
        KalypsoGisPlugin.getDefault().getLog().log( e.getStatus() );
        e.printStackTrace();
      }
    }

    return m_featureModiferFactory.createFeatureModifier( ftp, format, m_selectionManager, m_fcl );
  }

  private void refreshColumnProperties( )
  {
    final Table table = getTable();
    if( table.isDisposed() )
      return;

    final TableColumn[] columns = table.getColumns();
    final String[] properties = new String[columns.length];

    for( int i = 0; i < properties.length; i++ )
      properties[i] = columns[i].getData( COLUMN_PROP_NAME ).toString();

    setColumnProperties( properties );
  }

  public boolean isDisposed( )
  {
    return getTable().isDisposed();
  }

  public String getColumnAlignment( final int columnIndex )
  {
    if( columnIndex == -1 )
      return "SWT.LEAD"; //$NON-NLS-1$

    final TableColumn column = getTable().getColumn( columnIndex );
    return "" + column.getStyle(); //$NON-NLS-1$
  }

  public String getColumnFormat( final int columnIndex )
  {
    if( columnIndex == -1 )
      return null;

    final TableColumn column = getTable().getColumn( columnIndex );
    return (String) column.getData( COLUMN_PROP_FORMAT );
  }

  public boolean isEditable( final String property )
  {
    final TableColumn column = getColumn( property );
    return column == null ? false : ((Boolean) column.getData( COLUMN_PROP_EDITABLE )).booleanValue();
  }

  private TableColumn getColumn( final String property )
  {
    final TableColumn[] columns = getTable().getColumns();
    for( final TableColumn element : columns )
    {
      final String name = element.getData( COLUMN_PROP_NAME ).toString();
      if( property.equals( name ) )
        return element;
    }

    return null;
  }

  public int getColumnID( final String property )
  {
    final TableColumn[] columns = getTable().getColumns();
    for( int i = 0; i < columns.length; i++ )
    {
      final String name = columns[i].getData( COLUMN_PROP_NAME ).toString();
      if( property.equals( name ) )
        return i;
    }

    return -1;
  }

  public int getWidth( final String propertyName )
  {
    final TableColumn column = getColumn( propertyName );
    if( column != null )
      return column.getWidth();

    return 0;
  }

  public boolean hasColumn( final String propertyName )
  {
    return getColumn( propertyName ) != null;
  }

  public int getColumnCount( )
  {
    return getTable().getColumnCount();
  }

  public Gistableview createTableTemplate( )
  {
    final org.kalypso.template.gistableview.ObjectFactory OF = TemplateUtilities.OF_GISTABLEVIEW;
    final Gistableview tableTemplate = OF.createGistableview();
    final Layer layer = OF.createGistableviewLayer();

    fillLayerType( layer );

    tableTemplate.setLayer( layer );

    final List<Column> columns = layer.getColumn();

    final TableColumn[] tableColumns = getTable().getColumns();
    for( final TableColumn tc : tableColumns )
    {
      final Column columnType = OF.createGistableviewLayerColumn();

      columnType.setName( tc.getData( COLUMN_PROP_NAME ).toString() );
      columnType.setLabel( (String) tc.getData( COLUMN_PROP_LABEL ) );
      columnType.setTooltip( (String) tc.getData( COLUMN_PROP_TOOLTIP ) );
      columnType.setEditable( ((Boolean) tc.getData( COLUMN_PROP_EDITABLE )).booleanValue() );
      columnType.setWidth( tc.getWidth() );
      columnType.setAlignment( "" + tc.getStyle() ); //$NON-NLS-1$
      columnType.setFormat( (String) tc.getData( COLUMN_PROP_FORMAT ) );

      columns.add( columnType );
    }

    final LayerTableSorter sorter = (LayerTableSorter) getSorter();
    final String propertyName = sorter.getPropertyName();
    if( propertyName != null )
    {
      final Sort sort = OF.createGistableviewLayerSort();
      sort.setPropertyName( propertyName );
      sort.setInverse( sorter.isInverse() );
      layer.setSort( sort );
    }

    final ViewerFilter[] filters = getFilters();
    for( final ViewerFilter filter : filters )
    {
      if( filter instanceof FeatureViewerFilter )
      {
        try
        {
          final Filter ogcFilter = ((FeatureViewerFilter) filter).getFilter();
          TemplateUtilities.setFilter( layer, ogcFilter );
        }
        catch( final Exception e )
        {
          e.printStackTrace();
        }
      }
    }

    return tableTemplate;
  }

  private void fillLayerType( final LayerType layer )
  {
    final ILayerTableInput input = getInput();
    if( !(input instanceof PoolLayerTableInput) )
      throw new UnsupportedOperationException();

    final String featurePath = input.getFeaturePath();

    final IPoolableObjectType key = ((PoolLayerTableInput) input).getPoolKey();

    layer.setId( "id" );
    layer.setHref( key.getLocation() );
    layer.setLinktype( key.getType() );
    layer.setActuate( "onRequest" ); //$NON-NLS-1$
    layer.setType( "simple" ); //$NON-NLS-1$
    layer.setFeaturePath( featurePath );
  }

  public void saveData( final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      final ILayerTableInput input = getInput();
      if( input instanceof PoolLayerTableInput )
      {
        final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
        final IPoolableObjectType poolKey = ((PoolLayerTableInput) input).getPoolKey();
        final KeyInfo info = pool.getInfoForKey( poolKey );
        info.saveObject( monitor );
      }
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw new CoreException( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), "Fehler beim Speichern", e ) ); //$NON-NLS-1$
    }
  }

  public String[][] exportTable( final boolean onlySelected )
  {
    Object[] features;

    if( onlySelected )
    {
      final IStructuredSelection sel = (IStructuredSelection) getSelection();
      features = sel.toArray();
    }
    else
    {
      final IFeaturesProvider featuresProvider = getInput();
      if( featuresProvider == null )
        return new String[0][];

      features = featuresProvider.getFeatureList().toFeatures();
    }

    final Collection<String[]> lines = new ArrayList<String[]>();

    final ITableLabelProvider labelProvider = (ITableLabelProvider) getLabelProvider();

    final Table table = getTable();
    final TableColumn[] columns = table.getColumns();

    // TODO: exports the property name, not the current label; change this
    final String[] firstLine = new String[columns.length];
    for( int j = 0; j < columns.length; j++ )
      firstLine[j] = (String) columns[j].getData( COLUMN_PROP_NAME );
    lines.add( firstLine );

    for( final Object element : features )
    {
      final String[] line = new String[columns.length];

      for( int j = 0; j < columns.length; j++ )
        line[j] = labelProvider.getColumnText( element, j );

      lines.add( line );
    }

    return lines.toArray( new String[features.length][] );
  }

  public IFeatureModifier getModifier( final int columnIndex )
  {
    return m_modifier == null ? null : m_modifier[columnIndex];
  }

  /**
   * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
   */
  @Override
  public boolean canModify( final Object element, final String property )
  {
    // TODO ask modifier also, as for some types editor may not be implemented
    return isEditable( property );
  }

  /**
   * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
   */
  @Override
  public Object getValue( final Object element, final String property )
  {
    final IFeatureModifier modifier = getModifier( property );

    if( modifier != null )
      return modifier.getValue( (Feature) element );

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
   */
  @Override
  public void modify( final Object element, final String property, final Object value )
  {
    final IFeatureModifier modifier = getModifier( property );

    if( modifier != null )
    {
      final TableItem ti = (TableItem) element;
      final Feature feature = (Feature) ti.getData();
      // as result==null does not explicitly mean that
      // the value is invalid, we have to ask the celleditor for invalidity
      final int columnID = getColumnID( property );
      if( columnID < 0 )
        return;
      final CellEditor[] editors = getCellEditors();
      if( editors == null || editors.length < columnID )
        return;
      if( !editors[columnID].isValueValid() )
        return;

      final Object object = modifier.parseInput( feature, value );
      final Object oldValue = modifier.getValue( feature );
      if( oldValue != null && oldValue.equals( object ) )
        return;

      // dialogs may return FeatureChange objects (doemming)
      final FeatureChange fc;
      if( object instanceof FeatureChange )
        fc = (FeatureChange) object;
      else
      {
        final IPropertyType pt = FeatureHelper.getPT( feature, property );
        fc = new FeatureChange( feature, pt, object );
      }

      final ILayerTableInput input = getInput();
      final CommandableWorkspace workspace = input.getWorkspace();
      final ICommand command = new ChangeFeaturesCommand( workspace, new FeatureChange[] { fc } );

      final ICommandTarget commandTarget = getFeatureCommandTarget();
      commandTarget.postCommand( command, null );
    }
  }

  private ICommandTarget getFeatureCommandTarget( )
  {
    final ILayerTableInput input = getInput();
    if( input == null )
      return null;

    return input.getCommandTarget();
  }

  public IFeatureModifier getModifier( final String name )
  {
    if( m_modifier != null )
    {
      for( final IFeatureModifier fm : m_modifier )
      {
        if( fm != null )
        {
          final IPropertyType ftp = fm.getFeatureTypeProperty();
          if( ftp.getName().equals( name ) )
            return fm;
        }
      }
    }
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.StructuredViewer#getSelection()
   */
  @Override
  public ISelection getSelection( )
  {
    final IFeaturesProvider input = getInput();
    if( input == null )
      return super.getSelection();

    final IStructuredSelection selection = (IStructuredSelection) super.getSelection();

    final FeatureList featureList = input.getFeatureList();
    final CommandableWorkspace workspace = input.getWorkspace();

    if( m_tableCursor == null )
      return new KalypsoFeatureThemeSelection( selection.toList(), featureList, workspace, m_selectionManager, null, null );

    final TableItem row = m_tableCursor.getRow();
    final int column = m_tableCursor.getColumn();
    if( row != null && row.getData() instanceof Feature )
    {
      final Feature focusedFeature = row == null ? null : (Feature) row.getData();
      final IFeatureModifier modifier = (column < 0 || m_modifier == null || column > m_modifier.length - 1) ? null : m_modifier[column];

      final IPropertyType focusedProperty = modifier == null ? null : modifier.getFeatureTypeProperty();

      return new KalypsoFeatureThemeSelection( selection.toList(), featureList, workspace, m_selectionManager, focusedFeature, focusedProperty );
    }

    return StructuredSelection.EMPTY;
  }

  @Override
  protected void inputChanged( final Object input, final Object oldInput )
  {
    clearColumns();

    if( oldInput instanceof ILayerTableInput )
      ((ILayerTableInput) oldInput).dispose();

    if( !isDisposed() )
      super.inputChanged( input, oldInput );
  }

  /** Registers this MenuManager es context menu on table and table cursor */
  public void setMenu( final MenuManager menuManager )
  {
    final Table table = getTable();
    final Menu tablemenu = menuManager.createContextMenu( table );
    table.setMenu( tablemenu );

    final Menu cursormenu = menuManager.createContextMenu( m_tableCursor );
    m_tableCursor.setMenu( cursormenu );
  }
}