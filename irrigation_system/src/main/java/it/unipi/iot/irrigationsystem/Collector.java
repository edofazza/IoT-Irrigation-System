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

        label:
        while(true) {
            System.out.print(">!");
            try {
                command = br.readLine();
                chunks = command.split(" ");

                switch (chunks[0]) {
                    case "quit":
                        break label;
                    case "help":
                        printCommands();
                        break;
                    case "getDevicesList":
                        rs.printDevices();
                        break;
                    case "getTemp":
                        getTemperatureAction(rs.getTemperature());
                        break;
                    case "setTemp":
                        setTemperatureBound(chunks, rs);
                        break;
                    case "setUnit":
                        setUnit(chunks);
                        break;
                    case "getWeather":
                        getWeather(rs);
                        break;
                    case "getSoilTension":
                        getSoilTensionAction(rs.getSoilTension());
                        break;
                    case "setSoilTension":
                        setSoilTensionAction(chunks, rs);
                        break;
                    case "getTapInterval":
                        getTapIntervalAction(rs);
                        break;
                    case "getTapIntensity":
                        getTapIntensityAction(rs);
                        break;
                    case "setTapInterval":
                        setTapInterval(chunks, rs);
                        break;
                    case "setTapIntensity":
                        setTapIntensity(chunks, rs);
                        break;
                    case "getWaterLevels":
                        System.out.println("PASS"); //TODO: dedicated function
                        break;
                    case "start": // TODO: simulation thread
                        System.out.println("PASS"); //TODO: dedicated function
                        break;
                    case "stop":
                        System.out.println("PASS"); //TODO: dedicated function
                        break;
                    default:
                        System.out.println("Invalid command");
                        break;
                }

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
                "\n\t!getSoilTension: get the soil tension" + // DONE
                "\n\t!setSoilTension <lower tension> <upper tension>: set desired tension bounds" + // DONE
                "\n\t!getTapInterval: get interval which the tap operates" +
                "\n\t!getTapIntensity: get intensity which the tap operates" +
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
        if (tokens.length < 2)
            System.out.println("Too few parameters");

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

    private static void getSoilTensionAction(double soilTension) {
        System.out.println("The soil moisture tension is " + soilTension + "bar");
    }

    private static void setSoilTensionAction(String[] tokens, RegistrationServer rs) {
        double newTension = 0;
        try {
            newTension = Double.parseDouble(tokens[2]);
        } catch (Exception e) {
            System.out.println("Not correct value inserted, insert an integer");
        }

        switch (tokens[1]) {
            case "l":
                rs.changeSoilTensionBound(Bound.LOWER, newTension);
                System.out.println("Lower bound changed");
                break;
            case "u":
                rs.changeSoilTensionBound(Bound.UPPER, newTension);
                System.out.println("Upper bound changed");
                break;
            default:
                System.out.println("Bound inserted is not valid");
        }
    }

    private static void getTapIntensityAction(RegistrationServer rs) {
        System.out.println("The tap intensity is: " + rs.getTapIntensity());
    }

    private static void getTapIntervalAction(RegistrationServer rs) {
        System.out.println("The tap interval is: " + rs.getTapInterval());
    }

    private static void setTapInterval(String[] chunks, RegistrationServer rs) {

    }

    private static void setTapIntensity(String[] chunks, RegistrationServer rs) {
        
    }
}
