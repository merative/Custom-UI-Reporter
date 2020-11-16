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
package citizenworkspace.renderer.eligibilityresults;

import citizenworkspace.renderer.RendererUtils;
import citizenworkspace.renderer.eligibilityresults.helper.EligibilityResultsHelper;
import citizenworkspace.util.XmlTools;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.plugin.PlugInException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

/**
 * Renderer class for rendering the context.
 */
public class ContextRenderer extends AbstractViewRenderer {

  @Override
  public void render(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final RendererContract contract) throws ClientException,
    DataAccessException, PlugInException {

    final Document document =
      XmlTools.getRendererFieldDocument(field, context);

    documentFragment.appendChild(EligibilityResultsHelper.createComponent(
      documentFragment, context, contract,
      "cw-eligibility-results-context-panel", document));
    RendererUtils.fixEmptyHtmlElements(documentFragment);
  }
}
