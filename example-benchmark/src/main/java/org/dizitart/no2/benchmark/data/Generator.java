/*
 * Copyright 2017 Nitrite author or authors.
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
 */

package org.dizitart.no2.benchmark.data;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * @author Anindya Chatterjee.
 */
public class Generator {
    private static Generator ourInstance = new Generator();
    public static Generator getInstance() {
        return ourInstance;
    }

    private SecureRandom random = new SecureRandom();

    private Generator() {
    }

    public Person[] create(int count){
        Person[] persons = new Person[count];

        for (int i = 0; i < count; i++) {
            persons[i] = createPerson();
        }

        return persons;
    }

    private Person createPerson() {
        Person person = new Person();

        person.setFirstName(randomString());
        person.setLastName(randomString());
        person.setPersonalNote(randomText());
        person.setAddresses(new ArrayList<Address>());

        PrivateData privateData = new PrivateData();
        privateData.setUsername(randomAlphaNumericString());
        privateData.setPassword(randomAlphaNumericString());
        person.setPrivateData(privateData);

        int count = random.nextInt(5);
        if (count == 0) count = 2;
        for (int i = 0; i < count; i++) {
            Address address = new Address();
            address.setStreet(randomAlphaNumericString());
            address.setZip(Integer.toString(random.nextInt(99999)));
            person.getAddresses().add(address);
        }
        person.setDefaultAddress(person.getAddresses().get(0));

        return person;
    }


    private String randomString() {
        int length = random.nextInt(10);
        String alphabets = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        return RandomStringUtils.random(length, 0, alphabets.length(), true, false, alphabets.toCharArray(), random);
    }

    private String randomAlphaNumericString() {
        int length = random.nextInt(10);
        String alphanumerics = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        return RandomStringUtils.random(length, 0, alphanumerics.length(), true, true, alphanumerics.toCharArray(), random);
    }

    private String randomText() {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream("test.text");
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

        String strLine;
        int line = random.nextInt(49);
        int count = 0;
        try {
            while ((strLine = br.readLine()) != null) {
                if (count == line) {
                    return strLine;
                }
                count++;
            }
        } catch (IOException e) {
            // ignore
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                // ignore
            }
        }
        return null;
    }
}
