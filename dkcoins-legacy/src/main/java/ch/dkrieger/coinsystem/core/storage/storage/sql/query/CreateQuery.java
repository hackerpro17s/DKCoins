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
import java.sql.SQLException;
import java.sql.Statement;

/*
 *
 *  * Copyright (c) 2018 Davide Wietlisbach on 16.11.18 20:57
 *
 */

public class CreateQuery extends Query {

    public CreateQuery(SQLCoinStorage storage, String query){
        super(storage, query);
        firstvalue = true;
    }

    public CreateQuery create(String value) {
        if(!firstvalue) query = query.substring(0,query.length()-1)+",";
        else firstvalue = false;
        query += value+")";
        return this;
    }

    public void execute(){
        try(Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute(query);
            statement.close();
        } catch(SQLException exception){
            exception.printStackTrace();
        }
    }
}
