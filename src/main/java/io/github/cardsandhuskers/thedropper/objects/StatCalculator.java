package io.github.cardsandhuskers.thedropper.objects;

import io.github.cardsandhuskers.thedropper.TheDropper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static io.github.cardsandhuskers.teams.Teams.handler;
import static io.github.cardsandhuskers.thedropper.handlers.GameStageHandler.wins;

public class StatCalculator {
    private TheDropper plugin;
    private ArrayList<PlayerStatsHolder> playerStatsHolders;
    public StatCalculator(TheDropper plugin) {
        this.plugin = plugin;
    }

    /**
     * Calculates player stats based on stats.csv data
     * Creates a PlayerStatsHolder tuple for every player
     *
     *  Populates the playerStatsHolders ArrayList with them
     *
     * Should be updated for new stats collection method
     * @throws Exception
     */
    public void calculateStats() throws Exception{
        HashMap<String, PlayerStatsHolder> playerStatsMap = new HashMap<>();

        FileReader reader = null;
        try {
            reader = new FileReader(plugin.getDataFolder() + "/stats.csv");
        } catch (IOException e) {
            plugin.getLogger().warning("Stats file not found!");
            return;
        }
        String[] headers = {"Event", "Team", "Name", "Wins"};

        CSVFormat.Builder builder = CSVFormat.Builder.create();
        builder.setHeader(headers);
        CSVFormat format = builder.build();

        CSVParser parser;
        try {
            parser = new CSVParser(reader, format);
        } catch (IOException e) {
            throw new Exception(e);
        }
        List<CSVRecord> recordList = parser.getRecords();

        try {
            reader.close();
        } catch (IOException e) {
            throw new Exception(e);
        }

        for(CSVRecord r:recordList) {
            if (r.getRecordNumber() == 1) continue;
            String name = r.get(2);
            if(playerStatsMap.containsKey(name)) playerStatsMap.get(name).wins += Integer.parseInt(r.get(3));
            else playerStatsMap.put(name, new PlayerStatsHolder(name, Integer.parseInt(r.get(3))));
        }
        playerStatsHolders = new ArrayList<>(playerStatsMap.values());
        Comparator playerStatsComparator = new PlayerStatsComparator();
        playerStatsHolders.sort(playerStatsComparator);
        Collections.reverse(playerStatsHolders);

    }


    /**
     * This saves the results from the event.
     *
     * This does not save any data that new Stats class doesn't,
     * but the calculator format has not been updated to accommodate the new format
     * @throws IOException
     */
    public void saveRecords() throws IOException {
        //for(Player p:wins.keySet()) if(p != null) System.out.println(p.getDisplayName() + ": " + wins.get(p));
        //System.out.println("~~~~~~~~~~~~~~~");

        FileWriter writer = new FileWriter("plugins/TheDropper/stats.csv", true);
        FileReader reader = new FileReader("plugins/TheDropper/stats.csv");

        String[] headers = {"Event", "Team", "Name", "Wins"};

        CSVFormat.Builder builder = CSVFormat.Builder.create();
        builder.setHeader(headers);
        CSVFormat format = builder.build();

        CSVParser parser = new CSVParser(reader, format);

        if(!parser.getRecords().isEmpty()) {
            format = CSVFormat.DEFAULT;
        }

        CSVPrinter printer = new CSVPrinter(writer, format);

        int eventNum;
        try {eventNum = Bukkit.getPluginManager().getPlugin("LobbyPlugin").getConfig().getInt("eventNum");} catch (Exception e) {eventNum = 1;}
        //printer.printRecord(currentGame);
        for(Player p:wins.keySet()) {
            if(p == null) continue;
            if(handler.getPlayerTeam(p) == null) continue;
            printer.printRecord(eventNum, handler.getPlayerTeam(p).getTeamName(), p.getDisplayName(), wins.get(p));
        }
        writer.close();
        try {
            plugin.statCalculator.calculateStats();
        } catch (Exception e) {
            StackTraceElement[] trace = e.getStackTrace();
            String str = "";
            for(StackTraceElement element:trace) str += element.toString() + "\n";
            plugin.getLogger().severe("ERROR Calculating Stats!\n" + str);
        }

    }

    /**
     * Called by Placeholder class and returns all the stats holders
     * @return ArrayList - playerStatsHolders
     */
    public ArrayList<PlayerStatsHolder> getPlayerStatsHolders() {
        return new ArrayList<>(playerStatsHolders);
    }

    /**
     * Subclass that is a tuple containing the player's name and all of their wins across every event.
     */
    public class PlayerStatsHolder {
        int wins;
        String name;
        public PlayerStatsHolder(String name, int wins) {
            this.name = name;
            this.wins = wins;
        }
    }

    /**
     * Comparator class used to compare the holders
     *
     * Sorts by wins and then by names as a backup
     * (to give more deterministic results, order across ties would be random otherwise and this causes bugs)
     */
    public class PlayerStatsComparator implements Comparator<PlayerStatsHolder> {
        public int compare(PlayerStatsHolder h1, PlayerStatsHolder h2) {
            int compare = Integer.compare(h1.wins, h2.wins);
            if(compare == 0) compare = h1.name.compareTo(h2.name);
            return compare;
        }
    }
}
