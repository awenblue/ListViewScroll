package com.awen.listviewscroll;

import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by Awen on 2016/3/14.
 */
public class ListViewScrollHelper<T> {
    private static final String TAG = "ListViewScrollHelper";
    private static final int LIST_PAGE_SIZE = 15;

    private NextPage nextPage;
    private InitAdapter initAdapter;

    private int pageIndex = -1;
    private int totalPage;

    private ListView lv_goodsList;
    private BaseAdapter goodsAdapter;
    private List<T> goodsList = new ArrayList<>();
    private Semaphore semaphore = new Semaphore(0);

    public ListViewScrollHelper(ListView lv_goodsList, InitAdapter initAdapter, NextPage nextPage) {
        this.initAdapter = initAdapter;
        this.lv_goodsList = lv_goodsList;
        this.nextPage = nextPage;
        initView();
    }

    private void initView() {
        goodsAdapter = initAdapter.initAdapter(goodsList);
        lv_goodsList.setAdapter(goodsAdapter);
        lv_goodsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                // 判断是否到底部了
                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {
                    Log.d(TAG, String.valueOf(pageIndex));
                    if (hasNextPage() && canRequestNextPage()) {
                        pageIndex++;
                        if (nextPage != null)
                            nextPage.next();
                    } else {
                        Log.d(TAG, "列表已经到底部！");
                    }
                }
            }
        });
    }

    /**
     * 此函数将会作为分页刷新数据的主函数
     *     内部将会实现 根据当前页数判断是否重置数据 或者 继续下一页
     * 如果重置数据，初始化起始页需要使用函数 resetPageIndex()
     *
     * @param list
     * @param totalSize
     */
    public void refreshPage(List list, int totalSize) {
        if (pageIndex < 0) {
            initData(list, totalSize);
        } else {
            freshList(list);
        }
    }

    /**
     * 如果有多处控制一个listview数据内容（比如：分类，搜索都能控制商品列表展示）
     *   ，此时可以使用此函数设置分页信息
     *
     * @param list
     * @param totalSize
     */
    public void initData(List list, int totalSize) {
        pageIndex = 0;
        goodsList.clear();

        if (totalSize > LIST_PAGE_SIZE) {
            totalPage = totalSize / LIST_PAGE_SIZE;
            if (totalSize % LIST_PAGE_SIZE >= 1)
                totalPage++;
            Log.d("wbl", "totalPage:" + totalPage);
        } else {
            totalPage = 1;
        }

        freshList(list);
    }

    /**
     * 设置下一面的数据
     *
     * @param list
     * @param type （此字段废弃）
     */
    public void nextPage(List list, int type) {
        freshList(list);
    }

    /**
     * 当请求失败时，恢复上到一页
     */
    public void restoreLastPage() {
        if (!canRequestNextPage()) {
            if (pageIndex > 0) pageIndex--;
        }
        semaphore.release();
    }

    private boolean hasNextPage() {
        return pageIndex < totalPage - 1;
    }

    public int getPageIndex() {
        return pageIndex < 0 ? 0 : pageIndex;
    }

    public int getPageIndex(boolean hasCategoryChange) {
        if (hasCategoryChange)
            pageIndex = 0;
        return getPageIndex();
    }

    /**
     * 重置分页参数
     */
    public void resetPageIndex() {
        pageIndex = -1;
        totalPage = 0;
    }

    public void setEmptyView(View view) {
        lv_goodsList.setEmptyView(view);
    }

    /**
     * @param list 传入要显示的list
     */
    private void freshList(List list) {
        if (list != null && list.size() > 0) {
            goodsList.addAll(list);
        }
        goodsAdapter.notifyDataSetChanged();
        semaphore.release();
    }

    private boolean canRequestNextPage() {
        return semaphore.tryAcquire();
    }

    public List<T> getGoodsList() {
        return goodsList;
    }

    public interface NextPage {
        void next();
    }

    public interface InitAdapter<T> {
        BaseAdapter initAdapter(List<T> list);
    }
}
