// src/main/java/com/wongdarren/discordbot/service/CountdownService.java
package com.wongdarren.discordbot.service;

import com.wongdarren.discordbot.model.Countdown;
import com.wongdarren.discordbot.repository.CountdownRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CountdownService {
  private final CountdownRepository countdownRepository;
  private static final Logger log = LoggerFactory.getLogger(CountdownService.class);

  public CountdownService(CountdownRepository countdownRepository) {
    this.countdownRepository = countdownRepository;
  }

  @Transactional
  public void createCountdown(String name, LocalDate date, String channelId, String guildId) {
    log.info(
        "Creating countdown: name={}, date={}, channelId={}, guildId={}",
        name,
        date,
        channelId,
        guildId);

    Countdown countdown = new Countdown();
    countdown.setName(name);
    countdown.setDate(date);
    countdown.setChannelId(channelId);
    countdown.setGuildId(guildId);
    countdownRepository.save(countdown);

    log.info("Countdown created: {}", countdown);
  }

  public List<Countdown> getCountdownsByGuildId(String guildId) {
    log.info("Fetching countdowns for guildId={}", guildId);

    List<Countdown> countdowns = countdownRepository.findByGuildId(guildId);

    log.info("Found {} countdowns for guildId={}", countdowns.size(), guildId);
    return countdowns;
  }

  @Transactional
  public void deleteByNameAndGuildId(String name, String guildId) {
    log.info("Deleting countdown: name={}, guildId={}", name, guildId);

    countdownRepository.deleteByNameAndGuildId(name, guildId);

    log.info("Deleted countdown: name={}, guildId={}", name, guildId);
  }
}
