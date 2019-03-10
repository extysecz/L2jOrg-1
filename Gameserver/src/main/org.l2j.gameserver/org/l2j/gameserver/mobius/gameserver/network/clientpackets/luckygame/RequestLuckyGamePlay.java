package org.l2j.gameserver.mobius.gameserver.network.clientpackets.luckygame;

import org.l2j.commons.util.CommonUtil;
import org.l2j.commons.util.Rnd;
import org.l2j.gameserver.mobius.gameserver.data.xml.impl.LuckyGameData;
import org.l2j.gameserver.mobius.gameserver.datatables.ItemTable;
import org.l2j.gameserver.mobius.gameserver.enums.LuckyGameItemType;
import org.l2j.gameserver.mobius.gameserver.enums.LuckyGameResultType;
import org.l2j.gameserver.mobius.gameserver.enums.LuckyGameType;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.mobius.gameserver.model.holders.ItemChanceHolder;
import org.l2j.gameserver.mobius.gameserver.model.holders.ItemHolder;
import org.l2j.gameserver.mobius.gameserver.model.holders.LuckyGameDataHolder;
import org.l2j.gameserver.mobius.gameserver.model.items.instance.L2ItemInstance;
import org.l2j.gameserver.mobius.gameserver.model.variables.PlayerVariables;
import org.l2j.gameserver.mobius.gameserver.network.SystemMessageId;
import org.l2j.gameserver.mobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.SystemMessage;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.luckygame.ExBettingLuckyGameResult;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author Sdw
 */
public class RequestLuckyGamePlay extends IClientIncomingPacket {
    private static final int FORTUNE_READING_TICKET = 23767;
    private static final int LUXURY_FORTUNE_READING_TICKET = 23768;
    private LuckyGameType _type;
    private int _reading;

    @Override
    public void readImpl(ByteBuffer packet) {
        final int type = CommonUtil.constrain(packet.getInt(), 0, LuckyGameType.values().length);
        _type = LuckyGameType.values()[type];
        _reading = CommonUtil.constrain(packet.getInt(), 0, 50); // max play is 50
    }

    @Override
    public void runImpl() {
        final L2PcInstance player = client.getActiveChar();
        if (player == null) {
            return;
        }

        final int index = _type == LuckyGameType.LUXURY ? 102 : 2; // move to event config

        final LuckyGameDataHolder holder = LuckyGameData.getInstance().getLuckyGameDataByIndex(index);
        if (holder == null) {
            return;
        }

        final long tickets = _type == LuckyGameType.LUXURY ? player.getInventory().getInventoryItemCount(LUXURY_FORTUNE_READING_TICKET, -1) : player.getInventory().getInventoryItemCount(FORTUNE_READING_TICKET, -1);
        if (tickets < _reading) {
            player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_TICKETS_YOU_CANNOT_CONTINUE_THE_GAME);
            player.sendPacket(_type == LuckyGameType.LUXURY ? ExBettingLuckyGameResult.LUXURY_INVALID_ITEM_COUNT : ExBettingLuckyGameResult.NORMAL_INVALID_ITEM_COUNT);
            return;
        }

        int playCount = player.getVariables().getInt(PlayerVariables.FORTUNE_TELLING_VARIABLE, 0);
        boolean blackCat = player.getVariables().getBoolean(PlayerVariables.FORTUNE_TELLING_BLACK_CAT_VARIABLE, false);
        final EnumMap<LuckyGameItemType, List<ItemHolder>> rewards = new EnumMap<>(LuckyGameItemType.class);
        for (int i = 0; i < _reading; i++) {
            final double chance = 100 * Rnd.nextDouble();
            double totalChance = 0;

            for (ItemChanceHolder item : holder.getCommonReward()) {
                totalChance += item.getChance();
                if (totalChance >= chance) {
                    rewards.computeIfAbsent(LuckyGameItemType.COMMON, k -> new ArrayList<>()).add(item);
                    break;
                }
            }
            playCount++;
            if ((playCount >= holder.getMinModifyRewardGame()) && (playCount <= holder.getMaxModifyRewardGame()) && !blackCat) {
                final List<ItemChanceHolder> modifyReward = holder.getModifyReward();
                final double chanceModify = 100 * Rnd.nextDouble();
                totalChance = 0;

                for (ItemChanceHolder item : modifyReward) {
                    totalChance += item.getChance();
                    if (totalChance >= chanceModify) {
                        rewards.computeIfAbsent(LuckyGameItemType.RARE, k -> new ArrayList<>()).add(item);
                        blackCat = true;
                        break;
                    }
                }

                if (playCount == holder.getMaxModifyRewardGame()) {
                    rewards.computeIfAbsent(LuckyGameItemType.RARE, k -> new ArrayList<>()).add(modifyReward.get(Rnd.get(modifyReward.size())));
                    blackCat = true;
                }
            }
        }

        final int totalWeight = rewards.values().stream().mapToInt(list -> list.stream().mapToInt(item -> ItemTable.getInstance().getTemplate(item.getId()).getWeight()).sum()).sum();

        // Check inventory capacity
        if ((rewards.size() > 0) && (!player.getInventory().validateCapacity(rewards.size()) || !player.getInventory().validateWeight(totalWeight))) {
            player.sendPacket(_type == LuckyGameType.LUXURY ? ExBettingLuckyGameResult.LUXURY_INVALID_CAPACITY : ExBettingLuckyGameResult.NORMAL_INVALID_CAPACITY);
            player.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_EITHER_FULL_OR_OVERWEIGHT);
            return;
        }

        if (!player.destroyItemByItemId("LuckyGame", _type == LuckyGameType.LUXURY ? LUXURY_FORTUNE_READING_TICKET : FORTUNE_READING_TICKET, _reading, player, true)) {
            player.sendPacket(_type == LuckyGameType.LUXURY ? ExBettingLuckyGameResult.LUXURY_INVALID_ITEM_COUNT : ExBettingLuckyGameResult.NORMAL_INVALID_ITEM_COUNT);
            return;
        }

        for (int i = 0; i < _reading; i++) {
            final int serverGameNumber = LuckyGameData.getInstance().increaseGame();
            holder.getUniqueReward().stream().filter(reward -> reward.getPoints() == serverGameNumber).forEach(item -> rewards.computeIfAbsent(LuckyGameItemType.UNIQUE, k -> new ArrayList<>()).add(item));
        }

        player.sendPacket(new ExBettingLuckyGameResult(LuckyGameResultType.SUCCESS, _type, rewards, (int) (_type == LuckyGameType.LUXURY ? player.getInventory().getInventoryItemCount(LUXURY_FORTUNE_READING_TICKET, -1) : player.getInventory().getInventoryItemCount(FORTUNE_READING_TICKET, -1))));

        final InventoryUpdate iu = new InventoryUpdate();
        for (Entry<LuckyGameItemType, List<ItemHolder>> reward : rewards.entrySet()) {
            for (ItemHolder r : reward.getValue()) {
                final L2ItemInstance item = player.addItem("LuckyGame", r.getId(), r.getCount(), player, true);
                iu.addItem(item);
                if (reward.getKey() == LuckyGameItemType.UNIQUE) {
                    final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CONGRATULATIONS_C1_HAS_OBTAINED_S2_OF_S3_THROUGH_FORTUNE_READING);
                    sm.addPcName(player);
                    sm.addLong(r.getCount());
                    sm.addItemName(item);
                    player.broadcastPacket(sm, 1000);
                    break;
                }

            }
        }

        player.sendInventoryUpdate(iu);

        player.getVariables().set(PlayerVariables.FORTUNE_TELLING_VARIABLE, playCount >= 50 ? (playCount - 50) : playCount);
        if (blackCat && (playCount < 50)) {
            player.getVariables().set(PlayerVariables.FORTUNE_TELLING_BLACK_CAT_VARIABLE, true);
        }
    }
}
