/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 02.08.20, 20:44
 * @web %web%
 *
 * The DKCoins Project is under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package ch.dkrieger.coinsystem.core.storage.storage.sql.query;

import ch.dkrieger.coinsystem.core.storage.storage.sql.SQLCoinStorage;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;

/*
 *
 *  * Copyright (c) 2018 Davide Wietlisbach on 16.11.18 20:57
 *
 */

public class Query {

    protected SQLCoinStorage storage;
    protected String query;
    protected boolean firstvalue;
    protected boolean and;
    protected boolean comma;
    protected List<Object> values;

    public Query(SQLCoinStorage storage, String query){
        this.storage = storage;
        this.query = query;
        this.firstvalue = true;
        this.comma = false;
        this.and = false;
        this.values = new LinkedList<>();
    }

    public Connection getConnection() {
        return storage.getConnection();
    }

    public String toString(){
        return this.query;
    }

    public List<Object> getValues(){
        return this.values;
    }
}
