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
 * Software, Ltd. ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Curam Software.
 */
package citizenaccount.renderer;

import citizenworkspace.pageplayer.HTMLConsts;
import curam.ieg.player.PlayerUtils;
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
 * This renderer renders the case details associated to a citizen.
 */
public class CitizenCaseListRenderer extends AbstractViewRenderer {

  private final String kPropertyFileName = "CitizenAccountContactInformation";

  private final String kAltTextExtension = ".alttext";

  @Override
  public void render(final Field field, final DocumentFragment fragment,
    final RendererContext context, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    /**
     * Create the case List
     */
    final Element divEle =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
    fragment.appendChild(divEle);

    final Path caseworkerDetailsPath =
      field.getBinding().getSourcePath()
        .extendPath("/caseworkers/case-worker-details/case-worker-detail[]");

    // number of cases
    final int numCases =
      context.getDataAccessor().count(caseworkerDetailsPath);

    /**
     * Render the list of cases
     */
    for (int i = 1; i <= numCases; i++) {
      final Element caseListDiv =
        fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      caseListDiv.setAttribute(HTMLConsts.CLASS, "caseList");
      divEle.appendChild(caseListDiv);

      final Path casePath = caseworkerDetailsPath.applyIndex(0, i, true);

      final String caseTitle =
        context.getDataAccessor().get(
          casePath.extendPath("case-name-reference"));
      final String caseworkerName =
        context.getDataAccessor().get(casePath.extendPath("name"));

      // add case title element
      final Element caseTitleDiv =
        fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      caseTitleDiv.setAttribute(HTMLConsts.CLASS, "caseTitle");
      caseTitleDiv.setTextContent(caseTitle);
      caseListDiv.appendChild(caseTitleDiv);

      // add case worker name element
      final Element caseworkerNameDiv =
        fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
      caseworkerNameDiv.setAttribute(HTMLConsts.CLASS, "caseworkerName");
      caseworkerNameDiv.setTextContent(caseworkerName);
      caseListDiv.appendChild(caseworkerNameDiv);

      // get number of contact details
      final Path contactDetailsPath =
        casePath.extendPath("/details/detail[]");

      final int numContactModes =
        context.getDataAccessor().count(contactDetailsPath);

      // Loop through each contact mode and construct XML elements
      for (int j = 1; j <= numContactModes; j++) {
        final Element detailsDiv =
          fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);
        detailsDiv.setAttribute(HTMLConsts.CLASS, "caseListEntry");
        final Path contactPath = contactDetailsPath.applyIndex(0, j, true);

        final String image =
          context.getDataAccessor().get(contactPath.extendPath("image"));
        final String imageAltText =
          context.getDataAccessor().get(
            contactPath.extendPath("image/@altText"));
        final String altText =
          PlayerUtils.getProperty(kPropertyFileName, imageAltText
            + kAltTextExtension, context);
        final String contactValue =
          context.getDataAccessor().get(contactPath.extendPath("value"));
        final Element contactImageSpan =
          fragment.getOwnerDocument().createElement(HTMLConsts.SPAN_TAG);
        contactImageSpan.setAttribute(HTMLConsts.CLASS,
          "caseContactImageEntry");
        final Element imageEle =
          fragment.getOwnerDocument().createElement(HTMLConsts.IMG_TAG);
        imageEle.setAttribute(HTMLConsts.SRC_TAG,
          context.getApplicationResourceURI(image, getLocale()));
        imageEle.setAttribute(HTMLConsts.ALT_TAG, altText);
        imageEle.setAttribute(HTMLConsts.TITLE, altText);
        imageEle.setAttribute("align", "absmiddle");
        contactImageSpan.appendChild(imageEle);
        detailsDiv.appendChild(contactImageSpan);
        final Element contactValueSpan =
          fragment.getOwnerDocument().createElement(HTMLConsts.SPAN_TAG);

        if (PlayerUtils.getProperty(kPropertyFileName,
          "caseworker.emailaddress.icon", context).equals(image)) {
          final Element anchor =
            fragment.getOwnerDocument().createElement(HTMLConsts.A_TAG);
          anchor.setAttribute(HTMLConsts.HREF_TAG, "mailto:" + contactValue);
          anchor.setTextContent(contactValue);
          contactValueSpan.appendChild(anchor);
        } else {
          contactValueSpan.setTextContent(contactValue);
        }
        contactValueSpan.setAttribute(HTMLConsts.TITLE, altText);
        detailsDiv.appendChild(contactValueSpan);
        caseListDiv.appendChild(detailsDiv);
      }
    }
  }
}
