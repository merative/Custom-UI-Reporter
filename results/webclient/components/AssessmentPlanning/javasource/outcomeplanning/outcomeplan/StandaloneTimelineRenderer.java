/*
 * Licensed Materials - Property of IBM
 * 
 * PID 5725-H26
 * 
 * Copyright IBM Corporation 2012, 2020. All rights reserved.
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
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import curam.util.type.Blob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import outcomeplanning.internal.DateFormatStrings;

/**
 * View renderer for a standalone time line view.
 * 
 * @since 6.0
 * @deprecated Since Curam 7.0.10.0. Flex_OutcomePlan_Deprecation. Replaced
 * with
 * "OutcomePlan_homeNew.uim". This Flex implementation of the Outcome Plan
 * Workspace
 * has been replaced with a JavaScript implementation.
 */
@Deprecated
public class StandaloneTimelineRenderer extends AbstractViewRenderer {

  /**
   * The path to the time line SWF.
   */
  private static final String kFLEX_OUTCOME_PLAN_WORKSPACE_SWF =
    "../flex/OutcomePlanWorkspace.swf";

  /**
   * The width to be used for the time line.
   */
  private static final String kTIMELINE_WIDTH = "100%";

  /**
   * The height to be used for the time line.
   */
  private static final String kTIMELINE_HEIGHT = "200";

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
    id = "TimelineWorkspace";

    // get the xml
    final Path sourcePath = field.getBinding().getSourcePath();
    final Blob value = (Blob) context.getDataAccessor().getRaw(sourcePath);

    Document myDoc = null;

    try {
      final ByteArrayInputStream bais =
        new ByteArrayInputStream(value.copyBytes());
      final ObjectInputStream ois = new ObjectInputStream(bais);
      myDoc = (Document) ois.readObject();
      ois.close();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    } catch (final ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    final NodeList xmlList = myDoc.getChildNodes();

    final Node rootNode = xmlList.item(0);

    final String relatedID =
      rootNode.getAttributes().getNamedItem("relatedID").getNodeValue();
    final String relatedType =
      rootNode.getAttributes().getNamedItem("relatedType").getNodeValue();
    final String outcomePlanID =
      rootNode.getAttributes().getNamedItem("outcomePlanID").getNodeValue();
    context.includeScriptURIs(null,
      "../CDEJ/jscript/OutcomePlanActivityWorkspace.js");

    // Create the "object" element for the Flash Player plug-in.
    object = documentFragment.getOwnerDocument().createElement("object");
    object.setAttribute("id", id);
    object.setAttribute("name", id);
    object.setAttribute("WIDTH", kTIMELINE_WIDTH);
    object.setAttribute("HEIGHT", kTIMELINE_HEIGHT);

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
    param.setAttribute("value", kFLEX_OUTCOME_PLAN_WORKSPACE_SWF);
    object.appendChild(param);

    param = documentFragment.getOwnerDocument().createElement("param");
    param.setAttribute("name", "wmode");
    param.setAttribute("value", "opaque");
    object.appendChild(param);

    final StringBuffer flashVars =
      constructFlashVars(field, context, relatedID, relatedType,
        outcomePlanID);
    final Element flashVarsParam =
      documentFragment.getOwnerDocument().createElement("param");
    flashVarsParam.setAttribute("name", "flashVars");
    flashVarsParam.setAttribute("value", flashVars.toString());
    object.appendChild(flashVarsParam);

    // Add the "embed" element for Netscape and IE/Mac support.
    embed = documentFragment.getOwnerDocument().createElement("embed");
    embed.setAttribute("height", kTIMELINE_HEIGHT);
    embed.setAttribute("width", kTIMELINE_WIDTH);
    embed.setAttribute("src", kFLEX_OUTCOME_PLAN_WORKSPACE_SWF);
    embed.setAttribute("name", id);
    embed.setAttribute("id", id);
    embed.setAttribute("allowScriptAccess", "sameDomain");
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
   * @return A string buffer containing the flash vars.
   * @throws DataAccessException
   * Generic Exception Signature
   */
  private StringBuffer constructFlashVars(final Field field,
    final RendererContext context, final String relatedID,
    final String relatedType, final String outcomePlanID)
    throws DataAccessException {

    final StringBuffer flashVars = new StringBuffer();
    flashVars.append("outcomePlanID=").append(outcomePlanID);
    flashVars.append("&standaloneInd=true");
    flashVars.append("&relatedID=").append(relatedID);
    flashVars.append("&relatedType=").append(relatedType);
    flashVars.append("&dateFormat=").append(DateFormatStrings.getFormat());

    return flashVars;
  }
}
