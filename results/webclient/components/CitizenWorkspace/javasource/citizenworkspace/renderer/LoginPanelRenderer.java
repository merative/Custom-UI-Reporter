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

import citizenworkspace.pageplayer.HTMLConsts;
import citizenworkspace.util.ClientProperties;
import curam.ieg.player.PlayerUtils;
import curam.omega3.util.CDEJResources;
import curam.util.client.ClientException;
import curam.util.client.domain.render.edit.AbstractEditRenderer;
import curam.util.client.model.ComponentBuilderFactory;
import curam.util.client.model.Field;
import curam.util.client.model.FieldBuilder;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.domain.Domain;
import curam.util.common.path.DataAccessException;
import curam.util.common.plugin.PlugInException;
import curam.util.dom.html2.HTMLUtils;
import java.util.Locale;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Renderer class for rendering the login panel.
 */
public class LoginPanelRenderer extends AbstractEditRenderer {

  private static final String propertiesFileName = "LoginPanel";

  @SuppressWarnings("all")
  @Override
  public void render(final Field field, final DocumentFragment fragment,
    final RendererContext context, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    final Document ownerDocument = fragment.getOwnerDocument();

    // create an outer div for the panel
    final Element loginPanelDiv =
      ownerDocument.createElement(HTMLConsts.DIV_TAG);

    loginPanelDiv.setAttribute("class", "login-panel-wrapper");

    final Element loginHeaderDiv =
      ownerDocument.createElement(HTMLConsts.DIV_TAG);

    loginHeaderDiv.setAttribute("class", "login-header");

    loginPanelDiv.appendChild(loginHeaderDiv);

    // create the top label e.g. "Log into your account"
    final Element topLabelEle = ownerDocument.createElement("h2");

    topLabelEle.setAttribute("class", "login-panel-title");

    final String topLabelText =
      PlayerUtils.getProperty(propertiesFileName, "Banner.Text", context);

    topLabelEle.appendChild(fragment.getOwnerDocument().createTextNode(
      topLabelText));

    loginHeaderDiv.appendChild(topLabelEle);

    // add the 2 fields
    final Element fieldContainer = ownerDocument.createElement("div");

    fieldContainer.setAttribute("id", "login-field-container");
    fieldContainer.setAttribute("class", "login-field-container");

    loginPanelDiv.appendChild(fieldContainer);

    // setup the username field
    final Element usernameField =
      loginPanelDiv.getOwnerDocument().createElement("div");
    usernameField.setAttribute("class", "login-field-wrapper");

    createField(field, context, contract, usernameField,
      "Username.Label.Text", "PAGE_PLAYER_STRING_50", "username");
    fieldContainer.appendChild(usernameField);

    // setup the password field
    final Element passwordField =
      loginPanelDiv.getOwnerDocument().createElement("div");
    passwordField.setAttribute("class", "login-field-wrapper");
    createField(field, context, contract, passwordField,
      "Password.Label.Text", "CW_PASSWORD", "password");

    fieldContainer.appendChild(passwordField);

    final String buttonText =
      PlayerUtils.getProperty(propertiesFileName, "Button.Text", context);

    final String buttonAltText =
      PlayerUtils.getProperty(propertiesFileName, "Button.Alt.Text", context);

    createSubmitButton(loginPanelDiv, buttonText, buttonAltText);

    final String forgotPasswordLinkText =
      PlayerUtils.getProperty(propertiesFileName, "Forgot.Password.Text",
        context);

    final String forgotPasswordLinkAltText =
      PlayerUtils.getProperty(propertiesFileName, "Forgot.Password.Alt.Text",
        context);

    createForgottenPWLink(loginPanelDiv, forgotPasswordLinkText,
      forgotPasswordLinkAltText);

    fragment.appendChild(loginPanelDiv);
  }

  @SuppressWarnings("all")
  private Field createField(final Field field, final RendererContext context,
    final RendererContract contract, final Element parentEle,
    final String labelProperty, final String domain, final String fieldName)
    throws ClientException, DataAccessException, PlugInException {

    final DocumentFragment tempDocumentFragment =
      parentEle.getOwnerDocument().createDocumentFragment();

    final FieldBuilder fieldBuilder =
      ComponentBuilderFactory.createFieldBuilder();

    final Locale locale = getLocale();

    fieldBuilder.copy(field);

    final String labelText =
      ClientProperties.getApplicationProperty(propertiesFileName,
        labelProperty, context);

    fieldBuilder.setTitle(labelText);

    final Domain fieldDomain = context.getDomain(domain);

    fieldBuilder.setDomain(fieldDomain);

    fieldBuilder.setTargetPath("/data/si/ACTION/key$loginDetails/"
      + fieldName);

    context.render(fieldBuilder.getComponent(), tempDocumentFragment,
      contract.createSubcontract());

    parentEle.appendChild(tempDocumentFragment);

    // GUM-3686
    final String inputID = getInputElementId(parentEle);
    addFormInputLabel(parentEle.getOwnerDocument(), parentEle, inputID,
      labelText);

    return fieldBuilder.getComponent();
  }

  /**
   * Add a <label for=""> element to given container element, styling it such
   * that it is not visible. Required for screen readers.
   * 
   * @param ownerDocument
   * Containing DOM document.
   * @param container
   * Container element to which label will be added.
   * @param inputID
   * ID of associated input field
   * @param label
   * Label text
   */
  private void addFormInputLabel(final Document ownerDocument,
    final Element container, final String inputID, final String label) {

    final Element jsEle = ownerDocument.createElement("script");

    jsEle.setAttribute("type", "text/javascript");

    final StringBuffer sb = new StringBuffer();

    sb.append("document.getElementById('" + inputID
      + "').setAttribute('placeHolder', '" + label + "');");

    HTMLUtils.appendText(jsEle, sb.toString());

    container.appendChild(jsEle);
  }

  /**
   * Given an element tree, return ID of first input element found in tree.
   * 
   * @param rootElement
   * Root of element tree
   * @return ID of first input element, else null if none discovered
   */
  private String getInputElementId(final Element rootElement) {

    if ("input".equalsIgnoreCase(rootElement.getTagName())) {
      return rootElement.getAttribute("id");
    }
    final NodeList nodeList = rootElement.getElementsByTagName("input");
    for (int i = 0; i < nodeList.getLength(); i++) {
      final Node node = nodeList.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        return ((Element) node).getAttribute("id");
      }
    }
    return null;
  }

  private void createSubmitButton(final Element parentElement,
    final String buttonText, final String buttonAltText) {

    final Element wrapperDiv =
      parentElement.getOwnerDocument().createElement("div");

    final Element button =
      parentElement.getOwnerDocument().createElement("span");

    HTMLUtils.appendComment(button, "comment");

    button.setAttribute("data-dojo-type", "dijit/form/Button");
    button.setAttribute("label", buttonText);
    button.setAttribute(HTMLConsts.TITLE, buttonAltText);
    button.setAttribute("type", "submit");

    wrapperDiv.appendChild(button);

    parentElement.appendChild(wrapperDiv);

  }

  private void createForgottenPWLink(final Element parentElement,
    final String forgotPasswordLinkText,
    final String forgotPasswordLinkAltText) {

    final Element linkWrapper =
      parentElement.getOwnerDocument().createElement("div");

    linkWrapper.setAttribute("style", "margin-top: 5px");

    final Element linkEle =
      parentElement.getOwnerDocument().createElement("a");

    // no style required, use the default oneui style
    linkEle.setAttribute("class", "cw-program-show-link");
    linkEle.setAttribute("href", "#");
    linkEle.setAttribute("title", forgotPasswordLinkAltText);

    String forgotPasswordUrl =
      CDEJResources
        .getApplicationDataProperty("curam.citizenworkspace.forgot.password.url");
    // If application property has been set, create redirect link to that page
    if (forgotPasswordUrl != null) {
      forgotPasswordUrl = "window.open(\"" + forgotPasswordUrl + "\")";
    } else { // Otherwise redirect to our "Forgotten Your Password" page
      forgotPasswordUrl =
        "displayContent({pageID:'PagePlayerWrapper', param: [{paramKey:\"page\", paramValue:\"ForgottenPassword\"}]})";
    }

    linkEle.setAttribute("onclick", forgotPasswordUrl);

    linkEle.setTextContent(forgotPasswordLinkText);

    linkWrapper.appendChild(linkEle);

    parentElement.appendChild(linkWrapper);

  }
}
