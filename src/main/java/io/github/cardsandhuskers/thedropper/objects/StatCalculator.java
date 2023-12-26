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
    /*public void calculateStats() throws Exception{
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

    }*/
    public void calculateStats() throws IOException {
        int initialEvent = 2;

        int eventNum;
        try {eventNum = Bukkit.getPluginManager().getPlugin("LobbyPlugin").getConfig().getInt("eventNum");}
        catch (Exception e) {eventNum = initialEvent;}

        HashMap<String, PlayerStatsHolder> playerStatsMap = new HashMap<>();
        FileReader reader;

        for(int i = initialEvent; i <= eventNum; i++) {
            try {
                reader = new FileReader(plugin.getDataFolder() + "/dropperStats" + i + ".csv");
            } catch (IOException e) {
                plugin.getLogger().warning("Stats file not found!");
                continue;
            }
            String[] headers = {"Name", "Team", " Level", "Place", "LevelFails", "Skipped"};

            CSVFormat.Builder builder = CSVFormat.Builder.create();
            builder.setHeader(headers);
            CSVFormat format = builder.build();

            CSVParser parser;
            parser = new CSVParser(reader, format);

            List<CSVRecord> recordList = parser.getRecords();
            reader.close();

            for(CSVRecord r:recordList) {
                if (r.getRecordNumber() == 1) continue;

                String name = r.get(0);
                if(playerStatsMap.containsKey(name)) {
                    PlayerStatsHolder holder = playerStatsMap.get(name);
                    holder.addPlacement(Integer.parseInt(r.get(3)), Integer.parseInt(r.get(4)));
                }
                else {
                    PlayerStatsHolder holder = new PlayerStatsHolder(name);
                    holder.addPlacement(Integer.parseInt(r.get(3)), Integer.parseInt(r.get(4)));
                    playerStatsMap.put(name, holder);
                }
            }

        }

        playerStatsHolders = new ArrayList<>(playerStatsMap.values());
        System.out.println(playerStatsHolders);

        playerStatsHolders.sort(new PlayerStatsPlacementComparator());
    }

    /**
     * Called by Placeholder class and returns all the stats holders ordered by placement
     * @return ArrayList - playerStatsHolders
     */
    public ArrayList<PlayerStatsHolder> getPlayerPlacementStatsHolders() {
        ArrayList<PlayerStatsHolder> pph= new ArrayList<>(playerStatsHolders);
        pph.sort(new PlayerStatsPlacementComparator());
        return pph;
    }

    /**
     * Called by Placeholder class and returns all the stats holders ordered by fails
     * @return ArrayList - playerStatsHolders
     */
    public ArrayList<PlayerStatsHolder> getPlayerFailsStatsHolders() {
        ArrayList<PlayerStatsHolder> pfh= new ArrayList<>(playerStatsHolders);
        pfh.sort(new PlayerStatsFailComparator());
        Collections.reverse(pfh);
        return pfh;
    }

    /**
     * Subclass that is a tuple containing the player's name and all of their wins across every event.
     */
    public class PlayerStatsHolder {
        ArrayList<Integer> placements, fails;
        String name;

        public PlayerStatsHolder(String name) {
            this.name = name;
            placements = new ArrayList<>();
            fails = new ArrayList<>();
        }
        public void addPlacement(int placement, int fail) {
            if(placement != -1) placements.add(placement);
            fails.add(fail);

        }

        public double getAveragePlacement() {
            double sum = 0;
            for(Integer x: placements) {
                sum += x;
            }
            sum = sum / (double)placements.size();
            return sum;
        }

        public int getFails() {
            int sum = 0;
            for(Integer x: fails) {
                sum += x;
            }
            return sum;
        }
    }

    /**
     * Comparator class used to compare the holders
     *
     * Sorts by placement and then by names as a backup
     * (to give more deterministic results, order across ties would be random otherwise and this causes bugs)
     */
    public class PlayerStatsPlacementComparator implements Comparator<PlayerStatsHolder> {
        public int compare(PlayerStatsHolder h1, PlayerStatsHolder h2) {
            int compare = Double.compare(h1.getAveragePlacement(), h2.getAveragePlacement());
            if(compare == 0) compare = h1.name.compareTo(h2.name);
            return compare;
        }
    }

    /**
     * Comparator class used to compare the holders
     *
     * Sorts by fails and then by names as a backup
     * (to give more deterministic results, order across ties would be random otherwise and this causes bugs)
     */
    public class PlayerStatsFailComparator implements Comparator<PlayerStatsHolder> {
        public int compare(PlayerStatsHolder h1, PlayerStatsHolder h2) {
            int compare = Integer.compare(h1.getFails(), h2.getFails());
            if(compare == 0) compare = h1.name.compareTo(h2.name);
            return compare;
        }
    }
}
