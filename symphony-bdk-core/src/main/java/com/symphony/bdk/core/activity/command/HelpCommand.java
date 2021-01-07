package com.symphony.bdk.core.activity.command;

import com.symphony.bdk.core.activity.AbstractActivity;
import com.symphony.bdk.core.activity.ActivityRegistry;
import com.symphony.bdk.core.activity.model.ActivityInfo;
import com.symphony.bdk.core.activity.model.ActivityType;
import com.symphony.bdk.core.service.message.MessageService;
import com.symphony.bdk.core.service.message.model.Message;
import com.symphony.bdk.template.api.Template;

import org.apiguardian.api.API;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * A help command listing all the commands that can be performed by an end-user through the chat.
 */
@API(status = API.Status.STABLE)
public class HelpCommand extends PatternCommandActivity<CommandContext> {

  private static final String HELP_COMMAND = "/help";
  private static final String DEFAULT_DESCRIPTION = "List available commands";
  private final ActivityRegistry activityRegistry;
  private final MessageService messageService;

  public HelpCommand(@Nonnull ActivityRegistry activityRegistry, @Nonnull MessageService messageService) {
    this.activityRegistry = activityRegistry;
    this.messageService = messageService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Pattern pattern() {
    final String botMention = "@" + this.getBotDisplayName() + " ";
    return Pattern.compile("^" + botMention + HELP_COMMAND + "$");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onActivity(CommandContext context) {
    List<ActivityInfo> infos = this.activityRegistry.getActivityList()
        .stream()
        .filter(act -> !(act instanceof HelpCommand))
        .map(AbstractActivity::getInfo)
        .filter(info -> info.getType().equals(ActivityType.COMMAND))
        .collect(Collectors.toList());
    Template template = this.messageService.templates().newTemplateFromClasspath("/template/default_help_command.ftl");
    this.messageService.send(context.getStreamId(), Message.builder().template(template, Collections.singletonMap("commands", infos)).build());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ActivityInfo info() {
    return new ActivityInfo(ActivityType.COMMAND, HELP_COMMAND, DEFAULT_DESCRIPTION);
  }
}
