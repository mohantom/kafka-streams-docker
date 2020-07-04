package com.tangspring.kafkastreams.mongo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MoviePlayService {

  public String playMovie(String filepath) throws Exception {
    String player = "C:\\Program Files\\DAUM\\PotPlayer\\PotPlayerMini64.exe";
    String[] s = new String[] {player, filepath};
    Process process = Runtime.getRuntime().exec(s);

    String message = "Started playing movie: " + filepath;
    log.info(message);
    return message;
  }
}
