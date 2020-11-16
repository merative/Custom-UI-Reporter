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
package assessmentplanning.delivery;

import curam.client.util.XmlUtils;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.model.Field;
import curam.util.client.path.util.ClientPaths;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import java.util.List;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Renderer used for the assessment factor results list.
 */
public class AssessmentFactorResultsListRenderer extends AbstractViewRenderer {

  private static final String SCORE = "Field.Label.Score";

  private static final String DECISION = "Field.Label.Decision";

  private static final String ASSESSED_DATE = "Field.Label.AssessedDate";

  private static final String NAME = "Field.Label.Name";

  private static final String CATEGORY = "Field.Label.Category";

  private static final String CLASSIFICATION = "Field.Label.Classification";

  private static final String FACTOR = "Field.Label.Factor";

  private static final String TITLE = "Text.AttentionTitleText";

  private static final String ALT = "Text.AttentionAltText";

  private final String properties;

  /**
   * Constructor. Sets up the properties file.
   */
  public AssessmentFactorResultsListRenderer() {

    properties =
      "assessmentplanning.delivery.AssessmentFactorResultsListRenderer";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final RendererContract rendererContract) throws ClientException,
    DataAccessException, PlugInException {

    final Path sourcePath = field.getBinding().getSourcePath();
    final String value =
      (String) context.getDataAccessor().getRaw(sourcePath);
    final Document myDoc = XmlUtils.parseXmlText(value);

    final AssessmentFactorResultData assessmentFactorResultData =
      new AssessmentFactorResultData(myDoc);

    final Element div = createElement(documentFragment, "div");
    div.setAttribute("id", "priorityResultDiv");
    div.setAttribute("class", "list-no-space list");

    final Element table =
      documentFragment.getOwnerDocument().createElement("table");
    table.setAttribute("cellspacing", "0");
    table.setAttribute("cellpadding", "50");
    div.appendChild(table);

    final int numColumnsToDisplay =
      addTableColumns(documentFragment, assessmentFactorResultData, table);

    addTableHeader(documentFragment, context, assessmentFactorResultData,
      table, numColumnsToDisplay);

    addTableBody(documentFragment, assessmentFactorResultData, table,
      numColumnsToDisplay, context);

    documentFragment.appendChild(div);
  }

  /**
   * Adds the body of the table to be rendered. This contains the result rows.
   * 
   * @param documentFragment
   * The document fragment for the renderer
   * @param assessmentFactorResultData
   * The data to be rendered
   * @param table
   * The table the body is to be added to
   * @param numColumnsToDisplay
   * The number of columns to be displayed
   * @throws DataAccessException
   * @throws DOMException
   */
  @SuppressWarnings("restriction")
  private void addTableBody(final DocumentFragment documentFragment,
    final AssessmentFactorResultData assessmentFactorResultData,
    final Element table, final int numColumnsToDisplay,
    final RendererContext context) throws DOMException, DataAccessException {

    Element tr;
    Node textValue;
    int colIndex;
    final Element tbody =
      documentFragment.getOwnerDocument().createElement("tbody");

    table.appendChild(tbody);

    final List<FactorResultExt> factorResultsList =
      assessmentFactorResultData.getFactorResultsList();

    int rowIndex = 1;
    for (final FactorResultExt factorResult : factorResultsList) {
      colIndex = 1;
      tr = documentFragment.getOwnerDocument().createElement("tr");
      setTRClass(factorResultsList, tr, rowIndex);
      tbody.appendChild(tr);

      Element td;

      if (assessmentFactorResultData.getPrioritiesApplicableInd()) {
        td = documentFragment.getOwnerDocument().createElement("td");
        setTDClass(colIndex, numColumnsToDisplay, td);
        if ("yes".equalsIgnoreCase(factorResult.getPriority())) {
          final Element img =
            documentFragment.getOwnerDocument().createElement("img");
          img.setAttribute("src", "../Images/icon_attention.png");
          img
            .setAttribute(
              "alt",
              context.getDataAccessor().get(
                ClientPaths.GENERAL_RESOURCES_PATH
                  .extendPath(properties, ALT)));
          img.setAttribute(
            "title",
            context.getDataAccessor().get(
              ClientPaths.GENERAL_RESOURCES_PATH
                .extendPath(properties, TITLE)));
          td.appendChild(img);
        }
        tr.appendChild(td);
        colIndex++;
      }

      if (!assessmentFactorResultData.getGroupBasedInd()
        || assessmentFactorResultData.getDisplayClientsInd()) {
        td = documentFragment.getOwnerDocument().createElement("td");
        setTDClass(colIndex, numColumnsToDisplay, td);
        tr.appendChild(td);
        colIndex++;

        textValue =
          documentFragment.getOwnerDocument().createTextNode(
            factorResult.getClient());
        td.appendChild(textValue);
      }

      td = documentFragment.getOwnerDocument().createElement("td");
      setTDClass(colIndex, numColumnsToDisplay, td);
      tr.appendChild(td);
      colIndex++;

      textValue =
        documentFragment.getOwnerDocument().createTextNode(
          factorResult.getFactor());
      td.appendChild(textValue);

      if (assessmentFactorResultData.getMultipleCategoriesInd()) {
        td = documentFragment.getOwnerDocument().createElement("td");
        setTDClass(colIndex, numColumnsToDisplay, td);
        tr.appendChild(td);
        colIndex++;

        textValue =
          documentFragment.getOwnerDocument().createTextNode(
            factorResult.getCategory());
        td.appendChild(textValue);
      }

      td = documentFragment.getOwnerDocument().createElement("td");
      setTDClass(colIndex, numColumnsToDisplay, td);
      tr.appendChild(td);
      colIndex++;

      textValue =
        documentFragment.getOwnerDocument().createTextNode(
          factorResult.getClassification());
      td.appendChild(textValue);

      if (assessmentFactorResultData.getScoringUsedInd()) {
        td = documentFragment.getOwnerDocument().createElement("td");
        setTDClass(colIndex, numColumnsToDisplay, td);
        tr.appendChild(td);
        colIndex++;

        textValue =
          documentFragment.getOwnerDocument().createTextNode(
            factorResult.getScore());
        td.appendChild(textValue);
      }

      if (assessmentFactorResultData.getDisplayAssessmentDateInd()) {
        td = documentFragment.getOwnerDocument().createElement("td");
        setTDClass(colIndex, numColumnsToDisplay, td);
        tr.appendChild(td);
        colIndex++;

        textValue =
          documentFragment.getOwnerDocument().createTextNode(
            factorResult.getAssessedDate());
        td.appendChild(textValue);
      }

      rowIndex++;
    }
  }

  /**
   * Adds the header for the table to be rendered.
   * 
   * @param documentFragment
   * The document fragment for the renderer
   * @param context
   * The renderer context
   * @param assessmentFactorResultData
   * The data to be rendered
   * @param table
   * The table the header is to be added to
   * @throws DataAccessException
   */
  private void addTableHeader(final DocumentFragment documentFragment,
    final RendererContext context,
    final AssessmentFactorResultData assessmentFactorResultData,
    final Element table, final int numColumnsToDisplay)
    throws DataAccessException {

    final Element thead =
      documentFragment.getOwnerDocument().createElement("thead");
    table.appendChild(thead);

    final Element tr =
      documentFragment.getOwnerDocument().createElement("tr");
    thead.appendChild(tr);

    Element th;
    Node textValue;
    int colIndex = 1;

    // Add the priority result cell, if priorities are used
    if (assessmentFactorResultData.getPrioritiesApplicableInd()) {
      th = documentFragment.getOwnerDocument().createElement("th");
      setTHClass(colIndex, numColumnsToDisplay, th);
      tr.appendChild(th);
      colIndex++;
    }

    // Add the name cell, if its not a group based assessment
    if (!assessmentFactorResultData.getGroupBasedInd()
      || assessmentFactorResultData.getDisplayClientsInd()) {
      th = documentFragment.getOwnerDocument().createElement("th");
      setTHClass(colIndex, numColumnsToDisplay, th);
      textValue =
        createTextNode(
          documentFragment,
          context.getDataAccessor().get(
            ClientPaths.GENERAL_RESOURCES_PATH.extendPath(properties, NAME)));
      th.appendChild(textValue);
      tr.appendChild(th);
      colIndex++;
    }

    // Add the factor/decision cell
    th = documentFragment.getOwnerDocument().createElement("th");
    setTHClass(colIndex, numColumnsToDisplay, th);

    // use the 'Factor' column title, if its a factor based assessment,
    // otherwise
    // use the 'Decision' column title
    if (assessmentFactorResultData.getFactorsApplicableInd()) {
      textValue =
        createTextNode(documentFragment,
          getFactorResultLabelText(context, assessmentFactorResultData));
    } else {
      textValue =
        createTextNode(
          documentFragment,
          context.getDataAccessor().get(
            ClientPaths.GENERAL_RESOURCES_PATH.extendPath(properties,
              DECISION)));
    }
    th.appendChild(textValue);
    tr.appendChild(th);
    colIndex++;

    // Add a category cell if multiple categories exist
    if (assessmentFactorResultData.getMultipleCategoriesInd()) {
      th = documentFragment.getOwnerDocument().createElement("th");
      setTHClass(colIndex, numColumnsToDisplay, th);
      textValue =
        createTextNode(
          documentFragment,
          context.getDataAccessor().get(
            ClientPaths.GENERAL_RESOURCES_PATH.extendPath(properties,
              CATEGORY)));
      th.appendChild(textValue);
      tr.appendChild(th);
      colIndex++;
    }

    // Add the classification cell
    th = documentFragment.getOwnerDocument().createElement("th");
    setTHClass(colIndex, numColumnsToDisplay, th);
    textValue =
      createTextNode(documentFragment,
        getClassificationResultLabelText(context, assessmentFactorResultData));
    th.appendChild(textValue);
    tr.appendChild(th);
    colIndex++;

    // Add the score cell, if its a scoring based assessment
    if (assessmentFactorResultData.getScoringUsedInd()) {
      th = documentFragment.getOwnerDocument().createElement("th");
      setTHClass(colIndex, numColumnsToDisplay, th);
      textValue =
        createTextNode(
          documentFragment,
          context.getDataAccessor().get(
            ClientPaths.GENERAL_RESOURCES_PATH.extendPath(properties, SCORE)));
      th.appendChild(textValue);
      tr.appendChild(th);
      colIndex++;
    }

    if (assessmentFactorResultData.getDisplayAssessmentDateInd()) {
      th = documentFragment.getOwnerDocument().createElement("th");
      setTHClass(colIndex, numColumnsToDisplay, th);
      textValue =
        createTextNode(
          documentFragment,
          context.getDataAccessor().get(
            ClientPaths.GENERAL_RESOURCES_PATH.extendPath(properties,
              ASSESSED_DATE)));
      th.appendChild(textValue);
      tr.appendChild(th);
      colIndex++;
    }
  }

  /**
   * Adds the needed columns for the table. Some columns are always displayed,
   * but some depend on configurations set in the data passed to the renderer.
   * The number of columns to be displayed is returned.
   * 
   * @param documentFragment
   * The document fragment for the renderer
   * @param assessmentFactorResultData
   * The data to be rendered
   * @param table
   * The table to add the columns to
   * @return The number of columns to be displayed
   */
  private int addTableColumns(final DocumentFragment documentFragment,
    final AssessmentFactorResultData assessmentFactorResultData,
    final Element table) {

    Element col;
    int numCols = 0;

    // optional column
    if (assessmentFactorResultData.getPrioritiesApplicableInd()) {
      col = documentFragment.getOwnerDocument().createElement("col");
      col.setAttribute("class", "field");
      col.setAttribute("width", "3%");
      table.appendChild(col);
      numCols++;
    }

    // optional column
    if (!assessmentFactorResultData.getGroupBasedInd()
      || assessmentFactorResultData.getDisplayClientsInd()) {
      col = documentFragment.getOwnerDocument().createElement("col");
      col.setAttribute("class", "field");
      table.appendChild(col);
      numCols++;
    }

    // factor always displayed
    col = documentFragment.getOwnerDocument().createElement("col");
    col.setAttribute("class", "field");
    table.appendChild(col);
    numCols++;

    // optional column
    if (assessmentFactorResultData.getMultipleCategoriesInd()) {
      col = documentFragment.getOwnerDocument().createElement("col");
      col.setAttribute("class", "field");
      table.appendChild(col);
      numCols++;
    }

    // classification always displayed
    col = documentFragment.getOwnerDocument().createElement("col");
    col.setAttribute("class", "field");
    table.appendChild(col);
    numCols++;

    // optional column
    if (assessmentFactorResultData.getScoringUsedInd()) {
      col = documentFragment.getOwnerDocument().createElement("col");
      col.setAttribute("class", "field");
      col.setAttribute("width", "13%");
      table.appendChild(col);
      numCols++;
    }

    if (assessmentFactorResultData.getDisplayAssessmentDateInd()) {
      col = documentFragment.getOwnerDocument().createElement("col");
      col.setAttribute("class", "field");
      col.setAttribute("width", "20%");
      table.appendChild(col);
      numCols++;
    }

    return numCols;
  }

  /**
   * Sets the class attribute on the given TR {@link Element}. This is based on
   * the column.
   * 
   * @param factorResultsList
   * The list of all results to be displayed
   * @param tr
   * The TR element to be displayed
   * @param rowIndex
   * The current row
   */
  protected void setTRClass(final List<FactorResultExt> factorResultsList,
    final Element tr, final int rowIndex) {

    String rowClass;
    if (rowIndex % 2 == 1) {
      rowClass = "odd";
    } else {
      rowClass = "even";
    }
    if (rowIndex == factorResultsList.size()) {
      rowClass = rowClass + "-last-row";
    }
    tr.setAttribute("class", rowClass);
  }

  /**
   * Sets the class attribute on the given TH {@link Element}. This is based on
   * the column.
   * 
   * @param colIndex
   * The current column
   * @param numColumnsToDisplay
   * The total number of columns
   * @param th
   * The TH element to be displayed
   */
  protected void setTHClass(final int colIndex,
    final int numColumnsToDisplay, final Element th) {

    if (colIndex == 1) {
      th.setAttribute("class", "field first-header");
    } else if (colIndex == numColumnsToDisplay) {
      th.setAttribute("class", "field last-header");
    } else {
      th.setAttribute("class", "field");
    }
  }

  /**
   * Sets the class attribute on the given TD {@link Element}. This is based on
   * the column.
   * 
   * @param colIndex
   * The current column
   * @param numColumnsToDisplay
   * The total number of columns
   * @param td
   * The TD element to be displayed
   */
  protected void setTDClass(final int colIndex,
    final int numColumnsToDisplay, final Element td) {

    if (colIndex == 1) {
      td.setAttribute("class", "field first-field");
    } else if (colIndex == numColumnsToDisplay) {
      td.setAttribute("class", "field last-field");
    } else {
      td.setAttribute("class", "field");
    }
  }

  /**
   * Calculates the number of columns that are to be displayed in the results
   * list. Factor and classification are always displayed, the rest are based on
   * configurations from administration.
   * 
   * @param assessmentFactorResultData
   * The result data
   * @return The number of columns to be displayed in the list
   */
  int calculateNumColsToDisplay(
    final AssessmentFactorResultData assessmentFactorResultData) {

    int numCols = 0;
    if (assessmentFactorResultData.getPrioritiesApplicableInd()) {
      numCols++;
    }
    if (!assessmentFactorResultData.getGroupBasedInd()
      || assessmentFactorResultData.getDisplayClientsInd()) {
      numCols++;
    }
    numCols++; // factor always displayed
    if (assessmentFactorResultData.getMultipleCategoriesInd()) {
      numCols++;
    }
    numCols++; // classification always displayed
    if (assessmentFactorResultData.getScoringUsedInd()) {
      numCols++;
    }
    if (assessmentFactorResultData.getDisplayAssessmentDateInd()) {
      numCols++;
    }

    return numCols;
  }

  private Element createElement(final DocumentFragment documentFragment,
    final String elementName) {

    final Element element =
      documentFragment.getOwnerDocument().createElement(elementName);

    return element;
  }

  private Node createTextNode(final DocumentFragment documentFragment,
    final String text) {

    final Node textElement =
      documentFragment.getOwnerDocument().createTextNode(text);

    return textElement;
  }

  /**
   * Retrieves the factor result label text. If label text has been passed into
   * this render through
   * {@link AssessmentFactorResultData#getFactorResultLabelText()} this is used
   * as the label, else the default property value within the property entry,
   * 'Field.Label.Factor' is used.
   * 
   * @param context
   * the renderer context to retrieve the property from is a factor
   * label text was not passed into the renderer
   * @param assessmentFactorResultData
   * the data passed into the renderer where the passed in factor label
   * text is to be retrieved from
   * @return The factor result label text to be displayed
   */
  private String getFactorResultLabelText(final RendererContext context,
    final AssessmentFactorResultData assessmentFactorResultData)
    throws DataAccessException {

    String factorResultLabelText =
      assessmentFactorResultData.getFactorResultLabelText();
    if (factorResultLabelText.isEmpty()) {
      factorResultLabelText =
        context.getDataAccessor().get(
          ClientPaths.GENERAL_RESOURCES_PATH.extendPath(properties, FACTOR));
    }
    return factorResultLabelText;
  }

  /**
   * Retrieves the classification result label text. If label text has been
   * passed into this render through
   * {@link AssessmentFactorResultData#getClassificationResultLabelText()} this
   * is used as the label, else the default property value within the property
   * entry, 'Field.Label.Classification' is used.
   * 
   * @param context
   * the renderer context to retrieve the property from is a
   * classification label text was not passed into the renderer
   * @param assessmentFactorResultData
   * the data passed into the renderer where the passed in
   * classification label text is to be retrieved from
   * @return The classification result label text to be displayed
   */
  private String getClassificationResultLabelText(
    final RendererContext context,
    final AssessmentFactorResultData assessmentFactorResultData)
    throws DataAccessException {

    String classificationResultLabelText =
      assessmentFactorResultData.getClassificationResultLabelText();
    if (classificationResultLabelText.isEmpty()) {
      classificationResultLabelText =
        context.getDataAccessor().get(
          ClientPaths.GENERAL_RESOURCES_PATH.extendPath(properties,
            CLASSIFICATION));
    }
    return classificationResultLabelText;
  }
}
