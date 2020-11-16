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
 * Copyright 2009 Curam Software Ltd.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of Curam
 * Software, Ltd. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Curam Software.
 */
package assessmentplanning.delivery;

import assessmentplanning.util.RendererHelper;
import curam.util.client.BidiUtils;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Widget renderer for the select clients are roles page.
 */
@SuppressWarnings("restriction")
public class SelectClientAndRolesWidgetRenderer extends AbstractEditRenderer {

  private static final int kCHILD_AGE_LIMIT = 16;

  private static final String NAME = "Text.Name";

  private static final String GENDER_FEMALE = "SX2";

  private static final String GENDER = "Text.Gender";

  private static final String AGE = "Text.Age";

  private static final String ROLE = "Text.Role";

  private String properties;

  /**
   * Constructor.
   */
  public SelectClientAndRolesWidgetRenderer() {

    properties = new String();
    setPropertyFileLocation();
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
   * Sets the location of the properties file to be used. *
   */
  private void setPropertyFileLocation() {

    properties =
      "assessmentplanning.delivery.SelectClientAndRolesWidgetRenderer";
  }

  /**
   * Adds the roles drop down to the given row.
   * 
   * @param targetID
   * The selected role target ID
   * @param context
   * The Renderer context
   * @param parentElement
   * The parent element for the td to be added
   * @param documentFragment
   * The document fragment for the renderer
   * @param data
   * The client and roles data
   * @param targetIDList
   * The target ID list
   * @param prepopulateToRoleID
   * Indicates if the select for the role should be disabled
   * @param field
   * The field to be rendered
   * @param label
   * The label for the selected role
   * @param selectedRoles
   * The set of IDs of selected roles
   * @param multipleClientRolesSet
   * The set of IDs of roles which allow multiple clients
   * @throws ClientException
   * Generic Exception Signature
   */
  private void
    addRoles(final String targetID, final RendererContext context,
      final Element parentElement, final DocumentFragment documentFragment,
      final SelectClientAndRolesWidgetData data, final String targetIDList,
      final String prepopulateToRoleID, final Field field,
      final String label, final Set<String> selectedRoles,
      final Set<String> multipleClientRolesSet) throws ClientException {

    final Element td =
      documentFragment.getOwnerDocument().createElement("td");
    td.setAttribute("class", "field codetable last-field");
    parentElement.appendChild(td);

    final String selectedRoleHolderID =
      context.addFormItem(field, label, "selectedRole" + targetID, false);
    final Element input =
      documentFragment.getOwnerDocument().createElement("input");
    input.setAttribute("type", "hidden");
    input.setAttribute("id", selectedRoleHolderID);
    input.setAttribute("name", selectedRoleHolderID);
    input.setAttribute("value", "");
    td.appendChild(input);

    final Element select =
      documentFragment.getOwnerDocument().createElement("select");
    select.setAttribute("id", targetID);
    select.setAttribute("name", targetID);
    select.setAttribute("display", "none");
    select.setAttribute("onchange", "selectRole(" + targetIDList + ");");

    if (prepopulateToRoleID == null) {
      select.setAttribute("DISABLED", "DISABLED");
    }

    td.appendChild(select);

    final List<Role> rolesList = data.listRoles();

    if (rolesList.size() > 1) {
      final Element option =
        documentFragment.getOwnerDocument().createElement("option");
      final Node text =
        documentFragment.getOwnerDocument().createTextNode("");
      option.appendChild(text);
      select.appendChild(option);
    }

    for (final Role role : rolesList) {
      final Element option =
        documentFragment.getOwnerDocument().createElement("option");
      option.setAttribute("value", role.getID());
      final Node text =
        documentFragment.getOwnerDocument().createTextNode(role.getName());
      option.appendChild(text);

      if (prepopulateToRoleID != null
        && role.getID().equals(prepopulateToRoleID)) {
        option.setAttribute("selected", "true");
      } else if (selectedRoles.contains(role.getID())
        && !multipleClientRolesSet.contains(role.getID())) {
        option.setAttribute("disabled", "true");
      }

      select.appendChild(option);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final RendererContract rendererContract) throws ClientException,
    DataAccessException, PlugInException {

    final Document myDoc = getDocumentFromContext(field, context);
    context.includeScriptURIs("text/javascript",
      "../CDEJ/jscript/SelectClientsAndRoles.js");

    // parse the xml
    final SelectClientAndRolesWidgetData data =
      new SelectClientAndRolesWidgetData(myDoc);
    final String label = field.getParameters().get(FieldParameters.LABEL);

    final Element div =
      documentFragment.getOwnerDocument().createElement("div");
    div.setAttribute("class", "list");

    // input to hold multiple client role IDs
    final String multipleClientsNodeID =
      context.addFormItem(field, label, "multipleClients", false);
    final Element input =
      documentFragment.getOwnerDocument().createElement("input");
    input.setAttribute("type", "hidden");
    input.setAttribute("id", multipleClientsNodeID);
    input.setAttribute("name", multipleClientsNodeID);
    input.setAttribute("value", getMultipleClientRolesString(data));
    div.appendChild(input);

    // needed for updating the values with jScript
    final String hiddenInputTargetID =
      context.addFormItem(field,
        field.getParameters().get(FieldParameters.LABEL), "hiddenField",
        false);
    final Element hiddenInput =
      documentFragment.getOwnerDocument().createElement("input");
    hiddenInput.setAttribute("type", "hidden");
    hiddenInput.setAttribute("id", hiddenInputTargetID);
    hiddenInput.setAttribute("name", hiddenInputTargetID);

    if (!addRoleData(data)) {
      hiddenInput.setAttribute("role", data.listRoles().get(0).getID());
    }
    div.appendChild(hiddenInput);
    documentFragment.appendChild(div);

    // Add the scrollable div to the list
    final Element scrollableDiv =
      documentFragment.getOwnerDocument().createElement("div");
    scrollableDiv.setAttribute("class", "client-roles-scrollable");
    div.appendChild(scrollableDiv);

    final Element table =
      documentFragment.getOwnerDocument().createElement("table");
    table.setAttribute("cellspacing", "0");
    scrollableDiv.appendChild(table);

    addCols(documentFragment, data, table);

    addTableHeader(documentFragment, context, data, table);

    final Element tbody =
      documentFragment.getOwnerDocument().createElement("tbody");
    tbody.setAttribute("id", "tbody");
    table.appendChild(tbody);

    /**
     * Create the hidden target ids for the clients and check if they have
     * previously been selected.
     */
    final List<Client> clients = data.listClients();
    final Set<String> selectedRolesSet =
      createClientHiddenTargetsAndGetSelectedRoles(field, context, label,
        clients);
    final Set<String> multipleClientRolesSet =
      getMultipleClientRolesSet(data);
    Element tr;
    Element span;
    Node text;
    for (int i = 0; i < clients.size(); i++) {
      final Client client = clients.get(i);
      final String targetID =
        context.addFormItem(field, label, client.getConcernRole(), false);
      final String checkBoxHiddenTargetID =
        context.addFormItem(field, label,
          "CheckBox," + client.getConcernRole(), false);
      final Boolean checked =
        Boolean.valueOf(context.getFormItemInitialValue(field, true,
          "CheckBox," + client.getConcernRole()));

      tr = documentFragment.getOwnerDocument().createElement("tr");
      setOddEvenClasses(clients, tr, i);
      tbody.appendChild(tr);

      final String prepopulateToRoleID =
        addCheckbox(field, documentFragment, context, data, tr, client,
          targetID, checkBoxHiddenTargetID, checked);

      span = documentFragment.getOwnerDocument().createElement("span");
      span.setAttribute("class", "image-name-container");
      Element td = documentFragment.getOwnerDocument().createElement("td");
      td.setAttribute("class", "field");

      final Element img =
        documentFragment.getOwnerDocument().createElement("img");

      try {
        // setClientImageSrc(client, img, getHighContrastPreference(context));
        setClientImageSrc(client, img,
          RendererHelper.getHighContrastPreference(context));
      } catch (final SAXException e) {
        throw new RuntimeException(e);
      } catch (final ParserConfigurationException e) {
        throw new RuntimeException(e);
      } catch (final JDEException e) {
        throw new RuntimeException(e);
      }

      span.appendChild(img);
      td.appendChild(span);

      span = documentFragment.getOwnerDocument().createElement("span");
      text =
        documentFragment.getOwnerDocument().createTextNode(client.getName());
      span.appendChild(text);
      td.appendChild(span);
      tr.appendChild(td);

      td = documentFragment.getOwnerDocument().createElement("td");
      td.setAttribute("class", "field");
      text =
        documentFragment.getOwnerDocument().createTextNode(
          client.getGenderDescription());
      td.appendChild(text);
      tr.appendChild(td);

      td = documentFragment.getOwnerDocument().createElement("td");
      if (addRoleData(data)) {
        td.setAttribute("class", "field");
      } else {
        td.setAttribute("class", "field last-field");
      }
      text =
        documentFragment.getOwnerDocument().createTextNode(
          client.getAgeString());
      td.appendChild(text);
      tr.appendChild(td);

      if (addRoleData(data)) {
        addRoles(client.getTargetID(), context, td, documentFragment, data,
          parseClientTargetID(client, clients), prepopulateToRoleID, field,
          label, selectedRolesSet, multipleClientRolesSet);
      }
    }
  }

  /**
   * Adds the check box and related elements to the given row.
   * 
   * @param field
   * The field to be rendered
   * @param documentFragment
   * The document fragment for the renderer
   * @param context
   * The renderer context
   * @param data
   * The clients and roles data
   * @param tr
   * The current row
   * @param client
   * The current client
   * @param targetID
   * The target ID to be used for the check box
   * @param checkBoxHiddenTargetID
   * The check box hidden target ID
   * @param checked
   * Indicates if the check box should be checked
   * @return The pre-populate to role ID, used to indicate if the select for the
   * role should be disabled.
   * @throws ClientException
   * Generic Exception Signature
   * @throws DataAccessException
   * Generic Exception Signature
   */
  private String addCheckbox(final Field field,
    final DocumentFragment documentFragment, final RendererContext context,
    final SelectClientAndRolesWidgetData data, final Element tr,
    final Client client, final String targetID,
    final String checkBoxHiddenTargetID, final Boolean checked)
    throws ClientException, DataAccessException {

    final Element td =
      documentFragment.getOwnerDocument().createElement("td");
    td.setAttribute("class", "multiselect first-field");
    final Element input =
      documentFragment.getOwnerDocument().createElement("input");
    input.setAttribute("type", "checkbox");
    input.setAttribute("id", targetID);
    input.setAttribute("name", targetID);
    input.setAttribute("value", client.getConcernRole());
    input.setAttribute("class", "curam-checkbox"); 
    
    // pre-populate?
    String prepopulateToRoleID =
      data.getPrepopulatedRoles().get(client.getConcernRole());
    if (prepopulateToRoleID != null || checked) {
      input.setAttribute("checked", "checked");
    }

    // need to check for redisplay because of validation error
    if (prepopulateToRoleID == null && checked) {
      prepopulateToRoleID =
        context.getFormItemInitialValue(field, true,
          "Role," + client.getConcernRole());
    }

    if (addRoleData(data)) {
      input.setAttribute("onclick", "toggleDropDown(" + client.getTargetID()
        + "), updatedSelected(" + checkBoxHiddenTargetID + ");");
    } else {
      input.setAttribute("onclick", "buildDropDownString()");
    }
    td.appendChild(input);

    final Element checkBoxHiddenInput =
      documentFragment.getOwnerDocument().createElement("input");
    checkBoxHiddenInput.setAttribute("type", "hidden");
    checkBoxHiddenInput.setAttribute("id", checkBoxHiddenTargetID);
    checkBoxHiddenInput.setAttribute("name", checkBoxHiddenTargetID);
    checkBoxHiddenInput.setAttribute("value", "false");
    checkBoxHiddenInput.setAttribute("selected", "false");

    if (prepopulateToRoleID != null || checked) {
      checkBoxHiddenInput.setAttribute("value", "true");
      checkBoxHiddenInput.setAttribute("selected", "true");
    }

    // This label will be responsible to draw a custom checkbox instead of
    // rendering the browser's default checkbox.
    final Element checkboxDrawer =
      documentFragment.getOwnerDocument().createElement("label");
    checkboxDrawer.setAttribute("for", targetID);
    checkboxDrawer.setAttribute("aria-hidden", "true");
    checkboxDrawer.setTextContent("\u00A0");

    // This label will be responsible to draw the checkbox touchable area
    final Element checkboxTouchableArea =
      documentFragment.getOwnerDocument().createElement("label");
    checkboxTouchableArea.setAttribute("for", targetID);
    checkboxTouchableArea.setAttribute("aria-hidden", "true");
    checkboxTouchableArea.setAttribute("class", "checkbox-touchable-area");
    checkboxTouchableArea.setAttribute("title", input.getAttribute("title"));
    checkboxTouchableArea.setTextContent("\u00A0");

    td.appendChild(checkboxDrawer);
    td.appendChild(checkboxTouchableArea);

    td.appendChild(checkBoxHiddenInput);
    tr.appendChild(td);
    return prepopulateToRoleID;
  }

  /**
   * Sets the CSS classes for the display of odd or even rows on the table.
   * 
   * @param clients
   * The list of clients
   * @param tr
   * The current table row
   * @param index
   * The current row index
   */
  private void setOddEvenClasses(final List<Client> clients,
    final Element tr, final int index) {

    if (index % 2 > 0) {
      if (index == clients.size() - 1) {
        tr.setAttribute("class", "even-last-row");
      } else {
        tr.setAttribute("class", "even");
      }
    } else {
      if (index == clients.size() - 1) {
        tr.setAttribute("class", "odd-last-row");
      } else {
        tr.setAttribute("class", "odd");
      }
    }
  }

  /**
   * Adds the table header elements for the clients and roles table.
   * 
   * @param documentFragment
   * The document fragment to be updated
   * @param context
   * The renderer context
   * @param data
   * The client and roles data
   * @param table
   * The table to add the header to
   */
  private void addTableHeader(final DocumentFragment documentFragment,
    final RendererContext context, final SelectClientAndRolesWidgetData data,
    final Element table) {

    final Element thead =
      documentFragment.getOwnerDocument().createElement("thead");
    table.appendChild(thead);
    final Element tr =
      documentFragment.getOwnerDocument().createElement("tr");
    thead.appendChild(tr);
    Element th = documentFragment.getOwnerDocument().createElement("th");
    th.setAttribute("class", "field first-header");
    Element span = documentFragment.getOwnerDocument().createElement("span");
    Node text = documentFragment.getOwnerDocument().createTextNode("");
    span.appendChild(text);
    th.appendChild(span);
    tr.appendChild(th);
    th = documentFragment.getOwnerDocument().createElement("th");
    th.setAttribute("class", "field");
    span = documentFragment.getOwnerDocument().createElement("span");
    text =
      documentFragment.getOwnerDocument().createTextNode(
        getProperty(context, properties, NAME));
    span.appendChild(text);
    th.appendChild(span);
    tr.appendChild(th);
    text =
      documentFragment.getOwnerDocument().createTextNode(
        getProperty(context, properties, GENDER));
    span = documentFragment.getOwnerDocument().createElement("span");
    th = documentFragment.getOwnerDocument().createElement("th");
    th.setAttribute("class", "field");
    span.appendChild(text);
    th.appendChild(span);
    tr.appendChild(th);
    text =
      documentFragment.getOwnerDocument().createTextNode(
        getProperty(context, properties, AGE));
    th = documentFragment.getOwnerDocument().createElement("th");
    if (addRoleData(data)) {
      th.setAttribute("class", "field");
    } else {
      th.setAttribute("class", "field last-header");
    }
    span = documentFragment.getOwnerDocument().createElement("span");
    span.appendChild(text);
    th.appendChild(span);
    tr.appendChild(th);

    if (addRoleData(data)) {
      text =
        documentFragment.getOwnerDocument().createTextNode(
          getProperty(context, properties, ROLE));
      th = documentFragment.getOwnerDocument().createElement("th");
      th.setAttribute("class", "field last-header");
      span = documentFragment.getOwnerDocument().createElement("span");
      span.appendChild(text);
      th.appendChild(span);
      tr.appendChild(th);
    }
  }

  /**
   * Create the hidden target ids for the clients and check if they have
   * previously been selected.
   * 
   * @param field
   * The field
   * @param context
   * The renderer context
   * @param label
   * The label
   * @param clients
   * The list of clients to be selected
   * @param selectedRolesSet
   * The set of roles previously selected
   * @return The list of roles previously selected
   * @throws ClientException
   * @throws DataAccessException
   */
  private Set<String> createClientHiddenTargetsAndGetSelectedRoles(
    final Field field, final RendererContext context, final String label,
    final List<Client> clients) throws ClientException, DataAccessException {

    final Set<String> selectedRolesSet = new HashSet<String>();
    for (final Client client : clients) {
      client.setTargetID(context.addFormItem(field, label,
        "Role," + client.getConcernRole(), false));
      final Boolean checked =
        Boolean.valueOf(context.getFormItemInitialValue(field, true,
          "CheckBox," + client.getConcernRole()));
      if (checked) {
        final String roleID =
          context.getFormItemInitialValue(field, true,
            "Role," + client.getConcernRole());
        if (null != roleID) {
          selectedRolesSet.add(roleID);
        }
      }
    }
    return selectedRolesSet;
  }

  /**
   * Returns the {@link Document} for this renderer.
   * 
   * @param field
   * The {@link Field}
   * @param context
   * The {@link RendererContext}
   * @return The xml document to be rendered
   */
  private Document getDocumentFromContext(final Field field,
    final RendererContext context) throws DataAccessException {

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
    return myDoc;
  }

  /**
   * Sets the image source to be the correct image depending on the client's
   * gender and age.
   * 
   * @param client
   * The client for whom the image will be displayed
   * @param img
   * The image element
   * @throws DataAccessException
   * @throws ParserConfigurationException
   * @throws SAXException
   */
  private void setClientImageSrc(final Client client, final Element img,
    final boolean highContrastInd) throws DataAccessException, SAXException,
    ParserConfigurationException {

    String highContrastFileIndicator = new String();

    if (highContrastInd) {
      highContrastFileIndicator = "hc_";
    } else {
      highContrastFileIndicator = "";
    }

    final int age = client.getAgeAsInteger();

    if (client.getGenderCode().equals(GENDER_FEMALE)) {
      if (age > kCHILD_AGE_LIMIT) {
        img.setAttribute("src", "../Images/person_woman.png");
      } else {
        img.setAttribute("src", "../Images/person_girl.png");
      }
    } else { // Male or someone with no Gender(provider)
      if (age > kCHILD_AGE_LIMIT || age == -1) {
        img.setAttribute("src", "../Images/person_man.png");
      } else {
        img.setAttribute("src", "../Images/person_boy.png");
      }
    }
  }

  private void addCols(final DocumentFragment documentFragment,
    final SelectClientAndRolesWidgetData data, final Element table) {

    Element col = documentFragment.getOwnerDocument().createElement("col");
    col.setAttribute("class", "multiselect");
    col.setAttribute("width", "6%");
    table.appendChild(col);

    col = documentFragment.getOwnerDocument().createElement("col");
    col.setAttribute("class", "field");
    col.setAttribute("width", "32%");
    table.appendChild(col);

    col = documentFragment.getOwnerDocument().createElement("col");
    col.setAttribute("class", "field");
    col.setAttribute("width", "17%");
    table.appendChild(col);

    col = documentFragment.getOwnerDocument().createElement("col");
    col.setAttribute("class", "field");
    col.setAttribute("width", "17%");
    table.appendChild(col);

    if (addRoleData(data)) {
      col = documentFragment.getOwnerDocument().createElement("col");
      col.setAttribute("class", "field");
      col.setAttribute("width", "32%");
      table.appendChild(col);
    }
  }

  /**
   * Gets a comma separated list of role IDs where multiple clients are allowed
   * for a role.
   * 
   * @param data
   * The client and roles widget data
   * @return A list of all roles where multiple clients are allowed
   */
  private String getMultipleClientRolesString(
    final SelectClientAndRolesWidgetData data) {

    final StringBuffer multipleClientRoles = new StringBuffer();
    for (final Role role : data.listRoles()) {
      if (role.getMultipleClientsInd()) {
        multipleClientRoles.append(role.getID());
        multipleClientRoles.append(",");
      }
    }
    final String idString = multipleClientRoles.toString();
    return idString.toString();
  }

  /**
   * Gets a {@link Set} of role IDs where multiple clients are allowed for a
   * role.
   * 
   * @param data
   * The client and roles widget data
   * @return A set of all roles where multiple clients are allowed
   */
  private Set<String> getMultipleClientRolesSet(
    final SelectClientAndRolesWidgetData data) {

    final Set<String> multipleClientRoles = new HashSet<String>();
    for (final Role role : data.listRoles()) {
      if (role.getMultipleClientsInd()) {
        multipleClientRoles.add(role.getID());
      }
    }
    return multipleClientRoles;
  }

  /**
   * <p>
   * Determines if the the role data column is to be generated for the widget.
   * The determining factor is the number of roles that exist for the case. If
   * there is more than one role (e.g. primary client, secondary client), then
   * the role data column is to be generated.
   * </p>
   * 
   * @param data
   * The widget data from which the role data is accessed
   * @return true if the role data column is to be generated
   */
  private boolean addRoleData(final SelectClientAndRolesWidgetData data) {

    return data.listRoles().size() > 1 || data.listClients().size() > 1;
  }

  /**
   * Parses the client targetID list excluding the passed in client.
   * 
   * @param clientIn
   * The client to be excluded
   * @param clientList
   * The list of all clients
   * @return The target ID list for all clients except for the one passed in
   */
  private String parseClientTargetID(final Client clientIn,
    final List<Client> clientList) {

    String targetIDList = clientIn.getTargetID();

    // don't include the passed in client
    for (final Client client : clientList) {
      if (client.getConcernRole().equals(clientIn.getConcernRole())) {
        continue;
      }

      if (targetIDList.length() > 0) {
        targetIDList = targetIDList + "," + client.getTargetID();
      } else {
        targetIDList = client.getTargetID();
      }
    }
    return targetIDList;
  }

  /**
   * Creates a span with dir attribute (if Bidi is enabled) or a regular span
   * without any attribute.
   * 
   * @param th
   * The th where to insert the span node
   * @param span
   * The regular span to insert
   * @param text
   * The text for the span to create if Bidi is enabled
   */
  private void insertSpan(final Element th, final Element span,
    final String text) {

    if (BidiUtils.isBidi()) {
      BidiUtils.appendSpanNode(th, text);
    } else {
      th.appendChild(span);
    }
  }
}
