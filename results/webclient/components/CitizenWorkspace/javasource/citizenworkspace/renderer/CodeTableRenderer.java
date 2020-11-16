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

import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.ComponentBuilderFactory;
import curam.util.client.model.ComponentParameters;
import curam.util.client.model.Field;
import curam.util.client.model.FieldBuilder;
import curam.util.client.path.util.ClientPaths;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.domain.DomainException;
import curam.util.common.path.DataAccessException;
import curam.util.common.plugin.PlugInException;
import java.util.HashSet;
import java.util.Set;
import org.w3c.dom.DocumentFragment;

/**
 * Renders a code table value. The parameter "table" must be set to the
 * code table name, otherwise an exception is thrown.
 */
public class CodeTableRenderer extends AbstractViewRenderer {

  private static final Set<String> reservedParams = new HashSet<String>();

  static {
    reservedParams.add("table");
    reservedParams.add("child-domain");
  }

  /**
   * Render a code table value.
   * 
   * @param field the field.
   * @param fragment the document fragment.
   * @param context the renderer context.
   * @param contract the renderer contract.
   * 
   * @throws ClientException.
   * @throws DataAccessException.
   * @throws PlugInException.
   */
  @Override
  public void render(final Field field, final DocumentFragment fragment,
    final RendererContext context, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    final FieldBuilder fBuilder =
      ComponentBuilderFactory.createFieldBuilder();

    final ComponentParameters params = field.getParameters();
    final String codeTableValue =
      context.getDataAccessor().get(field.getBinding().getSourcePath());
    final String codeTableName = params.get("table");
    final String childDomain = params.get("child-domain");

    final String codeTablePath =
      ClientPaths.CODE_TABLE_PATH.extendPath(
        codeTableName + "[" + codeTableValue + "]").toString();

    try {
      fBuilder.setDomain(context.getDomain(childDomain));
    } catch (final DomainException e) {
      throw new ClientException(ClientException.ERR_UNKNOWN, getClass()
        .getName(), e);
    }
    fBuilder.setSourcePath(codeTablePath);

    for (final String key : params.keySet()) {
      if (reservedParams.contains(key)) {
        continue;
      }
      fBuilder.setParameter(key, params.get(key));
    }
    context.render(fBuilder.getComponent(), fragment, contract);
  }

}
