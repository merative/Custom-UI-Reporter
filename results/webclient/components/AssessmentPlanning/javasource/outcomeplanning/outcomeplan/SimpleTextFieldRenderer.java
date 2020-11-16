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
package outcomeplanning.outcomeplan;

import curam.util.client.ClientException;
import curam.util.client.domain.render.edit.AbstractEditRenderer;
import curam.util.client.model.ComponentBuilderFactory;
import curam.util.client.model.Field;
import curam.util.client.model.FieldBuilder;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.plugin.PlugInException;
import curam.util.dom.html2.HTMLUtils;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * Edit renderer for a simple text field.
 * 
 * @since 6.0
 */
public class SimpleTextFieldRenderer extends AbstractEditRenderer {

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final RendererContract arg3) throws ClientException, DataAccessException,
    PlugInException {

    final String domain =
      context.getDataAccessor().get(
        field.getBinding().getSourcePath().extendPath("@domain"));

    final String selected =
      context.getDataAccessor().get(
        field.getBinding().getSourcePath().extendPath("selection/@selected"));

    // Element div = documentFragment.getOwnerDocument().createElement("div");

    final FieldBuilder fBuilder =
      ComponentBuilderFactory.createFieldBuilder();
    fBuilder.setDomain(context.getDomain(domain));
    fBuilder.setID(field.getID());
    fBuilder.setTargetPath(field.getBinding().getTargetPath());

    final String targetPath =
      "choice/" + field.getID() + "/selected-options/";

    // Record the form item and get the correct ID to use.
    final String targetID =
      context.addFormItem(fBuilder.getComponent(), field.getID(), targetPath);

    final Element input =
      documentFragment.getOwnerDocument().createElement("input");
    input.setAttribute("id", targetID);
    input.setAttribute("name", targetID);
    input.setAttribute("value", selected);
    HTMLUtils.appendComment(input, "filler");
    // div.appendChild(input);
    documentFragment.appendChild(input);

  }

}
