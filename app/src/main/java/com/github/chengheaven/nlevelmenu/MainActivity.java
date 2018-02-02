package com.github.chengheaven.nlevelmenu;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Heaven・Cheng Created on 2018/2/2.
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.recycler)
    RecyclerView mRecycler;
    private RecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RecyclerAdapter(new ArrayList<>(), mListener);
        mRecycler.setAdapter(mAdapter);

        initMenu(this);
    }

    /**
     * 初始化 数据
     */
    private void initMenu(Context context) {
        String s = SystemUtil.getAssetsFile(context, "menu.json");
        List<Menu> list = new Gson().fromJson(s, new TypeToken<List<Menu>>() {
        }.getType());
        recurseResolveMenus(list, 0);
        mAdapter.update(list);
    }

    /**
     * 通过循环 list 给 所以 Menu 添加 具体层级
     *
     * @param list     Menu List
     * @param subLevel 具体层级
     */
    private void recurseResolveMenus(List<Menu> list, int subLevel) {
        for (Menu menu : list) {
            resolveMenus(menu, subLevel);
        }
    }

    private void resolveMenus(Menu menu, int subLevel) {
        if (menu.getMenus() == null) {
            menu.setItemType(Menu.ITEM);
            menu.setSubLevel(subLevel);
        } else {
            if (subLevel == 0) {
                menu.setItemType(Menu.HEADER);
            } else {
                menu.setItemType(Menu.TITLE);
            }
            menu.setSubLevel(subLevel);
            subLevel++;
            if (menu.getMenus() != null) {
                recurseResolveMenus(menu.getMenus(), subLevel);
            }
        }
    }

    /**
     * 得到 选中状态的 最下层的 Menu
     *
     * @param menu 需要获取最下层 选中状态 Menu 个数的 Menu
     * @return count 选中状态的 Menu 个数
     */
    private static int getMenuSelect(Menu menu) {
        int count = 0;
        if (menu.getMenus() != null) {
            for (int i = 0; i < menu.getMenus().size(); i++) {
                count += getMenuSelect(menu.getMenus().get(i));
            }
        } else {
            if (menu.isEnabled()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 将 该级 Menu 及其以下的 Menu 设为 收缩状态
     *
     * @param menu 点击的 menu
     */
    private void setMenuReduce(Menu menu) {
        menu.setExpanded(false);
        if (menu.getMenus() != null) {
            for (Menu l : menu.getMenus()) {
                setMenuReduce(l);
            }
        }
    }

    /**
     * 是否选中
     *
     * @param menu 点击的 Menu
     * @param flag 是否选中
     */
    private void setMenuEnable(Menu menu, boolean flag) {
        menu.setEnabled(flag);
        if (menu.getMenus() != null) {
            for (Menu m : menu.getMenus()) {
                setMenuEnable(m, flag);
            }
        }
    }

    /**
     * 得到 最下层的 Menu 个数
     *
     * @param menu 需要获取最下层 Menu 个数的 Menu
     * @return count Menu 总数
     */
    private static int getMenusCount(Menu menu) {
        int count = 0;
        if (menu.getMenus() != null) {
            for (int i = 0; i < menu.getMenus().size(); i++) {
                count += getMenusCount(menu.getMenus().get(i));
            }
        } else {
            count++;
        }
        return count;
    }

    private RecyclerAdapter.OnListItemClickListener mListener = new RecyclerAdapter.OnListItemClickListener() {
        @Override
        public void onItemExpandedClick(int position) {
            List<Menu> list = SystemUtil.deepCopy(mAdapter.getData());
            Menu menu = list.get(position);
            if (menu.isExpanded()) {
                int index = position + 1;
                if (menu.getItemType() == Menu.HEADER) {
                    while (list.size() > index
                            && list.get(index).getItemType() != Menu.HEADER
                            && list.get(index).getSubLevel() != 0) {
                        list.remove(index);
                    }
                } else if (menu.getItemType() == Menu.TITLE) {
                    while (list.size() > index
                            && list.get(index).getSubLevel() > menu.getSubLevel()
                            && list.get(index).getItemType() != Menu.HEADER) {
                        list.remove(index);
                    }
                }
                setMenuReduce(menu);
                menu.setExpanded(false);
            } else {
                int index = position + 1;
                for (Menu m : menu.getMenus()) {
                    list.add(index, m);
                    index++;
                }
                menu.setExpanded(true);
            }
            mAdapter.update(list);
        }

        @Override
        public void onItemControlClick(int position) {
            List<Menu> list = SystemUtil.deepCopy(mAdapter.getData());
            Menu menu = list.get(position);

            if (menu.isEnabled()) {
                setMenuEnable(menu, false);
            } else {
                setMenuEnable(menu, true);
            }

            for (int i = position; i > 0; i--) {
                if (list.get(position).getSubLevel() > list.get(i - 1).getSubLevel()) {
                    int c = getMenuSelect(list.get(i - 1));
                    if (c == 0) {
                        setMenuEnable(list.get(i - 1), false);
                    } else {
                        list.get(i - 1).setEnabled(true);
                    }
                }
            }
            mAdapter.update(list);
        }
    };

    static class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        private static final int LEVEL_EXPAND = 30;

        private List<Menu> mList;
        private OnListItemClickListener mListener;

        RecyclerAdapter(List<Menu> list, OnListItemClickListener listener) {
            this.mList = list;
            this.mListener = listener;
        }

        void update(List<Menu> list) {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffCallback(mList, list), true);
            result.dispatchUpdatesTo(this);
            mList = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Menu menu = mList.get(position);
            holder.mItemTitle.setText(menu.getName());
            switch (menu.getItemType()) {
                case Menu.HEADER:
                    holder.mItemTitle.setTextSize(16);
                    holder.mItemSelectLayout.setVisibility(View.VISIBLE);
                    holder.mItemNumber.setVisibility(View.VISIBLE);
                    holder.mItemExpandLayout.setVisibility(View.VISIBLE);
                    holder.mItemSwitchLayout.setVisibility(View.GONE);
                    int count = getMenusCount(menu);
                    int select = getMenuSelect(menu);
                    holder.mItemNumber.setText(String.format(Locale.getDefault(), "%d / %d", select, count));
                    if (menu.isExpanded()) {
                        holder.mItemExpand.setBackgroundResource(R.drawable.arrow_up);
                    } else {
                        holder.mItemExpand.setBackgroundResource(R.drawable.arrow_down);
                    }
                    if (menu.isEnabled()) {
                        holder.mItemSelect.setBackgroundResource(R.drawable.radiobutton);
                    } else {
                        holder.mItemSelect.setBackgroundResource(R.drawable.radiobutton_outline);
                    }

                    holder.mItemSelectLayout.setOnClickListener(v -> mListener.onItemControlClick(holder.getAdapterPosition()));
                    holder.mItemExpandLayout.setOnClickListener(v -> mListener.onItemExpandedClick(holder.getAdapterPosition()));

                    break;

                case Menu.TITLE:
                    holder.mItemTitle.setTextSize(15);
                    holder.mItemSelectLayout.setVisibility(View.VISIBLE);
                    holder.mItemNumber.setVisibility(View.GONE);
                    holder.mItemExpandLayout.setVisibility(View.GONE);
                    holder.mItemSwitchLayout.setVisibility(View.VISIBLE);
                    if (menu.isExpanded()) {
                        holder.mItemSelect.setBackgroundResource(R.drawable.circle_minus);
                    } else {
                        holder.mItemSelect.setBackgroundResource(R.drawable.circle_plus);
                    }
                    if (menu.isEnabled()) {
                        holder.mItemSwitch.setBackgroundResource(R.drawable.switch_on);
                    } else {
                        holder.mItemSwitch.setBackgroundResource(R.drawable.switch_off);
                    }
                    holder.mGroup.setPadding(LEVEL_EXPAND * menu.getSubLevel(), 0, 0, 0);

                    holder.mItemSelectLayout.setOnClickListener(v -> mListener.onItemExpandedClick(holder.getAdapterPosition()));
                    holder.mItemSwitchLayout.setOnClickListener(v -> mListener.onItemControlClick(holder.getAdapterPosition()));

                    break;

                case Menu.ITEM:
                    if (menu.getSubLevel() > 1) {
                        holder.mItemTitle.setTextSize(14);
                    } else {
                        holder.mItemTitle.setTextSize(15);
                    }
                    holder.mItemSelectLayout.setVisibility(View.GONE);
                    holder.mItemNumber.setVisibility(View.GONE);
                    holder.mItemExpandLayout.setVisibility(View.GONE);
                    holder.mItemSwitchLayout.setVisibility(View.VISIBLE);

                    if (menu.isEnabled()) {
                        holder.mItemSwitch.setBackgroundResource(R.drawable.switch_on);
                    } else {
                        holder.mItemSwitch.setBackgroundResource(R.drawable.switch_off);
                    }

                    holder.mGroup.setPadding(100 + LEVEL_EXPAND * menu.getSubLevel(), 0, 0, 0);

                    holder.mItemSwitchLayout.setOnClickListener(v -> mListener.onItemControlClick(holder.getAdapterPosition()));

                    break;

                default:
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mList.get(position).getItemType();
        }

        List<Menu> getData() {
            return mList;
        }

        /**
         * Item 点击监听
         */
        interface OnListItemClickListener {

            /**
             * 展开、收缩 Item
             *
             * @param position Item 在当前 RecyclerView 中的位置
             */
            void onItemExpandedClick(int position);

            /**
             * 选中 Item
             *
             * @param position Item 在当前RecyclerView中的位置
             */
            void onItemControlClick(int position);
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.item_select)
            ImageView mItemSelect;
            @BindView(R.id.item_select_layout)
            RelativeLayout mItemSelectLayout;
            @BindView(R.id.item_title)
            TextView mItemTitle;
            @BindView(R.id.item_number)
            TextView mItemNumber;
            @BindView(R.id.item_expand)
            ImageView mItemExpand;
            @BindView(R.id.item_expand_layout)
            RelativeLayout mItemExpandLayout;
            @BindView(R.id.item_switch)
            ImageView mItemSwitch;
            @BindView(R.id.item_switch_layout)
            RelativeLayout mItemSwitchLayout;
            @BindView(R.id.item_group)
            RelativeLayout mGroup;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        class DiffCallback extends DiffUtil.Callback {

            private List<Menu> mOldMenus = new ArrayList<>();
            private List<Menu> mNewMenus = new ArrayList<>();

            DiffCallback(List<Menu> oldMenus, List<Menu> newMenus) {
                this.mOldMenus = oldMenus;
                this.mNewMenus = newMenus;
            }

            @Override
            public int getOldListSize() {
                return mOldMenus == null ? 0 : mOldMenus.size();
            }

            @Override
            public int getNewListSize() {
                return mNewMenus == null ? 0 : mNewMenus.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return mOldMenus.get(oldItemPosition).getId().equals(mNewMenus.get(newItemPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {

                String oldName = mOldMenus.get(oldItemPosition).getName();
                String newName = mNewMenus.get(newItemPosition).getName();

                if (!oldName.equals(newName)) {
                    return false;
                }

                int oldCount = getMenuSelect(mOldMenus.get(oldItemPosition));
                int newCount = getMenuSelect(mNewMenus.get(newItemPosition));

                if (oldCount != newCount) {
                    return false;
                }

                boolean oldVisible = mOldMenus.get(oldItemPosition).isEnabled();
                boolean newVisible = mNewMenus.get(newItemPosition).isEnabled();

                if (mOldMenus.get(oldItemPosition).getItemType() != Menu.ITEM
                        && mNewMenus.get(newItemPosition).getItemType() != Menu.ITEM) {

                    boolean isOldExpand = mOldMenus.get(oldItemPosition).isExpanded();
                    boolean isNewExpand = mNewMenus.get(newItemPosition).isExpanded();

                    if (oldVisible != newVisible) {
                        return false;
                    }

                    if (isOldExpand != isNewExpand) {
                        return false;
                    }

                }

                return oldVisible == newVisible;
            }
        }
    }
}
