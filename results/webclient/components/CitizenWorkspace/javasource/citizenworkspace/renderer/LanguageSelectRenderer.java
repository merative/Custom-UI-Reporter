/*
 * Licensed Materials - Property of IBM
 * 
 * PID 5725-H26
 * 
 * Copyright IBM Corporation 2012, 2016. All rights reserved.
 * 
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 */
package citizenworkspace.renderer;

import citizenaccount.renderer.RendererUtil;
import citizenworkspace.pageplayer.HTMLConsts;
import citizenworkspace.util.ClientProperties;
import citizenworkspace.util.XmlTools;
import curam.ieg.player.PlayerUtils;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.plugin.PlugInException;
import curam.util.common.util.JavaScriptEscaper;
import curam.util.dom.html2.HTMLUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Renderer class for rendering the language select.
 */
public class LanguageSelectRenderer extends AbstractViewRenderer {

  /**
   * The index at which to start the substring retrieval for obtaining the URL.
   */
  public static final int urlSubstringStartIndex = 3;

  private static final String kRedirectQueryParam = "r";

  // BEGIN, 155467, PP
  static final String PROPERTIES_FILE_NAME =
    "citizenworkspace.renderer.LanguageSelect";

  // END, 155467

  @Override
  public void render(final Field field, final DocumentFragment fragment,
    final RendererContext context, final RendererContract contract)
      throws ClientException, DataAccessException, PlugInException {

    final Document rendererFieldDocument =
      XmlTools.getRendererFieldDocument(field, context);

    final String xPathExpression = "//languages/language";

    NodeList list;

    try {
      list = XmlTools.getXMLNodeList(xPathExpression, rendererFieldDocument);
    } catch (final XPathExpressionException e) {

      Logger.getAnonymousLogger().log(Level.WARNING, e.getLocalizedMessage());

      return;
    }

    final Element langDD =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    langDD.setAttribute(HTMLConsts.ID, "selectLang");
    RendererUtil.attachWaiAriaAttr(langDD, "Language");
    HTMLUtils.appendComment(langDD, "dummy comment");
    fragment.appendChild(langDD);

    // the language that the user session is running in.
    final String langStr = getLocale().toString();

    final Element javaScript =
      fragment.getOwnerDocument().createElement("script");

    final StringBuffer javaScriptText =
      new StringBuffer(
        "require([\"dijit/form/Select\", \"dojo/store/Memory\", \"dojo/ready\"], function(Select, Memory, ready){");

    javaScript.setAttribute("type", "text/javascript");
    // BEGIN, 155467, PP
    final String language =
      ClientProperties.getProperty(context, PROPERTIES_FILE_NAME, "Language");
    // END, 155467
    final StringBuffer sb = new StringBuffer();

    sb.append("ready(function(){");
    sb.append("var select = new Select({");
    sb.append("name: 'selectLang',");
    // BEGIN, 155467, PP
    // BEGIN, 173899, MV
    sb.append("title: '" + JavaScriptEscaper.escapeText(language) + "',");
    // END, 173899
    // END, 155467
    sb.append("options: [");

    for (int i = 0; i < list.getLength(); i++) {

      final StringBuffer entrySB = new StringBuffer();

      final Node node = list.item(i);

      final String value =
        node.getAttributes().getNamedItem("value").getNodeValue();

      final String displayVal =
        node.getAttributes().getNamedItem("display-value").getNodeValue();

      String url = getLocaleRedirectUrl(context, value);
      url = url.substring(urlSubstringStartIndex, url.length());
      url =
        url.replaceFirst(langStr + "%2FCitizenWorkspace_landingPagePage",
          "application");

      entrySB.append("{value: '");
      entrySB.append(url);
      entrySB.append("', label: '");
      // BEGIN, 173899, MV
      entrySB.append(JavaScriptEscaper.escapeText(displayVal));
      // END, 173899
      entrySB.append("'");
      if (langStr.equalsIgnoreCase(value)) {
        entrySB.append(", selected: true, disabled: true");
      }
      entrySB.append("}");

      if (i != list.getLength() - 1) {
        entrySB.append(",");
      }

      sb.append(entrySB);
    }

    sb.append("],");
    sb.append("onChange: function(value) {");

    // This is block is done to convert '&amp;' back to '&' in the URL.
    // It is converted to the '&amp;' as it is part of a Element.
    // The solution is ugly but it's safer than parsing characters.
    sb.append("var div = document.createElement('div');");
    sb.append("div.innerHTML = value;");
    sb.append("window.location.href = div.firstChild.nodeValue;");

    sb.append("}");
    sb.append("}, 'selectLang');");
    sb.append("select.startup();");
    sb.append("});");
    sb.append("});");

    javaScriptText.append(sb);

    javaScript.setTextContent(javaScriptText.toString());

    langDD.appendChild(javaScript);

  }

  /**
   * Due to the way that UA works, we cannot redirect to a particular subpage;
   * we must always redirect to the application.do landing page.
   *
   * We are getting URLs of the form:
   * http://localhost:9080/CitizenPortal/servlet
   * /locale?l=es&r=http%3A%2F%2Flocalhost
   * %3A9080%2FCitizenPortal%2Fen_CA%2FCitizenWorkspace_landingPagePage
   * .do%3Fdojo.preventCache%3D1366878322508%26o3ctx%3D1048576
   *
   * @param context
   * @param value
   * @return
   * @throws ClientException
   * @throws DataAccessException
   */
  private String getLocaleRedirectUrl(final RendererContext context,
    final String locale) throws DataAccessException, ClientException {

    final String url = PlayerUtils.getLocaleSetterURI(context, locale);
    final int indexOfQ = url.indexOf('?');
    final String urlMinusQuery = url.substring(0, indexOfQ);
    final String urlParams =
      fixUrlParams(locale, url.substring(indexOfQ + 1));

    // http://localhost:9080/CitizenPortal/servlet/locale?l=en_CA&r=../application.do
    return urlMinusQuery + "?" + urlParams;
  }

  private String fixUrlParams(final String locale, final String query) {

    final String[] queryParts = query.split("\\&");

    for (int i = 0; i < queryParts.length; i++) {
      final String param = queryParts[i];
      final int eqIdx = param.indexOf('=');
      if (eqIdx != -1
        && kRedirectQueryParam.equals(param.substring(0, eqIdx))) {
        queryParts[i] = "r=../application.do";
      }
    }

    return StringUtils.join(queryParts, '&');
  }

}
