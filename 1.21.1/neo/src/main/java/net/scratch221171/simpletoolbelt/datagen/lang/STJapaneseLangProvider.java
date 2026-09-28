package net.scratch221171.simpletoolbelt.datagen.lang;

import net.minecraft.data.PackOutput;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.registry.STItems;

public class STJapaneseLangProvider extends STLangProvider {

    public STJapaneseLangProvider(PackOutput output) {
        super(output, "ja_jp");
    }

    @Override
    protected void addTranslations() {
        add(Const.LangKey.MOD_MENU, "シンプルなツールベルトを追加します。");

        // アイテム
        addItem(STItems.TOOLBELT, "ツールベルト");

        // メニュー
        add(Const.LangKey.TOOLBELT_SCREEN_TITLE, "ツールベルト");
        add(Const.LangKey.WHEEL_SCREEN_TITLE, "ホイール");
        add(Const.LangKey.WHEEL_TOOLTIP_STOW, "取り出したアイテムを片付ける");
    }
}
