<?xml version="1.0" encoding="UTF-8"?>
<FeatureTypeStyle xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc">
    <Name>default</Name>
    <Title>Dreiecks-Netz</Title>
    <FeatureTypeName>{org.kalypso.gml.processes.mesh}Triangle</FeatureTypeName>
    <Rule>
        <Name>default</Name>
        <Title>default</Title>
        <Abstract>default</Abstract>
        <MinScaleDenominator>0.0</MinScaleDenominator>
        <MaxScaleDenominator>9.9999999901E8</MaxScaleDenominator>
        <PolygonSymbolizer>
            <Geometry>
                <ogc:PropertyName>triangle</ogc:PropertyName>
            </Geometry>
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
