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
import citizenworkspace.renderer.eligibilityresults.helper.LaunchApplyOnlineHelper;
import citizenworkspace.util.RendererHelper;
import citizenworkspace.util.RendererModel;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.plugin.PlugInException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

/**
 * Renders to render an iframe for a result action. The facade xml contains
 * motivationID, ProgramType reference and UIM page name.
 * 
 */
public class ResultActionIframeRenderer extends AbstractViewRenderer {

  @Override
  public void render(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final RendererContract contract) throws ClientException,
    DataAccessException, PlugInException {

    final RendererModel model =
      new RendererModel(this, field, documentFragment, context, contract);

    final RendererHelper<Node> helper = new LaunchApplyOnlineHelper();
    documentFragment.appendChild(helper.render(context, model));
    RendererUtils.fixEmptyHtmlElements(documentFragment);
  }
}
