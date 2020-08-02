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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
 *
 *  * Copyright (c) 2018 Davide Wietlisbach on 16.11.18 20:57
 *
 */

public class SelectQuery extends  Query{

    public SelectQuery(SQLCoinStorage storage, String query) {
        super(storage, query);
    }

    public SelectQuery where(String key, Object value) {
        if(!and){
            query += " WHERE";
            and = true;
        }else query += " AND";
        query += " `"+key+"`=?";
        values.add(value);
        return this;
    }

    public SelectQuery whereWithOr(String key, Object value) {
        if(!and){
            query += " WHERE";
            and = true;
        }else query += " or";
        query += " `"+key+"`=?";
        values.add(value);
        return this;
    }

    public SelectQuery noCase(){
        query += " COLLATE NOCASE";
        return this;
    }

    public <R> R execute(SelectResult<R> consumer) {
        try {
            return executeExcept(consumer);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <R> R executeExcept(SelectResult<R> consumer) throws SQLException{
        try(Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            int i = 1;
            for (Object object : values) {
                statement.setString(i, object.toString());
                i++;
            }
            ResultSet result =  statement.executeQuery();
            R object = consumer.get(result);
            result.close();
            statement.close();
            return object;
        }
    }

    public interface SelectResult<R> {

        R get(ResultSet result) throws SQLException;

    }
}
