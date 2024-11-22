package su.rbws.rtplayer.datasource;

import java.util.List;

import su.rbws.rtplayer.SoundItem;

// абстрактный класс источника звуков.
// источники данных - это локальная файловая система, интернет радио и т.д.

public abstract class SoundSourceAbstract {

    // возвращает элементы для отображения списка
    // parentItem = null для root уровня
    public abstract List<SoundItem> createViewableItems(SoundItem parentItem);

    // считыванеи очередной части данных
    public abstract List<SoundItem> appendViewableItems(SoundItem parentItem);

    // удаление элемента
    public abstract void removeItem(SoundItem item);

    // проверка на возможность обработки
    public abstract boolean isCanProcess(SoundItem item);
    public abstract boolean isCanProcess(String name);

    public abstract SoundItem createDefaultItem(String name);

}

