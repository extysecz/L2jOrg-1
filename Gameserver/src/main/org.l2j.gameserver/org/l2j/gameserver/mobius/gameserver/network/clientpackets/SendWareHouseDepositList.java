package org.l2j.gameserver.mobius.gameserver.network.clientpackets;

import org.l2j.gameserver.mobius.gameserver.Config;
import org.l2j.gameserver.mobius.gameserver.model.actor.L2Npc;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.mobius.gameserver.model.holders.ItemHolder;
import org.l2j.gameserver.mobius.gameserver.model.itemcontainer.ItemContainer;
import org.l2j.gameserver.mobius.gameserver.model.itemcontainer.PcWarehouse;
import org.l2j.gameserver.mobius.gameserver.model.items.instance.L2ItemInstance;
import org.l2j.gameserver.mobius.gameserver.network.InvalidDataPacketException;
import org.l2j.gameserver.mobius.gameserver.network.SystemMessageId;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2j.gameserver.mobius.gameserver.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.l2j.gameserver.mobius.gameserver.model.itemcontainer.Inventory.ADENA_ID;

/**
 * SendWareHouseDepositList client packet class.
 */
public final class SendWareHouseDepositList extends IClientIncomingPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendWareHouseDepositList.class);
    private static final int BATCH_LENGTH = 12;

    private List<ItemHolder> _items = null;

    @Override
    public void readImpl(ByteBuffer packet) throws InvalidDataPacketException {
        final int size = packet.getInt();
        if ((size <= 0) || (size > Config.MAX_ITEM_IN_PACKET) || ((size * BATCH_LENGTH) != packet.remaining())) {
            throw new InvalidDataPacketException();
        }

        _items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            final int objId = packet.getInt();
            final long count = packet.getLong();
            if ((objId < 1) || (count < 0)) {
                _items = null;
                throw new InvalidDataPacketException();
            }
            _items.add(new ItemHolder(objId, count));
        }
    }

    @Override
    public void runImpl() {
        if (_items == null) {
            return;
        }

        final L2PcInstance player = client.getActiveChar();
        if (player == null) {
            return;
        }

        if (!client.getFloodProtectors().getTransaction().tryPerformAction("deposit")) {
            player.sendMessage("You are depositing items too fast.");
            return;
        }

        final ItemContainer warehouse = player.getActiveWarehouse();
        if (warehouse == null) {
            return;
        }
        final boolean isPrivate = warehouse instanceof PcWarehouse;

        final L2Npc manager = player.getLastFolkNPC();
        if (((manager == null) || !manager.isWarehouse() || !manager.canInteract(player)) && !player.isGM()) {
            return;
        }

        if (!isPrivate && !player.getAccessLevel().allowTransaction()) {
            player.sendMessage("Transactions are disabled for your Access Level.");
            return;
        }

        if (player.hasItemRequest()) {
            Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to use enchant Exploit!", Config.DEFAULT_PUNISH);
            return;
        }

        // Alt game - Karma punishment
        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && (player.getReputation() < 0)) {
            return;
        }

        // Freight price from config or normal price per item slot (30)
        final long fee = _items.size() * 30;
        long currentAdena = player.getAdena();
        int slots = 0;

        for (ItemHolder i : _items) {
            final L2ItemInstance item = player.checkItemManipulation(i.getId(), i.getCount(), "deposit");
            if (item == null) {
                LOGGER.warn("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
                return;
            }

            // Calculate needed adena and slots
            if (item.getId() == ADENA_ID) {
                currentAdena -= i.getCount();
            }
            if (!item.isStackable()) {
                slots += i.getCount();
            } else if (warehouse.getItemByItemId(item.getId()) == null) {
                slots++;
            }
        }

        // Item Max Limit Check
        if (!warehouse.validateCapacity(slots)) {
            client.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
            return;
        }

        // Check if enough adena and charge the fee
        if ((currentAdena < fee) || !player.reduceAdena(warehouse.getName(), fee, manager, false)) {
            client.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            return;
        }

        // get current tradelist if any
        if (player.getActiveTradeList() != null) {
            return;
        }

        // Proceed to the transfer
        final InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
        for (ItemHolder i : _items) {
            // Check validity of requested item
            final L2ItemInstance oldItem = player.checkItemManipulation(i.getId(), i.getCount(), "deposit");
            if (oldItem == null) {
                LOGGER.warn("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
                return;
            }

            if (!oldItem.isDepositable(isPrivate) || !oldItem.isAvailable(player, true, isPrivate)) {
                continue;
            }

            final L2ItemInstance newItem = player.getInventory().transferItem(warehouse.getName(), i.getId(), i.getCount(), warehouse, player, manager);
            if (newItem == null) {
                LOGGER.warn("Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
                continue;
            }

            if (playerIU != null) {
                if ((oldItem.getCount() > 0) && (oldItem != newItem)) {
                    playerIU.addModifiedItem(oldItem);
                } else {
                    playerIU.addRemovedItem(oldItem);
                }
            }
        }

        // Send updated item list to the player
        if (playerIU != null) {
            player.sendInventoryUpdate(playerIU);
        } else {
            player.sendItemList();
        }
    }
}
