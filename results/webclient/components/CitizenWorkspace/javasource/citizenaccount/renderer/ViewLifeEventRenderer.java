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
import citizenworkspace.util.StringHelper;
import curam.ieg.player.PlayerUtils;
import curam.util.client.BidiUtils;
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
 * Renderer class for rendering the View Life Events page.
 * 
 * @author christopher.coughlan
 * 
 */
public class ViewLifeEventRenderer extends AbstractViewRenderer {

  private static final String LIFE_EVENT = "life-event/";

  private static final String LIFE_EVENT_TYPE_NAME = "life-event-type-name";

  private static final String IMAGE = "image";

  private static final String START_URL = "start-url";

  private static final String ADDITIONAL_INFO = "additional-info";

  private static final String IMAGES_BLANK_GIF = "../Images/blank.gif";

  private static final String LAUNCH_DESCRIPTION = "launch-description";

  private static final String DEFAULT_LAUNCH_DESCRIPTION =
    "default-launch-description";

  private static final String HAS_QUESTION_SCRIPT = "has-question-script";

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(final Field field, final DocumentFragment doc,
    final RendererContext rendererContext, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    final Path lifeEventsPath =
      field.getBinding().getSourcePath().extendPath(LIFE_EVENT);

    final Path lifeEventPath = lifeEventsPath.applyIndex(0, 1, true);

    final String url =
      rendererContext.getDataAccessor().get(
        lifeEventPath.extendPath(START_URL));
    final String lifeEventTypeName =
      rendererContext.getDataAccessor().get(
        lifeEventPath.extendPath(LIFE_EVENT_TYPE_NAME));

    final String launchDescription =
      rendererContext.getDataAccessor().get(
        lifeEventPath.extendPath(LAUNCH_DESCRIPTION));

    // Get the button text from the properties file
    String buttonText = null;

    if (StringHelper.isEmpty(launchDescription)) {
      // CHECKSTYLE_OFF: IllegalCatch
      try {
        buttonText =
          PlayerUtils.getProperty(lifeEventTypeName, "launch.button.text",
            rendererContext);
      } catch (final RuntimeException ex) {
        if (ex.getCause() instanceof curam.util.common.path.DataAccessException) {
          // Properties file not found. Do nothing and let the 'if' statement
          // below handle it.
        } else {
          throw ex; // Must be a RuntimeException for a different reason
        }
      }
      // CHECKSTYLE_ON: IllegalCatch

      // Fall back on the default value at this stage.
      if (buttonText == null || StringHelper.isEmpty(buttonText)) {
        buttonText =
          rendererContext.getDataAccessor().get(
            lifeEventPath.extendPath(DEFAULT_LAUNCH_DESCRIPTION));
      }
    } else {
      buttonText = launchDescription;
    }

    final Element iconDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

    iconDiv.setAttribute(HTMLConsts.CLASS, "icon-div");

    final Element blankImgEle =
      doc.getOwnerDocument().createElement(HTMLConsts.IMG_TAG);

    blankImgEle.setAttribute(HTMLConsts.SRC_TAG, "../Images/blank.gif");

    iconDiv.appendChild(blankImgEle);

    doc.appendChild(iconDiv);

    final Element titleAndDescDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

    titleAndDescDiv.setAttribute(HTMLConsts.CLASS, "name-desc-div");

    doc.appendChild(titleAndDescDiv);

    // We only add launch button if the LE has a question script
    final String hasQuestionScriptStr =
      rendererContext.getDataAccessor().get(
        lifeEventPath.extendPath(HAS_QUESTION_SCRIPT));
    final boolean hasQuestionScript =
      new Boolean(hasQuestionScriptStr).booleanValue();
    if (hasQuestionScript) {
      final Element linkEle =
        doc.getOwnerDocument().createElement(HTMLConsts.A_TAG);
      linkEle.setAttribute(HTMLConsts.CLASS, "button");
      linkEle.setAttribute(HTMLConsts.HREF_TAG, url);
      titleAndDescDiv.appendChild(linkEle);
      final Element buttonTextEle =
        doc.getOwnerDocument().createElement(HTMLConsts.SPAN_TAG);
      if (!BidiUtils.isBidi()) {
        buttonTextEle.setTextContent(buttonText);
      } else {
        buttonTextEle.setTextContent(BidiUtils.addEmbedingUCC(buttonText));
      }
      linkEle.appendChild(buttonTextEle);
    }

    // new row
    final Element lineBreak = doc.getOwnerDocument().createElement("BR");

    lineBreak.setAttribute(HTMLConsts.CLASS, "clear-both");

    doc.appendChild(lineBreak);

    // Get the icon name
    final String imageName =
      rendererContext.getDataAccessor().get(lifeEventPath.extendPath(IMAGE));
    String imageURI;
    if (StringHelper.isEmpty(imageName)) {
      imageURI = IMAGES_BLANK_GIF;
    } else {
      imageURI =
        rendererContext.getApplicationResourceURI(imageName, getLocale());
    }

    final Element leIconDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

    leIconDiv.setAttribute(HTMLConsts.CLASS, "icon-div");

    final Element iconEle =
      doc.getOwnerDocument().createElement(HTMLConsts.IMG_TAG);

    iconEle.setAttribute(HTMLConsts.SRC_TAG, imageURI);

    leIconDiv.appendChild(iconEle);

    doc.appendChild(leIconDiv);

    final Element moreInfoDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

    moreInfoDiv.setAttribute(HTMLConsts.CLASS, "name-desc-div");

    doc.appendChild(moreInfoDiv);

    // Call to the CEF provided rich text renderer
    final DocumentFragment frag =
      doc.getOwnerDocument().createDocumentFragment();

    final FieldBuilder builder = ComponentBuilderFactory.createFieldBuilder();

    builder.copy(field);

    builder.setSourcePath(lifeEventPath.extendPath(ADDITIONAL_INFO));

    RendererUtil.richTextRenderer(builder.getComponent(), moreInfoDiv,
      rendererContext, contract);
    // new RichTextViewRenderer().render(builder.getComponent(), frag,
    // rendererContext, contract);

    moreInfoDiv.appendChild(frag);
  }
}
