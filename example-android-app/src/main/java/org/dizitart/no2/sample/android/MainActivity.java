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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.event.ChangeInfo;
import org.dizitart.no2.event.ChangeListener;
import org.dizitart.no2.filters.ObjectFilters;
import org.dizitart.no2.util.Iterables;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ProgressBar progressBar;
    UserAdapter adapter;
    ChangeListener listener;

    private Nitrite db;
    private ObjectRepository<User> repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        ListView userList = (ListView) findViewById(R.id.userlist);
        adapter = new UserAdapter(this);
        userList.setAdapter(adapter);

        if (db == null) {
            progressBar.setVisibility(View.VISIBLE);
            String fileName = getFilesDir().getPath() + "/test.db";
            Log.i("Nitrite", "Nitrite file - " + fileName);
            db = Nitrite.builder()
                    .filePath(fileName)
                    .openOrCreate("test-user", "test-password");
            repository = db.getRepository(User.class);

            listener = new ChangeListener() {
                @Override
                public void onChange(ChangeInfo changeInfo) {
                    Iterable<User> users = repository.find().project(User.class);
                    adapter.setUsers(Iterables.toList(users));
                    adapter.notifyDataSetChanged();
                }
            };

            repository.register(listener);
            progressBar.setVisibility(View.INVISIBLE);
            getUsers();
        }
    }

    private void getUsers() {
        List<User> users = Iterables.toList(repository.find().project(User.class));
        if (users == null)
            users = new ArrayList<>();

        adapter.setUsers(users);
        adapter.notifyDataSetChanged();
    }

    private void addUser() {
        User user = new User("user "+System.currentTimeMillis(), "");
        repository.insert(user);
    }

    private void flushUsers() {
        repository.remove(ObjectFilters.ALL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        repository.deregister(listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.add_user) {
            addUser();
            return true;
        }

        if (id == R.id.flush_user) {
            flushUsers();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
