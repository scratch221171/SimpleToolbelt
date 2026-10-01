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
        addItem(STItems.NETHERITE_TOOLBELT, "ネザライトのツールベルト");
        add(Const.LangKey.Item.INITIALIZING_UUID, "初期化中...");
        add(Const.LangKey.Item.SHIFT_FOR_MORE_INFO, "[%s]を押してさらに表示");
        add(Const.LangKey.Item.HINT, "手に持って使用するか、GUI内で右クリックしてメニューを開きます。");

        // スクリーン
        add(Const.LangKey.Screen.TOOLBELT_SCREEN_TITLE, "ツールベルト");
        add(Const.LangKey.Screen.WHEEL_SCREEN_TITLE, "ホイール");
        add(Const.LangKey.Screen.WHEEL_TOOLTIP_STOW, "アイテムを片付ける");
        add(Const.LangKey.Screen.WHEEL_PAGE_INDEX, "ページ %s / %s");

        // コマンド
        add(Const.LangKey.Commands.NOT_FOUND, "ベルトが見つかりません");
        add(Const.LangKey.Commands.PRINT_ALL, "以下のUUIDが保存されています: %s");
        add(Const.LangKey.Commands.RESTORED, "以下のUUIDのベルトを復元しました: %s");
    }
}
