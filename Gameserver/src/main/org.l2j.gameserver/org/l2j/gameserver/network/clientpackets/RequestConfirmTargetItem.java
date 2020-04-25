package org.l2j.gameserver.network.clientpackets;

import org.l2j.gameserver.data.xml.impl.VariationData;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.model.item.instance.Item;
import org.l2j.gameserver.network.SystemMessageId;
import org.l2j.gameserver.network.serverpackets.ExPutItemResultForVariationMake;

/**
 * Format:(ch) d
 *
 * @author -Wooden-
 */
public final class RequestConfirmTargetItem extends AbstractRefinePacket {
    private int _itemObjId;

    @Override
    public void readImpl() {
        _itemObjId = readInt();
    }

    @Override
    public void runImpl() {
        final Player activeChar = client.getPlayer();
        if (activeChar == null) {
            return;
        }

        final Item item = activeChar.getInventory().getItemByObjectId(_itemObjId);
        if (item == null) {
            return;
        }

        if (!VariationData.getInstance().hasFeeData(item.getId())) {
            client.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
            return;
        }

        if (!isValid(activeChar, item)) {
            // Different system message here
            if (item.isAugmented()) {
                client.sendPacket(SystemMessageId.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN);
                return;
            }

            client.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
            return;
        }

        client.sendPacket(new ExPutItemResultForVariationMake(_itemObjId, item.getId()));
    }
}
