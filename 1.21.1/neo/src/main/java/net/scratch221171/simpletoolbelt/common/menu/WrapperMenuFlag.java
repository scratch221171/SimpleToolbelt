package net.scratch221171.simpletoolbelt.common.menu;

/**
 * ラッパー経由で開いたメニューの目印。AbstractContainerMenuMixinがAbstractContainerMenuに実装を注入する。
 * ※ mixinパッケージ内のクラスは通常コードから参照できないため、このインターフェースはmixinパッケージの外に置く。
 */
public interface WrapperMenuFlag {

    boolean simpletoolbelt$isWrapperMenu();

    void simpletoolbelt$setWrapperMenu(boolean value);

    static boolean is(Object menu) {
        return menu instanceof WrapperMenuFlag flag && flag.simpletoolbelt$isWrapperMenu();
    }
}
