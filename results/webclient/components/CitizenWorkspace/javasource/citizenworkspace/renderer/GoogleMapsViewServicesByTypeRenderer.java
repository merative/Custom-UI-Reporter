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
/*
 * Copyright 2009-2011 Curam Software Ltd.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Curam
 * Software, Ltd. ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Curam Software.
 */
package citizenworkspace.renderer;

import citizenaccount.renderer.LifeEventsRecommenationsRenderer;
import citizenaccount.renderer.RendererUtil;
import citizenworkspace.pageplayer.HTMLConsts;
import citizenworkspace.util.StringHelper;
import curam.ieg.player.PlayerUtils;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import curam.util.common.util.JavaScriptEscaper;
import curam.util.dom.html2.HTMLUtils;
import java.util.Locale;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

// REVIEWED, Damian McGrath 05/07/2010

// REVIEW_LOW, get rid of redundant import and fix other checkstyle errors

// REVIEW_HIGH, I am reviewing for pluggability of maps implementation
// in general. It seems that the renderer is the most granular level at
// which we can customise the map display...
// I'd suggest, to begin with we at least do the following:
// - Publish xml required by the renderers
// - Publish interface to the AJAX servlet
// - Publish javascript interface from community list to map

// REVIEW_MEDIUM, Look at removing tab logic from service_by_type_google.js
// renderer

/**
 * This is used by the ServiceProviders page to render javascript to display the
 * google map.
 */
public class GoogleMapsViewServicesByTypeRenderer extends
  AbstractViewRenderer {

  /**
   * The Curam Map DIV ID.
   */
  public static final String CURAM_MAP_DIV_ID = "curam_map_div_id";

  private static final String ID = "id";

  private static final String propertiesFileName = "ServiceProviders";

  /**
   * {@inheritDoc}
   */
  // CHECKSTYLE_OFF: MethodLength
  @Override
  public void render(final Field field, final DocumentFragment fragment,
    final RendererContext context, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    final String mapID = createMap(fragment);

    String zoomLevel = new String();
    String centerLatitude = new String();
    String centerLongitude = new String();
    String userAddress = new String();
    boolean geocodeRequired = false;
    String defaultCenterLatitude = new String();
    String defaultCenterLongitude = new String();

    final Path pinsMinZoomLevelPath =
      field.getBinding().getSourcePath().extendPath("pins-min-zoom-level");

    final String pinsMinZoomLevel =
      context.getDataAccessor().get(pinsMinZoomLevelPath);

    final Path apiKeyPath =
      field.getBinding().getSourcePath().extendPath("map-api-key");

    final String mapKey = context.getDataAccessor().get(apiKeyPath);

    final Path pagePlayerIDPath =
      field.getBinding().getSourcePath().extendPath("id");

    final String pagePlayerID =
      context.getDataAccessor().get(pagePlayerIDPath);

    final Path clientIDPath =
      field.getBinding().getSourcePath().extendPath("business-map-api-key");

    final String clientID = context.getDataAccessor().get(clientIDPath);

    final Path configPath =
      field.getBinding().getSourcePath().extendPath("config");

    if (!configPath.isEmpty()) {
      zoomLevel =
        context.getDataAccessor().get(configPath.extendPath("@zoom-level"));
      centerLatitude =
        context.getDataAccessor().get(
          configPath.extendPath("@center-latitude"));
      centerLongitude =
        context.getDataAccessor().get(
          configPath.extendPath("@center-longitude"));
      userAddress =
        context.getDataAccessor().get(
          configPath.extendPath("@address-string"));
      defaultCenterLatitude =
        context.getDataAccessor().get(
          configPath.extendPath("@default-center-latitude"));
      defaultCenterLongitude =
        context.getDataAccessor().get(
          configPath.extendPath("@default-center-longitude"));
      geocodeRequired =
        Boolean.valueOf(context.getDataAccessor().get(
          configPath.extendPath("@geocode-required")));
    }

    final Path geoBiasPath =
      field.getBinding().getSourcePath().extendPath("geo-bias-config");

    final String north =
      context.getDataAccessor().get(geoBiasPath.extendPath("/north"));
    final String south =
      context.getDataAccessor().get(geoBiasPath.extendPath("/south"));
    final String east =
      context.getDataAccessor().get(geoBiasPath.extendPath("/east"));
    final String west =
      context.getDataAccessor().get(geoBiasPath.extendPath("/west"));

    final StringBuffer scriptText = new StringBuffer();

    // Need to pass the localized messages into the Javascript
    final String addressNotFound =
      PlayerUtils.getProperty(propertiesFileName, "Address.Not.Found",
        context);
    final String multipeAddressesFound =
      PlayerUtils.getProperty(propertiesFileName, "Multipe.Addresses.Found",
        context);
    final String addressNotInRange =
      PlayerUtils.getProperty(propertiesFileName, "Address.Not.In.Range",
        context);
    final String moreInfoLinkText =
      PlayerUtils.getProperty(propertiesFileName,
        "More.Information.Link.Text", context);
    final String unknownAddress =
      PlayerUtils.getProperty(propertiesFileName, "Unknown.Address", context);
    final String serverError =
      PlayerUtils.getProperty(propertiesFileName, "Server.Error", context);
    final String missingQuery =
      PlayerUtils.getProperty(propertiesFileName, "Missing.Query", context);
    final String badKey =
      PlayerUtils.getProperty(propertiesFileName, "Bad.Key", context);
    final String badRequest =
      PlayerUtils.getProperty(propertiesFileName, "Bad.Request", context);
    final String unknownError =
      PlayerUtils.getProperty(propertiesFileName, "Unknown.Error", context);
    final String httpStatusCode =
      PlayerUtils
        .getProperty(propertiesFileName, "Http.Status.Code", context);

    scriptText.append("var ADDRESS_NOT_FOUND = '"
      + JavaScriptEscaper.escapeText(addressNotFound) + "';");
    scriptText.append("var MULTIPLE_ADDRESSES_FOUND = '"
      + JavaScriptEscaper.escapeText(multipeAddressesFound) + "';");
    scriptText.append("var ADDRESS_NOT_IN_RANGE = '"
      + JavaScriptEscaper.escapeText(addressNotInRange) + "';");
    scriptText.append("var MORE_INFO_LINK_TEXT = '"
      + JavaScriptEscaper.escapeText(moreInfoLinkText) + "';");
    scriptText.append("var UNKNOWN_ADDRESS = '"
      + JavaScriptEscaper.escapeText(unknownAddress) + "';");
    scriptText.append("var SERVER_ERROR = '"
      + JavaScriptEscaper.escapeText(serverError) + "';");
    scriptText.append("var MISSING_QUERY = '"
      + JavaScriptEscaper.escapeText(missingQuery) + "';");
    scriptText.append("var BAD_KEY = '"
      + JavaScriptEscaper.escapeText(badKey) + "';");
    scriptText.append("var BAD_REQUEST = '"
      + JavaScriptEscaper.escapeText(badRequest) + "';");
    scriptText.append("var UNKNOWN_ERROR = '"
      + JavaScriptEscaper.escapeText(unknownError) + "';");
    scriptText.append("var HTTP_STATUS_CODE = '"
      + JavaScriptEscaper.escapeText(httpStatusCode) + "';");

    // if a zoom level has been set, assign the value
    if (!StringHelper.isEmpty(zoomLevel)) {
      scriptText.append("zoomLevel="
        + JavaScriptEscaper.escapeText(zoomLevel) + ";");
    }
    if (!StringHelper.isEmpty(pinsMinZoomLevel)) {
      scriptText.append("pinsMinZoomLevel="
        + JavaScriptEscaper.escapeText(pinsMinZoomLevel) + ";");
    }
    scriptText.append("zoomWarningDivID='"
      + LifeEventsRecommenationsRenderer.ZOOM_WARNING_DIV_ID + "';");

    // set centre of map
    if (StringHelper.isEmpty(userAddress)) {

      scriptText.append("var geocodeRequired=false;");
      scriptText.append("var centerLatitude='"
        + JavaScriptEscaper.escapeText(defaultCenterLatitude) + "';");
      scriptText.append("var centerLongitude='"
        + JavaScriptEscaper.escapeText(defaultCenterLongitude) + "';");
      scriptText.append("var userAddress='';");

    } else {
      if (geocodeRequired) {

        scriptText.append("var geocodeRequired=true;");
        // for the moment the centre is the configured centre. This gets updated
        // when the address string is successfully geocoded.
        scriptText.append("var centerLatitude='"
          + JavaScriptEscaper.escapeText(defaultCenterLatitude) + "';");
        scriptText.append("var centerLongitude='"
          + JavaScriptEscaper.escapeText(defaultCenterLongitude) + "';");
        scriptText.append("var userAddress='"
          + JavaScriptEscaper.escapeText(userAddress) + "';");

      } else {
        scriptText.append("var geocodeRequired=false;");
        scriptText.append("var centerLatitude='"
          + JavaScriptEscaper.escapeText(centerLatitude) + "';");
        scriptText.append("var centerLongitude='"
          + JavaScriptEscaper.escapeText(centerLongitude) + "';");
        scriptText.append("var userAddress='"
          + JavaScriptEscaper.escapeText(userAddress) + "';");
      }
    }

    scriptText.append("var latLngBounds='';");
    scriptText.append("var pagePlayerID='"
      + JavaScriptEscaper.escapeText(pagePlayerID) + "';");
    scriptText.append("var sw = '';");
    scriptText.append("var ne = '';");
    scriptText.append("sw = new google.maps.LatLng("
      + JavaScriptEscaper.escapeText(south) + ", "
      + JavaScriptEscaper.escapeText(west) + ");");
    scriptText.append("ne = new google.maps.LatLng("
      + JavaScriptEscaper.escapeText(north) + ", "
      + JavaScriptEscaper.escapeText(east) + ");");
    scriptText.append("latLngBounds = new google.maps.LatLngBounds(sw, ne);");

    // Create javascript objects
    scriptText.append("function linkObject(){ ");
    scriptText.append("this.url='';");
    scriptText.append("this.displayText=''; }");

    context.includeScripts("text/javascript", scriptText.toString());

    // REVIEW_LOW, add a comment to say what these behaviours do.
    // Are these behviours really specific to Triage? Could these
    // behaviours ever be used by other screens? If so consider renaming this
    // file.

    // include the triage behaviors javascript
    context.includeScriptURIs("text/javascript",
      "../jscript/citizenworkspace/triage-behaviors.js");
    final Locale locale = getLocale();

    if (!clientID.isEmpty()) {
      context.includeScriptURIs(
        "text/javascript",
        "https://maps.googleapis.com/maps/api/js?v=3&sensor=false&client="
          + JavaScriptEscaper.escapeText(clientID) + "&language="
          + locale.toString());
    } else if (!mapKey.isEmpty()) {

      context.includeScriptURIs(
        "text/javascript",
        "https://maps.googleapis.com/maps/api/js?v=3&sensor=false&key="
          + JavaScriptEscaper.escapeText(mapKey) + "&language="
          + locale.toString());
    } else {
      context.includeScriptURIs("text/javascript",
        "https://maps.googleapis.com/maps/api/js?v=3&sensor=false&language="
          + locale.toString());
    }

    context.includeScriptURIs("text/javascript",
      "../jscript/citizenworkspace/googlemap.js",

      "../jscript/citizenworkspace/services_by_type_googlemap.js");

    // Need to define a function that will initialize the map identified by
    // its ID and then call this function from the "onload" event. As the
    // function passed to "dojo.addOnLoad" cannot take parameters, we just
    // add an anonymous wrapper function around the real call.
    context.includeScripts("text/javascript",
      "dojo.addOnLoad(function (){initGoogleMap(\"" + mapID + "\");});");

  }

  // CHECKSTYLE_ON: MethodLength

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

    final Element mapContainer =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    mapContainer.setAttribute(HTMLConsts.CLASS, "map-container");
    mapContainer.setAttribute(HTMLConsts.ID, "mapContainerDivId");
    try {
      RendererUtil.attachWaiAriaAttr(mapContainer, "MapContainer");
    } catch (final ClientException e) {
      // To add WAI_ARIA attribute. No need to propagate the exception.
    } catch (final DataAccessException e) {
      // To add WAI_ARIA attribute. No need to propagate the exception.
    }
    HTMLUtils.appendComment(mapContainer, "wrapper for the map div element");

    final Element map =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    final String mapID = CURAM_MAP_DIV_ID;

    map.setAttribute(ID, mapID);
    map.setAttribute(HTMLConsts.CLASS, "servicesMap");
    HTMLUtils.appendComment(map, "the div that will contain the google map");
    try {
      RendererUtil.attachWaiAriaAttr(map, "ServicesMap");
    } catch (final ClientException e) {
      // To add WAI_ARIA attribute. No need to propagate the exception.
    } catch (final DataAccessException e) {
      // To add WAI_ARIA attribute. No need to propagate the exception.
    }
    mapContainer.appendChild(map);

    fragment.appendChild(mapContainer);

    return mapID;
  }
}
