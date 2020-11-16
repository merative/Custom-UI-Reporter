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
package assessmentplanning.delivery;

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
import java.util.Iterator;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Widget renderer for the guidance in relation assessed factors.
 */
public class AssessmentDeliverySummaryClientListRenderer extends
  AbstractViewRenderer {

  /**
   * String constant for Client. Used to reference the client. <br>
   * <br>
   * Value = 'Client'.
   */
  // move to properties
  public String kCLIENT = "Client";

  /**
   * String constant for Role. Used to reference the client role. <br>
   * <br>
   * Value = 'Role'.
   */
  public String kROLE = "Role";

  /**
   * String constant for Age. Used to reference the client age. <br>
   * <br>
   * Value = 'Age'.
   */
  public String kAGE = "Age";

  /**
   * String constant for Gender. Used to reference the client gender. <br>
   * <br>
   * Value = 'Gender'.
   */
  public String kGENDER = "Gender";

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final RendererContract rendererContract) throws ClientException,
    DataAccessException, PlugInException {

    final Path sourcePath = field.getBinding().getSourcePath();
    final Blob value = (Blob) context.getDataAccessor().getRaw(sourcePath);

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

    final AssessmentDeliverySummaryClientListData assessmentDeliverySummaryClientListData =
      new AssessmentDeliverySummaryClientListData(myDoc);

    renderAssessmentSummaryClientListTable(documentFragment, context,
      assessmentDeliverySummaryClientListData);

  }

  /**
   * Renders the guidance widget where only a single column exists. This occurs
   * when either
   * <ul>
   * <li>The assessment is group based</li>
   * <li>The assessment exists for a single individual</li>
   * <li>the assessment exists for multiple people but only one was/can be
   * assessed</li>
   * </ul>
   * 
   * @param documentFragment
   * The document to add the data to
   * @param context
   * The renderer context
   * @param assessmentDeliverySummaryClientListData
   * Object holding the guidance data to be used in generating the
   * guidance widget
   */
  private
    void
    renderAssessmentSummaryClientListTable(
      final DocumentFragment documentFragment,
      final RendererContext context,
      final AssessmentDeliverySummaryClientListData assessmentDeliverySummaryClientListData) {

    final Element div =
      documentFragment.getOwnerDocument().createElement("div");
    div.setAttribute("class", "list");

    final Element scrollableScript =
      documentFragment.getOwnerDocument().createElement("script");
    scrollableScript.setAttribute("type", "text/javascript");

    final Node scriptTextNode =
      createTextNode(documentFragment,
        "curam.util.addScrollableHeightListener(\"scrollableN2014D\", 200);");
    scrollableScript.appendChild(scriptTextNode);
    div.appendChild(scrollableScript);

    final Element scrollableDiv =
      documentFragment.getOwnerDocument().createElement("div");

    scrollableDiv.setAttribute("id", "scrollableN2014D");
    scrollableDiv.setAttribute("class", "scrollable");
    scrollableDiv.setAttribute("style", "max-height: 200px");
    div.appendChild(scrollableDiv);

    final Element table =
      documentFragment.getOwnerDocument().createElement("table");
    table.setAttribute("id", "sortable_N2014D");
    table.setAttribute("class", "paginated-list-id-N2014D");
    table.setAttribute("cellspacing", "0");
    scrollableDiv.appendChild(table);

    // create the columns and set the widths
    Element column = documentFragment.getOwnerDocument().createElement("col");

    column.setAttribute("class", "field");
    column.setAttribute("width", "9%");
    table.appendChild(column);
    column = documentFragment.getOwnerDocument().createElement("col");
    column.setAttribute("class", "field");
    column.setAttribute("width", "23%");
    table.appendChild(column);
    column = documentFragment.getOwnerDocument().createElement("col");
    column.setAttribute("class", "field");
    column.setAttribute("width", "35%");
    table.appendChild(column);
    column = documentFragment.getOwnerDocument().createElement("col");
    column.setAttribute("class", "field");
    column.setAttribute("width", "13%");
    table.appendChild(column);
    column = documentFragment.getOwnerDocument().createElement("col");
    column.setAttribute("width", "20%");
    column.setAttribute("class", "field");
    table.appendChild(column);

    // create the table head element
    final Element thead =
      documentFragment.getOwnerDocument().createElement("thead");
    table.appendChild(thead);

    // create the tr
    final Element tr =
      documentFragment.getOwnerDocument().createElement("tr");
    thead.appendChild(tr);

    setTableHeadDetails(documentFragment, tr, " ", "field first-header");
    setTableHeadDetails(documentFragment, tr, kCLIENT, "field");
    setTableHeadDetails(documentFragment, tr, kROLE, "field");
    setTableHeadDetails(documentFragment, tr, kAGE, "field");
    setTableHeadDetails(documentFragment, tr, kGENDER, "field last-header");

    final Element tbody =
      documentFragment.getOwnerDocument().createElement("tbody");
    table.appendChild(tbody);

    Boolean useOdd = true;
    final Iterator<AssessmentDeliverySummaryClientDetails> iteratorToUse =
      assessmentDeliverySummaryClientListData
        .getAssessmentDeliverySummaryClientDetails().iterator();

    while (iteratorToUse.hasNext()) {
      final AssessmentDeliverySummaryClientDetails assessmentDeliverySummaryClientDetails =
        iteratorToUse.next();

      final Element tableBodyRow = createElement(documentFragment, "tr");
      tbody.appendChild(tableBodyRow);

      tableBodyRow.setAttribute("clientIdentifier",
        assessmentDeliverySummaryClientDetails.getClientIdentifier());
      if (useOdd) {
        if (iteratorToUse.hasNext()) {
          tableBodyRow.setAttribute("class", "odd");
        } else {
          tableBodyRow.setAttribute("class", "odd-last-row");
        }
        useOdd = false;
      } else {
        if (iteratorToUse.hasNext()) {
          tableBodyRow.setAttribute("class", "even");
        } else {
          tableBodyRow.setAttribute("class", "even-last-row");
        }
        useOdd = true;
      }

      // this is new
      setTableRowDetails(documentFragment, tableBodyRow, useOdd,
        assessmentDeliverySummaryClientDetails.getColor(),
        "field add-color-to-field first-field", "colorRow");

      // this is changed
      setTableRowDetails(documentFragment, tableBodyRow, useOdd,
        assessmentDeliverySummaryClientDetails.getName(), "field");
      setTableRowDetails(documentFragment, tableBodyRow, useOdd,
        assessmentDeliverySummaryClientDetails.getRoleName(), "field");
      setTableRowDetails(documentFragment, tableBodyRow, useOdd,
        assessmentDeliverySummaryClientDetails.getAge(), "field");
      setTableRowDetails(documentFragment, tableBodyRow, useOdd,
        assessmentDeliverySummaryClientDetails.getGender(),
        "field last-field");

    }

    documentFragment.appendChild(div);
  }

  /**
   * Sets the table header details.
   * 
   * @param documentFragment
   * The document fragment being used
   * @param tr
   * The current row
   * @param kclient2
   * The client
   * @param classStyle
   * The CSS class to be used
   */
  private void setTableHeadDetails(final DocumentFragment documentFragment,
    final Element tr, final String kclient2, final String classStyle) {

    final Element thClient =
      documentFragment.getOwnerDocument().createElement("th");
    thClient.setAttribute("id", "tableListThClient");
    thClient.setAttribute("class", classStyle);
    tr.appendChild(thClient);
    final Element span =
      documentFragment.getOwnerDocument().createElement("span");
    thClient.appendChild(span);
    final Element a1 = documentFragment.getOwnerDocument().createElement("a");
    a1.setAttribute("href", "#");
    a1.setAttribute("title", "sort column");
    final Node text1 = createTextNode(documentFragment, kclient2);
    a1.appendChild(text1);

    span.appendChild(a1);
  }

  /**
   * Sets the table row details.
   * 
   * @param documentFragment
   * The document fragment in use
   * @param tr
   * The current row
   * @param useOdd
   * Indicates if this is an odd or even row
   * @param color
   * The color to be used
   * @param classToUse
   * The CSS class to be used
   * @param name
   * The name of the client
   */
  private void setTableRowDetails(final DocumentFragment documentFragment,
    final Element tr, final Boolean useOdd, final String color,
    final String classToUse, final String name) {

    final Element tableBodyRowTD = createElement(documentFragment, "td");
    tr.appendChild(tableBodyRowTD);
    tableBodyRowTD.setAttribute("class", classToUse);
    tableBodyRowTD.setAttribute("colorToUse", "#" + color);
    tableBodyRowTD.setAttribute("name", name);
    final Node tableBodyRowTextName = createTextNode(documentFragment, "");
    tableBodyRowTD.appendChild(tableBodyRowTextName);

  }

  /**
   * Sets the table row details for the client.
   * 
   * @param documentFragment
   * The document fragment in use
   * @param tr
   * The table row
   * @param useOdd
   * Indicates if this is an odd row
   * @param assessmentDeliverySummaryClientDetails
   * The client summary details
   * @param classToUse
   * The CSS class to use
   */
  private void setTableRowDetails(final DocumentFragment documentFragment,
    final Element tr, final Boolean useOdd,
    final String assessmentDeliverySummaryClientDetails,
    final String classToUse) {

    final Element tableBodyRowTD = createElement(documentFragment, "td");
    tr.appendChild(tableBodyRowTD);
    tableBodyRowTD.setAttribute("class", classToUse);
    final Node tableBodyRowTextName =
      createTextNode(documentFragment, assessmentDeliverySummaryClientDetails);
    tableBodyRowTD.appendChild(tableBodyRowTextName);
  }

  /**
   * Creates an {@link Element} of the passed type for the passed in
   * {@link DocumentFragment}.
   * 
   * @param documentFragment
   * The document fragment the {@link Element} is to be created for
   * @param elementType
   * The type of {@link Element} to be created
   * @return The created {@link Element}
   */
  private Element createElement(final DocumentFragment documentFragment,
    final String elementType) {

    final Element element =
      documentFragment.getOwnerDocument().createElement(elementType);

    return element;
  }

  /**
   * Creates a text node using the passed in string.
   * 
   * @param documentFragment
   * The document the text node is to be created for
   * @param text
   * The text to be held in the text node
   * @return the created text node
   */
  private Node createTextNode(final DocumentFragment documentFragment,
    final String text) {

    final Node textElement =
      documentFragment.getOwnerDocument().createTextNode(text);

    return textElement;
  }
}
