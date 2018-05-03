/*
 * Tencent is pleased to support the open source community by making  XiaoweiSDK Demo Codes available.
 *
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.tencent.aiaudio.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.LinkedList;
import java.util.List;

/**
 * @param <E>
 */
public abstract class CommonListAdapter<E> extends BaseAdapter {

    public LinkedList<E> list = new LinkedList<E>();

    public E getLast() {
        return list.getLast();
    }

    public void add(E item) {
        if (item == null) {
            return;
        }
        list.add(item);
    }

    public void add(int index, E item) {
        list.add(index, item);
    }

    public void addAll(E[] list) {
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.length; i++) {
            add(list[i]);
        }
    }

    public void addAll(List<E> list) {
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            add(list.get(i));
        }
    }

    /**
     * E一般需要自己实现equals，不然这个替换没意义
     *
     * @param item
     */
    public void replace(E item) {
        int location = list.indexOf(item);
        if (location >= 0) {
            list.remove(location);
            list.add(location, item);
        } else {
            list.add(item);
        }
    }

    /**
     * 不存在才添加
     *
     * @param item
     */
    public void addIfNecessary(E item) {
        if (!list.contains(item)) {
            list.add(item);
        }
    }

    public void addFirst(E item) {
        list.addFirst(item);
    }

    public E remove(int position) {
        return list.remove(position);
    }

    public boolean remove(E item) {
        return list.remove(item);
    }

    public E removeLast() {
        return list.removeLast();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public E getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        list.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return initListCell(position, convertView, parent);
    }

    protected abstract View initListCell(int position, View convertView,
                                         ViewGroup parent);
}
