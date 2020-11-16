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
 * Copyright 2008 Curam Software Ltd.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Curam
 * Software, Ltd. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Curam Software.
 */
package citizenworkspace.renderer;

import citizenworkspace.util.SecurityUtils;
import curam.ieg.player.PlayerUtils;
import curam.util.client.BidiUtils;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.plugin.PlugInException;
import org.w3c.dom.DocumentFragment;

/**
 * Writes out rich text, without escaping the XML. Filters the content to
 * prevent XSS attacks.
 */
public class RichTextViewRenderer extends AbstractViewRenderer {

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(final Field field, final DocumentFragment fragment,
    final RendererContext context, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    final String desc =
      context.getDataAccessor().get(field.getBinding().getSourcePath());

    // Filter content to prevent XSS attacks
    final String filteredContent = SecurityUtils.filterACF(desc);

    if (!BidiUtils.isBidi()) {
      PlayerUtils
        .appendRichText(fragment, filteredContent, context, contract);
    } else {
      PlayerUtils.appendRichText(fragment,
        BidiUtils.addEmbedingUCC(filteredContent), context, contract);
    }
  }
}
