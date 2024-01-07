package io.github.cardsandhuskers.thedropper.objects;

import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.thedropper.TheDropper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;


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
     * @throws IOException
     */
    public void calculateStats() throws IOException {
        int initialEvent = 1;

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

                    //skip over levels they skipped
                    if(r.get(3).equalsIgnoreCase("-1")) continue;

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

    public String getPlayerFinishPosition(OfflinePlayer p) {
        String name = p.getName();
        ArrayList<PlayerStatsHolder> pph= new ArrayList<>(playerStatsHolders);
        pph.sort(new PlayerStatsPlacementComparator());

        int i = 1;
        PlayerStatsHolder playerHolder = null;
        for(PlayerStatsHolder holder: pph) {
            if(holder.name.equals(name)) {
                playerHolder = holder;
                break;
            }
            i++;
        }


        if(playerHolder == null || i <= 10) return "";

        Team team = TeamHandler.getInstance().getPlayerTeam(p.getPlayer());
        String color = "";
        if(team != null) color = team.getColor();

        return i + ". " + color + "You" + ChatColor.RESET + ": " + String.format("%.1f", playerHolder.getAveragePlacement());
    }

    public String getPlayerFailsPosition(OfflinePlayer p) {
        String name = p.getName();
        ArrayList<PlayerStatsHolder> pph= new ArrayList<>(playerStatsHolders);
        pph.sort(new PlayerStatsFailComparator());

        int i = 1;
        PlayerStatsHolder playerHolder = null;
        for(PlayerStatsHolder holder: pph) {
            if(holder.name.equals(name)) {
                playerHolder = holder;
                break;
            }
            i++;
        }
        if(playerHolder == null || i <= 10) return "";

        Team team = TeamHandler.getInstance().getPlayerTeam(p.getPlayer());
        String color = "";
        if(team != null) color = team.getColor();

        return i + ". " + color + "You" + ChatColor.RESET + ": " + playerHolder.getFails();
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
