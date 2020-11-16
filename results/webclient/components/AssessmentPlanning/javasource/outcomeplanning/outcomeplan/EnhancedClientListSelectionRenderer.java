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
package outcomeplanning.outcomeplan;

import curam.util.client.ClientException;
import curam.util.client.domain.render.edit.AbstractEditRenderer;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * An edit renderer that displays the a flex widget to capture a list of
 * ConcernRoleIDs.
 */
public class EnhancedClientListSelectionRenderer extends AbstractEditRenderer {

  /**
   * The width to be used for the client selection widget.
   */
  private static final String kCLIENT_SELECTION_WIDTH = "100%";

  /**
   * The height to be used for the client selection widget.
   */
  private static final String kCLIENT_SELECTION_HEIGHT = "400";

  /**
   * The path to the enhanced client selection SWF.
   */
  private static final String kFLEX_ENHANCED_CLIENT_SELECTION_SWF =
    "../flex/EnhancedClientSelection.swf";

  /**
   * Renderers the field by using its value as the value of a label embedded in
   * a Flex widget displayed by the Adobe Flash Plugin.
   * 
   * @param field
   * Everything we need to know about what we need to render.
   * @param documentFragment
   * An empty fragment that must be populated with DOM content (HTML,
   * text, etc.).
   * @param context
   * Provides the facilities needed to get the values of the data
   * referenced by the field and to add other content to the page,
   * e.g., JavaScript inclusions.
   * @param contract
   * Not used for now.
   */
  @Override
  public void render(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final RendererContract contract) throws ClientException,
    DataAccessException, PlugInException {

    String al3;
    final Path sourcePath;
    final String id;
    final String name;
    final Element object;
    final Element embed;
    Element param;

    // get the source value passed to the renderer
    sourcePath = field.getBinding().getSourcePath();

    al3 = context.getDataAccessor().get(sourcePath);

    String targetID;
    final String label = field.getParameters().get("LABEL");
    final String targetPath = field.getBinding().getTargetPath().toString();
    final Element input =
      documentFragment.getOwnerDocument().createElement("input");

    /*
     * This makes the association between a hidden input field value and the
     * target path that will be available in the action phase. If an error
     * occurs during validation, the label will be used in the error message to
     * identify the field on the page.
     */
    targetID = context.addFormItem(field, label, targetPath);
    input.setAttribute("name", targetID);
    input.setAttribute("id", targetID);
    input.setAttribute("type", "hidden");
    input.setAttribute("wmode", "opaque");
    input.setAttribute("class", "flex-widget");
    documentFragment.appendChild(input);

    /*
     * Generate a unique ID for this Flex label widget. Not really needed here,
     * but it demonstrates how to give each instance of a widget a different ID
     * in case there is more than one on the same page. The "Math.abs()" call
     * ensures that when the integer value wraps around eventually, that there
     * will be no minus sign in the ID, as a minus sign is not a valid character
     * in a HTML ID value.
     */
    id = "EnhancedSelectClients";
    name = "enhanced_select_clients";
    // Create the "object" element for the Flash Player plug-in.
    object = documentFragment.getOwnerDocument().createElement("object");
    object.setAttribute("id", id);
    object.setAttribute("name", name);
    object.setAttribute("width", kCLIENT_SELECTION_WIDTH);
    object.setAttribute("height", kCLIENT_SELECTION_HEIGHT);
    object.setAttribute("class", "flex-widget");
    object.setAttribute("allowScriptAccess", "sameDomain");
    object
      .setAttribute("codebase",
        "https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab");
    object.setAttribute("classid",
      "clsid:D27CDB6E-AE6D-11cf-96B8-444553540000");

    documentFragment.appendChild(object);

    /*
     * Add the parameters to the object. Note that the path to the SWF resource
     * is relative to the current page. As all page URIs have the form
     * "<locale>/<page-id>Page.do" relative to the application context, we need
     * to use ".." before the relative path to return to the "WebContent"
     * location (the root location for the application) from the "<locale>"
     * pseudo-location. Note also that these are URIs, not file paths, so only
     * forward slashes should be used.
     */
    param = documentFragment.getOwnerDocument().createElement("param");
    param.setAttribute("name", "src");
    param.setAttribute("value", kFLEX_ENHANCED_CLIENT_SELECTION_SWF);
    object.appendChild(param);

    // The "flashVars" facility is used to pass the data to the widget. The
    // value is not escaped here, but it really should be in production code.
    param = documentFragment.getOwnerDocument().createElement("param");
    param.setAttribute("name", "flashVars");
    param.setAttribute("value", "concernRoleID=" + al3);

    object.appendChild(param);
    // set up a param for the target to be set
    param = documentFragment.getOwnerDocument().createElement("param");
    param.setAttribute("name", "flashVars");
    param.setAttribute("value", "targetID=" + targetID);

    object.appendChild(param);

    param = documentFragment.getOwnerDocument().createElement("param");
    param.setAttribute("name", "wmode");
    param.setAttribute("value", "opaque");

    object.appendChild(param);

    // Add the "embed" element for Netscape and IE/Mac support.
    embed = documentFragment.getOwnerDocument().createElement("embed");
    embed.setAttribute("flashVars", "concernRoleID=" + al3);
    embed.setAttribute("width", kCLIENT_SELECTION_WIDTH);
    embed.setAttribute("height", kCLIENT_SELECTION_HEIGHT);
    embed.setAttribute("src", kFLEX_ENHANCED_CLIENT_SELECTION_SWF);
    embed.setAttribute("name", id);
    embed.setAttribute("id", id);
    embed.setAttribute("wmode", "opaque");
    embed.setAttribute("allowScriptAccess", "sameDomain");
    embed.setAttribute("class", "flex-widget");
    embed.setAttribute("pluginspage",
      "https://www.adobe.com/go/getflashplayer");

    object.appendChild(embed);

    /*
     * This is a workaround to allow Flex ExternalInterface calls to javscript
     * when the flex app is embedded in the <form> section of the document as is
     * the case when using Renderers to render the flex widget
     */

    context.includeScripts("", "window." + id + " = document.forms.mainForm."
      + id + ";");

    // Add javascript function that is called from flex to pass content back to
    // the uim target field
    context.includeScripts("",
      "function setTargetCaseAndClientList(link) {window.document.forms.mainForm."
        + curam.omega3.request.RequestUtils.escapeURL(targetID)
        + ".value=link;}");
    // function to return the concern role id returned from the person popup.
    // note that the attribute name is hard coded here
    context
      .includeScripts(
        "",
        "function getContextID(){return window.document.forms.mainForm.ACTION$dtls$concernRoleID_value.value;}");
  }

  /**
   * Builds up the string for the callFlashFunction. This function would call a
   * flash/flex function.
   * 
   * @return The string holding the callFlashFunction
   */
  public String createFlashFunction() {

    return "function callFlashFunction(concernRoleID)"
      + "{ var myFlexApp = document.getElementById('EnhancedSelectClients');myFlexApp.loadDataTree('103');}";
  }

  /**
   * Builds up the string with the onchange event.
   * 
   * @return The javascript for the onchange event
   */
  public String setEvent() {

    return "dojo.connect(document.getElementById('input#ACTION$dtls$concernRoleID_value'), 'onchange', this, 'onchange_PersonSearch');";
  }

  /**
   * Builds up the onchange_PersonSearch function which is called by the
   * onchange event.
   * 
   * @return The string holding the onchange_PersonSearch function
   */
  public String processEvent() {

    return "function onchange_PersonSearch() {"
      + "alert('personSearchChanged');}";
  }
}
