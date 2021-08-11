package it.unipi.iot.irrigationsystem;

import it.unipi.iot.irrigationsystem.enumerate.Bound;
import it.unipi.iot.irrigationsystem.registration.RegistrationServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;

public class Collector {
    private static boolean celciusUnit = true;

    public static void main(String[] args) throws SocketException {
        // Init
        RegistrationServer rs = new RegistrationServer();
        rs.start();

        // CLI
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String command = "";
        String[] chunks;

        printCommands();

        while(true) {
            System.out.print(">!");
            try {
                command = br.readLine();
                chunks = command.split(" ");

                if(chunks[0].equals("quit"))
                    break;

                else if(chunks[0].equals("help"))
                    printCommands();

                else if(chunks[0].equals("getDevicesList"))
                    rs.printDevices();

                else if(chunks[0].equals("getTemp"))
                    getTemperatureAction(rs.getTemperature());

                else if(chunks[0].equals("setTemp"))
                    setTemperatureBound(chunks, rs);

                else if(chunks[0].equals("setUnit"))
                    setUnit(chunks);

                else if(chunks[0].equals("getWeather"))
                    getWeather(rs);

                else if(chunks[0].equals("start")) // TODO: simulation thread
                    System.out.println("PASS"); //TODO: dedicated function

                else if(chunks[0].equals("stop"))
                    System.out.println("PASS"); //TODO: dedicated function

                else
                    System.out.println("Invalid command");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void printCommands() {
        System.out.println("Commands list:" +
                "\n\t!getDevicesList: show list of all available sensors" + // DONE
                "\n\t!getTemp: get the temperature" + // DONE
                "\n\t!setTemp <l/u> <value>: set desired temperature for specified bound" + // DONE
                "\n\t!setUnit <F/C>: change unit in C (Celsius) F (Fahrenheit)" +  // DONE
                "\n\t!getWeather: get if the rain sensor feels rain or not" + // DONE
                "\n\t!getSoilTension: get the soil tension" +
                "\n\t!setSoilTension <lower tension> <upper tension>: set desired tension bounds" +
                "\n\t!setTapInterval <seconds>: set interval which the tap operates" +
                "\n\t!setTapIntensity <value>: set intensity which the tap operates" +
                "\n\t!getWaterLevels: print the water levels of aquifer and reservoir" +
                "\n\t!start: start the simulation" +
                "\n\t!stop: stop the simulation" +
                "\n\t!help: print commands list" + // DONE
                "\n\t!quit: quit the program"); // DONE
    }


    // ACTIONS
    private static void getTemperatureAction(int temp) {
        if (celciusUnit)
            System.out.println("Temperature detected: " + temp + "C");
        else
            System.out.println("Temperature detected: " + ((int)(((double)temp)*9/5) + 32) + "F");
    }

    private static void setTemperatureBound(String[] tokens, RegistrationServer rs) {
        int newTemperature = 0;
        try {
           newTemperature = Integer.parseInt(tokens[2]);
        } catch (Exception e) {
            System.out.println("Not correct value inserted, insert an integer");
        }

        if (!celciusUnit)
            newTemperature = (newTemperature-32)*5/9;

        switch (tokens[1]) {
            case "l":
                rs.changeTemperatureBounds(Bound.LOWER, newTemperature);
                System.out.println("Lower bound changed");
                break;
            case "u":
                rs.changeTemperatureBounds(Bound.UPPER, newTemperature);
                System.out.println("Upper bound changed");
                break;
            default:
                System.out.println("Bound inserted is not valid");
        }
    }

    private static void setUnit(String[] tokens) {
        switch (tokens[1]) {
            case "F":
                celciusUnit = false;
                System.out.println("Measure unit changed to Fahrenheit");
                break;
            case "C":
                celciusUnit = true;
                System.out.println("Measure unit changed to Celsius");
                break;
            default:
                System.out.println("Passed unit is not valid");
        }
    }

    private static void getWeather(RegistrationServer rs) {
        if (rs.getWeather())
            System.out.println("The weather is SUNNY");
        else
            System.out.println("The weather is RAINING");
    }
}
