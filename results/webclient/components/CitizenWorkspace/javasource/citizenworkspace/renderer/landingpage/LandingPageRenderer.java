/*
 * Licensed Materials - Property of IBM
 *
 * PID 5725-H26
 *
 * Copyright IBM Corporation 2017. All rights reserved.
 *
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 */
package citizenworkspace.renderer.landingpage;

import citizenaccount.renderer.RendererUtil;
import citizenworkspace.pageplayer.HTMLConsts;
import citizenworkspace.renderer.RendererUtils;
import citizenworkspace.util.SecurityUtils;
import citizenworkspace.util.XmlTools;
import curam.ieg.player.PlayerUtils;
import curam.ieg.player.RichTextComponent;
import curam.omega3.util.CDEJResources;
import curam.util.client.BidiUtils;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.jsp.JspUtil;
import curam.util.client.model.ComponentBuilderFactory;
import curam.util.client.model.Container;
import curam.util.client.model.ContainerBuilder;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.plugin.PlugInException;
import curam.util.dom.html2.HTMLUtils;
import curam.util.exception.AppRuntimeException;
import javax.xml.transform.Result;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Renderer class for rendering the landing page.
 */
public class LandingPageRenderer extends AbstractViewRenderer {

  private static final String propertiesFileName = "LandingPage";

  private static final int TRIAGE_BUTTON = 1;

  private static final int SCREENING_BUTTON = 2;

  @Override
  public void render(final Field field, final DocumentFragment fragment,
    final RendererContext context, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    boolean renderLoginPanel = false;
    boolean triageEnabled = false;
    boolean hcEnabled = false;
    boolean screeningEnabled = true;
    boolean intakeEnabled = true;

    final Document rendererFieldDocument =
      XmlTools.getRendererFieldDocument(field, context);

    final String loggedInUserXPathExpression = "/landing-page/logged-in-user";
    final String displayTriageXPathExpression =
      "/landing-page/display-triage";
    final String displayContrastSelectorXPathExpression =
      "/landing-page/display-high-contrast";
    final String displayScreeningXPathExpression =
      "/landing-page/display-screening";
    final String displayIntakeXPathExpression =
      "/landing-page/display-intake";

    try {
      final boolean externalLogonEnabled =
        Boolean
          .valueOf(CDEJResources
            .getApplicationDataProperty("curam.citizenworkspace.use.external.login.page"));
      final NodeList loggedInUserlist =
        XmlTools.getXMLNodeList(loggedInUserXPathExpression,
          rendererFieldDocument);
      final boolean loggedInUser =
        new Boolean(loggedInUserlist.item(0).getTextContent()).booleanValue();

      renderLoginPanel = !loggedInUser && !externalLogonEnabled;

      final NodeList displayTriagelist =
        XmlTools.getXMLNodeList(displayTriageXPathExpression,
          rendererFieldDocument);

      triageEnabled =
        new Boolean(displayTriagelist.item(0).getTextContent())
        .booleanValue();

      final NodeList displayHighContrastlist =
        XmlTools.getXMLNodeList(displayContrastSelectorXPathExpression,
          rendererFieldDocument);

      hcEnabled =
        new Boolean(displayHighContrastlist.item(0).getTextContent())
          .booleanValue();

      final NodeList displayScreeninglist =
        XmlTools.getXMLNodeList(displayScreeningXPathExpression,
          rendererFieldDocument);

      screeningEnabled =
        new Boolean(displayScreeninglist.item(0).getTextContent())
          .booleanValue();

      final NodeList displayIntakelist =
        XmlTools.getXMLNodeList(displayIntakeXPathExpression,
          rendererFieldDocument);

      intakeEnabled =
        new Boolean(displayIntakelist.item(0).getTextContent())
        .booleanValue();

    } catch (final XPathExpressionException e) {
      throw new AppRuntimeException(e);
    }

    final Element landingPageContainer =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

    landingPageContainer.setAttribute("id", "lp-container");
    landingPageContainer.setAttribute("class", "lp-container");
    RendererUtil.attachWaiAriaAttr(landingPageContainer, "Main Container");

    fragment.appendChild(landingPageContainer);

    final Element landingPageBannerWrapper =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

    RendererUtil
      .attachWaiAriaAttr(landingPageBannerWrapper, "Banner Wrapper");

    landingPageContainer.appendChild(landingPageBannerWrapper);

    final Element landingPageBanner =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

    landingPageBanner.setAttribute("id", "lp-banner");
    landingPageBanner.setAttribute("class",
      "lp-banner lp-welcome-banner-background");
    RendererUtil.attachWaiAriaAttr(landingPageBanner, "Banner");

    landingPageBannerWrapper.appendChild(landingPageBanner);

    renderBannerText(field, landingPageBanner, context, contract, hcEnabled);

    final Element actionsContainer =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

    actionsContainer.setAttribute("id", "lp-actions-container");
    actionsContainer.setAttribute("class", "lp-actions-container");
    RendererUtil.attachWaiAriaAttr(actionsContainer, "Action Container");

    landingPageContainer.appendChild(actionsContainer);

    // login container
    final Element loginContainer =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    loginContainer.setAttribute("id", "login-container");
    RendererUtil.attachWaiAriaAttr(loginContainer, "Login Container");

    // renderer the session expired session message
    final DocumentFragment expiredMessageSessionFragment =
      landingPageBanner.getOwnerDocument().createDocumentFragment();

    renderExpiredMessageSession(expiredMessageSessionFragment, context,
      contract);

    actionsContainer.appendChild(loginContainer);

    // this is the login panel
    final Element loginPanel =
      fragment.getOwnerDocument().createElement("div");

    HTMLUtils.appendComment(loginPanel, "comment");

    RendererUtil.attachWaiAriaAttr(loginPanel, "Login Panel");

    if (renderLoginPanel) {

      loginPanel.setAttribute("dojoType", "cwtk.widget.FragmentPane");

      loginPanel.setAttribute("uim", "CitizenWorkspace_loginFragment");

      loginPanel.setAttribute("id", "loginPane");
    }

    // action links
    final Element actionLinksContainer =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

    actionLinksContainer.setAttribute("id", "lp-action-links-container");
    RendererUtil.attachWaiAriaAttr(actionLinksContainer,
      "Action Link Container");

    if (renderLoginPanel) {
      actionLinksContainer.setAttribute("class", "lp-action-links-container");
    } else {
      actionLinksContainer.setAttribute("class",
        "lp-action-links-container-no-login");
    }

    // gov benefits
    final Element govSectionContainer =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

    govSectionContainer.setAttribute("id", "gov-section-container");
    RendererUtil.attachWaiAriaAttr(govSectionContainer, "Section Container");

    if (renderLoginPanel) {
      govSectionContainer.setAttribute("class", "section-container");
    } else {
      govSectionContainer.setAttribute("class", "section-container-no-login");
    }

    renderGovBenefitsInfo(govSectionContainer, context, contract,
      screeningEnabled, intakeEnabled);

    actionLinksContainer.appendChild(govSectionContainer);

    /*
     * This section of the page should only display when triage is enabled.
     */
    if (triageEnabled) {
      // community services
      final Element commSectionContainer =
        fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

      RendererUtil.attachWaiAriaAttr(commSectionContainer,
        "Section Container");

      commSectionContainer.setAttribute("id", "community-section-container");
      if (renderLoginPanel) {
        commSectionContainer.setAttribute("class", "section-container");
      } else {
        commSectionContainer.setAttribute("class",
          "section-container-no-login");
      }

      renderCommunityServicesInfo(commSectionContainer, context, contract);

      actionLinksContainer.appendChild(commSectionContainer);
    }

    if (renderLoginPanel) {
      loginContainer.appendChild(expiredMessageSessionFragment);
      loginContainer.appendChild(loginPanel);
    }

    actionsContainer.appendChild(actionLinksContainer);

    // Set the browser tab title for the landing page. Calling
    // curam.util.setBrowserTabTitle() on the landing page will reset the
    // browser tab title to the static text only.
    final Element browserTabTitleScript =
      landingPageContainer.getOwnerDocument().createElement("script");

    browserTabTitleScript.appendChild(landingPageContainer.getOwnerDocument()
      .createProcessingInstruction(Result.PI_DISABLE_OUTPUT_ESCAPING, ""));
    browserTabTitleScript.setAttribute("type", "text/javascript");
    final String js =
      "require(['dojo/domReady!'], function() {"
      + "  var lpContainer = dojo.query('div#lp-container');"
      + "  if(lpContainer) {"
      + "    curam.debug.log('LandingPageRenderer.java calling curam.util.setBrowserTabTitle()');"
      + "    curam.util.getTopmostWindow().curam.util.setBrowserTabTitle();"
      + "  }" /*
               * no wrap
               */ + "});";

    HTMLUtils.appendText(browserTabTitleScript, js);
    browserTabTitleScript.appendChild(landingPageContainer.getOwnerDocument()
      .createProcessingInstruction(Result.PI_ENABLE_OUTPUT_ESCAPING, ""));
    landingPageContainer.appendChild(browserTabTitleScript);

    // RTC-168582, BD - Add script to install the appExitConfirmation
    RendererUtils.addAppExitConfirmationCode(landingPageContainer);
  }

  @SuppressWarnings("all")
  private void renderBannerText(final Field field,
    final Element landingPageBanner, final RendererContext context,
    final RendererContract contract, final boolean displayHCSelector)
    throws ClientException, DataAccessException, PlugInException {

    if (displayHCSelector) {
      // contrast mode selector
      final Element contrastModePanel =
        landingPageBanner.getOwnerDocument().createElement("div");

      HTMLUtils.appendComment(contrastModePanel, "Contrast Mode Selector");
      RendererUtil.attachWaiAriaAttr(contrastModePanel,
        "Contrast Mode Selector");
      contrastModePanel.setAttribute("dojoType", "cwtk.widget.FragmentPane");
      contrastModePanel.setAttribute("uim",
        "CitizenWorkspace_contrastSelectorFragment");
      contrastModePanel.setAttribute("id", "contrastModeSelector");
      landingPageBanner.appendChild(contrastModePanel);
    }
    final Element languageDiv =
      landingPageBanner.getOwnerDocument().createElement("div");

    languageDiv.setAttribute("class", "lp-welcome-banner-data");
    final Element headerLine1 =
      landingPageBanner.getOwnerDocument().createElement("h2");

    headerLine1.setAttribute("class", "lp-welcome-line1");
    headerLine1.setAttribute("id", "lp-welcome-line1");

    final DocumentFragment langPickerFragment =
      landingPageBanner.getOwnerDocument().createDocumentFragment();

    renderLangPicker(field, langPickerFragment, context, contract);
    landingPageBanner.appendChild(langPickerFragment);

    final ContainerBuilder descBuilder =
      ComponentBuilderFactory.createClusterBuilder();

    descBuilder.setStyle(context.getStyle("rich-text"));
    final String titlePart1 =
      PlayerUtils.getProperty(propertiesFileName, "Welcome.Message.Line1",
        context);
    // Filter content to prevent XSS attacks
    final String filteredTitlePart1 = SecurityUtils.filterACF(titlePart1);

    if (!BidiUtils.isBidi()) {
      descBuilder.setDescription(filteredTitlePart1);
    } else {
      descBuilder
        .setDescription(BidiUtils.addEmbedingUCC(filteredTitlePart1));
    }
    descBuilder.setParameter(RichTextComponent.kWrapContentParam, "true");
    final Container descRichText = descBuilder.getComponent();
    final DocumentFragment descFragment =
      landingPageBanner.getOwnerDocument().createDocumentFragment();

    context.render(descRichText, descFragment, contract);
    headerLine1.setTextContent(filteredTitlePart1);

    landingPageBanner.appendChild(languageDiv);

    landingPageBanner.appendChild(headerLine1);

    final Element headerLine2 =
      landingPageBanner.getOwnerDocument().createElement("h2");

    headerLine2.setAttribute("class", "lp-welcome-line2");
    headerLine2.setAttribute("id", "lp-welcome-line2");

    final ContainerBuilder descBuilder2 =
      ComponentBuilderFactory.createClusterBuilder();

    descBuilder2.setStyle(context.getStyle("rich-text"));

    final String titlePart2 =
      PlayerUtils.getProperty(propertiesFileName, "Welcome.Message.Line2",
        context);

    // Filter content to prevent XSS attacks
    final String filteredTitlePart2 = SecurityUtils.filterACF(titlePart2);

    if (!BidiUtils.isBidi()) {
      descBuilder2.setDescription(filteredTitlePart2);
    } else {
      descBuilder2.setDescription(BidiUtils
        .addEmbedingUCC(filteredTitlePart2));
    }
    descBuilder2.setParameter(RichTextComponent.kWrapContentParam, "true");

    final Container descRichText2 = descBuilder2.getComponent();
    final DocumentFragment descFragment2 =
      landingPageBanner.getOwnerDocument().createDocumentFragment();

    context.render(descRichText2, descFragment2, contract);

    headerLine2.setTextContent(filteredTitlePart2);
    landingPageBanner.appendChild(headerLine2);

    final Element headerLine3 =
      landingPageBanner.getOwnerDocument().createElement("p");

    headerLine3.setAttribute("class", "lp-welcome-line3");
    headerLine3.setAttribute("id", "lp-welcome-line3");

    final String titlePart3 =
      PlayerUtils.getProperty(propertiesFileName, "Welcome.Message.Line3",
        context);

    final ContainerBuilder descBuilder3 =
      ComponentBuilderFactory.createClusterBuilder();

    descBuilder3.setStyle(context.getStyle("rich-text"));
    // Filter content to prevent XSS attacks
    final String filteredTitlePart3 = SecurityUtils.filterACF(titlePart3);

    if (!BidiUtils.isBidi()) {
      descBuilder3.setDescription(filteredTitlePart3);
    } else {
      descBuilder3.setDescription(BidiUtils
        .addEmbedingUCC(filteredTitlePart3));
    }
    descBuilder3.setParameter(RichTextComponent.kWrapContentParam, "true");

    final Container descRichText3 = descBuilder3.getComponent();
    final DocumentFragment descFragment3 =
      landingPageBanner.getOwnerDocument().createDocumentFragment();

    context.render(descRichText3, descFragment3, contract);
    headerLine3.setTextContent(filteredTitlePart3);
    landingPageBanner.appendChild(headerLine3);

  }

  @SuppressWarnings("all")
  private void renderCommunityServicesInfo(
    final Element commSectionContainer, final RendererContext context,
    final RendererContract contract) throws PlugInException, ClientException,
    DataAccessException {

    // Add the header to the Section Container.
    final String title =
      PlayerUtils.getProperty(propertiesFileName, "Services.Title", context);

    createInfoHeader(context, commSectionContainer, title);

    // Add the description to the Section Container.
    final String description =
      PlayerUtils.getProperty(propertiesFileName, "Services.Description",
        context);

    renderSectionDescription(context, contract, description,
      commSectionContainer);

    renderButtonSection(context, commSectionContainer, TRIAGE_BUTTON, true,
      true);
  }

  @SuppressWarnings("all")
  private void renderGovBenefitsInfo(final Element govSectionContainer,
    final RendererContext context, final RendererContract contract,
    final boolean screeningEnabled, final boolean intakeEnbaled)
    throws PlugInException, ClientException, DataAccessException {

    // Add the header to the Section Container.
    final String title =
      PlayerUtils.getProperty(propertiesFileName,
      "Government.Benefits.Title", context);

    createInfoHeader(context, govSectionContainer, title);

    // Add the description to the Section Container.
    final String description =
      PlayerUtils.getProperty(propertiesFileName,
      "Government.Benefits.Description", context);

    renderSectionDescription(context, contract, description,
      govSectionContainer);

    renderButtonSection(context, govSectionContainer, SCREENING_BUTTON,
      screeningEnabled, intakeEnbaled);
  }

  /**
   * To render language picker.
   *
   * @param field
   * @param parentElement
   * @param context
   * @param contract
   * @throws ClientException
   * @throws DataAccessException
   * @throws PlugInException
   */
  private void renderLangPicker(final Field field,
    final DocumentFragment fragment, final RendererContext context,
    final RendererContract contract) throws ClientException,
    DataAccessException, PlugInException {

    final Element languagePickerDiv =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

    languagePickerDiv.setAttribute(HTMLConsts.CLASS, "lp-welcome-langPicker");
    languagePickerDiv.setAttribute("dojoType", "cwtk.widget.FragmentPane");
    languagePickerDiv.setAttribute("uim",
      "CitizenWorkspace_langSelectFragment");
    languagePickerDiv.setAttribute("id", "langSelectPane");
    RendererUtil.attachWaiAriaAttr(languagePickerDiv, "Language Picker");
    HTMLUtils.appendNbsp(languagePickerDiv);

    fragment.appendChild(languagePickerDiv);
  }

  /**
   * Adds a title/header to an information section.
   *
   * @param context
   * the context to render the section
   * @param SectionContainer
   * the Element that this header needs to be added to.
   * @param title
   * the title to add the the section as its header.
   * */
  private void createInfoHeader(final RendererContext context,
    final Element sectionContainer, final String title)
    throws ClientException, DataAccessException {

    final Element header =
      sectionContainer.getOwnerDocument().createElement("h2");

    RendererUtil.attachWaiAriaAttr(header, "Section header " + title);
    header.setAttribute("class", "lp-h2");

    if (!BidiUtils.isBidi()) {
      header.setTextContent(title);
    } else {
      header.setTextContent(BidiUtils.addEmbedingUCC(title));
    }
    sectionContainer.appendChild(header);
  }

  /**
   * Renders a description within a container for a section on the page. The
   * rich text in the description is filtered against XSS attacks.
   *
   * @param context
   * @param contract
   * @param description
   * the description to render within the section.
   * @param sectionContainer
   * the container that the description needs to be added to.
   * */
  private void renderSectionDescription(final RendererContext context,
    final RendererContract contract, final String description,
    final Element sectionContainer) throws PlugInException, ClientException,
    DataAccessException {

    final ContainerBuilder descBuilder =
      ComponentBuilderFactory.createClusterBuilder();

    descBuilder.setStyle(context.getStyle("rich-text"));

    // Filter content to prevent XSS attacks
    final String filteredDescription = SecurityUtils.filterACF(description);

    if (!BidiUtils.isBidi()) {
      descBuilder.setDescription(filteredDescription);
    } else {
      descBuilder.setDescription(BidiUtils
        .addEmbedingUCC(filteredDescription));
    }
    descBuilder.setParameter(RichTextComponent.kWrapContentParam, "true");
    final Container descRichText = descBuilder.getComponent();
    final DocumentFragment descFragment =
      sectionContainer.getOwnerDocument().createDocumentFragment();

    context.render(descRichText, descFragment, contract);
    sectionContainer.appendChild(descFragment);
  }

  private void renderButtonSection(final RendererContext context,
    final Element sectionContainer, final int buttonType,
    final boolean screeningEnabled, final boolean intakeEnabled)
    throws ClientException, DataAccessException {

    String buttonText;
    String buttonAltText;

    final String htmlDirection =
      PlayerUtils.getHtmlPresentationDirection(getLocale());
    final boolean isRTL = "rtl".equals(htmlDirection);
    final Element buttonDiv =
      sectionContainer.getOwnerDocument().createElement("div");

    switch (buttonType) {
      case TRIAGE_BUTTON:
        // Render the community services information section.
        RendererUtil.attachWaiAriaAttr(buttonDiv,
          "Community Services Section");
        buttonText =
          PlayerUtils.getProperty(propertiesFileName, "Triage.Button.Text",
            context);
        buttonAltText =
          PlayerUtils.getProperty(propertiesFileName,
          "Triage.Button.Alt.Text", context);

        buttonDiv.appendChild(createButton(sectionContainer, "Triage Button",
          buttonText, buttonAltText, "SetupTriage", true));
      break;

      case SCREENING_BUTTON:
        // Render the government benefits information section.
        RendererUtil.attachWaiAriaAttr(buttonDiv,
          "Government Benifits Section");
        buttonText =
          PlayerUtils.getProperty(propertiesFileName,
          "Screening.Button.Text", context);
        buttonAltText =
          PlayerUtils.getProperty(propertiesFileName,
          "Screening.Button.Alt.Text", context);

        buttonDiv.appendChild(createButton(sectionContainer,
          "Screening Button", buttonText, buttonAltText, "SetupScreening",
          screeningEnabled));

        // Set up for the apply button
        buttonText =
          PlayerUtils.getProperty(propertiesFileName, "Apply.Button.Text",
            context);
        buttonAltText =
          PlayerUtils.getProperty(propertiesFileName,
          "Apply.Button.Alt.Text", context);

        buttonDiv.appendChild(createButton(sectionContainer,
          "Apply Button Wrapper", buttonText, buttonAltText, "SetupIntake",
          intakeEnabled));
      break;
    }

    sectionContainer.appendChild(buttonDiv);
  }

  private Element createButton(final Element sectionContainer,
    final String buttonName, final String buttonText,
    final String buttonAltText, final String nextPage,
    final boolean isClientPropertyEnabled) throws ClientException,
    DataAccessException {

    final String htmlDirection =
      PlayerUtils.getHtmlPresentationDirection(getLocale());
    final boolean isRTL = "rtl".equals(htmlDirection);

    final Element buttonWrapper =
      sectionContainer.getOwnerDocument().createElement("div");

    RendererUtil.attachWaiAriaAttr(sectionContainer, buttonName);
    buttonWrapper.setAttribute("style", "float: "
      + (isRTL ? "right" : "left"));

    final Element button =
      sectionContainer.getOwnerDocument().createElement("span");

    RendererUtil.attachWaiAriaButtonAttr(button, buttonName);
    HTMLUtils.appendComment(button, "comment");

    button.setAttribute("data-dojo-type", "dijit/form/Button");
    button.setAttribute("label", buttonText);
    button.setAttribute(HTMLConsts.TITLE, buttonAltText);
    button
      .setAttribute(
        "onClick",
      "displayContent({pageID:'PagePlayerResolveWrapper', param: [{paramKey: \"page\", paramValue: \""
        + nextPage + "\"}]})");
    button.setAttribute("class", "idxSpecialButton");

    /*
     * This button should only be disabled when clientProperty is disabled.
     */
    if (!isClientPropertyEnabled) {
      button.setAttribute("disabled", "true");
    }

    if (BidiUtils.isBidi()) {
      BidiUtils.setTextDirForElement(button);
    }

    buttonWrapper.appendChild(button);
    return buttonWrapper;
  }

  /**
   * Render the session expired message session .
   *
   * @param parentElement
   * @param context
   * @param contract
   * @throws ClientException
   * @throws DataAccessException
   * @throws PlugInException
   */
  private void renderExpiredMessageSession(final DocumentFragment fragment,
    final RendererContext context, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    final Element expiredMessageSessionDiv =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

    expiredMessageSessionDiv.setAttribute("id", "expired-message-container");
    RendererUtil.attachWaiAriaAttr(expiredMessageSessionDiv,
      "Expired Message Session Wrapper");
    HTMLUtils.appendNbsp(expiredMessageSessionDiv);
    fragment.appendChild(expiredMessageSessionDiv);

    context.includeScripts("", JspUtil
      .getSessionExpiredMessageScript(expiredMessageSessionDiv
        .getAttribute("id")));

  }

}
