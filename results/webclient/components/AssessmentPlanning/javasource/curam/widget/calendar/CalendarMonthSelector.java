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

package curam.widget.calendar;

import curam.client.util.ClientUtils;
import curam.client.util.XmlUtils;
import curam.util.client.ClientException;
import curam.util.client.domain.render.view.AbstractViewRenderer;
import curam.util.client.domain.util.DomainUtils;
import curam.util.client.model.Field;
import curam.util.client.path.util.ClientPaths;
import curam.util.client.view.RendererContext;
import curam.util.client.view.RendererContract;
import curam.util.common.JDEException;
import curam.util.common.path.DataAccessException;
import curam.util.common.path.Path;
import curam.util.common.plugin.PlugInException;
import curam.util.dom.html2.HTMLUtils;
import curam.util.type.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Renderer used to display the month selector arrows above a calendar.
 */
@SuppressWarnings("restriction")
public class CalendarMonthSelector extends AbstractViewRenderer {

  /**
   * The first day of the month.
   */
  private static final int k_FIRST_OF_MONTH = 1;

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(final Field field, final DocumentFragment fragment,
    final RendererContext context, final RendererContract contract)
    throws ClientException, DataAccessException, PlugInException {

    // Get the calendar for today
    final Calendar currentCalendar = Calendar.getInstance();

    // Get the start date for the calendar currently on display
    final Path sourcePath = field.getBinding().getSourcePath();
    final String value = context.getDataAccessor().get(sourcePath);
    final Document myDoc = XmlUtils.parseXmlText(value);

    final Node rootNode = myDoc.getFirstChild();

    final String calendarDateStr =
      rootNode.getAttributes().getNamedItem("START_DATE").getNodeValue();
    final Date calendarDate = Date.fromISO8601(calendarDateStr);
    final Calendar displayedCalendarForPrevious = calendarDate.getCalendar();
    final Calendar displayedCalendarForNext = calendarDate.getCalendar();

    /*
     * Construct the previous and next url values using the url of the current
     * page and changing the start date parameter
     */
    final String url =
      context.getDataAccessor().get(
        ClientPaths.REQUEST_PATH_INFO_PATH.extendPath("client-url"));
    final int start = url.indexOf("startDate=");
    final int end = url.indexOf("&", start);
    final int startType = url.indexOf("calendarViewType=");
    final int endType = url.indexOf("&", startType);
    final String calendarType =
      url.substring(startType, endType).split("=")[1];

    // Construct the previous date
    final String previousDateStr =
      getPreviousDateString(displayedCalendarForPrevious, currentCalendar,
        calendarType);

    // Construct the next date
    final String nextDateStr =
      getNextDateString(displayedCalendarForNext, currentCalendar,
        calendarType);

    final String previousLink =
      replace(url, start, end, "startDate=" + previousDateStr);
    final String nextLink =
      replace(url, start, end, "startDate=" + nextDateStr);

    String date = new String();

    if ("CVT3".equals(calendarType)) {
      // Get the display text for the month
      final SimpleDateFormat simpleDateFormat =
        new SimpleDateFormat(getCalendarProperty("Month.Text.Format",
          "MMMM, yyyy", context), getLocale());
      date = simpleDateFormat.format(calendarDate.getCalendar().getTime());
    } else if ("CVT2".equals(calendarType)) {
      // Get the display text for the week
      final SimpleDateFormat simpleDateFormat =
        new SimpleDateFormat(getCalendarProperty("Week.Text.Format",
          "'Week 'ww, yyyy", context), getLocale());
      date = simpleDateFormat.format(calendarDate.getCalendar().getTime());
    } else if ("CVT1".equals(calendarType)) {
      // Get the display text for the week
      final SimpleDateFormat simpleDateFormat =
        new SimpleDateFormat(getCalendarProperty("Day.Text.Format",
          "MMMM d, yyyy", context), getLocale());
      date = simpleDateFormat.format(calendarDate.getCalendar().getTime());
    }
    // Create the outer div
    final Element div = fragment.getOwnerDocument().createElement("div");
    div.setAttribute("class", "month-selector");

    final Element previousAnchor =
      fragment.getOwnerDocument().createElement("a");
    previousAnchor.setAttribute("class", "month-previous");
    previousAnchor.setAttribute("title",
      getCalendarProperty("Previous.Month.ToolTip", "Previous", context));
    previousAnchor.setAttribute("href", previousLink);

    // get correct image for high contrast mode
    final String highContrastIndicator =
      rootNode.getAttributes().getNamedItem("HIGH_CONTRAST_IND")
        .getNodeValue();

    String backArrowImagePath = "";
    String nextArrowImagePath = "";

    if (!Boolean.valueOf(highContrastIndicator)) {
      nextArrowImagePath = "../Images/calendar/next_arrow.png";
      backArrowImagePath = "../Images/calendar/back_arrow.png";
    } else {
      nextArrowImagePath = "../Images/highcontrast/calendar/next_arrow.png";
      backArrowImagePath = "../Images/highcontrast/calendar/back_arrow.png";
    }

    final Element previousImage =
      fragment.getOwnerDocument().createElement("img");
    previousImage.setAttribute("src", backArrowImagePath);
    previousImage.setAttribute("alt",
      getCalendarProperty("Previous.Month.AltText", "Previous", context));
    previousAnchor.appendChild(previousImage);

    div.appendChild(previousAnchor);

    final Element textElement =
      fragment.getOwnerDocument().createElement("span");
    textElement.setAttribute("class", "month-txt");
    HTMLUtils.appendText(textElement, date);
    div.appendChild(textElement);

    final Element nextAnchor = fragment.getOwnerDocument().createElement("a");
    nextAnchor.setAttribute("class", "month-next");
    nextAnchor.setAttribute("title",
      getCalendarProperty("Next.Month.ToolTip", "Next", context));
    nextAnchor.setAttribute("href", nextLink);

    final Element nextImage =
      fragment.getOwnerDocument().createElement("img");
    nextImage.setAttribute("src", nextArrowImagePath);
    nextImage.setAttribute("alt",
      getCalendarProperty("Next.Month.AltText", "Next", context));
    nextAnchor.appendChild(nextImage);

    div.appendChild(nextAnchor);

    fragment.appendChild(div);
  }

  /**
   * Returns a string representation of the date to be used to open the calendar
   * view of the next month. If the next month is the current month, display the
   * current date, if not the current month, display the first of the month. If
   * the next month is January the year is increased by 1.
   * 
   * @param displayedCalendar
   * The calendar for the month currently on display
   * @param currentCalendar
   * The calendar for today
   * @return A string representation of the date to be used to open the calendar
   * for the next month
   * @throws DataAccessException
   */
  private String getNextDateString(final Calendar displayedCalendar,
    final Calendar currentCalendar, final String calendarType)
    throws DataAccessException {

    int nextDay = 0;
    int nextMonth = 0;
    int nextYear = 0;

    if ("CVT3".equals(calendarType)) {

      displayedCalendar.add(Calendar.MONTH, 1);

      nextDay = 1;
      nextMonth = displayedCalendar.get(Calendar.MONTH);
      nextYear = displayedCalendar.get(Calendar.YEAR);

    } else if ("CVT2".equals(calendarType)) {

      displayedCalendar.add(Calendar.WEEK_OF_YEAR, 1);

      nextDay = displayedCalendar.get(Calendar.DATE);
      nextMonth = displayedCalendar.get(Calendar.MONTH);
      nextYear = displayedCalendar.get(Calendar.YEAR);

    } else if ("CVT1".equals(calendarType)) {

      displayedCalendar.add(Calendar.DATE, 1);

      nextDay = displayedCalendar.get(Calendar.DATE);
      nextMonth = displayedCalendar.get(Calendar.MONTH);
      nextYear = displayedCalendar.get(Calendar.YEAR);
    }

    // calculate the date and create the new calendar
    final Calendar nextCalendar = Calendar.getInstance();
    nextCalendar.set(nextYear, nextMonth, nextDay);

    final Date nextDate = new Date(nextCalendar);
    final String nextDateStr = formatDate(nextDate);
    return nextDateStr;
  }

  /**
   * Returns a string representation of the date to be used to open the calendar
   * view of the previous month. If the previous month is the current month,
   * display the current date, if not the current month, display the first of
   * the month. If the previous month is December the year is decreased by 1.
   * 
   * @param displayedCalendar
   * The calendar for the month currently on display
   * @param currentCalendar
   * The calendar for today
   * @return A string representation of the date to be used to open the calendar
   * for the previous month
   * @throws DataAccessException
   */
  private String getPreviousDateString(final Calendar displayedCalendar,
    final Calendar currentCalendar, final String calendarType)
    throws DataAccessException {

    int prevDay = 0;
    int prevMonth = 0;
    int prevYear = 0;

    if ("CVT3".equals(calendarType)) {

      displayedCalendar.add(Calendar.MONTH, -1);

      prevDay = 1;
      prevMonth = displayedCalendar.get(Calendar.MONTH);
      prevYear = displayedCalendar.get(Calendar.YEAR);

    } else if ("CVT2".equals(calendarType)) {

      displayedCalendar.add(Calendar.WEEK_OF_YEAR, -1);

      prevDay = displayedCalendar.get(Calendar.DATE);
      prevMonth = displayedCalendar.get(Calendar.MONTH);
      prevYear = displayedCalendar.get(Calendar.YEAR);

    } else if ("CVT1".equals(calendarType)) {

      displayedCalendar.add(Calendar.DATE, -1);

      prevDay = displayedCalendar.get(Calendar.DATE);
      prevMonth = displayedCalendar.get(Calendar.MONTH);
      prevYear = displayedCalendar.get(Calendar.YEAR);
    }

    // calculate the date and create the new calendar
    final Calendar prevCalendar = Calendar.getInstance();
    prevCalendar.set(prevYear, prevMonth, prevDay);

    final Date prevDate = new Date(prevCalendar);
    final String prevDateStr = formatDate(prevDate);
    return prevDateStr;
  }

  /**
   * Formats a Curam date object so that it can be placed on the url.
   * 
   * @param date
   * The date to format.
   * @return The formatted date as a string.
   * @throws DataAccessException
   * If an exception occurs formatting the date.
   */
  private String formatDate(final Date date) throws DataAccessException {

    String previousDateStr;
    try {
      previousDateStr = DomainUtils.format(date, "SVR_DATE");
    } catch (final JDEException e) {
      throw new DataAccessException(DataAccessException.ERR_UNKNOWN, e);
    }
    return previousDateStr;
  }

  /**
   * Retrieves a property value for the CalendarMonthSelector.properties
   * application resource file.
   * 
   * @param propertyName
   * The name of the property to retrieve.
   * @param fallbackValue
   * The fall back value if the property has not been found.
   * @param context
   * The renderer context.
   * @return The property value.
   */
  private String getCalendarProperty(final String propertyName,
    final String fallbackValue, final RendererContext context) {

    return ClientUtils.getProperty("CalendarMonthSelector.properties",
      propertyName, fallbackValue, context);
  }

  /**
   * This method replaces the substring between the specified positions them
   * with another string.
   * 
   * @param text
   * The string to have replacements made to it.
   * @param start
   * The start position of replacement.
   * @param end
   * The end position of replacement (not included).
   * @param replace
   * The replacement string.
   * 
   * @return The replaced string.
   */
  private String replace(final String text, final int start, final int end,
    final String replace) {

    final StringBuffer result = new StringBuffer();
    result.append(text.substring(0, start));
    result.append(replace);
    result.append(text.substring(end));
    return result.toString();
  }

}
