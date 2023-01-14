package io.github.cardsandhuskers.thedropper.objects;

import io.github.cardsandhuskers.thedropper.TheDropper;
import io.github.cardsandhuskers.thedropper.handlers.GameStageHandler;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import static io.github.cardsandhuskers.thedropper.TheDropper.gameState;
import static io.github.cardsandhuskers.thedropper.TheDropper.timeVar;

public class Placeholder extends PlaceholderExpansion {
    private final TheDropper plugin;

    public Placeholder(TheDropper plugin) {
        this.plugin = plugin;
    }


    @Override
    public String getIdentifier() {
        return "TheDropper";
    }
    @Override
    public String getAuthor() {
        return "cardsandhuskers";
    }
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    @Override
    public boolean persist() {
        return true;
    }


    @Override
    public String onRequest(OfflinePlayer p, String s) {

        if(s.equalsIgnoreCase("timer")) {
            int time = timeVar;
            int mins = time / 60;
            String seconds = String.format("%02d", time - (mins * 60));
            return mins + ":" + seconds;
        }
        if(s.equalsIgnoreCase("timerstage")) {
            switch(gameState) {
                case GAME_STARTING:
                    return "Game Starts";
                case GAME_IN_PROGRESS:
                    return "Game Ends";
                case GAME_OVER:
                    return "Return To Lobby";
                default:
                    return "Game";
            }
        }
        if(s.equalsIgnoreCase("level")) {
            if(GameStageHandler.currentLevel != null && GameStageHandler.currentLevel.get(p.getUniqueId()) != null) {
                return GameStageHandler.currentLevel.get(p.getUniqueId()) - 1 + "";
            } else {
                return 0 + "";
            }

        }
        return null;
    }
}
