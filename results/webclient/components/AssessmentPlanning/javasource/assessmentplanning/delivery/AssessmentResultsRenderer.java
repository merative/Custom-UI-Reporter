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
 * Copyright 2011 Curam Software Ltd.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Curam
 * Software, Ltd. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Curam Software.
 */
package assessmentplanning.delivery;

import curam.client.util.StringHelper;
import curam.client.util.XmlUtils;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import curam.util.dom.html2.HTMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Renderer used for the assessment results list. This creates an IFrame with
 * the source page set to be the value passed in from the server. The XML data
 * passed in should contain the following attributes:
 * <p>
 * <b>resultsURI</b>: The full URI of the page to display the results. E.g.
 * <code>AssessmentDelivery_viewResultsForInstancePage.do?assessmentInstanceID
 * =1817202449643995136</code> </br> <b>style</b>: The style to be applied to
 * the IFrame. E.g. <code>'width:100%;
 * height:500px;'</code>
 * </p>
 */
public class AssessmentResultsRenderer extends AbstractViewRenderer {

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final RendererContract rendererContract) throws ClientException,
    DataAccessException, PlugInException {

    final Path sourcePath = field.getBinding().getSourcePath();
    final String value = context.getDataAccessor().get(sourcePath);
    final Document myDoc = XmlUtils.parseXmlText(value);
    final Node rootNode = myDoc.getFirstChild();
    final String resultsURI =
      rootNode.getAttributes().getNamedItem("resultsURI").getNodeValue()
        + "&o3ctx=4096";
    final String style = "width:100%; height:100%";

    // Create the IFrame and set the source to be the results URI
    if (!StringHelper.isEmpty(resultsURI)) { // only if URI is given

      // add javascript for manipulating style in the iFrame
      context.includeScriptURIs("text/javascript",
        "../CDEJ/jscript/AssessmentResults.js");

      // Create the div for the iFrame
      final Element frameDiv =
        documentFragment.getOwnerDocument().createElement("div");

      final Element iFrame =
        documentFragment.getOwnerDocument().createElement("iFrame");
      iFrame.setAttribute("src", resultsURI);
      iFrame.setAttribute("id", "assessment-results-frame");
      iFrame.setAttribute("frameBorder", "0");
      iFrame.setAttribute("style", style);
      iFrame.setAttribute("onload", "resizeResultsIFrame()");

      // Add a comment so that the HTML DOM object gets created correctly
      HTMLUtils.appendComment(iFrame, "frame");

      frameDiv.appendChild(iFrame);
      documentFragment.appendChild(frameDiv);
    }
  }

}
