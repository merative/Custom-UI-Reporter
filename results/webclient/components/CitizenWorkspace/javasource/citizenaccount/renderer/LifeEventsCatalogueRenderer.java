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
 * This class is for rendering the life events catalogue which consists of life
 * events not associated
 * with any category and life events associated with category.
 */
public class LifeEventsCatalogueRenderer extends AbstractViewRenderer {

  private static final String ROOT_LIFE_EVENT_TYPE =
    "/root/life-event-type[]/";

  private static final String LIFE_EVENT_CATEGORY =
    "/root/life-event-category[]/";

  private static final String LIFE_EVENT_TYPE = "/life-event-type[]/";

  private static final String DESCRIPTION = "description";

  private static final String TITLE = "title";

  private static final String IMAGES_BLANK_GIF = "../Images/blank.gif";

  private static final String IMAGE = "image";

  private static final String ID = "id";

  @Override
  public void render(final Field field, final DocumentFragment doc,
    final RendererContext rendererContext, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    // Render the life events that are not in any category at the top of the
    // list
    renderLifeEventTypesWithoutCategoryOnTop(field, doc, rendererContext,
      contract);

    final Path categoriesPath =
      field.getBinding().getSourcePath().extendPath(LIFE_EVENT_CATEGORY);

    final int numCategories =
      rendererContext.getDataAccessor().count(categoriesPath);

    final Element contentDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    contentDiv.setAttribute(HTMLConsts.CLASS, "content");
    doc.appendChild(contentDiv);

    for (int i = 1; i <= numCategories; i++) {

      final Path categoryPath = categoriesPath.applyIndex(0, i, true);

      final String name =
        rendererContext.getDataAccessor().get(categoryPath.extendPath(TITLE));
      // Get the icon URI
      final String imageName =
        rendererContext.getDataAccessor().get(categoryPath.extendPath(IMAGE));
      String imageURI;
      if (StringHelper.isEmpty(imageName)) {
        imageURI = IMAGES_BLANK_GIF;
      } else {
        imageURI =
          rendererContext.getApplicationResourceURI(imageName, getLocale());
      }

      final Element rowDiv =
        doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      rowDiv.setAttribute(HTMLConsts.CLASS, "category-wrapper");

      final Element iconDiv =
        doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      iconDiv.setAttribute(HTMLConsts.CLASS, "icon-div");

      final Element iconEle =
        doc.getOwnerDocument().createElement(HTMLConsts.IMG_TAG);
      iconEle.setAttribute(HTMLConsts.SRC_TAG, imageURI);

      // appending
      iconDiv.appendChild(iconEle);
      rowDiv.appendChild(iconDiv);

      final Element titleAndDescDiv =
        doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      titleAndDescDiv.setAttribute(HTMLConsts.CLASS, "name-desc-div");
      rowDiv.appendChild(titleAndDescDiv);

      // the desc and all the nested life event types go into this div. It can
      // be expanded/
      // collapsed.
      final Element divToHide =
        doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      divToHide.setAttribute(HTMLConsts.ID, "category" + i);
      divToHide.setAttribute(HTMLConsts.CLASS, "le-catalogue-type-div");

      final Element h3Ele = doc.getOwnerDocument().createElement("H3");
      h3Ele.setAttribute(HTMLConsts.ID, "header" + i);
      h3Ele.setAttribute("onclick", "toggleDiv('header" + i + "', '"
        + "category" + i + "');");

      final Element nameSpan =
        doc.getOwnerDocument().createElement(HTMLConsts.SPAN_TAG);
      nameSpan.setAttribute(HTMLConsts.CLASS, "le-catalogue-category-title ");

      contentDiv.appendChild(rowDiv);
      nameSpan.setTextContent(name);
      h3Ele.appendChild(nameSpan);
      titleAndDescDiv.appendChild(h3Ele);

      final Element descSpan =
        doc.getOwnerDocument().createElement(HTMLConsts.SPAN_TAG);

      descSpan.setAttribute("class", "le-description");

      final DocumentFragment fragment =
        doc.getOwnerDocument().createDocumentFragment();
      final FieldBuilder fieldBuilder =
        ComponentBuilderFactory.createFieldBuilder();
      fieldBuilder.copy(field);
      fieldBuilder.setSourcePath(categoryPath.extendPath(DESCRIPTION));
      new RichTextViewRenderer().render(fieldBuilder.getComponent(),
        fragment, rendererContext, contract);
      descSpan.appendChild(fragment);

      divToHide.appendChild(descSpan);
      titleAndDescDiv.appendChild(divToHide);

      final Element lineBreak = doc.getOwnerDocument().createElement("BR");

      lineBreak.setAttribute(HTMLConsts.CLASS, "clear-both");
      divToHide.appendChild(lineBreak);

      final Path lifeEventTypesPath =
        categoryPath.extendPath(LIFE_EVENT_TYPE);

      final int numTypes =
        rendererContext.getDataAccessor().count(lifeEventTypesPath);

      for (int j = 1; j <= numTypes; j++) {

        // for the life event types within a category.

        final Path typePath = lifeEventTypesPath.applyIndex(0, j, true);

        final String typeName =
          rendererContext.getDataAccessor().get(typePath.extendPath(TITLE));
        final String typeID =
          rendererContext.getDataAccessor().get(typePath.extendPath(ID));
        // Get the icon URI
        final String typeImage =
          rendererContext.getDataAccessor().get(typePath.extendPath(IMAGE));
        String typeImageURI;
        if (StringHelper.isEmpty(typeImage)) {
          typeImageURI = IMAGES_BLANK_GIF;
        } else {
          typeImageURI =
            rendererContext.getApplicationResourceURI(typeImage, getLocale());
        }

        final Element iconDiv2 =
          doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
        iconDiv2.setAttribute(HTMLConsts.CLASS, "icon-div");

        final Element iconEle2 =
          doc.getOwnerDocument().createElement(HTMLConsts.IMG_TAG);
        iconEle2.setAttribute(HTMLConsts.SRC_TAG, typeImageURI);

        iconDiv2.appendChild(iconEle2);

        final Element aLineBreak = doc.getOwnerDocument().createElement("BR");
        aLineBreak.setAttribute(HTMLConsts.CLASS, "clear-both");
        divToHide.appendChild(aLineBreak);

        final Element typeDiv =
          doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

        final Element typeNameDiv =
          doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

        final Element viewLink =
          doc.getOwnerDocument().createElement(HTMLConsts.A_TAG);
        viewLink.setAttribute(HTMLConsts.HREF_TAG,
          "CitizenAccount_viewLifeEventPage.do?lifeEventContextID=" + typeID);
        viewLink.setTextContent(typeName);

        typeNameDiv.appendChild(viewLink);
        typeNameDiv.setAttribute(HTMLConsts.CLASS, "le-catalogue-type-name");

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
        builder.setSourcePath(typePath.extendPath(DESCRIPTION));

        new RichTextViewRenderer().render(builder.getComponent(), frag,
          rendererContext, contract);
        descDiv.appendChild(frag);

        typeDiv.appendChild(iconDiv2);

        typeDiv.appendChild(typeNameDiv);

        typeDiv.appendChild(descDiv);
        typeDiv.setAttribute(HTMLConsts.CLASS, "le-catalogue-type-div");

        divToHide.appendChild(typeDiv);
      }

    }

  }

  /**
   * For rendering the LifeEvent Types not associated with any category.
   * 
   * @param field The field info
   * @param doc The Document Fragment
   * @param rendererContext Renderer Context
   * @param contract Renderer Contract
   * @throws DataAccessException Generic Exception Signature
   * @throws ClientException Generic Exception Signature
   * @throws PlugInException Generic Exception Signature
   */
  public void renderLifeEventTypesWithoutCategoryOnTop(final Field field,
    final DocumentFragment doc, final RendererContext rendererContext,
    final RendererContract contract) throws DataAccessException,
    ClientException, PlugInException {

    final Path rootLifeEventTypesPath =
      field.getBinding().getSourcePath().extendPath(ROOT_LIFE_EVENT_TYPE);
    final int lifeEventTypenumTypes =
      rendererContext.getDataAccessor().count(rootLifeEventTypesPath);

    final Element contentDiv1 =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    contentDiv1.setAttribute(HTMLConsts.CLASS, "content");
    doc.appendChild(contentDiv1);

    for (int j = 1; j <= lifeEventTypenumTypes; j++) {

      final Element rowDiv =
        doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

      final Path lifeEventPath =
        rootLifeEventTypesPath.applyIndex(0, j, true);

      final String name =
        rendererContext.getDataAccessor()
          .get(lifeEventPath.extendPath(TITLE));
      final String id =
        rendererContext.getDataAccessor().get(lifeEventPath.extendPath(ID));
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

      // Add a blank icon to left-hand side to pad the list item
      final Element blankIconDiv =
        doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      blankIconDiv.setAttribute(HTMLConsts.CLASS, "icon-div");
      final Element blankIconEle =
        doc.getOwnerDocument().createElement(HTMLConsts.IMG_TAG);
      blankIconEle.setAttribute(HTMLConsts.SRC_TAG, IMAGES_BLANK_GIF);
      blankIconDiv.appendChild(blankIconEle);
      rowDiv.appendChild(blankIconDiv);

      // Add the real icon (may be blank)
      final Element iconDiv =
        doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      iconDiv.setAttribute(HTMLConsts.CLASS, "icon-div");
      final Element iconEle =
        doc.getOwnerDocument().createElement(HTMLConsts.IMG_TAG);
      iconEle.setAttribute(HTMLConsts.SRC_TAG, imageURI);
      iconDiv.appendChild(iconEle);

      final Element titleAndDescDiv =
        doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      titleAndDescDiv.setAttribute(HTMLConsts.CLASS, "le-pop-name-desc-div");
      rowDiv.appendChild(titleAndDescDiv);

      // Add the icon div to the title/description div to get the correct layout
      titleAndDescDiv.appendChild(iconDiv);

      final Element nameSpan = doc.getOwnerDocument().createElement("H3");
      final Element linkEle =
        doc.getOwnerDocument().createElement(HTMLConsts.A_TAG);
      linkEle.setAttribute(HTMLConsts.HREF_TAG,
        "CitizenAccount_viewLifeEventPage.do?lifeEventContextID=" + id);
      linkEle.setTextContent(name);
      nameSpan.appendChild(linkEle);
      nameSpan.setAttribute(HTMLConsts.CLASS, "title-header");

      contentDiv1.appendChild(rowDiv);

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
      contentDiv1.appendChild(lineBreak);
    }
  }
}
