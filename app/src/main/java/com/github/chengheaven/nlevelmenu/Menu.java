package com.github.chengheaven.nlevelmenu;

import java.io.Serializable;
import java.util.List;

/**
 * @author Heaven_Cheng Created on 2018/2/2.
 */
public class Menu implements Serializable {

    public static final int HEADER = 0;
    public static final int TITLE = 1;
    public static final int ITEM = 2;

    /**
     * Menu id, 唯一标识符, 不能一样
     */
    private String id;
    private String name;
    private List<Menu> menu;

    /**
     * Menu 的选中状态
     */
    private boolean enabled;

    /**
     * Menu 的具体层级   0,1,2,3,4...
     */
    private int subLevel;

    /**
     * Menu 展开状态
     */
    private boolean expanded;

    /**
     * Menu 的层级，第一层级 HEADER 还是最后一层级 ITEM 或者 是其他中间层级 TITLE
     */
    private int itemType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Menu> getMenus() {
        return menu;
    }

    public void setMenus(List<Menu> menus) {
        this.menu = menus;
    }

    public int getSubLevel() {
        return subLevel;
    }

    public void setSubLevel(int subLevel) {
        this.subLevel = subLevel;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }
}
