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
import citizenworkspace.renderer.CommunityServicesListRenderer;
import citizenworkspace.renderer.GoogleMapsViewServicesByTypeRenderer;
import citizenworkspace.renderer.GovernmentServicesInformationRenderer;
import citizenworkspace.renderer.TriageResultsContainerRenderer;
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
import curam.util.common.util.JavaScriptEscaper;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * Renderer class for rendering the life events recommendations.
 */
public class LifeEventsRecommenationsRenderer extends AbstractViewRenderer {

  /**
   * The zoom warning div ID param.
   */
  public static final String ZOOM_WARNING_DIV_ID = "zoom-warning_div";

  /**
   * The properties file name param.
   */
  public static final String PROPERTIES_FILE_NAME = "ServiceProviders";

  @Override
  public void render(final Field field, final DocumentFragment fragment,
    final RendererContext context, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    TriageResultsContainerRenderer.addZoomWarning(fragment, context);

    final Path rootPath =
      field.getBinding().getSourcePath().extendPath("root");

    final Path infoPath = rootPath.applyIndex(0, 1, true);

    final Path servicesPath = infoPath.extendPath("services-root/");

    final Path mapPath = infoPath.extendPath("map/");

    final Path govServicesPath = infoPath.extendPath("gov-root/");

    // make the sessionID available to javascript processing
    final Path sessionIDPath = infoPath.extendPath("session-id/");

    final String sessionID = context.getDataAccessor().get(sessionIDPath);

    final Path lifeEventIDPath = infoPath.extendPath("life-event-id/");

    final String lifeEventID = context.getDataAccessor().get(lifeEventIDPath);

    final StringBuffer scriptText = new StringBuffer();

    scriptText.append("var sessionID = '"
      + JavaScriptEscaper.escapeText(sessionID) + "';");

    scriptText.append("var lifeEventID = '"
      + JavaScriptEscaper.escapeText(lifeEventID) + "';");

    context.includeScripts("text/javascript", scriptText.toString());

    context.includeScriptURIs("text/javascript",
      "../jscript/citizenworkspace/display-hide-div.js");

    TriageResultsContainerRenderer.addZoomWarning(fragment, context);

    // create a div to give the shadow effect
    final Element shadowDivEle =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

    shadowDivEle.setAttribute(HTMLConsts.CLASS, "recommendations-shadow");

    // create a wrapper div that will contain the 3 sections

    final Element wrapperDivEle =
      fragment.getOwnerDocument().createElement(HTMLConsts.DIV_TAG);

    wrapperDivEle.setAttribute(HTMLConsts.CLASS, "recommenations-wrapper");

    fragment.appendChild(wrapperDivEle);

    final DocumentFragment subFragment =
      fragment.getOwnerDocument().createDocumentFragment();

    final FieldBuilder builder = ComponentBuilderFactory.createFieldBuilder();

    // render the map
    final DocumentFragment mapFrag =
      fragment.getOwnerDocument().createDocumentFragment();

    builder.setSourcePath(mapPath);

    new GoogleMapsViewServicesByTypeRenderer().render(builder.getComponent(),
      mapFrag, context, contract);

    wrapperDivEle.appendChild(mapFrag);

    // render the list panel
    final DocumentFragment listFrag =
      fragment.getOwnerDocument().createDocumentFragment();

    builder.copy(field);

    builder.setSourcePath(servicesPath);

    new CommunityServicesListRenderer().render(builder.getComponent(),
      listFrag, context, contract);

    wrapperDivEle.appendChild(listFrag);

    // render the gov services panel
    final DocumentFragment govServicesFrag =
      fragment.getOwnerDocument().createDocumentFragment();

    builder.setSourcePath(govServicesPath);

    new GovernmentServicesInformationRenderer().render(
      builder.getComponent(), govServicesFrag, context, contract);

    wrapperDivEle.appendChild(govServicesFrag);
  }
}
