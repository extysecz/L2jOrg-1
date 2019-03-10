package org.l2j.gameserver.mobius.gameserver.model.items;

import org.l2j.gameserver.mobius.gameserver.model.StatsSet;
import org.l2j.gameserver.mobius.gameserver.model.items.type.ArmorType;

/**
 * This class is dedicated to the management of armors.
 */
public final class L2Armor extends L2Item {
    private ArmorType _type;

    /**
     * Constructor for Armor.
     *
     * @param set the StatsSet designating the set of couples (key,value) characterizing the armor.
     */
    public L2Armor(StatsSet set) {
        super(set);
    }

    @Override
    public void set(StatsSet set) {
        super.set(set);
        _type = set.getEnum("armor_type", ArmorType.class, ArmorType.NONE);

        final long _bodyPart = getBodyPart();
        if ((_bodyPart == L2Item.SLOT_NECK) || ((_bodyPart & L2Item.SLOT_L_EAR) != 0) || ((_bodyPart & L2Item.SLOT_L_FINGER) != 0) || ((_bodyPart & L2Item.SLOT_R_BRACELET) != 0) || ((_bodyPart & L2Item.SLOT_L_BRACELET) != 0) || ((_bodyPart & L2Item.SLOT_ARTIFACT_BOOK) != 0)) {
            _type1 = L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE;
            _type2 = L2Item.TYPE2_ACCESSORY;
        } else {
            if ((_type == ArmorType.NONE) && (getBodyPart() == L2Item.SLOT_L_HAND)) {
                _type = ArmorType.SHIELD;
            }
            _type1 = L2Item.TYPE1_SHIELD_ARMOR;
            _type2 = L2Item.TYPE2_SHIELD_ARMOR;
        }
    }

    /**
     * @return the type of the armor.
     */
    @Override
    public ArmorType getItemType() {
        return _type;
    }

    /**
     * @return the ID of the item after applying the mask.
     */
    @Override
    public final int getItemMask() {
        return _type.mask();
    }
}
