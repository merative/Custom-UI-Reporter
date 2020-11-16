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
package citizenworkspace.renderer;

import citizenaccount.renderer.RendererUtil;
import citizenworkspace.util.SecurityUtils;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.plugin.PlugInException;
import curam.util.dom.html2.HTMLUtils;
import javax.xml.transform.Result;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * Writes out rich text, without escaping the XML, and sets the height to be the
 * specified height set in properties.
 */
public class CWViewRichTextRenderer extends AbstractViewRenderer {

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(final Field field, final DocumentFragment fragment,
    final RendererContext context, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    final String desc =
      context.getDataAccessor().get(field.getBinding().getSourcePath());
    final Element wrapper = fragment.getOwnerDocument().createElement("div");

    final String iframeHeight = "100";

    wrapper.setAttribute("class", "rich-text");
    wrapper.setAttribute("height", iframeHeight + "px");
    RendererUtil.attachWaiAriaAttr(wrapper, "RichText");
    fragment.appendChild(wrapper);

    wrapper.appendChild(fragment.getOwnerDocument()
      .createProcessingInstruction(Result.PI_DISABLE_OUTPUT_ESCAPING, ""));
    // Filter content to prevent XSS attacks
    final String filteredContent = SecurityUtils.filterACF(desc);
    HTMLUtils.appendText(wrapper, filteredContent);
    wrapper.appendChild(fragment.getOwnerDocument()
      .createProcessingInstruction(Result.PI_ENABLE_OUTPUT_ESCAPING, ""));
  }
}
