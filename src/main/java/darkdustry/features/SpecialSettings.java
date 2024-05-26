package darkdustry.features;

import arc.util.Log;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.world.blocks.logic.MessageBlock;

public class SpecialSettings {
    private static final int coreShardUnitInc = Blocks.coreShard.unitCapModifier;
    private static final int coreAcropolisUnitInc = Blocks.coreAcropolis.unitCapModifier;
    private static final int coreBastionUnitInc = Blocks.coreBastion.unitCapModifier;
    private static final int coreCitadelUnitInc = Blocks.coreCitadel.unitCapModifier;
    private static final int coreFoundationUnitInc = Blocks.coreFoundation.unitCapModifier;
    private static final int coreNucleusUnitInc = Blocks.coreNucleus.unitCapModifier;

    /** Load initial settings */
    public static void load() {
        Blocks.coreShard.unitCapModifier = coreShardUnitInc;
        Blocks.coreAcropolis.unitCapModifier = coreAcropolisUnitInc;
        Blocks.coreBastion.unitCapModifier = coreBastionUnitInc;
        Blocks.coreCitadel.unitCapModifier = coreCitadelUnitInc;
        Blocks.coreFoundation.unitCapModifier = coreFoundationUnitInc;
        Blocks.coreNucleus.unitCapModifier = coreNucleusUnitInc;
    }

    /** Load settings from map */
    public static void update() {
        Blocks.coreShard.unitCapModifier = coreShardUnitInc;
        Blocks.coreAcropolis.unitCapModifier = coreAcropolisUnitInc;
        Blocks.coreBastion.unitCapModifier = coreBastionUnitInc;
        Blocks.coreCitadel.unitCapModifier = coreCitadelUnitInc;
        Blocks.coreFoundation.unitCapModifier = coreFoundationUnitInc;
        Blocks.coreNucleus.unitCapModifier = coreNucleusUnitInc;

        var anyBuild = Vars.world.build(0, 0);
        if (anyBuild == null || anyBuild.block().id != Blocks.worldMessage.id) {
            return;
        }

        var header = "";
        for (var line : ((MessageBlock.MessageBuild) anyBuild).config().split("\n")) {
            if (line.startsWith("[") && line.endsWith("]")) {
                header = line.substring(1, line.length() - 1);
                continue;
            }

            if (!line.contains("=")) continue;
            StringBuilder option = new StringBuilder(line.substring(0, line.indexOf("=")).trim());
            if (!header.isEmpty()) option.insert(0, header + ".");
            String value = line.substring(line.indexOf("=") + 1).trim();

            Log.info("Setting " + option + " = " + value);

            switch (option.toString()) {
                case "coreUnitInc.all" -> {
                    try {
                        int num = Integer.parseInt(value);
                        Log.info("Setting global unit cap: " + num);
                        Blocks.coreShard.unitCapModifier = num;
                        Blocks.coreAcropolis.unitCapModifier = num;
                        Blocks.coreBastion.unitCapModifier = num;
                        Blocks.coreCitadel.unitCapModifier = num;
                        Blocks.coreFoundation.unitCapModifier = num;
                        Blocks.coreNucleus.unitCapModifier = num;
                    } catch (Exception ignored) {
                        Log.warn("Argument is not a number");
                    }
                }
                case "coreUnitInc.shard" -> {
                    try {
                        Blocks.coreShard.unitCapModifier = Integer.parseInt(value);
                    } catch (Exception ignored) {
                        Log.warn("Argument is not a number");
                    }
                }
                case "coreUnitInc.acropolis" -> {
                    try {
                        Blocks.coreAcropolis.unitCapModifier = Integer.parseInt(value);
                    } catch (Exception ignored) {
                        Log.warn("Argument is not a number");
                    }
                }
                case "coreUnitInc.bastion" -> {
                    try {
                        Blocks.coreBastion.unitCapModifier = Integer.parseInt(value);
                    } catch (Exception ignored) {
                        Log.warn("Argument is not a number");
                    }
                }
                case "coreUnitInc.citadel" -> {
                    try {
                        Blocks.coreCitadel.unitCapModifier = Integer.parseInt(value);
                    } catch (Exception ignored) {
                        Log.warn("Argument is not a number");
                    }
                }
                case "coreUnitInc.foundation" -> {
                    try {
                        Blocks.coreFoundation.unitCapModifier = Integer.parseInt(value);
                    } catch (Exception ignored) {
                        Log.warn("Argument is not a number");
                    }
                }
                case "coreUnitInc.nucleus" -> {
                    try {
                        Blocks.coreNucleus.unitCapModifier = Integer.parseInt(value);
                    } catch (Exception ignored) {
                        Log.warn("Argument is not a number");
                    }
                }
            }
        }

        for (var team : Team.all) {
            if (team.core() == null) continue;
            team.data().unitCap = 0;
        }

        Groups.build.each(x -> x.team().data().unitCap += x.block.unitCapModifier);
    }
}
