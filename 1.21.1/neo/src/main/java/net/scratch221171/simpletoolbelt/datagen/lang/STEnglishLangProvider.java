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
        add(Const.LangKey.MOD_MENU, "シンプルなツールベルトを追加します。");

        // アイテム
        addItem(STItems.TOOLBELT, "ツールベルト");

        // メニュー
        add(Const.LangKey.TOOLBELT_SCREEN, "ツールベルト");
        add(Const.LangKey.WHEEL_SCREEN, "ホイール");
    }
}
