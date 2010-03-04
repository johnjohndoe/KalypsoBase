<?xml version="1.0" encoding="UTF-8"?>
<FeatureTypeStyle xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc">
	<Name>style</Name>
	<Title>%fts_title</Title>
	<Abstract>%fts_abstract</Abstract>
	<Rule>
		<Name>default</Name>
		<Title>%rule_default_title</Title>
		<Abstract>%rule_default_abstract</Abstract>
		<PolygonSymbolizer>
			<Fill>
				<CssParameter name="fill-opacity">0.7</CssParameter>
				<CssParameter name="fill">#ffff00</CssParameter>
			</Fill>
			<Stroke>
				<CssParameter name="stroke">#ff0000</CssParameter>
				<CssParameter name="stroke-width">2.0</CssParameter>
				<CssParameter name="stroke-linejoin">mitre</CssParameter>
				<CssParameter name="stroke-opacity">1.0</CssParameter>
				<CssParameter name="stroke-linecap">butt</CssParameter>
			</Stroke>
		</PolygonSymbolizer>
	</Rule>
</FeatureTypeStyle>
