package org.kalypso.chart.ext.base.layer.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.TimeZone;

import org.kalypso.chart.ext.base.data.AbstractDomainValueData;
import org.kalypso.chart.ext.base.layer.DefaultLineLayer;
import org.kalypso.chart.factory.configuration.exception.LayerProviderException;
import org.kalypso.chart.factory.provider.AbstractLayerProvider;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.impl.ComparableDataRange;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.util.math.ComparableNumber;


/**
 * @author alibu
 *
 */
public class UVFLayerProvider extends AbstractLayerProvider
{


	/**
	 * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayer(java.net.URL)
	 */
	public IChartLayer getLayer(URL context) throws LayerProviderException
	{
		IChartLayer<Calendar, ComparableNumber> icl = null;


		String domainAxisId = m_lt.getMapper().getDomainAxisRef().getRef();
		String targetAxisId = m_lt.getMapper().getTargetAxisRef().getRef();

		IAxis<Calendar> domAxis = m_model.getMapperRegistry().getAxis(domainAxisId);
		IAxis<ComparableNumber> valAxis = m_model.getMapperRegistry().getAxis(targetAxisId);

		AbstractDomainValueData<Calendar, ComparableNumber> data = new AbstractDomainValueData<Calendar, ComparableNumber>()
		{

			public boolean openData()
			{
				try
				{
					FileReader fr = new FileReader(m_file);

					BufferedReader br = new BufferedReader(fr);
					String s = "";
					int count = 0;
					while ((s = br.readLine()) != null)
					{
						if (count > 4)
						{
							String[] cols = s.split("  *");
							//YearString
							if (cols.length >= 2)
							{
								String ys = cols[0];
								//Datum zerpflücken (Bsp: 0510190530)
								//TODO: Auslagern in Toolbox-ähnliche Klasse
								int year = 2000 + Integer.parseInt(ys
										.substring(0, 2));
								int month = Integer
										.parseInt(ys.substring(2, 4)) - 1;
								int day = Integer.parseInt(ys.substring(4, 6));
								int hour = Integer.parseInt(ys.substring(6, 8));
								int minute = Integer.parseInt(ys.substring(8,
										10));
								Calendar cal = Calendar.getInstance();

								cal.set(Calendar.YEAR, year);
								cal.set(Calendar.MONTH, month);
								cal.set(Calendar.DAY_OF_MONTH, day);
								cal.set(Calendar.HOUR_OF_DAY, hour);
								cal.set(Calendar.MINUTE, minute);
								cal.set(Calendar.SECOND, 0);
								cal.set(Calendar.MILLISECOND, 0);
								//wichtig, damit die Zeiten richtig sind
								cal.setTimeZone(TimeZone
										.getTimeZone("GMT+0000"));
								m_domainValues.add(cal);
								m_targetValues.add(new ComparableNumber(Double.parseDouble(cols[1])));
							}
						}
						count++;
					}
				} catch (FileNotFoundException e)
				{
					e.printStackTrace();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
				return true;

			}

			public IDataRange<Calendar> getDomainRange() {
				return new ComparableDataRange<Calendar>(getDomainValues());
			}

			public IDataRange<ComparableNumber> getTargetRange() {
				return new ComparableDataRange<ComparableNumber>(getTargetValues());
			}

		};
		data.setInputFile(new File(m_pc.getParameterValue("url", m_lt.getId())));

		icl = new DefaultLineLayer<Calendar, ComparableNumber>(data, domAxis, valAxis);
		icl.setTitle(m_lt.getTitle());

		return icl;
	}


}
