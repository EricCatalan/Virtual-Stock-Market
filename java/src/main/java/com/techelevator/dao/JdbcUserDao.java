package com.techelevator.dao;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import com.techelevator.model.Gamer;
import com.techelevator.model.Portfolio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.techelevator.model.User;

@Service
public class JdbcUserDao implements UserDao {

    private JdbcTemplate jdbcTemplate;

    public JdbcUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long findIdByUsername(String username) {
        return jdbcTemplate.queryForObject("select user_id from users where username = ?", Long.class, username);
    }

    @Override
    public Gamer findGamerByUsername(String username) {
        SqlRowSet results = jdbcTemplate.queryForRowSet("select user_id, username from users where username = ?", username);
        if (results.next()) {
            return mapRowToGamer(results);
        } else {
            throw new RuntimeException("username "+ username +" was not found.");
        }
    }

	@Override
	public User getUserById(Long userId) {
		String sql = "SELECT * FROM users WHERE user_id = ?";
		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
		if(results.next()) {
			return mapRowToUser(results);
		} else {
			throw new RuntimeException("userId "+userId+" was not found.");
		}
	}

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "select * from users";

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while(results.next()) {
            User user = mapRowToUser(results);
            users.add(user);
        }

        return users;
    }

    @Override
    public User findByUsername(String username) throws UsernameNotFoundException {
        for (User user : this.findAll()) {
            if( user.getUsername().toLowerCase().equals(username.toLowerCase())) {
                return user;
            }
        }
        throw new UsernameNotFoundException("User " + username + " was not found.");
    }

    @Override
    public boolean create(String username, String password, String role) {
        boolean userCreated = false;

        // create user
        String insertUser = "insert into users (username,password_hash,role) values(?,?,?)";
        String password_hash = new BCryptPasswordEncoder().encode(password);
        String ssRole = "ROLE_" + role.toUpperCase();

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        String id_column = "user_id";
        userCreated = jdbcTemplate.update(con -> {
                    PreparedStatement ps = con.prepareStatement(insertUser, new String[]{id_column});
                    ps.setString(1, username);
                    ps.setString(2, password_hash);
                    ps.setString(3, ssRole);
                    return ps;
                }
                , keyHolder) == 1;
        int newUserId = (int) keyHolder.getKeys().get(id_column);

        return userCreated;
    }

    @Override
    public Portfolio getPortfolioByUserIdAndGameId(Long userId, Long gameId) {
        Portfolio portfolio = null;
        String sql = "SELECT portfolio_id, user_id, game_id, cash_balance, portfolio_value " +
                     "FROM portfolio WHERE user_id = ? AND game_id = ?;";

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId, gameId);
        while(results.next()) {
            portfolio = mapRowToPortfolio(results);
        }
        return portfolio;
    }

    private User mapRowToUser(SqlRowSet rs) {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password_hash"));
        user.setAuthorities(rs.getString("role"));
        user.setActivated(true);
        return user;
    }

    private Gamer mapRowToGamer(SqlRowSet rs) {
        Gamer gamer = new Gamer();
        gamer.setUserId(rs.getLong("user_id"));
        gamer.setUsername(rs.getString("username"));
        return gamer;
    }

    private Portfolio mapRowToPortfolio(SqlRowSet rowSet) {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(rowSet.getLong("portfolio_id"));
        portfolio.setUserId(rowSet.getLong("user_id"));
        portfolio.setGameId(rowSet.getLong("game_id"));
        portfolio.setCashBalance(rowSet.getBigDecimal("cash_balance"));
        portfolio.setPortfolioValue(rowSet.getBigDecimal("portfolio_value"));
        return portfolio;
    }
}
