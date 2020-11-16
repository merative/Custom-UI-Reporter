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

import citizenaccount.renderer.RendererUtil;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.plugin.PlugInException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * Renderer class for rendering the Default Standard User Home Page.
 */
public class DefaultStandardUserHomePageRenderer extends AbstractViewRenderer {

  @Override
  public void render(final Field arg0,
    final DocumentFragment documentFragment, final RendererContext arg2,
    final RendererContract arg3) throws ClientException, DataAccessException,
    PlugInException {

    final Document parentDocument = documentFragment.getOwnerDocument();

    final Element div = parentDocument.createElement("div");
    RendererUtil.attachWaiAriaAttr(div, "HomePage");
    div.setTextContent("Standard User Default Home Page");

    documentFragment.appendChild(div);

  }

}
