package com.bmt.MyStore.repositories;

import com.bmt.MyStore.models.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
@EnableJpaRepositories
public class ClientsRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Client>  getClients(){
        var clients = new ArrayList<Client>();

        String sql = "SELECT * FROM clients ORDER BY id DESC";
        SqlRowSet rows = jdbcTemplate.queryForRowSet(sql);

        while(rows.next()){
            Client client = new Client();
            client.setId(rows.getInt("id"));
            client.setFirstName(rows.getString("firstname"));
            client.setLastName(rows.getString("lastname"));
            client.setEmail(rows.getString("email"));
            client.setPhone(rows.getString("phone"));
            client.setAddress(rows.getString("address"));
            client.setCreatedAt(rows.getString("created_at"));

            clients.add(client);

        }


    return clients;
    }

 public Client getClient (int id) {
     String sql = "SELECT * FROM clients WHERE id=?";
     SqlRowSet rows = jdbcTemplate.queryForRowSet(sql, id);

     if (rows.next()) {
         Client client = new Client();
         client.setId(rows.getInt("id"));
         client.setFirstName(rows.getString("firstname"));
         client.setLastName(rows.getString("lastname"));
         client.setEmail(rows.getString("email"));
         client.setPhone(rows.getString("phone"));
         client.setAddress(rows.getString("address"));
         client.setCreatedAt(rows.getString("created_at"));


         return client;


     }
     return null;
 }

    public Client getClient (String email) {
        String sql = "SELECT * FROM clients WHERE email=?";
        SqlRowSet rows = jdbcTemplate.queryForRowSet(sql, email);

        if (rows.next()) {
            Client client = new Client();
            client.setId(rows.getInt("id"));
            client.setFirstName(rows.getString("firstname"));
            client.setLastName(rows.getString("lastname"));
            client.setEmail(rows.getString("email"));
            client.setPhone(rows.getString("phone"));
            client.setAddress(rows.getString("address"));
            client.setCreatedAt(rows.getString("created_at"));


            return client;


        }
        return null;
    }

public Client createClient(Client client) {
    String sql = "INSERT INTO clients (firstname, lastname, email, phone, address, created_at) VALUES (?, ?, ?, ?, ?, ?)";

    int count = jdbcTemplate.update(sql, client.getFirstName(), client.getLastName(), client.getEmail(),
            client.getPhone(), client.getAddress(), client.getCreatedAt());

    if(count>0) {

        int id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        return getClient(id);

    }
        return null;
}

public Client updateClient(Client client) {

    String sql = "UPDATE clients SET firstname=?, lastname=?, email=?, phone=?, address=?, created_at=? WHERE id=?";

    jdbcTemplate.update(sql, client.getFirstName(), client.getLastName(), client.getEmail(),
            client.getPhone(), client.getAddress(), client.getCreatedAt(), client.getId());

    return getClient(client.getId());
}

public void deleteClient (int id){
        String sql = "DELETE FROM clients WHERE id=?";
        jdbcTemplate.update(sql,id);
}

    public List<Client> getClientsSortedByNameAsc() {
        String sql = "SELECT * FROM clients ORDER BY firstname ASC, lastname ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Client.class));
    }

    public List<Client> getClientsSortedByNameDesc() {
        String sql = "SELECT * FROM clients ORDER BY firstname DESC, lastname DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Client.class));
    }

    public List<Client> getClientsSortedByPhoneAsc() {
        String sql = "SELECT * FROM clients ORDER BY phone ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Client.class));
    }

    public List<Client> getClientsSortedByPhoneDesc() {
        String sql = "SELECT * FROM clients ORDER BY phone DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Client.class));
    }



    public List<Client> getClientsFiltered(String name, String email, String phone) {
        StringBuilder query = new StringBuilder("SELECT * FROM clients WHERE 1=1");

        List<Object> params = new ArrayList<>();

        if (StringUtils.hasText(name)) {
            query.append(" AND (firstname LIKE ? OR lastname LIKE ?)");
            String namePattern = "%" + name + "%";
            params.add(namePattern);
            params.add(namePattern);
        }
        if (StringUtils.hasText(email)) {
            query.append(" AND email LIKE ?");
            params.add("%" + email + "%");
        }
        if (StringUtils.hasText(phone)) {
            query.append(" AND phone LIKE ?");
            params.add("%" + phone + "%");
        }

        return jdbcTemplate.query(query.toString(), params.toArray(), new BeanPropertyRowMapper<>(Client.class));
    }

    public List<Client> searchClients(String search) {
        String sql = "SELECT * FROM clients WHERE firstname LIKE ? OR lastname LIKE ? OR email LIKE ? OR phone LIKE ?  OR address LIKE ? ";
        String searchPattern = "%" + search + "%";
        return jdbcTemplate.query(sql, new Object[]{searchPattern, searchPattern, searchPattern, searchPattern, searchPattern}, new BeanPropertyRowMapper<>(Client.class));
    }


}




