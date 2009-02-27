package de.belger.swtchart.legend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

import de.belger.swtchart.ChartCanvas;
import de.belger.swtchart.IChartCanvasListener;
import de.belger.swtchart.layer.IChartLayer;

/**
 * Provides a legend for a {@link de.belger.swtchart.ChartCanvas}.
 * <p>
 * The legend shows all contained layers in the chart in a checklist and controls its visibility.
 * </p>
 * <p>
 * Painting of the layers symbol is delegated to the layers
 * </p>
 * 
 * @see de.belger.swtchart.ChartCanvas
 * @see de.belger.swtchart.layer.IChartLayer
 * @author gernot
 */
public class ChartLegend extends LabelProvider implements DisposeListener, IChartCanvasListener, ICheckStateListener, ISelectionChangedListener, IPersistableElement
{
  private static final String MEM_SELCOUNT = "memento.chartLegend.selCount";

  private static final String MEM_SELITEM = "memento.chartLegend.selItem";

  private final CheckboxTableViewer m_checklist;

  private final static Map<String, Image> m_images = new HashMap<String, Image>();

  private final boolean m_adaptgridsize;

  private final ChartCanvas m_chart;

  private String[] m_lastSelection = new String[0];

  private boolean m_lockselection = false;

  public ChartLegend( final Composite parent, final int style, final ChartCanvas chart, final boolean adaptgridsize )
  {
    m_chart = chart;
    m_adaptgridsize = adaptgridsize;

    m_checklist = CheckboxTableViewer.newCheckList( parent, style );
    m_checklist.getControl().addDisposeListener( this );

    m_checklist.setContentProvider( new ArrayContentProvider() );
    m_checklist.setLabelProvider( this );
    m_checklist.addCheckStateListener( this );
    m_checklist.addSelectionChangedListener( this );

    m_checklist.setInput( m_chart.getLayers() );

    m_chart.addLayerListener( this );

    onLayersChanged();
  }

  @Override
  public void dispose( )
  {
    if( m_chart != null )
      m_chart.removeLayerListener( this );

    clearImages();
  }

  private void clearImages( )
  {
    for( final Image i : m_images.values() )
      i.dispose();

    m_images.clear();
  }

  /**
   * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
   */
  public void widgetDisposed( final DisposeEvent e )
  {
    dispose();
  }

  /**
   * @see de.belger.swtchart.IChartCanvasListener#onLayersChanged()
   */
  public void onLayersChanged( )
  {
    final Control checkcontrol = m_checklist.getControl();

    final Runnable runnable = new Runnable()
    {
      public void run( )
      {
        layersChangedInternal( checkcontrol );
      }
    };

    if( !checkcontrol.isDisposed() )
      checkcontrol.getDisplay().asyncExec( runnable );
  }

  /**
   * @exception SWTException
   *                <ul>
   *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *                </ul>
   */
  protected void layersChangedInternal( final Control checkcontrol )
  {
    if( checkcontrol.isDisposed() )
      return;

    m_lockselection = true;

    try
    {
      m_checklist.refresh();

      if( m_chart != null )
      {
        for( final IChartLayer l : m_chart.getLayers() )
        {
          m_checklist.setGrayed( l, l.isNotPainting() );
          m_checklist.setChecked( l, m_chart.isVisible( l ) );
        }

        m_checklist.setSelection( calcSelection( m_lastSelection ), true );
      }

      if( m_adaptgridsize )
      {
        final Control control = checkcontrol;
        final Object layoutData = control.getLayoutData();
        if( layoutData instanceof GridData )
        {
          final Point size = control.computeSize( SWT.DEFAULT, SWT.DEFAULT, true );
          ((GridData) layoutData).minimumWidth = size.x;
          ((GridData) layoutData).minimumHeight = size.y;
        }
        control.getParent().layout( true, true );
      }
    }
    finally
    {
      m_lockselection = false;
    }
  }

  /**
   * Try to recall old selection. Compares layers by name ( {@link IChartLayer#toString()})
   * 
   * @param selection
   */
  private ISelection calcSelection( final String[] selection )
  {
    // make hash from newlayers
    final Collection<IChartLayer> layers = m_chart.getLayers();

    final List<IChartLayer> layersToSelect = new ArrayList<IChartLayer>();
    if( selection.length > 0 )
    {
      final Map<String, IChartLayer> namemap = new HashMap<String, IChartLayer>();
      for( final IChartLayer layer : layers )
        namemap.put( layer.toString(), layer );

      for( final String oldName : selection )
      {
        final IChartLayer layer = namemap.get( oldName );
        if( layer != null )
          layersToSelect.add( layer );
      }
    }

    if( layersToSelect.size() == 0 && layers.size() > 0 )
      layersToSelect.add( layers.iterator().next() );

    return new StructuredSelection( layersToSelect );
  }

  /**
   * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
   */
  public void checkStateChanged( final CheckStateChangedEvent event )
  {
    final IChartLayer layer = (IChartLayer) event.getElement();
    m_chart.setVisible( layer, event.getChecked() );
  }

  /**
   * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
   */
  @Override
  public Image getImage( final Object element )
  {
    final IChartLayer layer = (IChartLayer) element;

    final Image oldimage = m_images.get( layer.getId() );
    if( oldimage != null )
      return oldimage;

    final Display display = m_checklist.getControl().getDisplay();
    final Image image = new Image( display, 25, 25 );

    final GCWrapper gc = new GCWrapper( new GC( image ) );
    final Rectangle clipping = gc.getClipping();
    gc.setClipping( clipping.x + 1, clipping.y + 1, clipping.width - 1, clipping.height - 1 );
    layer.paintLegend( gc );

    gc.setClipping( clipping );
    gc.setForeground( display.getSystemColor( SWT.COLOR_BLACK ) );
    gc.setLineWidth( 1 );
    gc.drawRectangle( clipping.x, clipping.y, clipping.width - 1, clipping.height - 1 );
    gc.dispose();

    m_images.put( layer.getId(), image );

    return image;
  }

  public Control getControl( )
  {
    return m_checklist.getControl();
  }

  public ISelectionProvider getSelectionProvider( )
  {
    return m_checklist;
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
   */
  public void selectionChanged( final SelectionChangedEvent event )
  {
    if( !m_lockselection )
    {
      final ISelection selection = event.getSelection();
      if( selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection) )
        m_lastSelection = new String[0];
      else
      {
        final IStructuredSelection struct = (IStructuredSelection) selection;
        m_lastSelection = new String[struct.size()];
        int count = 0;
        for( final Iterator sIt = struct.iterator(); sIt.hasNext(); )
          m_lastSelection[count++] = sIt.next().toString();
      }
    }
  }

  /**
   * @see org.eclipse.ui.IPersistableElement#getFactoryId()
   */
  public String getFactoryId( )
  {
    return null;
  }

  /**
   * @see org.eclipse.ui.IPersistableElement#saveState(org.eclipse.ui.IMemento)
   */
  public void saveState( final IMemento memento )
  {
    memento.putInteger( MEM_SELCOUNT, m_lastSelection.length );
    for( int i = 0; i < m_lastSelection.length; i++ )
      memento.putString( MEM_SELITEM + i, m_lastSelection[i] );
  }

  public void restoreState( final IMemento memento )
  {
    final Integer integer = memento.getInteger( MEM_SELCOUNT );
    if( integer == null )
      m_lastSelection = new String[0];
    else
    {
      m_lastSelection = new String[integer.intValue()];
      for( int i = 0; i < integer.intValue(); i++ )
        m_lastSelection[i] = memento.getString( MEM_SELITEM + i );
    }

    onLayersChanged();
  }
}
