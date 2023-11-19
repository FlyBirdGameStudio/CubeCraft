package net.cubecraft.client.registry;

import net.cubecraft.client.CubecraftClient;
import net.cubecraft.client.control.InputSettingItem;
import net.cubecraft.util.setting.SettingItemRegistry;
import net.cubecraft.util.setting.item.BooleanSettingItem;
import net.cubecraft.util.setting.item.DoubleSettingItem;
import net.cubecraft.util.setting.item.IntegerSettingItem;
import net.cubecraft.util.setting.item.StringSettingItem;

public interface ClientSettingRegistry {
    @SettingItemRegistry
    IntegerSettingItem MAX_FPS = new IntegerSettingItem("render", "max_fps", 240);

    @SettingItemRegistry
    BooleanSettingItem V_SYNC = new BooleanSettingItem("render", "vsync", true);

    @SettingItemRegistry
    BooleanSettingItem FULL_SCREEN = new BooleanSettingItem("render", "fullscreen", false);

    @SettingItemRegistry
    IntegerSettingItem FXAA = new IntegerSettingItem("render", "fxaa", 0);

    @SettingItemRegistry
    DoubleSettingItem GUI_SCALE = new DoubleSettingItem("gui","scale",2.0);

    @SettingItemRegistry
    BooleanSettingItem SKIP_STUDIO_LOGO = new BooleanSettingItem("gui","skip_studio_logo",false);

    @SettingItemRegistry
    BooleanSettingItem DISABLE_CONSTANT_POOL = new BooleanSettingItem("performance","disable_constant_pool",true);

    @SettingItemRegistry
    IntegerSettingItem TICK_GC = new IntegerSettingItem("performance","gc_frequency",100);
}