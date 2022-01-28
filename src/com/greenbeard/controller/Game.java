package com.greenbeard.controller;

import com.apps.util.Prompter;

import com.greenbeard.model.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Game {
    private boolean gameOver;
    private Player player = new Player();
    private Prompter prompter = new Prompter(new Scanner(System.in));
    private String currentLocation = "town";
    private JSONParser jsonParser = new JSONParser();

    public void execute() {
        welcome();
        while (!gameOver) {
            if (player.getHealth() <= 0) {
                gameOver = true;
                break;
            }
            start();
        }
        gameOver();
    }

    private void welcome() {
        System.out.println("\n\n");
        try {
            //Files.lines(Path.of("data/welcome/banner.txt")).forEach(System.out::println);
            Files.lines(Path.of("data/welcome/banner1.txt")).forEach(System.out::println);
            delay();
            Files.lines(Path.of("data/welcome/banner2.txt")).forEach(System.out::println);
            delay();
            Files.lines(Path.of("data/welcome/banner3.txt")).forEach(System.out::println);
            delay();
            Files.lines(Path.of("data/welcome/banner4.txt")).forEach(System.out::println);
            delay();
            List<String> welcome = Files.readAllLines(Path.of("data/welcome/welcome.txt"));
            List<String> intro = Files.readAllLines(Path.of("data/welcome/intro.txt"));
            welcome.forEach(System.out::println);
            Thread.sleep(8000);
            clearConsole();
            player.setName(prompter.prompt("\nWhat is your name Captain? -> "));
            player.setShipName(prompter.prompt("What is the name of your Ship? -> "));
            String weapon = prompter.prompt("What kind of weapon do you carry?\n" +
                    "Options are: sword, or pistol\n --> ", "sword|pistol", "Invalid selection");
            player.setWeapon(weapon);
            System.out.printf("\n\nYou are the Great Captain %s, Captain of the %s.\n" +
                            "With your trusty %s by your side, you set off to town.%n",
                    player.getName(), player.getShipName(), player.getWeapon());
            intro.forEach(line -> System.out.println(line));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void clearConsole() {
        System.out.println("\033[H\033[2J");
        System.out.flush();
    }

    private void delay() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void start() {
        String input = prompter.prompt("What would you like to do?\n -> ").toLowerCase();
        textParser(input);
    }

    private void textParser(String input) {
        List<String> commands = Arrays.asList(input.split(" "));

        if (commands.size() != 2) {
            System.out.println("Invalid Command");
            return;
        }

        String verb = checkSynonym(commands.get(0));
        String noun = commands.get(1);

        try (Reader reader = new FileReader("data/locations/locations.json")) {
            JSONObject jObj = (JSONObject) jsonParser.parse(reader);
            JSONObject location = (JSONObject) jObj.get(this.currentLocation);

            if ("go".equals(verb)) {
                this.currentLocation = noun;
                location = (JSONObject) jObj.get(this.currentLocation);
                if (location != null) {
                    String description = (String) location.get("description");
                    System.out.println(description);
                }
            }
            if (!currentLocation.equals("town") && "recruit".equals(verb)) {
                JSONObject npcs = (JSONObject) location.get("npcs");
                JSONObject npc = (JSONObject) npcs.get(noun);

                if (npc != null) {
                    String name = (String) npc.get("name");
                    String ableToRecruit = (String) npc.get("ableToRecruit");
                    if (ableToRecruit.equals("true")) {
                        player.addCrewMate(name);
                    }
                }
            }
            if ("set".equals(verb) && "sail".equals(noun)) {
                this.currentLocation = "island";
                location = (JSONObject) jObj.get(this.currentLocation);
                String description = (String) location.get("description");
                System.out.println(description);
                ending();
            }

            if (!currentLocation.equals("town") && "talk".equals(verb)) {
                JSONObject npcs = (JSONObject) location.get("npcs");
                JSONObject npc = (JSONObject) npcs.get(noun);

                if (npc != null) {
                    String greet = (String) npc.get("greeting");
                    System.out.println(greet);
                } else {
                    System.out.println("Sorry you cannot speak to " + noun + ".");
                }
            }

            System.out.println(player.getCrewMates());

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private String checkSynonym(String command) {

        try (Reader readerSynonym = new FileReader("data/synonyms.json")) {
            JSONObject jObj = (JSONObject) jsonParser.parse(readerSynonym);
            if (jObj.containsKey(command)) {
                return command;
            } else {
                final String[] keyStr = {""};
                jObj.forEach((key, val) -> {
                    JSONArray arr = (JSONArray) val;
                    if (arr.contains(command)) {
                        keyStr[0] = String.valueOf(key);
                    }
                });
                return keyStr[0];
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return "Required return statement here";
    }

    private void gameOver() {
        System.out.println("GAME OVER!");
        String playAgain = prompter.prompt("Play again? yes or no?\n -> ", "yes|no", "Invalid Choice");
        if ("yes".equals(playAgain)) {
            execute();
        } else if ("no".equals(playAgain)) {
            System.out.println("Thanks for playing! See you again!");
            System.exit(0);
        }
    }

    private void ending() {
        if (player.getCrewMates().contains("mourner")) {
            System.out.println("You find the treasure and live happily ever after :)");
        } else {
            System.out.println("You didn't have a navigator and got lost at sea. Sorry :(\n" +
                    "GAME OVER");
        }
        gameOver = true;
    }
}