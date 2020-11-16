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
package citizenworkspace.renderer.onlineappeal;

import citizenaccount.renderer.RendererUtil;
import citizenworkspace.pageplayer.HTMLConsts;
import citizenworkspace.util.XmlTools;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.path.util.ClientPaths;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * Renders the "Launch An Online Appeal" page, CitizenWorkspace_appeals.
 * 
 * @since 6.0.5.5
 */
public class LaunchOnlineAppealRenderer extends AbstractViewRenderer {

  @Override
  public void render(final Field field, final DocumentFragment doc,
    final RendererContext context, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    // Look up page properties
    final Path propertiesPath =
      ClientPaths.GENERAL_RESOURCES_PATH
        .extendPath("curam.util.properties.page." + context.getPageID());

    // Create wrapper DIV
    final Element contentDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    contentDiv.setAttribute(HTMLConsts.CLASS, "launch-online-appeal-content");
    doc.appendChild(contentDiv);

    // Title and description, launch button will be places withing wrapper DIV
    final Element requestAppealtitleAndDescDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    requestAppealtitleAndDescDiv.setAttribute(HTMLConsts.CLASS,
      "launch-online-appeal-titledesc");
    contentDiv.appendChild(requestAppealtitleAndDescDiv);

    final Element requestAppealTitleDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    requestAppealTitleDiv.setAttribute(HTMLConsts.CLASS,
      "launch-online-appeal-titledesc-title");
    requestAppealtitleAndDescDiv.appendChild(requestAppealTitleDiv);
    RendererUtil.richTextRenderer(field, doc, context, contract,
      requestAppealTitleDiv,
      propertiesPath.extendPath("Request.Appeal.Title"));

    final Element requestAppealDescDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    requestAppealDescDiv.setAttribute(HTMLConsts.CLASS,
      "launch-online-appeal-titledesc-desc");
    requestAppealtitleAndDescDiv.appendChild(requestAppealDescDiv);
    RendererUtil.richTextRenderer(field, doc, context, contract,
      requestAppealDescDiv,
      propertiesPath.extendPath("Request.Appeal.Description"));

    // Launch button displayed conditionally
    final Document rendererFieldDocument =
      XmlTools.getRendererFieldDocument(field, context);
    final String canAppealStr =
      rendererFieldDocument.getDocumentElement().getAttribute("canAppeal");
    final Boolean buttonDisabled = !Boolean.parseBoolean(canAppealStr);

    if (!buttonDisabled) {

      final String buttonText =
        getProperty(context, propertiesPath, "Button.Text");
      final String buttonAltText =
        getProperty(context, propertiesPath, "Button.Alt.Text");

      final Element linkElement =
        doc.getOwnerDocument().createElement("button");
      linkElement.setAttribute("onClick", "window.location.href='"
        + "../cw/ResolvePage.do?page=SetupAppeal" + "'");
      linkElement.setAttribute("dojoType", "dijit.form.Button");
      linkElement.setAttribute("class", "idxSpecialButton");
      linkElement.setTextContent(buttonText);
      linkElement.setAttribute("title", buttonAltText);
      contentDiv.appendChild(linkElement);
    }

    // create "Your Rights" DIV
    final Element rightsTitleAndDescDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    rightsTitleAndDescDiv.setAttribute(HTMLConsts.CLASS,
      "launch-online-appeal-rights-titledesc");
    contentDiv.appendChild(rightsTitleAndDescDiv);

    final Element rightsTitleDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    rightsTitleDiv.setAttribute(HTMLConsts.CLASS,
      "launch-online-appeal-rights-titledesc-title");
    rightsTitleAndDescDiv.appendChild(rightsTitleDiv);
    RendererUtil.richTextRenderer(field, doc, context, contract,
      rightsTitleDiv, propertiesPath.extendPath("RightsOfAppeal.Title"));

    final Element rightsDescDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    rightsDescDiv.setAttribute(HTMLConsts.CLASS,
      "launch-online-appeal-rights-titledesc-desc");
    rightsTitleAndDescDiv.appendChild(rightsDescDiv);
    RendererUtil.richTextRenderer(field, doc, context, contract,
      rightsDescDiv, propertiesPath.extendPath("RightsOfAppeal.Description"));

  }

  /**
   * Helper method for property access.
   * 
   * @param context RendererContext instance
   * @param pagePropertiesPath Path to page properties
   * @param propertyName Property name
   * @return Property value, or an empty string when no such property is found
   */
  private String getProperty(final RendererContext context,
    final Path pagePropertiesPath, final String propertyName) {

    try {
      return context.getDataAccessor().get(
        pagePropertiesPath.extendPath(propertyName));
    } catch (final DataAccessException ex) {
      return new String();
    }
  }

}
