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
 * Copyright 2009 Curam Software Ltd.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Curam
 * Software, Ltd. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Curam Software.
 */

package assessmentplanning.delivery;

import curam.client.util.StringHelper;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import curam.util.dom.html2.HTMLUtils;
import curam.util.type.Blob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Widget renderer for the Assessment Answers. Answers given for a particular
 * assessment instance are shown here.
 */
@SuppressWarnings("restriction")
public class AssessmentAnswersRenderer extends AbstractViewRenderer {

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final RendererContract rendererContract) throws ClientException,
    DataAccessException, PlugInException {

    final Document myDoc = getXMLDocument(field, context);
    final NodeList xmlList = myDoc.getChildNodes();
    final Node rootNode = xmlList.item(0);
    final String executionID =
      rootNode.getAttributes().getNamedItem("executionID").getNodeValue();

    if (!StringHelper.isEmpty(executionID)) { // only if an executionID is given

      // add javascript for manipulating style in the iFrame
      context.includeScriptURIs("text/javascript",
        "../CDEJ/jscript/AssessmentAnswers.js");

      // Create the div for the iFrame
      // create the iFrame to hold the answers script
      final Element frameDiv =
        documentFragment.getOwnerDocument().createElement("div");
      frameDiv.setAttribute("id", "answers-cluster");

      final Element iFrame =
        documentFragment.getOwnerDocument().createElement("iFrame");
      final String playerFullPath =
        "../ieg/Screening.do?executionID=" + executionID;
      iFrame.setAttribute("src", playerFullPath);
      iFrame.setAttribute("id", "answers-frame");
      iFrame.setAttribute("class", "answers-frame");
      iFrame.setAttribute("frameBorder", "0");
      iFrame.setAttribute("onload", "changeFrameStyle()");
      // "window.frames[0].document.getElementById('doc4').id='answers-frame-doc4'");

      // Add a comment so that the html DOM object gets created correctly
      HTMLUtils.appendComment(iFrame, "frame");

      frameDiv.appendChild(iFrame);
      documentFragment.appendChild(frameDiv);
    }
  }

  /**
   * Gets the xml document containing the data from the server.
   * 
   * @param field
   * The field
   * @param context
   * The renderer context
   * @return The xml document containing the data from the server
   * @throws DataAccessException
   */
  private Document getXMLDocument(final Field field,
    final RendererContext context) throws DataAccessException {

    final Blob value;
    final Path sourcePath;

    sourcePath = field.getBinding().getSourcePath();
    // get the xml
    value = (Blob) context.getDataAccessor().getRaw(sourcePath);

    Document myDoc = null;

    try {
      final ByteArrayInputStream bais =
        new ByteArrayInputStream(value.copyBytes());

      final ObjectInputStream ois = new ObjectInputStream(bais);

      myDoc = (Document) ois.readObject();

      ois.close();

    } catch (final IOException e) {
      throw new RuntimeException(e);
    } catch (final ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return myDoc;
  }
}
