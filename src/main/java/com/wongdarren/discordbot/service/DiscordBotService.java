package com.wongdarren.discordbot.service;

import com.wongdarren.discordbot.model.Countdown;
import com.wongdarren.discordbot.repository.CountdownRepository;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DiscordBotService extends ListenerAdapter {

  private static final Logger log = LoggerFactory.getLogger(DiscordBotService.class);

  @Value("${BOT_TOKEN}")
  private String token;

  private JDA jda;

  private final CountdownRepository countdownRepository;
  private final CountdownService countdownService;

  public DiscordBotService(
      CountdownRepository countdownRepository, CountdownService countdownService) {
    this.countdownRepository = countdownRepository;
    this.countdownService = countdownService;
  }

  @PostConstruct
  public void startDiscordBot() throws Exception {
    jda = JDABuilder.createDefault(token).addEventListeners(this).build().awaitReady();

    jda.updateCommands().addCommands().queue();

    for (var guild : jda.getGuilds()) {
      guild
          .updateCommands()
          .addCommands(
              Commands.slash("countdown", "Set a countdown to a date (YYYY-MM-DD) with a name")
                  .addOption(OptionType.STRING, "name", "Name of the event", true)
                  .addOption(OptionType.STRING, "date", "Target date (YYYY-MM-DD)", true),
              Commands.slash("viewcountdowns", "View all active countdowns"),
              Commands.slash("deletecountdown", "Delete a countdown by name")
                  .addOption(OptionType.STRING, "name", "Name of the countdown to delete", true))
          .queue();
    }
  }

  @Override
  public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
    String guildId = Objects.requireNonNull(event.getGuild()).getId();

    switch (event.getName()) {
      case "countdown":
        handleCountdownCommand(event, guildId);
        break;
      case "viewcountdowns":
        handleViewCountdownsCommand(event, guildId);
        break;
      case "deletecountdown":
        handleDeleteCountdownCommand(event, guildId);
        break;
    }
  }

  private void handleCountdownCommand(SlashCommandInteractionEvent event, String guildId) {
    String name = Objects.requireNonNull(event.getOption("name")).getAsString();
    String dateStr = Objects.requireNonNull(event.getOption("date")).getAsString();
    LocalDate target;
    try {
      target = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (Exception e) {
      event.reply("Invalid date format. Use YYYY-MM-DD.").setEphemeral(true).queue();
      return;
    }
    if (target.isBefore(LocalDate.now())) {
      event.reply("Invalid or past date.").setEphemeral(true).queue();
      return;
    }
    countdownService.createCountdown(name, target, event.getChannel().getId(), guildId);
    event.reply("Countdown **" + name + "** set to " + target).queue();
  }

  private void handleViewCountdownsCommand(SlashCommandInteractionEvent event, String guildId) {
    List<Countdown> guildCountdowns = countdownService.getCountdownsByGuildId(guildId);
    if (guildCountdowns.isEmpty()) {
      event.reply("No active countdowns.").setEphemeral(true).queue();
    } else {
      StringBuilder sb = new StringBuilder();
      guildCountdowns.forEach(
          c ->
              sb.append("**")
                  .append(c.getName())
                  .append("**: Date: ")
                  .append(c.getDate())
                  .append("\n"));
      event.reply(sb.toString()).setEphemeral(true).queue();
    }
  }

  private void handleDeleteCountdownCommand(SlashCommandInteractionEvent event, String guildId) {
    String delName = Objects.requireNonNull(event.getOption("name")).getAsString();
    countdownService.deleteByNameAndGuildId(delName, guildId);
    event.reply("Countdown **" + delName + "** deleted.").setEphemeral(true).queue();
  }

  @Scheduled(cron = "0 0 0 * * *")
  public void sendCountdownReminders() {
    List<Countdown> allCountdowns = countdownRepository.findAll();

    for (Countdown countdown : allCountdowns) {
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime target = countdown.getDate().atStartOfDay();

      if (!target.isAfter(now)) continue;

      Duration duration = Duration.between(now, target);

      String message = getMessage(duration, countdown.getName());
      try {
        MessageChannel channel = jda.getTextChannelById(countdown.getChannelId());
        if (channel != null) {
          channel.sendMessage(message).queue();
          log.info(
              "Sent reminder for countdown: name={}, channelId={}, guildId={}",
              countdown.getName(),
              countdown.getChannelId(),
              countdown.getGuildId());
        }
      } catch (Exception ignored) {
      }
    }
  }

  @NotNull
  private static String getMessage(Duration duration, String name) {
    long totalMinutes = duration.toMinutes();

    long months = totalMinutes / (60 * 24 * 30);
    totalMinutes %= (60 * 24 * 30);
    long weeks = totalMinutes / (60 * 24 * 7);
    totalMinutes %= (60 * 24 * 7);
    long days = totalMinutes / (60 * 24);

    return String.format(
        "Countdown to **%s**: %d Months %d Weeks %d Days remaining!", name, months, weeks, days);
  }
}
