package database;

import DataClasses.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DBManager implements database.DataSource {
    private static DBManager instance = new DBManager();
    private String sql_password;

    private DBManager() {
        try {
            File file = new File("R:\\CS4B\\SQL_Password.txt");
            Scanner scanner = new Scanner(file);
            sql_password = scanner.next();
        } catch (FileNotFoundException e) {e.printStackTrace();}
    }

    public static DBManager getInstance() {return instance;}

    private Connection getConnection() {
        Connection connection = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/tic-tac-toe","root",
                    sql_password);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return connection;
    }

    @Override
    public boolean insert(Object obj) {
        Connection connection = getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        boolean wasSuccessful = true;

        try {
            if(obj instanceof TTT_GameData) {
                // insert game into database
                TTT_GameData TTTGameData = (TTT_GameData) obj;

                statement = connection.prepareStatement("insert into game (id, start_time, end_time, player1," +
                        " player2, starting_player, winner) values (?,?,?,?,?,?,?);");
                statement.setString(1, TTTGameData.getId());
                statement.setTimestamp(2, Timestamp.valueOf(TTTGameData.getStartingTime()));
                statement.setTimestamp(3, null);
                statement.setInt(4, TTTGameData.getPlayer1Id());
                statement.setInt(5, TTTGameData.getPlayer2Id());
                statement.setInt(6, TTTGameData.getStartingPlayerId());
                statement.setInt(7, TTTGameData.getWinningPlayerId());

                statement.executeUpdate();
            }
            else if(obj instanceof User) {
                // insert user into database
                User user = (User) obj;

                statement = connection.prepareStatement("insert into user (username, password, fname, lname," +
                        " is_active) values (?,?,?,?,?);");
                statement.setString(1, user.getUsername());
                statement.setString(2, user.getPassword());
                statement.setString(3, user.getFirstName());
                statement.setString(4, user.getLastName());
                statement.setBoolean(5, true);

                statement.executeUpdate();
                statement.close();

                // retrieve auto-generated id
                statement = connection.prepareStatement("select id from user where username = ?;");
                statement.setString(1, user.getUsername());
                resultSet = statement.executeQuery();
                resultSet.next();
                user.setId(resultSet.getInt("id"));
            }
            else if(obj instanceof TTT_MoveData) {
                // insert user into database
                TTT_MoveData ttt_moveData = (TTT_MoveData) obj;

                statement = connection.prepareStatement("insert into move (game_id, player_id, move_time, row," +
                        " col, turn) values (?,?,?,?,?, ?);");
                statement.setString(1, ttt_moveData.getGame_id());
                statement.setInt(2, ttt_moveData.getPlayer_id());
                statement.setTimestamp(3, Timestamp.valueOf(ttt_moveData.getTime()));
                statement.setInt(4, ttt_moveData.getRow());
                statement.setInt(5, ttt_moveData.getColumn());
                statement.setInt(6, ttt_moveData.getTurn());

                statement.executeUpdate();
                statement.close();
            }
            else if(obj instanceof TTT_ViewerData) {
                // insert user into database
                TTT_ViewerData ttt_viewerData = (TTT_ViewerData) obj;

                statement = connection.prepareStatement("insert into viewer (game_id, viewer_id) values (?,?);");
                statement.setString(1, ttt_viewerData.getGame_id());
                statement.setInt(2, ttt_viewerData.getViewer_id());

                statement.executeUpdate();
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            wasSuccessful = false;
        }
        finally {
            if(connection != null)
                try {connection.close();} catch (SQLException e) {e.printStackTrace();}
            if(statement != null)
                try {statement.close();} catch (SQLException e) {e.printStackTrace();}
            if(resultSet != null)
                try {resultSet.close();} catch (SQLException e) {e.printStackTrace();}
        }

        return wasSuccessful;
    }

    @Override
    public boolean delete(Object obj) {
        boolean wasSuccessful = true;
        Connection connection = getConnection();
        PreparedStatement statement = null;

        try {
            if(obj instanceof TTT_GameData) {
                TTT_GameData TTTGameData = (TTT_GameData) obj;
                statement = connection.prepareStatement("delete from game where id = ?;");
                statement.setString(1, TTTGameData.getId());
                statement.executeUpdate();
            }
            else if(obj instanceof User) {
                User user = (User) obj;
                statement = connection.prepareStatement("delete from user where id = ?;");
                statement.setInt(1, user.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            wasSuccessful = false;
        }
        finally {
            if(connection != null)
                try {connection.close();} catch (SQLException e) {e.printStackTrace();}
            if(statement != null)
                try {statement.close();} catch (SQLException e) {e.printStackTrace();}
        }

        return wasSuccessful;
    }

    @Override
    public boolean update(Object obj) {
        boolean wasSuccessful = true;
        Connection connection = getConnection();
        PreparedStatement statement = null;

        try {
            if(obj instanceof  User) {
                User user = (User) obj;

                statement = connection.prepareStatement("update user set username = ?, password = ?, fname = ?," +
                        " lname = ?, is_active = ? where id = ?;");
                statement.setString(1, user.getUsername());
                statement.setString(2, user.getPassword());
                statement.setString(3, user.getFirstName());
                statement.setString(4, user.getLastName());
                statement.setBoolean(5, user.isActive());
                statement.setInt(6, user.getId());

                statement.executeUpdate();
            }
            else if(obj instanceof TTT_GameData) {
                TTT_GameData TTTGameData = (TTT_GameData) obj;

                statement = connection.prepareStatement("update game set start_time = ?, end_time = ?, player1 = ?," +
                        " player2 = ?, starting_player = ?, winner = ? where id = ?;");
                statement.setTimestamp(1, Timestamp.valueOf(TTTGameData.getStartingTime()));
                statement.setTimestamp(2, (TTTGameData.getEndTime() == null)? null :Timestamp.valueOf(TTTGameData.getEndTime()));
                statement.setInt(3, TTTGameData.getPlayer1Id());
                statement.setInt(4, TTTGameData.getPlayer2Id());
                statement.setInt(5, TTTGameData.getStartingPlayerId());
                statement.setInt(5, TTTGameData.getWinningPlayerId());
                statement.setString(6, TTTGameData.getId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            wasSuccessful = false;
        }
        finally {
            if(connection != null)
                try {connection.close();} catch (SQLException e) {e.printStackTrace();}
            if(statement != null)
                try {statement.close();} catch (SQLException e) {e.printStackTrace();}
        }

        return wasSuccessful;
    }

    @Override
    public Object get(Class classType, String id) {
        Connection connection = getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Object obj = null;


        try {
            if(classType == TTT_GameData.class) {
                statement = connection.prepareStatement("select * from game where id = ?;");
                statement.setString(1, id);
                resultSet = statement.executeQuery();

                resultSet.next();
                Timestamp end_time = resultSet.getTimestamp("end_time");
                LocalDateTime end_time_ldt = (end_time == null)? null : end_time.toLocalDateTime();
                obj = new TTT_GameData(resultSet.getString("id"),
                        resultSet.getTimestamp("start_time").toLocalDateTime(),
                        end_time_ldt,
                        resultSet.getInt("player1"), resultSet.getInt("player2"),
                        resultSet.getInt("starting_player"), resultSet.getInt("winner"));
            }
            else if(classType == User.class) {
                statement = connection.prepareStatement("select * from user where id = ?;");
                statement.setInt(1, Integer.parseInt(id));
                resultSet = statement.executeQuery();

                resultSet.next();
                obj = new User(resultSet.getInt("id"), resultSet.getString("username"),
                        resultSet.getString("password"), resultSet.getString("fname"),
                        resultSet.getString("lname"), resultSet.getBoolean("is_active"));
            }
        } catch (SQLException e) {e.printStackTrace();}
        finally {
            if(resultSet != null)
                try {resultSet.close();} catch (SQLException e) {e.printStackTrace();}
            if(connection != null)
                try {connection.close();} catch (SQLException e) {e.printStackTrace();}
            if(statement != null)
                try {statement.close();} catch (SQLException e) {e.printStackTrace();}
        }

        return obj;
    }

    @Override
    public List<Object> list(Class classType) {
        List<Object> objs = new ArrayList<>();
        Connection connection = getConnection();
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.createStatement();
            if(classType == User.class) {
                resultSet = statement.executeQuery("select * from user;");

                while(resultSet.next())
                    objs.add(new User(resultSet.getInt("id"), resultSet.getString("username"),
                            resultSet.getString("password"), resultSet.getString("fname"),
                            resultSet.getString("lname"), resultSet.getBoolean("is_active")));
            }
            else if(classType == TTT_GameData.class) {
                resultSet = statement.executeQuery("select * from game;");

                while(resultSet.next())
                    objs.add(new TTT_GameData(resultSet.getString("id"),
                            resultSet.getTimestamp("start_time").toLocalDateTime(),
                            resultSet.getTimestamp("end_time").toLocalDateTime(),
                            resultSet.getInt("player1"), resultSet.getInt("player2"),
                            resultSet.getInt("starting_player"), resultSet.getInt("winner")));
            }
            else if(classType == TTT_MoveData.class) {
                resultSet = statement.executeQuery("select * from move;");

                while(resultSet.next())
                    objs.add(new TTT_MoveData(resultSet.getString("game_id"),
                            resultSet.getInt("player_id"),
                            resultSet.getTimestamp("move_time").toLocalDateTime(),
                            resultSet.getInt("row"), resultSet.getInt("col"),
                            resultSet.getInt("turn")));
            }
            else if(classType == TTT_ViewerData.class) {
                resultSet = statement.executeQuery("select * from viewer;");

                while(resultSet.next())
                    objs.add(new TTT_ViewerData(resultSet.getString("game_id"),
                            resultSet.getInt("viewer_id")));
            }
        } catch (SQLException e) {e.printStackTrace();}
        finally {
            if(resultSet != null)
                try {resultSet.close();} catch (SQLException e) {e.printStackTrace();}
            if(connection != null)
                try {connection.close();} catch (SQLException e) {e.printStackTrace();}
            if(statement != null)
                try {statement.close();} catch (SQLException e) {e.printStackTrace();}
        }

        return objs;
    }

    @Override
    public List<Object> query(Class classType, String filter) {
        List<Object> objs = new ArrayList<>();
        Connection connection = getConnection();
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.createStatement();
            if(classType == User.class) {
                switch (filter) {
                    case "active":
                        resultSet = statement.executeQuery("select * from user where is_active = true;");
                        break;
                    case "deactive":
                        resultSet = statement.executeQuery("select * from user where is_active = false;");
                        break;
                    case "all":
                    default:
                        resultSet = statement.executeQuery("select * from user;");
                }

                while(resultSet.next())
                    objs.add(new User(resultSet.getInt("id"), resultSet.getString("username"),
                            resultSet.getString("password"), resultSet.getString("fname"),
                            resultSet.getString("lname"), resultSet.getBoolean("is_active")));
            }
            else if(classType == TTT_GameData.class) {
                switch (filter) {
                    default:
                        resultSet = statement.executeQuery("select * from game;");
                }

                while(resultSet.next())
                    objs.add(new TTT_GameData(resultSet.getString("id"),
                            resultSet.getTimestamp("start_time").toLocalDateTime(),
                            resultSet.getTimestamp("end_time").toLocalDateTime(),
                            resultSet.getInt("player1"), resultSet.getInt("player2"),
                            resultSet.getInt("starting_player"), resultSet.getInt("winner")));
            }
            else if(classType == TTT_MoveData.class) {
                switch (filter) {
                    case "all":
                        resultSet = statement.executeQuery("select * from move;");
                        break;
                    default:
                        PreparedStatement preparedStatement = connection.prepareStatement("select * from move where game_id = ?");
                        preparedStatement.setString(1, filter);
                        resultSet = preparedStatement.executeQuery();
                }

                while(resultSet.next())
                    objs.add(new TTT_MoveData(resultSet.getString("game_id"),
                            resultSet.getInt("player_id"),
                            resultSet.getTimestamp("move_time").toLocalDateTime(),
                            resultSet.getInt("row"), resultSet.getInt("col"),
                            resultSet.getInt("turn")));
            }
            else if(classType == TTT_ViewerData.class) {
                switch(filter) {
                    case "all":
                        resultSet = statement.executeQuery("select * from viewer;");
                        break;
                    default:
                        PreparedStatement preparedStatement = connection.prepareStatement("select * from viewer where game_id = ?");
                        preparedStatement.setString(1, filter);
                        resultSet = preparedStatement.executeQuery();
                }

                while(resultSet.next())
                    objs.add(new TTT_ViewerData(resultSet.getString("game_id"),
                            resultSet.getInt("viewer_id")));
            }
        } catch (SQLException e) {e.printStackTrace();}
        finally {
            if(resultSet != null)
                try {resultSet.close();} catch (SQLException e) {e.printStackTrace();}
            if(connection != null)
                try {connection.close();} catch (SQLException e) {e.printStackTrace();}
            if(statement != null)
                try {statement.close();} catch (SQLException e) {e.printStackTrace();}
        }

        return objs;
    }
}
