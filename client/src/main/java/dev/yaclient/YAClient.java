package dev.yaclient;

import dev.yaclient.cosmetics.CosmeticsInstaller;
import dev.yaclient.modules.*;
import dev.yaclient.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.weavemc.api.ModInitializer;
import net.weavemc.api.event.EventBus;
import net.weavemc.api.event.RenderGameOverlayEvent;
import net.weavemc.api.event.TickEvent;
import org.lwjgl.input.Keyboard;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

/** YA CLIENT entry point (Weave loader, no Forge). Only legitimate features: no cheats. */
public class YAClient implements ModInitializer {
    public static final String NAME = "YA CLIENT";
    public static final String VERSION = "1.1.0";
    /** Key that opens the client menu (close it with ESC). */
    public static final int MENU_KEY = Keyboard.KEY_RSHIFT;

    public static final List<Module> MODULES = new ArrayList<Module>();
    private boolean menuKeyWasDown;

    @Override
    public void init() {
        Config.load();
        Lang.load(Config.get("lang", "en"));
        Theme.load();
        Hud.loadConfig();

        // HUD elements
        MODULES.add(new FpsModule());
        MODULES.add(new CpsModule());
        MODULES.add(new CoordsModule());
        MODULES.add(new KeystrokesModule());
        MODULES.add(new ArmorModule());
        MODULES.add(new PingModule());
        MODULES.add(new DirectionModule());
        MODULES.add(new ClockModule());
        MODULES.add(new SpeedModule());
        MODULES.add(new MemoryModule());
        MODULES.add(new HealthModule());
        MODULES.add(new PotionModule());
        MODULES.add(new ReachModule());
        MODULES.add(new SpotifyModule());
        MODULES.add(new WaypointsModule());
        // utilities
        MODULES.add(new DayCounterModule());
        MODULES.add(new BiomeModule());
        MODULES.add(new LightLevelModule());
        MODULES.add(new ServerAddressModule());
        MODULES.add(new SessionTimeModule());
        MODULES.add(new RotationModule());
        MODULES.add(new ItemInfoModule());
        MODULES.add(new PlayerCountModule());
        MODULES.add(new FoodModule());
        MODULES.add(new ToggleSprintModule());
        MODULES.add(new ToggleSneakModule());
        MODULES.add(new ZoomModule());
        MODULES.add(new FullbrightModule());
        MODULES.add(new MotionBlurModule());
        MODULES.add(new CrosshairModule());
        MODULES.add(new DiscordRpcModule());
        MODULES.add(new TimeChangerModule());
        MODULES.add(new ClearWeatherModule());
        MODULES.add(new HitboxesModule());
        MODULES.add(new FovModule());
        MODULES.add(new CinematicCameraModule());
        MODULES.add(new EntityCountModule());
        MODULES.add(new FallDistanceModule());
        MODULES.add(new XpLevelModule());
        MODULES.add(new ScoreboardModule());
        MODULES.add(new FpsBoostModule());
        // planned (need the Mixin phase, see docs/PHASE2.md)
        MODULES.add(new SoonModule("levelhead", Category.INFO));
        MODULES.add(new HitColorModule());
        MODULES.add(new OldAnimationsModule());
        MODULES.add(new AutoGGModule());
        MODULES.add(new MinimapModule());
        MODULES.add(new ComboModule());
        MODULES.add(new BowChargeModule());
        MODULES.add(new BlockInfoModule());
        MODULES.add(new NoHurtCamModule());
        MODULES.add(new BackgroundFpsModule());
        MODULES.add(new LowHealthModule());
        MODULES.add(new TargetHudModule());
        MODULES.add(new ItemCounterModule());
        MODULES.add(new InventoryHudModule());
        MODULES.add(new NearbyPlayersModule());

        for (Module m : MODULES) m.loadConfig();

        EventBus.subscribe(TickEvent.Post.class, e -> {
            Minecraft mc = Minecraft.getMinecraft();

            // cosmetics: add the render layer once the renderers exist, keep purchases in sync
            CosmeticsInstaller.tryInstall();
            CosmeticsApi.refreshAsync(false);

            // our title screen replaces the vanilla one
            if (mc.currentScreen != null && mc.currentScreen.getClass() == GuiMainMenu.class) {
                mc.displayGuiScreen(new MainMenuScreen());
            }

            ScoreboardModule.afterOverlay();
            TabBadge.tick();
            FriendsApi.tick();
            HitTracker.tick(mc);
            for (Module m : MODULES) {
                m.bind();
                m.onAlwaysTick();
                if (m.enabled) m.onTick();
            }

            boolean down = Keyboard.isKeyDown(MENU_KEY);
            if (down && !menuKeyWasDown && mc.currentScreen == null) {
                mc.displayGuiScreen(new HubScreen());
            }
            menuKeyWasDown = down;
        });

        EventBus.subscribe(RenderGameOverlayEvent.Pre.class, e -> ScoreboardModule.beforeOverlay());
        EventBus.subscribe(RenderGameOverlayEvent.Post.class, e -> {
            ScoreboardModule.afterOverlay();
            Hud.render();
            FriendToast.render();
        });

        System.out.println("[" + NAME + "] " + VERSION + " loaded. Press Right Shift for the menu.");
    }

    @Override
    @SuppressWarnings("deprecation")
    public void preInit(Instrumentation instrumentation) {}
}
