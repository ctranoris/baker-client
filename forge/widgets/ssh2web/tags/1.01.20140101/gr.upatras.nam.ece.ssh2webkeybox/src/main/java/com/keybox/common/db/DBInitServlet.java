/**
 * Copyright 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.keybox.common.db;

import com.keybox.manage.model.Auth;
import com.keybox.manage.util.DBUtils;
import com.keybox.manage.util.EncryptionUtil;
import com.keybox.manage.util.SSHUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Initial startup task.  Creates an SQLite DB and generates
 * the system public/private key pair if none exists
 */
@WebServlet(name = "DBInitServlet",
        urlPatterns = {"/config"},
        loadOnStartup = 1)
public class DBInitServlet extends javax.servlet.http.HttpServlet {

    /**
     * task init method that created DB and generated public/private keys
     *
     * @param config task config
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException {

        super.init(config);
        continueInit(config); //removed for now to start in windows
    }
    

    public void continueInit(ServletConfig config) throws ServletException {

        try {
            Connection connection = DBUtils.getConn();
            Statement statement = connection.createStatement();

            ResultSet rs = statement.executeQuery("select * from information_schema.tables where upper(table_name) = 'USERS' and table_schema='PUBLIC'");
            if (rs == null || !rs.next()) {
                statement.executeUpdate("create table if not exists users (id INTEGER PRIMARY KEY AUTO_INCREMENT, first_nm varchar, last_nm varchar, email varchar, username varchar not null, password varchar, auth_token varchar, enabled boolean not null default true, user_type varchar not null default '" + Auth.ADMINISTRATOR + "')");
                //insert default admin user
                statement.executeUpdate("insert into users (username, password, user_type) values('admin', '" + EncryptionUtil.hash("changeme") + "', '" + Auth.MANAGER + "')");

                statement.executeUpdate("create table if not exists system (id INTEGER PRIMARY KEY AUTO_INCREMENT, display_nm varchar not null, user varchar not null, host varchar not null, port INTEGER not null, authorized_keys varchar not null, status_cd varchar not null default 'INITIAL')");
                statement.executeUpdate("create table if not exists profiles (id INTEGER PRIMARY KEY AUTO_INCREMENT, nm varchar not null, desc varchar not null)");
                statement.executeUpdate("create table if not exists system_map (profile_id INTEGER, system_id INTEGER, foreign key (profile_id) references profiles(id) on delete cascade , foreign key (system_id) references system(id) on delete cascade, primary key (profile_id, system_id))");
                statement.executeUpdate("create table if not exists user_map (user_id INTEGER, profile_id INTEGER, foreign key (user_id) references users(id) on delete cascade, foreign key (profile_id) references profiles(id) on delete cascade, primary key (user_id, profile_id))");
                statement.executeUpdate("create table if not exists application_key (id INTEGER PRIMARY KEY AUTO_INCREMENT, public_key varchar not null, private_key varchar not null, passphrase varchar unique not null)");

                statement.executeUpdate("create table if not exists status (id INTEGER, user_id INTEGER, status_cd varchar not null default 'INITIAL', foreign key (id) references system(id) on delete cascade, foreign key (user_id) references users(id) on delete cascade)");
                statement.executeUpdate("create table if not exists scripts (id INTEGER PRIMARY KEY AUTO_INCREMENT, user_id INTEGER, display_nm varchar not null, script varchar not null, foreign key (user_id) references users(id) on delete cascade)");


                statement.executeUpdate("create table if not exists public_keys (id INTEGER PRIMARY KEY AUTO_INCREMENT, key_nm varchar not null, public_key varchar, profile_id INTEGER, foreign key (profile_id) references profiles(id) on delete cascade  )");

                statement.executeUpdate("create table if not exists session_log (id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id INTEGER, session_tm timestamp default CURRENT_TIMESTAMP, foreign key (user_id) references users(id) on delete cascade )");
                statement.executeUpdate("create table if not exists terminal_log (session_id BIGINT, system_id INTEGER, output varchar not null, log_tm timestamp default CURRENT_TIMESTAMP, foreign key (session_id) references session_log(id) on delete cascade, foreign key (system_id) references system(id) on delete cascade)");

                System.out.println("Generating KeyBox SSH public/private key pair");
                //generate new key and insert passphrase

                //generate application pub/pvt key and get vales
                String passphrase=SSHUtil.keyGen();
                String publicKey=SSHUtil.getPublicKey();
                String privateKey=SSHUtil.getPrivateKey();

                //delete ssh keys
                SSHUtil.deleteSshKeys();

                statement.executeUpdate("insert into application_key (public_key, private_key, passphrase) values('"+publicKey+"', '" + EncryptionUtil.encrypt(privateKey)  + "','"+EncryptionUtil.encrypt(passphrase)+"')");
                System.out.println("KeyBox Public Key:");
                System.out.println(publicKey);

                passphrase=null;
                publicKey=null;
                privateKey=null;

            }

            DBUtils.closeRs(rs);
            DBUtils.closeStmt(statement);
            DBUtils.closeConn(connection);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
