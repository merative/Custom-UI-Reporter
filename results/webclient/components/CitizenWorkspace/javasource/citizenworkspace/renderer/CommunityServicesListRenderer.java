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
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import curam.util.common.util.JavaScriptEscaper;
import curam.util.dom.html2.HTMLUtils;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

// REVIEW_HIGH, Define the javascript interface to the map renderer
// e.g. boxClick, centreOnAddress, submitAddress

// REVIEW_MEDIUM, Consider separating the address widget into its own renderer,
// it doesn't really belong with the services list from a functionality POV.

// REVIEW_MEDIUM, address the TODOs listed below

// REVIEWED Ramesh.Khairmode 20/04/2010
// REVIEW_MEDIUM, Ramesh, 20/04/2010 - insufficient java doc for renderer
// REVIEW_HIGH, Ramesh, 20/04/2010 - action TODOs
// REVIEW_MEDIUM, Ramesh, 20/04/2010 - use style sheet for goButtonEle,
// textInputEle and input elements

/**
 * This renderer renders the community services list for the Triage Results
 * page.
 */
public class CommunityServicesListRenderer extends AbstractViewRenderer {

  /**
   * The name of the properties file that stores the localised text for this
   * renderer.
   */
  private static final String propertiesFileName = "ServiceProviders";

  // CHECKSTYLE_OFF: MethodLength
  @Override
  public void render(final Field field, final DocumentFragment fragment,
    final RendererContext context, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    // used to create unique IDs for expand description links
    int idCounter = 0;

    final int numServicesToBeCheckedOnPageLoad =
      Integer.valueOf(context.getDataAccessor()
        .get(
          field.getBinding().getSourcePath()
            .extendPath("num-checked-services")));

    // show-min-zoom-passed-message
    final String showMinZoomLevelPassed =
      context.getDataAccessor().get(
        field.getBinding().getSourcePath()
          .extendPath("show-min-zoom-passed-message"));

    final String listTitle;

    final String addressFieldInitialText =
      PlayerUtils.getProperty(propertiesFileName,
        "Address.Field.InitialText", context);

    final String communityServicesTitle =
      PlayerUtils.getProperty(propertiesFileName, "Community.Services.Title",
        context);

    final String goButtonText =
      PlayerUtils.getProperty(propertiesFileName, "Go.Button.Text", context);

    final String goButtonAltText =
      PlayerUtils.getProperty(propertiesFileName, "Go.Button.Alt.Text",
        context);

    final boolean isBidi = BidiUtils.isBidi();

    // TODO from configuration
    // final String goButtonName = "go_button_prototype";

    // DIV to wrap the list and address input field
    final Element leftSideDivEle =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    leftSideDivEle.setAttribute(HTMLConsts.CLASS, "comm-services-container");
    RendererUtil.attachWaiAriaAttr(leftSideDivEle, "CommonService");
    fragment.appendChild(leftSideDivEle);

    // DIV to hold the left title
    final Element leftTitleEle =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    leftSideDivEle.appendChild(leftTitleEle);

    final Element leftTitlePara =
      fragment.getOwnerDocument().createElement(HTMLConsts.PARAGRAPH);
    if (!isBidi) {
      leftTitlePara.setTextContent(communityServicesTitle);
    } else {
      leftTitlePara.setTextContent(BidiUtils
        .addEmbedingUCC(communityServicesTitle));
    }
    RendererUtil.attachWaiAriaAttr(leftTitlePara, "CommunityService");
    leftTitlePara.setAttribute(HTMLConsts.CLASS, "community-services-title");

    leftTitleEle.setAttribute(HTMLConsts.CLASS, "section-title");
    RendererUtil.attachWaiAriaAttr(leftTitleEle, "CommunityService");
    leftTitleEle.appendChild(leftTitlePara);

    /**
     * Create the input box for address
     */
    final Element inputDivEle =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    inputDivEle.setAttribute("class", "triageSearchBox");
    RendererUtil.attachWaiAriaAttr(inputDivEle, "TriageSearch");
    leftSideDivEle.appendChild(inputDivEle);

    final String htmlDirection =
      PlayerUtils.getHtmlPresentationDirection(getLocale());
    final boolean isRTL = "rtl".equals(htmlDirection);

    final Element textInputDiv =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    textInputDiv.setAttribute("style", "float: " + (isRTL ? "right" : "left")
      + "; height: 25px;");
    RendererUtil.attachWaiAriaAttr(textInputDiv, "Inputs");

    final Element textInputEle =
      fragment.getOwnerDocument().createElement(HTMLConsts.INPUT_TAG);
    textInputEle.setAttribute("value", addressFieldInitialText);
    textInputEle.setAttribute("name", "addressField");
    textInputEle.setAttribute("id", "addressField");
    textInputEle.setAttribute("style", "margin-" + (isRTL ? "right" : "left")
      + ":5px; width: 150px");
    textInputEle.setAttribute(HTMLConsts.TITLE, PlayerUtils.getProperty(
      propertiesFileName, "Address.Field.Tooltip", context));
    RendererUtil.attachWaiAriaAttr(textInputEle, "InputAddress");

    inputDivEle.appendChild(textInputDiv);
    textInputDiv.appendChild(textInputEle);

    final Element goButtonContainer =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    goButtonContainer.setAttribute(HTMLConsts.CLASS, "go-button");
    RendererUtil.attachWaiAriaAttr(goButtonContainer, "GoButton");
    inputDivEle.appendChild(goButtonContainer);

    final Element goButtonEle =
      fragment.getOwnerDocument().createElement(HTMLConsts.A_TAG);

    // add dojo button type
    goButtonEle.setAttribute("data-dojo-type", "dijit/form/Button");

    // click is a submit address
    goButtonEle
      .setAttribute(
        "onclick",
        "centreOnAddress(document.getElementById('addressField').value); return false;");

    goButtonEle.setAttribute("id", "goButton");
    goButtonEle.setAttribute(HTMLConsts.ALT_TAG, goButtonAltText);

    goButtonEle.setAttribute(HTMLConsts.HREF_TAG, "#");
    goButtonEle.setAttribute(HTMLConsts.TITLE, PlayerUtils.getProperty(
      propertiesFileName, "Submit.Address.Tooltip", context));

    // add V6 styling to this button
    goButtonEle.setAttribute(HTMLConsts.CLASS,
      "bhv-nav-button buttonLink needsScript-hide-inline");

    goButtonEle.appendChild(RendererUtils.generateButton(fragment,
      goButtonText));
    RendererUtil.attachWaiAriaAttr(goButtonEle, "SubmitAddress");
    goButtonContainer.appendChild(goButtonEle);

    // create the title DIV
    final Element titleDiv =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    titleDiv.setAttribute("class", "servicesTitle");
    RendererUtil.attachWaiAriaAttr(titleDiv, "ServicesTitle");
    leftSideDivEle.appendChild(titleDiv);

    /**
     * Create the Services List
     */
    final Element divEle =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    divEle.setAttribute("class", "servicesList");
    HTMLUtils.appendComment(divEle, "comment to guard against an empty div");
    RendererUtil.attachWaiAriaAttr(divEle, "ServicesList");
    leftSideDivEle.appendChild(divEle);

    final Path serviceOfferingsPath =
      field.getBinding().getSourcePath()
        .extendPath("service-offerings/service-offering[]");

    // for each...
    final int numServiceOfferings =
      context.getDataAccessor().count(serviceOfferingsPath);
    if (numServiceOfferings > numServicesToBeCheckedOnPageLoad) {

      final String tempString =
        PlayerUtils.getProperty(propertiesFileName,
          "More.Than.Initial.Num.Services.Available", context);

      listTitle =
        tempString.replace("%1s",
          String.valueOf(numServicesToBeCheckedOnPageLoad));
    } else {
      listTitle =
        PlayerUtils.getProperty(propertiesFileName,
          "Less.Than.Initial.Num.Services.Available", context);
    }
    if (!isBidi) {
      titleDiv.setTextContent(listTitle);
    } else {
      titleDiv.setTextContent(BidiUtils.addEmbedingUCC(listTitle));
    }

    /**
     * Render the list of services
     */
    for (int i = 1; i < numServiceOfferings + 1; i++) {

      final Path serviceOfferingPath =
        serviceOfferingsPath.applyIndex(0, i, true);

      final String name =
        context.getDataAccessor().get(serviceOfferingPath.extendPath("name"));
      final String description =
        context.getDataAccessor().get(
          serviceOfferingPath.extendPath("description"));

      final String reference =
        context.getDataAccessor().get(
          serviceOfferingPath.extendPath("reference"));

      final Element listEntryDiv =
        fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      listEntryDiv.setAttribute("class", "servicesListEntry");
      listEntryDiv.setAttribute("id", "service" + i);
      RendererUtil.attachWaiAriaAttr(listEntryDiv, "ServicesListEntry");
      divEle.appendChild(listEntryDiv);

      // create checkbox
      final Element input =
        fragment.getOwnerDocument().createElement("input");
      input.setAttribute("class", "services-list");
      input.setAttribute("type", "checkbox");
      // TODO do i need ID what does it do for me
      input.setAttribute("onclick", getOnClick(reference));
      input.setAttribute("id", getInputID(reference));
      input.setAttribute(HTMLConsts.TITLE, PlayerUtils.getProperty(
        propertiesFileName, "Checkbox.Tooltip", context));
      input.setAttribute("reference", reference);
      input.setAttribute("listitemnumber", String.valueOf(i));
      RendererUtil.attachWaiAriaAttr(input, "ServiceInput");

      if (i <= numServicesToBeCheckedOnPageLoad) {
        input.setAttribute("checked", "yes");
      }

      final Element serviceName =
        fragment.getOwnerDocument().createElement(HTMLConsts.SPAN_TAG);
      serviceName.setAttribute("class", "serviceName");
      serviceName.setAttribute("onclick", "toggleDiv('flipper-" + i
        + "', 'desc" + i + "');");
      RendererUtil.attachWaiAriaAttr(serviceName, "Service");
      if (!isBidi) {
        serviceName.setTextContent(name);
      } else {
        serviceName.setTextContent(BidiUtils.addEmbedingUCC(name));
      }

      final Element divToHide =
        fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      divToHide.setAttribute(HTMLConsts.ID, "desc" + i);
      divToHide.setAttribute(HTMLConsts.CLASS, "moreInfo");
      RendererUtil.attachWaiAriaAttr(divToHide, "Service");
      divToHide.setAttribute("displayed", "off");

      final Element showDescriptionSpan =
        fragment.getOwnerDocument().createElement(HTMLConsts.SPAN_TAG);
      showDescriptionSpan.setAttribute(HTMLConsts.CLASS,
        "helpDescriptionText");
      if (!isBidi) {
        showDescriptionSpan.setTextContent(description);
      } else {
        showDescriptionSpan.setTextContent(BidiUtils
          .addEmbedingUCC(description));
      }
      RendererUtil.attachWaiAriaAttr(showDescriptionSpan, "HelpDesc");
      divToHide.appendChild(showDescriptionSpan);

      final Element imageEle =
        fragment.getOwnerDocument().createElement(HTMLConsts.IMG_TAG);
      imageEle.setAttribute("src", "../Images/services_numbers/service_no_"
        + i + ".png");
      imageEle.setAttribute(HTMLConsts.CLASS, "list-icon");
      imageEle.setAttribute("onclick", "toggleDiv('flipper-" + i + "', 'desc"
        + i + "');");
      imageEle.setAttribute(HTMLConsts.ALT_TAG, name);
      RendererUtil.attachWaiAriaAttr(imageEle, "Service");

      final Element flipperEle =
        fragment.getOwnerDocument().createElement(HTMLConsts.IMG_TAG);
      flipperEle.setAttribute("src", "../Images/arrow_contract.png");
      flipperEle.setAttribute("onclick", "toggleDiv('flipper-" + i
        + "', 'desc" + i + "');");
      flipperEle.setAttribute(HTMLConsts.ID, "flipper-" + i);
      flipperEle.setAttribute(HTMLConsts.CLASS, "flipper-image");
      flipperEle.setAttribute(HTMLConsts.TITLE, PlayerUtils.getProperty(
        propertiesFileName, "Service.Description.Tooltip", context));
      flipperEle.setAttribute(HTMLConsts.ALT_TAG, PlayerUtils.getProperty(
        propertiesFileName, "Service.Description.Tooltip", context));
      RendererUtil.attachWaiAriaAttr(flipperEle, "Service");

      listEntryDiv.appendChild(input);
      listEntryDiv.appendChild(serviceName);
      listEntryDiv.appendChild(imageEle);
      listEntryDiv.appendChild(flipperEle);
      listEntryDiv.appendChild(divToHide);

      // increment the counter
      idCounter++;
    }

    // used to determine whether to show the zoom warning
    final StringBuffer scriptText = new StringBuffer();

    scriptText.append("var SHOW_MIN_ZOOM_LEVEL_PASSED = "
      + JavaScriptEscaper.escapeText(showMinZoomLevelPassed) + ";");

    context.includeScripts("text/javascript", scriptText.toString());
  }

  // CHECKSTYLE_ON: MethodLength

  private String getInputID(final String reference) {

    return reference + "box";
  }

  private String getOnClick(final String reference) {

    return "boxclick(this, '" + reference + "')";
  }
}
