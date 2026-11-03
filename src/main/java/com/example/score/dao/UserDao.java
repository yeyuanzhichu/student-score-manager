package com.example.score.dao;

import com.example.score.config.Database;
import com.example.score.model.UserAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDao {
    public UserAccount findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, display_name, role FROM users WHERE username = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserAccount user = new UserAccount();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setDisplayName(rs.getString("display_name"));
                    user.setRole(rs.getString("role"));
                    return user;
                }
            }
            return null;
        } catch (Exception e) {
            throw new DataAccessException("查询教师账号失败", e);
        }
    }
}
