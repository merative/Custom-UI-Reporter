/*
 * Licensed Materials - Property of IBM
 *
 * PID 5725-H26
 *
 * Copyright IBM Corporation 2012-2017. All rights reserved.
 *
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 */
package citizenworkspace.renderer.eligibilityresults;

import citizenworkspace.renderer.RendererUtils;
import citizenworkspace.util.XmlTools;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.ComponentBuilderFactory;
import curam.util.client.model.Field;
import curam.util.client.model.FieldBuilder;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import curam.util.dom.html2.HTMLUtils;
import javax.xml.transform.Result;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * This is the top level renderer for the motivation eligibility results page.
 * This is a domains renderer mapped via DomainsConfig. This renderer takes the
 * full xml document and adds the component renders, which themselves add other
 * component renderers, to produce the motiviation eligibility results page.
 */
public class EligibilityResultsRenderer extends AbstractViewRenderer {

  @Override
  public void render(final Field component,
    final DocumentFragment documentFragment, final RendererContext context,
    final RendererContract contract) throws ClientException,
    DataAccessException, PlugInException {

    /*
     * Store the parsed xml so the child component renderers can get it without
     * reparsing. Accessing the full xml document in component renderers should
     * be avoided unless there is no alternative, ideally each renderer should
     * only deal with the xml fragment that it requires.
     */
    documentFragment.getOwnerDocument().setUserData("xml",
      XmlTools.getRendererFieldDocument(component, context), null);
    XmlTools.convertDocumentToText(XmlTools.getRendererFieldDocument(
      component, context));

    /*
     * Create a field for the layout renderer
     */
    final Field field = component;
    final FieldBuilder fBuilder =
      ComponentBuilderFactory.createFieldBuilder();

    fBuilder.copy(field);
    fBuilder.setDomain(null);

    /*
     * Add the layout component renderer and set the source
     */
    fBuilder.setStyle(context.getStyle("cw-eligibility-results-layout"));
    final Path sourcePath = field.getBinding().getSourcePath();

    fBuilder.setSourcePath(sourcePath);
    context.render(fBuilder.getComponent(), documentFragment,
      contract.createSubcontract());
    RendererUtils.fixEmptyHtmlElements(documentFragment);

    // Add javascript to set browser tab title
    addBrowserTabTitleCode(documentFragment);

    // RTC-168582, BD - Add script to install the appExitConfirmation
    RendererUtils.addAppExitConfirmationCode(documentFragment);

  }

  /**
   * Adds javascript code that sets the browser tab title to the primary title
   * on the page.
   *
   * @param docFrag
   * the {@linkplain DocumentFragment} we will attach the javascript to
   */
  private void addBrowserTabTitleCode(final DocumentFragment docFrag) {

    final Element browserTabTitleScript =
      docFrag.getOwnerDocument().createElement("script");

    browserTabTitleScript.appendChild(docFrag.getOwnerDocument()
      .createProcessingInstruction(Result.PI_DISABLE_OUTPUT_ESCAPING, ""));
    browserTabTitleScript.setAttribute("type", "text/javascript");
    final String js =
      "require(['dojo/domReady!', 'dojo/_base/lang'], function(domReady, lang) {"
        + "  var x = dojo.query('div#programs-div > div.cw-primary.cw-title-pane > div.cw-title-pane-title > div.cw-title-pane-text-div');"
        + "  if(x && x.length == 1 && x[0].textContent) {"
        + "    var title = lang.trim(x[0].textContent);"
        + "    curam.debug.log('EligibilityResultsRenderer calling curam.util.setBrowserTabTitle(' + title + ')');"
        + "    curam.util.getTopmostWindow().curam.util.setBrowserTabTitle(title);"
        + "  } else {" /*
                        * no wrap
                        */
        + "    curam.debug.log('Could not find title for EligibilityResultsRenderer page (x = '+x+')');"
        + "  }" /*
                 * no wrap
                 */ + "});";

    HTMLUtils.appendText(browserTabTitleScript, js);
    browserTabTitleScript.appendChild(docFrag.getOwnerDocument()
      .createProcessingInstruction(Result.PI_ENABLE_OUTPUT_ESCAPING, ""));
    docFrag.appendChild(browserTabTitleScript);
  }

}
