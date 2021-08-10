package it.unipi.iot.irrigationsystem;

import it.unipi.iot.irrigationsystem.registration.RegistrationServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;

public class Collector {
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
                    System.out.println("PASS"); //TODO: dedicated function

                else if(chunks[0].equals("setTemp"))
                    System.out.println("PASS"); //TODO: dedicated function

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
                "\n\t!getDevicesList: show list of all available sensors" +
                "\n!getTemp: get the temperature" +
                "\n!setTemp <lower temperature> <upper temperature>: set desired temperature bounds" +
                "\n!setUnit <F/C>: change unit in C (Celsius) F (Fahrenheit)" +
                "\n!getIsRaining: get if the rain sensor feels rain or not" +
                "\n!getSoilTension: get the soil tension" +
                "\n!setSoilTension <lower tension> <upper tension>: set desired tension bounds" +
                "\n!setTapInterval <seconds>: set interval which the tap operates" +
                "\n!setTapIntensity <value>: set intensity which the tap operates" +
                "\n!getWaterLevels: print the water levels of aquifer and reservoir" +
                "\n!start: start the simulation" +
                "\n!stop: stop the simulation" +
                "\n\t!help: print commands list" +
                "\n\t!quit: quit the program");
    }
}
