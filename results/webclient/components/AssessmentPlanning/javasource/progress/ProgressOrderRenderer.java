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
package progress;

import curam.client.util.StringHelper;
import curam.util.client.ClientException;
import curam.util.client.domain.render.edit.AbstractEditRenderer;
import curam.util.client.model.Field;
import curam.util.client.model.FieldParameters;
import curam.util.client.path.util.ClientPaths;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import curam.util.type.Blob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Renderer class for the progress order edit widget.
 * 
 * @since 6.0
 */
public class ProgressOrderRenderer extends AbstractEditRenderer {

  private static final String PROPERTIES = "progress.ProgressOrderRenderer";;

  private static final int kZERO = 0;

  private static final String kEMPTY_STRING = "";

  private static final String VIEW_ALT_ARROW_UP_TEXT = "Text.ArrowUpAltText";

  private static final String VIEW_ALT_ARROW_DOWN_TEXT =
    "Text.ArrowDownAltText";

  private final int tabindex = 0;

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final RendererContract contract) throws ClientException,
    DataAccessException, PlugInException {

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

    final ProgressOrderData progressOrderData = new ProgressOrderData(myDoc);
    final List<ProgressOrderableItem> orderableItemList =
      progressOrderData.getOrderableItems();

    context.includeScriptURIs("text/javascript",
      "../CDEJ/jscript/AdministerProgressOrder.js");

    final Element mainTable =
      documentFragment.getOwnerDocument().createElement("table");
    addStandardCol(documentFragment, mainTable, "left", "field");

    int headerCount = 0;
    Element maintr = null;
    Element maintd;

    if (headerCount % 2 == 0) {

      maintr = documentFragment.getOwnerDocument().createElement("tr");
      mainTable.appendChild(maintr);
    }
    maintd =
      createTableDataElementForRow(documentFragment, "vertical-field", kZERO,
        kZERO);

    // hidden text field
    final String hiddenTextFieldTargetID =
      context.addFormItem(field,
        field.getParameters().get(FieldParameters.LABEL), "hiddenTextField"
          + "test", false);

    final Element div =
      documentFragment.getOwnerDocument().createElement("div");
    div.setAttribute("class", "list");
    maintd.appendChild(div);
    maintr.appendChild(maintd);
    headerCount++;

    final Element table =
      documentFragment.getOwnerDocument().createElement("table");
    final String tableID =
      context.addFormItem(field,
        field.getParameters().get(FieldParameters.LABEL), "hiddenTableID"
          + "test", false);
    table.setAttribute("id", tableID);

    div.appendChild(table);

    // create and append the head information
    final Element thead =
      documentFragment.getOwnerDocument().createElement("thead");
    table.appendChild(thead);

    Element tr = documentFragment.getOwnerDocument().createElement("tr");
    thead.appendChild(tr);

    Element th = documentFragment.getOwnerDocument().createElement("th");
    tr.appendChild(th);

    String arrowUpImagePath = "";
    String arrowDownImagePath = "";

    if (!gethighContrastPreference(field, context)) {
      arrowUpImagePath = "../Images/arrow_up_24X24.png";
      arrowDownImagePath = "../Images/arrow_down_24X24.png";
    } else {
      arrowUpImagePath = "../Images/highcontrast/progress/arrow_up_24X24.png";
      arrowDownImagePath =
        "../Images/highcontrast/progress/arrow_down_24X24.png";
    }

    // create and append the arrow icons to the header
    Element img =
      createArrowIcon(documentFragment, context, hiddenTextFieldTargetID
        + "','" + tableID, arrowUpImagePath, "moveUp('",
        VIEW_ALT_ARROW_UP_TEXT, "vertical-align: middle",
        "moveOnKeyDown(event,'moveUp','");
    img.setAttribute("role", "img");
    th.appendChild(img);

    img =
      createArrowIcon(documentFragment, context, hiddenTextFieldTargetID
        + "','" + tableID, arrowDownImagePath, "moveDown('",
        VIEW_ALT_ARROW_DOWN_TEXT, "vertical-align: middle ",
        "moveOnKeyDown(event,'moveDown','");
    img.setAttribute("role", "img");
    th.appendChild(img);

    th = documentFragment.getOwnerDocument().createElement("th");
    th.setAttribute("class", "field");

    // create and append the body of the table
    final Element tbody =
      documentFragment.getOwnerDocument().createElement("tbody");
    tbody.setAttribute("id", "tbody" + tableID);
    tbody.setAttribute("role", "menubar");
    table.appendChild(tbody);

    int progressCounter = 0;

    // for each factor
    for (final ProgressOrderableItem progressOrderableItem : orderableItemList) {

      final String targetID =
        context.addFormItem(field,
          field.getParameters().get(FieldParameters.LABEL),
          progressOrderableItem.getID(), false);

      tr = documentFragment.getOwnerDocument().createElement("tr");
      tbody.appendChild(tr);

      final String hiddenInputTargetID =
        context.addFormItem(field,
          field.getParameters().get(FieldParameters.LABEL), "hiddenField"
            + progressOrderableItem.getID(), false);

      final Element td =
        documentFragment.getOwnerDocument().createElement("td");
      td.setAttribute("id", targetID);
      td.setAttribute("class", "field");

      Node textElement =
        documentFragment.getOwnerDocument().createTextNode("Name");
      textElement =
        documentFragment.getOwnerDocument().createTextNode(
          progressOrderableItem.getOrderItemName());
      td.appendChild(textElement);
      td.setAttribute("onclick", "highlightField('" + targetID + "');");
      td.setAttribute("style",
        "padding-top:0px, padding-bottom:0px, border:0px");
      td.setAttribute("onkeydown", "highlightFieldOnKeyDown(event,'"
        + targetID + "');");
      td.setAttribute("tabindex", Integer.toString(tabindex));
      td.setAttribute("title", progressOrderableItem.getOrderItemName());

      // needed for updating the values with javascript
      final Element hiddenInput =
        documentFragment.getOwnerDocument().createElement("input");
      hiddenInput.setAttribute("type", "hidden");
      hiddenInput.setAttribute("orderableItemID",
        progressOrderableItem.getID());
      hiddenInput.setAttribute("id", hiddenInputTargetID);
      hiddenInput.setAttribute("name", hiddenInputTargetID);
      hiddenInput.setAttribute("title",
        progressOrderableItem.getOrderItemName());

      td.appendChild(hiddenInput);
      tr.appendChild(td);

      progressCounter++;
    }

    final Element input =
      documentFragment.getOwnerDocument().createElement("input");
    input.setAttribute("type", "hidden");
    input.setAttribute("id", hiddenTextFieldTargetID);
    input.setAttribute("name", hiddenTextFieldTargetID);
    div.appendChild(input);

    documentFragment.appendChild(mainTable);
  }

  /**
   * Creates a table data Element for a table row, assigning the passed in
   * style. If the passed in style is an empty list, no style is applied.
   * 
   * @param documentFragment
   * The document fragment the table data element is to be created from
   * @param style
   * The style to be applied to the table data element
   * @param colspan
   * The number of columns the table data element is to span
   * @param rowspan
   * The number of rows the table data element is to span
   * @return The created Element with the assigned style
   */
  private Element createTableDataElementForRow(
    final DocumentFragment documentFragment, final String style,
    final int colspan, final int rowspan) {

    final Element td =
      documentFragment.getOwnerDocument().createElement("td");

    if (!style.equalsIgnoreCase(kEMPTY_STRING)) {
      td.setAttribute("class", style);
    }

    if (colspan != kZERO) {
      td.setAttribute("colspan", Integer.toString(colspan));
    }

    if (rowspan != kZERO) {
      td.setAttribute("rowspan", Integer.toString(rowspan));
    }

    return td;
  }

  /**
   * Adds a standard table column.
   * 
   * @param documentFragment
   * The document to hold the data
   * @param table
   * The table to add the column to
   * @param alignStyle
   * the column alignment
   * @param styleToUse
   * reference to the style sheet to be used for displaying the column.
   */
  private void addStandardCol(final DocumentFragment documentFragment,
    final Element table, final String alignStyle, final String styleToUse) {

    final Element col =
      documentFragment.getOwnerDocument().createElement("col");
    col.setAttribute("class", styleToUse);
    col.setAttribute("align", alignStyle);
    table.appendChild(col);
  }

  /**
   * Creates an image element holding the arrow icon that when click by the user
   * will invoke a java script method and move the selected item.
   * 
   * @param documentFragment
   * The document to add the data to
   * @param context
   * The renderer context
   * @param hiddenTextFieldTargetID
   * The hidden text field
   * @param imageLocation
   * The location of the image to be displayed
   * @param moveMethod
   * the move java script method to be called
   * @param altText
   * the alt text to be display for the image
   * @param style
   * the css style class name.
   * @param keyMethod
   * The on key down java script method to be called
   * @return the created image element representing an arrow icon
   * @throws DataAccessException
   * @throws DOMException
   */
  private Element createArrowIcon(final DocumentFragment documentFragment,
    final RendererContext context, final String hiddenTextFieldTargetID,
    final String imageLocation, final String moveMethod,
    final String altText, final String style, final String keyMethod)
    throws DataAccessException {

    final Element img =
      documentFragment.getOwnerDocument().createElement("img");
    img.setAttribute("src", imageLocation);
    // only set the style if one is passed in
    if (!StringHelper.isEmpty(style)) {
      img.setAttribute("style", style);
    }
    img.setAttribute("onclick", moveMethod + hiddenTextFieldTargetID + "')");
    img.setAttribute(
      "alt",
      context.getDataAccessor().get(
        ClientPaths.GENERAL_RESOURCES_PATH.extendPath(PROPERTIES, altText)));
    img.setAttribute(
      "title",
      context.getDataAccessor().get(
        ClientPaths.GENERAL_RESOURCES_PATH.extendPath(PROPERTIES, altText)));
    img.setAttribute("tabindex", Integer.toString(tabindex));
    img.setAttribute("onkeydown", keyMethod + hiddenTextFieldTargetID + "')");

    return img;
  }

  /**
   * Get a User Preference for high contrast mode.
   * 
   * @param context
   * The renderer context
   * @param field
   * The field to use
   * @return True with high contrast mode enabled, otherwise false
   */
  private boolean gethighContrastPreference(final Field field,
    final RendererContext context) throws DataAccessException {

    // get correct image for high contrast mode
    final Document doc = getXMLDocument(field, context);

    final Node rootNode = doc.getFirstChild();
    final String highContrastIndicator =
      rootNode.getAttributes().getNamedItem("HIGH_CONTRAST_IND")
        .getNodeValue();

    return Boolean.valueOf(highContrastIndicator);
  }

  /**
   * Gets the xml document containing the data from the server.
   * 
   * @param field
   * The field
   * @param context
   * The renderer context
   * @return The xml document containing the data from the server
   * @throws DataAccessException
   */
  private Document getXMLDocument(final Field field,
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
    return myDoc;
  }
}
