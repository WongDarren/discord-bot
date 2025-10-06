package com.wongdarren.discordbot.repository;

import com.wongdarren.discordbot.model.Countdown;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CountdownRepository extends JpaRepository<Countdown, String> {
  List<Countdown> findByGuildId(String guildId);

  void deleteByNameAndGuildId(String name, String guildId);
}
