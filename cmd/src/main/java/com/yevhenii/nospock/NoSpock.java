package com.yevhenii.nospock;

import com.beust.jcommander.JCommander;

import java.io.IOException;

public class NoSpock {

  static {
    System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-4s] %5$s %n");
  }

  public static void main(String[] args) throws IOException {
    final var command = new TranslateCommand();
    JCommander.newBuilder()
      .addCommand(command)
      .build()
      .parse(args);
    command.run();
  }
}
