package net.cubecraft.server;

import ink.flybird.fcommon.event.EventBus;
import ink.flybird.fcommon.event.SimpleEventBus;
import ink.flybird.fcommon.logging.LoggerContext;
import ink.flybird.fcommon.threading.LoopTickingThread;
import ink.flybird.fcommon.timer.Timer;
import ink.flybird.jflogger.ILogger;
import ink.flybird.jflogger.LogManager;
import net.cubecraft.EnvironmentPath;
import net.cubecraft.SharedContext;
import net.cubecraft.Side;
import net.cubecraft.mod.ModLoader;
import net.cubecraft.mod.ModManager;
import net.cubecraft.level.Level;
import net.cubecraft.level.LevelInfo;
import net.cubecraft.server.event.ServerSetupEvent;
import net.cubecraft.server.event.world.ServerWorldInitializedEvent;
import net.cubecraft.server.net.RakNetServerIO;
import net.cubecraft.server.net.ServerIO;
import net.cubecraft.server.service.Service;
import net.cubecraft.server.world.ServerWorldFactory;
import net.cubecraft.util.VersionInfo;
import net.cubecraft.util.setting.GameSetting;
import net.cubecraft.world.IWorld;

import java.net.InetSocketAddress;
import java.util.Map;

public final class CubecraftServer extends LoopTickingThread {
    public static final VersionInfo VERSION = new VersionInfo("server-0.3.2-b2");
    private static final ILogger LOGGER = LogManager.getLogger("Server");
    private final GameSetting setting = new GameSetting(EnvironmentPath.CONFIG_FOLDER + "/server_setting.toml");
    private final InetSocketAddress localAddress;
    private final ServerIO serverIO = new RakNetServerIO();
    private final PlayerTable playerTable = new PlayerTable();
    private final EventBus eventBus = new SimpleEventBus();
    private final boolean isIntegrated;
    private final Level level;
    private Map<String, Service> services;


    public CubecraftServer(InetSocketAddress localAddress, String levelName, boolean isIntegrated) {
        this.localAddress = localAddress;
        this.isIntegrated = isIntegrated;
        this.level = new Level(LevelInfo.create(levelName, 114514), new ServerWorldFactory(this));
    }

    public CubecraftServer(InetSocketAddress localAddress, Level initialLevel, boolean isIntegrated) {
        this.localAddress = localAddress;
        this.level = initialLevel;
        this.isIntegrated = isIntegrated;
    }

    @Override
    public void init() {
        long startTime = System.currentTimeMillis();

        ServerSharedContext.SERVER = this;
        this.setting.load();
        this.timer = new Timer(20);
        LOGGER.info("config loaded.");

        ModManager modManager = SharedContext.MOD;
        if (!this.isIntegrated) {
            ModLoader.loadServerInternalMod();
            ModLoader.loadServerInternalMod();
            modManager.completeModRegister(Side.SERVER);
        }

        modManager.getModLoaderEventBus().callEvent(new ServerSetupEvent(this));

        this.serverIO.start(this.localAddress.getPort(), 128);
        LOGGER.info("service started on %s.", this.localAddress);

        this.services = ServerSharedContext.SERVICE.createAll();
        for (Service service : this.services.values()) {
            try {
                service.initialize(this);
            } catch (ServerStartupFailedException e) {
                this.setRunning(false);
                return;
            } catch (Exception e) {
                LOGGER.error("find error when initializing service :(");
                LOGGER.error(e);
            }
        }
        LOGGER.info("services initialized.");

        for (IWorld world : this.level.getWorlds().values()) {
            this.getEventBus().callEvent(new ServerWorldInitializedEvent(this, world));
        }
        LOGGER.info("world loaded.");
        for (Service service : this.services.values()) {
            try {
                service.postInitialize(this);
            } catch (Exception e) {
                LOGGER.error(e);
            }
        }
        LOGGER.info("server started in %d ms.", ((System.currentTimeMillis() - startTime)));
    }

    @Override
    public void tick() {
        for (Service service : this.services.values()) {
            service.onServerTick(this);
        }
    }

    @Override
    public void stop() {
        this.serverIO.allCloseConnection();
        this.serverIO.stop();
        LOGGER.info("server net-core stopped.");

        this.level.save();

        for (Service service : this.services.values()) {
            service.stop(this);
        }
        LOGGER.info("service stopped.");

        for (Service service : this.services.values()) {
            service.postStop(this);
        }

        LoggerContext.getSharedContext().allSave();
        LOGGER.info("server stopped.");
    }

    @Override
    public boolean onException(Exception error) {
        LOGGER.error(error);
        return true;
    }

    @Override
    public boolean onError(Error error) {
        LOGGER.error(error);
        return true;
    }


    public EventBus getEventBus() {
        return eventBus;
    }

    public ServerIO getServerIO() {
        return serverIO;
    }

    public GameSetting getSetting() {
        return this.setting;
    }

    public PlayerTable getPlayers() {
        return playerTable;
    }

    public Level getLevel() {
        return this.level;
    }
}
