package org.l2j.gameserver.mobius.gameserver.network.clientpackets;

import org.l2j.gameserver.mobius.gameserver.Config;
import org.l2j.gameserver.mobius.gameserver.model.L2PremiumItem;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.mobius.gameserver.network.SystemMessageId;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.ExGetPremiumItemList;
import org.l2j.gameserver.mobius.gameserver.util.Util;

import java.nio.ByteBuffer;

/**
 * @author Gnacik
 */
public final class RequestWithDrawPremiumItem extends IClientIncomingPacket {
    private int _itemNum;
    private int _charId;
    private long _itemCount;

    @Override
    public void readImpl(ByteBuffer packet) {
        _itemNum = packet.getInt();
        _charId = packet.getInt();
        _itemCount = packet.getLong();
    }

    @Override
    public void runImpl() {
        final L2PcInstance activeChar = client.getActiveChar();

        if (activeChar == null) {
            return;
        } else if (_itemCount <= 0) {
            return;
        } else if (activeChar.getObjectId() != _charId) {
            Util.handleIllegalPlayerAction(activeChar, "[RequestWithDrawPremiumItem] Incorrect owner, Player: " + activeChar.getName(), Config.DEFAULT_PUNISH);
            return;
        } else if (activeChar.getPremiumItemList().isEmpty()) {
            Util.handleIllegalPlayerAction(activeChar, "[RequestWithDrawPremiumItem] Player: " + activeChar.getName() + " try to get item with empty list!", Config.DEFAULT_PUNISH);
            return;
        } else if ((activeChar.getWeightPenalty() >= 3) || !activeChar.isInventoryUnder90(false)) {
            client.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_THE_DIMENSIONAL_ITEM_BECAUSE_YOU_HAVE_EXCEED_YOUR_INVENTORY_WEIGHT_QUANTITY_LIMIT);
            return;
        } else if (activeChar.isProcessingTransaction()) {
            client.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_A_DIMENSIONAL_ITEM_DURING_AN_EXCHANGE);
            return;
        }

        final L2PremiumItem _item = activeChar.getPremiumItemList().get(_itemNum);
        if (_item == null) {
            return;
        } else if (_item.getCount() < _itemCount) {
            return;
        }

        final long itemsLeft = (_item.getCount() - _itemCount);

        activeChar.addItem("PremiumItem", _item.getItemId(), _itemCount, activeChar.getTarget(), true);

        if (itemsLeft > 0) {
            _item.updateCount(itemsLeft);
            activeChar.updatePremiumItem(_itemNum, itemsLeft);
        } else {
            activeChar.getPremiumItemList().remove(_itemNum);
            activeChar.deletePremiumItem(_itemNum);
        }

        if (activeChar.getPremiumItemList().isEmpty()) {
            client.sendPacket(SystemMessageId.THERE_ARE_NO_MORE_DIMENSIONAL_ITEMS_TO_BE_FOUND);
        } else {
            client.sendPacket(new ExGetPremiumItemList(activeChar));
        }
    }
}
