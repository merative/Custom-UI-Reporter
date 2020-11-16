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
import citizenworkspace.util.XmlTools;
import curam.ieg.player.PlayerUtils;
import curam.util.client.BidiUtils;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import curam.util.dom.html2.HTMLUtils;
import curam.util.resources.StringUtil;
import javax.xml.transform.Result;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * To render the life event details screen before the launch.
 * 
 * @since 6.0.5
 */
public class ViewLifeEventLaunchRenderer extends AbstractViewRenderer {

  private static final String LIFE_EVENT = "life-event/";

  private static final String LIFE_EVENT_TYPE_NAME = "life-event-type-name";

  private static final String START_URL = "start-url";

  private static final String ADDITIONAL_INFO = "additional-info";

  private static final String LAUNCH_DESCRIPTION = "launch-description";

  private static final String DEFAULT_LAUNCH_DESCRIPTION =
    "default-launch-description";

  private static final String HAS_QUESTION_SCRIPT = "has-question-script";

  private static final String HAS_LIFE_EVENT_CASE = "has-life-event-case";

  private static final String LIFE_EVENT_ID = "life-event-id";

  private static final String VIEW_LIFE_EVENT_PROPERTIES_FILE =
    "ViewLifeEvent";

  private static final String SESSION_ID = "session-id";

  private static final String HAS_RULESET = "has-ruleset";

  private static final String IS_GENERIC_RESULT_PAGE =
    "is-generic-result-page";

  private static final String MOTIVATION_ID = "motivation-id";

  private static final String SUBMISSION_TEXT = "submission-text";

  private static final String FINISHED_LIFE_EVENT = "finished-life-event";

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
    final boolean finishedLifeEvent =
      Boolean.parseBoolean(rendererContext.getDataAccessor().get(
        lifeEventPath.extendPath(FINISHED_LIFE_EVENT)));

    // Get the button text from the properties file
    final String buttonText =
      setButtonText(rendererContext, lifeEventPath, lifeEventTypeName,
        launchDescription);

    if (finishedLifeEvent) {
      final Element submissionMsgEle =
        buildMessageElement(field, doc, rendererContext, contract,
          lifeEventPath);
      doc.appendChild(submissionMsgEle);
    }

    final Element contentDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    contentDiv.setAttribute(HTMLConsts.CLASS, "lifeeventoneui name-desc-div");
    contentDiv.setAttribute(HTMLConsts.STYLE,
      "width:99%;margin:20px 25px 0px 25px;");
    doc.appendChild(contentDiv);

    final String htmlDirection =
      PlayerUtils.getHtmlPresentationDirection(getLocale());
    final boolean isRTL = "rtl".equals(htmlDirection);

    final Element titleAndDescDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    titleAndDescDiv.setAttribute(HTMLConsts.CLASS, "name-desc-div");
    titleAndDescDiv.setAttribute(HTMLConsts.STYLE, isRTL
      ? "width:60%;float:right;margin:0px 0px 0px 0px;"
      : "width:60%;float:left;margin:0px 0px 0px 0px;");
    contentDiv.appendChild(titleAndDescDiv);

    final Element titleInfoDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    titleInfoDiv.setAttribute(HTMLConsts.CLASS, "le-catalogue-type-div");
    titleAndDescDiv.appendChild(titleInfoDiv);
    RendererUtil.richTextRenderer(field, doc, rendererContext, contract,
      titleInfoDiv, lifeEventPath.extendPath("name"));

    final Element descDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    descDiv.setAttribute(HTMLConsts.CLASS, "name-desc-div-apply-online");
    descDiv
      .setAttribute(
        HTMLConsts.STYLE,
        isRTL
          ? "float:right; margin:20px 0px 0px 0px; font-family: Arial; font-size: 18px; color: #222222;"
          : "float:left; margin:20px 0px 0px 0px; font-family: Arial; font-size: 18px; color: #222222;");
    titleAndDescDiv.appendChild(descDiv);
    RendererUtil.richTextRenderer(field, doc, rendererContext, contract,
      descDiv, lifeEventPath.extendPath(ADDITIONAL_INFO));

    final Element buttonDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    buttonDiv.setAttribute(HTMLConsts.CLASS,
      "name-desc-div-application-forms");
    buttonDiv.setAttribute(HTMLConsts.STYLE, isRTL
      ? "height: auto; width:30%; float:right; margin:30px 40px 0px 0px;"
      : "height: auto; width:30%; float:left; margin:30px 0px 0px 40px;");
    buttonDiv.appendChild(doc.getOwnerDocument().createComment(" ")); // ensure
                                                                      // child
    contentDiv.appendChild(buttonDiv);

    // We only add launch button if the LE has a question script and life event
    // case
    final String hasQuestionScriptStr =
      rendererContext.getDataAccessor().get(
        lifeEventPath.extendPath(HAS_QUESTION_SCRIPT));
    final boolean hasQuestionScript =
      new Boolean(hasQuestionScriptStr).booleanValue();

    final String hasLifeEventCaseStr =
      rendererContext.getDataAccessor().get(
        lifeEventPath.extendPath(HAS_LIFE_EVENT_CASE));
    final boolean hasLifeEventCase =
      new Boolean(hasLifeEventCaseStr).booleanValue();

    if (hasQuestionScript && hasLifeEventCase) {

      final Element linkElement = doc.getOwnerDocument().createElement("a");
      linkElement.setAttribute("onclick", "window.location.href='" + url
        + "'");
      linkElement.setAttribute("type", "button");
      linkElement.setAttribute("data-dojo-type", "dijit.form.Button");
      linkElement.setTextContent(buttonText);
      if (BidiUtils.isBidi()) {
        BidiUtils.setTextDirForElement(linkElement);
      }
      contentDiv.appendChild(linkElement);

    }
  }

  private String setButtonText(final RendererContext rendererContext,
    final Path lifeEventPath, final String lifeEventTypeName,
    final String launchDescription) throws DataAccessException {

    String buttonText = new String();

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

    return buttonText;
  }

  /**
   * To build the HTML element to render the submitted life event message text.
   * 
   * @param field
   * Field
   * @param doc
   * DocumentFragment
   * @param rendererContext
   * RendererContext
   * @param contract
   * RendererContract
   * @param path
   * Path
   * @return HTML element
   * @throws ClientException
   * Exception
   * @throws DataAccessException
   * Exception
   * @throws PlugInException
   * Exception
   */
  @SuppressWarnings("deprecation")
  private Element buildMessageElement(final Field field,
    final DocumentFragment doc, final RendererContext rendererContext,
    final RendererContract contract, final Path path) throws ClientException,
    DataAccessException, PlugInException {

    final Element buildMessage =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    buildMessage.setAttribute(HTMLConsts.ID, "msgWrapper");
    buildMessage.setAttribute(HTMLConsts.CLASS, "msgWrapper");
    final Element iconDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    iconDiv.setAttribute(HTMLConsts.ID, "msgIcon");
    iconDiv.setAttribute(HTMLConsts.CLASS, "msgIcon");
    final Element titleAndDescDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    titleAndDescDiv.setAttribute(HTMLConsts.ID, "msgTitleAndDesc");
    titleAndDescDiv.setAttribute(HTMLConsts.CLASS, "msgTitleAndDesc");
    final Element titleDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    titleDiv.setAttribute(HTMLConsts.ID, "msgTitle");
    titleDiv.setAttribute(HTMLConsts.CLASS, "msgTitle");
    final Element descDiv =
      doc.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    descDiv.setAttribute(HTMLConsts.ID, "msgDesc");
    descDiv.setAttribute(HTMLConsts.CLASS, "msgDesc");
    buildMessage.appendChild(iconDiv);
    buildMessage.appendChild(titleAndDescDiv);
    titleAndDescDiv.appendChild(titleDiv);
    titleAndDescDiv.appendChild(descDiv);

    final String lifeEventID =
      rendererContext.getDataAccessor().get(path.extendPath(LIFE_EVENT_ID));
    final String sessionID =
      rendererContext.getDataAccessor().get(path.extendPath(SESSION_ID));
    final String motivationID =
      rendererContext.getDataAccessor().get(path.extendPath(MOTIVATION_ID));
    final String submissionMsgText =
      rendererContext.getDataAccessor().get(path.extendPath(SUBMISSION_TEXT));

    // If the Life Event has a rule set add a link to the recommendations page
    final boolean hasRuleset =
      Boolean.parseBoolean(rendererContext.getDataAccessor().get(
        path.extendPath(HAS_RULESET)));

    String linkTextStart =
      PlayerUtils.getProperty(VIEW_LIFE_EVENT_PROPERTIES_FILE,
        "Services.And.Programs.Start.Text", rendererContext);
    final String linkText =
      PlayerUtils.getProperty(VIEW_LIFE_EVENT_PROPERTIES_FILE,
        "Services.And.Programs.Default.Link.Text", rendererContext);
    final String defaultTextHeader =
      PlayerUtils.getProperty(VIEW_LIFE_EVENT_PROPERTIES_FILE,
        "Services.And.Programs.Default.Text", rendererContext);
    final boolean isBidi = BidiUtils.isBidi();

    if (!StringUtil.isNullOrEmpty(submissionMsgText)) {
      linkTextStart = submissionMsgText;
    } else if (!hasRuleset) {
      titleDiv.setTextContent(!isBidi ? defaultTextHeader : BidiUtils
        .addEmbedingUCC(defaultTextHeader));
      linkTextStart = "";
    } else {
      titleDiv.setTextContent(!isBidi ? defaultTextHeader : BidiUtils
        .addEmbedingUCC(defaultTextHeader));
    }

    descDiv.appendChild(doc.getOwnerDocument().createProcessingInstruction(
      Result.PI_DISABLE_OUTPUT_ESCAPING, ""));
    if (hasRuleset) {
      // If the Life Event has motivation rules set open the results page
      final boolean isGenericResultPage =
        Boolean.parseBoolean(rendererContext.getDataAccessor().get(
          path.extendPath(IS_GENERIC_RESULT_PAGE)));

      // Need to build a link (<a href="...) but this link won't be added to the
      // page DOM, it will be added to the additional text
      final Document linkDoc = XmlTools.createNewDocument();
      final Element linkEle = linkDoc.createElement(HTMLConsts.A_TAG);
      linkEle.setAttribute(HTMLConsts.ID, "msgLink");
      linkEle.setAttribute(HTMLConsts.CLASS, "msgLink");

      if (isGenericResultPage) {
        linkEle.setAttribute(HTMLConsts.HREF_TAG, "#");
        linkEle
          .setAttribute(
            "onclick",
            "window.parent.displayContent({pageID:'CitizenWorkspace_eligibilityResults', displayNavBar: false ,param: [{paramKey:\"motivationID\", paramValue:\""
              + motivationID + "\"}]})");
      } else {
        linkEle.setAttribute(HTMLConsts.HREF_TAG,
          "CitizenAccount_viewRecommendationsPage.do?o3ctx=1048576&lifeEventID="
            + lifeEventID + "&id=" + sessionID);
      }

      linkEle.setTextContent(!isBidi ? linkText : BidiUtils
        .addEmbedingUCC(linkText));
      linkDoc.appendChild(linkEle);

      if (!isBidi) {
        HTMLUtils.appendText(descDiv,
          linkTextStart + " " + XmlTools.convertDocumentToText(linkDoc));
      } else {
        HTMLUtils.appendText(
          descDiv,
          BidiUtils.addEmbedingUCC(linkTextStart + " "
            + XmlTools.convertDocumentToText(linkDoc)));
      }
      RendererUtil.richTextRenderer(field, doc, rendererContext, contract,
        descDiv, path);
    } else {
      HTMLUtils.appendText(descDiv,
        !isBidi ? linkTextStart : BidiUtils.addEmbedingUCC(linkTextStart));
      RendererUtil.richTextRenderer(field, doc, rendererContext, contract,
        descDiv, path);
    }
    descDiv.appendChild(doc.getOwnerDocument().createProcessingInstruction(
      Result.PI_ENABLE_OUTPUT_ESCAPING, ""));

    final Element imageEle =
      doc.getOwnerDocument().createElement(HTMLConsts.IMG_TAG);
    imageEle.setAttribute(HTMLConsts.CLASS, "image");
    imageEle.setAttribute(HTMLConsts.SRC_TAG, "../Images/my_updates.png");
    iconDiv.appendChild(imageEle);

    return buildMessage;
  }
}
