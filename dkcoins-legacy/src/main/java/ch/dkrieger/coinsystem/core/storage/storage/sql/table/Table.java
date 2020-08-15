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

package ch.dkrieger.coinsystem.core.storage.storage.sql.table;

/*
 *
 *  * Copyright (c) 2018 Davide Wietlisbach on 16.11.18 20:57
 *
 */

import ch.dkrieger.coinsystem.core.storage.storage.sql.SQLCoinStorage;
import ch.dkrieger.coinsystem.core.storage.storage.sql.query.*;

public class Table {

    private String name;
    private SQLCoinStorage sql;

    public Table(SQLCoinStorage sql, String name){
        this.sql = sql;
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    public CreateQuery create(){
        return new CreateQuery(sql,"CREATE TABLE IF NOT EXISTS `"+this.name+"` (");
    }
    public InsertQuery insert(){
        return new InsertQuery(sql,"INSERT INTO `"+this.name+"` (");
    }
    public UpdateQuery update(){
        return new UpdateQuery(sql,"UPDATE `"+this.name+"` SET");
    }
    public SelectQuery select(){
        return select("*");
    }
    public SelectQuery select(String selection){
        return new SelectQuery(sql, "SELECT "+selection+" FROM `"+this.name+"`");
    }
    public DeleteQuery delete(){
        return new DeleteQuery(sql, "DELETE FROM `"+this.name+"`");
    }
    public CustomQuery query(){
        return new CustomQuery(sql);
    }
}
