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

public class InsertQuery extends Query{

    public InsertQuery(SQLCoinStorage storage, String query) {
        super(storage, query);
    }

    public InsertQuery insert(String insert) {
        query += "`"+insert+"`,";
        return this;
    }

    public InsertQuery value(Object value) {
        query = query.substring(0, query.length() - 1);
        if(firstvalue){
            query += ") VALUES (?)";
            firstvalue = false;
        }else query += ",?)";
        values.add(value);
        return this;
    }

    public void execute(){
        try(Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            int i = 1;
            for (Object object : values) {
                statement.setString(i, object.toString());
                i++;
            }
            statement.executeUpdate();
            statement.close();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Object executeAndGetKey(){
        try(Connection connection = getConnection()) {
            PreparedStatement pstatement = connection.prepareStatement(query,PreparedStatement.RETURN_GENERATED_KEYS);
            int i = 1;
            for(Object object : values) {
                pstatement.setString(i, object.toString());
                i++;
            }
            pstatement.executeUpdate();
            ResultSet result = pstatement.getGeneratedKeys();
            if(result != null){
                if(result.next()) return result.getObject(1);
                result.close();
            }
            pstatement.close();
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int executeAndGetKeyInInt(){
        try(Connection connection = getConnection()) {
            PreparedStatement pstatement = connection.prepareStatement(query,PreparedStatement.RETURN_GENERATED_KEYS);
            int i = 1;
            for(Object object : values) {
                pstatement.setString(i, object.toString());
                i++;
            }
            pstatement.executeUpdate();
            ResultSet result = pstatement.getGeneratedKeys();
            if(result != null && result.next()) return result.getInt(1);
            if(result != null) result.close();
            pstatement.close();
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
