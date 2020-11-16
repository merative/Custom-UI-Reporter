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

import citizenworkspace.layout.PageParser;
import citizenworkspace.util.ComponentIterator;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Component;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.DefaultStep;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.DocumentFragment;

/**
 * Parses and renders content based on a <code>ComponentIterator</code> object
 * set as the user object for the field. If this user object is not set, this
 * renderer will not function.
 */
public class FieldComponentRenderer extends AbstractViewRenderer {

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(final Field field, final DocumentFragment fragment,
    final RendererContext context, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    final ComponentIterator iter;
    final Path source;

    if (field.getUserObject() == null
      || !(field.getUserObject() instanceof ComponentIterator)) {
      // A user object of type ComponentIterator must be included.
      throw new ClientException(ClientException.ERR_UNKNOWN, getClass()
        .getName());
    }

    iter = (ComponentIterator) field.getUserObject();

    source = field.getBinding().getSourcePath();

    // Find the last non-empty predicate
    final int numSteps = source.size();
    int numPredicates;
    Object predicate = null;
    final List<String> indices = new ArrayList<String>(2);

    for (int i = 0; i < numSteps; i++) {
      final DefaultStep step = (DefaultStep) source.getStep(i);

      numPredicates = step.size();

      for (int j = 0; j < numPredicates; j++) {
        predicate = step.getPredicate(j);

        if (!"".equals(predicate)) {
          try {
            // Only apply predicates that are auto-incremented by lists.
            Integer.parseInt(predicate.toString());
            indices.add(predicate.toString());
          } catch (final NumberFormatException e) { // Do nothing with this
                                                    // exception,
            // it is only used to check if the
            // predicate is an integer.
          }
        }
      }
    }

    iter.applyIndex(indices);

    // CHECKSTYLE_OFF: IllegalCatch
    try {
      final List<Component> components =
        PageParser.parseLayout(field, iter.getNode(), fragment, context,
          contract, iter.getServerInterface(), true);

      for (final Component component : components) {
        context.render(component, fragment, contract);
      }
    } catch (final Exception e) {
      Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
    }
    // CHECKSTYLE_ON: IllegalCatch
  }
}
