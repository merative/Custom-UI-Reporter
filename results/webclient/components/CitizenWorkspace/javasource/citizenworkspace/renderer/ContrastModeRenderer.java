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
package citizenworkspace.renderer;

import curam.ieg.player.PlayerUtils;
import curam.omega3.user.UserPreferences;
import curam.omega3.user.UserPreferencesFactory;
import curam.util.client.ClientException;
import curam.util.client.domain.render.edit.AbstractEditRenderer;
import curam.util.client.model.Field;
import curam.util.client.path.util.ClientPaths;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.JDEException;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * Renderer class for contrast model selector widget.
 */
public class ContrastModeRenderer extends AbstractEditRenderer {

  // Properties file containing strings rendered at UI
  private static final String propertiesFileName = "ContrastModeSelector";

  @Override
  public void render(final Field field, final DocumentFragment fragment,
    final RendererContext context, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    // Contrast mode flag
    boolean ishighContrastMode = false;

    // Read contrast mode from user preferences object
    final Path prefsPath =
      ClientPaths.SCOPED_ATTRIBUTE_PATH.extendPath("session",
        UserPreferencesFactory.USER_PREFS_ATTRIBUTE_NAME);

    final UserPreferences prefs =
      (UserPreferences) context.getDataAccessor().getRaw(prefsPath);

    // Value is stored as a string, parse to a bool
    try {
      ishighContrastMode =
        new Boolean((String) prefs.getUserPreference("high.contrast.enabled"));
    } catch (final JDEException e) {
      // Don't fail, just return false.
    }

    // Pass the localized messages to JS
    final String standardViewLinkText =
      PlayerUtils.getProperty(propertiesFileName, "Link.Standard.Contrast",
        context);
    final String highContrastViewLinkText =
      PlayerUtils.getProperty(propertiesFileName, "Link.High.Contrast",
        context);

    // Declaratively add ContrastModeSelector Dojo widget to DOM
    final Element contrastChooser =
      fragment.getOwnerDocument().createElement("div");
    contrastChooser.setAttribute("data-dojo-type",
      "cwtk/widget/ContrastModeChooser");
    contrastChooser.setAttribute("data-dojo-props", "highContrastEnabled: "
      + ishighContrastMode + "," + "standardViewLinkText: " + "'"
      + standardViewLinkText + "'" + "," + "highContrastViewLinkText: " + "'"
      + highContrastViewLinkText + "'");
    fragment.appendChild(contrastChooser);
  }

}
