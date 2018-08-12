/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.sample.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * @author Anindya Chatterjee.
 */
public class UserAdapter extends BaseAdapter {
    private List<User> users;
    LayoutInflater mInflator;
    Context mContext;

    public UserAdapter(Context context) {
        mContext = context;
        mInflator = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    @Override
    public int getCount() {
        return users != null ? users.size() : 0;
    }

    @Override
    public User getItem(int i) {
        return users.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {

        Viewholder holder;

        final User t = getItem(position);

        if (convertView == null) {
            holder = new Viewholder();
            convertView = mInflator.inflate(R.layout.row_user, container, false);
            holder.username = (TextView) convertView.findViewById(R.id.username);
            convertView.setTag(holder);

        } else {
            holder = (Viewholder) convertView.getTag();
        }

        holder.username.setText(t.getUsername());

        return convertView;
    }

    private class Viewholder {
        TextView username;
    }
}
