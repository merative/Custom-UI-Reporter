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
 * Copyright 2010-2011 Curam Software Ltd.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Curam
 * Software, Ltd. ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Curam Software.
 */
package citizenworkspace.renderer;

import citizenaccount.renderer.RendererUtil;
import citizenworkspace.layout.LayoutUtils;
import citizenworkspace.pageplayer.HTMLConsts;
import citizenworkspace.util.SecurityUtils;
import curam.ieg.player.PlayerUtils;
import curam.ieg.player.RichTextComponent;
import curam.util.client.BidiUtils;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.ComponentBuilderFactory;
import curam.util.client.model.Container;
import curam.util.client.model.ContainerBuilder;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

// REVIEWED Ramesh.Khairmode 29/04/2010

// REVIEW_MEDIUM, Ramesh, 29/04/2010 - unused imports
// REVIEW_HIGH, Ramesh, 29/04/2010 - action TODO

/**
 * Renderer to display the Government Services options that make up part of the
 * Triage Results screen.
 */
public class GovernmentServicesInformationRenderer extends
  AbstractViewRenderer {

  private static final String GOV_SERVICES_DIV_ID_EXPANDED =
    "gov_services_div_expanded";

  private static final String GOV_SERVICES_DIV_ID_COLLAPSED =
    "gov_services_div_collapsed";

  /**
   * The name of the properties file that stores the localised text for this
   * renderer.
   */
  private static final String propertiesFileName = "ServiceProviders";

  /**
   * {@inheritDoc}
   */
  // CHECKSTYLE_OFF: MethodLength
  @Override
  public void render(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final RendererContract contract) throws ClientException,
    DataAccessException, PlugInException {

    // process the screenings
    final Path screeningsPath =
      field.getBinding().getSourcePath().extendPath("screenings/screening[]");

    final int numScreenings = context.getDataAccessor().count(screeningsPath);

    // process the applications
    final Path intakeApplicationsPath =
      field.getBinding().getSourcePath()
        .extendPath("intake-applications/intake-application[]");

    final int numIntakeApplications =
      context.getDataAccessor().count(intakeApplicationsPath);

    if (numScreenings == 0 && numIntakeApplications == 0) {
      return;
    }

    String governmentServicesTitle =
      PlayerUtils.getProperty(propertiesFileName,
        "Government.Services.Title", context);

    String screeningsTitle =
      PlayerUtils.getProperty(propertiesFileName,
        "Government.Services.Screenings.Title", context);

    String applicationsTitle =
      PlayerUtils.getProperty(propertiesFileName,
        "Government.Services.Applications.Title", context);

    String screeningButtonAltText =
      PlayerUtils.getProperty(propertiesFileName,
        "Screening.Button.Alt.Text", context);

    String screeningButtonText =
      PlayerUtils.getProperty(propertiesFileName, "Screening.Button.Text",
        context);

    final String applicationButtonAltText =
      PlayerUtils.getProperty(propertiesFileName,
        "Application.Button.Alt.Text", context);

    String applicationButtonText =
      PlayerUtils.getProperty(propertiesFileName, "Application.Button.Text",
        context);

    final Element containerDiv =
      documentFragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    final boolean isBidi = BidiUtils.isBidi();
    if (isBidi) {
      governmentServicesTitle =
        BidiUtils.addEmbedingUCC(governmentServicesTitle);
      screeningsTitle = BidiUtils.addEmbedingUCC(screeningsTitle);
      applicationsTitle = BidiUtils.addEmbedingUCC(applicationsTitle);
      screeningButtonAltText =
        BidiUtils.addEmbedingUCC(screeningButtonAltText);
      screeningButtonText = BidiUtils.addEmbedingUCC(screeningButtonText);
      applicationButtonText = BidiUtils.addEmbedingUCC(applicationButtonText);
    }

    containerDiv.setAttribute(HTMLConsts.CLASS, "gov-services-container");
    containerDiv.setAttribute(HTMLConsts.ID, GOV_SERVICES_DIV_ID_EXPANDED);
    RendererUtil.attachWaiAriaAttr(containerDiv, "GovernmentServices");
    documentFragment.appendChild(containerDiv);

    final Element hiddenDiv =
      documentFragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    hiddenDiv.setAttribute(HTMLConsts.CLASS,
      "gov-services-container-collapsed");
    hiddenDiv.setAttribute(HTMLConsts.ID, GOV_SERVICES_DIV_ID_COLLAPSED);
    hiddenDiv.setAttribute(HTMLConsts.STYLE, "display: none;"); // hidden when
                                                                // page loads
                                                                // first
    RendererUtil.attachWaiAriaAttr(hiddenDiv, "GovernmentServices");
    documentFragment.appendChild(hiddenDiv);

    final Element hiddenTitleDiv =
      documentFragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    hiddenTitleDiv.setAttribute(HTMLConsts.CLASS, "section-title");
    RendererUtil.attachWaiAriaAttr(hiddenTitleDiv,
      "GovernmentServicesSection");
    hiddenDiv.appendChild(hiddenTitleDiv);

    final Element hiddenTextDiv =
      documentFragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    hiddenTextDiv.setAttribute(HTMLConsts.CLASS, "vertical-title");
    RendererUtil
      .attachWaiAriaAttr(hiddenTextDiv, "GovernmentServicesSection");
    hiddenDiv.appendChild(hiddenTextDiv);

    final Element hiddenSpan =
      documentFragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    hiddenSpan.setAttribute(HTMLConsts.CLASS, "vertical-text");
    hiddenSpan.setTextContent(governmentServicesTitle);
    RendererUtil.attachWaiAriaAttr(hiddenSpan, "GovernmentServicesSection");
    hiddenTextDiv.appendChild(hiddenSpan);

    final Element hiddenImgEle =
      documentFragment.getOwnerDocument().createElement(HTMLConsts.IMG_TAG);
    hiddenImgEle.setAttribute(HTMLConsts.SRC_TAG,
      "../Images/gov_service_arrow_mini.png");
    hiddenImgEle.setAttribute(HTMLConsts.EVENT_ONCLICK,
      "recommendations.restoreGovServices('" + GOV_SERVICES_DIV_ID_EXPANDED
        + "', '" + GoogleMapsViewServicesByTypeRenderer.CURAM_MAP_DIV_ID
        + "');");
    hiddenImgEle.setAttribute(HTMLConsts.CLASS, "slider-image-collapsed");
    hiddenImgEle.setAttribute(HTMLConsts.TITLE, PlayerUtils.getProperty(
      propertiesFileName, "Expand.Government.Services.Tooltip", context));
    hiddenImgEle.setAttribute(HTMLConsts.ALT_TAG, PlayerUtils.getProperty(
      propertiesFileName, "Expand.Government.Services.Tooltip", context));
    hiddenTitleDiv.appendChild(hiddenImgEle);

    final Element titleDiv =
      documentFragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    titleDiv.setAttribute(HTMLConsts.CLASS, "section-title");
    RendererUtil.attachWaiAriaAttr(titleDiv, "SectionTitle");
    containerDiv.appendChild(titleDiv);

    // Slider image
    final Element imgEle =
      documentFragment.getOwnerDocument().createElement(HTMLConsts.IMG_TAG);
    imgEle
      .setAttribute(HTMLConsts.SRC_TAG, "../Images/gov_service_arrow.png");
    imgEle.setAttribute(HTMLConsts.EVENT_ONCLICK, "recommendations.mapOnly('"
      + GOV_SERVICES_DIV_ID_EXPANDED + "', '"
      + GoogleMapsViewServicesByTypeRenderer.CURAM_MAP_DIV_ID + "');");
    imgEle.setAttribute(HTMLConsts.CLASS, "slider-image-expanded");
    imgEle.setAttribute(HTMLConsts.TITLE, PlayerUtils.getProperty(
      propertiesFileName, "Collapse.Government.Services.Tooltip", context));
    imgEle.setAttribute(HTMLConsts.ALT_TAG, PlayerUtils.getProperty(
      propertiesFileName, "Collapse.Government.Services.Tooltip", context));
    titleDiv.appendChild(imgEle);

    final Element titlePara =
      documentFragment.getOwnerDocument().createElement(HTMLConsts.PARAGRAPH);
    titlePara.setAttribute(HTMLConsts.CLASS, "government-services-title");
    titlePara.setTextContent(governmentServicesTitle);
    RendererUtil.attachWaiAriaAttr(titlePara, "ServicesTitle");
    titleDiv.appendChild(titlePara);

    final Element contentDiv =
      documentFragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    contentDiv.setAttribute(HTMLConsts.CLASS, "gov-options-container");
    contentDiv.setAttribute(HTMLConsts.ID, "gov-options-container");
    RendererUtil.attachWaiAriaAttr(contentDiv, "Services");
    containerDiv.appendChild(contentDiv);

    // Screenings title
    final Element screeningsTitleDiv =
      documentFragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    RendererUtil.attachWaiAriaAttr(screeningsTitleDiv, "Services");
    contentDiv.appendChild(screeningsTitleDiv);

    final Element screeningsTitlePara =
      documentFragment.getOwnerDocument().createElement(HTMLConsts.PARAGRAPH);
    screeningsTitlePara.setAttribute(HTMLConsts.CLASS, "gov-section-title");
    screeningsTitlePara.setTextContent(screeningsTitle);
    RendererUtil.attachWaiAriaAttr(screeningsTitlePara, "SectionTitle");
    screeningsTitleDiv.appendChild(screeningsTitlePara);

    // Element fieldSet = documentFragment.getOwnerDocument().createElement(
    // HTMLConsts.FIELDSET);
    // contentDiv.appendChild(fieldSet);

    // LayoutUtils.createFieldSet("a", fragment.getOwnerDocument(), title, null,
    final Element fieldSet = LayoutUtils.createEmptyFieldset(contentDiv);
    contentDiv.appendChild(fieldSet);

    for (int i = 1; i <= numScreenings; i++) {

      final Element screeningDiv =
        documentFragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      RendererUtil.attachWaiAriaAttr(screeningDiv, "Screening");
      fieldSet.appendChild(screeningDiv);

      final Path screeningPath = screeningsPath.applyIndex(0, i, true);

      final String name =
        context.getDataAccessor().get(screeningPath.extendPath("name"));
      final String description =
        context.getDataAccessor()
          .get(screeningPath.extendPath("description"));
      final String id =
        context.getDataAccessor().get(screeningPath.extendPath("id"));

      final Element inputDiv =
        documentFragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      RendererUtil.attachWaiAriaAttr(inputDiv, "Screening");
      final Element inputEle =
        documentFragment.getOwnerDocument().createElement(
          HTMLConsts.INPUT_TAG);
      inputEle.setAttribute("type", "radio");
      inputEle.setAttribute("class", "optionSelect");
      inputEle.setAttribute("value", id);
      inputEle.setAttribute("id", "screening-" + i);
      inputEle.setAttribute("name", "screeningID");

      inputDiv.appendChild(inputEle);

      screeningDiv.appendChild(inputDiv);

      final Element screenNameDiv =
        documentFragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      screenNameDiv.setAttribute("class", "service-title-div");
      screenNameDiv.setAttribute(HTMLConsts.FOR_ATTR, "screening-" + i);
      RendererUtil.attachWaiAriaAttr(screenNameDiv, "ScreeningName");

      final Element screeningNameSpan =
        documentFragment.getOwnerDocument()
          .createElement(HTMLConsts.SPAN_TAG);
      screeningNameSpan.setAttribute("class", "optionName");
      RendererUtil.attachWaiAriaAttr(screeningNameSpan, "ScreeningOption");
      if (!isBidi) {
        screeningNameSpan.setTextContent(name);
      } else {
        screeningNameSpan.setTextContent(BidiUtils.addEmbedingUCC(name));
      }

      screenNameDiv.appendChild(screeningNameSpan);

      screeningDiv.appendChild(screenNameDiv);

      final Element screeningDescDiv =
        documentFragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      screeningDescDiv.setAttribute("class", "screeningDesc");
      RendererUtil
        .attachWaiAriaAttr(screeningDescDiv, "ScreeningDescription");
      screeningDescDiv.appendChild(createRichTextDescription(
        documentFragment, context, contract, description));
      screeningDiv.appendChild(screeningDescDiv);
    }

    // ******************************
    // SCREENING BUTTON
    // ******************************
    final Element screeningButtonContainer =
      documentFragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    screeningButtonContainer.setAttribute(HTMLConsts.CLASS, "go-button");
    RendererUtil.attachWaiAriaAttr(screeningButtonContainer,
      "ScreeningButton");
    contentDiv.appendChild(screeningButtonContainer);

    final Element screeningButtonEle =
      documentFragment.getOwnerDocument().createElement(HTMLConsts.A_TAG);
    screeningButtonEle.setAttribute("onclick", "startScreening();");
    screeningButtonEle.setAttribute(HTMLConsts.CLASS,
      "bhv-nav-button buttonLink needsScript-hide-inline");
    screeningButtonEle.setAttribute("id", "screenButton");
    screeningButtonEle.setAttribute(HTMLConsts.ALT_TAG,
      screeningButtonAltText);

    screeningButtonEle.setAttribute(HTMLConsts.HREF_TAG, "#");
    screeningButtonEle.setAttribute(HTMLConsts.TITLE, PlayerUtils
      .getProperty(propertiesFileName, "Start.Screening.Tooltip", context));

    screeningButtonEle.appendChild(RendererUtils.generateButton(
      documentFragment, screeningButtonText));

    screeningButtonContainer.appendChild(screeningButtonEle);

    // This is the divider.
    final Element brEle =
      documentFragment.getOwnerDocument().createElement("BR");
    brEle.setAttribute(HTMLConsts.CLASS, "clear-both");
    contentDiv.appendChild(brEle);

    final Element hrEle =
      documentFragment.getOwnerDocument().createElement("HR");
    hrEle.setAttribute(HTMLConsts.CLASS, "gov-services-hr");
    contentDiv.appendChild(hrEle);

    // Applications title
    final Element applicationsTitleDiv =
      documentFragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    RendererUtil.attachWaiAriaAttr(applicationsTitleDiv, "ApplicationTitle");
    contentDiv.appendChild(applicationsTitleDiv);
    applicationsTitleDiv.setAttribute(HTMLConsts.STYLE, "display: inline;");

    final Element applicationsTitlePara =
      documentFragment.getOwnerDocument().createElement(HTMLConsts.PARAGRAPH);
    applicationsTitlePara.setAttribute(HTMLConsts.CLASS, "gov-section-title");
    applicationsTitlePara.setTextContent(applicationsTitle);
    RendererUtil.attachWaiAriaAttr(applicationsTitlePara, "ApplicationTitle");
    applicationsTitleDiv.appendChild(applicationsTitlePara);

    for (int i = 1; i <= numIntakeApplications; i++) {

      final Element applicationDiv =
        documentFragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      RendererUtil.attachWaiAriaAttr(applicationDiv, "ApplicationContent");
      contentDiv.appendChild(applicationDiv);

      final Path intakeApplicationPath =
        intakeApplicationsPath.applyIndex(0, i, true);

      final String name =
        context.getDataAccessor().get(
          intakeApplicationPath.extendPath("name"));
      final String description =
        context.getDataAccessor().get(
          intakeApplicationPath.extendPath("description"));
      final String id =
        context.getDataAccessor().get(intakeApplicationPath.extendPath("id"));

      final Element inputDiv =
        documentFragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      RendererUtil.attachWaiAriaAttr(inputDiv, "ApplicationOption");
      final Element inputEle =
        documentFragment.getOwnerDocument().createElement(
          HTMLConsts.INPUT_TAG);
      inputEle.setAttribute("type", "radio");
      inputEle.setAttribute("class", "optionSelect");
      inputEle.setAttribute("value", id);
      inputEle.setAttribute("id", "application-" + i);
      inputEle.setAttribute("name", "applicationID");

      inputDiv.appendChild(inputEle);

      applicationDiv.appendChild(inputDiv);

      final Element appNameDiv =
        documentFragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      appNameDiv.setAttribute("class", "service-title-div");
      appNameDiv.setAttribute(HTMLConsts.FOR_ATTR, "application-" + i);
      RendererUtil.attachWaiAriaAttr(appNameDiv, "ApplicationName");

      final Element applicationNameSpan =
        documentFragment.getOwnerDocument()
          .createElement(HTMLConsts.SPAN_TAG);
      applicationNameSpan.setAttribute("class", "optionName");
      if (!isBidi) {
        applicationNameSpan.setTextContent(name);
      } else {
        applicationNameSpan.setTextContent(BidiUtils.addEmbedingUCC(name));
      }
      RendererUtil.attachWaiAriaAttr(applicationNameSpan, "ApplicationName");
      appNameDiv.appendChild(applicationNameSpan);

      applicationDiv.appendChild(appNameDiv);

      final Element applicationDescDiv =
        documentFragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      applicationDescDiv.setAttribute("class", "screeningDesc");
      RendererUtil.attachWaiAriaAttr(applicationDescDiv,
        "ScreeningDescription");
      applicationDescDiv.appendChild(createRichTextDescription(
        documentFragment, context, contract, description));
      applicationDiv.appendChild(applicationDescDiv);

    }

    // ******************************
    // APPLICATION BUTTON
    // ******************************
    final Element applicationButtonContainer =
      documentFragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    applicationButtonContainer.setAttribute(HTMLConsts.CLASS, "go-button");
    RendererUtil.attachWaiAriaAttr(applicationButtonContainer, "GoButton");
    contentDiv.appendChild(applicationButtonContainer);

    final Element applicationButtonEle =
      documentFragment.getOwnerDocument().createElement(HTMLConsts.A_TAG);
    applicationButtonEle.setAttribute("onclick", "startApplication();");
    applicationButtonEle.setAttribute("id", "applyButton");
    applicationButtonEle.setAttribute(HTMLConsts.TITLE, PlayerUtils
      .getProperty(propertiesFileName, "Start.Application.Tooltip", context));
    applicationButtonEle.setAttribute(HTMLConsts.ALT_TAG, PlayerUtils
      .getProperty(propertiesFileName, "Start.Application.Tooltip", context));
    applicationButtonEle.setAttribute(HTMLConsts.CLASS,
      "bhv-nav-button buttonLink needsScript-hide-inline");
    applicationButtonEle.setAttribute(HTMLConsts.HREF_TAG, "#");

    applicationButtonEle.appendChild(RendererUtils.generateButton(
      documentFragment, applicationButtonText));

    applicationButtonContainer.appendChild(applicationButtonEle);

    // ******************************

    // used to determine which button was clicked
    final Element hiddenField =
      documentFragment.getOwnerDocument().createElement(HTMLConsts.INPUT_TAG);
    hiddenField.setAttribute("name", "screeningSelected");
    hiddenField.setAttribute("id", "screeningSelected");
    hiddenField.setAttribute("value", "true");
    hiddenField.setAttribute("type", "hidden");
    documentFragment.appendChild(hiddenField);

  }

  // CHECKSTYLE_ON: MethodLength

  /**
   * Creates a document fragment suitable for displaying rich text. This
   * consists of a DocumentFragment whose style is set to "rich-text" so that it
   * will be correctly rendered.
   * 
   * @param documentFragment
   * @param context
   * @param contract
   * @param description
   * The rich text
   * @return a DocumentFragment containing the rich text.
   * @throws PlugInException
   * @throws ClientException
   * @throws DataAccessException
   */
  private Node createRichTextDescription(
    final DocumentFragment documentFragment, final RendererContext context,
    final RendererContract contract, final String description)
    throws PlugInException, ClientException, DataAccessException {

    final ContainerBuilder titleBuilder =
      ComponentBuilderFactory.createClusterBuilder();
    titleBuilder.setStyle(context.getStyle("rich-text"));
    // Filter content to prevent XSS attacks
    final String filteredContent = SecurityUtils.filterACF(description);
    if (!BidiUtils.isBidi()) {
      titleBuilder.setDescription(filteredContent);
    } else {
      titleBuilder.setDescription(BidiUtils.addEmbedingUCC(filteredContent));
    }
    titleBuilder.setParameter(RichTextComponent.kWrapContentParam, "true");
    final Container titleRichText = titleBuilder.getComponent();
    final DocumentFragment titleFragment =
      documentFragment.getOwnerDocument().createDocumentFragment();
    context.render(titleRichText, titleFragment, contract);
    return titleFragment;
  }

}
