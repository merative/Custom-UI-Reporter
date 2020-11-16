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
 * Copyright 2010 Curam Software Ltd.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Curam
 * Software, Ltd. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Curam Software.
 */
package assessmentplanning.delivery;

import curam.util.client.ClientException;
import curam.util.client.domain.convert.LocalizedMessageConverter;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import curam.util.type.Blob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Widget renderer for the guidance in relation assessed factors.
 */
public class AssessmentGraphColorsRenderer extends AbstractViewRenderer {

  /**
   * String constant for Client. Used to reference the client. <br>
   * <br>
   * Value = 'Client'.
   */
  // move to properties
  public String kCLIENT = "Client";

  /**
   * String constant for color. Used to reference the graph color. <br>
   * <br>
   * Value = 'color'.
   */
  public String kCOLOR = "color";

  /**
   * String constant for hideClientResults. Used to indicate if client results
   * should be hidden. <br>
   * <br>
   * Value = 'hideClientResults'.
   */
  public String kHIDETITLETEXT = "hideClientResults";

  /**
   * String constant for displayClientResults. Used to indicate if client
   * results should be displayed. <br>
   * <br>
   * Value = 'displayClientResults'.
   */
  public String kDISPLAYTITLETEXT = "displayClientResults";

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final RendererContract rendererContract) throws ClientException,
    DataAccessException, PlugInException {

    final Path sourcePath = field.getBinding().getSourcePath();
    final Blob value = (Blob) context.getDataAccessor().getRaw(sourcePath);

    Document document = null;

    try {
      final ByteArrayInputStream bais =
        new ByteArrayInputStream(value.copyBytes());
      final ObjectInputStream ois = new ObjectInputStream(bais);
      document = (Document) ois.readObject();

      ois.close();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    } catch (final ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    // Get the required values from the xml

    if (document.hasChildNodes()) {
      final Element clientColorElement = document.getDocumentElement();

      final Node clientNode = clientColorElement.getFirstChild();
      final String client =
        clientNode.getAttributes().getNamedItem(kCLIENT).getTextContent();
      final String color =
        clientNode.getAttributes().getNamedItem(kCOLOR).getTextContent();
      final String displayTitleText =
        clientNode.getAttributes().getNamedItem(kDISPLAYTITLETEXT)
          .getTextContent();
      final String hideTitleText =
        clientNode.getAttributes().getNamedItem(kHIDETITLETEXT)
          .getTextContent();

      final Element hiddenInputField =
        createElement(documentFragment, "input");
      hiddenInputField.setAttribute("type", "hidden");
      hiddenInputField.setAttribute("colorToUse", "#" + color);
      hiddenInputField.setAttribute("id", client);
      hiddenInputField.setAttribute("name", "colorRow");
      documentFragment.appendChild(hiddenInputField);

      final LocalizedMessageConverter formatthis =
        new LocalizedMessageConverter();

      // this div represents the square that is the legend item
      final Element div = createElement(documentFragment, "div");
      div.setAttribute("class", "assessment-legend-item");
      div.setAttribute("style", "background-color:" + "#" + color);
      div.setAttribute("title", formatthis.format(hideTitleText));
      div.setAttribute("displayTitle", "false");
      div.setAttribute("displayTitleText",
        formatthis.format(displayTitleText));
      div.setAttribute("hideTitleText", formatthis.format(hideTitleText));
      documentFragment.appendChild(div);

    } else {
      return;
    }

  }

  /**
   * Creates an {@link Element} of the passed type for the passed in
   * {@link DocumentFragment}.
   * 
   * @param documentFragment
   * The document fragment the {@link Element} is to be created for
   * @param elementType
   * The type of {@link Element} to be created
   * @return The created {@link Element}
   */
  private Element createElement(final DocumentFragment documentFragment,
    final String elementType) {

    final Element element =
      documentFragment.getOwnerDocument().createElement(elementType);

    return element;
  }
}
