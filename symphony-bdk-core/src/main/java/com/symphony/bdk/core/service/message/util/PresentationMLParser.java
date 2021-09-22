package com.symphony.bdk.core.service.message.util;

import com.symphony.bdk.core.service.message.exception.PresentationMLParserException;

import lombok.Generated;
import lombok.SneakyThrows;
import org.apache.commons.text.StringEscapeUtils;
import org.apiguardian.api.API;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Helper class for parsing the PresentationML to text content.
 */
@API(status = API.Status.STABLE)
public class PresentationMLParser {

  private static final ThreadLocal<DocumentBuilder> LOCAL_BUILDER = ThreadLocal.withInitial(
      PresentationMLParser::initBuilder);
  private static final String NBSP = "&nbsp;";

  /**
   * Get text content from PresentationML
   *
   * @param presentationML the PresentationML to be parsed
   * @param trim           flag if we want to trim the text result
   * @return the message text content extracted from the given PresentationML
   */
  public static String getTextContent(String presentationML, Boolean trim) throws PresentationMLParserException {
    try {
      if (presentationML.contains(NBSP)) {
        presentationML = presentationML.replace(NBSP, " ");
      }

      final Document doc = LOCAL_BUILDER.get().parse(
          new ByteArrayInputStream(presentationML.getBytes(StandardCharsets.UTF_8)));
      String textContent = doc.getChildNodes().item(0).getTextContent();
      String escapedPresentationML = StringEscapeUtils.unescapeHtml4(textContent);
      return trim ? escapedPresentationML.trim() : escapedPresentationML;
    } catch (SAXException | IOException e) {
      throw new PresentationMLParserException(presentationML, "Failed to parse the PresentationML", e);
    }
  }

  /**
   * Get trimmed text content from PresentationML
   *
   * @param presentationML the PresentationML to be parsed
   * @return the message text content extracted from the given PresentationML
   */
  public static String getTextContent(String presentationML) throws PresentationMLParserException {
    return getTextContent(presentationML, true);
  }

  // Ignore the code coverage check because cannot produce the exception
  @Generated
  @SneakyThrows
  private static DocumentBuilder initBuilder() {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    return factory.newDocumentBuilder();
  }

  private PresentationMLParser() {

  }
}
