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

        // メニュー
        add(Const.LangKey.TOOLBELT_SCREEN_TITLE, "Toolbelt");
        add(Const.LangKey.WHEEL_SCREEN_TITLE, "Wheel");
        add(Const.LangKey.WHEEL_TOOLTIP_STOW, "Stow All");
    }
}
