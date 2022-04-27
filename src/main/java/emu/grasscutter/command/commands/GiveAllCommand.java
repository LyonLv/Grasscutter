package emu.grasscutter.command.commands;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.def.AvatarData;
import emu.grasscutter.data.def.ItemData;
import emu.grasscutter.game.Player;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.inventory.ItemType;

import java.util.*;

@Command(label = "giveall", usage = "giveall [player] <amount>",
        description = "Gives all items", aliases = {"givea"}, permission = "player.giveall", threading = true)
public class GiveAllCommand implements CommandHandler {

    @Override
    public void execute(Player sender, List<String> args) {
        int target, amount = 99999;

        switch (args.size()) {
            case 0: // *no args*
                if (sender == null) {
                    CommandHandler.sendMessage(null, "This usage can only be run in-game");
                    return;
                }
                target = sender.getUid();
                break;

            case 1: // [player]
                try {
                    target = Integer.parseInt(args.get(0));
                    if (Grasscutter.getGameServer().getPlayerByUid(target) == null) {
                        CommandHandler.sendMessage(sender, "Invalid player ID.");
                        return;
                    }
                }catch (NumberFormatException ignored){
                    CommandHandler.sendMessage(sender, "Invalid player ID.");
                    return;
                }
                break;

            case 2: // [player] [amount]
                try {
                    target = Integer.parseInt(args.get(0));
                    if (Grasscutter.getGameServer().getPlayerByUid(target) == null) {
                        target = sender.getUid();
                        amount = Integer.parseInt(args.get(0));
                    } else {
                        amount = Integer.parseInt(args.get(1));
                    }
                } catch (NumberFormatException ignored) {
                    CommandHandler.sendMessage(sender, "Invalid amount or player ID.");
                    return;
                }
                break;

            default: // invalid
                CommandHandler.sendMessage(null, "Usage: giveall [player] <amount>");
                return;
        }

        Player targetPlayer = Grasscutter.getGameServer().getPlayerByUid(target);
        if (targetPlayer == null) {
            CommandHandler.sendMessage(sender, "Player not found.");
            return;
        }

        this.giveAllItems(targetPlayer, amount);
        CommandHandler.sendMessage(sender, "Giving all items done");
    }

    public void giveAllItems(Player player, int amount) {
        CommandHandler.sendMessage(player, "Giving all items...");

        for (AvatarData avatarData: GameData.getAvatarDataMap().values()) {
            //Exclude test avatar
            if (isTestAvatar(avatarData.getId())) continue;

            Avatar avatar = new Avatar(avatarData);
            avatar.setLevel(90);
            avatar.setPromoteLevel(6);
            for (int i = 1; i <= 6; ++i) {
                avatar.getTalentIdList().add((avatar.getAvatarId() - 10000000) * 10 + i);
            }
            // This will handle stats and talents
            avatar.recalcStats();
            player.addAvatar(avatar);
        }

        //some test items
        List<GameItem> itemList = new ArrayList<>();
        for (ItemData itemdata: GameData.getItemDataMap().values()) {
            //Exclude test item
            if (isTestItem(itemdata.getId())) continue;

            if (itemdata.isEquip()) {
                for (int i = 0; i < 10; ++i) {
                    GameItem item = new GameItem(itemdata);
                    if (itemdata.getItemType() == ItemType.ITEM_WEAPON) {
                        item.setLevel(90);
                        item.setPromoteLevel(6);
                        item.setRefinement(4);
                    }
                    itemList.add(item);
                }
            }
            else {
                GameItem item = new GameItem(itemdata);
                item.setCount(amount);
                itemList.add(item);
            }
        }
        int packetNum = 20;
        int itemLength = itemList.size();
        int number = itemLength / packetNum;
        int remainder = itemLength % packetNum;
        int offset = 0;
        for (int i = 0; i < packetNum; ++i) {
            if (remainder > 0) {
                player.getInventory().addItems(itemList.subList(i * number + offset, (i + 1) * number + offset + 1));
                --remainder;
                ++offset;
            }
            else {
                player.getInventory().addItems(itemList.subList(i * number + offset, (i + 1) * number + offset));
            }
        }
    }

    public boolean isTestAvatar(int avatarId) {
        return avatarId < 10000002 || avatarId >= 11000000;
    }

    public boolean isTestItem(int itemId) {
        for (Range range: testItemRanges) {
            if (range.check(itemId)) {
                return true;
            }
        }

        if (testItemsList.contains(itemId)) {
            return true;
        }

        return false;
    }

    static class Range {
        private int min;
        private int max;

        public Range(int min, int max) {
            if(min > max){
                min ^= max;
                max ^= min;
                min ^= max;
            }
            this.min = min;
            this.max = max;
        }

        public boolean check(int value) {
            return value >= this.min && value <= this.max;
        }
    }

    private static final Range[] testItemRanges = new Range[] {
            new Range(106, 139),
            new Range(1000, 1099),
            new Range(2001, 2008),
            new Range(2017, 2029),
          //  new Range(108001, 108387) //food
    };

    private static final Integer[] testItemsIds = new Integer[] {
            210, 211, 314, 315, 317, 1005, 1007, 1105, 1107, 1201, 1202, 2800,
            100001, 100002, 100244, 100305, 100312, 100313, 101212, 11411, 11506, 11507, 11508, 12505,
            12506, 12508, 12509, 13503, 13506, 14411, 14503, 14505, 14508, 15411, 15504, 15505,
            15506, 15508, 20001, 10002, 10003, 10004, 10005, 10006, 10008 //9
    };

    private static final Collection<Integer> testItemsList = Arrays.asList(testItemsIds);

}

