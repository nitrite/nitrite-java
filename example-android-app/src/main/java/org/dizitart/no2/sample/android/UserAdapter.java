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
