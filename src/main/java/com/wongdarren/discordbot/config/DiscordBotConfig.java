package com.wongdarren.discordbot.config;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class DiscordBotConfig {

  @Bean
  @Profile("!test")
  public JDA jda(@Value("${BOT_TOKEN}") String token) throws Exception {
    return JDABuilder.createDefault(token).build();
  }
}
