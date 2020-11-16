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
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * Renders the "Apply for Benefits" page.
 */
public class ApplyForBenefitsRenderer extends AbstractViewRenderer {

  private static final String listOfficePageID =
    "CitizenWorkspace_listLocalOffices";

  @Override
  public void render(final Field field, final DocumentFragment doc,
    final RendererContext context, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    final Path listOfficesPropertiesPath =
      ClientPaths.GENERAL_RESOURCES_PATH
        .extendPath("curam.util.properties.page." + listOfficePageID);

    final String localOfficesPageTitle =
      getProperty(context, listOfficesPropertiesPath, "PageTitle.StaticText1");

    final Path propertiesPath =
      ClientPaths.GENERAL_RESOURCES_PATH
        .extendPath("curam.util.properties.page." + context.getPageID());

    final String applyTitle =
      getProperty(context, propertiesPath, "Apply.Title");
    final String applyDesc =
      getProperty(context, propertiesPath, "Apply.Description");
    final String buttonText =
      getProperty(context, propertiesPath, "Button.Text");
    final String buttonAltText =
      getProperty(context, propertiesPath, "Button.Alt.Text");
    final String formsTitle =
      getProperty(context, propertiesPath, "Forms.Title");
    final String formsDesc =
      getProperty(context, propertiesPath, "Forms.Description");

    final String htmlDirection =
      PlayerUtils.getHtmlPresentationDirection(getLocale());
    final boolean isRTL = "rtl".equals(htmlDirection);

    final Element contentDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    contentDiv.setAttribute(HTMLConsts.CLASS,
      "lifeeventoneui apply-for-benefits-content");
    contentDiv.setAttribute(HTMLConsts.STYLE, "width:99%;margin:"
      + (isRTL ? "0px 0px 0px 25px;" : "0px 25px 0px 0px;"));
    doc.appendChild(contentDiv);

    final Element titleAndDescDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    titleAndDescDiv.setAttribute(HTMLConsts.CLASS,
      "apply-for-benefits-titleanddesc");
    titleAndDescDiv.setAttribute(HTMLConsts.STYLE, "width:60%;float:"
      + (isRTL ? "right" : "left") + ";margin:0px 0px 0px 0px;");
    contentDiv.appendChild(titleAndDescDiv);

    final Element titleInfoDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    titleInfoDiv.setAttribute(HTMLConsts.CLASS,
      "le-catalogue-type-div-apply-online");
    titleInfoDiv.setAttribute(HTMLConsts.ID, "le-apply-online-title");
    titleAndDescDiv.appendChild(titleInfoDiv);
    RendererUtil.richTextRenderer(field, doc, context, contract,
      titleInfoDiv, propertiesPath.extendPath("Apply.Title"));

    final Element descDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    descDiv.setAttribute(HTMLConsts.CLASS, "name-desc-div-apply-online");
    descDiv.setAttribute(HTMLConsts.ID, "name-desc-div-apply-online");
    descDiv
      .setAttribute(
        HTMLConsts.STYLE,
        (isRTL ? "float:right;" : "float:left;")
          + " margin:20px 0px 0px 0px; font-family: Arial; font-size: 18px; color: #222222;");
    titleAndDescDiv.appendChild(descDiv);
    RendererUtil.richTextRenderer(field, doc, context, contract, descDiv,
      propertiesPath.extendPath("Apply.Description"));

    final Element buttonDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

    final Element linkElement =
      doc.getOwnerDocument().createElement("dijit.form.Button");
    linkElement.setAttribute("onClick", "window.location.href='"
      + "../cw/ResolvePage.do?page=SetupIntake" + "'");
    linkElement.setAttribute("dojoType", "dijit.form.Button");
    linkElement.setAttribute("class", "idxSpecialButton");
    linkElement.setTextContent(buttonText);
    linkElement.setAttribute("title", buttonAltText);
    if (BidiUtils.isBidi()) {
      BidiUtils.setTextDirForElement(linkElement);
    }

    buttonDiv.appendChild(linkElement);

    // TODO add the button at some point
    contentDiv.appendChild(buttonDiv);

    // add the PDF forms title and desc
    final Element pdfTitleAndDescDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    pdfTitleAndDescDiv
      .setAttribute(HTMLConsts.CLASS, "name-desc-div-pdfinfo");
    pdfTitleAndDescDiv.setAttribute(HTMLConsts.STYLE, "width:60%;"
      + (isRTL ? "float:right;" : "float:left;")
      + " margin:20px 0px 0px 0px;");
    contentDiv.appendChild(pdfTitleAndDescDiv);

    final Element pdfTitleInfoDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    pdfTitleInfoDiv.setAttribute(HTMLConsts.CLASS,
      "le-catalogue-type-div-pdf-titleinfo");
    pdfTitleInfoDiv.setAttribute(HTMLConsts.ID,
      "le-catalogue-type-div-pdf-titleinfo");
    pdfTitleAndDescDiv.appendChild(pdfTitleInfoDiv);
    RendererUtil.richTextRenderer(field, doc, context, contract,
      pdfTitleInfoDiv, propertiesPath.extendPath("Forms.Title"));

    final Element pdfDescDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    pdfDescDiv
      .setAttribute(HTMLConsts.CLASS, "name-desc-div-pdf-description");
    pdfDescDiv.setAttribute(HTMLConsts.ID, "name-desc-div-pdf-description");
    pdfDescDiv
      .setAttribute(
        HTMLConsts.STYLE,
        (isRTL ? "float:right;" : "float:left;")
          + " margin:20px 0px 0px 0px; font-family: Arial; font-size: 18px; color: #222222; text-decoration: underline; cursor: pointer;");
    pdfTitleAndDescDiv.appendChild(pdfDescDiv);

    RendererUtil.richTextRenderer(field, doc, context, contract, pdfDescDiv,
      propertiesPath.extendPath("Forms.Description"));

    // pdfDescDiv.setAttribute("onclick",
    // "window.parent.curam.util.UimDialog.open('CitizenWorkspace_localOfficeSearchPage.do')");
    pdfDescDiv.setAttribute("onclick",
      "window.parent.cw.popup.showNew({href: '" + listOfficePageID
        + "Page.do', " + "title: '" + localOfficesPageTitle + "'});");

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
