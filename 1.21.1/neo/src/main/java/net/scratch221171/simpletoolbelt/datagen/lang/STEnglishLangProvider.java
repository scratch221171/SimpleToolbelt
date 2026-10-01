package net.scratch221171.simpletoolbelt.datagen.lang;

import net.minecraft.data.PackOutput;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.registry.STItems;

public class STEnglishLangProvider extends STLangProvider {

    public STEnglishLangProvider(PackOutput output) {
        super(output, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(Const.LangKey.MOD_MENU, "Adds a simple tool belt.");

        // アイテム
        addItem(STItems.TOOLBELT, "Toolbelt");
        addItem(STItems.NETHERITE_TOOLBELT, "Netherite Toolbelt");
        add(Const.LangKey.Item.INITIALIZING_UUID, "Initializing...");
        add(Const.LangKey.Item.SHIFT_FOR_MORE_INFO, "Hold [%s] for more");
        add(Const.LangKey.Item.HINT, "Use while held, or right-click in a GUI to open the menu.");

        // スクリーン
        add(Const.LangKey.Screen.TOOLBELT_SCREEN_TITLE, "Toolbelt");
        add(Const.LangKey.Screen.WHEEL_SCREEN_TITLE, "Wheel");
        add(Const.LangKey.Screen.WHEEL_TOOLTIP_STOW, "Stow All");
        add(Const.LangKey.Screen.WHEEL_PAGE_INDEX, "Page %s / %s");

        // コマンド
        add(Const.LangKey.Commands.NOT_FOUND, "Belt not found");
        add(Const.LangKey.Commands.PRINT_ALL, "UUID stored in storage: %s");
        add(Const.LangKey.Commands.RESTORED, "Restored the belt with the following UUID: %s");
    }
}
