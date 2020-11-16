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
package citizenaccount.renderer;

import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.ComponentBuilderFactory;
import curam.util.client.model.Field;
import curam.util.client.model.FieldBuilder;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.message.generated.RuntimeMessages;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import curam.util.resources.XMLParserCache;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Renderer class for rendering the My Activities Provider fields.
 */
public class MyActivitiesProviderFieldRenderer extends AbstractViewRenderer {

  @Override
  public void render(final Field field,
    final DocumentFragment documentFragment,
    final RendererContext rendererContext,
    final RendererContract rendererContract) throws ClientException,
    DataAccessException, PlugInException {

    // Get the owner document
    final Document parentDocument = documentFragment.getOwnerDocument();

    // determine if a provider is specified
    final Path itemPath = field.getBinding().getSourcePath();

    final boolean providerNameSpecified =
      rendererContext.getDataAccessor().count(
        itemPath.extendPath("/provider/@name")) != 0;

    final boolean providerLinkSpecified =
      rendererContext.getDataAccessor().count(
        itemPath.extendPath("/provider/@hasLink")) != 0;

    // if not, do nothing
    if (!providerNameSpecified) {
      return;
    }

    // if a name and no link is to be rendered, just render the name
    if (providerNameSpecified && !providerLinkSpecified) {
      renderName(rendererContext, itemPath, parentDocument, documentFragment);
    } else if (providerNameSpecified && providerLinkSpecified) {
      try {
        renderNameAndLink(rendererContext, itemPath, rendererContract,
          parentDocument, documentFragment);
      } catch (final TransformerException e) {
        Logger.getAnonymousLogger().log(Level.WARNING, e.getMessage(), e);
      }
    }
  }

  private void renderName(final RendererContext rendererContext,
    final Path itemPath, final Document parentDocument,
    final DocumentFragment documentFragment) throws DataAccessException {

    final String providerName =
      rendererContext.getDataAccessor().get(
        itemPath.extendPath("/provider/@name"));
    final Element divEle = parentDocument.createElement("div");
    divEle.setTextContent(providerName);
    documentFragment.appendChild(divEle);
  }

  private void renderNameAndLink(final RendererContext rendererContext,
    final Path itemPath, final RendererContract rendererContract,
    final Document parentDocument, final DocumentFragment documentFragment)
    throws ClientException, DataAccessException, PlugInException,
    TransformerException {

    /**
     * Delegate to the Link renderer to render the link, it has everything it
     * needs in the <link> element.
     */
    final FieldBuilder fieldBuilder =
      ComponentBuilderFactory.createFieldBuilder();

    fieldBuilder.setSourcePath(itemPath.extendPath("/provider"));
    fieldBuilder.setStyle(rendererContext.getStyle("link"));

    // This variable is used to hold document fragment returned from the
    // link renderer
    final DocumentFragment linkFragment =
      parentDocument.createDocumentFragment();

    // Invoking the LinkRenderer
    rendererContext.render(fieldBuilder.getComponent(), linkFragment,
      rendererContract.createSubcontract());

    final Node campaignItemNode =
      convertToNode(getStringFromDocumentFragment(linkFragment),
        documentFragment);

    documentFragment.appendChild(campaignItemNode);
  }

  // TODO these were duplicated from the CitizenHomeCampaignPanelRenderer
  // class and need to be externalised
  private Node convertToNode(final String campaignItemText,
    final DocumentFragment documentFragment) throws ClientException {

    Node fragmentNode = null;
    final DocumentBuilder builder = XMLParserCache.getDocumentBuilder();

    try {
      fragmentNode =
        builder.parse(new InputSource(new StringReader(campaignItemText)))
          .getDocumentElement();
      final Document doc = documentFragment.getOwnerDocument();
      fragmentNode = doc.importNode(fragmentNode, true);

    } catch (final SAXException e) {
      throw new ClientException(RuntimeMessages.ERR_RENDER_FAILED, e);
    } catch (final IOException e) {
      throw new ClientException(RuntimeMessages.ERR_RENDER_FAILED, e);
    }
    return fragmentNode;
  }

  private String getStringFromDocumentFragment(
    final DocumentFragment documentFragment) throws TransformerException {

    final DOMSource domSource = new DOMSource(documentFragment);
    final StringWriter writer = new StringWriter();
    final StreamResult result = new StreamResult(writer);
    final TransformerFactory tf = TransformerFactory.newInstance();
    final Transformer transformer = tf.newTransformer();

    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.transform(domSource, result);

    return writer.toString();
  }
}
