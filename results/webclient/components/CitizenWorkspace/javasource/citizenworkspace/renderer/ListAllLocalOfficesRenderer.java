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

import citizenaccount.renderer.RendererUtil;
import citizenworkspace.pageplayer.HTMLConsts;
import citizenworkspace.util.XmlTools;
import curam.ieg.player.PlayerUtils;
import curam.util.client.BidiUtils;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.path.util.ClientPaths;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import curam.util.dom.html2.HTMLUtils;
import curam.util.exception.AppRuntimeException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Renderer class for rendering the list of all local offices.
 */
public class ListAllLocalOfficesRenderer extends AbstractViewRenderer {

  @Override
  public void render(final Field field, final DocumentFragment fragment,
    final RendererContext context, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    final Path listOfficesPropertiesPath =
      ClientPaths.GENERAL_RESOURCES_PATH
        .extendPath("curam.util.properties.page." + context.getPageID());

    final String closeLabel =
      getProperty(context, listOfficesPropertiesPath,
        "ActionControl.Label.Close");
    final String closeAltText =
      getProperty(context, listOfficesPropertiesPath,
        "ActionControl.Label.Close.Alt.Text");

    final Document rendererFieldDocument =
      XmlTools.getRendererFieldDocument(field, context);

    final String xPathExpression = "/list-root/local-office";

    NodeList list;
    try {
      list = XmlTools.getXMLNodeList(xPathExpression, rendererFieldDocument);
    } catch (final XPathExpressionException e) {
      throw new AppRuntimeException(e);
    }

    final Element containerDiv =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

    fragment.appendChild(containerDiv);

    final Element listEle = fragment.getOwnerDocument().createElement("ul");

    containerDiv.appendChild(listEle);

    HTMLUtils.appendComment(listEle, "list container");

    for (int i = 0; i < list.getLength(); i++) {
      final Node childNode = list.item(i);
      final Element listEntry =
        fragment.getOwnerDocument().createElement("li");
      final String address = childNode.getTextContent();
      RendererUtil.richTextRenderer(address, listEntry, context, contract);
      listEle.appendChild(listEntry);
    }

    final Element buttonWrapper =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

    final String htmlDirection =
      PlayerUtils.getHtmlPresentationDirection(getLocale());
    final boolean isRTL = "rtl".equals(htmlDirection);

    buttonWrapper.setAttribute("style", "float: "
      + (isRTL ? "left" : "right") + "; padding-bottom: 20px;");

    containerDiv.appendChild(buttonWrapper);

    final Element dismissButton =
      fragment.getOwnerDocument().createElement("dijit.form.Button");

    dismissButton.setAttribute("dojoType", "dijit.form.Button");
    dismissButton.setAttribute("label", closeLabel);
    dismissButton.setAttribute(HTMLConsts.TITLE, closeAltText);
    dismissButton.setAttribute("onClick",
      "window.parent.cw.form.dismiss(this)");
    if (BidiUtils.isBidi()) {
      BidiUtils.setTextDirForElement(dismissButton);
    }

    buttonWrapper.appendChild(dismissButton);
  }

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
