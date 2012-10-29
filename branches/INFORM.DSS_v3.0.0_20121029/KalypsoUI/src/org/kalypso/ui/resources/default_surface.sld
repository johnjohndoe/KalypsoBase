<?xml version="1.0" encoding="UTF-8"?>
<FeatureTypeStyle xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc">
	<Name>style</Name>
	<Title>%fts_title</Title>
	<Abstract>%fts_abstract</Abstract>
	<Rule>
		<Name>default</Name>
		<Title>%rule_default_titel</Title>
		<Abstract>%rule_default_abstract</Abstract>
		<PolygonSymbolizer>
			<Fill>
				<CssParameter name="fill-opacity">0.3</CssParameter>
				<CssParameter name="fill">#c0c0c0</CssParameter>
			</Fill>
			<Stroke>
				<CssParameter name="stroke">#808080</CssParameter>
				<CssParameter name="stroke-width">1.0</CssParameter>
				<CssParameter name="stroke-linejoin">mitre</CssParameter>
				<CssParameter name="stroke-opacity">1.0</CssParameter>
				<CssParameter name="stroke-linecap">butt</CssParameter>
			</Stroke>
		</PolygonSymbolizer>
	</Rule>
</FeatureTypeStyle>
