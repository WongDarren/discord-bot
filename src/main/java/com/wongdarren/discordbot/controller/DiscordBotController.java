package com.wongdarren.discordbot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DiscordBotController {
  private static final Logger log = LoggerFactory.getLogger(DiscordBotController.class);

  @GetMapping("/")
  public String home() {
    log.info("GET / endpoint was hit");
    return "Discord bot running!";
  }
}
