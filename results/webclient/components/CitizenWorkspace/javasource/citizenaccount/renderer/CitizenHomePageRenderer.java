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
 * Copyright 2010 Curam Software Ltd.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Curam
 * Software, Ltd. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Curam Software.
 */
package citizenaccount.renderer;

import citizenworkspace.pageplayer.HTMLConsts;
import citizenworkspace.util.XmlTools;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.ComponentBuilderFactory;
import curam.util.client.model.Field;
import curam.util.client.model.FieldBuilder;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.message.generated.RuntimeMessages;
import curam.util.common.path.DataAccessException;
import curam.util.common.plugin.PlugInException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This is the parent renderer for the Citizen Account home page. This delegates
 * to other renderers to render specific components on the home page.
 * 
 */
public class CitizenHomePageRenderer extends AbstractViewRenderer {

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(final Field field, final DocumentFragment fragment,
    final RendererContext context, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    // Welcome-Panel, added to top level node, no impact on layout
    /*
     * fragment.appendChild(constructPanel(field, fragment, context, contract,
     * CitizenAccountConst.kCitizenHomeWelcomePanelExtendPath,
     * CitizenAccountConst.kCitizenHomeWelcomePanelStyleConfig));
     */

    // Get the owner document
    final Document parentDocument = fragment.getOwnerDocument();

    // Gum-2054/2401 Find Element "RenderCampaigns" and populate boolean based
    // on its values. This
    // element is created in CitizenHomeImpl.java based on user properties.
    boolean displayCampaigns = true; // Outreach Campaigns
    final Document d1 = XmlTools.getRendererFieldDocument(field, context);
    try {
      final String xPathExpression = "/citizenhome/Render/RenderCampaigns";
      final Node n = XmlTools.getXMLNode(xPathExpression, d1);
      if (n != null) {
        displayCampaigns = new Boolean(n.getTextContent()).booleanValue();
      }
    } catch (final XPathExpressionException ex) {
      throw new ClientException(RuntimeMessages.ERR_RENDER_FAILED, ex);
    }

    final Element parentElement =
      parentDocument.createElement(HTMLConsts.DIV_TAG);
    parentElement.setAttribute(HTMLConsts.CLASS, "newparent");

    final Element leftSideDiv =
      parentDocument.createElement(HTMLConsts.DIV_TAG);
    leftSideDiv.setAttribute(HTMLConsts.CLASS, "newleft");

    final Element centreDiv =
      parentDocument.createElement(HTMLConsts.DIV_TAG);
    centreDiv.setAttribute(HTMLConsts.ID, "centrediv");

    // Select CSS class for centre depending on visibility of other panels
    if (displayCampaigns) {
      centreDiv.setAttribute(HTMLConsts.CLASS, "newcentre_noleft");
    } else {
      centreDiv
        .setAttribute(HTMLConsts.CLASS, "newcentre_noleft_nocampaigns");
    }

    final Element rightSideDiv =
      parentDocument.createElement(HTMLConsts.DIV_TAG);
    if (displayCampaigns) {
      rightSideDiv.setAttribute(HTMLConsts.CLASS, "newright");
    } else {
      rightSideDiv.setAttribute(HTMLConsts.CLASS, "newright_nocampaigns");
    }

    parentElement.appendChild(rightSideDiv);
    parentElement.appendChild(centreDiv);

    centreDiv.appendChild(constructPanel(field, fragment, context, contract,
      CitizenAccountConst.kCitizenHomeWelcomePanelExtendPath,
      CitizenAccountConst.kCitizenHomeWelcomePanelStyleConfig));

    // Message Panel
    centreDiv.appendChild(constructPanel(field, fragment, context, contract,
      CitizenAccountConst.kCitizenHomeMessagePanelExtendPath,
      CitizenAccountConst.kCitizenHomeMessagePanelStyleConfig));

    if (displayCampaigns) {
      // campaign panel (right-hand side)
      rightSideDiv.appendChild(constructPanel(field, fragment, context,
        contract, CitizenAccountConst.kCitizenHomeCampaignPanelExtendPath,
        CitizenAccountConst.kCitizenHomeCampaignPanelStyleConfig));
    }

    fragment.appendChild(parentElement);
  }

  /**
   * Constructs a panel for a given extended path and style. This method
   * internally calls the corresponding renderer to construct XML.
   * 
   * @param field
   * contains an instance of Field
   * @param fragment
   * contains instance of DocumentFragment
   * @param context
   * contains instance of RendererContext
   * @param contract
   * contains instance of RendererContract
   * @param extendedPath
   * contains extend path of a panel to be used to set as source path
   * for the target renderer
   * @param panelStyle
   * contains style which is configured in styles config. This helps to
   * call the corresponding renderer.
   * @throws PlugInException
   * @throws ClientException
   * @throws DataAccessException
   */
  private DocumentFragment constructPanel(final Field field,
    final DocumentFragment fragment, final RendererContext context,
    final RendererContract contract, final String extendedPath,
    final String panelStyle) throws PlugInException, ClientException,
    DataAccessException {

    final FieldBuilder fieldBuilder =
      ComponentBuilderFactory.createFieldBuilder();

    final DocumentFragment documentFragment =
      fragment.getOwnerDocument().createDocumentFragment();

    fieldBuilder.setSourcePath(field.getBinding().getSourcePath()
      .extendPath(extendedPath));

    fieldBuilder.setStyle(context.getStyle(panelStyle));
    context.render(fieldBuilder.getComponent(), documentFragment, contract);

    return documentFragment;
  }
}
