<?xml version="1.0" encoding="UTF-8"?>
<FeatureTypeStyle xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:om="http://www.opengis.net/om">
	<Name>waterBody</Name>
	<Title>Water Body Style</Title>
	<Rule>
		<Name>centerline</Name>
		<Title>Achse</Title>
		<TextSymbolizer>
			<Geometry>
				<ogc:PropertyName>centerLine</ogc:PropertyName>
			</Geometry>
			<Label>
				<ogc:PropertyName>name</ogc:PropertyName>
			</Label>
			<Font>
				<CssParameter name="font-family">Dialog</CssParameter>
				<CssParameter name="font-color">#0000ff</CssParameter>
				<CssParameter name="font-size">12.0</CssParameter>
				<CssParameter name="font-style">normal</CssParameter>
				<CssParameter name="font-weight">normal</CssParameter>
			</Font>
			<LabelPlacement>
				<LinePlacement>
					<PerpendicularOffset>auto</PerpendicularOffset>
					<LineWidth>3</LineWidth>
					<Gap>10</Gap>
				</LinePlacement>
			</LabelPlacement>
		</TextSymbolizer>
		<LineSymbolizer uom="pixel">
			<Geometry>
				<ogc:PropertyName>centerLine</ogc:PropertyName>
			</Geometry>
			<Stroke>
				<CssParameter name="stroke">#0000ff</CssParameter>
				<CssParameter name="stroke-width">3.0</CssParameter>
				<CssParameter name="stroke-linejoin">round</CssParameter>
				<CssParameter name="stroke-opacity">1.0</CssParameter>
				<CssParameter name="stroke-linecap">round</CssParameter>
			</Stroke>
		</LineSymbolizer>
	</Rule>
</FeatureTypeStyle>