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
 * Copyright 2010 - 2011 Curam Software Ltd.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Curam
 * Software, Ltd. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Curam Software.
 */
package citizenaccount.renderer;

import citizenworkspace.pageplayer.HTMLConsts;
import citizenworkspace.renderer.RichTextViewRenderer;
import citizenworkspace.util.StringHelper;
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
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * Renders the list of popular Life Events in Citizen Account.
 */
public class PopularLifeEventsRenderer extends AbstractViewRenderer {

  private static final String IMAGES_BLANK_GIF = "../Images/blank.gif";

  private static final String LIFE_EVENT = "/life-events/life-event[]/";

  private static final String CONTEXT_ID = "context-id";

  private static final String NAME = "name";

  private static final String IMAGE = "image";

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(final Field field, final DocumentFragment doc,
    final RendererContext rendererContext, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    final Path lifeEventsPath =
      field.getBinding().getSourcePath().extendPath(LIFE_EVENT);

    final int numLifeEvents =
      rendererContext.getDataAccessor().count(lifeEventsPath);

    final Element contentDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

    contentDiv.setAttribute(HTMLConsts.CLASS, "content");

    doc.appendChild(contentDiv);

    for (int i = 1; i <= numLifeEvents; i++) {

      final Path lifeEventPath = lifeEventsPath.applyIndex(0, i, true);

      final String name =
        rendererContext.getDataAccessor().get(lifeEventPath.extendPath(NAME));
      final String id =
        rendererContext.getDataAccessor().get(
          lifeEventPath.extendPath(CONTEXT_ID));
      final String imageName =
        rendererContext.getDataAccessor()
          .get(lifeEventPath.extendPath(IMAGE));
      String imageURI;
      if (StringHelper.isEmpty(imageName)) {
        imageURI = IMAGES_BLANK_GIF;
      } else {
        imageURI =
          rendererContext.getApplicationResourceURI(imageName, getLocale());
      }

      final Element rowDiv =
        doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

      final Element iconDiv =
        doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

      iconDiv.setAttribute(HTMLConsts.CLASS, "icon-div");

      final Element iconEle =
        doc.getOwnerDocument().createElement(HTMLConsts.IMG_TAG);
      iconEle.setAttribute(HTMLConsts.SRC_TAG, imageURI);

      iconEle.setAttribute(HTMLConsts.ALT_TAG, name);

      iconDiv.appendChild(iconEle);

      rowDiv.appendChild(iconDiv);

      final Element titleAndDescDiv =
        doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

      titleAndDescDiv.setAttribute(HTMLConsts.CLASS, "pop-name-desc-div");

      rowDiv.appendChild(titleAndDescDiv);

      final Element nameSpan = doc.getOwnerDocument().createElement("H3");

      final Element linkEle =
        doc.getOwnerDocument().createElement(HTMLConsts.A_TAG);

      linkEle.setAttribute(HTMLConsts.HREF_TAG,
        "CitizenAccount_viewLifeEventPage.do?lifeEventContextID=" + id);

      linkEle.setTextContent(name);

      nameSpan.appendChild(linkEle);

      nameSpan.setAttribute(HTMLConsts.CLASS, "title-header");

      contentDiv.appendChild(rowDiv);

      // nameSpan.setTextContent(name);

      titleAndDescDiv.appendChild(nameSpan);

      final Element descDiv =
        doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

      descDiv.setAttribute(HTMLConsts.CLASS, "name-desc-div");

      doc.appendChild(descDiv);

      // Call to the CEF provided rich text renderer
      final DocumentFragment frag =
        doc.getOwnerDocument().createDocumentFragment();

      final FieldBuilder builder =
        ComponentBuilderFactory.createFieldBuilder();

      builder.copy(field);

      builder.setSourcePath(lifeEventPath.extendPath("description"));

      new RichTextViewRenderer().render(builder.getComponent(), frag,
        rendererContext, contract);

      descDiv.appendChild(frag);

      titleAndDescDiv.appendChild(descDiv);

      final Element lineBreak = doc.getOwnerDocument().createElement("BR");

      lineBreak.setAttribute(HTMLConsts.CLASS, "clear-both");

      contentDiv.appendChild(lineBreak);
    }
  }
}
