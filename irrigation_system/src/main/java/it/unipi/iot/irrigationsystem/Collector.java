package it.unipi.iot.irrigationsystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Collector {
    public static void main(String[] args) {
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

                else if(chunks[0].equals("getSensorsList"))
                    System.out.println("PASS"); //TODO: dedicated function

                else if(chunks[0].equals("setTemp"))
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
                "\n\t!getSensorsList: show list of all available sensors" +
                "\n!setTemp <lower temperature> <upper temperature> <unit[C or F]>: set desired temperature bounds" +
                "\n\t!help: print commands list" +
                "\n\t!quit: quit the program");
    }
}
