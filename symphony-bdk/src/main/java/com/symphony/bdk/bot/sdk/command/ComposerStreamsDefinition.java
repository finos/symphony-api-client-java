package com.symphony.bdk.bot.sdk.command;

import java.util.Collection;

/**
 * Defines the streams to be linked to a message
 *
 * @author Gabriel Berberian
 */
public interface ComposerStreamsDefinition {

  /**
   * Defines the id of the recipient streams to be linked to a message
   *
   * @param streamIds the stream IDs list
   * @return an instance of ComposerMessageDefinition
   */
  ComposerMessageDefinition toStreams(String... streamIds);

  /**
   * Defines the id of the recipient streams to be linked to a message
   *
   * @param streamIds the stream IDs list
   * @return an instance of ComposerMessageDefinition
   */
  ComposerMessageDefinition toStreams(Collection<String> streamIds);

}
