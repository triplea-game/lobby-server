package org.triplea.spitfire.server;

import be.tomcools.dropwizard.websocket.WebsocketBundle;
import com.codahale.metrics.MetricRegistry;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jdbi3.JdbiFactory;
import java.util.List;

import io.dropwizard.jdbi3.bundles.JdbiExceptionsBundle;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.LobbyModuleRowMappers;
import org.triplea.dropwizard.common.AuthenticationConfiguration;
import org.triplea.dropwizard.common.IllegalArgumentMapper;
import org.triplea.dropwizard.common.JdbiLogging;
import org.triplea.modules.chat.ChatMessagingService;
import org.triplea.modules.chat.Chatters;
import org.triplea.modules.game.listing.GameListing;
import org.triplea.server.error.reporting.ErrorReportController;
import org.triplea.spitfire.server.access.authentication.ApiKeyAuthenticator;
import org.triplea.spitfire.server.access.authentication.AuthenticatedUser;
import org.triplea.spitfire.server.access.authorization.BannedPlayerFilter;
import org.triplea.spitfire.server.access.authorization.RoleAuthorizer;
import org.triplea.spitfire.server.controllers.lobby.GameHostingController;
import org.triplea.spitfire.server.controllers.lobby.GameListingController;
import org.triplea.spitfire.server.controllers.lobby.LobbyWatcherController;
import org.triplea.spitfire.server.controllers.lobby.PlayersInGameController;
import org.triplea.spitfire.server.controllers.lobby.moderation.AccessLogController;
import org.triplea.spitfire.server.controllers.lobby.moderation.BadWordsController;
import org.triplea.spitfire.server.controllers.lobby.moderation.DisconnectUserController;
import org.triplea.spitfire.server.controllers.lobby.moderation.ModeratorAuditHistoryController;
import org.triplea.spitfire.server.controllers.lobby.moderation.ModeratorsController;
import org.triplea.spitfire.server.controllers.lobby.moderation.MuteUserController;
import org.triplea.spitfire.server.controllers.lobby.moderation.RemoteActionsController;
import org.triplea.spitfire.server.controllers.lobby.moderation.UserBanController;
import org.triplea.spitfire.server.controllers.lobby.moderation.UsernameBanController;
import org.triplea.spitfire.server.controllers.user.account.CreateAccountController;
import org.triplea.spitfire.server.controllers.user.account.ForgotPasswordController;
import org.triplea.spitfire.server.controllers.user.account.LoginController;
import org.triplea.spitfire.server.controllers.user.account.PlayerInfoController;
import org.triplea.spitfire.server.controllers.user.account.UpdateAccountController;
import org.triplea.web.socket.GameConnectionWebSocket;
import org.triplea.web.socket.GenericWebSocket;
import org.triplea.web.socket.PlayerConnectionWebSocket;
import org.triplea.web.socket.SessionBannedCheck;
import org.triplea.web.socket.WebSocketMessagingBus;

/**
 * Main entry-point for launching drop wizard HTTP server. This class is responsible for configuring
 * any Jersey plugins, registering resources (controllers) and injecting those resources with
 * configuration properties from 'AppConfig'.
 */
@Slf4j
public class SpitfireServerApplication extends Application<SpitfireServerConfig> {

  private static final String[] DEFAULT_ARGS = new String[] {"server", "configuration.yml"};

  private final WebsocketBundle websocketBundle = new WebsocketBundle();

  /**
   * Main entry-point method, launches the drop-wizard http server. If no args are passed then will
   * use default values suitable for local development.
   */
  public static void main(final String[] args) throws Exception {
    final SpitfireServerApplication application = new SpitfireServerApplication();
    // if no args are provided then we will use default values.
    application.run(args.length == 0 ? DEFAULT_ARGS : args);
  }

  @Override
  public void initialize(final Bootstrap<SpitfireServerConfig> bootstrap) {
    // enable environment variables in configuration.yml
    bootstrap.setConfigurationSourceProvider(
            new SubstitutingSourceProvider(
                    bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
    // Better JDBI exceptions
    bootstrap.addBundle(new JdbiExceptionsBundle());
    bootstrap.addBundle(websocketBundle);
  }

  @Override
  public void run(final SpitfireServerConfig configuration, final Environment environment) {
    final Jdbi jdbi =
        new JdbiFactory()
            .build(environment, configuration.getDatabase(), "postgresql-connection-pool");

    LobbyModuleRowMappers.rowMappers().forEach(jdbi::registerRowMapper);

    if (configuration.isLogSqlStatements()) {
      JdbiLogging.registerSqlLogger(jdbi);
    }

    websocketBundle.addEndpoint(GameConnectionWebSocket.class);
    websocketBundle.addEndpoint(PlayerConnectionWebSocket.class);

    environment.jersey().register(BannedPlayerFilter.newBannedPlayerFilter(jdbi));

    final MetricRegistry metrics = new MetricRegistry();
    AuthenticationConfiguration.enableAuthentication(
        environment,
        metrics,
        ApiKeyAuthenticator.build(jdbi),
        new RoleAuthorizer(),
        AuthenticatedUser.class);

    environment.jersey().register(new IllegalArgumentMapper());

    final var sessionIsBannedCheck = SessionBannedCheck.build(jdbi);
    final var gameConnectionMessagingBus = new WebSocketMessagingBus();

    GenericWebSocket.init(
        GameConnectionWebSocket.class, gameConnectionMessagingBus, sessionIsBannedCheck);

    final var playerConnectionMessagingBus = new WebSocketMessagingBus();
    GenericWebSocket.init(
        PlayerConnectionWebSocket.class, playerConnectionMessagingBus, sessionIsBannedCheck);

    final var chatters = Chatters.build();
    ChatMessagingService.build(chatters, jdbi).configure(playerConnectionMessagingBus);

    final GameListing gameListing = GameListing.build(jdbi, playerConnectionMessagingBus);
    List.of(
            // lobby module controllers
            AccessLogController.build(jdbi),
            BadWordsController.build(jdbi),
            CreateAccountController.build(jdbi),
            DisconnectUserController.build(jdbi, chatters, playerConnectionMessagingBus),
            ForgotPasswordController.build(
                configuration, jdbi, configuration.getSmtpHost(), configuration.getSmtpPort()),
            GameHostingController.build(jdbi),
            GameListingController.build(gameListing),
            ErrorReportController.build(configuration.createGamesRepoGithubApiClient(), jdbi),
            LobbyWatcherController.build(configuration, gameListing),
            LoginController.build(jdbi, chatters),
            UsernameBanController.build(jdbi),
            UserBanController.build(
                jdbi, chatters, playerConnectionMessagingBus, gameConnectionMessagingBus),
            ModeratorAuditHistoryController.build(jdbi),
            ModeratorsController.build(jdbi),
            MuteUserController.build(chatters),
            PlayerInfoController.build(jdbi, chatters, gameListing),
            PlayersInGameController.build(gameListing),
            RemoteActionsController.build(jdbi, gameConnectionMessagingBus),
            UpdateAccountController.build(jdbi))
        .forEach(controller -> environment.jersey().register(controller));

    log.info(
        "STARTUP CONFIG -- DB connection: {}, user: {}, parameters: {}",
        configuration.getDatabase().getUrl(),
        configuration.getDatabase().getUser(),
        configuration);
  }
}
