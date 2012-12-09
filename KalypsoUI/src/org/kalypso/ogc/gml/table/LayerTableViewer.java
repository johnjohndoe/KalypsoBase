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
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.kalypso.contribs.eclipse.swt.SWTUtilities;
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
import org.kalypso.template.gistableview.Gistableview.Layer.Column.Param;
import org.kalypso.template.gistableview.Gistableview.Layer.Sort;
import org.kalypso.template.gistableview.StyleType;
import org.kalypso.template.types.LayerType;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.KalypsoUIExtensions;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.filterencoding.FilterConstructionException;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathException;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;

/**
 * @todo TableCursor soll sich auch bewegen, wenn die Sortierung sich ändert
 * @author Gernot Belger
 */
public class LayerTableViewer extends TableViewer implements ICellModifier
{
  private static final String PROPERTY_COLUMN_DESCRIPTOR = "columnDescriptor"; //$NON-NLS-1$

  private final IFeatureModifierFactory m_featureModiferFactory;

// private IFeatureModifier[] m_modifier;

  private final LayerTableSorter m_sorter = new LayerTableSorter();

  protected final ICommandTarget m_templateTarget;

  protected boolean m_isApplyTemplate = false;

  private final IFeatureSelectionListener m_globalSelectionListener = new IFeatureSelectionListener()
  {
    @Override
    public void selectionChanged( final Object source, final IFeatureSelection selection )
    {
      final Feature[] features = FeatureSelectionHelper.getFeatures( selection );
      final List<Feature> globalFeatureList = new ArrayList<>( Arrays.asList( features ) );

      // filter ths which are in my list
      final IFeaturesProvider featureProvider = getInput();
      if( featureProvider == null )
        return;

      final FeatureList featureList = featureProvider.getFeatureList();
      final List< ? > themeFeatures = featureList == null ? new ArrayList<>() : (List< ? >)featureList;
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
            final IFeatureSelection currentSelection = (IFeatureSelection)tableSelection;
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
     * If the column has not been selected previously, it will set the sorter of that column to be the current tasklist sorter. Repeated presses on the same column header will toggle sorting order
     * (ascending/descending/original).
     */
    @Override
    public void widgetSelected( final SelectionEvent e )
    {
      // column selected - need to sort
      final TableColumn tableColumn = (TableColumn)e.widget;

      m_templateTarget.postCommand( new ChangeSortingCommand( LayerTableViewer.this, tableColumn ), null );
    }
  };

  private final ControlListener m_headerControlListener = new ControlAdapter()
  {
    @Override
    public void controlResized( final ControlEvent e )
    {
      if( m_isApplyTemplate == true )
        return;

      final TableColumn tc = (TableColumn)e.widget;

      // kann nicht rückgüngig gemacht werden, sorgt aber dafür, dass der Editor dirty ist
      final int width = tc.getWidth();

      final IColumnDescriptor column = getDescriptor( tc );

      if( width != column.getWidth() )
      {
        m_templateTarget.postCommand( new InvisibleCommand(), null );
      }
    }
  };

  private final IFeatureSelectionManager m_selectionManager;

  private final IFeatureChangeListener m_fcl;

  private ExcelTableCursor m_tableCursor = null;

  private NamespaceContext m_namespaceContext;

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

    table.addListener( SWT.EraseItem, new LayerTablePainter( this ) );

    m_tableCursor = new ExcelTableCursor( this, SWT.NONE, ExcelTableCursor.ADVANCE_MODE.DOWN, true );
  }

  @Override
  protected void hookEditingSupport( final Control control )
  {
    // BUGFIX/Intended behavior, see #958 : let the table cursor handle all editing stuff.
    // super.hookEditingSupport( control );
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

  @Override
  public ILayerTableInput getInput( )
  {
    return (ILayerTableInput)super.getInput();
  }

  public void setInput( final Layer layer, final URL context )
  {
    if( layer == null )
      setInput( null );
    else
    {
      final String href = layer.getHref();
      final String type = layer.getLinktype();
      final String featurePath = layer.getFeaturePath();

      final IPoolableObjectType poolKey = new PoolableObjectType( type, href, context );

      final ILayerTableInput input = new PoolLayerTableInput( poolKey, featurePath );

      setInput( input );
    }
  }

  public void setInput( final CommandableWorkspace workspace, final String featurePath, final ICommandTarget commandTarget )
  {
    final ILayerTableInput layerTableInput = new WorkspaceLayerTableInput( workspace, featurePath, commandTarget );
    setInput( layerTableInput );
  }

  /** Configures the table accordingly to the template. Does NOT change the input element. */
  public void applyLayer( final Layer layer, final URL context )
  {
    // FIXME: get namepsace context from outside
    m_namespaceContext = null;
    m_isApplyTemplate = true;

    try
    {
      clearColumns();

      setFilters( new ViewerFilter[0] );

      if( layer == null )
        return;

      final StyleType styleRef = layer.getStyle();

      final LayerTableStyle globalStyle = LayerTableStyleUtils.parseStyle( styleRef, new LayerTableStyle( null ), context );

      final Sort sort = layer.getSort();
      final List<Column> columnList = layer.getColumn();
      setSortAndColumns( sort, columnList, globalStyle, context );

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

  private void setSortAndColumns( final Sort sort, final List<Column> columnList, final LayerTableStyle globalStyle, final URL context )
  {
    for( final Column ct : columnList )
    {
      final ColumnDescriptor cd = createColumnDescriptor( ct, globalStyle, context );
      addColumn( cd, false );

      /* Configure sorter, if any */
      if( sort != null )
      {
        final String propertyName = sort.getPropertyName();
        final GMLXPath sortPath = parseQuietXPath( propertyName );
        if( ObjectUtils.equals( sortPath, cd.getPropertyPath() ) )
        {
          m_sorter.setColumn( cd );
          m_sorter.setInverse( sort.isInverse() );
        }
      }
    }
  }

  private ColumnDescriptor createColumnDescriptor( final Column ct, final LayerTableStyle globalStyle, final URL context )
  {
    final String propertyName = ct.getName();
    final GMLXPath propertyPath = parseQuietXPath( propertyName );

    final ColumnDescriptor cd = new ColumnDescriptor( propertyPath );

    final int alignmentInt = SWTUtilities.createStyleFromString( ct.getAlignment() );

    cd.setLabel( ct.getLabel() );
    cd.setTooltip( ct.getTooltip() );
    cd.setEditable( ct.isEditable() );
    cd.setWidth( ct.getWidth() );
    cd.setAlignment( alignmentInt );
    cd.setFormat( ct.getFormat() );
    cd.setModifier( ct.getModifier() );

    final List<Param> params = ct.getParam();
    for( final Param param : params )
      cd.setParam( param.getKey(), param.getValue() );

    final LayerTableStyle style = LayerTableStyleUtils.parseStyle( ct.getStyle(), globalStyle, context );
    cd.setStyle( style );

    cd.setSortEnabled( ct.isSortEnabled() );

    return cd;
  }

  private GMLXPath parseQuietXPath( final String propertyName )
  {
    if( StringUtils.isBlank( propertyName ) )
      return null;

    return new GMLXPath( propertyName, m_namespaceContext );
  }

  void clearColumns( )
  {
    final Table table = getTable();
    if( table.isDisposed() )
      return;

    final TableColumn[] columns = table.getColumns();
    for( final TableColumn column : columns )
      column.dispose();
  }

  public void addColumn( final IColumnDescriptor column, final boolean doRefreshColumns )
  {
    final Table table = getTable();

    final int alignment = column.getAlignment();

    // TODO: strange: alignment used as alignment and as style as well...
    final TableColumn tc = new TableColumn( table, alignment );
    tc.setAlignment( alignment );

    tc.setData( PROPERTY_COLUMN_DESCRIPTOR, column );

    tc.setWidth( column.getWidth() );
    tc.setResizable( column.isResizeable() );
    setColumnText( tc );

    if( column.isSortEnabled() )
      tc.addSelectionListener( m_headerListener );

    /* synchronize width of tc and descriptor */
    tc.addControlListener( m_headerControlListener );

    if( doRefreshColumns )
      refreshAll();
  }

  protected void setColumnText( final TableColumn tc )
  {
    final IColumnDescriptor column = getDescriptor( tc );

    tc.setToolTipText( column.getTooltip() );

    // TODO: instead: delegate to column?
    final GMLXPath propertyPath = column.getPropertyPath();
    if( propertyPath == null )
      return;

    final String label = column.getLabel();
    final String tooltip = column.getTooltip();

    final String[] textAndTooltip = getLabelAndTooltip( label, tooltip, propertyPath );

    final String text = textAndTooltip[0];
    final String tooltipText = textAndTooltip[1];

    tc.setText( text );
    tc.setToolTipText( tooltipText );
  }

  private String[] getLabelAndTooltip( final String label, final String tooltip, final GMLXPath propertyPath )
  {
    final String[] result = new String[2];

    result[0] = ObjectUtils.toString( propertyPath, StringUtils.EMPTY ); // prepare for exception

    try
    {
      final IFeaturesProvider input = getInput();

      final IFeatureType featureType = input.getFeatureType();

      if( featureType != null )
      {
        final Object property = GMLXPathUtilities.query( propertyPath, featureType );
        if( property instanceof IPropertyType )
        {
          final IAnnotation annotation = ((IPropertyType)property).getAnnotation();
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
        final IColumnDescriptor cd = getDescriptor( column );
        final GMLXPath propPath = cd.getPropertyPath();
        if( propPath != null )
        {
          final IPropertyType propertyType = findPropertyType( featureType, propPath );
          if( propertyType == null )
          {
            column.dispose();
            changed = true;
          }
        }
      }
    }

    if( changed )
      refreshAll();
  }

  static IPropertyType findPropertyType( final IFeatureType featureType, final GMLXPath propPath )
  {
    try
    {
      final Object propertyType = GMLXPathUtilities.query( propPath, featureType );
      if( propertyType instanceof IPropertyType )
        return (IPropertyType)propertyType;
    }
    catch( final GMLXPathException e )
    {
      e.printStackTrace();
    }

    return null;
  }

  public void refreshAll( )
  {
    refreshCellEditors();
    refreshColumnProperties();
    refresh();
  }

  public void removeColumn( final GMLXPath propertyPath )
  {
    final TableColumn column = getColumn( propertyPath );
    if( column != null )
      column.dispose();

    refreshAll();
  }

  @Override
  public void refresh( )
  {
    if( isDisposed() )
      return;
    // FIXME: causes refresh to be called twice...
    checkColumns();

    // Update header text, feature type may have changed
    final TableColumn[] columns = getTable().getColumns();
    for( final TableColumn element : columns )
      setColumnText( element );

    /* Update sort icon */
    getTable().setSortDirection( m_sorter.getSortDirection() );
    final IColumnDescriptor column = m_sorter.getColumn();
    final TableColumn tc = getColumn( column );
    getTable().setSortColumn( tc );

    super.refresh();
  }

  private TableColumn getColumn( final IColumnDescriptor searchColumn )
  {
    final ILayerTableInput tableInput = getInput();
    if( tableInput == null )
      return null;

    final TableColumn[] columns = getTable().getColumns();
    for( final TableColumn element : columns )
    {
      final IColumnDescriptor column = getDescriptor( element );
      if( searchColumn == column )
        return element;
    }

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.TableViewer#getCellEditors()
   */
  private void refreshCellEditors( )
  {
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
    for( int i = 0; i < editors.length; i++ )
    {
      final IColumnDescriptor column = getDescriptor( columns[i] );
      final IFeatureModifier modifier = createModifier( column, featureType );
      column.setModifier( modifier );

      if( modifier != null )
      {
        editors[i] = modifier.createCellEditor( table );
        if( editors[i] != null )
          editors[i].setValidator( modifier );
      }
    }
    setCellEditors( editors );
  }

  // TODO: delegate to column?!
  private IFeatureModifier createModifier( final IColumnDescriptor column, final IFeatureType featureType )
  {
    final GMLXPath propPath = column.getPropertyPath();
    if( propPath == null )
      return null;

    final String format = column.getFormat();
    final IPropertyType ftp = findPropertyType( featureType, propPath );
    final String modifierId = column.getModifierID();

    if( !StringUtils.isBlank( modifierId ) )
    {
      try
      {
        final Map<String, String> params = column.getParameters();
        final IFeatureModifier modifier = KalypsoUIExtensions.createFeatureModifier( propPath, ftp, modifierId, params );
        if( modifier == null )
          System.out.println( Messages.getString( "LayerTableViewer_0" ) + modifierId ); //$NON-NLS-1$
        else
          return modifier;
      }
      catch( final CoreException e )
      {
        KalypsoGisPlugin.getDefault().getLog().log( e.getStatus() );
        e.printStackTrace();
      }
    }

    return m_featureModiferFactory.createFeatureModifier( propPath, ftp, format, m_selectionManager, m_fcl );
  }

  private void refreshColumnProperties( )
  {
    final Table table = getTable();
    if( table.isDisposed() )
      return;

    final TableColumn[] columns = table.getColumns();
    final String[] pathes = new String[columns.length];

    for( int i = 0; i < pathes.length; i++ )
    {
      final IColumnDescriptor column = getDescriptor( columns[i] );
      pathes[i] = column.getColumnProperty();
    }

    setColumnProperties( pathes );
  }

  public boolean isDisposed( )
  {
    return getTable().isDisposed();
  }

  public IColumnDescriptor getColumnDescriptor( final int columnIndex )
  {
    if( columnIndex == -1 )
      // FIXME: does it ever happen?
      return null;

    final TableColumn column = getTable().getColumn( columnIndex );
    return getDescriptor( column );
  }

  private boolean isEditable( final GMLXPath propertyPath )
  {
    final TableColumn column = getColumn( propertyPath );
    if( column == null )
      return false;

    final IColumnDescriptor cd = getDescriptor( column );
    return cd.isEditable();
  }

  private TableColumn getColumn( final GMLXPath propertyPath )
  {
    final ILayerTableInput tableInput = getInput();
    if( tableInput == null )
      return null;

    final IFeatureType featureType = tableInput.getFeatureType();

    final IPropertyType searchType = findPropertyType( featureType, propertyPath );

    final TableColumn[] columns = getTable().getColumns();
    for( final TableColumn element : columns )
    {
      final IColumnDescriptor column = getDescriptor( element );
      final GMLXPath path = column.getPropertyPath();

      // REMARK: we need to resolve the property type here, instead
      // of comparing the paths, because the path may not always be
      // qualified by a namespace, but still denotes the same property
      final IPropertyType pt = findPropertyType( featureType, path );
      if( pt == searchType )
        return element;
    }

    return null;
  }

  public int getColumnID( final GMLXPath propertyPath )
  {
    final TableColumn[] columns = getTable().getColumns();
    for( int i = 0; i < columns.length; i++ )
    {
      final IColumnDescriptor column = getDescriptor( columns[i] );

      final GMLXPath columnPath = column.getPropertyPath();
      if( propertyPath.equals( columnPath ) )
        return i;
    }

    return -1;
  }

  public boolean hasColumn( final GMLXPath propertyPath )
  {
    return getColumn( propertyPath ) != null;
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

      final IColumnDescriptor column = getDescriptor( tc );

      final GMLXPath path = column.getPropertyPath();
      final String name = ObjectUtils.toString( path, StringUtils.EMPTY );
      columnType.setName( name );
      columnType.setLabel( column.getLabel() );
      columnType.setTooltip( column.getTooltip() );
      columnType.setEditable( column.isEditable() );
      columnType.setSortEnabled( column.isSortEnabled() );
      columnType.setWidth( tc.getWidth() );
      columnType.setAlignment( "" + tc.getStyle() ); //$NON-NLS-1$
      columnType.setFormat( column.getFormat() );

      final String modifiderID = column.getModifierID();
      if( !StringUtils.isBlank( modifiderID ) )
        columnType.setModifier( modifiderID );

      /* serialize parameters */
      final List<Param> params = columnType.getParam();

      final Map<String, String> columnParameters = column.getParameters();
      final String[] keys = columnParameters.keySet().toArray( new String[columnParameters.size()] );
      for( final String key : keys )
      {
        final Param param = OF.createGistableviewLayerColumnParam();
        param.setKey( key );
        param.setValue( column.getParam( key ) );

        params.add( param );
      }

      columns.add( columnType );
    }

    final LayerTableSorter sorter = (LayerTableSorter)getSorter();
    final IColumnDescriptor sortColumn = sorter.getColumn();
    if( sortColumn != null )
    {
      final Sort sort = OF.createGistableviewLayerSort();
      final GMLXPath propertyPath = sortColumn.getPropertyPath();
      final String sortPropertyName = ObjectUtils.toString( propertyPath, StringUtils.EMPTY );
      sort.setPropertyName( sortPropertyName );
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
          final Filter ogcFilter = ((FeatureViewerFilter)filter).getFilter();
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

    final IPoolableObjectType key = ((PoolLayerTableInput)input).getPoolKey();

    layer.setId( "id" ); //$NON-NLS-1$
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
        final IPoolableObjectType poolKey = ((PoolLayerTableInput)input).getPoolKey();
        final KeyInfo info = pool.getInfoForKey( poolKey );
        if( info.isDirty() )
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
      final IStructuredSelection sel = (IStructuredSelection)getSelection();
      features = sel.toArray();
    }
    else
    {
      final IFeaturesProvider featuresProvider = getInput();
      if( featuresProvider == null )
        return new String[0][];

      features = featuresProvider.getFeatureList().toFeatures();
    }

    final Collection<String[]> lines = new ArrayList<>();

    final ITableLabelProvider labelProvider = (ITableLabelProvider)getLabelProvider();

    final Table table = getTable();
    final TableColumn[] columns = table.getColumns();

    final String[] firstLine = new String[columns.length];
    for( int j = 0; j < columns.length; j++ )
      firstLine[j] = columns[j].getText();
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
    final IColumnDescriptor column = getColumnDescriptor( columnIndex );
    if( column == null )
      return null;

    return column.getModifier();
  }

  @Override
  public boolean canModify( final Object element, final String property )
  {
    final GMLXPath propertyPath = parseQuietXPath( property );

    // TODO ask modifier also, as for some types editor may not be implemented
    return isEditable( propertyPath );
  }

  @Override
  public Object getValue( final Object element, final String property )
  {
    final GMLXPath propertyPath = parseQuietXPath( property );

    final IFeatureModifier modifier = findModifier( propertyPath );

    if( modifier != null )
      return modifier.getProperty( (Feature)element );

    return null;
  }

  @Override
  public void modify( final Object element, final String property, final Object value )
  {
    final GMLXPath propertyPath = parseQuietXPath( property );

    final IFeatureModifier modifier = findModifier( propertyPath );

    if( modifier != null )
    {
      final TableItem ti = (TableItem)element;
      final Feature feature = (Feature)ti.getData();
      // as result==null does not explicitly mean that
      // the value is invalid, we have to ask the celleditor for invalidity
      final int columnID = getColumnID( propertyPath );
      if( columnID < 0 )
        return;
      final CellEditor[] editors = getCellEditors();
      if( editors == null || editors.length < columnID )
        return;
      if( !editors[columnID].isValueValid() )
        return;

      final Object object = modifier.parseInput( feature, value );
      final Object oldValue = modifier.getProperty( feature );
      if( oldValue != null && oldValue.equals( object ) )
        return;

      // dialogs may return FeatureChange objects (doemming)
      final FeatureChange fc;
      if( object instanceof FeatureChange )
        fc = (FeatureChange)object;
      else
      {
        final IPropertyType pt = modifier.getPropertyType();
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

  private IFeatureModifier findModifier( final GMLXPath propertyPath )
  {
    final TableColumn tc = getColumn( propertyPath );
    final IColumnDescriptor column = getDescriptor( tc );
    if( column == null )
      return null;

    return column.getModifier();
  }

  @Override
  public ISelection getSelection( )
  {
    final IFeaturesProvider input = getInput();
    if( input == null )
      return super.getSelection();

    final IStructuredSelection selection = (IStructuredSelection)super.getSelection();

    final FeatureList featureList = input.getFeatureList();
    final CommandableWorkspace workspace = input.getWorkspace();

    if( m_tableCursor == null )
      return new KalypsoFeatureThemeSelection( selection.toList(), featureList, workspace, m_selectionManager, null, null );

    final TableItem row = m_tableCursor.getRow();
    final int column = m_tableCursor.getColumn();
    if( row != null && row.getData() instanceof Feature )
    {
      final Feature focusedFeature = (Feature)row.getData();

      final IFeatureModifier modifier = getModifier( column );

      final IPropertyType focusedProperty = modifier == null ? null : modifier.getPropertyType();

      return new KalypsoFeatureThemeSelection( selection.toList(), featureList, workspace, m_selectionManager, focusedFeature, focusedProperty );
    }

    return StructuredSelection.EMPTY;
  }

  @Override
  protected void inputChanged( final Object input, final Object oldInput )
  {
    clearColumns();

    if( oldInput instanceof ILayerTableInput )
      ((ILayerTableInput)oldInput).dispose();

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

  public IFeatureSelectionManager getSelectionManager( )
  {
    return m_selectionManager;
  }

  public LayerTableStyle getStyle( final int columnIndex )
  {
    final TableColumn[] columns = getTable().getColumns();
    if( columnIndex < 0 || columnIndex > columns.length - 1 )
      return null;

    final TableColumn tc = columns[columnIndex];

    final IColumnDescriptor column = getDescriptor( tc );

    return column.getStyle();
  }

  public static IColumnDescriptor getDescriptor( final TableColumn tc )
  {
    return (IColumnDescriptor)tc.getData( PROPERTY_COLUMN_DESCRIPTOR );
  }
}