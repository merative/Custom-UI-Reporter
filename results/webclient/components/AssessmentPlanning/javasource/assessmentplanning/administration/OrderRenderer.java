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
package assessmentplanning.administration;

import assessmentplanning.util.RendererHelper;
import curam.client.util.StringHelper;
import curam.util.client.ClientException;
import curam.util.client.domain.render.edit.AbstractEditRenderer;
import curam.util.client.model.Field;
import curam.util.client.model.FieldParameters;
import curam.util.client.path.util.ClientPaths;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.JDEException;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import curam.util.type.Blob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Renderer for creating the edit order control.
 * 
 * @since 6.0
 */
@SuppressWarnings("restriction")
public class OrderRenderer extends AbstractEditRenderer {

  private static final String kEMPTY_STRING = "";

  private static final int kZERO = 0;

  private static final String VIEW_ALT_ARROW_UP_TEXT = "Text.ArrowUpAltText";

  private static final String VIEW_ALT_ARROW_DOWN_TEXT =
    "Text.ArrowDownAltText";

  private String properties;

  private final int tabindex = 0;

  /**
   * Constructor. Sets up the properties file.
   */
  public OrderRenderer() {

    properties = new String();
    setPropertyFileLocation();
  }

  /**
   * Sets the location of the properties file to be used. *
   */
  private void setPropertyFileLocation() {

    properties = "assessmentplanning.administration.OrderRenderer";
  }

  /**
   * Reads a property with the given name from the given properties file.
   * 
   * @param context
   * The renderer context
   * @param file
   * The properties file to use
   * @param property
   * The name of the property to read
   * @return A property with the given name from the given properties file
   */
  private String getProperty(final RendererContext context,
    final String file, final String property) {

    try {
      return context.getDataAccessor().get(
        ClientPaths.GENERAL_RESOURCES_PATH.extendPath(file, property));
    } catch (final DataAccessException exception) {
      return null;
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unused")
  public void render(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final RendererContract contract) throws ClientException,
    DataAccessException, PlugInException {

    final Document myDoc = getDocument(field, context);
    final OrderData orderData = new OrderData(myDoc);
    final List<OrderableItem> orderableItemList =
      orderData.getOrderableItems();

    context.includeScriptURIs("text/javascript",
      "../CDEJ/jscript/AdministerFactorOrder.js");

    final List<String> generatedParents = new ArrayList<String>();

    final Element mainTable =
      documentFragment.getOwnerDocument().createElement("table");
    addStandardCol(documentFragment, mainTable, "left", "field");

    if (orderData.getSplitOrderItemsByParent()) {
      addStandardCol(documentFragment, mainTable, "left", "field");
    }

    int headerCount = 0;
    Element maintr = null;
    Element maintd;

    for (final OrderableItem orderableItemParents : orderableItemList) {
      final String currentParent = orderableItemParents.getParentID();

      // ensure dealing with order item related to current parent
      if (currentParent.equalsIgnoreCase(orderableItemParents.getParentID())
        && !generatedParents.contains(currentParent)) {

        // new stuff
        if (headerCount % 2 == 0) {

          maintr = documentFragment.getOwnerDocument().createElement("tr");
          mainTable.appendChild(maintr);
        }
        maintd =
          createTableDataElementForRow(documentFragment, "vertical-field",
            kZERO, kZERO);

        // hidden text field
        final String hiddenTextFieldTargetID =
          context.addFormItem(field,
            field.getParameters().get(FieldParameters.LABEL),
            "hiddenTextField" + orderableItemParents.getParentID(), false);

        final Element div =
          documentFragment.getOwnerDocument().createElement("div");
        div.setAttribute("class", "list orderFactorsDiv");
        maintd.appendChild(div);
        maintr.appendChild(maintd);
        headerCount++;
        // documentFragment.appendChild(div);

        final Element table =
          documentFragment.getOwnerDocument().createElement("table");
        final String tableID =
          context.addFormItem(field,
            field.getParameters().get(FieldParameters.LABEL), "hiddenTableID"
              + orderableItemParents.getParentID(), false);
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

        if (orderData.getSplitOrderItemsByParent()) {

          final Node textElement =
            documentFragment.getOwnerDocument().createTextNode(
              orderableItemParents.getParentName() + "   ");
          th.appendChild(textElement);
        }

        final String arrowUpImagePath =
          setPathBasedOnHighContrast(context, "../Images/arrow_up_24X24.png",
            "../Images/highcontrast/progress/arrow_up_24X24.png");
        final String arrowDownImagePath =
          setPathBasedOnHighContrast(context,
            "../Images/arrow_down_24X24.png",
            "../Images/highcontrast/progress/arrow_down_24X24.png");

        // create and append the arrow icons to the header
        Element img =
          createArrowIcon(documentFragment, context, hiddenTextFieldTargetID
            + "','" + tableID, "../Images/arrow_up_24X24.png", "moveUp('",
            VIEW_ALT_ARROW_UP_TEXT, "vertical-align: middle");
        th.appendChild(img);

        img =
          createArrowIcon(documentFragment, context, hiddenTextFieldTargetID
            + "','" + tableID, "../Images/arrow_down_24X24.png",
            "moveDown('", VIEW_ALT_ARROW_DOWN_TEXT, "vertical-align: middle");

        th.appendChild(img);

        th = documentFragment.getOwnerDocument().createElement("th");
        th.setAttribute("class", "field");

        // create and append the body of the table
        final Element tbody =
          documentFragment.getOwnerDocument().createElement("tbody");
        tbody.setAttribute("id", "tbody" + tableID);
        table.appendChild(tbody);

        int factorCounter = 0;

        // for each factor
        for (final OrderableItem orderableItem : orderableItemList) {

          if (currentParent.equalsIgnoreCase(orderableItem.getParentID())) {
            generatedParents.add(currentParent);

            final String targetID =
              context.addFormItem(field,
                field.getParameters().get(FieldParameters.LABEL),
                orderableItem.getID(), false);

            tr = documentFragment.getOwnerDocument().createElement("tr");
            tbody.appendChild(tr);

            final String hiddenInputTargetID =
              context.addFormItem(field,
                field.getParameters().get(FieldParameters.LABEL),
                "hiddenField" + orderableItem.getID(), false);

            final Element td =
              documentFragment.getOwnerDocument().createElement("td");
            td.setAttribute("id", targetID);
            td.setAttribute("class", "field");
            /*
             * if there is more than one parent being listed, the name of the
             * parent will be displayed along with the move up and move down
             * icons in the table head. Hence the table data element has to span
             * two columns
             */
            if (orderData.getSplitOrderItemsByParent()) {
              td.setAttribute("colspan", "2");
            }

            Node textElement =
              documentFragment.getOwnerDocument().createTextNode("Name");
            textElement =
              documentFragment.getOwnerDocument().createTextNode(
                orderableItem.getOrderItemName());

            td.setAttribute("onclick", "highlightField('" + targetID + "');");
            td.setAttribute("style",
              "padding-top:0px, padding-bottom:0px, border:0px");
            td.setAttribute("tabindex", "1");

            // needed for updating the values with jscript
            final Element hiddenInput =
              documentFragment.getOwnerDocument().createElement("input");
            hiddenInput.setAttribute("type", "hidden");
            hiddenInput
              .setAttribute("orderableItemID", orderableItem.getID());
            hiddenInput.setAttribute("parentID", orderableItem.getParentID());
            hiddenInput.setAttribute("id", hiddenInputTargetID);
            hiddenInput.setAttribute("name", hiddenInputTargetID);

            td.appendChild(hiddenInput);

            // Do not move this line: The span/text node should be placed AFTER
            // the hidden input
            insertSpanOrTextNode(td, textElement,
              orderableItem.getOrderItemName());

            tr.appendChild(td);

            factorCounter++;
          }
        }

        final Element input =
          documentFragment.getOwnerDocument().createElement("input");
        input.setAttribute("type", "hidden");
        input.setAttribute("id", hiddenTextFieldTargetID);
        input.setAttribute("name", hiddenTextFieldTargetID);
        div.appendChild(input);

      }
    }
    documentFragment.appendChild(mainTable);
  }

  private String setPathBasedOnHighContrast(final RendererContext context,
    final String normal, final String high) {

    String path = "";
    try {
      if (!RendererHelper.getHighContrastPreference(context)) {
        path = normal;
      } else {
        path = high;
      }
    } catch (final SAXException e) {
      throw new RuntimeException(e);
    } catch (final ParserConfigurationException e) {
      throw new RuntimeException(e);
    } catch (final JDEException e) {
      throw new RuntimeException(e);
    }
    return path;
  }

  /**
   * 
   * Adds a row with an OrderableItem.
   * 
   * @param field
   * Render field
   * @param documentFragment
   * The document fragment to be render
   * @param context
   * The renderer context
   * @param orderData
   * Information for how to order data
   * @param generatedParents
   * The parent of list of items
   * @param currentParent
   * The parent of the current item
   * @param tbody
   * Table body for a list
   * @param orderableItem
   * An item to be ordered
   * @throws ClientException
   */
  private void addOrderableItem(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final OrderData orderData, final List<String> generatedParents,
    final String currentParent, final Element tbody,
    final OrderableItem orderableItem) throws ClientException {

    Element tr;
    if (currentParent.equalsIgnoreCase(orderableItem.getParentID())) {
      generatedParents.add(currentParent);

      final String targetID =
        context.addFormItem(field,
          field.getParameters().get(FieldParameters.LABEL),
          orderableItem.getID(), false);

      tr = documentFragment.getOwnerDocument().createElement("tr");
      tbody.appendChild(tr);

      final String hiddenInputTargetID =
        context.addFormItem(field,
          field.getParameters().get(FieldParameters.LABEL), "hiddenField"
            + orderableItem.getID(), false);

      final Element td =
        documentFragment.getOwnerDocument().createElement("td");
      td.setAttribute("id", targetID);
      td.setAttribute("class", "field");
      /*
       * if there is more than one parent being listed, the name of the parent
       * will be displayed along with the move up and move down icons in the
       * table head. Hence the table data element has to span two columns
       */
      if (orderData.getSplitOrderItemsByParent()) {
        td.setAttribute("colspan", "2");
      }

      Node textElement =
        documentFragment.getOwnerDocument().createTextNode("Name");
      textElement =
        documentFragment.getOwnerDocument().createTextNode(
          orderableItem.getOrderItemName());
      td.appendChild(textElement);
      td.setAttribute("onclick", "highlightField('" + targetID + "');");
      td.setAttribute("onkeydown", "highlightFieldOnKeyDown(event,'"
        + targetID + "');");
      td.setAttribute("style",
        "padding-top:0px, padding-bottom:0px, border:0px");
      td.setAttribute("tabindex", Integer.toString(tabindex));

      // needed for updating the values with jscript
      final Element hiddenInput =
        documentFragment.getOwnerDocument().createElement("input");
      hiddenInput.setAttribute("type", "hidden");
      hiddenInput.setAttribute("orderableItemID", orderableItem.getID());
      hiddenInput.setAttribute("parentID", orderableItem.getParentID());
      hiddenInput.setAttribute("id", hiddenInputTargetID);
      hiddenInput.setAttribute("name", hiddenInputTargetID);

      td.appendChild(hiddenInput);

      tr.appendChild(td);
    }
  }

  /**
   * Gets the XML document for the renderer.
   * 
   * @param field
   * The field to be rendered
   * @param context
   * The context for this renderer. This can be used to resolve the
   * paths to actual values
   * @return The XML document for the renderer
   * @throws DataAccessException
   * Generic Exception Signature
   */
  private Document getDocument(final Field field,
    final RendererContext context) throws DataAccessException {

    final Path sourcePath = field.getBinding().getSourcePath();
    final Blob value = (Blob) context.getDataAccessor().getRaw(sourcePath);
    Document myDoc = null;

    final ByteArrayInputStream bais =
      new ByteArrayInputStream(value.copyBytes());
    try {
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
   * The move java script method to be called
   * @param altText
   * the alt text to be display for the image
   * @param style
   * The CSS style to be applied
   * @param keyMethod
   * The on key down java script method to be called
   * @return the created image element representing an arrow icon
   */
  private Element createArrowIcon(final DocumentFragment documentFragment,
    final RendererContext context, final String hiddenTextFieldTargetID,
    final String imageLocation, final String moveMethod,
    final String altText, final String style) {

    final Element img =
      documentFragment.getOwnerDocument().createElement("img");
    img.setAttribute("src", imageLocation);
    // only set the style if one is passed in
    if (!StringHelper.isEmpty(style)) {
      img.setAttribute("style", style);
    }
    img.setAttribute("onclick", moveMethod + hiddenTextFieldTargetID + "')");
    img.setAttribute("alt", getProperty(context, properties, altText));
    img.setAttribute("title", getProperty(context, properties, altText));
    img.setAttribute("tabindex", "1");
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

  private void insertSpanOrTextNode(final Element td, final Node textElement,
    final String text) {

    // if (BidiUtils.isBidi()) {
    // BidiUtils.appendSpanNode(td, text);
    // } else {
    td.appendChild(textElement);
    // }
  }
}
