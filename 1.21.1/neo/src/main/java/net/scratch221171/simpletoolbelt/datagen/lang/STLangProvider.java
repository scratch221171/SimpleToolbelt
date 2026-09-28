package net.scratch221171.simpletoolbelt.datagen.lang;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.scratch221171.simpletoolbelt.Const;

public abstract class STLangProvider extends LanguageProvider {
    public STLangProvider(PackOutput output, String locale) {
        super(output, Const.MOD_ID, locale);
    }

    protected void addConfig(String configID, String name) {
        add(Const.MOD_ID + ".configuration." + configID, name);
    }

    protected void addConfigWithDesc(String configID, String name, String desc) {
        add(Const.MOD_ID + ".configuration." + configID, name);
        add(Const.MOD_ID + ".configuration." + configID + ".tooltip", desc);
    }
}
