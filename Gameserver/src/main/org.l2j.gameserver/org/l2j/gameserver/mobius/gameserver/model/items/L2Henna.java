package org.l2j.gameserver.mobius.gameserver.model.items;

import org.l2j.gameserver.mobius.gameserver.model.StatsSet;
import org.l2j.gameserver.mobius.gameserver.model.base.ClassId;
import org.l2j.gameserver.mobius.gameserver.model.skills.Skill;
import org.l2j.gameserver.mobius.gameserver.model.stats.BaseStats;
import org.l2j.gameserver.mobius.gameserver.model.stats.Stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for the Henna object.
 *
 * @author Zoey76
 */
public class L2Henna {
    private final int _dyeId;
    private final int _dyeItemId;
    private final Map<BaseStats, Integer> _baseStats = new HashMap<>();
    private final int _wear_fee;
    private final int _wear_count;
    private final int _cancel_fee;
    private final int _cancel_count;
    private final int _duration;
    private final List<Skill> _skills;
    private final List<ClassId> _wear_class;

    public L2Henna(StatsSet set) {
        _dyeId = set.getInt("dyeId");
        _dyeItemId = set.getInt("dyeItemId");
        _baseStats.put(BaseStats.STR, set.getInt("str", 0));
        _baseStats.put(BaseStats.CON, set.getInt("con", 0));
        _baseStats.put(BaseStats.DEX, set.getInt("dex", 0));
        _baseStats.put(BaseStats.INT, set.getInt("int", 0));
        _baseStats.put(BaseStats.MEN, set.getInt("men", 0));
        _baseStats.put(BaseStats.WIT, set.getInt("wit", 0));
        _wear_fee = set.getInt("wear_fee");
        _wear_count = set.getInt("wear_count");
        _cancel_fee = set.getInt("cancel_fee");
        _cancel_count = set.getInt("cancel_count");
        _duration = set.getInt("duration", -1);
        _skills = new ArrayList<>();
        _wear_class = new ArrayList<>();
    }

    /**
     * @return the dye Id.
     */
    public int getDyeId() {
        return _dyeId;
    }

    /**
     * @return the item Id, required for this dye.
     */
    public int getDyeItemId() {
        return _dyeItemId;
    }

    public int getBaseStats(Stats stat) {
        return _baseStats.getOrDefault(stat, 0);
    }

    public Map<BaseStats, Integer> getBaseStats() {
        return _baseStats;
    }

    /**
     * @return the wear fee, cost for adding this dye to the player.
     */
    public int getWearFee() {
        return _wear_fee;
    }

    /**
     * @return the wear count, the required count to add this dye to the player.
     */
    public int getWearCount() {
        return _wear_count;
    }

    /**
     * @return the cancel fee, cost for removing this dye from the player.
     */
    public int getCancelFee() {
        return _cancel_fee;
    }

    /**
     * @return the cancel count, the retrieved amount of dye items after removing the dye.
     */
    public int getCancelCount() {
        return _cancel_count;
    }

    /**
     * @return the duration of this dye.
     */
    public int getDuration() {
        return _duration;
    }

    /**
     * @return the skills related to this dye.
     */
    public List<Skill> getSkills() {
        return _skills;
    }

    /**
     * @param skillList the list of skills related to this dye.
     */
    public void setSkills(List<Skill> skillList) {
        _skills.addAll(skillList);
    }

    /**
     * @return the list with the allowed classes to wear this dye.
     */
    public List<ClassId> getAllowedWearClass() {
        return _wear_class;
    }

    /**
     * @param c the class trying to wear this dye.
     * @return {@code true} if the player is allowed to wear this dye, {@code false} otherwise.
     */
    public boolean isAllowedClass(ClassId c) {
        return _wear_class.contains(c);
    }

    /**
     * @param wearClassIds the list of classes that can wear this dye.
     */
    public void setWearClassIds(List<ClassId> wearClassIds) {
        _wear_class.addAll(wearClassIds);
    }
}