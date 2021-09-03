package it.unipi.iot.irrigationsystem.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class IrrigationSystemDbManager {
    private final static String databaseIP = "localhost";
    private final static String databasePort = "3306";
    private final static String databaseUsername = "root";
    private final static String databasePassword = "root";
    private final static String databaseName = "iot_irrigation_system";

    private static Connection makeJDBCConnection() {
        Connection databaseConnection = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");//checks if the Driver class exists (correctly available)
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            // DriverManager: The basic service for managing a set of JDBC drivers.
            databaseConnection = DriverManager.getConnection(
                    "jdbc:mysql://" + databaseIP + ":" + databasePort +
                            "/" + databaseName + "?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=CET",
                    databaseUsername,
                    databasePassword);
            //The Driver Manager provides the connection specified in the parameter string
            if (databaseConnection == null) {
                System.err.println("Connection to Db failed");
            }
        } catch (SQLException e) {
            System.err.println("MySQL Connection Failed!\n");
            e.printStackTrace();
        }
       return databaseConnection;
    }

    public static void insertTemperature(int temperature) {
        String insertQueryStatement = "INSERT INTO temperature (tempValue) VALUES (?)";

        try (Connection IrrigationConnection = makeJDBCConnection();
             PreparedStatement prepareStatement = IrrigationConnection.prepareStatement(insertQueryStatement);
        ) {
            prepareStatement.setInt(1, temperature);
            prepareStatement.executeUpdate();
        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
    }

    public static void insertRainStatus(boolean status) {
        String insertQueryStatement = "INSERT INTO rain (isRaining) VALUES (?)";

        try (Connection IrrigationConnection = makeJDBCConnection();
             PreparedStatement prepareStatement = IrrigationConnection.prepareStatement(insertQueryStatement);
        ) {
            prepareStatement.setBoolean(1, status);
            prepareStatement.executeUpdate();
        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
    }

    public static void insertSoilMoistureValue(double soilValue) {
        String insertQueryStatement = "INSERT INTO soilMoisture (soilValue) VALUES (?);";

        try (Connection IrrigationConnection = makeJDBCConnection();
             PreparedStatement prepareStatement = IrrigationConnection.prepareStatement(insertQueryStatement);
        ) {
            prepareStatement.setDouble(1, soilValue);
            prepareStatement.executeUpdate();

        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
    }

    public static void insertTapValues(double intensity, int interval) {
        String insertQueryStatement = "INSERT INTO tap(intensity, tap.interval) VALUES (?, ?);";

        try (Connection IrrigationConnection = makeJDBCConnection();
             PreparedStatement prepareStatement = IrrigationConnection.prepareStatement(insertQueryStatement);
        ) {
            prepareStatement.setDouble(1, intensity);
            prepareStatement.setInt(2, interval);
            prepareStatement.executeUpdate();

        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
    }

    public static void insertWaterLevAquifer(String nodeId, double waterLevel) {
        String insertQueryStatement = "INSERT INTO waterLevelAquifer(nodeId, waterLevel) VALUES (?, ?);";

        try (Connection IrrigationConnection = makeJDBCConnection();
             PreparedStatement prepareStatement = IrrigationConnection.prepareStatement(insertQueryStatement);
        ) {
            prepareStatement.setString(1, nodeId);
            prepareStatement.setDouble(2, waterLevel);
            prepareStatement.executeUpdate();

        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
    }

    public static void insertWaterLevReservoir(String nodeId, double waterLevel) {
        String insertQueryStatement = "INSERT INTO waterLevelReservoir (nodeId, waterLevel) VALUES (?, ?);";

        try (Connection IrrigationConnection = makeJDBCConnection();
             PreparedStatement prepareStatement = IrrigationConnection.prepareStatement(insertQueryStatement);
        ) {
            prepareStatement.setString(1, nodeId);
            prepareStatement.setDouble(2, waterLevel);
            prepareStatement.executeUpdate();

        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
    }
}
