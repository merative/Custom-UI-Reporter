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
 * Copyright 2009-2011 Curam Software Ltd.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of Curam
 * Software, Ltd. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Curam Software.
 */
package progress;

import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.message.generated.RuntimeMessages;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import curam.util.resources.XMLParserCache;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Progress Icon Renderer. Renders an icon within the field. XML string should
 * take the following format: <root> <progress progressIconName=
 * "[name of icon reference in the application resource table]"/> </root>
 */
public class ProgressIconRenderer extends AbstractViewRenderer {

  /**
   * The name of xml attribute name used to store the icon name.
   */
  private static final String ICON_NAME = "iconName";

  /**
   * The name of xml attribute name used to store the tooltip value.
   */
  private static final String TOOLTIP = "tooltip";

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("restriction")
  public void render(final Field field, final DocumentFragment fragment,
    final RendererContext context, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    // Get the source path for the field
    final Path sourcePath = field.getBinding().getSourcePath();

    // Get the field value, the xml passed from the server
    final String xmlString = context.getDataAccessor().get(sourcePath);

    // If the field is blank, display nothing
    if (StringUtils.isEmpty(xmlString)) {
      return;
    }

    // Parse the xml string to produce a xml document
    final Document document = parseXmlText(xmlString);

    // Get the required values from the xml
    final Element progress = document.getDocumentElement();
    final String iconName = progress.getAttribute(ICON_NAME);
    final String tooltip = progress.getAttribute(TOOLTIP);

    // Get the owner document from the fragment
    final Document ownerDocument = fragment.getOwnerDocument();

    // Create the image and add it to the cell
    final Element image = ownerDocument.createElement("img");

    // load the image from the resource store
    final String imageReference =
      context.getApplicationResourceURI(iconName, getLocale());

    // Set the attributes for the image
    image.setAttribute("src", imageReference);
    image.setAttribute("alt", tooltip);
    image.setAttribute("title", tooltip);

    // add the image element to the document fragment
    fragment.appendChild(image);
  }

  /**
   * Parses XML text to create a DOM document.
   *
   * @param value
   * The XML text to be parsed.
   * @return The DOM document.
   * @throws DataAccessException
   * If the XML text cannot be parsed.
   */
  public static Document parseXmlText(final String value)
    throws DataAccessException {

    Document xmlDoc = null;

    try {
      final DocumentBuilder documentBuilder =
        XMLParserCache.getDocumentBuilder();
      final InputSource source = new InputSource();

      source.setCharacterStream(new StringReader(value));

      xmlDoc = documentBuilder.parse(source);

    } catch (final SAXException e) {
      throw new DataAccessException(RuntimeMessages.ERR_UNKNOWN, e);
    } catch (final IOException e) {
      throw new DataAccessException(RuntimeMessages.ERR_UNKNOWN, e);
    }
    return xmlDoc;
  }
}
