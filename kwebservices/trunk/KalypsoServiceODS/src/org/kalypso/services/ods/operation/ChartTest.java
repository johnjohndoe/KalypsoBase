package org.kalypso.services.ods.operation;
/*
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.Plot;
import org.kalypso.swtchart.chart.configuration.ChartLoader;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
*/


public class ChartTest {

	/*
public static Image createChart (Display disp, Image img, String chartname) {
		
		
		Shell shell = new Shell( disp );
		shell.setSize( 800, 600 );
		shell.setLayout( new FillLayout() );
		Chart chart = new Chart( shell, SWT.NONE );
		ChartLoader cl=new ChartLoader(chart, "/home/alibu/dev/workspace32/KalypsoChart/etc/binding/examples/Configuration.xml");
		cl.createChart(chartname);
		chart.layout();
		chart.update();
		chart.setSize(new Point(1000,800));
		
		GC gc=new GC(img);
		GCWrapper gcw=new GCWrapper(gc);
		//Image img2=( (Plot) chart.getPlot()).getImgBuffer();
		
		Plot plot=(Plot) chart.getPlot();
		img=plot.paintChart(disp, gcw, img.getBounds(), null);
		//img=plot.getBufferImg();
		chart.dispose();
		gc.dispose();
		
		
		
		
		//gc.copyArea(img2, img2.getBounds().x, img2.getBounds().y);
		//Image img=null;//((Plot) chart.getPlot()).getBufferImg();
		return  img;
        
	}
    */
}
