package org.kalypso.gml.ui.commands.exportshape;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.shape.IShapeData;
import org.kalypso.shape.ShapeWriter;
import org.kalypso.shape.dbf.DBaseException;
import org.kalypso.shape.deegree.IShapeDataFactory;

/**
 * @author belger
 *
 */
public final class ExportShapeOperation implements ICoreRunnableWithProgress
{
  private final Charset m_shapeCharset;

  private final String m_shapeFileBase;

  private final IShapeDataFactory m_shapeDataFactory;

  public ExportShapeOperation( Charset shapeCharset, String shapeFileBase, IShapeDataFactory shapeDataFactory )
  {
    m_shapeCharset = shapeCharset;
    m_shapeFileBase = shapeFileBase;
    m_shapeDataFactory = shapeDataFactory;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws CoreException, InvocationTargetException
  {
    try
    {
      final IShapeData dataProvider = m_shapeDataFactory.createData();

      final ShapeWriter shapeWriter = new ShapeWriter( dataProvider );
      shapeWriter.write( m_shapeFileBase, m_shapeCharset, monitor );
    }
    catch( final CoreException e )
    {
      throw e;
    }
    catch( final DBaseException e )
    {
      throw new InvocationTargetException( e, "Failed to create default shape provider" );
    }
    catch( final Exception e )
    {
      throw new InvocationTargetException( e );
    }
    return Status.OK_STATUS;
  }
}