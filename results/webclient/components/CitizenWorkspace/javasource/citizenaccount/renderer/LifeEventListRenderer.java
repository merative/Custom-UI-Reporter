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
package citizenaccount.renderer;

import citizenworkspace.pageplayer.HTMLConsts;
import citizenworkspace.util.StringHelper;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * To render the life events based on popularity and then based on categories.
 * Remaining life events will be listed under general category.
 * 
 * @since 6.0.5
 */
public class LifeEventListRenderer extends AbstractViewRenderer {

  private static final String LIFE_EVENT_CATEGORY =
    "/root/life-event-category[]/";

  private static final String LIFE_EVENT_TYPE = "/life-event-type[]/";

  private static final String DESCRIPTION = "description";

  private static final String TITLE = "title";

  private static final String IMAGE = "image";

  private static final String ID = "id";

  @Override
  public void render(final Field field, final DocumentFragment doc,
    final RendererContext rendererContext, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    final Path categoriesPath =
      field.getBinding().getSourcePath().extendPath(LIFE_EVENT_CATEGORY);
    final int numCategories =
      rendererContext.getDataAccessor().count(categoriesPath);

    final Element contentDiv =
      createDIVTag(doc, "main-content", "main-content",
        "margin: 0px 0px 0px 0px; padding: 0px 0px 0px 0px;");
    doc.appendChild(contentDiv);

    for (int i = 1; i <= numCategories; i++) {

      final Path categoryPath = categoriesPath.applyIndex(0, i, true);
      final String name =
        rendererContext.getDataAccessor().get(categoryPath.extendPath(TITLE));

      final Element rowDiv = createDIVTag(doc, "category-wrapper-div ");

      final Element titleAndIconDiv =
        createDIVTag(doc, "category-title-icon ");

      // the desc and all the nested life event types go into this div. It can
      // be expanded/
      // collapsed.
      final Element divToHide =
        createDIVTag(doc, "category-details-hide ", "category" + i);

      final Element h3Ele =
        createH3Tag(doc, "category-h3 ", "header" + i, "Category");

      final Element h3Div =
        doc.getOwnerDocument().createElement(HTMLConsts.SPAN_TAG);
      h3Div.setAttribute(HTMLConsts.CLASS, "category-h3-span ");

      final String imageName =
        rendererContext.getDataAccessor().get(categoryPath.extendPath(IMAGE));
      String imageURI;
      if (StringHelper.isEmpty(imageName)) {
        imageURI = CitizenAccountConst.IMAGES_BLANK_GIF;
      } else {
        imageURI =
          rendererContext.getApplicationResourceURI(imageName, getLocale());
      }
      final Element imgDivTag = createDIVTag(doc, "nomargin-div ");

      final Element imgTag = createIMGTag(doc, imageURI, name);
      imgTag.setAttribute(HTMLConsts.CLASS, "category-image ");

      final Element inputTag = createInputTag(doc, name);

      // BEGIN, SSK

      imgTag.setAttribute(HTMLConsts.TITLE, name);
      // END, SSK

      inputTag.setAttribute("title", name + " arrow");

      imgDivTag.appendChild(imgTag);
      imgDivTag.appendChild(inputTag);
      h3Ele.appendChild(imgDivTag);

      final Element nameSpan =
        doc.getOwnerDocument().createElement(HTMLConsts.SPAN_TAG);
      nameSpan.setAttribute(HTMLConsts.CLASS, "category-title ");
      nameSpan.setTextContent(name);

      final Element grouptoggleArrowSpan =
        doc.getOwnerDocument().createElement(HTMLConsts.SPAN_TAG);
      grouptoggleArrowSpan.setAttribute(HTMLConsts.CLASS,
        "CitizenWorkspace-grouptoggleArrow");
      grouptoggleArrowSpan.setAttribute(HTMLConsts.TITLE, "Toggle");
      grouptoggleArrowSpan.setAttribute(HTMLConsts.EVENT_ONCLICK,
        "toggleDiv('header" + i + "', '" + "category" + i + "');");
      grouptoggleArrowSpan.setTextContent("\u00a0");

      final Element hiddenSpan =
        doc.getOwnerDocument().createElement(HTMLConsts.SPAN_TAG);
      hiddenSpan.setAttribute(HTMLConsts.CLASS, "hidden");
      hiddenSpan.setAttribute(HTMLConsts.TAB_INDEX, "0");
      hiddenSpan.setTextContent("Toggle Button");

      grouptoggleArrowSpan.appendChild(hiddenSpan);

      // RendererUtil.getImageDiv(doc,rendererContext, nameSpan,
      // categoryPath.extendPath(IMAGE), getLocale());
      h3Ele.appendChild(nameSpan);
      h3Ele.appendChild(grouptoggleArrowSpan);
      titleAndIconDiv.appendChild(h3Ele);
      rowDiv.appendChild(titleAndIconDiv);
      contentDiv.appendChild(rowDiv);

      final Element descSpan =
        doc.getOwnerDocument().createElement(HTMLConsts.SPAN_TAG);
      descSpan.setAttribute(HTMLConsts.CLASS, "category-description ");
      RendererUtil.richTextRenderer(field, doc, rendererContext, contract,
        descSpan, categoryPath.extendPath(DESCRIPTION));

      divToHide.appendChild(descSpan);
      titleAndIconDiv.appendChild(divToHide);

      /*
       * final Element lineBreak = doc.getOwnerDocument().createElement("BR");
       * lineBreak.setAttribute(HTMLConsts.CLASS, "clear-both");
       * divToHide.appendChild(lineBreak);
       */

      final Path lifeEventTypesPath =
        categoryPath.extendPath(LIFE_EVENT_TYPE);
      final int numTypes =
        rendererContext.getDataAccessor().count(lifeEventTypesPath);

      for (int j = 1; j <= numTypes; j++) {

        final Path typePath = lifeEventTypesPath.applyIndex(0, j, true);
        final String typeName =
          rendererContext.getDataAccessor().get(typePath.extendPath(TITLE));
        final String typeID =
          rendererContext.getDataAccessor().get(typePath.extendPath(ID));

        /*
         * final Element aLineBreak =
         * doc.getOwnerDocument().createElement("BR");
         * aLineBreak.setAttribute(HTMLConsts.CLASS, "clear-both");
         * divToHide.appendChild(aLineBreak);
         */

        final Element typeDiv =
          doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
        // typeDiv.setAttribute(HTMLConsts.STYLE,
        // "margin: 0px 0px 0px 0px; padding: 20px 0px 0px 0px;");
        typeDiv.setAttribute(HTMLConsts.CLASS, "life-event-wrapper ");
        // RendererUtil.getImageDiv(doc,rendererContext,typeDiv,
        // typePath.extendPath(IMAGE), getLocale());

        final Element divTag =
          doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
        divTag.setAttribute(HTMLConsts.CLASS, "icon-div");
        final String iconName =
          rendererContext.getDataAccessor().get(typePath.extendPath(IMAGE));
        if (StringHelper.isEmpty(iconName)) {
          imageURI = CitizenAccountConst.IMAGES_BLANK_GIF;
          divTag.setAttribute(HTMLConsts.STYLE,
            "width: 62px; height: 51px; margin-top: 0px;");
        } else {
          imageURI =
            rendererContext.getApplicationResourceURI(iconName, getLocale());
          divTag.setAttribute(HTMLConsts.STYLE,
            "width: auto; margin-top: 0px;");
        }

        final Element imageTag = createIMGTag(doc, imageURI, typeName);
        imageTag.setAttribute(HTMLConsts.ALT_TAG, "");
        divTag.appendChild(imageTag);
        typeDiv.appendChild(divTag);

        final Element viewLink =
          doc.getOwnerDocument().createElement(HTMLConsts.A_TAG);
        viewLink.setAttribute(HTMLConsts.CLASS, "life-event-link ");
        viewLink.setAttribute(HTMLConsts.TITLE, typeName);
        viewLink.setAttribute(HTMLConsts.ALT_TAG, typeName);
        viewLink.setAttribute(HTMLConsts.HREF_TAG,
          "CitizenAccount_viewLifeEventLaunchPage.do?o3ctx=1048576&lifeEventContextID="
            + typeID);
        viewLink.setTextContent(typeName);

        final Element typeNameDiv =
          doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
        typeNameDiv.appendChild(viewLink);
        typeNameDiv.setAttribute(HTMLConsts.CLASS, "life-event-name-div ");
        typeDiv.appendChild(typeNameDiv);

        final Element descDiv =
          doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
        descDiv.setAttribute(HTMLConsts.CLASS, "life-event-desc-div");

        RendererUtil.richTextRenderer(field, doc, rendererContext, contract,
          descDiv, typePath.extendPath(DESCRIPTION));
        typeDiv.appendChild(descDiv);

        divToHide.appendChild(typeDiv);
      }
    }
  }

  /*
   * At the moment, I only create a few private methods to avoid check style
   * failing, while the best way, in terms of reuse, is that we create a helper
   * class, and all of the renders only use the methods provided in that class
   * to create elements.
   */
  private Element
    createInputTag(final DocumentFragment doc, final String name) {

    final Element inputTag =
      doc.getOwnerDocument().createElement(HTMLConsts.INPUT_TAG);
    inputTag.setAttribute(HTMLConsts.CLASS, "hidden");
    inputTag.setAttribute("type", "button");
    inputTag.setAttribute(HTMLConsts.ROLE, "button");
    inputTag.setAttribute("title", name + " arrow");

    return inputTag;
  }

  private Element createIMGTag(final DocumentFragment doc,
    final String imageURI, final String name) {

    final Element imgTag =
      doc.getOwnerDocument().createElement(HTMLConsts.IMG_TAG);
    imgTag.setAttribute(HTMLConsts.SRC_TAG, imageURI);
    imgTag.setAttribute(HTMLConsts.ALT_TAG, name);

    return imgTag;
  }

  private Element createDIVTag(final DocumentFragment doc,
    final String className, final String id, final String style) {

    final Element divTag =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    divTag.setAttribute(HTMLConsts.CLASS, className);
    divTag.setAttribute(HTMLConsts.ID, id);
    divTag.setAttribute(HTMLConsts.STYLE, style);

    return divTag;
  }

  private Element createDIVTag(final DocumentFragment doc,
    final String className) {

    final Element divTag =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    divTag.setAttribute(HTMLConsts.CLASS, className);

    return divTag;
  }

  private Element createDIVTag(final DocumentFragment doc,
    final String className, final String id) {

    final Element divTag =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    divTag.setAttribute(HTMLConsts.CLASS, className);
    divTag.setAttribute(HTMLConsts.ID, id);

    return divTag;
  }

  private Element createH3Tag(final DocumentFragment doc,
    final String className, final String id, final String ariaLabel) {

    final Element h3Tag = doc.getOwnerDocument().createElement("H3");
    h3Tag.setAttribute(HTMLConsts.ID, id);
    h3Tag.setAttribute(HTMLConsts.CLASS, className);
    h3Tag.setAttribute(HTMLConsts.ROLE, HTMLConsts.REGION);
    h3Tag.setAttribute(HTMLConsts.ARIA_LABEL, ariaLabel);

    return h3Tag;
  }
}
