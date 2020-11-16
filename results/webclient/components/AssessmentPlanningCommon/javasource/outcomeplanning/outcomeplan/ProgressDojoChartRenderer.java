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
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import curam.util.type.Blob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * Renderer class for the progress dojo chart widget.
 * 
 * @since 6.0
 */
public class ProgressDojoChartRenderer extends AbstractViewRenderer {

  /**
   * The width to be used for the progress chart.
   */
  private static final String kCHART_WIDTH = "100%";

  /**
   * The height to be used for the progress chart.
   */
  private static final String kCHART_HEIGHT = "150";

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final RendererContract arg3) throws ClientException, DataAccessException,
    PlugInException {

    final ProgressChartData progressChartData =
      getProgressChartData(field, context);

    final String id = "om_progressChartContainer";
    final Element loaderScript;
    final Element callbackScript;
    final Element chartContainerDiv;

    loaderScript =
      documentFragment.getOwnerDocument().createElement("script");
    loaderScript.setAttribute("type", "text/javascript");
    loaderScript.setAttribute("src",
      "../CDEJ/jscript/curam/smartercare/ipsum/util/ApplicationLoader.js");
    loaderScript.setTextContent("//script content");
    documentFragment.appendChild(loaderScript);

    // assign the params for the js callback
    callbackScript =
      documentFragment.getOwnerDocument().createElement("script");
    callbackScript.setAttribute("type", "text/javascript");
    final String js =
      "dojo.addOnLoad(function(){window.appLoader.loadApp({context: this, componentToLoad: 'curam/om/app/progress/ProgressChart', layerFileToLoad:'../CDEJ/jscript/curam/layers/ProgressChartLayer.js', targetDomID: '"
        + id
        + "', functionHandler: function(){ return {style: {height: '"
        + kCHART_HEIGHT
        + "px',width: '"
        + kCHART_WIDTH
        + "'}, serverClassName: 'ChartProgress', serverMethodName: 'chartProgress', serverMethodParams: {relatedID: '"
        + progressChartData.getProgressRelatedID()
        + "', relatedType: '"
        + progressChartData.getProgressRelatedType() + "'}};}})});";
    callbackScript.setTextContent(js);
    documentFragment.appendChild(callbackScript);

    // Create the div element for the chart widget.
    chartContainerDiv =
      documentFragment.getOwnerDocument().createElement("div");
    chartContainerDiv.setAttribute("id", id);

    documentFragment.appendChild(chartContainerDiv);

  }

  /**
   * Retrieves the progress data to be used for the creation of the chart flash
   * vars.
   * 
   * @param field
   * @param context
   * @return the progress chart data for use in the creation of the flash vars
   * in the chart
   * @throws DataAccessException
   */
  private ProgressChartData getProgressChartData(final Field field,
    final RendererContext context) throws DataAccessException {

    final Blob value;
    final Path sourcePath;

    sourcePath = field.getBinding().getSourcePath();

    // get the xml
    value = (Blob) context.getDataAccessor().getRaw(sourcePath);

    Document myDoc = null;

    try {
      final ByteArrayInputStream bais =
        new ByteArrayInputStream(value.copyBytes());
      final ObjectInputStream ois = new ObjectInputStream(bais);
      myDoc = (Document) ois.readObject();

      ois.close();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    } catch (final ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    return new ProgressChartData(myDoc);
  }

}
