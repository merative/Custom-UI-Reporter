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
package curam.clinicaldata.client.render;

import curam.client.util.StringHelper;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import curam.util.common.util.CodeItem;
import curam.util.dom.html2.HTMLUtils;
import org.w3c.dom.DocumentFragment;

/**
 * View renderer for parsing the clinical code values to display the appropriate
 * value. The description value is displayed if available, otherwise the code is
 * displayed.
 * 
 * @since 6.0.5.4
 */
public class ClinicalCodeViewRenderer extends AbstractViewRenderer {

  /**
   * The marker string inserted into the content if the tag has no value to
   * produce.
   */
  private static final String NO_VALUE_MARKER = "o3nv";

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unused")
  @Override
  public void render(final Field field, final DocumentFragment fragment,
      final RendererContext context, final RendererContract contract)
      throws ClientException, DataAccessException, PlugInException {

    final Path sourcePath = field.getBinding().getSourcePath();
    final Object rawObject = context.getDataAccessor().getRaw(sourcePath);
    String fieldValue;

    if (rawObject instanceof CodeItem) {
      final CodeItem codeItem = (CodeItem) rawObject;
      fieldValue = codeItem.getCode();
    } else {
      fieldValue = rawObject.toString();
    }

    // Get the text to display from the field value
    final String displayText = ClinicalCodeUtils.getDisplayText(fieldValue);

    if (StringHelper.isEmpty(displayText)) {
      HTMLUtils.appendComment(fragment, NO_VALUE_MARKER);
      HTMLUtils.appendNbsp(fragment);
    } else {
      HTMLUtils.appendTextNode(fragment, displayText);
    }
  }
}
