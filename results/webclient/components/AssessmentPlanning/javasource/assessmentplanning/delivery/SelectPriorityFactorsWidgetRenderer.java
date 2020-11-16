/*
 * Licensed Materials - Property of IBM
 *
 * PID 5725-H26
 *
 * Copyright IBM Corporation 2012, 2017. All rights reserved.
 *
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 */
/*
 * Copyright 2009 Curam Software Ltd.
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
import curam.util.client.domain.render.edit.AbstractEditRenderer;
import curam.util.client.model.Field;
import curam.util.client.model.FieldParameters;
import curam.util.client.path.util.ClientPaths;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Widget renderer for the Assessment Results. Priority factors are chosen from
 * here.
 */
public class SelectPriorityFactorsWidgetRenderer
  extends AbstractEditRenderer {

  private static final String kCOMMA_SPACE = ", ";

  private static final String kCLOSE_BRACKET_ROUND = ")";

  private static final String kTAB = "\t";

  private static final String kSPACE_OPEN_BRACKET_ROUND = " (";

  private static final String SCORE = "Text.Score";

  private static final String CLIENT = "Text.Client";

  private static final String CLIENTS = "Text.Clients";

  private static final String CLASSIFICATION = "Field.Label.Classification";

  private static final String FACTOR = "Field.Label.Factor";

  private static final String CURRENT_PRIORITIES = "Text.CurrentPriorities";

  private static final String SELECT_PRIORITIES_FROM_THE_LISTS_BELOW =
    "Text.SelectPrioritiesFromLists";

  private static final String LATEST_RESULTS_FOR_ALL_OTHER_FACTORS =
    "Text.LatestResultsOtherFactors";

  private static final String REASSESSMENT_RESULTS =
    "Text.ReassessmentResults";

  private String properties;

  private int numColumnsToDisplay;

  /**
   * Constructor. Sets up the properties file.
   */
  public SelectPriorityFactorsWidgetRenderer() {

    properties = new String();
    setPropertyFileLocation();
  }

  /**
   * Sets the location of the properties file to be used. *
   */
  private void setPropertyFileLocation() {

    properties =
      "assessmentplanning.delivery.SelectPriorityFactorsWidgetRenderer";
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
  private String getProperty(final RendererContext context, final String file,
    final String property) {

    try {
      return context.getDataAccessor()
        .get(ClientPaths.GENERAL_RESOURCES_PATH.extendPath(file, property));
    } catch (final DataAccessException exception) {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    @SuppressWarnings("unused")
    final RendererContract rendererContract) throws ClientException, DataAccessException, PlugInException {

    final Path sourcePath = field.getBinding().getSourcePath();
    final String value =
      (String) context.getDataAccessor().getRaw(sourcePath);
    final Document myDoc = XmlUtils.parseXmlText(value);
    context.includeScriptURIs("text/javascript",
      "../CDEJ/jscript/PriorityResults.js");

    // parse the XML
    final SelectPriorityFactorsWidgetData data =
      new SelectPriorityFactorsWidgetData(myDoc);
    final String label = field.getParameters().get(FieldParameters.LABEL);

    // add div with ID so I can find the parent field and change its style
    final Element div =
      documentFragment.getOwnerDocument().createElement("div");
    div.setAttribute("id", "priorityResultDiv");
    div.setAttribute("class", "outer-cluster-borderless-nospace");
    documentFragment.appendChild(div);

    if (data.isReassessment()) {
      addReassessmentTables(documentFragment, data, field, context, label);
    } else { // first assessment layout
      addAssessmentTables(field, documentFragment, context, data, label);
    }
  }

  /**
   * Creates the categories tables with the priority min and max descriptions
   * and results for all factors assessed.
   *
   * @param documentFragment
   * The document to add the data to
   * @param data
   * The data containing the assessment result information
   * @param field
   * The field
   * @param context
   * The renderer context
   * @param label
   * The label
   * @throws ClientException
   * @throws DataAccessException
   */
  private void addAssessmentTables(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final SelectPriorityFactorsWidgetData data, final String label)
    throws ClientException, DataAccessException {

    if (data.isPrioritiesApplicable()) {
      // create table with previous priorities, if any
      createPreviousPrioritiesTable(documentFragment, context, data);
    }

    // clients and roles
    if (displayRolesTable(data)) {
      createRolesTable(documentFragment, data.listRoles());
    }

    // priority min and max numbers
    if (data.isPrioritiesApplicable()) {
      final Element priorityDiv = setPriorityMinMaxDescriptions(
        documentFragment, data.listPriorityValidations());
      documentFragment.appendChild(priorityDiv);
    }

    final List<Category> categories = data.listCategories();

    addCategories(documentFragment, data, field, context, label, categories);
  }

  /**
   * Creates the previous priorities table if any previous priorities exist, add
   * the header, the reassessment results and the results from other factors if
   * any.
   *
   * @param documentFragment
   * The document to add the data to
   * @param data
   * The data containing the assessment result information
   * @param field
   * The field
   * @param context
   * The renderer context
   * @param label
   * The label
   * @throws ClientException
   * Generic Exception Signature
   * @throws DataAccessException
   * Generic Exception Signature
   */
  private void addReassessmentTables(final DocumentFragment documentFragment,
    final SelectPriorityFactorsWidgetData data, final Field field,
    final RendererContext context, final String label)
    throws ClientException, DataAccessException {

    if (data.isPrioritiesApplicable()) {
      // create table with previous priorities, if any
      createPreviousPrioritiesTable(documentFragment, context, data);

      // add the header
      addReassessmentMainHeader(documentFragment, context);
    }

    // clients and roles
    if (displayRolesTable(data)) {
      createRolesTable(documentFragment, data.listRoles());
    }

    // priority min and max numbers
    if (data.isPrioritiesApplicable()) {
      final Element priorityDiv = setPriorityMinMaxDescriptions(
        documentFragment, data.listPriorityValidations());
      documentFragment.appendChild(priorityDiv);
    }

    // add the reassessment results
    final List<Category> categories = data.listCategories();
    if (categories.size() > 0) {
      addReassessmentMinorHeader(documentFragment,
        getProperty(context, properties, REASSESSMENT_RESULTS));
    }

    addCategories(documentFragment, data, field, context, label, categories);

    // add the results for other factors
    final List<Category> otherCategories = data.listOtherCategories();
    if (otherCategories.size() > 0) {
      addReassessmentMinorHeader(documentFragment, getProperty(context,
        properties, LATEST_RESULTS_FOR_ALL_OTHER_FACTORS));
    }
    addCategories(documentFragment, data, field, context, label,
      otherCategories);

  }

  /**
   * Adds the tables required for categories using either multiple or single
   * category layout.
   *
   * @param documentFragment
   * The document fragment
   * @param data
   * The priority factors data
   * @param field
   * The field being updated
   * @param context
   * The renderer context
   * @param label
   * The label parameter
   * @param categories
   * The list of categories to be displayed
   * @throws ClientException
   * Generic Exception Signature
   * @throws DataAccessException
   * Generic Exception Signature
   */
  private void addCategories(final DocumentFragment documentFragment,
    final SelectPriorityFactorsWidgetData data, final Field field,
    final RendererContext context, final String label,
    final List<Category> categories)
    throws ClientException, DataAccessException {

    if (data.getDisplayResultsByCategoryInd() || categories.size() > 1) {
      for (final Category category : categories) {
        // display in lists of categories
        createCategoryListLayout(field, documentFragment, context, label,
          category, data);
      }
    } else if (categories.size() == 1) {
      final Category category = categories.get(0);
      createSingleCategoryLayout(field, documentFragment, context, label,
        category, data);
    }
  }

  private boolean
    displayRolesTable(final SelectPriorityFactorsWidgetData data) {

    return data.isAbbreviationMissing() && !data.isGroupBasedAssessment();
  }

  /**
   * Adds one of the list headers for the reassessment lists.
   *
   * @param documentFragment
   * The document to add the data to
   * @param headerText
   * The header text to display
   */
  private void addReassessmentMinorHeader(
    final DocumentFragment documentFragment, final String headerText) {

    final Element div =
      documentFragment.getOwnerDocument().createElement("div");
    div.setAttribute("class",
      "outer-cluster-borderless-nospace cluster-transparent");

    final Element h2 =
      documentFragment.getOwnerDocument().createElement("h2");
    h2.setAttribute("class", "stand-alone");
    final Node text =
      documentFragment.getOwnerDocument().createTextNode(headerText);
    insertSpanOrTextNode(h2, text);
    div.appendChild(h2);
    documentFragment.appendChild(div);
  }

  /**
   * Adds the main header for the reassessment lists.
   *
   * @param documentFragment
   * The document to add the data to
   * @param context
   * The document context
   */
  private void addReassessmentMainHeader(
    final DocumentFragment documentFragment, final RendererContext context) {

    final Element div =
      documentFragment.getOwnerDocument().createElement("div");
    div.setAttribute("class",
      "outer-cluster-borderless-nospace cluster-transparent");

    final Element br =
      documentFragment.getOwnerDocument().createElement("br");
    div.appendChild(br);

    final Element h1 =
      documentFragment.getOwnerDocument().createElement("h1");
    final Node text =
      documentFragment.getOwnerDocument().createTextNode(getProperty(context,
        properties, SELECT_PRIORITIES_FROM_THE_LISTS_BELOW));
    insertSpanOrTextNode(h1, text);
    div.appendChild(h1);
    documentFragment.appendChild(div);
  }

  /**
   * Creates the previous priorities table if any previous priorities exist.
   *
   * @param documentFragment
   * The document to add the data to
   * @param context
   * The renderer context
   * @param data
   * The data containing the assessment result information
   * @throws DataAccessException
   */
  private void createPreviousPrioritiesTable(
    final DocumentFragment documentFragment, final RendererContext context,
    final SelectPriorityFactorsWidgetData data) throws DataAccessException {

    if (data.listPreviousPrioritiesFromOutcomePlan().size() == 0) {
      return;
    }

    final Element div1 =
      documentFragment.getOwnerDocument().createElement("div");
    div1.setAttribute("class", "list-nospace list list-with-header collapse");
    div1.setAttribute("style", "margin-bottom: 6px;");
    div1.setAttribute("onclick", "toggleCluster(this,arguments[0]);");
    documentFragment.appendChild(div1);

    final Element headerDiv =
      documentFragment.getOwnerDocument().createElement("div");
    headerDiv.setAttribute("class", "header-wrapper");
    div1.appendChild(headerDiv);

    final Element div =
      documentFragment.getOwnerDocument().createElement("div");
    div.setAttribute("class", "scrollable factor-result-list");
    div.setAttribute("style", "MAX-HEIGHT: 300px");
    div1.appendChild(div);

    final Element h2 =
      documentFragment.getOwnerDocument().createElement("h2");
    h2.setAttribute("class", "collapse");
    final Node text = documentFragment.getOwnerDocument()
      .createTextNode(getProperty(context, properties, CURRENT_PRIORITIES));
    insertSpanOrTextNode(h2, text);
    headerDiv.appendChild(h2);

    final Element table =
      documentFragment.getOwnerDocument().createElement("table");
    table.setAttribute("cellspacing", "0");
    div.appendChild(table);

    final int totalNumCols = 2; //
    addStandardCol(documentFragment, table);
    addStandardCol(documentFragment, table);

    final Element thead =
      documentFragment.getOwnerDocument().createElement("thead");
    table.appendChild(thead);

    final Element tr =
      documentFragment.getOwnerDocument().createElement("tr");
    thead.appendChild(tr);

    int colIndex = 1;
    // factor column using generic title as factors are from plan
    numColumnsToDisplay = totalNumCols;
    final String factorColText = context.getDataAccessor()
      .get(ClientPaths.GENERAL_RESOURCES_PATH.extendPath(properties, FACTOR));
    final Node text1 =
      documentFragment.getOwnerDocument().createTextNode(factorColText);
    Element th = documentFragment.getOwnerDocument().createElement("th");
    setTHClass(colIndex, th, "");
    insertSpanOrTextNode(th, text1);
    tr.appendChild(th);
    colIndex++;

    Node colText = null;
    if (!data.getCountSameScoreAsOnePriorityInd()) {
      colText = documentFragment.getOwnerDocument()
        .createTextNode(getProperty(context, properties, CLIENT));
    } else {
      colText = documentFragment.getOwnerDocument()
        .createTextNode(getProperty(context, properties, CLIENTS));
    }
    th = documentFragment.getOwnerDocument().createElement("th");
    setTHClass(colIndex, th, "");
    insertSpanOrTextNode(th, colText);
    tr.appendChild(th);
    colIndex++;

    final Element tbody =
      documentFragment.getOwnerDocument().createElement("tbody");
    table.appendChild(tbody);

    addOutcomePlanPreviousPriorities(documentFragment, data, tbody);

  }

  /**
   * Add the previous priorities for an outcome plan.
   *
   * @param documentFragment
   * The document fragment in use
   * @param data
   * The results data
   * @param tbody
   * The table body to add the priority rows to
   */
  void addOutcomePlanPreviousPriorities(
    final DocumentFragment documentFragment,
    final SelectPriorityFactorsWidgetData data, final Element tbody) {

    Element tr;
    int rowIndex = 1;
    for (final String previousPriority : data
      .listPreviousPrioritiesFromOutcomePlan()) {

      final String[] previousPrioritySplit = previousPriority.split("\t");

      tr = documentFragment.getOwnerDocument().createElement("tr");
      setTRClass(tr, rowIndex);
      tbody.appendChild(tr);

      int columnIndex = 1;
      for (int i = 0; i < previousPrioritySplit.length; i++) {
        final Element td =
          documentFragment.getOwnerDocument().createElement("td");
        setTDClass(columnIndex, td, "");
        final Node text2 = documentFragment.getOwnerDocument()
          .createTextNode(previousPrioritySplit[i]);
        insertSpanOrTextNode(td, text2);
        tr.appendChild(td);
        columnIndex++;
      }
      rowIndex++;
    }

    // set the style for the last row
    final Element lastTR = (Element) tbody.getLastChild();
    String lastRowStyle =
      lastTR.getAttributes().getNamedItem("class").getNodeValue();
    lastRowStyle = lastRowStyle + "-last-row";
    lastTR.setAttribute("class", lastRowStyle);
  }

  /**
   * Creates the table with the details for a classification when there is only
   * one category.
   *
   * @param field
   * The containing field
   * @param documentFragment
   * The document to add the data to
   * @param context
   * The renderer context
   * @param label
   * The label parameter
   * @param data
   * The data containing the assessment result information
   * @param classification
   * The classification we are dealing with
   * @throws ClientException
   * @throws DataAccessException
   */
  private void createSingleCategoryLayout(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final String label, final Category category,
    final SelectPriorityFactorsWidgetData data)
    throws ClientException, DataAccessException {

    final Element div =
      documentFragment.getOwnerDocument().createElement("div");
    div.setAttribute("class",
      "outer-cluster-borderless-nospace cluster-transparent");
    documentFragment.appendChild(div);

    final Element table =
      documentFragment.getOwnerDocument().createElement("table");
    table.setAttribute("class", "inner-cluster-no-top-right-left-margin");
    div.appendChild(table);

    addStandardCol(documentFragment, table);
    addStandardCol(documentFragment, table);

    final Element tbody =
      documentFragment.getOwnerDocument().createElement("tbody");
    table.appendChild(tbody);

    int classificationCount = 0;
    Element tr = null;
    Element td;
    for (final Classification classification : category
      .getClassifications()) {
      if (classificationCount % 2 == 0) {
        tr = documentFragment.getOwnerDocument().createElement("tr");
        tbody.appendChild(tr);
      }
      td = documentFragment.getOwnerDocument().createElement("td");
      if (classificationCount % 2 == 0) {
        td.setAttribute("class",
          "vertically-aligned-field right-padded-field");
      } else {
        td.setAttribute("class",
          "vertically-aligned-field left-padded-field");
      }

      final Element classificationsDiv = createClassificationList(field,
        documentFragment, context, label, classification, data);
      td.appendChild(classificationsDiv);
      tr.appendChild(td);

      classificationCount++;
    }
  }

  /**
   * Adds the classification and factor result table rows.
   *
   * @param field
   * The containing field
   * @param documentFragment
   * The document to add the data to
   * @param context
   * The renderer context
   * @param label
   * The label parameter
   * @param tbody
   * The body of the table the row is to be added to
   * @param classification
   * The classification we are dealing with
   * @param rowNum
   * The current row number
   * @param data
   * The data containing the assessment result information
   * @throws ClientException
   */
  private int createClassificationRows(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final String label, final Element tbody,
    final Classification classification, final int rowNum,
    final SelectPriorityFactorsWidgetData data) throws ClientException {

    int currentRowNum = rowNum;
    final List<FactorResult> processedFactorResultList =
      new ArrayList<FactorResult>();

    final List<FactorResult> factorResults =
      classification.getFactorResults();
    Collections.sort(factorResults, new FactorResultComparator());

    for (final FactorResult factorResult : factorResults) {
      if (!processedFactorResultList.contains(factorResult)) {
        createFactorRow(field, documentFragment, context, label, tbody,
          currentRowNum, factorResult, classification, data,
          processedFactorResultList, classification.getFactorResults());
        processedFactorResultList.add(factorResult);
        currentRowNum++;
      }
    }
    return currentRowNum;
  }

  /**
   * Creates the roles table to hold the details of the clients and their roles.
   *
   * @param documentFragment
   * The document to add the data to
   * @param roles
   * The list of roles for the assessment
   */
  private void createRolesTable(final DocumentFragment documentFragment,
    final List<AssessmentDeliveryRole> roles) {

    final Element div =
      documentFragment.getOwnerDocument().createElement("div");
    div.setAttribute("class",
      "outer-cluster-borderless-nospace cluster-transparent");
    final Element table =
      documentFragment.getOwnerDocument().createElement("table");
    table.setAttribute("cellspacing", "0");
    div.appendChild(table);

    final Element col =
      documentFragment.getOwnerDocument().createElement("col");
    table.appendChild(col);

    final Element tbody =
      documentFragment.getOwnerDocument().createElement("tbody");
    table.appendChild(tbody);

    final Element tr =
      documentFragment.getOwnerDocument().createElement("tr");
    tbody.appendChild(tr);

    final Element td =
      documentFragment.getOwnerDocument().createElement("td");
    td.setAttribute("class", "bold-field field");

    int roleCount = 0;
    for (final AssessmentDeliveryRole role : roles) {
      roleCount++;
      final String nameAndRole =
        getClientNameAndRoleString(roles, roleCount, role);
      final Text text =
        documentFragment.getOwnerDocument().createTextNode(nameAndRole);
      insertSpanOrTextNode(td, text);
    }
    tr.appendChild(td);

    documentFragment.appendChild(div);
  }

  /**
   * Returns the details of the client name and role. Appends a comma and space
   * to the end of the string if there are more roles to be processed.
   *
   * @param roles
   * The list of all client roles
   * @param roleCount
   * The current role number
   * @param role
   * The current role
   * @return The details of the client name and role
   */
  private String getClientNameAndRoleString(
    final List<AssessmentDeliveryRole> roles, final int roleCount,
    final AssessmentDeliveryRole role) {

    String nameAndRole = role.getClientName() + kSPACE_OPEN_BRACKET_ROUND
      + role.getRoleType() + kCLOSE_BRACKET_ROUND;
    if (roleCount < roles.size()) {
      nameAndRole = nameAndRole + kCOMMA_SPACE;
    }
    return nameAndRole;
  }

  /**
   * Creates the table with the details for a classification.
   *
   * @param field
   * The containing field
   * @param documentFragment
   * The document to add the data to
   * @param context
   * The renderer context
   * @param label
   * The label parameter
   * @param classification
   * The classification we are dealing with
   * @param data
   * The data containing the assessment result information
   * @return The classifications element
   * @throws ClientException
   * @throws DataAccessException
   */
  private Element createClassificationList(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final String label, final Classification classification,
    final SelectPriorityFactorsWidgetData data)
    throws ClientException, DataAccessException {

    final Element div1 =
      documentFragment.getOwnerDocument().createElement("div");
    div1.setAttribute("class", "list list-with-header");

    final Element div2 =
      documentFragment.getOwnerDocument().createElement("div");
    div2.setAttribute("style", "zoom: 1;");
    div1.appendChild(div2);

    final Element headerDiv =
      documentFragment.getOwnerDocument().createElement("div");
    headerDiv.setAttribute("class", "header-wrapper");
    div2.appendChild(headerDiv);

    final Element h2 =
      documentFragment.getOwnerDocument().createElement("h2");
    h2.setAttribute("title", classification.getType());
    headerDiv.appendChild(h2);

    final Element span1 =
      documentFragment.getOwnerDocument().createElement("span");
    span1.setAttribute("class", "collapse-title");
    h2.appendChild(span1);

    final Node spanText1 = documentFragment.getOwnerDocument()
      .createTextNode(classification.getType());
    insertSpanOrTextNode(span1, spanText1);

    final Element div =
      documentFragment.getOwnerDocument().createElement("div");
    div.setAttribute("class", "scrollable factor-result-list");
    div.setAttribute("style", "MAX-HEIGHT: 300px");
    div1.appendChild(div);

    final Element table =
      documentFragment.getOwnerDocument().createElement("table");
    table.setAttribute("cellspacing", "0");
    div.appendChild(table);

    if (data.isPrioritiesApplicable()) {
      final Element col =
        documentFragment.getOwnerDocument().createElement("col");
      col.setAttribute("class", "multiselect");
      table.appendChild(col);
    }

    // client col
    if (!data.isGroupBasedAssessment()) {
      final Element col =
        documentFragment.getOwnerDocument().createElement("col");
      col.setAttribute("class", "field");
      col.setAttribute("width", "35%");
      table.appendChild(col);
    }

    addStandardCol(documentFragment, table);

    if (data.isScoringUsed()) {
      final Element col =
        documentFragment.getOwnerDocument().createElement("col");
      col.setAttribute("class", "field");
      table.appendChild(col);
      col.setAttribute("width", "24%");
    }

    final Element thead =
      documentFragment.getOwnerDocument().createElement("thead");
    table.appendChild(thead);

    Element tr = documentFragment.getOwnerDocument().createElement("tr");
    thead.appendChild(tr);
    int colIndex = 1;
    numColumnsToDisplay =
      calculateNumColsToDisplayForClassificationList(data);
    if (data.isPrioritiesApplicable()) {
      // checkbox is the first header
      final Element th =
        documentFragment.getOwnerDocument().createElement("th");
      setTHClass(colIndex, th, "multiselect");
      tr.appendChild(th);
      colIndex++;
    }

    if (!data.isGroupBasedAssessment()) {
      Node colText = null;
      if (!data.getCountSameScoreAsOnePriorityInd()) {
        colText = documentFragment.getOwnerDocument()
          .createTextNode(getProperty(context, properties, CLIENT));
      } else {
        colText = documentFragment.getOwnerDocument()
          .createTextNode(getProperty(context, properties, CLIENTS));
      }
      final Element th =
        documentFragment.getOwnerDocument().createElement("th");
      setTHClass(colIndex, th, "");
      insertSpanOrTextNode(th, colText);
      tr.appendChild(th);
      colIndex++;
    }

    final Node text1 = documentFragment.getOwnerDocument()
      .createTextNode(getFactorDisplayText(context, data));
    Element th = documentFragment.getOwnerDocument().createElement("th");
    setTHClass(colIndex, th, "");
    insertSpanOrTextNode(th, text1);
    tr.appendChild(th);
    colIndex++;

    if (data.isScoringUsed()) {
      final Node text2 = documentFragment.getOwnerDocument()
        .createTextNode(getProperty(context, properties, SCORE));
      th = documentFragment.getOwnerDocument().createElement("th");
      setTHClass(colIndex, th, "");
      insertSpanOrTextNode(th, text2);
      tr.appendChild(th);
      colIndex++;
    }

    final Element tbody =
      documentFragment.getOwnerDocument().createElement("tbody");
    table.appendChild(tbody);

    tr = documentFragment.getOwnerDocument().createElement("tr");
    tr.setAttribute("class", "odd");
    tbody.appendChild(tr);

    int rowNum = 1;
    final List<FactorResult> processedFactorResultList =
      new ArrayList<FactorResult>();

    final List<FactorResult> factorResults =
      classification.getFactorResults();
    Collections.sort(factorResults, new FactorResultComparator());

    for (final FactorResult factorResult : factorResults) {
      if (!processedFactorResultList.contains(factorResult)) {
        createFactorRow(field, documentFragment, context, label, tbody,
          rowNum, factorResult, null, data, processedFactorResultList,
          classification.getFactorResults());
        processedFactorResultList.add(factorResult);
        rowNum++;
      }

    }

    // set the style for the last row
    final Element lastTR = (Element) tbody.getLastChild();
    String lastRowStyle =
      lastTR.getAttributes().getNamedItem("class").getNodeValue();
    lastRowStyle = lastRowStyle + "-last-row";
    lastTR.setAttribute("class", lastRowStyle);
    return div1;
  }

  /**
   * Creates the table with the details for a classification.
   *
   * @param field
   * The containing field
   * @param documentFragment
   * The document to add the data to
   * @param context
   * The renderer context
   * @param label
   * The label parameter
   * @param data
   * The data containing the assessment result information
   * @param classification
   * The classification we are dealing with
   * @throws ClientException
   * @throws
   */
  private void createCategoryListLayout(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final String label, final Category category,
    final SelectPriorityFactorsWidgetData data)
    throws ClientException, DataAccessException {

    if (category.getClassifications().size() == 0) {
      return;
    }

    final Element div1 =
      documentFragment.getOwnerDocument().createElement("div");
    div1.setAttribute("class", "list-nospace list list-with-header");
    documentFragment.appendChild(div1);

    final Element div2 =
      documentFragment.getOwnerDocument().createElement("div");
    div2.setAttribute("style", "zoom: 1;");
    div1.appendChild(div2);

    final Element headerDiv =
      documentFragment.getOwnerDocument().createElement("div");
    headerDiv.setAttribute("class", "header-wrapper");
    div2.appendChild(headerDiv);

    final Element h2 =
      documentFragment.getOwnerDocument().createElement("h2");
    h2.setAttribute("title", category.getName());
    headerDiv.appendChild(h2);

    final Element span1 =
      documentFragment.getOwnerDocument().createElement("span");
    span1.setAttribute("class", "collapse-title");
    h2.appendChild(span1);

    final Node spanText1 =
      documentFragment.getOwnerDocument().createTextNode(category.getName());
    insertSpanOrTextNode(span1, spanText1);

    // Removed this span as there is no need for the toggle and it was causing
    // issues as the span didn't have any content
    /*
     * final Element span2 = documentFragment.getOwnerDocument().createElement(
     * "span"); span2.setAttribute("tabIndex", "0"); span2.setAttribute("title",
     * getProperty(context, properties, TOGGLE)); span2.setAttribute("class",
     * "grouptoggleArrow"); h2.appendChild(span2); final Node spanText2 =
     * documentFragment.getOwnerDocument().createTextNode( "");
     * span2.appendChild(spanText2);
     */

    final Element div =
      documentFragment.getOwnerDocument().createElement("div");
    div.setAttribute("class", "scrollable factor-result-list");
    div.setAttribute("style", "MAX-HEIGHT: 300px");
    div1.appendChild(div);

    final Element table =
      documentFragment.getOwnerDocument().createElement("table");
    table.setAttribute("cellspacing", "0");
    div.appendChild(table);

    if (data.isPrioritiesApplicable()) {
      final Element col =
        documentFragment.getOwnerDocument().createElement("col");
      col.setAttribute("class", "multiselect");
      table.appendChild(col);
    }

    // client col
    if (!data.isGroupBasedAssessment()) {
      addStandardCol(documentFragment, table);
    }
    addStandardCol(documentFragment, table);
    addStandardCol(documentFragment, table);

    if (data.isScoringUsed()) {
      final Element col =
        documentFragment.getOwnerDocument().createElement("col");
      col.setAttribute("class", "field");
      table.appendChild(col);
      col.setAttribute("width", "24%");
    }

    final Element thead =
      documentFragment.getOwnerDocument().createElement("thead");
    table.appendChild(thead);

    final Element tr =
      documentFragment.getOwnerDocument().createElement("tr");
    thead.appendChild(tr);

    int colIndex = 1;
    numColumnsToDisplay = calculateNumColsToDisplayForCategoryList(data);
    if (data.isPrioritiesApplicable()) {
      final Element th =
        documentFragment.getOwnerDocument().createElement("th");
      setTHClass(colIndex, th, "multiselect");
      tr.appendChild(th);
      colIndex++;
    }

    if (!data.isGroupBasedAssessment()) {
      Node colText = null;
      if (!data.getCountSameScoreAsOnePriorityInd()) {
        colText = documentFragment.getOwnerDocument()
          .createTextNode(getProperty(context, properties, CLIENT));
      } else {
        colText = documentFragment.getOwnerDocument()
          .createTextNode(getProperty(context, properties, CLIENTS));
      }
      final Element th =
        documentFragment.getOwnerDocument().createElement("th");
      setTHClass(colIndex, th, "");
      insertSpanOrTextNode(th, colText);
      tr.appendChild(th);
      colIndex++;
    }

    final Node text1 = documentFragment.getOwnerDocument()
      .createTextNode(getClassificationDisplayText(context, data));
    Element th = documentFragment.getOwnerDocument().createElement("th");
    setTHClass(colIndex, th, "");
    insertSpanOrTextNode(th, text1);
    tr.appendChild(th);
    colIndex++;

    final Node text2 = documentFragment.getOwnerDocument()
      .createTextNode(getFactorDisplayText(context, data));
    th = documentFragment.getOwnerDocument().createElement("th");
    setTHClass(colIndex, th, "");
    insertSpanOrTextNode(th, text2);
    tr.appendChild(th);
    colIndex++;

    if (data.isScoringUsed()) {
      final Node text3 = documentFragment.getOwnerDocument()
        .createTextNode(getProperty(context, properties, SCORE));
      th = documentFragment.getOwnerDocument().createElement("th");
      setTHClass(colIndex, th, "");
      insertSpanOrTextNode(th, text3);
      tr.appendChild(th);
      colIndex++;
    }

    final Element tbody =
      documentFragment.getOwnerDocument().createElement("tbody");
    table.appendChild(tbody);

    int rowNum = 1;
    for (final Classification classification : category
      .getClassifications()) {
      rowNum = createClassificationRows(field, documentFragment, context,
        label, tbody, classification, rowNum, data);
    }

    // set the style for the last row
    final Element lastTR = (Element) tbody.getLastChild();
    String lastRowStyle =
      lastTR.getAttributes().getNamedItem("class").getNodeValue();
    lastRowStyle = lastRowStyle + "-last-row";
    lastTR.setAttribute("class", lastRowStyle);
  }

  /**
   * Adds a standard table column.
   *
   * @param documentFragment
   * The document to hold the data
   * @param table
   * The table to add the column to
   */
  private void addStandardCol(final DocumentFragment documentFragment,
    final Element table) {

    final Element col =
      documentFragment.getOwnerDocument().createElement("col");
    col.setAttribute("class", "field");
    table.appendChild(col);
  }

  /**
   * Adds the factor result table row data.
   *
   * @param field
   * The containing field
   * @param documentFragment
   * The document to add the data to
   * @param context
   * The renderer context
   * @param label
   * The label parameter
   * @param tbody
   * The body of the table the row is to be added to
   * @param rowNum
   * The current row number
   * @param factorResult
   * The factor result we are dealing with
   * @param classification
   * If this is not null, add the classification name to the row
   * @param data
   * The data containing the assessment result information
   * @param processedFactorResultList
   * List of factorResults that have previously being processed for
   * display
   * @param factorResults
   * List of all factor results that exist for the classification the
   * passed in factor result is in relation to
   * @throws ClientException
   */
  private void createFactorRow(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final String label, final Element tbody, final int rowNum,
    final FactorResult factorResult, final Classification classification,
    final SelectPriorityFactorsWidgetData data,
    final List<FactorResult> processedFactorResultList,
    final List<FactorResult> factorResults) throws ClientException {

    Element tr;
    String targetID = "";
    String tabbedTargetID = factorResult.getId();
    if (!data.isGroupBasedAssessment()) {
      if (!data.getCountSameScoreAsOnePriorityInd()) {
        targetID =
          context.addFormItem(field, label, factorResult.getId(), false);
      } else {

        for (final FactorResult factorResultItem : factorResults) {
          /*
           * if scores are the same, and for the same factor, add to the client
           * string, and process list
           */
          if (!factorResultItem.getId().equals(factorResult.getId())
            && factorResultItem.getScore().equals(factorResult.getScore())
            && factorResult.getFactor()
              .equals(factorResultItem.getFactor())) {

            tabbedTargetID += kTAB + factorResultItem.getId();

          }
        }
        targetID = context.addFormItem(field, label, tabbedTargetID, false);
      }
    } else {
      targetID =
        context.addFormItem(field, label, factorResult.getId(), false);
    }

    // add factor rows
    int colIndex = 1;
    tr = documentFragment.getOwnerDocument().createElement("tr");
    setTRClass(tr, rowNum);
    tbody.appendChild(tr);

    if (data.isPrioritiesApplicable()) {

      colIndex = addPrioritiesCheckbox(field, documentFragment, context,
        label, factorResult, data, tr, targetID, tabbedTargetID, colIndex);
    }

    // client data
    colIndex = addFactorClientData(documentFragment, factorResult, data,
      processedFactorResultList, factorResults, tr, colIndex);

    // classification
    if (classification != null) {
      final Element td =
        documentFragment.getOwnerDocument().createElement("td");
      setTDClass(colIndex, td, "padded-field");
      final Node text = documentFragment.getOwnerDocument()
        .createTextNode(classification.getType());
      insertSpanOrTextNode(td, text);
      tr.appendChild(td);
      colIndex++;
    }
    final Element td1 =
      documentFragment.getOwnerDocument().createElement("td");
    setTDClass(colIndex, td1, "padded-field");
    final Node text1 = documentFragment.getOwnerDocument()
      .createTextNode(factorResult.getFactor());
    insertSpanOrTextNode(td1, text1);
    tr.appendChild(td1);
    colIndex++;

    if (data.isScoringUsed()) {
      final Element td =
        documentFragment.getOwnerDocument().createElement("td");
      setTDClass(colIndex, td, "score-field");
      final Node text = documentFragment.getOwnerDocument()
        .createTextNode(factorResult.getScore());
      insertSpanOrTextNode(td, text);
      tr.appendChild(td);
      colIndex++;
    }
  }

  /**
   * Adds the client data to a factor row.
   *
   * @param documentFragment
   * The document fragment
   * @param factorResult
   * The current factor result
   * @param data
   * The priority factor data
   * @param processedFactorResultList
   * The list of processed factor results
   * @param factorResults
   * The list of all factor results
   * @param tr
   * The current row
   * @param colIndex
   * The current column index
   * @return The new column index
   */
  private int addFactorClientData(final DocumentFragment documentFragment,
    final FactorResult factorResult,
    final SelectPriorityFactorsWidgetData data,
    final List<FactorResult> processedFactorResultList,
    final List<FactorResult> factorResults, final Element tr,
    final int colIndex) {

    if (data.isGroupBasedAssessment()) {
      return colIndex; // no cols added
    }

    if (!data.getCountSameScoreAsOnePriorityInd()) {
      final Element td =
        documentFragment.getOwnerDocument().createElement("td");
      setTDClass(colIndex, td, "padded-field");
      final Node text = documentFragment.getOwnerDocument()
        .createTextNode(getClientDetails(factorResult, data));
      insertSpanOrTextNode(td, text);
      tr.appendChild(td);
      return colIndex + 1;
    } else {

      // TODO refactor- poh similar to other code
      /*
       * logic is, if here the current factor result is not in the processed
       * list. Need to go through all the other factor results to try identify
       * if there are any out there with the same score and they are for the
       * same factor. If one is found retrieve the client details from the
       * factor and add to the client list details that will be placed into the
       * column. In addition, the result is to be also added to the processed
       * factor result list.
       */
      String clientList = getClientDetails(factorResult, data);
      for (final FactorResult factorResultItem : factorResults) {
        // if scores are the same, and for the same factor, add to
        // the client
        // string, and process list
        if (!factorResultItem.getId().equals(factorResult.getId())
          && factorResultItem.getScore().equals(factorResult.getScore())
          && factorResult.getFactor().equals(factorResultItem.getFactor())) {
          clientList +=
            kCOMMA_SPACE + getClientDetails(factorResultItem, data);
          processedFactorResultList.add(factorResultItem);
        }
      }
      final Element td =
        documentFragment.getOwnerDocument().createElement("td");
      setTDClass(colIndex, td, "padded-field");
      final Node text =
        documentFragment.getOwnerDocument().createTextNode(clientList);
      insertSpanOrTextNode(td, text);
      tr.appendChild(td);
      return colIndex + 1;
    }

  }

  /**
   * Adds the check box and all related elements for priority selection.
   *
   * @param field
   * The field to be updated
   * @param documentFragment
   * The document fragment
   * @param context
   * The renderer context
   * @param label
   * The label parameter
   * @param factorResult
   * The current factor result
   * @param data
   * The priority and factors data
   * @param tr
   * The current row
   * @param targetID
   * The target ID for the check box
   * @param tabbedTargetID
   * The tabbed target ID
   * @param colIndex
   * The current column index
   * @return The new column index
   * @throws ClientException
   * Generic Exception Signature
   */
  private int addPrioritiesCheckbox(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final String label, final FactorResult factorResult,
    final SelectPriorityFactorsWidgetData data, final Element tr,
    final String targetID, final String tabbedTargetID, final int colIndex)
    throws ClientException {

    // check for previous value (in case of validation errors)
    final String checkBoxHiddenTargetID = context.addFormItem(field, label,
      "CheckBox," + factorResult.getId(), false);
    final boolean checked = isCheckBoxPreviouslySelected(field, context,
      "CheckBox," + factorResult.getId());

    final Element td =
      documentFragment.getOwnerDocument().createElement("td");
    setTDClass(colIndex, td, "multiselect");
    td.setAttribute("selected", "true");
    final Element input =
      documentFragment.getOwnerDocument().createElement("input");
    input.setAttribute("type", "checkbox");
    input.setAttribute("id", targetID);
    input.setAttribute("name", targetID);
    input.setAttribute("value", tabbedTargetID);
    input.setAttribute("onclick",
      "updatedSelected(" + checkBoxHiddenTargetID + ");");

    // determine if the attribute should be checked or not.
    final boolean resultCurrentlySelectedAsPriority =
      resultCurrentlySelectedAsPriority(
        data.listCurrentlySelectedPotentialPriorityCategories(),
        tabbedTargetID);
    if (resultCurrentlySelectedAsPriority || checked) {
      input.setAttribute("checked", "yes");
    }
    td.appendChild(input);

    // Hidden input to indicate if the Checkbox has been selected.
    final Element checkBoxHiddenInput =
      documentFragment.getOwnerDocument().createElement("input");
    checkBoxHiddenInput.setAttribute("type", "hidden");
    checkBoxHiddenInput.setAttribute("id", checkBoxHiddenTargetID);
    checkBoxHiddenInput.setAttribute("name", checkBoxHiddenTargetID);
    checkBoxHiddenInput.setAttribute("value", "false");
    checkBoxHiddenInput.setAttribute("selected", "false");

    if (resultCurrentlySelectedAsPriority || checked) {
      checkBoxHiddenInput.setAttribute("value", "true");
      checkBoxHiddenInput.setAttribute("selected", "true");
    }
    td.appendChild(checkBoxHiddenInput);

    tr.appendChild(td);
    return colIndex + 1;
  }

  /**
   * Indicates if the check box has been previously selected. This may be the
   * case if the form is being redisplayed due to validation errors.
   *
   * @param field
   * The field
   * @param context
   * The current renderer context
   * @param targetID
   * The corresponding to the check box
   * @throws ClientException
   */
  private boolean isCheckBoxPreviouslySelected(final Field field,
    final RendererContext context, final String targetID)
    throws ClientException {

    try {
      return Boolean
        .valueOf(context.getFormItemInitialValue(field, true, targetID));
    } catch (final DataAccessException e) {
      // nothing needed
    }
    return false;
  }

  /**
   * Sets the class attribute on the given TR {@link Element}. This is based on
   * the column.
   *
   * @param tr
   * The TR element to be displayed
   * @param rowIndex
   * The current row
   */
  protected void setTRClass(final Element tr, final int rowIndex) {

    String rowClass;
    if (rowIndex % 2 == 1) {
      rowClass = "odd";
    } else {
      rowClass = "even";
    }
    tr.setAttribute("class", rowClass);
  }

  /**
   * Sets the class attribute on the given TH {@link Element}. This is based on
   * the column.
   *
   * @param colIndex
   * The current column
   * @param th
   * The TH element to be displayed
   * @param extraStyle
   * Any additional styles to be applied to the field
   */
  protected void setTHClass(final int colIndex, final Element th,
    final String extraStyle) {

    if (colIndex == 1) {
      th.setAttribute("class", extraStyle + " field first-header");
    } else if (colIndex == numColumnsToDisplay) {
      th.setAttribute("class", extraStyle + " field last-header");
    } else {
      th.setAttribute("class", extraStyle + " field");
    }
  }

  /**
   * Sets the class attribute on the given TD {@link Element}. This is based on
   * the column.
   *
   * @param colIndex
   * The current column
   * @param td
   * The TD element to be displayed
   * @param extraStyle
   * Any additional styles to be applied to the field
   */
  protected void setTDClass(final int colIndex, final Element td,
    final String extraStyle) {

    if (colIndex == 1) {
      td.setAttribute("class", extraStyle + " field first-field");
    } else if (colIndex == numColumnsToDisplay) {
      td.setAttribute("class", extraStyle + " field last-field");
    } else {
      td.setAttribute("class", extraStyle + " field");
    }
  }

  /**
   * Calculates the number of columns that are to be displayed in the results
   * list. Factor and classification are always displayed, the rest are based on
   * configurations from administration.
   *
   * @param data
   * The result data
   * @return The number of columns to be displayed in the list
   */
  protected int calculateNumColsToDisplayForClassificationList(
    final SelectPriorityFactorsWidgetData data) {

    int numCols = 0;
    if (data.isPrioritiesApplicable()) {
      numCols++;
    }
    if (!data.isGroupBasedAssessment()) {
      numCols++;
    }
    numCols++; // factor is always displayed
    if (data.isScoringUsed()) {
      numCols++;
    }

    return numCols;
  }

  /**
   * Calculates the number of columns that are to be displayed in the results
   * list. Factor and classification are always displayed, the rest are based on
   * configurations from administration.
   *
   * @param data
   * The result data
   * @return The number of columns to be displayed in the list
   */
  protected int calculateNumColsToDisplayForCategoryList(
    final SelectPriorityFactorsWidgetData data) {

    int numCols = 0;
    if (data.isPrioritiesApplicable()) {
      numCols++;
    }
    if (!data.isGroupBasedAssessment()) {
      numCols++;
    }
    numCols++; // classification is always displayed
    numCols++; // factor is always displayed
    if (data.isScoringUsed()) {
      numCols++;
    }

    return numCols;
  }

  /**
   * Determines if the tabbedTargetID string is already selected as to be a
   * priority.
   *
   * @param listCurrentlySelectedPotentialPriorityCategories
   * the list of currently selected factor results as priorities
   * @param tabbedTargetID
   * the results or group of results to be determined if to be
   * displayed as currently selected or not
   * @return true is the passed in target identifier is to be displayed as
   * selected, otherwise false
   */
  private boolean resultCurrentlySelectedAsPriority(
    final List<Category> listCurrentlySelectedPotentialPriorityCategories,
    final String tabbedTargetID) {

    for (final Category category : listCurrentlySelectedPotentialPriorityCategories) {
      for (final Classification classification : category
        .getClassifications()) {

        final List<FactorResult> factorResults =
          classification.getFactorResults();
        Collections.sort(factorResults, new FactorResultComparator());

        for (final FactorResult outerFactorResultItem : factorResults) {

          String innerTabbedTargetID = outerFactorResultItem.getId();
          for (final FactorResult innerfactorResultItem : classification
            .getFactorResults()) {
            // if scores are the same, and for the same factor, add
            // to the
            // client
            // string, and process list

            if (!innerfactorResultItem.getId()
              .equals(outerFactorResultItem.getId())
              && innerfactorResultItem.getScore()
                .equals(outerFactorResultItem.getScore())
              && outerFactorResultItem.getFactor()
                .equals(innerfactorResultItem.getFactor())) {

              innerTabbedTargetID += kTAB + innerfactorResultItem.getId();

            }
          }
          if (tabbedTargetID.equals(innerTabbedTargetID)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  /**
   * Gets the client details for display with the factor result. If a role
   * abbreviation is not missing, then the client name and abbreviation is
   * returned.
   *
   * @param factorResult
   * The factor result we are processing
   * @param data
   * The data containing the assessment result information
   * @return The client details for display with the factor result
   */
  private String getClientDetails(final FactorResult factorResult,
    final SelectPriorityFactorsWidgetData data) {

    final StringBuffer clientDetails = new StringBuffer();
    clientDetails.append(factorResult.getClient());
    if (!data.isAbbreviationMissing()) {
      clientDetails.append(kSPACE_OPEN_BRACKET_ROUND);
      clientDetails.append(factorResult.getRoleAbbreviation());
      clientDetails.append(kCLOSE_BRACKET_ROUND);
    }
    final String clientOrGroup = clientDetails.toString();
    return clientOrGroup;
  }

  /**
   * Returns an element containing the description strings for the priority min
   * and max numbers.
   *
   * @param documentFragment
   * The document to add the data to
   * @param classification
   * The classification we are dealing with
   * @return An element containing the description strings for the priority min
   * and max numbers.
   */
  private Element setPriorityMinMaxDescriptions(
    final DocumentFragment documentFragment,
    final List<PriorityValidation> priorityValidations) {

    final Element div =
      documentFragment.getOwnerDocument().createElement("div");
    div.setAttribute("class",
      "outer-cluster-borderless-nospace cluster-transparent");
    final Element table =
      documentFragment.getOwnerDocument().createElement("table");
    table.setAttribute("cellspacing", "0");
    div.appendChild(table);

    final Element col =
      documentFragment.getOwnerDocument().createElement("col");
    table.appendChild(col);

    final Element tbody =
      documentFragment.getOwnerDocument().createElement("tbody");
    table.appendChild(tbody);

    final Element tr =
      documentFragment.getOwnerDocument().createElement("tr");
    tbody.appendChild(tr);

    final Element td =
      documentFragment.getOwnerDocument().createElement("td");
    td.setAttribute("class", "field");

    for (final PriorityValidation priorityValidation : priorityValidations) {
      final Node text = documentFragment.getOwnerDocument()
        .createTextNode(priorityValidation.getValidationMessage());
      insertSpanOrTextNode(td, text);
    }

    tr.appendChild(td);
    return div;
  }

  /**
   * Retrieves the classification display label text. If label text has been
   * passed into this render through
   * {@link SelectPriorityFactorsWidgetData#getClassificationDisplayText()} this
   * is used as the label, else the default property value within the property
   * entry, 'Field.Label.Classification' is used.
   *
   * @param context
   * the renderer context to retrieve the property from is a
   * classification display text was not passed into the renderer
   * @param selectPriorityFactorsWidgetData
   * the data passed into the renderer where the passed in
   * classification display text is to be retrieved from
   * @return The classification display text to be displayed
   */
  private String getClassificationDisplayText(final RendererContext context,
    final SelectPriorityFactorsWidgetData selectPriorityFactorsWidgetData)
    throws DataAccessException {

    String classificationResultLabelText =
      selectPriorityFactorsWidgetData.getClassificationDisplayText();
    if (classificationResultLabelText.isEmpty()) {
      classificationResultLabelText =
        context.getDataAccessor().get(ClientPaths.GENERAL_RESOURCES_PATH
          .extendPath(properties, CLASSIFICATION));
    }
    return classificationResultLabelText;
  }

  /**
   * Retrieves the factor display label text. If label text has been passed into
   * this render through
   * {@link SelectPriorityFactorsWidgetData#getFactorDisplayText())} this is
   * used as the label, else the default property value within the property
   * entry, 'Field.Label.Factor' is used.
   *
   * @param context
   * the renderer context to retrieve the property from is a factor
   * display label text was not passed into the renderer
   * @param selectPriorityFactorsWidgetDAta
   * the data passed into the renderer where the passed in factor label
   * text is to be retrieved from
   * @return The factor display label text to be displayed
   */
  private String getFactorDisplayText(final RendererContext context,
    final SelectPriorityFactorsWidgetData selectPriorityFactorsWidgetData)
    throws DataAccessException {

    String factorDisplayText =
      selectPriorityFactorsWidgetData.getFactorDisplayText();
    if (factorDisplayText.isEmpty()) {
      factorDisplayText = context.getDataAccessor().get(
        ClientPaths.GENERAL_RESOURCES_PATH.extendPath(properties, FACTOR));
    }
    return factorDisplayText;
  }

  /**
   * Creates a span with dir attribute (if Bidi is enabled) or a Text node.
   *
   * @param elem
   * The element where to insert the span/text node
   * @param textElement
   * The text node to insert
   */
  private void insertSpanOrTextNode(final Element elem,
    final Node textElement) {

    if (curam.util.client.BidiUtils.isBidi()) {
      curam.util.client.BidiUtils.appendSpanNode(elem,
        textElement.getTextContent());
    } else {
      elem.appendChild(textElement);
    }
  }

  /**
   * Compare 2 factor results based on the factor order.
   */
  class FactorResultComparator implements Comparator<FactorResult> {

    /**
     * Compares the order of factor result 1 to factor result 2.
     *
     * @param factorResult1
     * The first factor result
     * @param factorResult2
     * The second factor result
     * @return The value 0 if the order of factor result 2 is equal to the order
     * of factor result 1; a value less than 0 if the order of factor
     * result 1 is lexicographically less than the order of factor
     * result 2; and a value greater than 0 if the order of factor
     * result 1 is lexicographically greater than the order of factor
     * result 2.
     */
    @Override
    public int compare(final FactorResult factorResult1,
      final FactorResult factorResult2) {

      return factorResult1.getOrder().compareTo(factorResult2.getOrder());
    }
  }
}
