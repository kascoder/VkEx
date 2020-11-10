package org.kascoder.vkex.cli.command.auth;

import org.kascoder.vkex.cli.command.auth.login.LoginCommand;
import picocli.CommandLine.Command;

@Command(name = "auth", subcommands = { LoginCommand.class, LogoutCommand.class, StatusCommand.class })
public class AuthCommand {
}
