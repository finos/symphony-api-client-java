package com.symphony.bdk.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.symphony.bdk.core.service.message.exception.PresentationMLParserException;
import com.symphony.bdk.core.service.message.util.PresentationMLParser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class PresentationMLParserTest {

  static Stream<Arguments> validPresentationMLs() {
    return Stream.of(
        arguments(
            "<div data-format=\"PresentationML\" data-version=\"2.0\">\n"
                + "<a href=\"http://www.symphony.com\">This is a link to Symphony's Website</a>\n</div>",
            "This is a link to Symphony's Website"),
        arguments("<div data-format=\"PresentationML\" data-version=\"2.0\"> <p>/test &lt;/messageML&gt;</p> </div>",
            "/test </messageML>"),
        arguments("<div data-format=\"PresentationML\" data-version=\"2.0\">Hello&#xA0;World</div>", "Hello World"),
        arguments("<div data-format=\"PresentationML\" data-version=\"2.0\">Hello&nbsp;World&nbsp;Again</div>",
            "Hello World Again"));
  }


  @ParameterizedTest
  @MethodSource("validPresentationMLs")
  void getMessageFromPresentationMLTest(String presentationML, String expectedContent)
      throws PresentationMLParserException {
    assertEquals(expectedContent, PresentationMLParser.getTextContent(presentationML));
  }

  @Test
  void getMessageFromPresentationMLNotTrimTest() throws PresentationMLParserException {
    String presentationML = "<div data-format=\"PresentationML\" data-version=\"2.0\"> \n"
        + "  <a href=\"http://www.symphony.com\">This is a link to Symphony's Website</a> \n"
        + "</div>";

    String content = PresentationMLParser.getTextContent(presentationML, false);

    assertNotEquals("This is a link to Symphony's Website", content);
    assertEquals("This is a link to Symphony's Website", content.trim());
  }

  @Test
  void getMessageFromPresentationMLFailedToParse() {
    String presentationML = "<div data-format=\"PresentationML\" data-version=\"2.0\"> \n"
        + "  <a href=\"http://www.symphony.com\">This is a link to Symphony's Website<a> \n"
        + "</div>";

    assertThrows(PresentationMLParserException.class, () -> PresentationMLParser.getTextContent(presentationML));
  }

  @Test
  void getMessageFromEmptyPresentationMLFailed() {
    String presentationML = "";

    assertThrows(PresentationMLParserException.class, () -> PresentationMLParser.getTextContent(presentationML));
  }

  @Test
  void getMessageFromInvalidPresentationMLFailed() {
    String presentationML = "<div data-format=\"PresentationML\" data-version=\"2.0\">";

    assertThrows(PresentationMLParserException.class, () -> PresentationMLParser.getTextContent(presentationML));
  }
}
