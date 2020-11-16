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
 * Copyright 2011 Curam Software Ltd.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Curam
 * Software, Ltd. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Curam Software.
 */
package assessmentplanning.delivery;

import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.path.util.ClientPaths;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import outcomeplanning.internal.DateFormatStrings;

/**
 * View Renderer for the Assessment Delivery overview control.
 * 
 * @since 6.0
 */
public class AssessmentDeliveryOverviewDetailsRenderer extends
  AbstractViewRenderer {

  /**
   * The path to the overview details SWF.
   */
  private static final String kFLEX_BAR_AND_SPIDER_CHART_SWF =
    "../flex/BarAndSpiderChart.swf";

  /**
   * The width to be used for the chart.
   */
  private static final String kCHART_WIDTH = "100%";

  /**
   * The height to be used for the chart.
   */
  private static final String kCHART_HEIGHT = "200";

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final RendererContract arg3) throws ClientException, DataAccessException,
    PlugInException {

    final String id;
    final Element object;
    final Element embed;
    Element param;
    // CDR (SBN) - Consider adding this as a parameter instead
    id = "AssessmentDeliveryHome";

    final Path sourcePath;
    sourcePath = field.getBinding().getSourcePath();
    final String value =
      (String) context.getDataAccessor().getRaw(sourcePath);

    context.includeScriptURIs("text/javascript", "../CDEJ/jscript/flexJS.js");

    // Create the "object" element for the Flash Player plug-in.
    object = documentFragment.getOwnerDocument().createElement("object");
    object.setAttribute("id", id);
    object.setAttribute("WIDTH", kCHART_WIDTH);
    // CDR (SBN) - Consider adding this as a parameter instead
    object.setAttribute("HEIGHT", kCHART_HEIGHT);
    object.setAttribute("allowScriptAccess", "always");
    object
      .setAttribute("codebase",
        "https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab");
    object.setAttribute("classid",
      "clsid:D27CDB6E-AE6D-11cf-96B8-444553540000");

    documentFragment.appendChild(object);

    // Add the parameters to the object. Note that the path to the SWF
    // resource
    // is relative to the current page. As all page URIs have the form
    // "<locale>/<page-id>Page.do" relative to the application context, we
    // need
    // to use ".." before the relative path to return to the "WebContent"
    // location (the root location for the application) from the "<locale>"
    // pseudo-location. Note also that these are URIs, not file paths, so
    // only
    // forward slashes should be used.
    param = documentFragment.getOwnerDocument().createElement("param");
    param.setAttribute("name", "src");
    param.setAttribute("value", kFLEX_BAR_AND_SPIDER_CHART_SWF);
    object.appendChild(param);

    param = documentFragment.getOwnerDocument().createElement("param");
    param.setAttribute("name", "wmode");
    param.setAttribute("value", "opaque");
    object.appendChild(param);

    final StringBuffer flashVars = constructFlashVars(field, context, value);
    final Element flashVarsParam =
      documentFragment.getOwnerDocument().createElement("param");
    flashVarsParam.setAttribute("name", "flashVars");
    flashVarsParam.setAttribute("value", flashVars.toString());

    object.appendChild(flashVarsParam);

    // Add the "embed" element for Netscape and IE/Mac support.
    embed = documentFragment.getOwnerDocument().createElement("embed");
    embed.setAttribute("height", kCHART_HEIGHT);
    embed.setAttribute("width", kCHART_WIDTH);
    embed.setAttribute("src", kFLEX_BAR_AND_SPIDER_CHART_SWF);
    embed.setAttribute("name", id);
    embed.setAttribute("allowScriptAccess", "always");
    embed.setAttribute("pluginspage",
      "https://www.adobe.com/go/getflashplayer");
    embed.setAttribute("wmode", "opaque");
    embed.setAttribute("flashVars", flashVars.toString());

    object.appendChild(embed);
  }

  /**
   * Constructs the string buffer for the flash vars.
   * 
   * @param field
   * The field on the client page
   * @param context
   * The renderer context
   * @param assessmentCaseID
   * The unique ID of the assessment case
   * @return A string buffer containing the flash vars.
   * @throws DataAccessException
   * Generic Exception Signature
   */
  private StringBuffer constructFlashVars(final Field field,
    final RendererContext context, final String assessmentCaseID)
    throws DataAccessException {

    // Get the value of the page param
    final String pageParamValue =
      context.getDataAccessor().get(
        ClientPaths.PARAM_PATH.extendPath("caseID"));

    final String assessmentInstanceID =
      context.getDataAccessor().get(
        ClientPaths.PARAM_PATH.extendPath("assessmentInstanceID"));

    final String executionID =
      context.getDataAccessor().get(
        ClientPaths.PARAM_PATH.extendPath("executionID"));

    final StringBuffer flashVars = new StringBuffer();

    if (assessmentCaseID.length() > 0) {
      flashVars.append("assessmentCaseID=").append(assessmentCaseID);
    } else {
      flashVars.append("assessmentCaseID=").append(pageParamValue);
    }

    if (assessmentInstanceID != null) {
      flashVars.append("&assessmentInstanceID=").append(assessmentInstanceID);
    }

    if (executionID != null) {
      flashVars.append("&executionID=").append(executionID);
    }

    flashVars.append("&url=").append(context.getPageID());
    flashVars.append("&dateFormat=").append(DateFormatStrings.getFormat());
    flashVars.append("&locale=").append(getLocale());

    return flashVars;
  }
}
