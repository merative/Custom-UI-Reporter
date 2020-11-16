/*
 * Licensed Materials - Property of IBM
 * 
 * PID 5725-H26
 * 
 * Copyright IBM Corporation 2012. All rights reserved.
 * 
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 */
package citizenworkspace.renderer;

import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * Renderer class for the Geocode Bias Map.
 */
public class GeocodeBiasMapRenderer extends AbstractViewRenderer {

  @Override
  public void render(final Field field, final DocumentFragment fragment,
    final RendererContext context, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    try {

      final String mapID = createMap(fragment);
      String northBound = "";
      String southBound = "";
      String eastBound = "";
      String westBound = "";
      String business_map_api_key = "";
      String mapZoomLevel = "";
      boolean staticMap = false;

      final Path staticPath =
        field.getBinding().getSourcePath().extendPath("map");

      staticMap =
        Boolean.valueOf(context.getDataAccessor().get(
          staticPath.extendPath("@static")));

      final Path configPath =
        field.getBinding().getSourcePath().extendPath("map/config");

      northBound =
        context.getDataAccessor().get(configPath.extendPath("@north-bound"));
      southBound =
        context.getDataAccessor().get(configPath.extendPath("@south-bound"));
      eastBound =
        context.getDataAccessor().get(configPath.extendPath("@east-bound"));
      westBound =
        context.getDataAccessor().get(configPath.extendPath("@west-bound"));
      business_map_api_key =
        context.getDataAccessor().get(
          configPath.extendPath("@business-map-api-key"));

      mapZoomLevel =
        context.getDataAccessor().get(
          configPath.extendPath("@default-zoom-level"));

      final StringBuffer scriptText = new StringBuffer();
      scriptText.append("var mapZoomLevel=" + mapZoomLevel + ";");
      scriptText.append("var northBound=" + northBound + ";");
      scriptText.append("var southBound=" + southBound + ";");
      scriptText.append("var eastBound=" + eastBound + ";");
      scriptText.append("var westBound=" + westBound + ";");

      context.includeScripts("text/javascript", scriptText.toString());

      if (!business_map_api_key.isEmpty()) {
        context.includeScriptURIs("text/javascript",
          "https://maps.googleapis.com/maps/api/js?v=3&sensor=false&client="
            + business_map_api_key + "&language=" + getLocale().toString());
      } else {
        context.includeScriptURIs("text/javascript",
          "https://maps.googleapis.com/maps/api/js?v=3&sensor=false&language="
            + getLocale().toString());
      }

      context.includeScriptURIs("text/javascript",
        "../jscript/geocode_bias_googlemap.js");

      // Need to define a function that will initialize the map identified by
      // its ID and then call this function from the "onload" event. As the
      // function passed to "dojo.addOnLoad" cannot take parameters, we just
      // add an anonymous wrapper function around the real call.
      context.includeScripts("text/javascript",
        "dojo.addOnLoad(function (){initGoogleMap(\"" + mapID + "\");});");
      if (staticMap) {
        context.includeScripts("text/javascript", "var staticMap = true;");
      } else {
        context.includeScripts("text/javascript", "var staticMap = false;");
      }

      // Create a div for directions returned from google.
      final Element row = fragment.getOwnerDocument().createElement("tr");
      final Element cell = fragment.getOwnerDocument().createElement("td");
      final Element div = fragment.getOwnerDocument().createElement("div");

      div.setAttribute("id", "directionsDiv");
      div.setAttribute("class", "directions");
      cell.appendChild(div);
      row.appendChild(cell);
      fragment.appendChild(row);

      // createHiddenForm(fragment);

    } catch (final DataAccessException e) {

    }
  }

  // __________________________________________________________________________
  /**
   * Creates the map element and appends the other necessary content to the
   * document fragment. The ID of the map element is returned and can be passed
   * to scripts as necessary. The scripts are not created by this method.
   * 
   * @param fragment
   * The document fragment to which to append the content.
   * @return The ID of the map element.
   */
  protected String createMap(final DocumentFragment fragment) {

    // The ADDRESS_DATA domain causes the CLUSTER element to be only partially
    // rendered to a HTML table at generation time. We need to fill in the
    // "tbody" element here, so a single cell on a single row is required to
    // contain the map.
    final Element row = fragment.getOwnerDocument().createElement("tr");
    final Element cell = fragment.getOwnerDocument().createElement("td");
    final Element map = fragment.getOwnerDocument().createElement("div");
    final String mapID = "google-map-div";

    map.setAttribute("id", mapID);
    // TODO: Move width and height settings to configuration file
    map.setAttribute("style",
      "position:relative; width: 100%; height: 450px;");
    cell.setAttribute("class", "field");
    cell.appendChild(map);
    row.appendChild(cell);
    fragment.appendChild(row);

    return mapID;
  }
}
